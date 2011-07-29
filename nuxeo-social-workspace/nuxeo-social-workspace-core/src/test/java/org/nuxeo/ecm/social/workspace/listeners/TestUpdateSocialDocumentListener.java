/*
 * (C) Copyright 2011 Nuxeo SA (http:nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http:www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package org.nuxeo.ecm.social.workspace.listeners;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static org.nuxeo.ecm.social.workspace.SocialConstants.NEWS_ITEM_TYPE;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialDocument;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.common.collections.ScopeType;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.social.workspace.AbstractSocialWorkspaceTest;
import org.nuxeo.ecm.social.workspace.SocialConstants;
import org.nuxeo.ecm.social.workspace.adapters.SocialDocument;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.google.inject.Inject;

@LocalDeploy("org.nuxeo.ecm.social.workspace.core:test-social-workspace-usermanager-contrib.xml")
public class TestUpdateSocialDocumentListener extends
        AbstractSocialWorkspaceTest {

    @Inject
    protected EventService eventService;

    protected DocumentModel privateSection;

    protected DocumentModel publicSection;

    @Before
    public void setup() throws Exception {
        socialWorkspace = createSocialWorkspace("Socialworkspace for test");
        socialWorkspaceDoc = socialWorkspace.getDocument();

        publicSection = session.getDocument(new PathRef(
                socialWorkspace.getPublicSectionPath()));
        privateSection = session.getDocument(new PathRef(
                socialWorkspace.getPrivateSectionPath()));
    }

    @Test
    public void proxyShouldBeUpdatedWhenDocumentIsModifiedForPrivateNews()
            throws Exception {

        DocumentModel newsItem = createSocialDocument(
                socialWorkspaceDoc.getPathAsString(), "A private News",
                NEWS_ITEM_TYPE, false);
        SocialDocument socialDocument = toSocialDocument(newsItem);
        DocumentModel initialExposedDocument = socialDocument.getRestrictedDocument();

        newsItem = updateTitle(newsItem, "Test1");
        socialDocument = toSocialDocument(newsItem);
        DocumentModel exposedDocument = socialDocument.getRestrictedDocument();
        assertEquals(initialExposedDocument.getId(), exposedDocument.getId());
        assertEquals("Test1", exposedDocument.getPropertyValue("dc:title"));

        socialDocument.makePublic();
        exposedDocument = socialDocument.getPublicDocument();
        assertEquals("Test1", exposedDocument.getPropertyValue("dc:title"));
        assertEquals(initialExposedDocument.getId(), exposedDocument.getId());

        newsItem = updateTitle(newsItem, "Test2");
        exposedDocument = socialDocument.getPublicDocument();
        assertEquals("Test2", exposedDocument.getPropertyValue("dc:title"));
        assertEquals(initialExposedDocument.getId(), exposedDocument.getId());

        socialDocument.restrictToMembers();
        exposedDocument = socialDocument.getRestrictedDocument();
        assertEquals("Test2", exposedDocument.getPropertyValue("dc:title"));
        assertEquals(initialExposedDocument.getId(), exposedDocument.getId());

    }

    @Test
    public void proxyShouldBeUpdatedWhenDocumentIsModifiedForPrivateArticle()
            throws Exception {
        DocumentModel article = createSocialDocument(
                socialWorkspaceDoc.getPathAsString(), "A private Article",
                SocialConstants.ARTICLE_TYPE, false);
        SocialDocument socialDocument = toSocialDocument(article);
        DocumentModel initialExposedDocument = socialDocument.getRestrictedDocument();

        article = updateTitle(article, "Test1");
        socialDocument = toSocialDocument(article);
        DocumentModel exposedDocument = socialDocument.getRestrictedDocument();
        assertEquals("Test1", exposedDocument.getPropertyValue("dc:title"));
        assertEquals(initialExposedDocument.getId(), exposedDocument.getId());

        socialDocument.makePublic();
        exposedDocument = socialDocument.getPublicDocument();
        assertEquals("Test1", exposedDocument.getPropertyValue("dc:title"));
        assertNotSame(initialExposedDocument.getId(), exposedDocument.getId());
        // Id change for Article when visibility change

        initialExposedDocument = exposedDocument;
        article = updateTitle(article, "Test2");
        socialDocument = toSocialDocument(article);
        exposedDocument = socialDocument.getPublicDocument();
        assertEquals("Test2", exposedDocument.getPropertyValue("dc:title"));
        assertEquals(initialExposedDocument.getId(), exposedDocument.getId());

        socialDocument.restrictToMembers();
        exposedDocument = socialDocument.getRestrictedDocument();
        assertEquals("Test2", exposedDocument.getPropertyValue("dc:title"));
        assertNotSame(initialExposedDocument.getId(), exposedDocument.getId());
        // Id change for Article when visibility change

    }

    protected DocumentModel updateTitle(DocumentModel doc, String value)
            throws Exception {
        doc.getContextData().clearScope(ScopeType.DEFAULT);
        doc.setPropertyValue("dc:title", value);
        doc = session.saveDocument(doc);
        session.save(); // fire post commit event listener
        eventService.waitForAsyncCompletion();
        session.save(); // flush the session to retrieve document
        return session.getDocument(doc.getRef());
    }
}
