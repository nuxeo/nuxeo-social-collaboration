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

package org.nuxeo.ecm.social.mini.message;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.nuxeo.ecm.activity.ActivitiesList;
import org.nuxeo.ecm.activity.ActivitiesListImpl;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityReply;
import org.nuxeo.ecm.activity.ActivityStreamFilter;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.activity.ActivityStreamServiceImpl;
import org.nuxeo.ecm.social.relationship.RelationshipKind;
import org.nuxeo.ecm.social.relationship.service.RelationshipService;
import org.nuxeo.runtime.api.Framework;

/**
 * Activity Stream filter handling mini message activities.
 * <p>
 * The different queries this filter can handle are defined in the
 * {@link QueryType} enum.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class MiniMessageActivityStreamFilter implements ActivityStreamFilter {

    public static final String ID = "MiniMessageActivityStreamFilter";

    public enum QueryType {
        MINI_MESSAGES_FOR_ACTOR, MINI_MESSAGES_FROM_ACTOR, MINI_MESSAGE_BY_ID
    }

    public static final String VERB = "minimessage";

    public static final String QUERY_TYPE_PARAMETER = "queryType";

    public static final String ACTOR_PARAMETER = "actor";

    public static final String RELATIONSHIP_KIND_PARAMETER = "relationshipKind";

    public static final String CONTEXT_PARAMETER = "context";

    public static final String MINI_MESSAGE_ID_PARAMETER = "miniMessageId";

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
        // nothing to do
    }

    @Override
    @Deprecated
    public void handleRemovedActivities(
            ActivityStreamService activityStreamService,
            Collection<Serializable> activityIds) {
        // nothing to do
    }

    @Override
    public void handleRemovedActivities(
            ActivityStreamService activityStreamService,
            ActivitiesList activities) {
    }

    @Override
    public void handleRemovedActivityReply(
            ActivityStreamService activityStreamService, Activity activity,
            ActivityReply activityReply) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public ActivitiesList query(ActivityStreamService activityStreamService,
            Map<String, Serializable> parameters, long offset, long limit) {
        QueryType queryType = (QueryType) parameters.get(QUERY_TYPE_PARAMETER);
        if (queryType == null) {
            throw new IllegalArgumentException(QUERY_TYPE_PARAMETER
                    + " is required.");
        }
        String actor = (String) parameters.get(ACTOR_PARAMETER);
        String context = (String) parameters.get(CONTEXT_PARAMETER);

        EntityManager em = ((ActivityStreamServiceImpl) activityStreamService).getEntityManager();
        Query query;
        switch (queryType) {
        case MINI_MESSAGES_FOR_ACTOR:
            if (actor == null) {
                throw new IllegalArgumentException(ACTOR_PARAMETER
                        + " is required");
            }

            RelationshipKind relationshipKind = (RelationshipKind) parameters.get(RELATIONSHIP_KIND_PARAMETER);
            RelationshipService relationshipService = Framework.getLocalService(RelationshipService.class);
            List<String> actors = relationshipService.getTargetsOfKind(actor,
                    relationshipKind);
            actors.add(actor);

            StringBuilder sb = new StringBuilder(
                    "select activity from Activity activity where activity.actor in (:actors) and activity.verb = :verb ");
            if (context != null) {
                sb.append("and (activity.context = :context or activity.target = :target) ");
            } else {
                sb.append("and activity.context is null and activity.target is null ");
            }
            sb.append("order by activity.publishedDate desc");
            query = em.createQuery(sb.toString());
            query.setParameter("actors", actors);
            query.setParameter("verb", VERB);
            if (context != null) {
                query.setParameter("context", context);
                query.setParameter("target", context);
            }
            break;
        case MINI_MESSAGES_FROM_ACTOR:
            if (actor == null) {
                throw new IllegalArgumentException(ACTOR_PARAMETER
                        + " is required");
            }
            query = em.createQuery("select activity from Activity activity "
                    + "where activity.actor = :actor and activity.verb = :verb "
                    + "and activity.context is null and activity.target is null "
                    + "order by activity.publishedDate desc");
            query.setParameter(ACTOR_PARAMETER, actor);
            query.setParameter("verb", VERB);
            break;
        case MINI_MESSAGE_BY_ID:
            query = em.createQuery("select activity from Activity activity where activity.id = :id");
            Serializable miniMessageId = parameters.get(MINI_MESSAGE_ID_PARAMETER);
            if (miniMessageId == null) {
                throw new IllegalArgumentException(MINI_MESSAGE_ID_PARAMETER
                        + " is required");
            }
            query.setParameter("id", miniMessageId);
            break;
        default:
            throw new IllegalArgumentException("Invalid QueryType parameter");
        }

        if (limit > 0) {
            query.setMaxResults((int) limit);
        }
        if (offset > 0) {
            query.setFirstResult((int) offset);
        }
        return new ActivitiesListImpl(query.getResultList());
    }

}
