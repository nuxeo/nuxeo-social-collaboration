/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Thomas Roger (troger@nuxeo.com)
 */

package org.nuxeo.ecm.social.workspace.activity;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.nuxeo.ecm.activity.AbstractActivityUpgrader;
import org.nuxeo.ecm.activity.ActivitiesList;
import org.nuxeo.ecm.activity.ActivitiesListImpl;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityBuilder;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.activity.ActivityStreamServiceImpl;

/**
 * Upgrades the activities for the relationship between an user and a Social
 * Workspace created in Nuxeo 5.5 to match Nuxeo >= 5.6.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.7
 */
public class SocialWorkspaceMembersActivityUpgrader extends
        AbstractActivityUpgrader {

    @Override
    public void doUpgrade(ActivityStreamService activityStreamService) {
        EntityManager em = ((ActivityStreamServiceImpl) activityStreamService).getEntityManager();
        Query query = em.createQuery("select activity from Activity activity where activity.verb = :verb and activity.actor like :actor");
        query.setParameter("verb", "socialworkspace:members");
        query.setParameter("actor", "doc:%");

        @SuppressWarnings("unchecked")
        ActivitiesList activities = new ActivitiesListImpl(
                query.getResultList());
        for (Activity activity : activities) {
            String oldActor = activity.getActor();
            String oldDisplayActor = activity.getDisplayActor();
            String oldObject = activity.getObject();
            String oldDisplayObject = activity.getDisplayObject();
            activity.setActor(oldObject);
            activity.setDisplayActor(oldDisplayObject);
            activity.setObject(oldActor);
            activity.setDisplayObject(oldDisplayActor);
            activity.setTarget(oldActor);
            activity.setDisplayTarget(oldDisplayActor);
            activity.setContext(oldDisplayActor);
            em.merge(activity);

            // Store activity without context
            Activity newActivity = new ActivityBuilder(activity).context(null).build();
            em.persist(newActivity);
        }
    }
}
