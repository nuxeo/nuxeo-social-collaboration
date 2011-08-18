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

package org.nuxeo.ecm.social.activity.stream.operations;

import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamFilter.QUERY_TYPE_PARAMETER;
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamFilter.QueryType.ACTIVITY_STREAM_FOR_ACTOR;
import static org.nuxeo.ecm.social.activity.stream.UserActivityStreamFilter.QueryType.ACTIVITY_STREAM_FROM_ACTOR;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.impl.blob.InputStreamBlob;
import org.nuxeo.ecm.social.activity.stream.UserActivityStreamFilter;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
@Operation(id = GetActivityStreamForActor.ID, category = Constants.CAT_SERVICES, label = "Get activity stream", description = "Get activity stream for the current user.")
public class GetActivityStreamForActor {

    public static final String ID = "Services.GetActivityStreamForActor";

    public static final String FOR_ACTOR_ACTIVITY_STREAM_TYPE = "forActor";

    public static final String FROM_ACTOR_ACTIVITY_STREAM_TYPE = "fromActor";

    @Context
    protected CoreSession session;

    @Context
    protected ActivityStreamService activityStreamService;

    @Param(name = "actor", required = false)
    protected String actor;

    @Param(name = "language", required = false)
    protected String language;

    @Param(name = "activityStreamType", required = false)
    protected String activityStreamType;

    @Param(name = "page", required = false)
    protected Integer page;

    @Param(name = "pageSize", required = false)
    protected Integer pageSize;

    @OperationMethod
    public Blob run() throws Exception {
        if (StringUtils.isBlank(actor)) {
            actor = session.getPrincipal().getName();
        }
        if (StringUtils.isBlank(activityStreamType)) {
            activityStreamType = FOR_ACTOR_ACTIVITY_STREAM_TYPE;
        }

        if (pageSize == null) {
            pageSize = 0;
        }
        if (page == null) {
            page = 0;
        }

        Locale locale = language != null && !language.isEmpty() ? new Locale(
                language) : Locale.ENGLISH;
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM,
                locale);

        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(UserActivityStreamFilter.ACTOR_PARAMETER, actor);
        if (FOR_ACTOR_ACTIVITY_STREAM_TYPE.equals(activityStreamType)) {
            parameters.put(QUERY_TYPE_PARAMETER, ACTIVITY_STREAM_FOR_ACTOR);
        } else if (FROM_ACTOR_ACTIVITY_STREAM_TYPE.equals(activityStreamType)) {
            parameters.put(QUERY_TYPE_PARAMETER, ACTIVITY_STREAM_FROM_ACTOR);
        }
        List<Activity> activities = activityStreamService.query(
                UserActivityStreamFilter.ID, parameters, pageSize, page);

        List<Map<String, Object>> m = new ArrayList<Map<String, Object>>();
        for (Activity activity : activities) {
            Map<String, Object> o = new HashMap<String, Object>();
            o.put("id", activity.getId());
            o.put("activityMessage",
                    activityStreamService.toFormattedMessage(activity, locale));
            o.put("publishedDate",
                    dateFormat.format(activity.getPublishedDate()));
            m.add(o);
        }

        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, m);

        return new InputStreamBlob(new ByteArrayInputStream(
                writer.toString().getBytes("UTF-8")), "application/json");
    }

}
