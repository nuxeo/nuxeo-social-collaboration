/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.nuxeo.ecm.social.activity.stream;

import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_CREATED;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_REMOVED;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_UPDATED;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.EVERYTHING;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ;
import static org.nuxeo.ecm.social.user.relationship.UserRelationshipConstants.CIRCLE_RELATIONSHIP_KIND_GROUP;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityImpl;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.activity.ActivityStreamServiceImpl;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.EventServiceAdmin;
import org.nuxeo.ecm.core.persistence.PersistenceProvider;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.social.relationship.RelationshipKind;
import org.nuxeo.ecm.social.relationship.service.RelationshipService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(repositoryName = "default", type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.core.persistence", "org.nuxeo.ecm.activity",
        "org.nuxeo.ecm.user.relationships",
        "org.nuxeo.ecm.social.user.activity.stream" })
@LocalDeploy("org.nuxeo.ecm.social.user.activity.stream:user-activity-stream-test.xml")
public abstract class AbstractUserActivityTest {

    @Inject
    protected FeaturesRunner featuresRunner;

    @Inject
    protected ActivityStreamService activityStreamService;

    @Inject
    protected RelationshipService relationshipService;

    @Inject
    protected EventService eventService;

    @Inject
    protected EventServiceAdmin eventServiceAdmin;

    @Inject
    protected CoreSession session;

    protected String benderActivityObject = ActivityHelper.createUserActivityObject("Bender");

    protected String leelaActivityObject = ActivityHelper.createUserActivityObject("Leela");

    protected String fryActivityObject = ActivityHelper.createUserActivityObject("Fry");

    protected String zappActivityObject = ActivityHelper.createUserActivityObject("Zapp");

    @Before
    public void cleanupDatabase() throws ClientException {
        ((ActivityStreamServiceImpl) activityStreamService).getOrCreatePersistenceProvider().run(
                true, new PersistenceProvider.RunVoid() {
                    @Override
                    public void runWith(EntityManager em) {
                        Query query = em.createQuery("delete from Activity");
                        query.executeUpdate();
                    }
                });
    }

    @Before
    public void disableBinaryTextListener() {
        eventServiceAdmin.setListenerEnabledFlag("sql-storage-binary-text",
                false);
    }

    protected CoreSession openSessionAs(String username) throws ClientException {
        CoreFeature coreFeature = featuresRunner.getFeature(CoreFeature.class);
        return coreFeature.getRepository().getRepositoryHandler().openSessionAs(
                username);
    }

    protected void initializeSomeRelations() {
        RelationshipKind friends = RelationshipKind.newInstance(
                CIRCLE_RELATIONSHIP_KIND_GROUP, "friends");
        RelationshipKind coworkers = RelationshipKind.newInstance(
                CIRCLE_RELATIONSHIP_KIND_GROUP, "coworkers");

        relationshipService.addRelation(leelaActivityObject,
                benderActivityObject, friends);
        relationshipService.addRelation(leelaActivityObject, fryActivityObject,
                friends);
        relationshipService.addRelation(leelaActivityObject,
                zappActivityObject, coworkers);

        initializeRelationsActivities();
    }

    protected void initializeRelationsActivities() {
        DateTime now = new DateTime();
        Activity activity = new ActivityImpl();
        activity.setActor(fryActivityObject);
        activity.setObject(benderActivityObject);
        activity.setVerb(CIRCLE_RELATIONSHIP_KIND_GROUP);
        activity.setPublishedDate(now.plusHours(2).toDate());
        activityStreamService.addActivity(activity);
        activity = new ActivityImpl();
        activity.setActor(benderActivityObject);
        activity.setObject(zappActivityObject);
        activity.setVerb(CIRCLE_RELATIONSHIP_KIND_GROUP);
        activity.setPublishedDate(now.plusHours(3).toDate());
        activityStreamService.addActivity(activity);
    }

    protected void initializeDummyDocumentRelatedActivities() {
        DateTime now = new DateTime();
        Activity activity = new ActivityImpl();
        activity.setActor(benderActivityObject);
        activity.setObject("doc:default:docId1");
        activity.setVerb(DOCUMENT_CREATED);
        activity.setPublishedDate(now.toDate());
        activityStreamService.addActivity(activity);
        activity = new ActivityImpl();
        activity.setActor(benderActivityObject);
        activity.setObject("doc:default:docId1");
        activity.setVerb(DOCUMENT_UPDATED);
        activity.setPublishedDate(now.plusHours(1).toDate());
        activityStreamService.addActivity(activity);
        activity = new ActivityImpl();
        activity.setActor(benderActivityObject);
        activity.setObject("doc:default:docId1");
        activity.setVerb(DOCUMENT_REMOVED);
        activity.setPublishedDate(now.plusHours(4).toDate());
        activityStreamService.addActivity(activity);
    }

    protected void createDocumentsWithBender() throws ClientException {
        DocumentModel workspacesDocument = session.getDocument(new PathRef(
                "/default-domain/workspaces"));
        ACP acp = workspacesDocument.getACP();
        ACL acl = acp.getOrCreateACL();
        acl.add(new ACE("Bender", EVERYTHING, true));
        acl.add(new ACE("Leela", READ, true));
        workspacesDocument.setACP(acp, true);
        session.save();
        session.save();

        CoreSession newSession = openSessionAs("Bender");
        DocumentModel doc = newSession.createDocumentModel(
                workspacesDocument.getPathAsString(), "file1", "File");
        doc = newSession.createDocument(doc);
        acp = doc.getACP();
        acl = acp.getOrCreateACL();
        acl.add(new ACE("Leela", READ, true));
        doc.setACP(acp, true);
        newSession.save();
        session.save();

        doc = newSession.createDocumentModel(workspacesDocument.getPathAsString(),
                "file2", "File");
        doc = newSession.createDocument(doc);
        acp = doc.getACP();
        acl = acp.getOrCreateACL();
        acl.add(new ACE("Leela", READ, true));
        doc.setACP(acp, true);
        newSession.save();
        newSession.save();

        doc = newSession.createDocumentModel(workspacesDocument.getPathAsString(),
                "file-without-right", "File");
        doc = newSession.createDocument(doc);
        acp = doc.getACP();
        acl = acp.getOrCreateACL();
        acl.add(new ACE("Leela", READ, false));
        doc.setACP(acp, true);
        newSession.save();
        newSession.save();

        eventService.waitForAsyncCompletion();

        CoreInstance.getInstance().close(newSession);
    }

}
