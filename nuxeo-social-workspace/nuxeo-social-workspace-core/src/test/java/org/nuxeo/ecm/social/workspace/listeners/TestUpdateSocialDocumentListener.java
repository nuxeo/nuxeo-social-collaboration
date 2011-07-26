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
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;
import static org.nuxeo.ecm.social.workspace.ToolsForTests.createDocumentModel;
import static org.nuxeo.ecm.social.workspace.ToolsForTests.createSocialDocument;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialDocument;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.collections.ScopeType;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.social.workspace.SocialConstants;
import org.nuxeo.ecm.social.workspace.adapters.SocialDocument;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.platform.content.template",
        "org.nuxeo.ecm.social.workspace.core" })
@LocalDeploy("org.nuxeo.ecm.social.workspace.core:test-social-workspace-usermanager-contrib.xml")
public class TestUpdateSocialDocumentListener {

    @Inject
    protected CoreSession session;

    @Inject
    protected EventService eventService;

    protected DocumentModel socialWorkspaceDoc;

    protected DocumentModel privateSection;

    protected DocumentModel publicSection;

    @Before
    public void setup() throws Exception {
        socialWorkspaceDoc = createDocumentModel(session,
                session.getRootDocument().getPathAsString(),
                "Socialworkspace for test", SOCIAL_WORKSPACE_TYPE);
        SocialWorkspace socialWorkspace = SocialWorkspaceHelper.toSocialWorkspace(socialWorkspaceDoc);

        String AdministratorGroup = socialWorkspace.getAdministratorsGroupName();
        NuxeoPrincipal principal = (NuxeoPrincipal) session.getPrincipal();
        principal.getGroups().add(AdministratorGroup);

        publicSection = session.getDocument(new PathRef(
                socialWorkspace.getPublicSectionPath()));
        privateSection = session.getDocument(new PathRef(
                socialWorkspace.getPrivateSectionPath()));
    }

    @Test
    public void proxyShouldBeUpdatedWhenDocumentIsModifiedForPrivateNews()
            throws Exception {

        DocumentModel newsItem = createSocialDocument(session,
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
        DocumentModel article = createSocialDocument(session,
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
