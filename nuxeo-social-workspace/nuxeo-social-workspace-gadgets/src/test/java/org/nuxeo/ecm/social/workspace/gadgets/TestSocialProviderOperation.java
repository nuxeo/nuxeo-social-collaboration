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
package org.nuxeo.ecm.social.workspace.gadgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_SOCIAL_DOCUMENT_IS_PUBLIC;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialWorkspace;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationParameters;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 *
 */
@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.social.workspace.core",
        "org.nuxeo.ecm.social.workspace.gadgets",
        "org.nuxeo.ecm.automation.core", "org.nuxeo.ecm.automation.features",
        "org.nuxeo.ecm.platform.query.api" })
public class TestSocialProviderOperation {

    @Inject
    CoreSession session;

    @Inject
    AutomationService service;

    @Inject
    UserManager userManager;

    @Before
    public void initRepo() throws Exception {

        session.removeChildren(session.getRootDocument().getRef());
        session.save();

        // create social workspaces
        DocumentModel sws1 = session.createDocumentModel("/", "sws1",
                "SocialWorkspace");
        sws1.setPropertyValue("dc:title", "Social Workspace 1");
        sws1 = session.createDocument(sws1);

        session.save();

        DocumentModel sws2 = session.createDocumentModel("/", "sws2",
                "SocialWorkspace");
        sws2.setPropertyValue("dc:title", "Social Workspace 2");
        sws2 = session.createDocument(sws2);
        session.save();

        // create two articles in 2nd social workspace
        DocumentModel article1 = session.createDocumentModel("/sws2",
                "article1", "Article");
        article1.setPropertyValue(FIELD_SOCIAL_DOCUMENT_IS_PUBLIC, true);
        article1.setPropertyValue("dc:title", "Public Article");
        article1 = session.createDocument(article1);
        session.save();
        Framework.getService(EventService.class).waitForAsyncCompletion();
        session.save();
        article1 = session.getDocument(article1.getRef());

        DocumentModel article2 = session.createDocumentModel("/sws2",
                "article2", "Article");
        article2.setPropertyValue("dc:title", "Non Public Article");
        article2 = session.createDocument(article2);

        session.save();
    }

    @Test
    public void testSocialProviderOperation() throws Exception {
        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("fakeChain");
        OperationParameters oParams = new OperationParameters(
                SocialProviderOperation.ID);
        oParams.set("query", "select * from Article where ecm:isProxy = 0");
        oParams.set("socialWorkspacePath", "/sws2");
        chain.add(oParams);

        DocumentModelList result = (DocumentModelList) service.run(ctx, chain);
        assertEquals(2, result.size());

        // remove current user from admins of sws2
        DocumentModel sws = session.getDocument(new PathRef("/sws2"));
        SocialWorkspace socialWorkspace = toSocialWorkspace(sws);
        DocumentModel group = userManager.getGroupModel(socialWorkspace.getAdministratorsGroupName());
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) group.getProperty(
                userManager.getGroupSchemaName(), "members");
        list.remove(session.getPrincipal().getName());
        group.setProperty(userManager.getGroupSchemaName(), "members", list);
        userManager.updateGroup(group);

        oParams.set("query", "select * from Article where ecm:isProxy = 1");
        result = (DocumentModelList) service.run(ctx, chain);
        assertEquals(1, result.size()); // return only the public article
    }

    @Test
    public void testOnlyPublicDocumentsParameters() throws Exception {
        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("fakeChain");
        OperationParameters oParams = new OperationParameters(
                SocialProviderOperation.ID);
        oParams.set("query", "select * from Article");
        oParams.set("socialWorkspacePath", "/sws2");
        oParams.set("onlyPublicDocuments", "true");
        chain.add(oParams);

        DocumentModelList result = (DocumentModelList) service.run(ctx, chain);
        assertEquals(2, result.size());

        oParams.set("onlyPublicDocuments", "false");
        result = (DocumentModelList) service.run(ctx, chain);
        assertEquals(6, result.size());

        oParams.set("onlyPublicDocuments", "wrong string for a boolean");
        result = (DocumentModelList) service.run(ctx, chain);
        assertEquals(6, result.size());

    }
}
