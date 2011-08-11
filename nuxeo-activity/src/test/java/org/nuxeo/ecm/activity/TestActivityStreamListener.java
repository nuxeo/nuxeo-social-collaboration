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

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.EventImpl;
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
public class TestActivityStreamListener {

    @Inject
    protected CoreSession session;

    @Inject
    protected EventService eventService;

    @Inject
    protected ActivityStreamService activityStreamService;

    @Before
    public void cleanupDatabase() throws ClientException {
        ((ActivityStreamServiceImpl) activityStreamService).getOrCreatePersistenceProvider().run(
                true, new PersistenceProvider.RunVoid() {
                    public void runWith(EntityManager em) {
                        Query query = em.createQuery("delete from Activity");
                        query.executeUpdate();
                        query = em.createQuery("delete from Tweet");
                        query.executeUpdate();
                    }
                });
    }

    @Test
    public void shouldAddNewActivitiesThroughListener() throws ClientException {
        Activity firstActivity = new ActivityImpl();
        firstActivity.setActor("Administrator");
        firstActivity.setVerb("tweeted");
        firstActivity.setObject("yo");
        firstActivity.setPublishedDate(new Date());
        ActivityEventContext activityEventContext = new ActivityEventContext(
                session, session.getPrincipal(), firstActivity);
        Event event = new EventImpl("activityStreamEvent", activityEventContext);
        eventService.fireEvent(event);

        Activity secondActivity = new ActivityImpl();
        secondActivity.setActor("bob");
        secondActivity.setVerb("tweeted");
        secondActivity.setObject("hello!");
        secondActivity.setPublishedDate(new Date());
        activityEventContext = new ActivityEventContext(session,
                session.getPrincipal(), secondActivity);
        event = new EventImpl("activityStreamEvent", activityEventContext);
        eventService.fireEvent(event);

        session.save();
        eventService.waitForAsyncCompletion();

        List<Activity> activities = activityStreamService.query(ActivityStreamService.ALL_ACTIVITIES, null);
        assertNotNull(activities);
        assertEquals(2, activities.size());
        Activity storedActivity = activities.get(0);
        assertEquals(1, storedActivity.getId());
        assertEquals(firstActivity.getActor(), storedActivity.getActor());
        assertEquals(firstActivity.getVerb(), storedActivity.getVerb());
        assertEquals(firstActivity.getObject(), storedActivity.getObject());

        storedActivity = activities.get(1);
        assertEquals(2, storedActivity.getId());
        assertEquals(secondActivity.getActor(), storedActivity.getActor());
        assertEquals(secondActivity.getVerb(), storedActivity.getVerb());
        assertEquals(secondActivity.getObject(), storedActivity.getObject());
    }

}
