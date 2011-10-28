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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class TweetActivityStreamFilter implements ActivityStreamFilter {

    public static final String ID = "TweetActivityStreamFilter";

    public static final String TWEET_VERB = "tweet";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isInterestedIn(Activity activity) {
        return true;
    }

    @Override
    public void handleNewActivity(ActivityStreamService activityStreamService,
            Activity activity) {
        if (TWEET_VERB.equals(activity.getVerb())) {
            EntityManager em = ((ActivityStreamServiceImpl) activityStreamService).getEntityManager();
            TweetActivity tweetActivity = new TweetActivity();
            tweetActivity.setActivityId(activity.getId());
            tweetActivity.setSeenBy("Bob");
            em.persist(tweetActivity);
            tweetActivity = new TweetActivity();
            tweetActivity.setActivityId(activity.getId());
            tweetActivity.setSeenBy("Joe");
            em.persist(tweetActivity);
            tweetActivity = new TweetActivity();
            tweetActivity.setActivityId(activity.getId());
            tweetActivity.setSeenBy("John");
            em.persist(tweetActivity);
        }
    }

    @Override
    public void handleRemovedActivities(
            ActivityStreamService activityStreamService,
            Collection<Serializable> activityIds) {
        EntityManager em = ((ActivityStreamServiceImpl) activityStreamService).getEntityManager();
        Query query = em.createQuery("delete from Tweet tweet where tweet.activityId in (:activityIds)");
        query.setParameter("activityIds", activityIds);
        query.executeUpdate();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ActivitiesList query(ActivityStreamService activityStreamService,
            Map<String, Serializable> parameters, long offset, long limit) {
        if (parameters.containsKey("seenBy")) {
            String seenBy = (String) parameters.get("seenBy");
            EntityManager em = ((ActivityStreamServiceImpl) activityStreamService).getEntityManager();
            Query query = em.createQuery("select activity from Tweet tweet, Activity activity where tweet.seenBy=:seenBy and tweet.activityId = activity.id");
            query.setParameter("seenBy", seenBy);
            return new ActivitiesListImpl(query.getResultList());
        }
        return new ActivitiesListImpl();
    }

}
