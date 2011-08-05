package org.nuxeo.ecm.social.workspace;

import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialWorkspace;

import java.util.Arrays;

import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.user.relationship.service.UserRelationshipService;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@RepositoryConfig(type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({
        "org.nuxeo.ecm.platform.api",
        "org.nuxeo.ecm.platform.dublincore",
        "org.nuxeo.ecm.directory",
        "org.nuxeo.ecm.directory.sql",
        "org.nuxeo.ecm.directory.types.contrib",
        "org.nuxeo.ecm.platform.usermanager.api",
        "org.nuxeo.ecm.platform.usermanager",
        "org.nuxeo.ecm.platform.test:test-usermanagerimpl/directory-config.xml",
        "org.nuxeo.ecm.platform.picture.core:OSGI-INF/picturebook-schemas-contrib.xml",
        "org.nuxeo.ecm.platform.content.template",
        "org.nuxeo.ecm.opensocial.spaces",
        "org.nuxeo.ecm.social.workspace.core",
        "org.nuxeo.ecm.platform.content.template",
        "org.nuxeo.ecm.user.relationships"})
@LocalDeploy( { "org.nuxeo.ecm.user.relationships:test-user-relationship-directories-contrib.xml" })
public abstract class AbstractSocialWorkspaceTest {

    @Inject
    protected CoreSession session;

    @Inject
    protected UserManager userManager;

    @Inject
    protected UserRelationshipService userRelationshipService;

    @Inject
    protected FeaturesRunner featuresRunner;

    protected SocialWorkspace socialWorkspace;

    protected DocumentModel socialWorkspaceDoc;

    /**
     * Creates document and wait for all post-commit listener execution
     */
    public DocumentModel createDocument(String pathAsString, String name,
            String type) throws Exception {
        DocumentModel doc = session.createDocumentModel(pathAsString, name,
                type);
        doc.setPropertyValue("dc:title", name);
        doc = session.createDocument(doc);
        session.save(); // fire post commit event listener
        session.save(); // flush the session to retrieve document
        Framework.getService(EventService.class).waitForAsyncCompletion();
        return doc;
    }

    /**
     * Creates document and wait for all post-commit listener execution
     */
    public DocumentModel createSocialDocument(String pathAsString, String name,
            String type, boolean isPublic) throws Exception {
        DocumentModel doc = session.createDocumentModel(pathAsString, name, type);
        doc.setPropertyValue(
                SocialConstants.SOCIAL_DOCUMENT_IS_PUBLIC_PROPERTY, isPublic);
        doc = session.createDocument(doc);
        session.save(); // fire post commit event listener
        session.save(); // flush the session to retrieve document
        Framework.getService(EventService.class).waitForAsyncCompletion();
        return doc;
    }

    protected SocialWorkspace createSocialWorkspace(String socialWorkspaceName,
            boolean isPublic) throws Exception {
        DocumentModel doc = createDocument(
                session.getRootDocument().getPathAsString(),
                socialWorkspaceName, SOCIAL_WORKSPACE_TYPE);
        SocialWorkspace sw = toSocialWorkspace(doc);

        if (isPublic) {
            sw.makePublic();
        }
        return sw;
    }

    protected SocialWorkspace createSocialWorkspace(String socialWorkspaceName)
            throws Exception {
        return createSocialWorkspace(socialWorkspaceName, false);
    }

    protected NuxeoPrincipal createUserWithGroup(String username,
            String... groups) throws ClientException {
        NuxeoPrincipalImpl user = new NuxeoPrincipalImpl(username);
        user.allGroups = Arrays.asList(groups);
        return user;
    }

    protected void switchUser(String username) {
        featuresRunner.getFeature(CoreFeature.class).getRepository().switchUser(username);
    }

    protected void switchBackToAdministrator() {
        featuresRunner.getFeature(CoreFeature.class).getRepository().switchToAdminUser("Administrator");
    }
}
