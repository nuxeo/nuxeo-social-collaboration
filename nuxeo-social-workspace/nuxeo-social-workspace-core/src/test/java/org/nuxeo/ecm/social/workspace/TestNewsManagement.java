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
 */package org.nuxeo.ecm.social.workspace;

import static org.nuxeo.ecm.core.api.security.SecurityConstants.EVERYTHING;
import static junit.framework.Assert.*;
import static org.nuxeo.ecm.social.workspace.SocialConstants.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.api.security.impl.ACLImpl;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.RepositorySettings;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.platform.content.template",
        "org.nuxeo.ecm.social.workspace.core" })
public class TestNewsManagement {

    private static final String BASE_WORKSPACE_NAME = "base";

    private static final String COMMUNITY_CREATOR_NAME = "Community creator";

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

        assertFalse("A news out of a SocialWorkspace shouldn't be publishable",
                SocialWorkspaceHelper.isSocialDocumentPublishable(session,
                        wrongNews));

        DocumentModel correctDirectNews = session.createDocumentModel("/"
                + TEST_NAME_SOCIAL_WORKSPACE, "Publishable Novelty", NEWS_TYPE);
        correctDirectNews = session.createDocument(correctDirectNews);
        session.save();

        assertTrue(correctDirectNews.hasFacet(SOCIAL_DOCUMENT_FACET));

        assertTrue("A news within a SocialWorkspace should be publishable",
                SocialWorkspaceHelper.isSocialDocumentPublishable(session,
                        correctDirectNews));

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

        assertTrue(
                "A news created in a Folder of a SocialWorkspace should be publishable",
                SocialWorkspaceHelper.isSocialDocumentPublishable(session,
                        anOtherPublishableNews));

        DocumentModel aNoteInFolderWihinSocialWorkspace = createDocumentModelInSession(
                "", "a lambda not", "Note");

        assertNotNull(
                "A document with out a socialDocument facet could be created in a Folder within a SocialWorkspace",
                aNoteInFolderWihinSocialWorkspace);
        assertFalse(
                "A document with out a socialDocument facet created in a Folder of a SocialWorkspace shouldn't have the SocialDocument facet",
                aNoteInFolderWihinSocialWorkspace.hasFacet(SOCIAL_DOCUMENT_FACET));

        assertFalse(
                "A document with out a socialDocument facet created in a Folder of a SocialWorkspace shouldn't be publishable",
                SocialWorkspaceHelper.isSocialDocumentPublishable(session,
                        aNoteInFolderWihinSocialWorkspace));

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
    public void testPublishSocialdocument() throws Exception {
        DocumentModel containerWorkspace = createDocumentModelInSession(
                session.getRootDocument().getPathAsString(),
                BASE_WORKSPACE_NAME, "Workspace");

        DocumentModel socialWorkspace = createDocumentModelInSession(
                containerWorkspace.getPathAsString(),
                TEST_NAME_SOCIAL_WORKSPACE, SOCIAL_WORKSPACE_TYPE);

        DocumentModel oneDoc = createDocumentModelInSession(
                socialWorkspace.getPathAsString(),
                "DocWithSocialDocumentFacet", "Document");
        oneDoc.addFacet(SOCIAL_DOCUMENT_FACET);
        session.save();

        DocumentModel socialSectionOutOfTheTemplate = createDocumentModelInSession(
                socialWorkspace.getPathAsString(), "socialSectionForTesting",
                SOCIAL_PUBLICATION_TYPE);


        DocumentModel newsPublicationInSocialSection = SocialWorkspaceHelper.publishSocialdocument(
                session, oneDoc, socialSectionOutOfTheTemplate.getName());

        assertNotNull("The proxy of the news should exist.", newsPublicationInSocialSection);


        try {
            DocumentModel newsNonPublished = SocialWorkspaceHelper.publishSocialdocument(
                    session, oneDoc, "inexsiting social section");

            assertNull("The proxy shouldn't exist in an inexisting section",newsNonPublished);
        } catch (Exception e) {

        }

        DocumentModel newsPublishInTemplateSection = SocialWorkspaceHelper.publishSocialdocument(
                session, oneDoc, ROOT_SECTION_NAME+"/"+NEWS_SECTION_NAME);

        assertNotNull("The proxy of the news should exist.", newsPublishInTemplateSection);

        DocumentModel newsPublishInPublicTemplateSection = SocialWorkspaceHelper.publishSocialdocument(
                session, oneDoc, ROOT_SECTION_NAME+"/"+NEWS_SECTION_NAME+"/"+PUBLIC_NEWS_SECTION_NAME);

        assertNotNull("The proxy of the news should exist.", newsPublishInPublicTemplateSection);


    }

     @Test
    public void testReadingRightToEveryOne() throws Exception {
        DocumentModel containerWorkspace = session.createDocumentModel(
                session.getRootDocument().getPathAsString(),
                BASE_WORKSPACE_NAME, "Workspace");
        containerWorkspace = session.createDocument(containerWorkspace);
        containerWorkspace.setACP(
                instanciateUserAcpToEveryThing(COMMUNITY_CREATOR_NAME), true);
        containerWorkspace = session.saveDocument(containerWorkspace);
        session.save();

        changeUser(COMMUNITY_CREATOR_NAME);

        createDocumentModelInSession(containerWorkspace.getPathAsString(),
                TEST_NAME_SOCIAL_WORKSPACE, SOCIAL_WORKSPACE_TYPE);

        DocumentModel publicationSection = session.getDocument(new PathRef("/"
                + BASE_WORKSPACE_NAME + "/" + TEST_NAME_SOCIAL_WORKSPACE + "/"
                + ROOT_SECTION_NAME));

        ACP currentSectionACP = publicationSection.getACP();

        assertTrue(
                "The Administrator should have the READ right on the Publication Section",
                currentSectionACP.getAccess("Administrator",
                        SecurityConstants.READ).toBoolean());
        assertTrue(
                "The Administrator should have the WRITE right on the Publication Section",
                currentSectionACP.getAccess("Administrator",
                        SecurityConstants.WRITE).toBoolean());

        assertTrue(
                "The community creator should have READ Right on the Publication section",
                currentSectionACP.getAccess(COMMUNITY_CREATOR_NAME,
                        SecurityConstants.READ).toBoolean());
        assertTrue(
                "The community creator should have WRITE Right on the Publication section",
                currentSectionACP.getAccess(COMMUNITY_CREATOR_NAME,
                        SecurityConstants.WRITE).toBoolean());

        assertFalse(
                "Whomever shouldn't have the READ right on the Publication section",
                currentSectionACP.getAccess("nobody", SecurityConstants.READ).toBoolean());
        assertFalse(
                "Whomever shouldn't have the WRITE right on the Publication section",
                currentSectionACP.getAccess("nobody", SecurityConstants.WRITE).toBoolean());

        DocumentModel newsSection = session.getDocument(new PathRef("/"
                + BASE_WORKSPACE_NAME + "/" + TEST_NAME_SOCIAL_WORKSPACE + "/"
                + ROOT_SECTION_NAME + "/" + NEWS_SECTION_NAME));
        currentSectionACP = newsSection.getACP();

        assertTrue(
                "The Administrator should have the READ right on the News Section",
                currentSectionACP.getAccess("Administrator",
                        SecurityConstants.READ).toBoolean());
        assertTrue(
                "The Administrator should have the WRITE right on the News Section",
                currentSectionACP.getAccess("Administrator",
                        SecurityConstants.WRITE).toBoolean());

        assertTrue(
                "The community creator should have READ Right on the News section",
                currentSectionACP.getAccess(COMMUNITY_CREATOR_NAME,
                        SecurityConstants.READ).toBoolean());
        assertTrue(
                "The community creator should have WRITE Right on the News section",
                currentSectionACP.getAccess(COMMUNITY_CREATOR_NAME,
                        SecurityConstants.WRITE).toBoolean());

        assertFalse(
                "Whomever shouldn't have the READ right on the News section",
                currentSectionACP.getAccess("nobody", SecurityConstants.READ).toBoolean());
        assertFalse(
                "Whomever shouldn't have the WRITE right on the News section",
                currentSectionACP.getAccess("nobody", SecurityConstants.WRITE).toBoolean());

        DocumentModel publicSection = session.getDocument(new PathRef("/"
                + BASE_WORKSPACE_NAME + "/" + TEST_NAME_SOCIAL_WORKSPACE + "/"
                + ROOT_SECTION_NAME + "/" + NEWS_SECTION_NAME + "/"
                + PUBLIC_NEWS_SECTION_NAME));
        currentSectionACP = publicSection.getACP();

        assertTrue(
                "The Administrator should have the READ right on the PublicNews Section",
                currentSectionACP.getAccess("Administrator",
                        SecurityConstants.READ).toBoolean());
        assertTrue(
                "The Administrator should have the WRITE right on the PublicNews Section",
                currentSectionACP.getAccess("Administrator",
                        SecurityConstants.WRITE).toBoolean());

        assertTrue(
                "The community creator should have READ Right on the PublicNews section",
                currentSectionACP.getAccess(COMMUNITY_CREATOR_NAME,
                        SecurityConstants.READ).toBoolean());
        assertTrue(
                "The community creator should have WRITE Right on the PublicNews section",
                currentSectionACP.getAccess(COMMUNITY_CREATOR_NAME,
                        SecurityConstants.WRITE).toBoolean());

        assertTrue(
                "Whomever should have the READ right on the PublicNews section",
                currentSectionACP.getAccess("nobody", SecurityConstants.READ).toBoolean());
        assertFalse(
                "Whomever shouldn't have the WRITE right on the PublicNews section",
                currentSectionACP.getAccess("nobody", SecurityConstants.WRITE).toBoolean());
    }

    private ACP instanciateUserAcpToEveryThing(String userName) {
        ACP acp = new ACPImpl();
        ACL acl = new ACLImpl("acl");
        ACE ace = new ACE(userName, EVERYTHING, true);
        acl.add(ace);
        acp.addACL(acl);
        return acp;
    }

    protected void changeUser(String username) {
        CoreFeature coreFeature = featuresRunner.getFeature(CoreFeature.class);
        RepositorySettings repository = coreFeature.getRepository();
        repository.shutdown();
        repository.setUsername(username);
        session = repository.get();
    }

}
