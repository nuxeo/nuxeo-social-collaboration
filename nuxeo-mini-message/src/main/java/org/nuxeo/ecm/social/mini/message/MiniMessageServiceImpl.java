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

package org.nuxeo.ecm.social.mini.message;

import static org.nuxeo.ecm.social.mini.message.MiniMessageActivityStreamFilter.ACTOR_PARAMETER;
import static org.nuxeo.ecm.social.mini.message.MiniMessageActivityStreamFilter.CONTEXT_PARAMETER;
import static org.nuxeo.ecm.social.mini.message.MiniMessageActivityStreamFilter.MINI_MESSAGE_ID_PARAMETER;
import static org.nuxeo.ecm.social.mini.message.MiniMessageActivityStreamFilter.QUERY_TYPE_PARAMETER;
import static org.nuxeo.ecm.social.mini.message.MiniMessageActivityStreamFilter.QueryType.MINI_MESSAGES_FOR_ACTOR;
import static org.nuxeo.ecm.social.mini.message.MiniMessageActivityStreamFilter.QueryType.MINI_MESSAGES_FROM_ACTOR;
import static org.nuxeo.ecm.social.mini.message.MiniMessageActivityStreamFilter.QueryType.MINI_MESSAGE_BY_ID;
import static org.nuxeo.ecm.social.mini.message.MiniMessageActivityStreamFilter.VERB;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.activity.ActivitiesList;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityBuilder;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.social.relationship.RelationshipKind;
import org.nuxeo.runtime.api.Framework;

/**
 * Default implementation of {@link MiniMessageService}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class MiniMessageServiceImpl implements MiniMessageService {

    @Override
    public MiniMessage addMiniMessage(Principal principal, String message, Date publishedDate) {
        return addMiniMessage(principal, message, publishedDate, null);
    }

    @Override
    public MiniMessage addMiniMessage(Principal principal, String message, Date publishedDate,
            String contextActivityObject) {
        Activity activity = new ActivityBuilder().actor(ActivityHelper.createUserActivityObject(principal)).displayActor(
                ActivityHelper.generateDisplayName(principal)).verb(VERB).object(message).publishedDate(publishedDate).context(
                contextActivityObject).build();
        activity = getActivityStreamService().addActivity(activity);
        return MiniMessage.fromActivity(activity);
    }

    @Override
    public MiniMessage addMiniMessage(Principal principal, String message) {
        return addMiniMessage(principal, message, new Date());
    }

    @Override
    public void removeMiniMessage(MiniMessage miniMessage) {
        ActivityStreamService activityStreamService = getActivityStreamService();
        Activity activity = activityStreamService.getActivity(miniMessage.getId());
        activityStreamService.removeActivities(Collections.singleton(activity));
    }

    @Override
    public MiniMessage getMiniMessage(Serializable miniMessageId) {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_TYPE_PARAMETER, MINI_MESSAGE_BY_ID);
        parameters.put(MINI_MESSAGE_ID_PARAMETER, miniMessageId);
        List<Activity> activities = getActivityStreamService().query(MiniMessageActivityStreamFilter.ID, parameters);
        return activities.isEmpty() ? null : MiniMessage.fromActivity(activities.get(0));
    }

    @Override
    public List<MiniMessage> getMiniMessageFor(String actorActivityObject, RelationshipKind relationshipKind,
            long offset, long limit) {
        return getMiniMessageFor(actorActivityObject, relationshipKind, null, offset, limit);
    }

    @Override
    public List<MiniMessage> getMiniMessageFor(String actorActivityObject, RelationshipKind relationshipKind,
            String contextActivityObject, long offset, long limit) {
        List<Activity> activities = getMiniMessageActivitiesFor(actorActivityObject, relationshipKind,
                contextActivityObject, offset, limit);
        List<MiniMessage> miniMessages = new ArrayList<MiniMessage>();
        for (Activity activity : activities) {
            miniMessages.add(MiniMessage.fromActivity(activity));
        }
        return miniMessages;
    }

    @Override
    public List<MiniMessage> getMiniMessageFrom(String actorActivityObject, long offset, long limit) {
        List<Activity> activities = getMiniMessageActivitiesFrom(actorActivityObject, offset, limit);
        List<MiniMessage> miniMessages = new ArrayList<MiniMessage>();
        for (Activity activity : activities) {
            miniMessages.add(MiniMessage.fromActivity(activity));
        }
        return miniMessages;
    }

    @Override
    public ActivitiesList getMiniMessageActivitiesFor(String actorActivityObject, RelationshipKind relationshipKind,
            long offset, long limit) {
        return getMiniMessageActivitiesFor(actorActivityObject, relationshipKind, null, offset, limit);
    }

    @Override
    public ActivitiesList getMiniMessageActivitiesFor(String actorActivityObject, RelationshipKind relationshipKind,
            String contextActivityObject, long offset, long limit) {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(ACTOR_PARAMETER, actorActivityObject);
        parameters.put(QUERY_TYPE_PARAMETER, MINI_MESSAGES_FOR_ACTOR);
        parameters.put(CONTEXT_PARAMETER, contextActivityObject);
        return getActivityStreamService().query(MiniMessageActivityStreamFilter.ID, parameters, offset, limit);
    }

    @Override
    public ActivitiesList getMiniMessageActivitiesFrom(String actorActivityObject, long offset, long limit) {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(ACTOR_PARAMETER, actorActivityObject);
        parameters.put(QUERY_TYPE_PARAMETER, MINI_MESSAGES_FROM_ACTOR);
        return getActivityStreamService().query(MiniMessageActivityStreamFilter.ID, parameters, offset, limit);
    }

    public ActivityStreamService getActivityStreamService() {
        return Framework.getLocalService(ActivityStreamService.class);
    }

}
