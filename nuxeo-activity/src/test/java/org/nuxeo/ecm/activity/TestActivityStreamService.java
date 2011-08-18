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
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.nuxeo.ecm.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.persistence.PersistenceProvider;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
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
@RepositoryConfig(repositoryName = "default", type = BackendType.H2, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.core.persistence", "org.nuxeo.ecm.activity" })
@LocalDeploy("org.nuxeo.ecm.activity:activity-stream-service-test.xml")
public class TestActivityStreamService {

    @Inject
    protected ActivityStreamService activityStreamService;

    @Before
    public void cleanupDatabase() throws ClientException {
        ((ActivityStreamServiceImpl) activityStreamService).getOrCreatePersistenceProvider().run(
                true, new PersistenceProvider.RunVoid() {
                    @Override
                    public void runWith(EntityManager em) {
                        Query query = em.createQuery("delete from Activity");
                        query.executeUpdate();
                        query = em.createQuery("delete from Tweet");
                        query.executeUpdate();
                    }
                });
    }

    @Test
    public void serviceRegistration() {
        assertNotNull(activityStreamService);
    }

    @Test
    public void shouldStoreAnActivity() {
        Activity activity = new ActivityImpl();
        activity.setActor("Administrator");
        activity.setVerb("test");
        activity.setObject("yo");
        activity.setPublishedDate(new Date());
        activityStreamService.addActivity(activity);

        List<Activity> activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null);
        assertNotNull(activities);
        assertEquals(1, activities.size());

        Activity storedActivity = activities.get(0);
        assertEquals(activity.getActor(), storedActivity.getActor());
        assertEquals(activity.getVerb(), storedActivity.getVerb());
        assertEquals(activity.getObject(), storedActivity.getObject());
    }

    @Test
    public void shouldCallRegisteredActivityStreamFilter() {
        Activity activity = new ActivityImpl();
        activity.setActor("Administrator");
        activity.setVerb("test");
        activity.setObject("yo");
        activity.setPublishedDate(new Date());
        activityStreamService.addActivity(activity);

        Map<String, ActivityStreamFilter> filters = ((ActivityStreamServiceImpl) activityStreamService).activityStreamFilters;
        assertEquals(2, filters.size());

        List<Activity> activities = activityStreamService.query(
                DummyActivityStreamFilter.ID, null);
        assertNotNull(activities);
        assertEquals(1, activities.size());

        activities = filters.get(DummyActivityStreamFilter.ID).query(null,
                null, 0, 0);
        assertEquals(1, activities.size());

        Activity storedActivity = activities.get(0);
        assertEquals(activity.getActor(), storedActivity.getActor());
        assertEquals(activity.getVerb(), storedActivity.getVerb());
        assertEquals(activity.getObject(), storedActivity.getObject());
    }

    @Test(expected = ClientRuntimeException.class)
    public void shouldThrowExceptionIfFilterIsNotRegistered() {
        Activity activity = new ActivityImpl();
        activity.setActor("Administrator");
        activity.setVerb("test");
        activity.setObject("yo");
        activity.setPublishedDate(new Date());
        activityStreamService.addActivity(activity);

        activityStreamService.query("nonExistingFilter", null);
    }

    @Test
    public void shouldHandlePagination() {
        addTestActivities(10);

        List<Activity> activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null, 5, 1);
        assertEquals(5, activities.size());
        for (int i = 0; i < 5; i++) {
            assertEquals("activity" + i, activities.get(i).getObject());
        }

        activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null, 5, 2);
        assertEquals(5, activities.size());
        for (int i = 5; i < 10; i++) {
            assertEquals("activity" + i, activities.get(i - 5).getObject());
        }

        activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null, 15, 1);
        assertEquals(10, activities.size());
        for (int i = 0; i < 10; i++) {
            assertEquals("activity" + i, activities.get(i).getObject());
        }

        activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null, 5, 3);
        assertEquals(0, activities.size());
    }

    protected void addTestActivities(int activitiesCount) {
        for (int i = 0; i < activitiesCount; i++) {
            Activity activity = new ActivityImpl();
            activity.setActor("Administrator");
            activity.setVerb("test");
            activity.setObject("activity" + i);
            activity.setPublishedDate(new Date());
            activityStreamService.addActivity(activity);
        }
    }

    @Test
    public void shouldStoreTweetActivities() {
        Activity activity = new ActivityImpl();
        activity.setActor("Administrator");
        activity.setVerb(TweetActivityStreamFilter.TWEET_VERB);
        activity.setObject("yo");
        activity.setPublishedDate(new Date());
        activityStreamService.addActivity(activity);

        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("seenBy", "Bob");
        List<Activity> activities = activityStreamService.query(
                TweetActivityStreamFilter.ID, parameters);
        assertEquals(1, activities.size());
        Activity storedActivity = activities.get(0);
        assertEquals(activity.getActor(), storedActivity.getActor());
        assertEquals(activity.getVerb(), storedActivity.getVerb());
        assertEquals(activity.getObject(), storedActivity.getObject());

        parameters = new HashMap<String, Serializable>();
        parameters.put("seenBy", "Joe");
        activities = activityStreamService.query(TweetActivityStreamFilter.ID,
                parameters);
        assertEquals(1, activities.size());
        storedActivity = activities.get(0);
        assertEquals(activity.getActor(), storedActivity.getActor());
        assertEquals(activity.getVerb(), storedActivity.getVerb());
        assertEquals(activity.getObject(), storedActivity.getObject());

        parameters = new HashMap<String, Serializable>();
        parameters.put("seenBy", "John");
        activities = activityStreamService.query(TweetActivityStreamFilter.ID,
                parameters);
        assertEquals(1, activities.size());
        storedActivity = activities.get(0);
        assertEquals(activity.getActor(), storedActivity.getActor());
        assertEquals(activity.getVerb(), storedActivity.getVerb());
        assertEquals(activity.getObject(), storedActivity.getObject());
    }

}
