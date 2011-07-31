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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.platform.usermanager.exceptions.GroupAlreadyExistsException;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.adapters.SubscriptionRequest;
import org.nuxeo.ecm.social.workspace.listeners.SocialWorkspaceListener;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

import static org.nuxeo.ecm.core.api.security.SecurityConstants.EVERYONE;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.WRITE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_IS_PUBLIC_PROPERTY;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_FACET;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.isSocialWorkspace;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialWorkspace;

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

    private SubscriptionRequestHandler subscriptionRequestHandler;

    private int validationDays = 15;

    @Override
    public List<SocialWorkspace> getDetachedPublicSocialWorkspaces(
            CoreSession session) {
        return searchDetachedPublicSocialWorkspaces(session, null);
    }

    @Override
    public List<SocialWorkspace> searchDetachedPublicSocialWorkspaces(
            CoreSession session, final String pattern) {

        final List<SocialWorkspace> socialWorkspaces = new ArrayList<SocialWorkspace>();

        UnrestrictedSessionRunner runner = new UnrestrictedSessionRunner(
                session) {

            private static final String ALL_PUBLIC_SOCIAL_WORKSPACE_QUERY = "SELECT * FROM Document "
                    + "WHERE ecm:mixinType != 'HiddenInNavigation' "
                    + "AND ecm:mixinType = '%s' "
                    + "AND ecm:currentLifeCycleState !='deleted' "
                    + "AND socialw:isPublic = 1 ";

            private static final String FULL_TEXT_WHERE_CLAUSE = "AND ecm:fulltext = '%s' ";

            private static final String ORDER_BY = "ORDER BY dc:title";

            @Override
            public void run() throws ClientException {
                String query = String.format(ALL_PUBLIC_SOCIAL_WORKSPACE_QUERY,
                        SOCIAL_WORKSPACE_FACET);
                if (!(pattern == null) && !"".equals(pattern.trim())) {
                    query = String.format(query + FULL_TEXT_WHERE_CLAUSE,
                            pattern);
                }
                query += ORDER_BY;

                DocumentModelList docs = session.query(query);
                for (DocumentModel doc : docs) {
                    ((DocumentModelImpl) doc).detach(true);
                    socialWorkspaces.add(toSocialWorkspace(doc));
                }
            }
        };

        try {
            runner.runUnrestricted();
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }

        return socialWorkspaces;
    }

    @Override
    public void activate(ComponentContext context) throws Exception {
        super.activate(context);
        subscriptionRequestHandler = new DefaultSubscriptionRequestHandler();
    }

    @Override
    public void deactivate(ComponentContext context) throws Exception {
        super.deactivate(context);
        subscriptionRequestHandler = null;
    }

    @Override
    public SocialWorkspace getDetachedSocialWorkspaceContainer(DocumentModel doc) {
        return getDetachedSocialWorkspaceContainer(doc.getCoreSession(),
                doc.getRef());
    }

    @Override
    public SocialWorkspace getDetachedSocialWorkspaceContainer(
            CoreSession session, DocumentRef docRef) {
        try {
            SocialWorkspaceFinder finder = new SocialWorkspaceFinder(session,
                    docRef);
            finder.runUnrestricted();
            if (finder.socialWorkspace != null) {
                return toSocialWorkspace(finder.socialWorkspace);
            }
            return null;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @Override
    public SocialWorkspace getSocialWorkspaceContainer(DocumentModel doc) {
        try {
            CoreSession session = doc.getCoreSession();
            List<DocumentModel> parents = session.getParentDocuments(doc.getRef());
            for (DocumentModel parent : parents) {
                if (isSocialWorkspace(parent)) {
                    return toSocialWorkspace(parent);
                }
            }
            return null;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

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
    public void handleSocialWorkspaceCreation(SocialWorkspace socialWorkspace,
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

    @Override
    public void handleSocialWorkspaceDeletion(SocialWorkspace socialWorkspace) {
        deleteGroup(socialWorkspace.getAdministratorsGroupName());
        deleteGroup(socialWorkspace.getMembersGroupName());
    }

    private void deleteGroup(String groupName) {
        try {
            getUserManager().deleteGroup(groupName);
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
            ACL acl = acp.getOrCreateACL(SOCIAL_WORKSPACE_ACL_NAME);
            addSocialWorkspaceACL(acl, socialWorkspace);
            doc.setACP(acp, true);
            doc.getCoreSession().saveDocument(doc);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    private void addSocialWorkspaceACL(ACL acl, SocialWorkspace socialWorkspace) {
        addEverythingForAdministratorsACE(acl);
        acl.add(new ACE(socialWorkspace.getAdministratorsGroupName(),
                SecurityConstants.EVERYTHING, true));
        acl.add(new ACE(socialWorkspace.getMembersGroupName(),
                SecurityConstants.READ_WRITE, true));
        acl.add(new ACE(EVERYONE, SecurityConstants.EVERYTHING, false));
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
    public boolean addSocialWorkspaceAdministrator(
            SocialWorkspace socialWorkspace, String principalName) {
        return addMemberToGroup(principalName,
                socialWorkspace.getAdministratorsGroupName());
    }

    @Override
    public boolean addSocialWorkspaceMember(SocialWorkspace socialWorkspace,
            String principalName) {
        return addMemberToGroup(principalName,
                socialWorkspace.getMembersGroupName());
    }

    @Override
    public void removeSocialWorkspaceAdministrator(
            SocialWorkspace socialWorkspace, String principalName) {
        removeMemberFromGroup(principalName,
                socialWorkspace.getMembersGroupName());
    }

    @Override
    public void removeSocialWorkspaceMember(SocialWorkspace socialWorkspace,
            String principalName) {
        removeMemberFromGroup(principalName,
                socialWorkspace.getAdministratorsGroupName());
    }

    @SuppressWarnings("unchecked")
    private boolean addMemberToGroup(String principalName, String groupName) {
        try {
            if (!StringUtils.isBlank(principalName)) {
                UserManager userManager = getUserManager();
                DocumentModel group = userManager.getGroupModel(groupName);
                String groupSchemaName = userManager.getGroupSchemaName();
                String groupMembersField = userManager.getGroupMembersField();
                List<String> groupMembers = (List<String>) group.getProperty(
                        groupSchemaName, groupMembersField);
                if (groupMembers == null) {
                    groupMembers = new ArrayList<String>();
                }
                if (!groupMembers.contains(principalName)) {
                    groupMembers.add(principalName);
                    group.setProperty(groupSchemaName, groupMembersField,
                            groupMembers);
                    userManager.updateGroup(group);
                    return true;
                }
            }
            return false;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void removeMemberFromGroup(String principalName, String groupName) {
        try {
            if (!StringUtils.isBlank(principalName)) {
                UserManager userManager = getUserManager();
                DocumentModel group = userManager.getGroupModel(groupName);
                String groupSchemaName = userManager.getGroupSchemaName();
                String groupMembersField = userManager.getGroupMembersField();
                List<String> groupMembers = (List<String>) group.getProperty(
                        groupSchemaName, groupMembersField);
                if (groupMembers != null) {
                    groupMembers.remove(principalName);
                    group.setProperty(groupSchemaName, groupMembersField,
                            groupMembers);
                    userManager.updateGroup(group);
                }
            }
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @Override
    public void makeSocialWorkspacePublic(SocialWorkspace socialWorkspace) {
        try {
            DocumentModel doc = socialWorkspace.getDocument();
            doc.setPropertyValue(SOCIAL_WORKSPACE_IS_PUBLIC_PROPERTY, true);
            doc.putContextData(SocialWorkspaceListener.DO_NOT_PROCESS, true);

            CoreSession session = doc.getCoreSession();
            makePublicSectionReadable(session, socialWorkspace);
            makePublicDashboardReadable(session, socialWorkspace);
            doc = session.saveDocument(doc);
            session.save();
            socialWorkspace.setDocument(doc);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    private void makePublicSectionReadable(CoreSession session,
            SocialWorkspace socialWorkspace) throws ClientException {
        PathRef publicSectionRef = new PathRef(
                socialWorkspace.getPublicSectionPath());
        DocumentModel publicSection = session.getDocument(publicSectionRef);

        ACP acp = publicSection.getACP();
        ACL acl = acp.getOrCreateACL(PUBLIC_SOCIAL_WORKSPACE_ACL_NAME);
        acl.clear();
        addReadForDefaultGroup(acl);
        publicSection.setACP(acp, true);
        session.saveDocument(publicSection);
    }

    private void addReadForDefaultGroup(ACL acl) {
        String defaultGroup = getUserManager().getDefaultGroup();
        defaultGroup = defaultGroup == null ? EVERYONE : defaultGroup;
        acl.add(new ACE(defaultGroup, READ, true));
    }

    private void makePublicDashboardReadable(CoreSession session,
            SocialWorkspace socialWorkspace) throws ClientException {
        PathRef dashboardSpacesRootRef = new PathRef(
                socialWorkspace.getDashboardSpacesRootPath());
        DocumentModel dashboardSpacesRoot = session.getDocument(dashboardSpacesRootRef);
        ACP acp = dashboardSpacesRoot.getACP();
        ACL acl = acp.getOrCreateACL(PUBLIC_SOCIAL_WORKSPACE_ACL_NAME);
        acl.clear();
        addReadForDefaultGroup(acl);
        dashboardSpacesRoot.setACP(acp, true);
        session.saveDocument(dashboardSpacesRoot);

        PathRef privateDashboardSpaceRef = new PathRef(
                socialWorkspace.getPrivateDashboardSpacePath());
        DocumentModel privateDashboardSpace = session.getDocument(privateDashboardSpaceRef);
        acp = privateDashboardSpace.getACP();
        acl = acp.getOrCreateACL(PUBLIC_SOCIAL_WORKSPACE_ACL_NAME);
        addSocialWorkspaceACL(acl, socialWorkspace);
        privateDashboardSpace.setACP(acp, true);
        session.saveDocument(privateDashboardSpace);
    }

    @Override
    public void makeSocialWorkspacePrivate(SocialWorkspace socialWorkspace) {
        try {
            DocumentModel doc = socialWorkspace.getDocument();
            doc.setPropertyValue(SOCIAL_WORKSPACE_IS_PUBLIC_PROPERTY, false);
            doc.putContextData(SocialWorkspaceListener.DO_NOT_PROCESS, true);

            CoreSession session = doc.getCoreSession();
            makePublicSectionUnreadable(session, socialWorkspace);
            makePublicDashboardUnreadable(session, socialWorkspace);
            doc = session.saveDocument(doc);
            session.save();
            socialWorkspace.setDocument(doc);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    private void makePublicSectionUnreadable(CoreSession session,
            SocialWorkspace socialWorkspace) throws ClientException {
        PathRef publicSectionRef = new PathRef(
                socialWorkspace.getPublicSectionPath());
        DocumentModel publicSection = session.getDocument(publicSectionRef);

        ACP acp = publicSection.getACP();
        acp.removeACL(PUBLIC_SOCIAL_WORKSPACE_ACL_NAME);
        publicSection.setACP(acp, true);
        session.saveDocument(publicSection);
    }

    private void makePublicDashboardUnreadable(CoreSession session,
            SocialWorkspace socialWorkspace) throws ClientException {
        PathRef dashboardSpacesRootRef = new PathRef(
                socialWorkspace.getDashboardSpacesRootPath());
        DocumentModel dashboardSpacesRoot = session.getDocument(dashboardSpacesRootRef);
        ACP acp = dashboardSpacesRoot.getACP();
        acp.removeACL(PUBLIC_SOCIAL_WORKSPACE_ACL_NAME);
        dashboardSpacesRoot.setACP(acp, true);
        session.saveDocument(dashboardSpacesRoot);

        PathRef privateDashboardSpaceRef = new PathRef(
                socialWorkspace.getPrivateDashboardSpacePath());
        DocumentModel privateDashboardSpace = session.getDocument(privateDashboardSpaceRef);
        acp = privateDashboardSpace.getACP();
        acp.removeACL(PUBLIC_SOCIAL_WORKSPACE_ACL_NAME);
        privateDashboardSpace.setACP(acp, true);
        session.saveDocument(privateDashboardSpace);
    }

    @Override
    public void handleSubscriptionRequest(SocialWorkspace socialWorkspace,
            String principalName) {
        subscriptionRequestHandler.handleSubscriptionRequestFor(
                socialWorkspace, principalName);
    }

    @Override
    public boolean isSubscriptionRequestPending(
            SocialWorkspace socialWorkspace, String principalName) {
        return subscriptionRequestHandler.isSubscriptionRequestPending(
                socialWorkspace, principalName);
    }

    @Override
    public void acceptSubscriptionRequest(SocialWorkspace socialWorkspace,
            SubscriptionRequest subscriptionRequest) {
        subscriptionRequestHandler.acceptSubscriptionRequest(socialWorkspace,
                subscriptionRequest);
    }

    @Override
    public void rejectSubscriptionRequest(SocialWorkspace socialWorkspace,
            SubscriptionRequest subscriptionRequest) {
        subscriptionRequestHandler.rejectSubscriptionRequest(socialWorkspace,
                subscriptionRequest);
    }

    private static class SocialWorkspaceFinder extends
            UnrestrictedSessionRunner {

        private final DocumentRef docRef;

        public DocumentModel socialWorkspace;

        protected SocialWorkspaceFinder(CoreSession session, DocumentRef docRef) {
            super(session);
            this.docRef = docRef;
        }

        @Override
        public void run() throws ClientException {
            List<DocumentModel> parents = session.getParentDocuments(docRef);
            for (DocumentModel parent : parents) {
                if (isSocialWorkspace(parent)) {
                    socialWorkspace = parent;
                    if (socialWorkspace instanceof DocumentModelImpl) {
                        ((DocumentModelImpl) socialWorkspace).detach(true);
                        break;
                    }
                }
            }
        }

    }

}
