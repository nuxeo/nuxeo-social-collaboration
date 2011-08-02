package org.nuxeo.ecm.social.workspace.adapters;

import static org.nuxeo.ecm.social.workspace.SocialConstants.DASHBOARD_SPACES_ROOT_NAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_SOCIAL_WORKSPACE_APPROVE_SUBSCRIPTION;
import static org.nuxeo.ecm.social.workspace.SocialConstants.PRIVATE_DASHBOARD_SPACE_NAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.PUBLIC_DASHBOARD_SPACE_NAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_IS_PUBLIC_PROPERTY;

import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;
import org.nuxeo.runtime.api.Framework;

/**
 * Default implementation of {@see SocialWorkspace}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class SocialWorkspaceAdapter extends BaseAdapter implements
        SocialWorkspace {

    public SocialWorkspaceAdapter(DocumentModel doc) {
        super(doc);
    }

    @Override
    public String getId() {
        return doc.getId();
    }

    @Override
    public String getTitle() {
        try {
            return doc.getTitle();
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @Override
    public String getPath() {
        return doc.getPathAsString();
    }

    @Override
    public boolean isPublic() {
        Boolean isPublic = (Boolean) getDocProperty(doc,
                SOCIAL_WORKSPACE_IS_PUBLIC_PROPERTY);
        return isPublic == null ? false : isPublic;
    }

    @Override
    public boolean isPrivate() {
        return !isPublic();
    }

    @Override
    public void makePublic() {
        getSocialWorkspaceService().makeSocialWorkspacePublic(this);
    }

    @Override
    public void makePrivate() {
        getSocialWorkspaceService().makeSocialWorkspacePrivate(this);
    }

    @Override
    public boolean mustApproveSubscription() {
        Boolean approveSubscription = (Boolean) getDocProperty(doc,
                FIELD_SOCIAL_WORKSPACE_APPROVE_SUBSCRIPTION);
        return approveSubscription == null ? false : approveSubscription;
    }

    @Override
    public boolean addAdministrator(String principalName) {
        return getSocialWorkspaceService().addSocialWorkspaceAdministrator(
                this, principalName);
    }

    @Override
    public boolean addMember(String principalName) {
        return getSocialWorkspaceService().addSocialWorkspaceMember(this,
                principalName);
    }

    @Override
    public void removeAdministrator(String principalName) {
        getSocialWorkspaceService().removeSocialWorkspaceAdministrator(this,
                principalName);
    }

    @Override
    public void removeMember(String principalName) {
        getSocialWorkspaceService().removeSocialWorkspaceMember(this,
                principalName);
    }

    @Override
    public boolean isAdministrator(NuxeoPrincipal principal) {
        return principal.isMemberOf(getAdministratorsGroupName());
    }

    @Override
    public boolean isMember(NuxeoPrincipal principal) {
        return principal.isMemberOf(getMembersGroupName());
    }

    @Override
    public boolean isAdministratorOrMember(NuxeoPrincipal principal) {
        return isAdministrator(principal) || isMember(principal);
    }

    @Override
    public String getAdministratorsGroupName() {
        return SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupName(doc.getId());
    }

    @Override
    public String getAdministratorsGroupLabel() {
        try {
            return SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupLabel(doc.getTitle());
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @Override
    public String getMembersGroupName() {
        return SocialWorkspaceHelper.getSocialWorkspaceMembersGroupName(doc.getId());
    }

    @Override
    public String getMembersGroupLabel() {
        try {
            return SocialWorkspaceHelper.getSocialWorkspaceMembersGroupLabel(doc.getTitle());
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @Override
    public String getPublicSectionPath() {
        return SocialWorkspaceHelper.getPublicSectionPath(doc);
    }

    @Override
    public String getPrivateSectionPath() {
        return SocialWorkspaceHelper.getPrivateSectionPath(doc);
    }

    @Override
    public String getNewsItemsRootPath() {
        return SocialWorkspaceHelper.getNewsItemsRootPath(doc);
    }

    @Override
    public String getDashboardSpacesRootPath() {
        return doc.getPath().append(DASHBOARD_SPACES_ROOT_NAME).toString();
    }

    @Override
    public String getPublicDashboardSpacePath() {
        return new Path(getDashboardSpacesRootPath()).append(
                PUBLIC_DASHBOARD_SPACE_NAME).toString();
    }

    @Override
    public String getPrivateDashboardSpacePath() {
        return new Path(getDashboardSpacesRootPath()).append(
                PRIVATE_DASHBOARD_SPACE_NAME).toString();
    }

    @Override
    public void handleSubscriptionRequest(String principalName) {
        getSocialWorkspaceService().handleSubscriptionRequest(this,
                principalName);
    }

    @Override
    public boolean isSubscriptionRequestPending(String principalName) {
        return getSocialWorkspaceService().isSubscriptionRequestPending(this,
                principalName);
    }

    @Override
    public void acceptSubscriptionRequest(
            SubscriptionRequest subscriptionRequest) {
        getSocialWorkspaceService().acceptSubscriptionRequest(this,
                subscriptionRequest);
    }

    @Override
    public void rejectSubscriptionRequest(
            SubscriptionRequest subscriptionRequest) {
        getSocialWorkspaceService().rejectSubscriptionRequest(this,
                subscriptionRequest);
    }

    @Override
    public DocumentModel getDocument() {
        return doc;
    }

    @Override
    public void setDocument(DocumentModel doc) {
        if (!this.doc.getId().equals(doc.getId())) {
            throw new ClientRuntimeException("");
        }
        this.doc = doc;
    }

    private SocialWorkspaceService getSocialWorkspaceService() {
        try {
            return Framework.getService(SocialWorkspaceService.class);
        } catch (Exception e) {
            throw new ClientRuntimeException(e);
        }
    }

}
