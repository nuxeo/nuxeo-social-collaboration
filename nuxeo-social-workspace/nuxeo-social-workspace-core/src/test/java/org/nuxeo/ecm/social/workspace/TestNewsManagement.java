package org.nuxeo.ecm.social.workspace;

import static org.nuxeo.ecm.core.api.security.SecurityConstants.EVERYTHING;
import static junit.framework.Assert.*;

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

    public static final String NAME_SOCIAL_WORKSPACE = "socialworkspace";

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
                NAME_SOCIAL_WORKSPACE, SocialConstants.SOCIAL_WORKSPACE_TYPE);

        DocumentModel wrongNews = createDocumentModelInSession("/",
                "Unpublishable Novelty", SocialConstants.NEWS_TYPE);

        assertTrue(wrongNews.hasFacet(SocialConstants.SOCIAL_DOCUMENT_FACET));

        assertFalse("A news out of a SocialWorkspace shouldn't be publishable",
                SocialWorkspaceHelper.couldDocumentBePublished(session,
                        wrongNews));

        DocumentModel correctNews = session.createDocumentModel("/"
                + NAME_SOCIAL_WORKSPACE, "Publishable Novelty",
                SocialConstants.NEWS_TYPE);
        correctNews = session.createDocument(correctNews);
        session.save();

        assertTrue(correctNews.hasFacet(SocialConstants.SOCIAL_DOCUMENT_FACET));

        assertTrue("A news within a SocialWorkspace should be publishable",
                SocialWorkspaceHelper.couldDocumentBePublished(session,
                        correctNews));

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
                NAME_SOCIAL_WORKSPACE, SocialConstants.SOCIAL_WORKSPACE_TYPE);

        DocumentModel publicationSection = session.getDocument(new PathRef("/"
                + BASE_WORKSPACE_NAME + "/" + NAME_SOCIAL_WORKSPACE + "/"
                + SocialConstants.ROOT_SECTION_NAME));

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
                + BASE_WORKSPACE_NAME + "/" + NAME_SOCIAL_WORKSPACE + "/"
                + SocialConstants.ROOT_SECTION_NAME + "/"
                + SocialConstants.NEWS_SECTION_NAME));
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
                + BASE_WORKSPACE_NAME + "/" + NAME_SOCIAL_WORKSPACE + "/"
                + SocialConstants.ROOT_SECTION_NAME + "/"
                + SocialConstants.NEWS_SECTION_NAME + "/"
                + SocialConstants.PUBLIC_NEWS_SECTION_NAME));
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
