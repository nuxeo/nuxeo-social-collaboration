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

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.social.workspace.AbstractSocialWorkspaceTest;
import org.nuxeo.runtime.test.runner.LocalDeploy;

/**
 * @author Benjamin JALON <bjalon@nuxeo.com>
 *
 */
@LocalDeploy("org.nuxeo.ecm.social.workspace.core:test-social-workspace-usermanager-contrib.xml")
public class TestSocialWorkspaceHelper extends AbstractSocialWorkspaceTest {

    @Before
    public void setup() throws Exception {
        socialWorkspace = createSocialWorkspace("Socialworkspace for test");
        socialWorkspaceDoc = socialWorkspace.getDocument();
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
