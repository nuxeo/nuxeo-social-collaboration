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
import static org.nuxeo.ecm.social.workspace.SocialConstants.SUBSCRIPTION_REQUEST_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SUBSCRIPTION_REQUEST_TYPE_JOIN;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SUBSCRIPTION_REQUEST_TYPE_PROPERTY;

import org.junit.Test;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.social.workspace.adapters.Article;
import org.nuxeo.ecm.social.workspace.adapters.SubscriptionRequest;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.LocalDeploy;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 */
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
                SUBSCRIPTION_REQUEST_TYPE);
        request.setPropertyValue(SUBSCRIPTION_REQUEST_TYPE_PROPERTY,
                SUBSCRIPTION_REQUEST_TYPE_JOIN);
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
