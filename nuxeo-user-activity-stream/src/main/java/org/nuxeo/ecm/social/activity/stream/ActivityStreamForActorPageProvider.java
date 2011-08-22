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

import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamFilter.ACTOR_PARAMETER;
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamFilter.QUERY_TYPE_PARAMETER;
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamFilter.QueryType.ACTIVITY_STREAM_FOR_ACTOR;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityMessage;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.platform.query.api.AbstractPageProvider;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class ActivityStreamForActorPageProvider extends
        AbstractPageProvider<ActivityMessage> {

    private static final long serialVersionUID = 1L;

    public static final String ACTOR_PROPERTY = "actor";

    public static final String LOCALE_PROPERTY = "locale";

    protected ActivityStreamService activityStreamService;

    protected List<ActivityMessage> pageActivityMessages;

    @Override
    public List<ActivityMessage> getCurrentPage() {
        if (pageActivityMessages == null) {
            pageActivityMessages = new ArrayList<ActivityMessage>();
            long pageSize = getMinMaxPageSize();
            Map<String, Serializable> parameters = new HashMap<String, Serializable>();
            parameters.put(ACTOR_PARAMETER, getActor());
            parameters.put(QUERY_TYPE_PARAMETER, ACTIVITY_STREAM_FOR_ACTOR);
            List<Activity> activities = getActivityStreamService().query(
                    UserActivityStreamFilter.ID, parameters, (int) pageSize,
                    (int) getCurrentPageIndex());
            pageActivityMessages.addAll(toActivityMessages(activities));
            resultsCount = Integer.MAX_VALUE - 1;
        }
        return pageActivityMessages;
    }

    protected List<ActivityMessage> toActivityMessages(List<Activity> activities) {
        List<ActivityMessage> messages = new ArrayList<ActivityMessage>();
        for (Activity activity : activities) {
            ActivityMessage activityMessage = new ActivityMessage(
                    getActivityStreamService().toFormattedMessage(activity,
                            getLocale()), activity.getPublishedDate());
            messages.add(activityMessage);
        }
        return messages;
    }

    protected ActivityStreamService getActivityStreamService()
            throws ClientRuntimeException {
        if (activityStreamService == null) {
            try {
                activityStreamService = Framework.getService(ActivityStreamService.class);
            } catch (Exception e) {
                final String errMsg = "Error connecting to ActivityStreamService. "
                        + e.getMessage();
                throw new ClientRuntimeException(errMsg, e);
            }
            if (activityStreamService == null) {
                throw new ClientRuntimeException(
                        "ActivityStreamService service not bound");
            }
        }
        return activityStreamService;
    }

    protected String getActor() {
        Map<String, Serializable> props = getProperties();
        String actor = (String) props.get(ACTOR_PROPERTY);
        if (actor == null) {
            throw new ClientRuntimeException("Cannot find " + ACTOR_PROPERTY
                    + " property.");
        }
        return actor;
    }

    protected Locale getLocale() {
        Map<String, Serializable> props = getProperties();
        return (Locale) props.get(LOCALE_PROPERTY);
    }

    @Override
    public boolean isSortable() {
        return false;
    }

    @Override
    protected void pageChanged() {
        super.pageChanged();
        pageActivityMessages = null;
    }

    @Override
    public void refresh() {
        super.refresh();
        pageActivityMessages = null;
    }

}
