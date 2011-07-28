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
 */
package org.nuxeo.ecm.social.workspace.listeners;

import static junit.framework.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.social.workspace.AbstractSocialWorkspaceTest;
import org.nuxeo.ecm.social.workspace.SocialConstants;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

@LocalDeploy("org.nuxeo.ecm.social.workspace.core:test-social-workspace-usermanager-contrib.xml")
public class TestDeleteSocialDocumentListener extends
        AbstractSocialWorkspaceTest {

    protected DocumentModel privateSection;

    protected DocumentModel publicSection;

    @Before
    public void setup() throws Exception {
        socialWorkspace = createSocialWorkspace("SocialWorkspace for test");
        socialWorkspaceDoc = socialWorkspace.getDocument();

        publicSection = session.getDocument(new PathRef(
                socialWorkspace.getPublicSectionPath()));
        privateSection = session.getDocument(new PathRef(
                socialWorkspace.getPrivateSectionPath()));
    }

    @Test
    public void testShouldCleanupOfProxiesForNewsAtDeletionTime()
            throws Exception {
        DocumentModel privateNews1 = createSocialDocument(
                socialWorkspaceDoc.getPathAsString(), "A private News",
                SocialConstants.NEWS_ITEM_TYPE, false);

        DocumentModel privateNews2 = createSocialDocument(
                socialWorkspaceDoc.getPathAsString(),
                "AAA another private News", SocialConstants.NEWS_ITEM_TYPE,
                false);

        assertEquals(1, getNumberOfProxy(privateNews1));
        assertEquals(1, getNumberOfProxy(privateNews2));

        session.followTransition(privateNews1.getRef(),
                LifeCycleConstants.DELETE_TRANSITION);
        assertEquals(0, getNumberOfProxy(privateNews1));
        assertEquals(1, getNumberOfProxy(privateNews2));

        DocumentModel publicNews = createSocialDocument(
                socialWorkspaceDoc.getPathAsString(), "A public news",
                SocialConstants.NEWS_ITEM_TYPE, true);
        assertEquals(1, getNumberOfProxy(publicNews));

        session.followTransition(publicNews.getRef(),
                LifeCycleConstants.DELETE_TRANSITION);
        assertEquals(0, getNumberOfProxy(publicNews));

        String query = String.format("Select * from NewsItem where ecm:isProxy = 1");
        DocumentModelList proxies = session.query(query);
        assertEquals(1, proxies.size());

    }

    @Test
    public void testShouldCleanupOfProxiesForArticleAtDeletionTime()
            throws Exception {
        DocumentModel privateArticle1 = createSocialDocument(
                socialWorkspaceDoc.getPathAsString(), "A private News",
                SocialConstants.ARTICLE_TYPE, false);

        assertEquals(0, getNumberOfProxy(privateArticle1));
        session.followTransition(privateArticle1.getRef(),
                LifeCycleConstants.DELETE_TRANSITION);
        assertEquals(0, getNumberOfProxy(privateArticle1));

        DocumentModel publicArticle = createSocialDocument(
                socialWorkspaceDoc.getPathAsString(), "A public news",
                SocialConstants.NEWS_ITEM_TYPE, true);

        assertEquals(1, getNumberOfProxy(publicArticle));
        session.followTransition(publicArticle.getRef(),
                LifeCycleConstants.DELETE_TRANSITION);
        assertEquals(0, getNumberOfProxy(publicArticle));

        String query = String.format("Select * from NewsItem where ecm:isProxy = 1");
        DocumentModelList proxies = session.query(query);
        assertEquals(0, proxies.size());

    }

    protected int getNumberOfProxy(DocumentModel doc) throws ClientException {
        int result = 0;
        result = result
                + session.getProxies(doc.getRef(), publicSection.getRef()).size();
        result = result
                + session.getProxies(doc.getRef(), privateSection.getRef()).size();
        return result;
    }

}
