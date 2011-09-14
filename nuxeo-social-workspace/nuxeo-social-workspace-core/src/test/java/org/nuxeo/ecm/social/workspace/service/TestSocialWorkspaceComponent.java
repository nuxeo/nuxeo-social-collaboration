/*
 * (C) Copyright 2006-2011 Nuxeo SAS (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 */
package org.nuxeo.ecm.social.workspace.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ_WRITE;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.common.collections.ScopeType;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.platform.ec.notification.service.NotificationServiceHelper;
import org.nuxeo.ecm.platform.url.api.DocumentViewCodecManager;
import org.nuxeo.ecm.social.workspace.AbstractSocialWorkspaceTest;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.LocalDeploy;

@Deploy({ "org.nuxeo.ecm.automation.core", "org.nuxeo.ecm.automation.features",
        "org.nuxeo.ecm.platform.url.api", "org.nuxeo.ecm.platform.url.core",
        "org.nuxeo.ecm.platform.notification.core" })
@LocalDeploy({
        "org.nuxeo.ecm.social.workspace.core:test-social-workspace-listener-contrib.xml",
        "org.nuxeo.ecm.social.workspace.core:test-social-workspace-service-contrib.xml" })
public class TestSocialWorkspaceComponent extends AbstractSocialWorkspaceTest {

    @Inject
    SocialWorkspaceService socialWorkspaceService;

    @Inject
    EventService eventService;

    @Before
    public void testSetUp() {
        ImportEventListener.reset();
    }

    @Test
    public void testService() {
        // Services needed for testing SendMail operation
        assertNotNull(Framework.getLocalService(DocumentViewCodecManager.class));
        assertNotNull(NotificationServiceHelper.getNotificationService());
    }

    @Test
    public void testAddSeveralSocialWorkspaceMembers() throws Exception {
        assertEquals(0, ImportEventListener.getMemberAddedCount());
        SocialWorkspace socialWorkspace = createSocialWorkspace("Social workspace for test");
        assertTrue(socialWorkspace.isMembersNotificationEnabled());

        String userAlreadyMember1Email = "userAlreadyMember1@mail.net";
        DocumentModel userAlreadyMember1 = createUserForTest(
                userAlreadyMember1Email, "userAlreadyMember1");
        socialWorkspace.addMember(userManager.getPrincipal(userAlreadyMember1.getId()));
        assertEquals(2, ImportEventListener.getMemberAddedCount());

        socialWorkspace.getDocument().putContextData(ScopeType.REQUEST,
                "memberNotificationDisabled", true);
        assertFalse(socialWorkspace.isMembersNotificationEnabled());

        DocumentModel userAlreadyMember2 = createUserForTest(
                "userAlreadyMember2@mail.net", "userAlreadyMember2");
        socialWorkspace.addMember(userManager.getPrincipal(userAlreadyMember2.getId()));

        assertEquals(2, ImportEventListener.getMemberAddedCount());
        socialWorkspace.getDocument().putContextData(ScopeType.REQUEST,
                "memberNotificationDisabled", false);

        DocumentModel fulltextEmailUser1 = createUserForTest(
                "fulltextEmailUser1@mail.net", "fulltextEmailUser1");

        String userNewMember1Email = "userNewMember1@mail.net";
        createUserForTest(userNewMember1Email, "userNewMember1");
        createUserForTest("userNewMember2@mail.net", "userNewMember2");

        String nonExsitingUser1Email = "nonExistingUser1@mail.net";
        List<String> emails = Arrays.asList(userAlreadyMember1Email,
                "userAlreadyMember2@mail.net", "userNewMember1@mail.net",
                "userNewMember2@mail.net", nonExsitingUser1Email,
                "nonExistingUser2@mail.net", fulltextEmailUser1.getId());

        List<String> addedUsers = socialWorkspaceService.addSocialWorkspaceMembers(
                socialWorkspace, emails);
        assertEquals(3, addedUsers.size());
        assertEquals(5, ImportEventListener.getMemberAddedCount());
        assertEquals(3, ImportEventListener.getLastPrincipalsCount());
        assertFalse(addedUsers.contains(userAlreadyMember1Email));
        assertFalse(addedUsers.contains(nonExsitingUser1Email));
        assertTrue(addedUsers.contains(userNewMember1Email));

        addedUsers = socialWorkspaceService.addSocialWorkspaceMembers(
                socialWorkspace, emails);
        assertTrue(addedUsers.isEmpty());

        addedUsers = socialWorkspaceService.addSocialWorkspaceMembers(
                socialWorkspace, new ArrayList<String>());
        assertTrue(addedUsers.isEmpty());
    }

    @Test(expected = ClientException.class)
    public void testaddSocialWorkspaceMembersFromGroup() throws Exception {
        assertEquals(0, ImportEventListener.getMemberAddedCount());

        String existingUser1 = "userAlreadyMember1";
        String existingUser2 = "userAlreadyMember2";

        DocumentModel group1 = userManager.getBareGroupModel();
        group1.setPropertyValue(userManager.getGroupSchemaName() + ":"
                + userManager.getGroupIdField(), "group1");
        List<String> members = Arrays.asList(existingUser1, existingUser2,
                "Administrator", "unknown");
        group1.setProperty(userManager.getGroupSchemaName(),
                userManager.getGroupMembersField(), members);

        group1 = userManager.createGroup(group1);
        members = (List<String>) group1.getPropertyValue(userManager.getGroupSchemaName()
                + ":" + userManager.getGroupMembersField());
        assertEquals(4, members.size());

        SocialWorkspace sw = createSocialWorkspace("SocialWorkspaceWithGroup");
        assertEquals(1, sw.getMembers().size());

        int beforeImportCount = ImportEventListener.getMemberAddedCount();
        sw.getDocument().putContextData("allowMemberNotification", false);
        List<String> imported = socialWorkspaceService.addSocialWorkspaceMembers(
                sw, group1.getName());
        assertEquals(2, imported.size());
        assertEquals(beforeImportCount,
                ImportEventListener.getMemberAddedCount());
        assertTrue(imported.contains(existingUser1));
        assertTrue(imported.contains(existingUser2));
        assertFalse(imported.contains("Administrator"));

        assertEquals(3, sw.getMembers().size());

        // Will throw a ClientException
        socialWorkspaceService.addSocialWorkspaceMembers(sw, "john_doe_group");
    }

    @Test
    public void testSocialWorkspaceContainer() throws ClientException {
        Principal john = createUserWithGroup("JohnDoe", "members");
        Principal polo = createUserWithGroup("PoloDoe", "trash");
        assertFalse(session.exists(new PathRef(
                "/default-domain/test-social-workspaces")));
        DocumentModel container = socialWorkspaceService.getOrCreateSocialWorkspaceContainer(session);
        assertTrue(session.hasPermission(john, container.getRef(), READ_WRITE));
        assertFalse(session.hasPermission(polo, container.getRef(), READ_WRITE));
        assertEquals("test-social-workspaces", container.getTitle());
        assertEquals("/default-domain",
                session.getDocument(container.getParentRef()).getPathAsString());
        assertTrue(session.exists(new PathRef(
                "/default-domain/test-social-workspaces")));
    }

    protected DocumentModel createUserForTest(String userEmail, String userId)
            throws ClientException {
        DocumentModel user = userManager.getBareUserModel();
        user.setPropertyValue(userManager.getUserSchemaName() + ":"
                + userManager.getUserEmailField(), userEmail);
        user.setPropertyValue(userManager.getUserSchemaName() + ":"
                + userManager.getUserIdField(), userId);

        return userManager.createUser(user);
    }

}
