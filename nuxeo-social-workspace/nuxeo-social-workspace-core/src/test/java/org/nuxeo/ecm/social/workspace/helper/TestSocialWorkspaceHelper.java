/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Benjamin JALON <bjalon@nuxeo.com>
 */
package org.nuxeo.ecm.social.workspace.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.buildRelationAdministratorKind;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.buildRelationKindFromGroupName;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.buildRelationMemberKind;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.getRelationDocActivityObjectFromGroupName;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupName;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.getSocialWorkspaceMembersGroupName;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.isValidSocialWorkspaceGroupName;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.social.workspace.AbstractSocialWorkspaceTest;
import org.nuxeo.runtime.test.runner.LocalDeploy;

/**
 * @author Benjamin JALON <bjalon@nuxeo.com>
 *
 */
@LocalDeploy("org.nuxeo.ecm.social.workspace.core:test-social-workspace-usermanager-contrib.xml")
public class TestSocialWorkspaceHelper extends AbstractSocialWorkspaceTest {

    @Before
    public void setup() throws Exception {
        socialWorkspace = createSocialWorkspace("Social Workspace for test");
        socialWorkspaceDoc = socialWorkspace.getDocument();
    }

    @Test
    public void testShouldReturnGroupNames() {
        String idSW = socialWorkspaceDoc.getId();
        String labelSW = socialWorkspaceDoc.getName();

        assertEquals(
                SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupName(socialWorkspaceDoc),
                socialWorkspace.getAdministratorsGroupName());
        assertEquals("Administrators of " + labelSW,
                socialWorkspace.getAdministratorsGroupLabel());
        assertEquals(
                SocialWorkspaceHelper.getSocialWorkspaceMembersGroupName(socialWorkspaceDoc),
                socialWorkspace.getMembersGroupName());
        assertEquals("Members of " + labelSW,
                socialWorkspace.getMembersGroupLabel());
    }

    @Test
    public void testGroupsMethods() {
        String admGroupName = getSocialWorkspaceAdministratorsGroupName("32-34");
        String memGroupName = getSocialWorkspaceMembersGroupName("33223-343244");
        String other = "bla-bla_trucm";

        assertTrue(isValidSocialWorkspaceGroupName(admGroupName));
        assertTrue(isValidSocialWorkspaceGroupName(memGroupName));
        assertFalse(isValidSocialWorkspaceGroupName(other));

        assertEquals("32-34",
                getRelationDocActivityObjectFromGroupName(admGroupName));
        assertEquals("33223-343244",
                getRelationDocActivityObjectFromGroupName(memGroupName));

        assertEquals(buildRelationAdministratorKind(),
                buildRelationKindFromGroupName(admGroupName));
        assertNotSame(buildRelationMemberKind(),
                buildRelationKindFromGroupName(memGroupName));
        assertEquals(buildRelationMemberKind(),
                buildRelationKindFromGroupName(memGroupName));
        assertNotSame(buildRelationAdministratorKind(),
                buildRelationKindFromGroupName(memGroupName));
    }

}
