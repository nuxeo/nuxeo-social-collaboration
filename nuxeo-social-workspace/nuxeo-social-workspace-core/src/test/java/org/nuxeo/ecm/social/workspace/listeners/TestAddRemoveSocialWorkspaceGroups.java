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
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.buildRelationAdministratorKind;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.buildRelationMemberKind;

import org.junit.Test;
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

        String docId = socialWorkspace.getId();
        assertEquals(1, userRelationshipService.getTargets(docId).size());
        assertEquals(
                1,
                userRelationshipService.getTargetsOfKind(docId,
                        buildRelationAdministratorKind()).size());
        assertEquals(
                0,
                userRelationshipService.getTargetsOfKind(docId,
                        buildRelationMemberKind()).size());

        session.removeDocument(socialWorkspaceDoc.getRef());
        session.save();

        assertEquals(0, userRelationshipService.getTargets(docId).size());
    }

}
