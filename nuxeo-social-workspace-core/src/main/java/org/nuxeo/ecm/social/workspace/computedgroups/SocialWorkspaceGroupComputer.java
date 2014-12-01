package org.nuxeo.ecm.social.workspace.computedgroups;

import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.buildRelationAdministratorKind;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.buildRelationKindFromGroupName;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.buildRelationMemberKind;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.getRelationDocActivityObjectFromGroupName;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupName;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.getSocialWorkspaceMembersGroupName;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.isValidSocialWorkspaceGroupName;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.platform.computedgroups.AbstractGroupComputer;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;
import org.nuxeo.ecm.social.relationship.service.RelationshipService;
import org.nuxeo.runtime.api.Framework;

/**
 * Social Workspace group computer to provide virtual groups.
 * <p>
 * We have two virtual groups: - members of a social workspace - administrators
 * of a social workspace
 * <p>
 * It do not provide sub groups.
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
public class SocialWorkspaceGroupComputer extends AbstractGroupComputer {

    protected RelationshipService relationshipService;

    private static final Log log = LogFactory.getLog(SocialWorkspaceGroupComputer.class);

    @Override
    public List<String> getGroupsForUser(NuxeoPrincipalImpl nuxeoPrincipal) {
        String user = ActivityHelper.createUserActivityObject(nuxeoPrincipal);
        List<String> groupsId = new ArrayList<String>();
        // member of a social workspace
        for (String swId : getRelationshipService().getTargetsOfKind(user,
                buildRelationMemberKind())) {
            groupsId.add(getSocialWorkspaceMembersGroupName(swId));
        }
        // administrator of a social workspace
        for (String swId : getRelationshipService().getTargetsOfKind(user,
                buildRelationAdministratorKind())) {
            groupsId.add(getSocialWorkspaceAdministratorsGroupName(swId));
        }
        return groupsId;
    }

    @Override
    public List<String> getAllGroupIds() {
        // Retrieve all groupIds is not desired.
        return null;
    }

    @Override
    public List<String> getGroupMembers(String groupName) {
        if (!isValidSocialWorkspaceGroupName(groupName)) {
            return null;
        }
        return getRelationshipService().getTargetsOfKind(
                getRelationDocActivityObjectFromGroupName(groupName),
                buildRelationKindFromGroupName(groupName));
    }

    @Override
    public List<String> getParentsGroupNames(String groupName) {
        // Make a subgroup with social workspace group should not be
        // implemented.
        return null;
    }

    @Override
    public List<String> getSubGroupsNames(String groupName) {
        // not needed here
        return null;
    }

    protected RelationshipService getRelationshipService() {
        if (relationshipService == null) {
            try {
                relationshipService = Framework.getService(RelationshipService.class);
            } catch (Exception e) {
                log.warn("Cannot retrieve RelationshipService Service");
                log.debug(e, e);
            }
        }
        return relationshipService;
    }
}
