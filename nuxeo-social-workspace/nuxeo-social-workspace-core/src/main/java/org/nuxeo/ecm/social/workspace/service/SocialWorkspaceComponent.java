/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Contributors:
 *      Nuxeo
 */

package org.nuxeo.ecm.social.workspace.service;

import static org.nuxeo.ecm.core.api.security.SecurityConstants.EVERYONE;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.WRITE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_SOCIAL_WORKSPACE_IS_PUBLIC;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.platform.usermanager.exceptions.GroupAlreadyExistsException;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.listeners.SocialWorkspaceListener;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * Default implementation of {@see SocialWorkspaceService} service.
 */
public class SocialWorkspaceComponent extends DefaultComponent implements
        SocialWorkspaceService {

    private static final Log log = LogFactory.getLog(SocialWorkspaceComponent.class);

    public static final String CONFIGURATION_EP = "configuration";

    public static final String SOCIAL_WORKSPACE_ACL_NAME = "socialWorkspaceAcl";

    public static final String NEWS_ITEMS_ROOT_ACL_NAME = "newsItemsRootAcl";

    public static final String PUBLIC_SOCIAL_WORKSPACE_ACL_NAME = "publicSocialWorkspaceAcl";

    private UserManager userManager;

    private int validationDays = 15;

    @Override
    public int getValidationDays() {
        return validationDays;
    }

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {
        if (CONFIGURATION_EP.equals(extensionPoint)) {
            ConfigurationDescriptor config = (ConfigurationDescriptor) contribution;
            if (config.getValidationTimeInDays() > 0) {
                validationDays = config.getValidationTimeInDays();
            }
        }
    }

    @Override
    public void initializeSocialWorkspace(SocialWorkspace socialWorkspace,
            String principalName) {
        createSocialWorkspaceGroups(socialWorkspace, principalName);
        initializeSocialWorkspaceRights(socialWorkspace);
        initializeNewsItemsRootRights(socialWorkspace);
        if (socialWorkspace.isPublic()) {
            makeSocialWorkspacePublic(socialWorkspace);
        }
    }

    private void createSocialWorkspaceGroups(SocialWorkspace socialWorkspace,
            String principalName) {
        createGroup(socialWorkspace.getAdministratorsGroupName(),
                socialWorkspace.getAdministratorsGroupLabel(), principalName);
        createGroup(socialWorkspace.getMembersGroupName(),
                socialWorkspace.getMembersGroupLabel(), principalName);
    }

    private void createGroup(String groupName, String groupLabel,
            String principal) {
        UserManager userManager = getUserManager();
        try {
            String groupSchemaName = userManager.getGroupSchemaName();

            DocumentModel group = userManager.getBareGroupModel();
            group.setProperty(groupSchemaName, userManager.getGroupIdField(),
                    groupName);
            group.setProperty(groupSchemaName,
                    userManager.getGroupLabelField(), groupLabel);
            group = userManager.createGroup(group);

            if (!StringUtils.isBlank(principal)) {
                group.setProperty(groupSchemaName,
                        userManager.getGroupMembersField(),
                        Arrays.asList(principal));
            }
            userManager.updateGroup(group);
        } catch (GroupAlreadyExistsException e) {
            log.info("Group already exists : " + groupName);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    protected UserManager getUserManager() {
        if (userManager == null) {
            try {
                userManager = Framework.getService(UserManager.class);
            } catch (Exception e) {
                throw new ClientRuntimeException(e);
            }
        }
        return userManager;
    }

    private void initializeSocialWorkspaceRights(SocialWorkspace socialWorkspace) {
        try {
            DocumentModel doc = socialWorkspace.getDocument();
            ACP acp = doc.getACP();
            addSocialWorkspaceACL(acp, socialWorkspace);
            doc.setACP(acp, true);
            doc.getCoreSession().saveDocument(doc);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    private void addSocialWorkspaceACL(ACP acp, SocialWorkspace socialWorkspace) {
        ACL acl = acp.getOrCreateACL(SOCIAL_WORKSPACE_ACL_NAME);
        addEverythingForAdministratorsACE(acl);
        acl.add(new ACE(socialWorkspace.getAdministratorsGroupName(),
                SecurityConstants.EVERYTHING, true));
        acl.add(new ACE(socialWorkspace.getMembersGroupName(),
                SecurityConstants.READ_WRITE, true));
        acl.add(new ACE(EVERYONE, SecurityConstants.EVERYTHING, false));
        acp.addACL(acl);
    }

    private void addEverythingForAdministratorsACE(ACL acl) {
        for (String adminGroup : getUserManager().getAdministratorsGroups()) {
            acl.add(new ACE(adminGroup, SecurityConstants.EVERYTHING, true));
        }
    }

    private void initializeNewsItemsRootRights(SocialWorkspace socialWorkspace) {
        try {
            CoreSession session = socialWorkspace.getDocument().getCoreSession();
            PathRef newsItemsRootPath = new PathRef(
                    socialWorkspace.getNewsItemsRootPath());
            DocumentModel newsItemsRoot = session.getDocument(newsItemsRootPath);

            ACP acp = newsItemsRoot.getACP();
            ACL acl = acp.getOrCreateACL(NEWS_ITEMS_ROOT_ACL_NAME);
            acl.add(new ACE(socialWorkspace.getMembersGroupName(), WRITE, false));
            newsItemsRoot.setACP(acp, true);
            session.saveDocument(newsItemsRoot);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @Override
    public void makeSocialWorkspacePublic(SocialWorkspace socialWorkspace) {
        try {
            DocumentModel doc = socialWorkspace.getDocument();
            doc.setPropertyValue(FIELD_SOCIAL_WORKSPACE_IS_PUBLIC, true);
            doc.putContextData(SocialWorkspaceListener.DO_NOT_PROCESS, true);

            CoreSession session = doc.getCoreSession();
            addReadRightOnPublicSection(session, socialWorkspace);
            addReadRightOnPublicDashboard(session, socialWorkspace);
            doc = session.saveDocument(doc);
            session.save();
            socialWorkspace.setDocument(doc);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    private void addReadRightOnPublicSection(CoreSession session,
            SocialWorkspace socialWorkspace) throws ClientException {
        PathRef publicSectionRef = new PathRef(
                socialWorkspace.getPublicSectionPath());
        if (!session.exists(publicSectionRef)) {
            return;
        }
        DocumentModel publicSection = session.getDocument(publicSectionRef);

        ACP acp = publicSection.getACP();
        ACL acl = acp.getOrCreateACL(PUBLIC_SOCIAL_WORKSPACE_ACL_NAME);
        acl.clear();
        String defaultGroup = userManager.getDefaultGroup();
        defaultGroup = defaultGroup == null ? EVERYONE : defaultGroup;
        acl.add(new ACE(defaultGroup, READ, true));
        publicSection.setACP(acp, true);
        session.saveDocument(publicSection);
    }

    private void addReadRightOnPublicDashboard(CoreSession session,
            SocialWorkspace socialWorkspace) throws ClientException {
        PathRef publicDashboardSpaceRef = new PathRef(
                socialWorkspace.getPublicDashboardSpacePath());
        if (!session.exists(publicDashboardSpaceRef)) {
            return;
        }
        DocumentModel publicDashboardSpace = session.getDocument(publicDashboardSpaceRef);

        ACP acp = publicDashboardSpace.getACP();
        ACL acl = acp.getOrCreateACL(PUBLIC_SOCIAL_WORKSPACE_ACL_NAME);
        acl.clear();
        String defaultGroup = userManager.getDefaultGroup();
        defaultGroup = defaultGroup == null ? EVERYONE : defaultGroup;
        acl.add(new ACE(defaultGroup, READ, true));
        publicDashboardSpace.setACP(acp, true);
        session.saveDocument(publicDashboardSpace);
    }

    @Override
    public void makeSocialWorkspacePrivate(SocialWorkspace socialWorkspace) {
        try {
            DocumentModel doc = socialWorkspace.getDocument();
            doc.setPropertyValue(FIELD_SOCIAL_WORKSPACE_IS_PUBLIC, false);
            doc.putContextData(SocialWorkspaceListener.DO_NOT_PROCESS, true);

            CoreSession session = doc.getCoreSession();
            removeReadRightOnPublicSection(session, socialWorkspace);
            removeReadRightOnPublicDashboard(session, socialWorkspace);
            doc = session.saveDocument(doc);
            session.save();
            socialWorkspace.setDocument(doc);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    private void removeReadRightOnPublicSection(CoreSession session,
            SocialWorkspace socialWorkspace) throws ClientException {
        PathRef publicSectionRef = new PathRef(
                socialWorkspace.getPublicSectionPath());
        if (!session.exists(publicSectionRef)) {
            log.warn("The public section '" + publicSectionRef.toString()
                    + "' does not exist");
            return;
        }
        DocumentModel publicSection = session.getDocument(publicSectionRef);

        ACP acp = publicSection.getACP();
        acp.removeACL(PUBLIC_SOCIAL_WORKSPACE_ACL_NAME);
        publicSection.setACP(acp, true);
        session.saveDocument(publicSection);
    }

    private void removeReadRightOnPublicDashboard(CoreSession session,
            SocialWorkspace socialWorkspace) throws ClientException {
        PathRef publicDashboardSpaceRef = new PathRef(
                socialWorkspace.getPublicSectionPath());
        if (!session.exists(publicDashboardSpaceRef)) {
            log.warn("The public dashboard space '"
                    + publicDashboardSpaceRef.toString() + "' does not exist");
            return;
        }
        DocumentModel publicDashboardSpace = session.getDocument(publicDashboardSpaceRef);

        ACP acp = publicDashboardSpace.getACP();
        acp.removeACL(PUBLIC_SOCIAL_WORKSPACE_ACL_NAME);
        publicDashboardSpace.setACP(acp, true);
        session.saveDocument(publicDashboardSpace);
    }

}
