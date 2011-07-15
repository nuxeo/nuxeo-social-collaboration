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
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.collections.ScopeType;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.SocialConstants;
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
public class TestDeleteSocialDocumentListener {

    @Inject
    protected CoreSession session;

    @Inject
    protected UserManager userManager;

    @Inject
    protected FeaturesRunner featuresRunner;

    @Test
    public void testRemoveProxy() throws Exception {
        DocumentModel containerWorkspace = createDocumentModelInSession(
                session.getRootDocument().getPathAsString(),
                "Workspace of tests", "Workspace");

        DocumentModel socialWorkspace = createDocumentModelInSession(
                containerWorkspace.getPathAsString(),
                "Socialworkspace for test", SOCIAL_WORKSPACE_TYPE);

        DocumentModel privateNews = createDocumentModelInSession(
                socialWorkspace.getPathAsString(), "A private News",
                SocialConstants.NEWS_TYPE);

        createDocumentModelInSession(
                socialWorkspace.getPathAsString(), "AAA another private News",
                SocialConstants.NEWS_TYPE);

        String queryToGetProxy = String.format(
                "Select * from News where ecm:isProxy = 1 and ecm:currentLifeCycleState <> 'deleted' and ecm:name = '%s'",
                privateNews.getName());
        DocumentModelList newsProxies = session.query(queryToGetProxy);
        assertEquals(1, newsProxies.size());

        session.followTransition(privateNews.getRef(),
                LifeCycleConstants.DELETE_TRANSITION);
        newsProxies = session.query(queryToGetProxy);
        assertEquals(0, newsProxies.size());

        DocumentModel publicNews = session.createDocumentModel(
                socialWorkspace.getPathAsString(), "A public news",
                SocialConstants.NEWS_TYPE);
        publicNews.putContextData(ScopeType.REQUEST, "Public", Boolean.TRUE);
        publicNews = session.createDocument(publicNews);
        session.save();
        String queryToGetPublicProxy = String.format(
                "Select * from News where ecm:isProxy = 1 and ecm:currentLifeCycleState <> 'deleted' and ecm:name = '%s'",
                publicNews.getName());

        newsProxies = session.query(queryToGetPublicProxy);
        assertEquals(1, newsProxies.size());

        session.followTransition(publicNews.getRef(),
                LifeCycleConstants.DELETE_TRANSITION);
        newsProxies = session.query(queryToGetProxy);
        assertEquals(0, newsProxies.size());

        String queryToGetProxies = String.format("Select * from News where ecm:isProxy = 1 and ecm:currentLifeCycleState <> 'deleted'");

        newsProxies = session.query(queryToGetProxies);
        assertEquals(1, newsProxies.size());

    }

    private DocumentModel createDocumentModelInSession(String pathAsString,
            String name, String type) throws ClientException {
        DocumentModel doc = session.createDocumentModel(pathAsString, name,
                type);
        doc = session.createDocument(doc);
        session.save();
        return doc;
    }

}
