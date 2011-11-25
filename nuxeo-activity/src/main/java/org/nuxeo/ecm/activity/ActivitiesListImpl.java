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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.nuxeo.common.utils.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.IterableQueryResult;
import org.nuxeo.runtime.api.Framework;

/**
 * Default implementation of {@link ActivitiesList}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class ActivitiesListImpl extends ArrayList<Activity> implements
        ActivitiesList {

    private static final long serialVersionUID = 1L;

    public ActivitiesListImpl() {
        super();
    }

    public ActivitiesListImpl(Collection<? extends Activity> c) {
        super(c);
    }

    @Override
    public ActivitiesList filterActivities(CoreSession session) {
        ActivitiesList filteredActivities = new ActivitiesListImpl(this);

        Map<String, List<Activity>> activitiesByDocument = getActivitiesByDocument();

        List<String> authorizedDocuments = filterAuthorizedDocuments(
                activitiesByDocument.keySet(), session);
        // remove all activities the user has access to
        for (String authorizedDocument : authorizedDocuments) {
            activitiesByDocument.remove(authorizedDocument);
        }

        // extract all unauthorized activities
        List<Activity> unauthorizedActivities = new ArrayList<Activity>();
        for (List<Activity> activities : activitiesByDocument.values()) {
            unauthorizedActivities.addAll(activities);
        }

        // remove all unauthorized activities
        filteredActivities.removeAll(unauthorizedActivities);
        return filteredActivities;
    }

    protected Map<String, List<Activity>> getActivitiesByDocument() {
        Map<String, List<Activity>> activitiesByDocuments = new HashMap<String, List<Activity>>();
        for (Activity activity : this) {
            List<String> relatedDocuments = getRelatedDocuments(activity);
            for (String doc : relatedDocuments) {
                List<Activity> value = activitiesByDocuments.get(doc);
                if (value == null) {
                    value = new ArrayList<Activity>();
                    activitiesByDocuments.put(doc, value);
                }
                value.add(activity);
            }
        }
        return activitiesByDocuments;
    }

    protected List<String> getRelatedDocuments(Activity activity) {
        List<String> relatedDocuments = new ArrayList<String>();

        String activityObject = activity.getActor();
        if (activityObject != null && ActivityHelper.isDocument(activityObject)) {
            relatedDocuments.add(ActivityHelper.getDocumentId(activityObject));
        }
        activityObject = activity.getObject();
        if (activityObject != null && ActivityHelper.isDocument(activityObject)) {
            relatedDocuments.add(ActivityHelper.getDocumentId(activityObject));
        }
        activityObject = activity.getTarget();
        if (activityObject != null && ActivityHelper.isDocument(activityObject)) {
            relatedDocuments.add(ActivityHelper.getDocumentId(activityObject));
        }

        return relatedDocuments;
    }

    protected List<String> filterAuthorizedDocuments(Set<String> allDocuments,
            CoreSession session) {
        try {
            String idsParam = "('"
                    + StringUtils.join(
                            allDocuments.toArray(new String[allDocuments.size()]),
                            "', '") + "')";
            String query = String.format(
                    "SELECT ecm:uuid FROM Document WHERE ecm:uuid IN %s",
                    idsParam);
            IterableQueryResult res = session.queryAndFetch(query, "NXQL");

            try {
                List<String> authorizedDocuments = new ArrayList<String>();
                for (Map<String, Serializable> map : res) {
                    authorizedDocuments.add((String) map.get("ecm:uuid"));
                }
                return authorizedDocuments;
            } finally {
                if (res != null) {
                    res.close();
                }
            }
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @Override
    public List<ActivityMessage> toActivityMessages(Locale locale) {
        ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
        List<ActivityMessage> messages = new ArrayList<ActivityMessage>();
        for (Activity activity : this) {
            messages.add(activityStreamService.toActivityMessage(activity,
                    locale));
        }
        return messages;
    }

}
