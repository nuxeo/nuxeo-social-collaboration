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
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityStreamFilter;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.activity.ActivityStreamServiceImpl;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.social.user.relationship.RelationshipKind;
import org.nuxeo.ecm.social.user.relationship.service.UserRelationshipService;
import org.nuxeo.runtime.api.Framework;

/**
 * Activity Stream filter handling mini message activities.
 * <p>
 * The different queries this filter can handle are defined in the
 * {@link QueryType} enum.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class MiniMessageActivityStreamFilter implements ActivityStreamFilter {

    public static final String ID = "MiniMessageActivityStreamFilter";

    public enum QueryType {
        MINI_MESSAGES_FOR_ACTOR, MINI_MESSAGES_FROM_ACTOR
    }

    public static final String VERB = "minimessage";

    public static final String QUERY_TYPE_PARAMETER = "queryType";

    public static final String ACTOR_PARAMETER = "actor";

    public static final String RELATIONSHIP_KIND_PARAMETER = "relationshipKind";

    private UserRelationshipService userRelationshipService;

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

    @SuppressWarnings("unchecked")
    @Override
    public List<Activity> query(ActivityStreamService activityStreamService,
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

        EntityManager em = ((ActivityStreamServiceImpl) activityStreamService).getEntityManager();
        Query query = null;
        switch (queryType) {
        case MINI_MESSAGES_FOR_ACTOR:
            RelationshipKind relationshipKind = (RelationshipKind) parameters.get(RELATIONSHIP_KIND_PARAMETER);
            List<String> users = getUserRelationshipService().getTargetsOfKind(
                    actor, relationshipKind);
            users.add(actor);
            query = em.createQuery("select activity from Activity activity where activity.actor in (:actor) and activity.verb = :verb order by activity.publishedDate desc");
            query.setParameter(ACTOR_PARAMETER, users);
            query.setParameter("verb", VERB);
            break;
        case MINI_MESSAGES_FROM_ACTOR:
            query = em.createQuery("select activity from Activity activity where activity.actor = :actor and activity.verb = :verb order by activity.publishedDate desc");
            query.setParameter(ACTOR_PARAMETER, actor);
            query.setParameter("verb", VERB);
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
        return query.getResultList();
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
