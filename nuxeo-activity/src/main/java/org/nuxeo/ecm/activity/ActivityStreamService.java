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
import java.util.Locale;
import java.util.Map;

/**
 * Service storing and querying activities.
 * <p>
 * It also uses contributed {@link ActivityStreamFilter}s to store and filter
 * activities for specific use cases.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public interface ActivityStreamService {

    /**
     * To be used as {@code filterId}
     */
    String ALL_ACTIVITIES = "allActivities";

    /**
     * Add and store a new {@code Activity}.
     */
    Activity addActivity(Activity activity);

    /**
     * Remove the Activities referenced by the given {@code activityIds}.
     */
    void removeActivities(Collection<Serializable> activityIds);

    /**
     * Returns the list of activities filtered by the given parameters using the
     * {@code ActivityStreamFilter} referenced by {@code filterId}.
     *
     * @param filterId the id of the {@code ActivityStreamFilter} to use.
     * @param parameters this query parameters.
     * @param offset the offset (starting at 0) into the list of activities.
     * @param limit the maximum number of activities to retrieve, or 0 for all
     *            of them.
     *
     * @throws org.nuxeo.ecm.core.api.ClientRuntimeException if there is no
     *             {@code ActivityStreamFilter} matching the given
     *             {@code filterId}.
     */
    ActivitiesList query(String filterId, Map<String, Serializable> parameters,
            long offset, long limit);

    /**
     * Returns the list of activities filtered by the given parameters using the
     * {@code ActivityStreamFilter} referenced by {@code filterId}.
     *
     * @param filterId the id of the {@code ActivityStreamFilter} to use.
     * @param parameters this query parameters.
     *
     * @throws org.nuxeo.ecm.core.api.ClientRuntimeException if there is no
     *             {@code ActivityStreamFilter} matching the given
     *             {@code filterId}.
     */
    ActivitiesList query(String filterId, Map<String, Serializable> parameters);

    /**
     * Computes an {@link ActivityMessage} from the given {@code activity} and
     * {@code locale}.
     */
    ActivityMessage toActivityMessage(Activity activity, Locale locale);

    /**
     * Returns the {@link ActivityStream} with the given {@code name},
     * {@code null} if it does not exist.
     */
    ActivityStream getActivityStream(String name);

}
