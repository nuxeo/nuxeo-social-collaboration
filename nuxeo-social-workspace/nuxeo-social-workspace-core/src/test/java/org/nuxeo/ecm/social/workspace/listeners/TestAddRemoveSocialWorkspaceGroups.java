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
 *     ronan
 */
package org.nuxeo.ecm.social.workspace.listeners;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.social.workspace.AbstractSocialWorkspaceTest;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 *
 */
public class TestAddRemoveSocialWorkspaceGroups extends
        AbstractSocialWorkspaceTest {

    public static final String SOCIAL_WORKSPACE_NAME = "sws";

    @Test
    public void shouldAddAndRemoveSocialWorkspaceGroups() throws Exception {
        socialWorkspace = createSocialWorkspace(SOCIAL_WORKSPACE_NAME);
        socialWorkspaceDoc = socialWorkspace.getDocument();

        assertNotNull(userManager);
        String adminGroupName = socialWorkspace.getAdministratorsGroupName();
        DocumentModel adminGroup = userManager.getGroupModel(adminGroupName);
        assertNotNull(adminGroup);
        assertEquals(socialWorkspace.getAdministratorsGroupLabel(),
                adminGroup.getProperty(userManager.getGroupSchemaName(),
                        userManager.getGroupLabelField()));

        String membersGroupName = socialWorkspace.getMembersGroupName();
        DocumentModel membersGroup = userManager.getGroupModel(membersGroupName);
        assertNotNull(membersGroup);
        assertEquals(socialWorkspace.getMembersGroupLabel(),
                membersGroup.getProperty(userManager.getGroupSchemaName(),
                        userManager.getGroupLabelField()));

        session.removeDocument(socialWorkspaceDoc.getRef());
        session.save();

        adminGroup = userManager.getGroupModel(adminGroupName);
        assertNull(adminGroup);
        membersGroup = userManager.getGroupModel(membersGroupName);
        assertNull(membersGroup);
    }

}
