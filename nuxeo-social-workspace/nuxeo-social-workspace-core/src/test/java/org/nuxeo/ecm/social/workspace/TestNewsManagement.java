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
 *     Ronan
 */
package org.nuxeo.ecm.social.workspace;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.nuxeo.ecm.social.workspace.SocialConstants.NEWS_SECTION_NAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.NEWS_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.PUBLIC_NEWS_SECTION_NAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.ROOT_SECTION_NAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_DOCUMENT_FACET;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.usermanager.UserManager;
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
public class TestNewsManagement {

    private static final String BASE_WORKSPACE_NAME = "base";

    public static final String TEST_NAME_SOCIAL_WORKSPACE = "socialworkspace";

    @Inject
    protected CoreSession session;

    @Inject
    protected UserManager userManager;

    @Inject
    protected FeaturesRunner featuresRunner;

    @Test
    public void testFacetOnNews() throws ClientException {
        // initialize the context by creating socialWorkspace
        createDocumentModelInSession(
                session.getRootDocument().getPathAsString(),
                TEST_NAME_SOCIAL_WORKSPACE, SOCIAL_WORKSPACE_TYPE);

        DocumentModel wrongNews = createDocumentModelInSession("/",
                "Unpublishable Novelty", NEWS_TYPE);

        assertTrue(wrongNews.hasFacet(SOCIAL_DOCUMENT_FACET));

        DocumentModel correctDirectNews = session.createDocumentModel("/"
                + TEST_NAME_SOCIAL_WORKSPACE, "Publishable Novelty", NEWS_TYPE);
        correctDirectNews = session.createDocument(correctDirectNews);
        session.save();

        assertTrue(correctDirectNews.hasFacet(SOCIAL_DOCUMENT_FACET));

        DocumentModel folderInSocialWorkspace = createDocumentModelInSession(
                "/" + TEST_NAME_SOCIAL_WORKSPACE, "Folder", "Folder");

        DocumentModel anOtherPublishableNews = createDocumentModelInSession(
                folderInSocialWorkspace.getPathAsString(),
                "another publishable news", NEWS_TYPE);
        assertNotNull(
                "A news could be created in a Folder within a SocialWorkspace",
                anOtherPublishableNews);
        assertTrue(
                "A news created in a Folder of a SocialWorkspace should have the SocialDocument facet",
                anOtherPublishableNews.hasFacet(SOCIAL_DOCUMENT_FACET));

        DocumentModel aNoteInFolderWihinSocialWorkspace = createDocumentModelInSession(
                "", "a lambda not", "Note");

        assertNotNull(
                "A document with out a socialDocument facet could be created in a Folder within a SocialWorkspace",
                aNoteInFolderWihinSocialWorkspace);
        assertFalse(
                "A document with out a socialDocument facet created in a Folder of a SocialWorkspace shouldn't have the SocialDocument facet",
                aNoteInFolderWihinSocialWorkspace.hasFacet(SOCIAL_DOCUMENT_FACET));

    }

    private DocumentModel createDocumentModelInSession(String pathAsString,
            String name, String type) throws ClientException {
        DocumentModel sws = session.createDocumentModel(pathAsString, name,
                type);
        sws = session.createDocument(sws);
        session.save();
        return sws;
    }

    @Test
    public void testRightsOnSocialSections() throws Exception {
        DocumentModel workspace = createDocumentModelInSession(
                session.getRootDocument().getPathAsString(),
                BASE_WORKSPACE_NAME, "Workspace");
        DocumentModel socialWorkspace = createDocumentModelInSession(
                workspace.getPathAsString(), TEST_NAME_SOCIAL_WORKSPACE,
                SOCIAL_WORKSPACE_TYPE);
        DocumentModel pubsec = session.getDocument(new PathRef(
                socialWorkspace.getPathAsString() + "/" + ROOT_SECTION_NAME));
        assertNotNull(pubsec);
        DocumentModel privatePubsec = session.getDocument(new PathRef(
                pubsec.getPathAsString() + "/" + NEWS_SECTION_NAME));
        DocumentModel publicPubsec = session.getDocument(new PathRef(
                privatePubsec.getPathAsString(), PUBLIC_NEWS_SECTION_NAME));

        ACP acp = pubsec.getACP();
        assertFalse(acp.getAccess(userManager.getDefaultGroup(),
                SecurityConstants.READ).toBoolean());
        assertFalse(acp.getAccess("u,uuie,", SecurityConstants.READ_WRITE).toBoolean());
        assertTrue(
                "The members of the community should have the READ_WRIGHT right",
                acp.getAccess(
                        SocialWorkspaceHelper.getCommunityMembersGroupName(socialWorkspace),
                        SecurityConstants.READ_WRITE).toBoolean());
        assertTrue(acp.getAccess(
                SocialWorkspaceHelper.getCommunityAdministratorsGroupName(socialWorkspace),
                SecurityConstants.READ).toBoolean());

        acp = privatePubsec.getACP();
        assertFalse(acp.getAccess("u,uuie,", SecurityConstants.READ_WRITE).toBoolean());
        assertFalse(acp.getAccess(userManager.getDefaultGroup(),
                SecurityConstants.READ).toBoolean());
        assertTrue(acp.getAccess(
                SocialWorkspaceHelper.getCommunityMembersGroupName(socialWorkspace),
                SecurityConstants.READ_WRITE).toBoolean());
        assertFalse(acp.getAccess("u,uuie,", SecurityConstants.READ_WRITE).toBoolean());
        assertTrue(acp.getAccess(
                SocialWorkspaceHelper.getCommunityAdministratorsGroupName(socialWorkspace),
                SecurityConstants.READ).toBoolean());

        acp = publicPubsec.getACP();
        assertFalse(acp.getAccess("u,uuie,", SecurityConstants.READ).toBoolean());
        assertTrue(acp.getAccess(userManager.getDefaultGroup(),
                SecurityConstants.READ).toBoolean());
        assertTrue(acp.getAccess(
                SocialWorkspaceHelper.getCommunityMembersGroupName(socialWorkspace),
                SecurityConstants.READ_WRITE).toBoolean());
        assertTrue(acp.getAccess(
                SocialWorkspaceHelper.getCommunityAdministratorsGroupName(socialWorkspace),
                SecurityConstants.READ_WRITE).toBoolean());
    }

}
