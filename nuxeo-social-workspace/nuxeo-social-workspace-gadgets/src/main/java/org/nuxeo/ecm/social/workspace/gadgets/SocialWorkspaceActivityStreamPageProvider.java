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

import static org.nuxeo.ecm.social.workspace.gadgets.SocialWorkspaceActivityStreamFilter.REPOSITORY_NAME_PARAMETER;
import static org.nuxeo.ecm.social.workspace.gadgets.SocialWorkspaceActivityStreamFilter.SOCIAL_WORKSPACE_ID_PARAMETER;

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
 * Page provider listing activity messages for a given social workspace
 * <p>
 * This page provider requires four properties:
 * <ul>
 * <li>the social workspace ID</li>
 * <li>the repository name</li>
 * <li>the CoreSession used to filter the Activities</li>
 * <li>the locale to internationalize the activity messages</li>
 * </ul>
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class SocialWorkspaceActivityStreamPageProvider extends
        AbstractActivityPageProvider<ActivityMessage> {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(SocialWorkspaceActivityStreamPageProvider.class);

    public static final String SOCIAL_WORKSPACE_ID_PROPERTY = "socialWorkspaceId";

    public static final String REPOSITORY_NAME_PROPERTY = "repositoryName";

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
            parameters.put(REPOSITORY_NAME_PARAMETER, getRepositoryName());
            parameters.put(SOCIAL_WORKSPACE_ID_PARAMETER,
                    getSocialWorkspaceId());

            ActivitiesList activities = activityStreamService.query(
                    SocialWorkspaceActivityStreamFilter.ID, parameters,
                    getCurrentPageOffset(), pageSize);
            nextOffset = offset + activities.size();
            activities = activities.filterActivities(getCoreSession());
            pageActivityMessages.addAll(activities.toActivityMessages(getLocale()));

            setResultsCount(UNKNOWN_SIZE_AFTER_QUERY);
        }
        return pageActivityMessages;
    }

    protected String getSocialWorkspaceId() {
        Map<String, Serializable> props = getProperties();
        String socialWorkspaceId = (String) props.get(SOCIAL_WORKSPACE_ID_PROPERTY);
        if (socialWorkspaceId == null) {
            throw new ClientRuntimeException("Cannot find "
                    + SOCIAL_WORKSPACE_ID_PROPERTY + " property.");
        }
        return socialWorkspaceId;
    }

    protected String getRepositoryName() {
        Map<String, Serializable> props = getProperties();
        String repositoryName = (String) props.get(REPOSITORY_NAME_PROPERTY);
        if (repositoryName == null) {
            throw new ClientRuntimeException("Cannot find "
                    + REPOSITORY_NAME_PROPERTY + " property.");
        }
        return repositoryName;
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
