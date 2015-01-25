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

package org.nuxeo.ecm.wall.operations;

import static org.nuxeo.ecm.activity.ActivityHelper.getUsername;
import static org.nuxeo.ecm.wall.WallActivityStreamPageProvider.ACTIVITY_LINK_BUILDER_NAME_PROPERTY;
import static org.nuxeo.ecm.wall.WallActivityStreamPageProvider.ACTIVITY_STREAM_NAME_PROPERTY;
import static org.nuxeo.ecm.wall.WallActivityStreamPageProvider.CONTEXT_DOCUMENT_PROPERTY;
import static org.nuxeo.ecm.wall.WallActivityStreamPageProvider.CORE_SESSION_PROPERTY;
import static org.nuxeo.ecm.wall.WallActivityStreamPageProvider.LOCALE_PROPERTY;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityMessage;
import org.nuxeo.ecm.activity.ActivityReplyMessage;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderService;
import org.nuxeo.ecm.rating.api.LikeService;
import org.nuxeo.ecm.rating.api.LikeStatus;
import org.nuxeo.ecm.wall.WallActivityStreamPageProvider;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
@Operation(id = GetWallActivityStream.ID, category = Constants.CAT_SERVICES, label = "Get a wall activity stream", description = "Get a wall activity stream for the given document.")
public class GetWallActivityStream {

    public static final String ID = "Services.GetWallActivityStream";

    public static final String PROVIDER_NAME = "wall_activity_stream";

    @Context
    protected CoreSession session;

    @Context
    protected PageProviderService pageProviderService;

    @Context
    protected LikeService likeService;

    @Param(name = "activityStreamName", required = false)
    protected String activityStreamName;

    @Param(name = "activityLinkBuilder", required = false)
    protected String activityLinkBuilder;

    @Param(name = "contextPath", required = true)
    protected String contextPath;

    @Param(name = "language", required = false)
    protected String language;

    @Param(name = "offset", required = false)
    protected Integer offset;

    @Param(name = "limit", required = false)
    protected Integer limit;

    @OperationMethod
    public Blob run() throws Exception {
        Long targetOffset = 0L;
        if (offset != null) {
            targetOffset = offset.longValue();
        }
        Long targetLimit = null;
        if (limit != null) {
            targetLimit = limit.longValue();
        }

        DocumentModel doc = session.getDocument(new PathRef(contextPath));
        if (StringUtils.isBlank(activityStreamName)) {
            // assume we are on a Social workspace
            activityStreamName = "socialWorkspaceWallActivityStream";
        }

        Locale locale = language != null && !language.isEmpty() ? new Locale(language) : Locale.ENGLISH;

        Map<String, Serializable> props = new HashMap<String, Serializable>();
        props.put(ACTIVITY_STREAM_NAME_PROPERTY, activityStreamName);
        props.put(ACTIVITY_LINK_BUILDER_NAME_PROPERTY, activityLinkBuilder);
        props.put(CONTEXT_DOCUMENT_PROPERTY, doc);
        props.put(LOCALE_PROPERTY, locale);
        props.put(CORE_SESSION_PROPERTY, (Serializable) session);
        @SuppressWarnings("unchecked")
        PageProvider<ActivityMessage> pageProvider = (PageProvider<ActivityMessage>) pageProviderService.getPageProvider(
                PROVIDER_NAME, null, targetLimit, 0L, props);
        pageProvider.setCurrentPageOffset(targetOffset);

        List<ActivityMessage> activityMessages = pageProvider.getCurrentPage();
        List<Map<String, Object>> activitiesJSON = new ArrayList<Map<String, Object>>();
        for (ActivityMessage activityMessage : activityMessages) {
            Map<String, Object> o = activityMessage.toMap(session, locale, activityLinkBuilder);
            o.put("replies", toActivityReplyMessagesJSON(session, locale, activityMessage.getActivityReplyMessages()));
            if (activityMessage.getVerb().equals("minimessage")) {
                o.put("allowDeletion", getAllowDeletion(activityMessage.getActor()));
            }
            o.put("likeStatus", getLikeStatus(activityMessage.getActivityId()));
            activitiesJSON.add(o);
        }

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("offset", ((WallActivityStreamPageProvider) pageProvider).getNextOffset());
        m.put("limit", pageProvider.getPageSize());
        m.put("activities", activitiesJSON);

        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, m);

        return Blobs.createBlob(writer.toString(), "application/json");
    }

    private List<Map<String, Object>> toActivityReplyMessagesJSON(CoreSession session, Locale locale,
            List<ActivityReplyMessage> activityReplyMessages) throws ClientException {
        List<Map<String, Object>> replies = new ArrayList<Map<String, Object>>();
        for (ActivityReplyMessage activityReplyMessage : activityReplyMessages) {
            Map<String, Object> o = activityReplyMessage.toMap(session, locale, activityLinkBuilder);
            o.put("allowDeletion", getAllowDeletion(activityReplyMessage.getActor()));
            o.put("likeStatus", getLikeStatus(activityReplyMessage.getActivityReplyId()));
            replies.add(o);
        }
        return replies;
    }

    private boolean getAllowDeletion(String actor) {
        String actorUsername = getUsername(actor);
        return session.getPrincipal().getName().equals(actorUsername);
    }

    private Map<String, Serializable> getLikeStatus(Serializable id) {
        String activityObject = ActivityHelper.createActivityObject(id);
        LikeStatus likeStatus = likeService.getLikeStatus(session.getPrincipal().getName(), activityObject);
        return likeStatus.toMap();
    }

}
