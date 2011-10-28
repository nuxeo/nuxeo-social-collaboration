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
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class DummyActivityStreamFilter implements ActivityStreamFilter {

    public static final String ID = "DummyActivityStreamFilter";

    protected Activity lastActivity;

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
        lastActivity = activity;
    }

    @Override
    public void handleRemovedActivities(
            ActivityStreamService activityStreamService,
            Collection<Serializable> activityIds) {
        // nothing
    }

    @Override
    public ActivitiesList query(ActivityStreamService activityStreamService,
            Map<String, Serializable> parameters, long offset, long limit) {
        ActivitiesList list = new ActivitiesListImpl();
        list.add(lastActivity);
        return list;
    }

}
