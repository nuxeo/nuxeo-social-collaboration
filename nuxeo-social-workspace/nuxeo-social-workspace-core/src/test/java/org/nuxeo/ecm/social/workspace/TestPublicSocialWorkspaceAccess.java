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
package org.nuxeo.ecm.social.workspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.security.Principal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.UserPrincipal;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;
import org.nuxeo.runtime.api.Framework;

/**
 * @author Benjamin JALON <bjalon@nuxeo.com>
 *
 */
public class TestPublicSocialWorkspaceAccess extends
        AbstractSocialWorkspaceTest {

    @Before
    public void setup() throws Exception {
        Principal user = session.getPrincipal();
        assertFalse(((UserPrincipal) user).isAdministrator());

        createSocialWorkspace("marketing", true);
        createSocialWorkspace("sales", false);

        session.getRootDocument().getACP().getOrCreateACL().add(
                new ACE("John", "READ", true));
        session.setACP(session.getRootDocument().getRef(),
                session.getRootDocument().getACP(), true);
        session.save();
    }

    @Test
    public void shouldReturnPublicSocialWorkspace() throws Exception {
        CoreSession newSession = openSessionAs("John");

        SocialWorkspaceService service = Framework.getService(SocialWorkspaceService.class);
        List<SocialWorkspace> socialWorkspaces = service.getDetachedPublicSocialWorkspaces(newSession);
        assertEquals(1, socialWorkspaces.size());

        String query = String.format("Select * From %s ",
                SocialConstants.SOCIAL_WORKSPACE_TYPE);
        DocumentModelList docs = newSession.query(query);
        assertEquals(0, docs.size());

        socialWorkspaces = service.searchDetachedPublicSocialWorkspaces(
                newSession, "marketing");
        assertEquals(1, socialWorkspaces.size());

        CoreInstance.getInstance().close(newSession);
    }

}
