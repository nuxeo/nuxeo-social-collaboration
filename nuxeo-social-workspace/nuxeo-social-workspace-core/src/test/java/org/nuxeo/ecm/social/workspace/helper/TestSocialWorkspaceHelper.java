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
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;
import static org.nuxeo.ecm.social.workspace.ToolsForTests.createDocumentModel;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialWorkspace;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.google.inject.Inject;

/**
 * @author Benjamin JALON <bjalon@nuxeo.com>
 *
 */
@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.platform.content.template",
        "org.nuxeo.ecm.social.workspace.core" })
@LocalDeploy("org.nuxeo.ecm.social.workspace.core:test-social-workspace-usermanager-contrib.xml")
public class TestSocialWorkspaceHelper {

    @Inject
    protected CoreSession session;

    protected DocumentModel socialWorkspaceDoc;

    protected SocialWorkspace socialWorkspace;

    @Before
    public void setup() throws Exception {
        socialWorkspaceDoc = createDocumentModel(session,
                session.getRootDocument().getPathAsString(),
                "Socialworkspace for test", SOCIAL_WORKSPACE_TYPE);
        socialWorkspace = toSocialWorkspace(socialWorkspaceDoc);
    }

    @Test
    public void testShouldReturnGroupNames() {
        String idSW = socialWorkspaceDoc.getId();
        String labelSW = socialWorkspaceDoc.getName();

        assertEquals(idSW + "_administrators",
                socialWorkspace.getAdministratorsGroupName());
        assertEquals("Administrators of " + labelSW,
                socialWorkspace.getAdministratorsGroupLabel());
        assertEquals(idSW + "_members", socialWorkspace.getMembersGroupName());
        assertEquals("Members of " + labelSW,
                socialWorkspace.getMembersGroupLabel());
    }

}
