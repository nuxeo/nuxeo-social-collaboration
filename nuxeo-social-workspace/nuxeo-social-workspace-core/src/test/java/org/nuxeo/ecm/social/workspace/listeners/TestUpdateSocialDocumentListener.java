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
import static org.nuxeo.ecm.social.workspace.SocialConstants.NEWS_ITEM_TYPE;import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;
import static org.nuxeo.ecm.social.workspace.ToolsForTests.createDocumentModel;
import static org.nuxeo.ecm.social.workspace.ToolsForTests.createSocialDocument;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.collections.ScopeType;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.SocialConstants;
import org.nuxeo.ecm.social.workspace.adapters.SocialDocumentAdapter;
import org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper;
import org.nuxeo.runtime.api.Framework;
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
    protected UserManager userManager;

    @Inject
    protected FeaturesRunner featuresRunner;

    @Inject
    protected EventService eventService;

    protected DocumentModel socialWorkspace;

    DocumentModel privateSection;

    DocumentModel publicSection;

    @Before
    public void setup() throws Exception {

        socialWorkspace = createDocumentModel(session,
                session.getRootDocument().getPathAsString(),
                "Socialworkspace for test", SOCIAL_WORKSPACE_TYPE);

        String AdministratorGroup = SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupName(socialWorkspace);
        NuxeoPrincipal principal = (NuxeoPrincipal) session.getPrincipal();
        principal.getGroups().add(AdministratorGroup);

        publicSection = SocialWorkspaceHelper.getPublicSection(session,
                socialWorkspace);
        privateSection = SocialWorkspaceHelper.getPrivateSection(session,
                socialWorkspace);
    }

    @Test
    public void proxyShouldBeUpdatedWhenDocumentIsModifiedForPrivateNews()
            throws Exception {

        DocumentModel newsItem = createSocialDocument(session,
                socialWorkspace.getPathAsString(), "A private News",
                NEWS_ITEM_TYPE, false);
        SocialDocumentAdapter socialDocument = newsItem.getAdapter(SocialDocumentAdapter.class);
        DocumentModel initialExposedDocument = socialDocument.getDocumentRestrictedToMembers();

        newsItem = updateTitle(newsItem, "Test1");
        socialDocument = newsItem.getAdapter(SocialDocumentAdapter.class);
        DocumentModel exposedDocument = socialDocument.getDocumentRestrictedToMembers();
        assertEquals(initialExposedDocument.getId(), exposedDocument.getId());
        assertEquals("Test1", exposedDocument.getPropertyValue("dc:title"));

        socialDocument.makePublic();
        exposedDocument = socialDocument.getDocumentPublic();
        assertEquals("Test1", exposedDocument.getPropertyValue("dc:title"));
        assertEquals(initialExposedDocument.getId(), exposedDocument.getId());

        newsItem = updateTitle(newsItem, "Test2");
        exposedDocument = socialDocument.getDocumentPublic();
        assertEquals("Test2", exposedDocument.getPropertyValue("dc:title"));
        assertEquals(initialExposedDocument.getId(), exposedDocument.getId());

        socialDocument.restrictToSocialWorkspaceMembers();
        exposedDocument = socialDocument.getDocumentRestrictedToMembers();
        assertEquals("Test2", exposedDocument.getPropertyValue("dc:title"));
        assertEquals(initialExposedDocument.getId(), exposedDocument.getId());

    }

    @Test
    public void proxyShouldBeUpdatedWhenDocumentIsModifiedForPrivateArticle()
            throws Exception {
        DocumentModel article = createSocialDocument(session,
                socialWorkspace.getPathAsString(), "A private Article",
                SocialConstants.ARTICLE_TYPE, false);
        SocialDocumentAdapter socialDocument = article.getAdapter(SocialDocumentAdapter.class);
        DocumentModel initialExposedDocument = socialDocument.getDocumentRestrictedToMembers();

        article = updateTitle(article, "Test1");
        socialDocument = article.getAdapter(SocialDocumentAdapter.class);
        DocumentModel exposedDocument = socialDocument.getDocumentRestrictedToMembers();
        assertEquals("Test1", exposedDocument.getPropertyValue("dc:title"));
        assertEquals(initialExposedDocument.getId(), exposedDocument.getId());

        socialDocument.makePublic();
        exposedDocument = socialDocument.getDocumentPublic();
        assertEquals("Test1", exposedDocument.getPropertyValue("dc:title"));
        assertNotSame(initialExposedDocument.getId(), exposedDocument.getId());
        // Id change for Article when visibility change

        initialExposedDocument = exposedDocument;
        article = updateTitle(article, "Test2");
        socialDocument = article.getAdapter(SocialDocumentAdapter.class);
        exposedDocument = socialDocument.getDocumentPublic();
        assertEquals("Test2", exposedDocument.getPropertyValue("dc:title"));
        assertEquals(initialExposedDocument.getId(), exposedDocument.getId());

        socialDocument.restrictToSocialWorkspaceMembers();
        exposedDocument = socialDocument.getDocumentRestrictedToMembers();
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
