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

import static org.junit.Assert.assertEquals;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_CREATED;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_REMOVED;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_UPDATED;
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamFilter.ACTOR_PARAMETER;
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamFilter.QUERY_TYPE_PARAMETER;
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamFilter.QueryType.ACTIVITY_STREAM_FOR_ACTOR;
import static org.nuxeo.ecm.social.user.relationship.UserRelationshipConstants.CIRCLE_RELATIONSHIP_KIND_GROUP;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityImpl;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.activity.ActivityStreamServiceImpl;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.persistence.PersistenceProvider;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.social.user.relationship.RelationshipKind;
import org.nuxeo.ecm.social.user.relationship.service.UserRelationshipService;
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
@Features(PlatformFeature.class)
@RepositoryConfig(repositoryName = "default", type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.core.persistence", "org.nuxeo.ecm.activity",
        "org.nuxeo.ecm.user.relationships", "org.nuxeo.ecm.social.user.activity.stream" })
@LocalDeploy("org.nuxeo.ecm.social.user.activity.stream:user-activity-stream-test.xml")
public class TestUserActivityStreamFilter {

    @Inject
    protected FeaturesRunner featuresRunner;

    @Inject
    protected ActivityStreamService activityStreamService;

    @Inject
    protected UserRelationshipService userRelationshipService;

    @Inject
    protected EventService eventService;

    @Inject
    protected CoreSession session;

    @Before
    public void cleanupDatabase() throws ClientException {
        ((ActivityStreamServiceImpl) activityStreamService).getOrCreatePersistenceProvider().run(
                true, new PersistenceProvider.RunVoid() {
                    public void runWith(EntityManager em) {
                        Query query = em.createQuery("delete from Activity");
                        query.executeUpdate();
                    }
                });
    }

    @Test
    public void t() {
        Pattern p = Pattern.compile("http://\\[?(.*?)\\]?:.*");
        String loopbackURL = "http://[2a01:240:fe8e:0:226:bbff:fe09:55cd]:8080/nuxeo";
        Matcher m = p.matcher(loopbackURL);



        Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");

        String s = "${actor} added ${object} as a relation with ${actor}.";
        m = pattern.matcher(s);
        while(m.find()) {
            String param = m.group().replaceAll("[\\|$\\|{\\}]", "");
            s = s.replace(m.group(), param);
        }

    }

    @Test
    public void shouldGetAllActivitiesFromUserNetwork() {
        RelationshipKind friends = RelationshipKind.newInstance(CIRCLE_RELATIONSHIP_KIND_GROUP,
                "friends");
        RelationshipKind coworkers = RelationshipKind.newInstance(CIRCLE_RELATIONSHIP_KIND_GROUP,
                "coworkers");
        String benderActivityObject = ActivityHelper.createUserActivityObject("Bender");
        String leelaActivityObject = ActivityHelper.createUserActivityObject("Leela");
        String fryActivityObject = ActivityHelper.createUserActivityObject("Fry");
        String zappActivityObject = ActivityHelper.createUserActivityObject("Zapp");
        userRelationshipService.addRelation(leelaActivityObject, benderActivityObject, friends);
        userRelationshipService.addRelation(leelaActivityObject, fryActivityObject, friends);
        userRelationshipService.addRelation(leelaActivityObject, zappActivityObject,
                coworkers);

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
        activity = new ActivityImpl();
        activity.setActor(benderActivityObject);
        activity.setObject("doc:default:docId1");
        activity.setVerb(DOCUMENT_REMOVED);
        activity.setPublishedDate(now.plusHours(4).toDate());
        activityStreamService.addActivity(activity);

        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_TYPE_PARAMETER, ACTIVITY_STREAM_FOR_ACTOR);
        parameters.put(ACTOR_PARAMETER, "Leela");
        List<Activity> activities = activityStreamService.query(
                UserActivityStreamFilter.ID, parameters);
        assertEquals(5, activities.size());

        activity = activities.get(0);
        assertEquals(benderActivityObject, activity.getActor());
        assertEquals(DOCUMENT_REMOVED, activity.getVerb());
        assertEquals("doc:default:docId1", activity.getObject());
        activity = activities.get(1);
        assertEquals(benderActivityObject, activity.getActor());
        assertEquals(CIRCLE_RELATIONSHIP_KIND_GROUP, activity.getVerb());
        assertEquals(zappActivityObject, activity.getObject());
        activity = activities.get(2);
        assertEquals(fryActivityObject, activity.getActor());
        assertEquals(CIRCLE_RELATIONSHIP_KIND_GROUP, activity.getVerb());
        assertEquals(benderActivityObject, activity.getObject());
    }
}
