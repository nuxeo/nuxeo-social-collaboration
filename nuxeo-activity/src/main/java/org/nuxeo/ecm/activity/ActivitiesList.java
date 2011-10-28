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

package org.nuxeo.ecm.activity;

import java.util.List;
import java.util.Locale;

import org.nuxeo.ecm.core.api.CoreSession;

/**
 * A list of Activities with useful methods to filter it or transform it.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public interface ActivitiesList extends List<Activity> {

    /**
     * Returns a filtered {@code ActivitiesList} based on the given
     * {@code session}.
     * <p>
     * All the activities related to documents the user has no read access will
     * be filter out.
     */
    ActivitiesList filterActivities(CoreSession session);

    /**
     * Transforms this {@code ActivitiesList} into a list of
     * {@code ActivityMessage}, internationalized with the given {@code locale}.
     */
    List<ActivityMessage> toActivityMessages(Locale locale);

}
