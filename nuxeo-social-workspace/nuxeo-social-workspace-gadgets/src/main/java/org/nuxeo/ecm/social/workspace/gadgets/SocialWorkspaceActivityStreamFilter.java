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

import static org.nuxeo.ecm.social.workspace.SocialConstants.DOCUMENT_CREATED_IN_SOCIAL_WORKSPACE_VERB;
import static org.nuxeo.ecm.social.workspace.SocialConstants.DOCUMENT_UPDATED_IN_SOCIAL_WORKSPACE_VERB;
import static org.nuxeo.ecm.social.workspace.SocialConstants.MAKE_DOCUMENT_PUBLIC_VERB;

import java.io.Serializable;
import java.util.Arrays;
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
import org.nuxeo.ecm.social.user.relationship.RelationshipKind;
import org.nuxeo.ecm.social.user.relationship.service.UserRelationshipService;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class SocialWorkspaceActivityStreamFilter implements
        ActivityStreamFilter {

    public static final String ID = "SocialWorkspaceActivityStreamFilter";

    public static final String REPOSITORY_NAME_PARAMETER = "repositoryName";

    public static final String SOCIAL_WORKSPACE_ID_PARAMETER = "socialWorkspaceId";

    public static final String[] VERBS = new String[] {
            MAKE_DOCUMENT_PUBLIC_VERB,
            DOCUMENT_CREATED_IN_SOCIAL_WORKSPACE_VERB,
            DOCUMENT_UPDATED_IN_SOCIAL_WORKSPACE_VERB,
            "socialworkspace:members" };

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
        List<String> actors = getUserRelationshipService().getTargetsOfKind(
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
        query.setParameter("verbs", Arrays.asList(VERBS));

        if (limit > 0) {
            query.setMaxResults((int) limit);
            if (offset > 0) {
                query.setFirstResult((int) offset);
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
