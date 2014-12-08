/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
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

package org.nuxeo.ecm.wall;

import static org.nuxeo.ecm.wall.WallActivityStreamFilter.ACTIVITY_STREAM_PARAMETER;
import static org.nuxeo.ecm.wall.WallActivityStreamFilter.CONTEXT_DOCUMENT_PARAMETER;

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
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public class WallActivityStreamPageProvider extends AbstractActivityPageProvider<ActivityMessage> {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(WallActivityStreamPageProvider.class);

    public static final String ACTIVITY_STREAM_NAME_PROPERTY = "activityStreamName";

    public static final String ACTIVITY_LINK_BUILDER_NAME_PROPERTY = "activityLinkBuilderName";

    public static final String CONTEXT_DOCUMENT_PROPERTY = "contextDocument";

    public static final String LOCALE_PROPERTY = "locale";

    public static final String CORE_SESSION_PROPERTY = "coreSession";

    protected List<ActivityMessage> pageActivityMessages;

    @Override
    public List<ActivityMessage> getCurrentPage() {
        if (pageActivityMessages == null) {
            pageActivityMessages = new ArrayList<ActivityMessage>();
            long pageSize = getMinMaxPageSize();

            ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
            Map<String, Serializable> parameters = new HashMap<String, Serializable>();
            parameters.put(ACTIVITY_STREAM_PARAMETER, getActivityStreamName());
            parameters.put(CONTEXT_DOCUMENT_PARAMETER, getContextDocument());
            ActivitiesList activities = activityStreamService.query(WallActivityStreamFilter.ID, parameters,
                    getCurrentPageOffset(), pageSize);
            nextOffset = offset + activities.size();
            activities = activities.filterActivities(getCoreSession());
            pageActivityMessages.addAll(activities.toActivityMessages(getLocale(), getActivityLinkBuilderName()));
            setResultsCount(UNKNOWN_SIZE_AFTER_QUERY);
        }
        return pageActivityMessages;
    }

    protected String getActivityStreamName() {
        Map<String, Serializable> props = getProperties();
        String activityStreamName = (String) props.get(ACTIVITY_STREAM_NAME_PROPERTY);
        if (activityStreamName == null) {
            throw new ClientRuntimeException("Cannot find " + ACTIVITY_STREAM_NAME_PROPERTY + " property.");
        }
        return activityStreamName;
    }

    protected DocumentModel getContextDocument() {
        Map<String, Serializable> props = getProperties();
        DocumentModel contextDocument = (DocumentModel) props.get(CONTEXT_DOCUMENT_PROPERTY);
        if (contextDocument == null) {
            throw new ClientRuntimeException("Cannot find " + CONTEXT_DOCUMENT_PROPERTY + " property.");
        }
        return contextDocument;
    }

    protected Locale getLocale() {
        Map<String, Serializable> props = getProperties();
        Locale locale = (Locale) props.get(LOCALE_PROPERTY);
        if (locale == null) {
            throw new ClientRuntimeException("Cannot find " + LOCALE_PROPERTY + " property.");
        }
        return locale;
    }

    protected String getActivityLinkBuilderName() {
        Map<String, Serializable> props = getProperties();
        return (String) props.get(ACTIVITY_LINK_BUILDER_NAME_PROPERTY);
    }

    protected CoreSession getCoreSession() {
        Map<String, Serializable> props = getProperties();
        CoreSession session = (CoreSession) props.get(CORE_SESSION_PROPERTY);
        if (session == null) {
            throw new ClientRuntimeException("Cannot find " + CORE_SESSION_PROPERTY + " property.");
        }
        return session;
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
