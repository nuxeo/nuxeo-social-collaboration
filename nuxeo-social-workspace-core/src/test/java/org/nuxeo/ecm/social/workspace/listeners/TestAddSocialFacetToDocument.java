/*
 * (C) Copyright 2006-2011 Nuxeo SAS (http://nuxeo.com/) and others.
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
 *     Nuxeo - initial API and implementation
 */
package org.nuxeo.ecm.social.workspace.listeners;

import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_DOCUMENT_FACET;

import org.junit.Test;
import static org.junit.Assert.*;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.social.workspace.AbstractSocialWorkspaceTest;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;

public class TestAddSocialFacetToDocument extends AbstractSocialWorkspaceTest {

    public static final String SOCIAL_WORKSPACE_NAME = "social workspace";

    protected SocialWorkspace socialWorkspace;

    private DocumentModel folderInSocialWorkspace;

    @Test
    public void testAddSocialFacet() throws Exception {

        socialWorkspace = createSocialWorkspace(SOCIAL_WORKSPACE_NAME, true);
        folderInSocialWorkspace = createDocument(socialWorkspace.getPath(),
                "Folder in Social Workspace", "SocialFolder");

        DocumentModel fileInSocialWorkspace = createDocument(
                socialWorkspace.getPath(), "Regular File in Social Workspace",
                "File");
        session.save();
        fileInSocialWorkspace = session.getDocument(fileInSocialWorkspace.getRef());
        assertTrue(fileInSocialWorkspace.hasFacet(SOCIAL_DOCUMENT_FACET));

        DocumentModel fileOutOfSocialWorkspace = createDocument("/",
                "Regular File create at root level", "File");
        assertFalse(fileOutOfSocialWorkspace.hasFacet(SOCIAL_DOCUMENT_FACET));

        DocumentModel copy = session.copy(fileOutOfSocialWorkspace.getRef(),
                socialWorkspace.getDocument().getRef(),
                "Copy of File out of Social Workspace");
        String copyId = copy.getId();
        session.save();
        copy = session.getDocument(new IdRef(copyId));

        assertNotNull(copy);
        assertTrue(copy.hasFacet(SOCIAL_DOCUMENT_FACET));
        assertFalse(fileOutOfSocialWorkspace.hasFacet(SOCIAL_DOCUMENT_FACET));

        DocumentRef swsRef = socialWorkspace.getDocument().getRef();
        DocumentModel movedFile = session.move(
                fileOutOfSocialWorkspace.getRef(), swsRef, "Moved File");
        session.save();
        movedFile = session.getDocument(movedFile.getRef());
        assertNotNull(movedFile);
        assertTrue(movedFile.hasFacet(SOCIAL_DOCUMENT_FACET));
    }

}
