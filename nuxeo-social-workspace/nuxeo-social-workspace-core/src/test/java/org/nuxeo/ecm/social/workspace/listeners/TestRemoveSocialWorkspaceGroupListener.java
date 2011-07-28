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
 *     Benjamin JALON <bjalon@nuxeo.com>
 */
package org.nuxeo.ecm.social.workspace.listeners;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.AbstractSocialWorkspaceTest;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


/**
 * @author Benjamin JALON <bjalon@nuxeo.com>
 *
 */
@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy("org.nuxeo.ecm.social.workspace.core")
public class TestRemoveSocialWorkspaceGroupListener extends
        AbstractSocialWorkspaceTest {

    @Inject
    protected EventService eventService;

    @Inject
    protected UserManager userManager;

    @Before
    public void setup() throws Exception {
        socialWorkspace = createSocialWorkspace("test");
        socialWorkspaceDoc = socialWorkspace.getDocument();
    }

    @Test
    public void testRemoveMethodGroups() throws ClientException {

        assertNotNull(userManager.getGroupModel(socialWorkspace.getAdministratorsGroupName()));
        assertNotNull(userManager.getGroupModel(socialWorkspace.getMembersGroupName()));

        session.removeDocument(socialWorkspaceDoc.getRef());
        eventService.waitForAsyncCompletion();

        assertNull(userManager.getGroupModel(socialWorkspace.getAdministratorsGroupName()));
        assertNull(userManager.getGroupModel(socialWorkspace.getMembersGroupName()));
    }

}
