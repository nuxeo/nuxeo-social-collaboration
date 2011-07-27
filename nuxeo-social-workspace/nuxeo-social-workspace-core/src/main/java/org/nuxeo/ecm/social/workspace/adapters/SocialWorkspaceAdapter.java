package org.nuxeo.ecm.social.workspace.adapters;

import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_SOCIAL_WORKSPACE_APPROVE_SUBSCRIPTION;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_SOCIAL_WORKSPACE_IS_PUBLIC;

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
    public void initialize(String principalName) {
        getSocialWorkspaceService().initializeSocialWorkspace(this,
                principalName);
    }

    @Override
    public boolean isPublic() {
        Boolean isPublic = (Boolean) getDocProperty(doc,
                FIELD_SOCIAL_WORKSPACE_IS_PUBLIC);
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
    public boolean isAdministrator(NuxeoPrincipal principal) {
        return principal.isMemberOf(getAdministratorsGroupName());
    }

    @Override
    public boolean isMember(NuxeoPrincipal principal) {
        return principal.isMemberOf(getAdministratorsGroupName());
    }

    @Override
    public boolean isAdministratorOrMember(NuxeoPrincipal principal) {
        return isAdministrator(principal) || isMember(principal);
    }

    @Override
    public String getAdministratorsGroupName() {
        return SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupName(doc);
    }

    @Override
    public String getAdministratorsGroupLabel() {
        return SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupLabel(doc);
    }

    @Override
    public String getMembersGroupName() {
        return SocialWorkspaceHelper.getSocialWorkspaceMembersGroupName(doc);
    }

    @Override
    public String getMembersGroupLabel() {
        return SocialWorkspaceHelper.getSocialWorkspaceMembersGroupLabel(doc);
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
    public String getPublicDashboardSpacePath() {
        return doc.getPath().append("dashboardSpacesRoot").append(
                "publicDashboardSpace").toString();
    }

    @Override
    public String getPrivateDashboardSpacePath() {
        return doc.getPath().append("dashboardSpacesRoot").append(
                "privateDashboardSpace").toString();
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
