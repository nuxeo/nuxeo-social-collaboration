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

import java.security.Principal;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.google.inject.Inject;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import static org.nuxeo.ecm.core.api.security.SecurityConstants.EVERYTHING;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ_WRITE;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.WRITE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;
import static org.nuxeo.ecm.social.workspace.ToolsForTests.createDocumentModel;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupName;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.getSocialWorkspaceMembersGroupName;

@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.platform.content.template",
        "org.nuxeo.ecm.social.workspace.core" })
@LocalDeploy("org.nuxeo.ecm.social.workspace.core:test-social-workspace-usermanager-contrib.xml")
public class TestNewsManagement {

    @Inject
    protected CoreSession session;

    @Inject
    protected UserManager userManager;

    @Inject
    protected FeaturesRunner featuresRunner;

    protected DocumentModel socialWorkspace;

    protected Principal nobody;

    protected Principal applicationMember;

    protected Principal swMember;

    protected Principal swAdministrator;

    @Before
    public void setup() throws Exception {

        socialWorkspace = createDocumentModel(session,
                session.getRootDocument().getPathAsString(),
                "SocialWorkspace", SOCIAL_WORKSPACE_TYPE);

        nobody = new NuxeoPrincipalImpl("user");
        applicationMember = createUserWithGroup(userManager.getDefaultGroup());
        swMember = createUserWithGroup(getSocialWorkspaceMembersGroupName(socialWorkspace));
        swAdministrator = createUserWithGroup(getSocialWorkspaceAdministratorsGroupName(socialWorkspace));

        NuxeoPrincipal principal = (NuxeoPrincipal) session.getPrincipal();
        principal.getGroups().add(getSocialWorkspaceAdministratorsGroupName(socialWorkspace));

    }

    @Test
    public void testSocialWorkspaceRights() throws Exception {

        assertFalse(session.hasPermission(nobody, socialWorkspace.getRef(), READ));
        assertFalse(session.hasPermission(applicationMember, socialWorkspace.getRef(), READ));
        assertTrue(session.hasPermission(swMember, socialWorkspace.getRef(), READ_WRITE));
        assertFalse(session.hasPermission(swMember, socialWorkspace.getRef(), EVERYTHING));
        assertTrue(session.hasPermission(swAdministrator, socialWorkspace.getRef(), EVERYTHING));
    }

    @Test
    public void testPublicSectionRights() throws Exception {

        PathRef publicSectionPath = SocialWorkspaceHelper.getPublicSectionPath(socialWorkspace);
        assertTrue(session.exists(publicSectionPath));
        assertNotNull(session.getDocument(publicSectionPath));

        assertFalse(session.hasPermission(nobody, publicSectionPath, READ));
        assertTrue(session.hasPermission(applicationMember, publicSectionPath, READ));
        assertFalse(session.hasPermission(applicationMember, publicSectionPath, READ_WRITE));
        assertTrue(session.hasPermission(swMember, publicSectionPath, READ_WRITE));
        assertFalse(session.hasPermission(swMember, publicSectionPath, EVERYTHING));
        assertTrue(session.hasPermission(swAdministrator, publicSectionPath, EVERYTHING));
    }

    @Test
    public void testPrivateSectionRights() throws Exception {

        PathRef privateSectionPath = SocialWorkspaceHelper.getPrivateSectionPath(socialWorkspace);
        assertTrue(session.exists(privateSectionPath));
        assertNotNull(session.getDocument(privateSectionPath));

        assertFalse(session.hasPermission(nobody, privateSectionPath, READ));
        assertFalse(session.hasPermission(applicationMember, privateSectionPath, READ));
        assertTrue(session.hasPermission(swMember, privateSectionPath, READ_WRITE));
        assertFalse(session.hasPermission(swMember, privateSectionPath, EVERYTHING));
        assertTrue(session.hasPermission(swAdministrator, privateSectionPath, EVERYTHING));
    }

    @Test
    public void testNewsRootRights() throws Exception {

        PathRef newsRootPath = SocialWorkspaceHelper.getNewsRootPath(socialWorkspace);
        assertTrue(session.exists(newsRootPath));
        assertNotNull(session.getDocument(newsRootPath));

        assertFalse(session.hasPermission(nobody, newsRootPath, READ));
        assertFalse(session.hasPermission(applicationMember, newsRootPath, READ));
        assertTrue(session.hasPermission(swMember, newsRootPath, READ));
        assertFalse(session.hasPermission(swMember, newsRootPath, WRITE));
        assertFalse(session.hasPermission(swMember, newsRootPath, EVERYTHING));
        assertTrue(session.hasPermission(swAdministrator, newsRootPath, EVERYTHING));
    }

    protected Principal createUserWithGroup(String groupName) throws ClientException {
        NuxeoPrincipalImpl user = new NuxeoPrincipalImpl("user");
        user.allGroups = Arrays.asList(groupName);
        return user;
    }
}
