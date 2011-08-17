/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Ronan
 */
package org.nuxeo.ecm.social.workspace;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.EVERYTHING;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ_WRITE;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.WRITE;

import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;
import org.nuxeo.runtime.test.runner.LocalDeploy;

@LocalDeploy("org.nuxeo.ecm.social.workspace.core:test-social-workspace-usermanager-contrib.xml")
public class TestRights extends AbstractSocialWorkspaceTest {

    protected Principal nobody;

    protected Principal applicationMember;

    protected Principal swMember;

    protected Principal swAdministrator;

    @Before
    public void setup() throws Exception {
        socialWorkspace = createSocialWorkspace("SocialWorkspace");
        socialWorkspaceDoc = socialWorkspace.getDocument();

        nobody = new NuxeoPrincipalImpl("nobody");
        applicationMember = createUserWithGroup("applicationMember",
                userManager.getDefaultGroup());
        swMember = createUserWithGroup("swMember",
                socialWorkspace.getMembersGroupName());
        swAdministrator = createUserWithGroup("swAdministrator",
                socialWorkspace.getAdministratorsGroupName());
    }

    @Test
    public void testSocialWorkspaceRights() throws Exception {
        assertFalse(session.hasPermission(nobody, socialWorkspaceDoc.getRef(),
                READ));
        assertFalse(session.hasPermission(applicationMember,
                socialWorkspaceDoc.getRef(), READ));
        assertTrue(session.hasPermission(swMember, socialWorkspaceDoc.getRef(),
                READ_WRITE));
        assertFalse(session.hasPermission(swMember,
                socialWorkspaceDoc.getRef(), EVERYTHING));
        assertTrue(session.hasPermission(swAdministrator,
                socialWorkspaceDoc.getRef(), EVERYTHING));
    }

    @Test
    public void testPublicSocialWorkspaceRights() throws Exception {
        PathRef publicSectionPathRef = new PathRef(
                socialWorkspace.getPublicSectionPath());
        assertTrue(session.exists(publicSectionPathRef));
        assertNotNull(session.getDocument(publicSectionPathRef));
        PathRef publicDashboardPathRef = new PathRef(
                socialWorkspace.getPublicDashboardSpacePath());
        assertTrue(session.exists(publicDashboardPathRef));
        assertNotNull(session.getDocument(publicDashboardPathRef));

        // public section
        assertFalse(session.hasPermission(nobody, publicSectionPathRef, READ));
        assertFalse(session.hasPermission(applicationMember,
                publicSectionPathRef, READ));
        assertFalse(session.hasPermission(applicationMember,
                publicSectionPathRef, READ_WRITE));
        assertTrue(session.hasPermission(swMember, publicSectionPathRef,
                READ_WRITE));
        assertFalse(session.hasPermission(swMember, publicSectionPathRef,
                EVERYTHING));
        assertTrue(session.hasPermission(swAdministrator, publicSectionPathRef,
                EVERYTHING));

        // public dashboard
        assertFalse(session.hasPermission(nobody, publicDashboardPathRef, READ));
        assertFalse(session.hasPermission(applicationMember,
                publicDashboardPathRef, READ));
        assertFalse(session.hasPermission(applicationMember,
                publicDashboardPathRef, READ_WRITE));
        assertTrue(session.hasPermission(swMember, publicDashboardPathRef,
                READ_WRITE));
        assertFalse(session.hasPermission(swMember, publicDashboardPathRef,
                EVERYTHING));
        assertTrue(session.hasPermission(swAdministrator,
                publicDashboardPathRef, EVERYTHING));

        socialWorkspace.makePublic();

        // public section
        assertFalse(session.hasPermission(nobody, publicSectionPathRef, READ));
        assertTrue(session.hasPermission(applicationMember,
                publicSectionPathRef, READ));
        assertFalse(session.hasPermission(applicationMember,
                publicSectionPathRef, READ_WRITE));
        assertTrue(session.hasPermission(swMember, publicSectionPathRef,
                READ_WRITE));
        assertFalse(session.hasPermission(swMember, publicSectionPathRef,
                EVERYTHING));
        assertTrue(session.hasPermission(swAdministrator, publicSectionPathRef,
                EVERYTHING));

        // public dashboard
        assertFalse(session.hasPermission(nobody, publicDashboardPathRef, READ));
        assertTrue(session.hasPermission(applicationMember,
                publicDashboardPathRef, READ));
        assertFalse(session.hasPermission(applicationMember,
                publicDashboardPathRef, READ_WRITE));
        assertTrue(session.hasPermission(swMember, publicDashboardPathRef,
                READ_WRITE));
        assertFalse(session.hasPermission(swMember, publicDashboardPathRef,
                EVERYTHING));
        assertTrue(session.hasPermission(swAdministrator,
                publicDashboardPathRef, EVERYTHING));
    }

    @Test
    public void testPrivateSocialWorkspaceRights() throws Exception {
        PathRef privateSectionPathRef = new PathRef(
                socialWorkspace.getPrivateSectionPath());
        assertTrue(session.exists(privateSectionPathRef));
        assertNotNull(session.getDocument(privateSectionPathRef));
        PathRef privateDashboardPathRef = new PathRef(
                socialWorkspace.getPrivateDashboardSpacePath());
        assertTrue(session.exists(privateDashboardPathRef));
        assertNotNull(session.getDocument(privateDashboardPathRef));

        // private section
        assertFalse(session.hasPermission(nobody, privateSectionPathRef, READ));
        assertFalse(session.hasPermission(applicationMember,
                privateSectionPathRef, READ));
        assertTrue(session.hasPermission(swMember, privateSectionPathRef,
                READ_WRITE));
        assertFalse(session.hasPermission(swMember, privateSectionPathRef,
                EVERYTHING));
        assertTrue(session.hasPermission(swAdministrator,
                privateSectionPathRef, EVERYTHING));

        // private dashboard
        assertFalse(session.hasPermission(nobody, privateDashboardPathRef, READ));
        assertFalse(session.hasPermission(applicationMember,
                privateDashboardPathRef, READ));
        assertTrue(session.hasPermission(swMember, privateDashboardPathRef,
                READ_WRITE));
        assertFalse(session.hasPermission(swMember, privateDashboardPathRef,
                EVERYTHING));
        assertTrue(session.hasPermission(swAdministrator,
                privateDashboardPathRef, EVERYTHING));
    }

    @Test
    public void testNewsItemsRootRights() throws Exception {
        PathRef newsItemsRootPathRef = new PathRef(
                socialWorkspace.getNewsItemsRootPath());

        assertTrue(session.exists(newsItemsRootPathRef));
        assertNotNull(session.getDocument(newsItemsRootPathRef));

        assertFalse(session.hasPermission(nobody, newsItemsRootPathRef, READ));
        assertFalse(session.hasPermission(applicationMember,
                newsItemsRootPathRef, READ));
        assertTrue(session.hasPermission(swMember, newsItemsRootPathRef, READ));
        assertFalse(session.hasPermission(swMember, newsItemsRootPathRef, WRITE));
        assertFalse(session.hasPermission(swMember, newsItemsRootPathRef,
                EVERYTHING));
        assertTrue(session.hasPermission(swAdministrator, newsItemsRootPathRef,
                EVERYTHING));
    }

}
