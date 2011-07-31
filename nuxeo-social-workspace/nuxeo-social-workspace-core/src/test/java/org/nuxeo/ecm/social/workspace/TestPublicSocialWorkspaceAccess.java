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

import java.security.Principal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.core.api.impl.UserPrincipal;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;
import org.nuxeo.runtime.api.Framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.nuxeo.ecm.core.api.security.SecurityConstants.EVERYTHING;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ;

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

        SocialWorkspace sw = createSocialWorkspaceWithoutRightForUser("marketing", true);
        assertFalse(session.hasPermission(sw.getDocument().getRef(), READ));

        sw = createSocialWorkspace("sales", true);
        assertTrue(session.hasPermission(sw.getDocument().getRef(), EVERYTHING));
    }

    @Test
    public void shouldReturnPublicSocialWorkspace() throws Exception {
        // TODO : Try to find why query return all documents (no right check)
//        String query = String.format("Select * From %s ", SocialConstants.SOCIAL_WORKSPACE_TYPE);
//
//        DocumentModelList docs = session.query(query);
//        assertEquals(1, docs.size());

        SocialWorkspaceService service = Framework.getService(SocialWorkspaceService.class);
        List<SocialWorkspace> socialWorkspaces = service.getDetachedPublicSocialWorkspaces(session);
        assertEquals(2, socialWorkspaces.size());

        // TODO : Try to find why query fulltext doesn't work
//        socialWorkspaces = service.searchDetachedPublicSocialWorkspaces(
//                session, "marketing");
//        assertEquals(1, socialWorkspaces.size());
    }

}
