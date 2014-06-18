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
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationParameters;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.EventServiceAdmin;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 *
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({
        "org.nuxeo.ecm.platform.api",
        "org.nuxeo.ecm.platform.dublincore",
        "org.nuxeo.ecm.directory",
        "org.nuxeo.ecm.directory.sql",
        "org.nuxeo.ecm.directory.types.contrib",
        "org.nuxeo.ecm.platform.usermanager.api",
        "org.nuxeo.ecm.platform.usermanager",
        "org.nuxeo.ecm.platform.content.template",
        "org.nuxeo.ecm.opensocial.spaces",
        "org.nuxeo.ecm.platform.types.api",
        "org.nuxeo.ecm.platform.types.core",
        "org.nuxeo.ecm.automation.core",
        "org.nuxeo.ecm.core.persistence",
        "org.nuxeo.ecm.platform.url.api",
        "org.nuxeo.ecm.platform.url.core",
        "org.nuxeo.ecm.platform.ui:OSGI-INF/urlservice-framework.xml",
        "org.nuxeo.ecm.user.center:OSGI-INF/urlservice-contrib.xml",
        "org.nuxeo.ecm.platform.userworkspace.types",
        "org.nuxeo.ecm.platform.userworkspace.api",
        "org.nuxeo.ecm.platform.userworkspace.core",
        "org.nuxeo.ecm.user.center.profile",
        "org.nuxeo.ecm.activity",
        "org.nuxeo.ecm.automation.features",
        "org.nuxeo.ecm.platform.query.api",
        "org.nuxeo.ecm.social.workspace.core",
        "org.nuxeo.ecm.platform.content.template",
        "org.nuxeo.ecm.user.relationships",
        "org.nuxeo.ecm.social.workspace.gadgets",
        "org.nuxeo.ecm.platform.test:test-usermanagerimpl/directory-config.xml",
        "org.nuxeo.ecm.platform.picture.core:OSGI-INF/picturebook-schemas-contrib.xml" })
@LocalDeploy({
        "org.nuxeo.ecm.user.relationships:test-user-relationship-directories-contrib.xml",
        "org.nuxeo.ecm.social.workspace.core:social-workspace-test.xml" })
public class TestSocialWorkspaceMembersOperation {
    @Inject
    protected CoreSession session;

    @Inject
    protected UserManager userManager;

    @Inject
    protected EventServiceAdmin eventServiceAdmin;

    @Inject
    AutomationService automationService;

    DocumentModel socialWorkspaceDocument;

    SocialWorkspace socialWorkspace;

    @Before
    public void disableListeners() {
        eventServiceAdmin.setListenerEnabledFlag("activityStreamListener",
                false);
        eventServiceAdmin.setListenerEnabledFlag("sql-storage-binary-text",
                false);
    }

    @Before
    public void setup() throws Exception {
        // create social workspace
        socialWorkspaceDocument = session.createDocumentModel("/",
                "testSocialWorkspace", SOCIAL_WORKSPACE_TYPE);
        socialWorkspaceDocument = session.createDocument(socialWorkspaceDocument);
        socialWorkspace = socialWorkspaceDocument.getAdapter(SocialWorkspace.class);
        assertNotNull(socialWorkspace);

        // create a user
        DocumentModel testUser = userManager.getBareUserModel();
        String schemaName = userManager.getUserSchemaName();

        testUser.setPropertyValue(schemaName + ":username", "testUser");
        testUser.setPropertyValue(schemaName + ":firstName",
                "testUser_firstName");
        testUser.setPropertyValue(schemaName + ":lastName", "testUser_lastName");
        userManager.createUser(testUser);
        assertNotNull(userManager.getPrincipal("testUser"));

        // make the user member of SocialWorkspace
        socialWorkspace.addMember(userManager.getPrincipal("testUser"));

        session.save();
    }

    @After
    public void waitForAsyncEvents() {
        Framework.getLocalService(EventService.class).waitForAsyncCompletion();
    }

    @Test
    public void testOperation() throws Exception {
        List<String> list = socialWorkspace.searchMembers("t%");
        assertEquals(1, list.size());

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("fakeChain");
        OperationParameters oParams = new OperationParameters(
                GetSocialWorkspaceMembers.ID);
        oParams.set("pattern", "testU%");
        oParams.set("page", 0);
        oParams.set("pageSize", 5);
        oParams.set("contextPath", "/testSocialWorkspace");
        chain.add(oParams);

        Blob result = (Blob) automationService.run(ctx, chain);
        JSONObject o = JSONObject.fromObject(result.getString());
        JSONArray array = (JSONArray) o.get("users");
        assertEquals(1, array.size());
        assertEquals("testUser_firstName",
                ((JSONObject) array.get(0)).get("firstName"));
        assertEquals("testUser_lastName",
                ((JSONObject) array.get(0)).get("lastName"));
    }
}
