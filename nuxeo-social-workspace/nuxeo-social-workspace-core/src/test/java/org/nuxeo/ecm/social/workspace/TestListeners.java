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
 *     eugen
 */
package org.nuxeo.ecm.social.workspace;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 *
 */
@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.social.workspace.core" })
public class TestListeners {

    public static final String NAME_SOCIAL_WORKSPACE = "sws";

    @Inject
    protected CoreSession session;

    @Inject
    protected UserManager userManager;

    @Test
    public void testGroupListeners() throws Exception {
        DocumentModel sws = session.createDocumentModel(
                session.getRootDocument().getPathAsString(),
                NAME_SOCIAL_WORKSPACE, "SocialWorkspace");
        sws = session.createDocument(sws);
        session.save();
        assertNotNull(userManager);
        String adminGroupName = SocialWorkspaceHelper.getCommunityAdministratorsGroupName(sws);
        NuxeoGroup adminGroup = userManager.getGroup(adminGroupName);
        assertNotNull(adminGroup);

        String membersGroupName = SocialWorkspaceHelper.getCommunityMembersGroupName(sws);
        NuxeoGroup membersGroup = userManager.getGroup(membersGroupName);
        assertNotNull(membersGroup);

        session.removeDocument(sws.getRef());
        session.save();

        adminGroup = userManager.getGroup(adminGroupName);
        assertNull(adminGroup);
        membersGroup = userManager.getGroup(membersGroupName);
        assertNull(membersGroup);
    }

}
