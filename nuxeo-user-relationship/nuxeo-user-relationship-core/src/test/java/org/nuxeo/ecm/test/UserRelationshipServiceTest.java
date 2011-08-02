package org.nuxeo.ecm.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.user.relationship.service.UserRelationshipService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.4.3
 */
@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(repositoryName = "default", type = BackendType.H2, user = "Administrator", cleanup = Granularity.METHOD)
@LocalDeploy("org.nuxeo.ecm.user.relationships:OSGI-INF/user-relationship-directories-test-contrib.xml")
@Deploy("org.nuxeo.ecm.user.relationships")
public class UserRelationshipServiceTest {

    @Inject
    CoreSession session;

    @Inject
    UserRelationshipService relationshipService;

    @Inject
    UserManager userManager;

    @Test
    public void testServiceRegistering() throws Exception {
        UserRelationshipService tmp = Framework.getService(UserRelationshipService.class);
        assertNotNull(tmp);
        assertNotNull(session);
        assertNotNull(relationshipService);
    }

    @Test
    public void testTypes() {
        assertEquals(4, relationshipService.getKinds().size());
    }

    @Test
    public void testFriendshipsCreation() throws ClientException {
        String user1 = createUser("user1").getId();
        String user2 = createUser("user2").getId();
        String user3 = createUser("user3").getId();
        String user4 = createUser("user4").getId();

        String relation = "relation";
        String coworker = "coworker";

        assertTrue(relationshipService.addRelation(user1, user2, relation));
        assertTrue(relationshipService.addRelation(user1, user2, coworker));
        assertTrue(relationshipService.addRelation(user1, user3, coworker));
        assertTrue(relationshipService.addRelation(user3, user4, coworker));
        assertTrue(relationshipService.addRelation(user1, user4, coworker));

        // Add the same twice
        assertFalse(relationshipService.addRelation(user1, user4, coworker));

        assertEquals(3, relationshipService.getTargets(user1).size());
        assertEquals(2, relationshipService.getRelationshipKinds(user1, user2).size());

        // is he into a relationship ?
        List<String> user1Relations = relationshipService.getTargetsOfKind(user1,
                relation);
        assertEquals(1, user1Relations.size());
        assertEquals("user2", user1Relations.get(0));

        // They broke up ...
        assertFalse(relationshipService.removeRelation(user1, user3, relation));
        assertTrue(relationshipService.removeRelation(user1, user2, relation));

        user1Relations = relationshipService.getTargetsOfKind(user1, relation);
        assertEquals(0, user1Relations.size());
    }

    @Test
    public void testRelationshipWithPrefix() throws ClientException {
        final String USER_PREFIX = "user:";
        final String DOC_PREFIX = "doc:";

        String user1 = USER_PREFIX + createUser("user21").getId();
        String user2 = USER_PREFIX + createUser("user22").getId();
        String user3 = USER_PREFIX + createUser("user23").getId();
        String doc1 = DOC_PREFIX + createDoc("doc21").getId();

        String read = "have_read";
        String coworker = "coworker";

        assertTrue(relationshipService.addRelation(user1, user3, coworker));
        assertTrue(relationshipService.addRelation(user1, user2, coworker));
        assertTrue(relationshipService.addRelation(user1, doc1, read));

        assertEquals(3, relationshipService.getTargets(user1).size());
        assertEquals(2, relationshipService.getTargetsWithPrefix(user1, USER_PREFIX).size());
        assertEquals(1, relationshipService.getTargetsWithPrefix(user1, DOC_PREFIX).size());
    }

    protected DocumentModel createUser(String username) throws ClientException {
        DocumentModel user = userManager.getBareUserModel();
        user.setPropertyValue("user:username", username);
        try {
            return userManager.createUser(user);
        } finally {
            session.save();
        }
    }

    protected DocumentModel createDoc(String title) throws ClientException {
        DocumentModel doc = session.createDocumentModel("File");
        doc.setProperty("dublincore", "title", title);
        try {
            return session.createDocument(doc);
        } finally {
            session.save();
        }
    }
}
