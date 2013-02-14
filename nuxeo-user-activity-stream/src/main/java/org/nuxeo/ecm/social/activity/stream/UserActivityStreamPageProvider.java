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
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamFilter.QueryType.ACTIVITY_STREAM_FROM_ACTOR;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.activity.AbstractActivityPageProvider;
import org.nuxeo.ecm.activity.ActivitiesList;
import org.nuxeo.ecm.activity.ActivityMessage;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.runtime.api.Framework;

/**
 * Page provider listing activity messages for a given actor
 * <p>
 * This page provider requires four properties:
 * <ul>
 * <li>the actor</li>
 * <li>the CoreSession used to filter the Activities</li>
 * <li>the user activity stream type: for the actor or from the actor</li>
 * <li>the locale to internationalize the activity messages</li>
 * </ul>
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class UserActivityStreamPageProvider extends
        AbstractActivityPageProvider<ActivityMessage> {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(UserActivityStreamPageProvider.class);

    public static final String ACTOR_PROPERTY = "actor";

    public static final String LOCALE_PROPERTY = "locale";

    public static final String CORE_SESSION_PROPERTY = "coreSession";

    public static final String STREAM_TYPE_PROPERTY = "streamType";

    public static final String ACTIVITY_LINK_BUILDER_NAME_PROPERTY = "activityLinkBuilderName";

    public static final String FOR_ACTOR_STREAM_TYPE = "forActor";

    public static final String FROM_ACTOR_STREAM_TYPE = "fromActor";

    protected List<ActivityMessage> pageActivityMessages;

    @Override
    public List<ActivityMessage> getCurrentPage() {
        if (pageActivityMessages == null) {
            pageActivityMessages = new ArrayList<ActivityMessage>();
            long pageSize = getMinMaxPageSize();

            ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
            String streamType = getStreamType();
            if (FOR_ACTOR_STREAM_TYPE.equals(streamType)) {
                Map<String, Serializable> parameters = new HashMap<String, Serializable>();
                parameters.put(ACTOR_PARAMETER, getActor());
                parameters.put(QUERY_TYPE_PARAMETER, ACTIVITY_STREAM_FOR_ACTOR);
                ActivitiesList activities = activityStreamService.query(
                        UserActivityStreamFilter.ID, parameters,
                        getCurrentPageOffset(), pageSize);
                nextOffset = offset + activities.size();
                activities = activities.filterActivities(getCoreSession());
                pageActivityMessages.addAll(activities.toActivityMessages(
                        getLocale(), getActivityLinkBuilderName()));
            } else if (FROM_ACTOR_STREAM_TYPE.equals(streamType)) {
                Map<String, Serializable> parameters = new HashMap<String, Serializable>();
                parameters.put(ACTOR_PARAMETER, getActor());
                parameters.put(QUERY_TYPE_PARAMETER, ACTIVITY_STREAM_FROM_ACTOR);
                ActivitiesList activities = activityStreamService.query(
                        UserActivityStreamFilter.ID, parameters,
                        getCurrentPageOffset(), pageSize);
                nextOffset = offset + activities.size();
                activities = activities.filterActivities(getCoreSession());
                pageActivityMessages.addAll(activities.toActivityMessages(
                        getLocale(), getActivityLinkBuilderName()));
            } else {
                log.error("Unknown stream type: " + streamType);
            }

            setResultsCount(UNKNOWN_SIZE_AFTER_QUERY);
        }
        return pageActivityMessages;
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
        Locale locale = (Locale) props.get(LOCALE_PROPERTY);
        if (locale == null) {
            throw new ClientRuntimeException("Cannot find " + LOCALE_PROPERTY
                    + " property.");
        }
        return locale;
    }

    protected CoreSession getCoreSession() {
        Map<String, Serializable> props = getProperties();
        CoreSession session = (CoreSession) props.get(CORE_SESSION_PROPERTY);
        if (session == null) {
            throw new ClientRuntimeException("Cannot find "
                    + CORE_SESSION_PROPERTY + " property.");
        }
        return session;
    }

    protected String getStreamType() {
        Map<String, Serializable> props = getProperties();
        String streamType = (String) props.get(STREAM_TYPE_PROPERTY);
        if (streamType == null) {
            streamType = FOR_ACTOR_STREAM_TYPE;
        }
        return streamType;
    }

    protected String getActivityLinkBuilderName() {
        Map<String, Serializable> props = getProperties();
        return (String) props.get(ACTIVITY_LINK_BUILDER_NAME_PROPERTY);
    }

    @Override
    public boolean isSortable() {
        return false;
    }

    @Override
    protected void pageChanged() {
        pageActivityMessages = null;
        super.pageChanged();
    }

    @Override
    public void refresh() {
        pageActivityMessages = null;
        super.refresh();
    }

}
