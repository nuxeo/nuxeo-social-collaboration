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
import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ_WRITE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;
import static org.nuxeo.ecm.social.workspace.ToolsForTests.createDocumentModel;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialWorkspace;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

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
public class TestNewsManagementWithoutLocalConfig {

    private static final String BASE_WORKSPACE_NAME = "base";

    public static final String TEST_NAME_SOCIAL_WORKSPACE = "socialworkspace";

    @Inject
    protected CoreSession session;

    @Inject
    protected UserManager userManager;

    protected DocumentModel socialWorkspaceDoc;

    @Before
    public void setup() throws Exception {
        DocumentModel workspace = createDocumentModel(session,
                session.getRootDocument().getPathAsString(),
                BASE_WORKSPACE_NAME, "Workspace");

        socialWorkspaceDoc = createDocumentModel(session,
                workspace.getPathAsString(), TEST_NAME_SOCIAL_WORKSPACE,
                SOCIAL_WORKSPACE_TYPE);
    }

    @Test
    public void testRightsOnSocialSections() throws Exception {
        SocialWorkspace socialWorkspace = toSocialWorkspace(socialWorkspaceDoc);

        DocumentModel privateSection = session.getDocument(new PathRef(
                socialWorkspace.getPrivateSectionPath()));
        assertNotNull(privateSection);
        DocumentModel publicSection = session.getDocument(new PathRef(
                socialWorkspace.getPublicSectionPath()));
        assertNotNull(privateSection);

        ACP acp = privateSection.getACP();
        assertFalse(acp.getAccess(userManager.getDefaultGroup(), READ).toBoolean());

        assertFalse(acp.getAccess("u,uuie,", READ_WRITE).toBoolean());
        assertTrue(
                "The members of the social workspace should have the READ_WRIGHT right",
                acp.getAccess(socialWorkspace.getMembersGroupName(), READ_WRITE).toBoolean());
        assertTrue(acp.getAccess(socialWorkspace.getAdministratorsGroupName(),
                READ).toBoolean());

        acp = publicSection.getACP();
        assertTrue(acp.getAccess("u,uuie,", READ).toBoolean());
        assertTrue(acp.getAccess(userManager.getDefaultGroup(), READ).toBoolean());
        assertTrue(acp.getAccess(socialWorkspace.getMembersGroupName(),
                READ_WRITE).toBoolean());
        assertTrue(acp.getAccess(socialWorkspace.getAdministratorsGroupName(),
                READ_WRITE).toBoolean());
    }

}
