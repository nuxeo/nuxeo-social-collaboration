package org.nuxeo.ecm.social.workspace.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.core.versioning.VersioningService;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.social.workspace.SocialConstants;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-core-types-contrib.xml",
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-life-cycle-contrib.xml",
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-content-template-contrib.xml",
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-adapters-contrib.xml",
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-notifications-contrib.xml" })
public class TestSocialDocumentPublicationHandler {

    @Inject
    protected CoreSession session;

    private SocialDocumentPublicationHandler underTest;

    private DocumentModel socialWorkspace;

    private DocumentModel currentSocialDocument;

    @Test
    public void testProxiesManagement() throws Exception {
        initNominalContext();
        underTest = new SocialDocumentPublicationHandler(session,
                currentSocialDocument);
        DocumentModel proxy1 = session.publishDocument(
                underTest.currentSocialDocument, underTest.privateSocialSection);
        session.save();

        DocumentModel chosenDocForPublication = underTest.getProxyOrCurrentDoc();
        assertNotNull(chosenDocForPublication);
        assertEquals(proxy1, chosenDocForPublication);
        String proxyId = proxy1.getId();

        currentSocialDocument.putContextData(
                VersioningService.VERSIONING_OPTION, VersioningOption.MAJOR);
        session.saveDocument(currentSocialDocument);

        chosenDocForPublication = underTest.getProxyOrCurrentDoc();
        assertNotNull(chosenDocForPublication);
        assertEquals(proxy1, chosenDocForPublication);
        assertEquals(proxyId, chosenDocForPublication.getId());

        DocumentModel proxy2 = session.publishDocument(
                underTest.currentSocialDocument, underTest.publicSocialSection);
        session.save();
        assertFalse(proxyId.equals(proxy2.getId()));

        chosenDocForPublication = underTest.getProxyOrCurrentDoc();
        assertNotNull(chosenDocForPublication);
        assertEquals(currentSocialDocument, chosenDocForPublication);

        chosenDocForPublication = underTest.getProxyOrCurrentDoc();
        assertNotNull(chosenDocForPublication);
        assertEquals(currentSocialDocument, chosenDocForPublication);
    }

    @Test
    public void testPublishSocialDocument() throws Exception {
        initNominalContext();
        underTest = new SocialDocumentPublicationHandler(session,
                currentSocialDocument);

        DocumentModel proxyManuallyCreated = session.publishDocument(
                currentSocialDocument, underTest.privateSocialSection);
        session.save();
        assertNotNull(proxyManuallyCreated);

        DocumentModel proxyHandled = underTest.publishSocialDocument(underTest.privateSocialSection);
        assertNotNull(proxyHandled);
        assertEquals(proxyManuallyCreated, proxyHandled);

        session.removeDocument(proxyHandled.getRef());
        DocumentModel proxyNewlyHandled = underTest.publishSocialDocument(underTest.privateSocialSection);
        assertNotNull(proxyNewlyHandled);
        assertFalse(proxyHandled.equals(proxyNewlyHandled));

        DocumentModel publicProxyHandled = underTest.publishSocialDocument(underTest.publicSocialSection);
        assertNotNull(publicProxyHandled);
        assertEquals(proxyNewlyHandled, publicProxyHandled);
        assertEquals(underTest.publicSocialSection.getRef(),
                publicProxyHandled.getParentRef());
    }

    public void initNominalContext() throws Exception {
        socialWorkspace = createDocumentModel("/", "currentSocialWorkspace",
                SocialConstants.SOCIAL_WORKSPACE_TYPE);
        currentSocialDocument = createDocumentModel(
                socialWorkspace.getPathAsString() + "/", "ineffective news",
                SocialConstants.NEWS_TYPE);
    }

    protected DocumentModel createDocumentModel(String path, String name,
            String type) throws ClientException {
        DocumentModel doc = session.createDocumentModel(path, name, type);
        doc = session.createDocument(doc);
        session.save();
        return doc;
    }

    @Test
    public void testConstructorBrokenCases() throws Exception {
        initNominalContext();
        underTest = new SocialDocumentPublicationHandler(null, null);
        assertFalse(underTest.isPrivatePublishable());
        assertNull(underTest.publishPrivatelySocialDocument());
        assertFalse(underTest.isPublicPublishable());
        assertNull(underTest.publishPubliclySocialDocument());

        underTest = new SocialDocumentPublicationHandler(session, null);
        assertFalse(underTest.isPrivatePublishable());
        assertNull(underTest.publishPrivatelySocialDocument());
        assertFalse(underTest.isPublicPublishable());
        assertNull(underTest.publishPubliclySocialDocument());

        underTest = new SocialDocumentPublicationHandler(null,
                currentSocialDocument);
        assertFalse(underTest.isPrivatePublishable());
        assertNull(underTest.publishPrivatelySocialDocument());
        assertFalse(underTest.isPublicPublishable());
        assertNull(underTest.publishPubliclySocialDocument());
    }

    @Test
    public void testNominalCase() throws Exception {
        initNominalContext();
        underTest = new SocialDocumentPublicationHandler(session,
                currentSocialDocument);

        assertTrue(underTest.isPrivatePublishable());
        DocumentModel proxy = underTest.publishPrivatelySocialDocument();
        String proxyId = proxy.getId();
        assertNotNull(String.format(
                "The proxy of the document \"%s\"should exist",
                currentSocialDocument.toString()), proxy);

        currentSocialDocument.putContextData(
                VersioningService.VERSIONING_OPTION, VersioningOption.MAJOR);
        session.saveDocument(currentSocialDocument);

        underTest = new SocialDocumentPublicationHandler(session,
                currentSocialDocument);
        assertTrue(underTest.isPrivatePublishable());
        proxy = underTest.publishPrivatelySocialDocument();

        assertNotNull(String.format(
                "The proxy of the document \"%s\"should exist",
                currentSocialDocument.toString()), proxy);
        assertEquals(proxyId, proxy.getId());

        assertTrue(String.format(
                "The social document \"%s\" should be publishable",
                currentSocialDocument), underTest.isPublicPublishable());
        proxy = underTest.publishPubliclySocialDocument();
        assertNotNull(String.format("The public proxy of \"%s\" should exist",
                currentSocialDocument), proxy);
        assertEquals(proxyId, proxy.getId());
    }

}
