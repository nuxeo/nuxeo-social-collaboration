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
 */
package org.nuxeo.ecm.social.workspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.nuxeo.ecm.social.workspace.SocialConstants.ARTICLE_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_REQUEST_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.REQUEST_DOC_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.REQUEST_TYPE_JOIN;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.social.workspace.adapters.Article;
import org.nuxeo.ecm.social.workspace.adapters.SubscriptionRequest;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 */
@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.platform.content.template",
        "org.nuxeo.ecm.social.workspace.core" })
@LocalDeploy("org.nuxeo.ecm.social.workspace.core:test-social-workspace-service-contrib.xml")
public class TestAdapters extends AbstractSocialWorkspaceTest {

    @Test
    public void testArticleAdapter() throws Exception {
        DocumentModel article = session.createDocumentModel(
                session.getRootDocument().getPathAsString(), "article1",
                ARTICLE_TYPE);
        assertNotNull(article);
        article = session.createDocument(article);
        session.save();

        Article adapter = article.getAdapter(Article.class);
        assertNotNull(adapter);
        assertNotNull(adapter.getCreated());
    }

    @Test
    public void testRequestAdapter() throws Exception {
        DocumentModel request = session.createDocumentModel(
                session.getRootDocument().getPathAsString(), "request",
                REQUEST_DOC_TYPE);
        request.setPropertyValue(FIELD_REQUEST_TYPE, REQUEST_TYPE_JOIN);
        assertNotNull(request);
        request = session.createDocument(request);
        session.save();

        SubscriptionRequest adapter = request.getAdapter(SubscriptionRequest.class);
        assertNotNull(adapter);
        assertNotNull(adapter.getType());
    }

    @Test
    public void testSocialWorkspaceService() throws Exception {
        SocialWorkspaceService service = Framework.getService(SocialWorkspaceService.class);
        assertEquals(3, service.getValidationDays());
    }
}
