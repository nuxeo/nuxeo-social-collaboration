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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.nuxeo.ecm.activity.ActivitiesList;
import org.nuxeo.ecm.activity.ActivitiesListImpl;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityStreamFilter;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.activity.ActivityStreamServiceImpl;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.social.user.relationship.RelationshipKind;
import org.nuxeo.ecm.social.user.relationship.service.UserRelationshipService;
import org.nuxeo.runtime.api.Framework;

/**
 * Activity Stream filter handling user activity stream.
 * <p>
 * The different queries this filter can handle are defined in the
 * {@link QueryType} enum.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class UserActivityStreamFilter implements ActivityStreamFilter {

    public static final String ID = "UserActivityStreamFilter";

    public enum QueryType {
        ACTIVITY_STREAM_FOR_ACTOR, ACTIVITY_STREAM_FROM_ACTOR
    }

    public static final String QUERY_TYPE_PARAMETER = "queryType";

    public static final String ACTOR_PARAMETER = "actor";

    public static final String[] VERBS = new String[] { DOCUMENT_CREATED,
            DOCUMENT_UPDATED, DOCUMENT_REMOVED, "socialworkspace:members",
            "circle" };

    private UserRelationshipService userRelationshipService;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isInterestedIn(Activity activity) {
        return false;
    }

    @Override
    public void handleNewActivity(ActivityStreamService activityStreamService,
            Activity activity) {
        // nothing for now
    }

    @SuppressWarnings("unchecked")
    @Override
    public ActivitiesList query(ActivityStreamService activityStreamService,
            Map<String, Serializable> parameters, int pageSize, int currentPage) {
        QueryType queryType = (QueryType) parameters.get(QUERY_TYPE_PARAMETER);
        if (queryType == null) {
            throw new IllegalArgumentException(QUERY_TYPE_PARAMETER
                    + " is required.");
        }

        String actor = (String) parameters.get(ACTOR_PARAMETER);
        if (actor == null) {
            throw new IllegalArgumentException(ACTOR_PARAMETER + " is required");
        }
        actor = ActivityHelper.createUserActivityObject(actor);

        EntityManager em = ((ActivityStreamServiceImpl) activityStreamService).getEntityManager();
        Query query;
        switch (queryType) {
        case ACTIVITY_STREAM_FOR_ACTOR:
            List<String> actors = getUserRelationshipService().getTargetsOfKind(
                    actor,
                    RelationshipKind.fromString("socialworkspace:members"));
            actors.addAll(getUserRelationshipService().getTargetsOfKind(
                    actor,
                    RelationshipKind.fromGroup("circle")));
            if (actors.isEmpty()) {
                return new ActivitiesListImpl();
            }

            query = em.createQuery("select activity from Activity activity where activity.actor in (:actors) and activity.verb in (:verbs) order by activity.publishedDate desc");
            query.setParameter("actors", actors);
            query.setParameter("verbs", Arrays.asList(VERBS));
            break;
        case ACTIVITY_STREAM_FROM_ACTOR:
            query = em.createQuery("select activity from Activity activity where activity.actor = :actor and activity.verb in (:verbs) order by activity.publishedDate desc");
            query.setParameter("actor", actor);
            query.setParameter("verbs", Arrays.asList(VERBS));
            break;
        default:
            throw new IllegalArgumentException("Invalid QueryType parameter");
        }

        if (pageSize > 0) {
            query.setMaxResults(pageSize);
            if (currentPage > 0) {
                query.setFirstResult(currentPage * pageSize);
            }
        }
        return new ActivitiesListImpl(query.getResultList());
    }

    private UserRelationshipService getUserRelationshipService()
            throws ClientRuntimeException {
        if (userRelationshipService == null) {
            try {
                userRelationshipService = Framework.getService(UserRelationshipService.class);
            } catch (Exception e) {
                final String errMsg = "Error connecting to UserRelationshipService. "
                        + e.getMessage();
                throw new ClientRuntimeException(errMsg, e);
            }
            if (userRelationshipService == null) {
                throw new ClientRuntimeException(
                        "UserRelationshipService service not bound");
            }
        }
        return userRelationshipService;
    }

}
