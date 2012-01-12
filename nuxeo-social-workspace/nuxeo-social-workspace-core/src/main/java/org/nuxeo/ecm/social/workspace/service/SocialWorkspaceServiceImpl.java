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
import static org.nuxeo.ecm.core.api.security.SecurityConstants.EVERYTHING;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.WRITE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.CTX_PRINCIPALS_PROPERTY;
import static org.nuxeo.ecm.social.workspace.SocialConstants.EVENT_MEMBERS_ADDED;
import static org.nuxeo.ecm.social.workspace.SocialConstants.EVENT_MEMBERS_REMOVED;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_FACET;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_IS_PUBLIC_PROPERTY;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.buildRelationAdministratorKind;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.buildRelationMemberKind;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupName;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.getSocialWorkspaceMembersGroupName;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.isSocialWorkspace;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialWorkspace;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.collections.ScopeType;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityBuilder;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.platform.usermanager.exceptions.GroupAlreadyExistsException;
import org.nuxeo.ecm.social.relationship.RelationshipKind;
import org.nuxeo.ecm.social.relationship.service.RelationshipService;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.adapters.SubscriptionRequest;
import org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper;
import org.nuxeo.ecm.social.workspace.listeners.SocialWorkspaceListener;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * Default implementation of {@see SocialWorkspaceService} service.
 */
public class SocialWorkspaceServiceImpl extends DefaultComponent implements
        SocialWorkspaceService {

    private static final Log log = LogFactory.getLog(SocialWorkspaceServiceImpl.class);

    public static final String CONFIGURATION_EP = "configuration";

    public static final String SOCIAL_WORKSPACE_CONTAINER_EP = "socialWorkspaceContainer";

    public static final String SOCIAL_WORKSPACE_ACL_NAME = "socialWorkspaceAcl";

    public static final String NEWS_ITEMS_ROOT_ACL_NAME = "newsItemsRootAcl";

    public static final String PUBLIC_SOCIAL_WORKSPACE_ACL_NAME = "publicSocialWorkspaceAcl";

    private UserManager userManager;

    private SubscriptionRequestHandler subscriptionRequestHandler;

    private int validationDays;

    private SocialWorkspaceContainerDescriptor socialWorkspaceContainer;

    private RelationshipService relationshipService;

    private ActivityStreamService activityStreamService;

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
                if (!StringUtils.isBlank(pattern)) {
                    query = String.format(query + FULL_TEXT_WHERE_CLAUSE,
                            pattern);
                }
                query += ORDER_BY;

                DocumentModelList docs = session.query(query);
                for (DocumentModel doc : docs) {
                    doc.detach(true);
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
    public SocialWorkspace getDetachedSocialWorkspace(DocumentModel doc) {
        return getDetachedSocialWorkspace(doc.getCoreSession(), doc.getRef());
    }

    @Override
    public SocialWorkspace getDetachedSocialWorkspace(CoreSession session,
            DocumentRef docRef) {
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
    public SocialWorkspace getSocialWorkspace(DocumentModel doc) {
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
        } else if (SOCIAL_WORKSPACE_CONTAINER_EP.equals(extensionPoint)) {
            socialWorkspaceContainer = (SocialWorkspaceContainerDescriptor) contribution;
        }
    }

    @Override
    public SocialWorkspaceContainerDescriptor getSocialWorkspaceContainerDescriptor() {
        return socialWorkspaceContainer;
    }

    @Override
    public DocumentModel getSocialWorkspaceContainer(CoreSession session) {
        try {
            return session.getDocument(new PathRef(
                    socialWorkspaceContainer.getPath()));
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @Override
    public void handleSocialWorkspaceCreation(
            final SocialWorkspace socialWorkspace, final Principal principal) {
        createBaseRelationshipsWithSocialWorkspace(socialWorkspace, principal);
        CoreSession session = socialWorkspace.getDocument().getCoreSession();
        try {
            new UnrestrictedSessionRunner(session) {
                @Override
                public void run() throws ClientException {
                    SocialWorkspace unrestrictedSocialWorkspace = toSocialWorkspace(session.getDocument(new IdRef(
                            socialWorkspace.getId())));
                    initializeSocialWorkspaceRights(unrestrictedSocialWorkspace);
                    initializeNewsItemsRootRights(unrestrictedSocialWorkspace);
                    if (unrestrictedSocialWorkspace.isPublic()) {
                        makeSocialWorkspacePublic(unrestrictedSocialWorkspace);
                    }
                }
            }.runUnrestricted();
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    private void createBaseRelationshipsWithSocialWorkspace(
            SocialWorkspace socialWorkspace, Principal principal) {
        addSocialWorkspaceAdministrator(socialWorkspace, principal);
        // This group is just added here to prevent user from re-login before
        // matching this virtual group.
        ((NuxeoPrincipal) principal).getAllGroups().add(
                socialWorkspace.getAdministratorsGroupName());
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
        getRelationshipService().removeRelation(socialWorkspace.getId(), null,
                SocialWorkspaceHelper.buildRelationKind());
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
            CoreSession session = doc.getCoreSession();
            ACP acp = doc.getACP();
            ACL acl = acp.getOrCreateACL(SOCIAL_WORKSPACE_ACL_NAME);
            addSocialWorkspaceACL(acl, socialWorkspace);
            doc.setACP(acp, true);
            doc.putContextData(ScopeType.REQUEST,
                    SocialWorkspaceListener.DO_NOT_PROCESS, true);
            doc = session.saveDocument(doc);
            socialWorkspace.setDocument(doc);
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
            acl.add(new ACE(adminGroup, EVERYTHING, true));
        }
    }

    private static void initializeNewsItemsRootRights(
            SocialWorkspace socialWorkspace) {
        try {
            CoreSession session = socialWorkspace.getDocument().getCoreSession();
            PathRef newsItemsRootPath = new PathRef(
                    socialWorkspace.getNewsItemsRootPath());
            DocumentModel newsItemsRoot = session.getDocument(newsItemsRootPath);

            ACP acp = newsItemsRoot.getACP();
            ACL acl = acp.getOrCreateACL(NEWS_ITEMS_ROOT_ACL_NAME);
            acl.add(new ACE(socialWorkspace.getAdministratorsGroupName(),
                    EVERYTHING, true));
            acl.add(new ACE(socialWorkspace.getMembersGroupName(), WRITE, false));
            newsItemsRoot.setACP(acp, true);
            session.saveDocument(newsItemsRoot);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @Override
    public boolean addSocialWorkspaceAdministrator(
            SocialWorkspace socialWorkspace, Principal principal) {
        if (addPrincipalToSocialWorkspace(
                ActivityHelper.createUserActivityObject(principal.getName()),
                ActivityHelper.createDocumentActivityObject(socialWorkspace.getDocument()),
                buildRelationAdministratorKind())) {
            addSocialWorkspaceMember(socialWorkspace, principal);
            updatePrincipalGroups(principal);
            return true;
        }
        return false;
    }

    @Override
    public boolean addSocialWorkspaceMember(SocialWorkspace socialWorkspace,
            Principal principal) {
        Boolean memberCreated = addSocialWorkspaceMemberWithoutNotification(
                socialWorkspace, principal);
        if (memberCreated) {
            updatePrincipalGroups(principal);
            fireEventMembersManagement(socialWorkspace,
                    Arrays.asList(principal), EVENT_MEMBERS_ADDED);
        }
        return memberCreated;
    }

    private void updatePrincipalGroups(Principal principal) {
        try {
            if (principal instanceof NuxeoPrincipalImpl) {
                ((NuxeoPrincipalImpl) principal).updateAllGroups();
            }
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    private boolean addSocialWorkspaceMemberWithoutNotification(
            SocialWorkspace socialWorkspace, Principal principal) {
        if (addPrincipalToSocialWorkspace(
                ActivityHelper.createUserActivityObject(principal.getName()),
                ActivityHelper.createDocumentActivityObject(socialWorkspace.getDocument()),
                buildRelationMemberKind())) {
            addNewActivity(principal, socialWorkspace,
                    buildRelationMemberKind());
            return true;
        }
        return false;
    }

    @Override
    public List<String> addSocialWorkspaceMembers(
            SocialWorkspace socialWorkspace, String groupName)
            throws ClientException {
        NuxeoGroup group = getUserManager().getGroup(groupName);
        if (group == null) {
            throw new ClientException(String.format("Group (%s) not found",
                    groupName));
        }

        List<String> importedUsers = new ArrayList<String>();
        List<Principal> importedPrincipal = new ArrayList<Principal>();
        for (String userName : group.getMemberUsers()) {
            Principal principal = userManager.getPrincipal(userName);
            if (principal == null) {
                log.info(String.format("User (%s) doesn't exist.", userName));
                continue;
            }

            if (addSocialWorkspaceMemberWithoutNotification(socialWorkspace,
                    principal)) {
                importedUsers.add(userName);
                importedPrincipal.add(principal);
            }
        }

        // Notify bulk import
        fireEventMembersManagement(socialWorkspace, importedPrincipal,
                EVENT_MEMBERS_ADDED);

        return importedUsers;
    }

    @Override
    public List<String> addSocialWorkspaceMembers(
            SocialWorkspace socialWorkspace, List<String> emails)
            throws ClientException {
        List<String> memberAddedList = new ArrayList<String>(emails.size());
        List<Principal> principalAdded = new ArrayList<Principal>();
        for (String email : emails) {
            Map<String, Serializable> filter = new HashMap<String, Serializable>();
            String emailKey = getUserManager().getUserEmailField();
            filter.put(emailKey, email);
            Set<String> pattern = new HashSet<String>();
            pattern.add(emailKey);

            DocumentModelList foundUsers = userManager.searchUsers(filter,
                    pattern);

            if (foundUsers.isEmpty()) {
                continue;
            } else if (foundUsers.size() > 1) {
                log.info("For the email " + email
                        + " several user were found. First one used.");
            }

            DocumentModel firstUser = foundUsers.get(0);
            NuxeoPrincipalImpl principal = new NuxeoPrincipalImpl(
                    firstUser.getId());
            principal.setModel(firstUser, false);

            if (addSocialWorkspaceMemberWithoutNotification(socialWorkspace,
                    principal)) {
                memberAddedList.add(email);
                principalAdded.add(principal);
            }

        }
        // Notify bulk import
        fireEventMembersManagement(socialWorkspace, principalAdded,
                EVENT_MEMBERS_ADDED);

        return memberAddedList;
    }

    private void addNewActivity(Principal principal,
            SocialWorkspace socialWorkspace, RelationshipKind kind) {
        Activity activity = new ActivityBuilder().actor(
                ActivityHelper.createDocumentActivityObject(socialWorkspace.getDocument())).displayActor(
                socialWorkspace.getTitle()).verb(kind.toString()).object(
                ActivityHelper.createUserActivityObject(principal.getName())).displayObject(
                ActivityHelper.generateDisplayName(principal)).target(
                ActivityHelper.createDocumentActivityObject(socialWorkspace.getDocument())).displayTarget(
                socialWorkspace.getTitle()).build();
        Framework.getLocalService(ActivityStreamService.class).addActivity(
                activity);
    }

    @Override
    public void removeSocialWorkspaceAdministrator(
            SocialWorkspace socialWorkspace, Principal principal) {
        removePrincipalFromSocialWorkspace(
                ActivityHelper.createUserActivityObject(principal.getName()),
                ActivityHelper.createDocumentActivityObject(socialWorkspace.getDocument()),
                buildRelationAdministratorKind());
    }

    @Override
    public void removeSocialWorkspaceMember(SocialWorkspace socialWorkspace,
            Principal principal) {
        if (removePrincipalFromSocialWorkspace(
                ActivityHelper.createUserActivityObject(principal.getName()),
                ActivityHelper.createDocumentActivityObject(socialWorkspace.getDocument()),
                buildRelationMemberKind())) {
            fireEventMembersManagement(socialWorkspace,
                    Arrays.asList(principal), EVENT_MEMBERS_REMOVED);
        }
    }

    private boolean addPrincipalToSocialWorkspace(String principalName,
            String socialWorkspaceId, RelationshipKind kind) {
        boolean added = getRelationshipService().addRelation(principalName,
                socialWorkspaceId, kind);
        added &= getRelationshipService().addRelation(socialWorkspaceId,
                principalName, kind);
        return added;
    }

    private boolean removePrincipalFromSocialWorkspace(String principalName,
            String socialWorkspaceId, RelationshipKind kind) {
        boolean removed = getRelationshipService().removeRelation(
                principalName, socialWorkspaceId, kind);
        removed |= getRelationshipService().removeRelation(socialWorkspaceId,
                principalName, kind);
        return removed;
    }

    @Override
    public void makeSocialWorkspacePublic(SocialWorkspace socialWorkspace) {
        try {
            DocumentModel doc = socialWorkspace.getDocument();
            doc.setPropertyValue(SOCIAL_WORKSPACE_IS_PUBLIC_PROPERTY, true);
            doc.putContextData(ScopeType.REQUEST,
                    SocialWorkspaceListener.DO_NOT_PROCESS, true);

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
            doc.putContextData(ScopeType.REQUEST,
                    SocialWorkspaceListener.DO_NOT_PROCESS, true);

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

    private static void makePublicSectionUnreadable(CoreSession session,
            SocialWorkspace socialWorkspace) throws ClientException {
        PathRef publicSectionRef = new PathRef(
                socialWorkspace.getPublicSectionPath());
        DocumentModel publicSection = session.getDocument(publicSectionRef);

        ACP acp = publicSection.getACP();
        acp.removeACL(PUBLIC_SOCIAL_WORKSPACE_ACL_NAME);
        publicSection.setACP(acp, true);
        session.saveDocument(publicSection);
    }

    private static void fireEventMembersManagement(
            SocialWorkspace socialWorkspace, List<Principal> usernames,
            String eventName) {
        if (socialWorkspace.isMembersNotificationEnabled()) {
            DocumentModel doc = socialWorkspace.getDocument();
            EventContext ctx = new DocumentEventContext(doc.getCoreSession(),
                    doc.getCoreSession().getPrincipal(), doc);
            ctx.setProperty(CTX_PRINCIPALS_PROPERTY, (Serializable) usernames);

            try {
                Framework.getLocalService(EventService.class).fireEvent(
                        ctx.newEvent(eventName));
            } catch (ClientException e) {
                log.warn("Unable to notify social workspace members", e);
            }
        }
    }

    private static void makePublicDashboardUnreadable(CoreSession session,
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
            Principal principal) {
        subscriptionRequestHandler.handleSubscriptionRequestFor(
                socialWorkspace, principal);
    }

    @Override
    public boolean isSubscriptionRequestPending(
            SocialWorkspace socialWorkspace, Principal principal) {
        return subscriptionRequestHandler.isSubscriptionRequestPending(
                socialWorkspace, principal);
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

    @Override
    public List<String> searchUsers(SocialWorkspace socialWorkspace,
            RelationshipKind kind, String pattern) {
        List<String> targets = getRelationshipService().getTargetsWithFulltext(
                ActivityHelper.createDocumentActivityObject(socialWorkspace.getDocument()),
                kind, pattern);
        List<String> users = new ArrayList<String>();
        for (String target : targets) {
            users.add(ActivityHelper.getUsername(target));
        }
        return users;

    }

    @Override
    public List<String> searchMembers(SocialWorkspace socialWorkspace,
            String pattern) {
        // get members of the social workspace
        List<String> list = searchUsers(socialWorkspace,
                buildRelationMemberKind(), null);
        return filterUsers(pattern, list);
    }

    @Override
    public List<String> searchAdministrators(SocialWorkspace socialWorkspace,
            String pattern) {
        // get administrators of the social workspace
        List<String> list = searchUsers(socialWorkspace,
                buildRelationAdministratorKind(), null);
        return filterUsers(pattern, list);
    }

    private List<String> filterUsers(String pattern, List<String> validNames) {
        List<String> members = new ArrayList<String>();
        DocumentModelList users = null;
        // get users that match the pattern
        try {
            users = getUserManager().searchUsers(pattern);
        } catch (ClientException e) {
            log.warn("failed to get users that match pattern:" + pattern, e);
        }
        if (users != null) {
            for (DocumentModel user : users) {
                String name;
                try {
                    name = (String) user.getProperty(
                            getUserManager().getUserSchemaName(),
                            getUserManager().getUserIdField());
                    if (validNames.contains(name)) {
                        members.add(name);
                    }
                } catch (PropertyException e) {
                    log.debug(e, e);
                } catch (ClientException e) {
                    log.debug(e, e);
                }

            }
        }
        return members;
    }

    private RelationshipService getRelationshipService() {
        if (relationshipService == null) {
            try {
                relationshipService = Framework.getService(RelationshipService.class);
            } catch (Exception e) {
                throw new ClientRuntimeException(e);
            }
        }
        if (relationshipService == null) {
            throw new ClientRuntimeException(
                    "RelationshipService is not registered.");
        }
        return relationshipService;
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
                    socialWorkspace.detach(true);
                    if (socialWorkspace instanceof DocumentModelImpl) {
                        break;
                    }
                }
            }
        }

    }

}
