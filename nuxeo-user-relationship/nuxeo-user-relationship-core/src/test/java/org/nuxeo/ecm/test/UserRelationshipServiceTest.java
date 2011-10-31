package org.nuxeo.ecm.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.social.user.relationship.service.UserRelationshipServiceImpl.KINDS_EXTENSION_POINT;

import java.util.List;

import com.google.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.user.relationship.RelationshipKind;
import org.nuxeo.ecm.social.user.relationship.service.UserRelationshipKindDescriptor;
import org.nuxeo.ecm.social.user.relationship.service.UserRelationshipService;
import org.nuxeo.ecm.social.user.relationship.service.UserRelationshipServiceImpl;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

/**
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.5
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

    @BeforeClass
    public static void setUp() throws Exception {
        // Register default kinds here, to ensure datasource is correctly defined.
        UserRelationshipServiceImpl service = (UserRelationshipServiceImpl)Framework.getLocalService(UserRelationshipService.class);
        service.registerContribution(buildKind("user", "friend"), KINDS_EXTENSION_POINT, null);
        service.registerContribution(buildKind("user", "coworker"), KINDS_EXTENSION_POINT, null);
        service.registerContribution(buildKind("user", "connected"), KINDS_EXTENSION_POINT, null);
        service.registerContribution(buildKind("user", "relation"), KINDS_EXTENSION_POINT, null);
        service.registerContribution(buildKind("other", "trucmuch"), KINDS_EXTENSION_POINT, null);

    }

    protected static UserRelationshipKindDescriptor buildKind(String group, String name) {
        UserRelationshipKindDescriptor desc = new UserRelationshipKindDescriptor();
        desc.setGroup(group);
        desc.setName(name);
        return desc;
    }

    @Test
    public void testServiceRegistering() throws Exception {
        UserRelationshipService tmp = Framework.getService(UserRelationshipService.class);
        assertNotNull(tmp);
        assertNotNull(session);
        assertNotNull(relationshipService);
    }

    @Test
    public void testRegisteredTypes() {
        assertEquals(5, relationshipService.getRegisteredKinds(null).size());
        assertEquals(4, relationshipService.getRegisteredKinds("user").size());
        assertEquals(1, relationshipService.getRegisteredKinds("other").size());
        assertEquals(0, relationshipService.getRegisteredKinds("fake").size());
    }

    @Test
    public void testFriendshipsCreation() throws ClientException {
        String user1 = ActivityHelper.createUserActivityObject(createUser(
                "user1").getId());
        String user2 = ActivityHelper.createUserActivityObject(createUser(
                "user2").getId());
        String user3 = ActivityHelper.createUserActivityObject(createUser(
                "user3").getId());
        String user4 = ActivityHelper.createUserActivityObject(createUser(
                "user4").getId());

        RelationshipKind relation = RelationshipKind.newInstance("group",
                "relation");
        RelationshipKind coworker = RelationshipKind.newInstance("group",
                "coworker");

        assertTrue(relationshipService.addRelation(user1, user2, relation));
        assertTrue(relationshipService.addRelation(user1, user2, coworker));
        assertTrue(relationshipService.addRelation(user1, user3, coworker));
        assertTrue(relationshipService.addRelation(user3, user4, coworker));
        assertTrue(relationshipService.addRelation(user1, user4, coworker));

        // Add the same twice
        assertFalse(relationshipService.addRelation(user1, user4, coworker));

        assertEquals(3, relationshipService.getTargets(user1).size());
        assertEquals(2,
                relationshipService.getRelationshipKinds(user1, user2).size());

        // is he into a relationship ?
        List<String> user1Relations = relationshipService.getTargetsOfKind(
                user1, relation);
        assertEquals(1, user1Relations.size());
        assertEquals(user2, user1Relations.get(0));

        // They broke up ...
        assertFalse(relationshipService.removeRelation(user1, user3, relation));
        assertTrue(relationshipService.removeRelation(user1, user2, relation));

        user1Relations = relationshipService.getTargetsOfKind(user1, relation);
        assertEquals(0, user1Relations.size());
    }

    @Test
    public void testRelationshipStringBuilder() {
        String relation1 = "circle:";
        String relation2 = ":friend";
        String relation3 = "circle:friend";

        RelationshipKind kind = RelationshipKind.fromString(relation1);
        assertEquals("circle", kind.getGroup());
        assertTrue(StringUtils.isEmpty(kind.getName()));

        kind = RelationshipKind.fromString(relation2);
        assertEquals("friend", kind.getName());
        assertTrue(StringUtils.isEmpty(kind.getGroup()));

        kind = RelationshipKind.fromString(relation3);
        assertEquals("circle", kind.getGroup());
        assertEquals("friend", kind.getName());

        assertNull(RelationshipKind.fromString("circleFriend")); // Without
                                                                 // separator
                                                                 // char
    }

    @Test
    public void testRelationshipKindsSearch() throws ClientException {
        String user = ActivityHelper.createUserActivityObject("user_kindSearch");

        RelationshipKind doc_read = RelationshipKind.newInstance("doc", "read");
        RelationshipKind doc_readWrite = RelationshipKind.newInstance("doc",
                "readWrite");

        RelationshipKind user_friend = RelationshipKind.newInstance("user",
                "friend");
        RelationshipKind user_coworker = RelationshipKind.newInstance("user",
                "coworker");
        RelationshipKind user_ignored = RelationshipKind.newInstance("user",
                "ignored");

        assertTrue(relationshipService.addRelation(user, "user2", user_friend));
        assertTrue(relationshipService.addRelation(user, "user2", user_coworker));
        assertTrue(relationshipService.addRelation(user, "user3", user_friend));
        assertTrue(relationshipService.addRelation(user, "user4", user_ignored));

        assertTrue(relationshipService.addRelation(user, "doc1", doc_read));
        assertTrue(relationshipService.addRelation(user, "doc2", doc_readWrite));
        assertTrue(relationshipService.addRelation(user, "doc3", doc_read));

        assertEquals(6, relationshipService.getTargets(user).size());
        assertEquals(
                3,
                relationshipService.getTargetsOfKind(user,
                        RelationshipKind.fromGroup("user")).size());
        assertEquals(
                1,
                relationshipService.getTargetsOfKind(user,
                        RelationshipKind.fromName("coworker")).size());
        assertEquals(
                0,
                relationshipService.getTargetsOfKind(user,
                        RelationshipKind.fromName("unknown")).size());
        assertEquals(
                0,
                relationshipService.getTargetsOfKind(user,
                        RelationshipKind.newInstance("user", "unknown")).size());
    }

    @Test
    public void testRelationshipWithFulltext() throws ClientException {
        String pattern = "patternToFind";
        String user1 = ActivityHelper.createUserActivityObject(createUser(
                "user21").getId());
        String user2 = ActivityHelper.createUserActivityObject(createUser(
                "user" + pattern).getId());
        String user3 = ActivityHelper.createUserActivityObject(createUser(
                "user23").getId());
        String doc1 = ActivityHelper.DOC_PREFIX + createDoc("doc21").getId();
        String doc2 = ActivityHelper.DOC_PREFIX + "doc" + pattern;

        RelationshipKind read = RelationshipKind.newInstance("document",
                "have_read");
        RelationshipKind coworker = RelationshipKind.newInstance("group",
                "relation");

        assertTrue(relationshipService.addRelation(user1, user3, coworker));
        assertTrue(relationshipService.addRelation(user1, user2, coworker));
        assertTrue(relationshipService.addRelation(user1, doc1, read));
        assertTrue(relationshipService.addRelation(user1, doc2, read));

        assertEquals(4, relationshipService.getTargets(user1).size());
        assertEquals(
                2,
                relationshipService.getTargetsWithFulltext(user1,
                        ActivityHelper.USER_PREFIX).size());
        assertEquals(
                2,
                relationshipService.getTargetsWithFulltext(user1,
                        ActivityHelper.DOC_PREFIX).size());

        List<String> targetsWithFulltext = relationshipService.getTargetsWithFulltext(
                user1, pattern);
        assertEquals(2, targetsWithFulltext.size());

        assertEquals(
                1,
                relationshipService.getTargetsWithFulltext(user1, read, pattern).size());

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
