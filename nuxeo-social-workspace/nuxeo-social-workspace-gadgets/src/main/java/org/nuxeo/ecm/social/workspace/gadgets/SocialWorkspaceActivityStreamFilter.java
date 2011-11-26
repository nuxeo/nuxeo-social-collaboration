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

package org.nuxeo.ecm.social.workspace.gadgets;

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
import org.nuxeo.ecm.activity.ActivityStreamFilter;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.activity.ActivityStreamServiceImpl;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.social.relationship.RelationshipKind;
import org.nuxeo.ecm.social.relationship.service.RelationshipService;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class SocialWorkspaceActivityStreamFilter implements
        ActivityStreamFilter {

    public static final String ID = "SocialWorkspaceActivityStreamFilter";

    public static final String REPOSITORY_NAME_PARAMETER = "repositoryName";

    public static final String SOCIAL_WORKSPACE_ID_PARAMETER = "socialWorkspaceId";

    public static final String SOCIAL_WORKSPACE_ACTIVITY_STREAM_NAME = "socialWorkspaceActivityStream";

    private RelationshipService relationshipService;

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

    @Override
    public void handleRemovedActivities(
            ActivityStreamService activityStreamService,
            Collection<Serializable> activityIds) {
        // nothing for now
    }

    @SuppressWarnings("unchecked")
    @Override
    public ActivitiesList query(ActivityStreamService activityStreamService,
            Map<String, Serializable> parameters, long offset, long limit) {
        String repositoryName = (String) parameters.get(REPOSITORY_NAME_PARAMETER);
        if (repositoryName == null) {
            throw new IllegalArgumentException(REPOSITORY_NAME_PARAMETER
                    + " is required");
        }

        String socialWorkspaceId = (String) parameters.get(SOCIAL_WORKSPACE_ID_PARAMETER);
        if (socialWorkspaceId == null) {
            throw new IllegalArgumentException(SOCIAL_WORKSPACE_ID_PARAMETER
                    + " is required");
        }

        EntityManager em = ((ActivityStreamServiceImpl) activityStreamService).getEntityManager();
        Query query;
        String socialWorkspaceActivityObject = ActivityHelper.createDocumentActivityObject(
                repositoryName, socialWorkspaceId);
        List<String> actors = getRelationshipService().getTargetsOfKind(
                socialWorkspaceActivityObject,
                RelationshipKind.fromString("socialworkspace:members"));
        actors.add(socialWorkspaceActivityObject);
        if (actors.isEmpty()) {
            return new ActivitiesListImpl();
        }

        query = em.createQuery("select activity from Activity activity where activity.actor in (:actors) and activity.verb in (:verbs) "
                + "and activity.target = :target order by activity.publishedDate desc");
        query.setParameter("actors", actors);
        query.setParameter("target", socialWorkspaceActivityObject);

        List<String> verbs = activityStreamService.getActivityStream(
                SOCIAL_WORKSPACE_ACTIVITY_STREAM_NAME).getVerbs();
        query.setParameter("verbs", verbs);

        if (limit > 0) {
            query.setMaxResults((int) limit);
            if (offset > 0) {
                query.setFirstResult((int) offset);
            }
        }
        return new ActivitiesListImpl(query.getResultList());
    }

    private RelationshipService getRelationshipService()
            throws ClientRuntimeException {
        if (relationshipService == null) {
            try {
                relationshipService = Framework.getService(RelationshipService.class);
            } catch (Exception e) {
                final String errMsg = "Error connecting to RelationshipService. "
                        + e.getMessage();
                throw new ClientRuntimeException(errMsg, e);
            }
            if (relationshipService == null) {
                throw new ClientRuntimeException(
                        "RelationshipService service not bound");
            }
        }
        return relationshipService;
    }

}
