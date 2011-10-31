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

/**
 * Filter called by the {@code ActivityStreamService} to store and filter
 * activities for specific use cases.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public interface ActivityStreamFilter {

    /**
     * Returns the id of this {@code ActivityStreamFilter}.
     */
    String getId();

    /**
     * Returns {@code true} if this {@code ActivityStreamFilter} is interested
     * in the given {@code activity}, {@code false} otherwise.
     */
    boolean isInterestedIn(Activity activity);

    /**
     * Called by the {@code ActivityStreamService} when a new {@code Activity}
     * is stored.
     * <p>
     * The given {@code activity} must not be modified.
     */
    void handleNewActivity(ActivityStreamService activityStreamService,
            Activity activity);

    /**
     * Called by the {@code ActivityStreamService} before removing the
     * activities referenced by the given {@code activityIds}.
     */
    void handleRemovedActivities(ActivityStreamService activityStreamService,
            Collection<Serializable> activityIds);

    /**
     * Returns the list of activities filtered by the given parameters.
     *
     * @param activityStreamService the main {@code ActivityStreamService}
     * @param parameters this query parameters.
     * @param offset the offset (starting at 0) into the list of activities.
     * @param limit the maximum number of activities to retrieve, or 0 for all
     *            of them.
     */
    ActivitiesList query(ActivityStreamService activityStreamService,
            Map<String, Serializable> parameters, long offset, long limit);

}
