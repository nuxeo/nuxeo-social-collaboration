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
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;
import static org.nuxeo.ecm.social.workspace.ToolsForTests.createDocumentModel;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialWorkspace;

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
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.google.inject.Inject;

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

    protected DocumentModel socialWorkspaceDoc;

    protected Principal nobody;

    protected Principal applicationMember;

    protected Principal swMember;

    protected Principal swAdministrator;

    @Before
    public void setup() throws Exception {

        socialWorkspaceDoc = createDocumentModel(session,
                session.getRootDocument().getPathAsString(), "SocialWorkspace",
                SOCIAL_WORKSPACE_TYPE);
        SocialWorkspace socialWorkspace = toSocialWorkspace(socialWorkspaceDoc);

        nobody = new NuxeoPrincipalImpl("user");
        applicationMember = createUserWithGroup(userManager.getDefaultGroup());
        swMember = createUserWithGroup(socialWorkspace.getMembersGroupName());
        swAdministrator = createUserWithGroup(socialWorkspace.getAdministratorsGroupName());

        NuxeoPrincipal principal = (NuxeoPrincipal) session.getPrincipal();
        principal.getGroups().add(socialWorkspace.getAdministratorsGroupName());

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
    public void testPublicSectionRights() throws Exception {
        SocialWorkspace socialWorkspace = toSocialWorkspace(socialWorkspaceDoc);
        PathRef publicSectionPathRef = new PathRef(
                socialWorkspace.getPublicSectionPath());
        assertTrue(session.exists(publicSectionPathRef));
        assertNotNull(session.getDocument(publicSectionPathRef));

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
    }

    @Test
    public void testPrivateSectionRights() throws Exception {
        SocialWorkspace socialWorkspace = toSocialWorkspace(socialWorkspaceDoc);
        PathRef privateSectionPathRef = new PathRef(
                socialWorkspace.getPrivateSectionPath());
        assertTrue(session.exists(privateSectionPathRef));
        assertNotNull(session.getDocument(privateSectionPathRef));

        assertFalse(session.hasPermission(nobody, privateSectionPathRef, READ));
        assertFalse(session.hasPermission(applicationMember,
                privateSectionPathRef, READ));
        assertTrue(session.hasPermission(swMember, privateSectionPathRef,
                READ_WRITE));
        assertFalse(session.hasPermission(swMember, privateSectionPathRef,
                EVERYTHING));
        assertTrue(session.hasPermission(swAdministrator,
                privateSectionPathRef, EVERYTHING));
    }

    @Test
    public void testNewsRootRights() throws Exception {
        SocialWorkspace socialWorkspace = toSocialWorkspace(socialWorkspaceDoc);
        PathRef newsRootPathRef = new PathRef(socialWorkspace.getNewsRootPath());

        assertTrue(session.exists(newsRootPathRef));
        assertNotNull(session.getDocument(newsRootPathRef));

        assertFalse(session.hasPermission(nobody, newsRootPathRef, READ));
        assertFalse(session.hasPermission(applicationMember, newsRootPathRef,
                READ));
        assertTrue(session.hasPermission(swMember, newsRootPathRef, READ));
        assertFalse(session.hasPermission(swMember, newsRootPathRef, WRITE));
        assertFalse(session.hasPermission(swMember, newsRootPathRef, EVERYTHING));
        assertTrue(session.hasPermission(swAdministrator, newsRootPathRef,
                EVERYTHING));
    }

    protected Principal createUserWithGroup(String groupName)
            throws ClientException {
        NuxeoPrincipalImpl user = new NuxeoPrincipalImpl("user");
        user.allGroups = Arrays.asList(groupName);
        return user;
    }
}
