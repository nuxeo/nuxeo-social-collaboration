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

import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

/**
 * This test case aims to test the case where "userManager.getDefaultGroup()" is
 * not configured in Nuxeo distribution.
 *
 * @author rlegall@nuxeo.com
 */
@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.platform.content.template",
        "org.nuxeo.ecm.social.workspace.core" })
public class TestRightsWithoutDefaultGroup extends AbstractSocialWorkspaceTest {

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
    public void testPublicSectionRights() throws Exception {
        PathRef publicSectionPathRef = new PathRef(
                socialWorkspace.getPublicSectionPath());
        assertTrue(session.exists(publicSectionPathRef));
        assertNotNull(session.getDocument(publicSectionPathRef));

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

        socialWorkspace.makePublic();

        assertTrue(session.hasPermission(nobody, publicSectionPathRef, READ));
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
    }

}
