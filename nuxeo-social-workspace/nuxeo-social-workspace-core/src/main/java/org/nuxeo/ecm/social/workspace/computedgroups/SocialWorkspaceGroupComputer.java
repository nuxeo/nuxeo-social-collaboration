package org.nuxeo.ecm.social.workspace.computedgroups;

import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.buildRelationAdministratorKind;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.getRelationDocIdFromGroupName;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.buildRelationKindFromGroupName;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.buildRelationMemberKind;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupName;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.getSocialWorkspaceMembersGroupName;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.isValidSocialWorkspaceGroupName;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.platform.computedgroups.AbstractGroupComputer;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;
import org.nuxeo.ecm.social.user.relationship.service.UserRelationshipService;
import org.nuxeo.runtime.api.Framework;

/**
 * Social Workspace group computer to provide virtual groups.
 * <p/>
 * We have two virtual groups:
 * - members of a social workspace
 * - administrators of a social workspace
 * <p/>
 * It do not provide sub groups.
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
public class SocialWorkspaceGroupComputer extends AbstractGroupComputer {

    protected UserRelationshipService relationshipService;

    private static final Log log = LogFactory.getLog(
            SocialWorkspaceGroupComputer.class);

    @Override
    public List<String> getGroupsForUser(NuxeoPrincipalImpl nuxeoPrincipal)
            throws Exception {
        String userId = nuxeoPrincipal.getModel().getId();
        List<String> groupsId = new ArrayList<String>();
        // member of a social workspace
        for (String swId : getRelationshipService().getTargetsOfKind(userId,
                buildRelationMemberKind())) {
            groupsId.add(getSocialWorkspaceMembersGroupName(swId));
        }
        // administrator of a social workspace
        for (String swId : getRelationshipService().getTargetsOfKind(userId,
                buildRelationAdministratorKind())) {
            groupsId.add(getSocialWorkspaceAdministratorsGroupName(swId));
        }
        return groupsId;
    }

    @Override
    public List<String> getAllGroupIds() throws Exception {
        // Retrieve all groupIds is not desired.
        return null;
    }

    @Override
    public List<String> getGroupMembers(String groupName) throws Exception {
        if (!isValidSocialWorkspaceGroupName(groupName)) {
            return null;
        }
        return getRelationshipService().getTargetsOfKind(
                getRelationDocIdFromGroupName(groupName),
                buildRelationKindFromGroupName(groupName));
    }

    @Override
    public List<String> getParentsGroupNames(String groupName) throws Exception {
        // Make a subgroup with social workspace group should not be implemented.
        return null;
    }

    @Override
    public List<String> getSubGroupsNames(String groupName) throws Exception {
        // not needed here
        return null;
    }

    protected UserRelationshipService getRelationshipService() {
        if (relationshipService == null) {
            try {
                relationshipService = Framework.getService(
                        UserRelationshipService.class);
            } catch (Exception e) {
                log.warn("Cannot retrieve UserRelationship Service");
                log.debug(e, e);
            }
        }
        return relationshipService;
    }
}
