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
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_REMOVED;
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamFilter.ACTOR_PARAMETER;
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamFilter.QUERY_TYPE_PARAMETER;
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamFilter.QueryType.ACTIVITY_STREAM_FOR_ACTOR;
import static org.nuxeo.ecm.social.user.relationship.UserRelationshipConstants.CIRCLE_RELATIONSHIP_KIND_GROUP;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.activity.Activity;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */

public class TestUserActivityStreamFilter extends AbstractUserActivityTest {

    @Before
    public void disableActivityStreamListener() {
        eventServiceAdmin.setListenerEnabledFlag("activityStreamListener",
                false);
    }

    @Test
    public void shouldGetAllActivitiesFromUserNetwork() {
        initializeSomeRelations();
        initializeDummyDocumentRelatedActivities();

        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_TYPE_PARAMETER, ACTIVITY_STREAM_FOR_ACTOR);
        parameters.put(ACTOR_PARAMETER, "Leela");
        List<Activity> activities = activityStreamService.query(
                UserActivityStreamFilter.ID, parameters);
        assertEquals(5, activities.size());

        Activity activity = activities.get(0);
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
