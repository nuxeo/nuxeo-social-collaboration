/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * Contributors:
 * Nuxeo - initial API and implementation
 */

package org.nuxeo.ecm.social.workspace.listeners;

import static org.nuxeo.ecm.social.workspace.SocialConstants.PUBLIC_SOCIAL_SECTION_NAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_SECTION_NAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_ACL_NAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_FACET;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.platform.usermanager.exceptions.GroupAlreadyExistsException;
import org.nuxeo.ecm.social.workspace.SocialWorkspaceHelper;
import org.nuxeo.runtime.api.Framework;

/**
 * Class to handle social workspace document creation. It automatically creates two
 * groups :
 * <ul>
 * <li>{doc_id}_administrators : with an EVERYTHING permission</li>
 * <li>{doc_id}_members : with a READ_WRITE permission</li>
 * </ul>
 * 
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.4.1
 */
public class CreateSocialWorkspaceGroupListener implements EventListener {

    protected UserManager userManager;

    private static final Log log = LogFactory.getLog(CreateSocialWorkspaceGroupListener.class);

    @Override
    public void handleEvent(Event event) throws ClientException {
        if (!event.getName().equals(DocumentEventTypes.DOCUMENT_CREATED)) {
            return;
        }

        if (!(event.getContext() instanceof DocumentEventContext)) {
            return;
        }

        DocumentEventContext ctx = (DocumentEventContext) event.getContext();
        DocumentModel doc = ctx.getSourceDocument();

        if (!doc.hasFacet(SOCIAL_WORKSPACE_FACET)) {
            return;
        }
        ACP acp = doc.getACP();
        acp.addACL(createSocialWorkspaceACL(acp, doc));
        doc.setACP(acp, true);

        CoreSession session = ctx.getCoreSession();
        session.saveDocument(doc);

        handleACPOnSocialSections(doc, session);

        createGroup(
                SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupName(doc),
                SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupLabel(doc),
                ctx.getPrincipal().getName());
        createGroup(SocialWorkspaceHelper.getSocialWorkspaceMembersGroupName(doc),
                SocialWorkspaceHelper.getSocialWorkspaceMembersGroupLabel(doc), null);
    }

    /**
     * Create a group, and if exists add the principal as a member.
     * 
     * @param groupName group name you want to create
     * @param groupLabel group label that can be null
     * @param principal null is you do not want to add a member
     */
    protected void createGroup(String groupName, String groupLabel,
            String principal) throws ClientException {
        UserManager userManager = getUserManager();
        try {
            String groupSchemaName = userManager.getGroupSchemaName();

            DocumentModel group = userManager.getBareGroupModel();
            group.setProperty(groupSchemaName, userManager.getGroupIdField(),
                    groupName);
            group.setProperty(groupSchemaName,
                    userManager.getGroupLabelField(), groupLabel);
            group = userManager.createGroup(group);

            if (!(principal == null || "".equals(principal))) {
                group.setProperty(groupSchemaName,
                        userManager.getGroupMembersField(),
                        Arrays.asList(principal));
            }
            userManager.updateGroup(group);
        } catch (GroupAlreadyExistsException e) {
            log.info("Group already exists : " + groupName);
        } catch (ClientException e) {
            log.error("Cannot create group " + groupName, e);
        }
    }

    protected ACL createSocialWorkspaceACL(ACP acp, DocumentModel doc) throws ClientException {
        ACL acl = acp.getOrCreateACL(SOCIAL_WORKSPACE_ACL_NAME);
        grantSpecificSocialWorkspaceRights(doc, acl);
        acl.add(new ACE(SecurityConstants.EVERYONE,
                SecurityConstants.EVERYTHING, false));
        return acl;
    }

    protected UserManager getUserManager() throws ClientException {
        if (userManager == null) {
            try {
                userManager = Framework.getService(UserManager.class);
            } catch (Exception e) {
                log.error("Cannot instantiate userManager", e);
                throw new ClientException(e);
            }
        }
        return userManager;
    }

    protected void handleACPOnSocialSections(DocumentModel socialWorkspace,
            CoreSession session) throws ClientException {
        DocumentModel socialSection = session.getChild(
                socialWorkspace.getRef(), SOCIAL_SECTION_NAME);

        DocumentModel publicSocialSection = session.getChild(
                socialSection.getRef(), PUBLIC_SOCIAL_SECTION_NAME);
        grantReadRightForEveryOneOnSocialSection(socialWorkspace, session,
                publicSocialSection);
    }

    protected void grantSpecificSocialWorkspaceRights(
            DocumentModel socialWorkspace, ACL acl) throws ClientException {
        grantEverythingToAdministrator(acl);
        acl.add(new ACE(
                SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupName(socialWorkspace),
                SecurityConstants.EVERYTHING, true));
        acl.add(new ACE(
                SocialWorkspaceHelper.getSocialWorkspaceMembersGroupName(socialWorkspace),
                SecurityConstants.READ_WRITE, true));

    }

    protected void grantEverythingToAdministrator(ACL acl)
            throws ClientException {
        for (String adminGroup : getUserManager().getAdministratorsGroups()) {
            acl.add(new ACE(adminGroup, SecurityConstants.EVERYTHING, true));
        }
    }

    protected void grantReadRightForEveryOneOnSocialSection(
            DocumentModel socialWorkspace, CoreSession session,
            DocumentModel publicSocialSection) throws ClientException {

        ACP acpPublicSection = new ACPImpl();

        ACL aclPublicSection = acpPublicSection.getOrCreateACL(SOCIAL_WORKSPACE_ACL_NAME);
        grantSpecificSocialWorkspaceRights(socialWorkspace, aclPublicSection);
        grantPublicReadRight(aclPublicSection);

        acpPublicSection.addACL(aclPublicSection);
        publicSocialSection.setACP(acpPublicSection, true);

        session.saveDocument(publicSocialSection);
    }

    protected void grantPublicReadRight(ACL aclPublicSection)
            throws ClientException {
        String defaultGroupName = getUserManager().getDefaultGroup();
        if (defaultGroupName == null) {
            defaultGroupName = SecurityConstants.EVERYONE;
        }
        aclPublicSection.add(new ACE(defaultGroupName, SecurityConstants.READ,
                true));
    }

}
