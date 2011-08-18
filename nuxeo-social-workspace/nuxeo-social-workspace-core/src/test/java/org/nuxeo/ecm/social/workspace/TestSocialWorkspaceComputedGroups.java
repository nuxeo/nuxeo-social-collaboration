package org.nuxeo.ecm.social.workspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.buildRelationAdministratorKind;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.buildRelationMemberKind;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupName;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.getSocialWorkspaceMembersGroupName;

import java.util.List;

import org.junit.Test;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.computedgroups.ComputedGroupsService;
import org.nuxeo.ecm.platform.computedgroups.UserManagerWithComputedGroups;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;
import org.nuxeo.ecm.social.user.relationship.RelationshipKind;
import org.nuxeo.ecm.social.user.relationship.service.UserRelationshipService;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.computedgroups.SocialWorkspaceGroupComputer;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
public class TestSocialWorkspaceComputedGroups extends
        AbstractSocialWorkspaceTest {

    @Inject
    UserRelationshipService relationshipService;

    @Inject
    ComputedGroupsService computedGroupsService;

    @Test
    public void isComputedServiceDeployed() throws Exception {
        assertTrue(computedGroupsService.activateComputedGroups());
        assert userManager instanceof UserManagerWithComputedGroups;
    }

    @Test
    public void testSocialWorkspaceComputer() throws Exception {
        SocialWorkspaceGroupComputer computer = new SocialWorkspaceGroupComputer();

        SocialWorkspace sw = createSocialWorkspace("mySocialWorkspace", true);
        assertNotNull(sw);
        SocialWorkspace sw2 = createSocialWorkspace("mySocialWorkspace2", true);
        assertNotNull(sw);

        // Rights for SocialWorkspace: 1 admin and 2 members
        assertTrue(addBidirectionalRelation(
                ActivityHelper.createUserActivityObject("userComputer"),
                ActivityHelper.createDocumentActivityObject(sw.getDocument()),
                buildRelationAdministratorKind()));
        assertTrue(addBidirectionalRelation(
                ActivityHelper.createUserActivityObject("userComputer2"),
                ActivityHelper.createDocumentActivityObject(sw.getDocument()),
                buildRelationMemberKind()));
        assertTrue(addBidirectionalRelation(
                ActivityHelper.createUserActivityObject("userComputer3"),
                ActivityHelper.createDocumentActivityObject(sw.getDocument()),
                buildRelationMemberKind()));

        assertEquals(
                3,
                computer.getGroupMembers(
                        getSocialWorkspaceMembersGroupName(sw.getDocument())).size());

        // There is the creator and a freshly added one.
        assertEquals(
                2,
                computer.getGroupMembers(
                        getSocialWorkspaceAdministratorsGroupName(sw.getDocument())).size());

        // Right for SocialWorkspace 2: 1 admin and 0 member
        assertTrue(addBidirectionalRelation(
                ActivityHelper.createUserActivityObject("userComputer2"),
                ActivityHelper.createDocumentActivityObject(sw2.getDocument()),
                buildRelationAdministratorKind()));

        assertEquals(
                2,
                computer.getGroupMembers(
                        getSocialWorkspaceAdministratorsGroupName(sw2.getDocument())).size());
        assertEquals(
                1,
                computer.getGroupMembers(
                        getSocialWorkspaceMembersGroupName(sw2.getDocument())).size());

        DocumentModel user1 = userManager.getBareUserModel();
        user1.setProperty(userManager.getUserSchemaName(),
                userManager.getUserIdField(), "userComputer2");
        userManager.createUser(user1);
        session.save();

        NuxeoPrincipal principal = userManager.getPrincipal("userComputer2");

        List<String> groups = computer.getGroupsForUser((NuxeoPrincipalImpl) principal);

        assertEquals(2, groups.size());
        assertTrue(groups.contains(getSocialWorkspaceAdministratorsGroupName(sw2.getDocument())));
        assertTrue(groups.contains(getSocialWorkspaceMembersGroupName(sw.getDocument())));

        assertEquals(2, principal.getAllGroups().size());
    }

    private boolean addBidirectionalRelation(String actor, String target,
            RelationshipKind kind) {
        boolean ret = relationshipService.addRelation(actor, target, kind);
        ret &= relationshipService.addRelation(target, actor, kind);
        return ret;
    }
}
