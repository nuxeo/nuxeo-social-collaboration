/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
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

package org.nuxeo.ecm.wall;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.nuxeo.ecm.activity.ActivitiesList;
import org.nuxeo.ecm.activity.ActivitiesListImpl;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityReply;
import org.nuxeo.ecm.activity.ActivityStream;
import org.nuxeo.ecm.activity.ActivityStreamFilter;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.activity.ActivityStreamServiceImpl;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public class WallActivityStreamFilter implements ActivityStreamFilter {

    public static final String ID = "WallActivityStreamFilter";

    public static final String DEFAULT_WALL_ACTIVITY_STREAM_NAME = "defaultWallActivityStream";

    public static final String CONTEXT_DOCUMENT_PARAMETER = "contextDocumentParameter";

    public static final String ACTIVITY_STREAM_PARAMETER = "activityStreamParameter";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isInterestedIn(Activity activity) {
        return false;
    }

    @Override
    public void handleNewActivity(ActivityStreamService activityStreamService, Activity activity) {
        // nothing for now
    }

    @Override
    @Deprecated
    public void handleRemovedActivities(ActivityStreamService activityStreamService,
            Collection<Serializable> activityIds) {
        // nothing for now
    }

    @Override
    public void handleRemovedActivities(ActivityStreamService activityStreamService, ActivitiesList activities) {
        // nothing for now
    }

    @Override
    public void handleRemovedActivityReply(ActivityStreamService activityStreamService, Activity activity,
            ActivityReply activityReply) {
    }

    @Override
    public ActivitiesList query(ActivityStreamService activityStreamService, Map<String, Serializable> parameters,
            long offset, long limit) {
        DocumentModel doc = (DocumentModel) parameters.get(CONTEXT_DOCUMENT_PARAMETER);
        if (doc == null) {
            throw new IllegalArgumentException(CONTEXT_DOCUMENT_PARAMETER + " is required");
        }
        String docActivityObject = ActivityHelper.createDocumentActivityObject(doc);

        String activityStreamName = (String) parameters.get(ACTIVITY_STREAM_PARAMETER);
        if (activityStreamName == null) {
            activityStreamName = DEFAULT_WALL_ACTIVITY_STREAM_NAME;
        }
        ActivityStream wallActivityStream = activityStreamService.getActivityStream(activityStreamName);
        List<String> verbs = wallActivityStream.getVerbs();

        EntityManager em = ((ActivityStreamServiceImpl) activityStreamService).getEntityManager();
        Query query = em.createQuery("select activity from Activity activity " + "where activity.context = :context "
                + "and activity.verb in (:verbs) " + "and activity.actor like :actor "
                + "order by activity.lastUpdatedDate desc");
        query.setParameter("context", docActivityObject);
        query.setParameter("verbs", verbs);
        query.setParameter("actor", "user:%");

        if (limit > 0) {
            query.setMaxResults((int) limit);
        }
        if (offset > 0) {
            query.setFirstResult((int) offset);
        }
        return new ActivitiesListImpl(query.getResultList());
    }
}
