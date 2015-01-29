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

import static org.nuxeo.ecm.social.workspace.gadgets.SocialWorkspaceActivityStreamPageProvider.ACTIVITY_LINK_BUILDER_NAME_PROPERTY;
import static org.nuxeo.ecm.social.workspace.gadgets.SocialWorkspaceActivityStreamPageProvider.CORE_SESSION_PROPERTY;
import static org.nuxeo.ecm.social.workspace.gadgets.SocialWorkspaceActivityStreamPageProvider.LOCALE_PROPERTY;
import static org.nuxeo.ecm.social.workspace.gadgets.SocialWorkspaceActivityStreamPageProvider.REPOSITORY_NAME_PROPERTY;
import static org.nuxeo.ecm.social.workspace.gadgets.SocialWorkspaceActivityStreamPageProvider.SOCIAL_WORKSPACE_ID_PROPERTY;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.nuxeo.ecm.activity.ActivityMessage;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.impl.blob.InputStreamBlob;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderService;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;

/**
 * Operation to get the activity stream for a given Social Workspace.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
@Operation(id = GetSocialWorkspaceActivityStream.ID, category = Constants.CAT_SERVICES, label = "Get a social workspace activity stream", description = "Get a social workspace activity stream.")
public class GetSocialWorkspaceActivityStream {

    public static final String ID = "Services.GetSocialWorkspaceActivityStream";

    public static final String PROVIDER_NAME = "social_workspace_activity_stream";

    @Context
    protected CoreSession session;

    @Context
    protected PageProviderService pageProviderService;

    @Context
    protected SocialWorkspaceService socialWorkspaceService;

    @Param(name = "contextPath", required = true)
    protected String contextPath;

    @Param(name = "language", required = false)
    protected String language;

    @Param(name = "activityLinkBuilder", required = true)
    protected String activityLinkBuilder;

    @Param(name = "offset", required = false)
    protected Integer offset;

    @Param(name = "limit", required = false)
    protected Integer limit;

    @SuppressWarnings("unchecked")
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

        SocialWorkspace socialWorkspace = socialWorkspaceService.getDetachedSocialWorkspace(session, new PathRef(
                contextPath));

        Locale locale = language != null && !language.isEmpty() ? new Locale(language) : Locale.ENGLISH;

        Map<String, Serializable> props = new HashMap<String, Serializable>();
        props.put(SOCIAL_WORKSPACE_ID_PROPERTY, socialWorkspace.getId());
        props.put(REPOSITORY_NAME_PROPERTY, socialWorkspace.getDocument().getRepositoryName());
        props.put(LOCALE_PROPERTY, locale);
        props.put(CORE_SESSION_PROPERTY, (Serializable) session);
        props.put(ACTIVITY_LINK_BUILDER_NAME_PROPERTY, activityLinkBuilder);
        PageProvider<ActivityMessage> pageProvider = (PageProvider<ActivityMessage>) pageProviderService.getPageProvider(
                PROVIDER_NAME, null, targetLimit, 0L, props);
        pageProvider.setCurrentPageOffset(targetOffset);

        List<Map<String, Object>> activities = new ArrayList<Map<String, Object>>();
        for (ActivityMessage activityMessage : pageProvider.getCurrentPage()) {
            activities.add(activityMessage.toMap(session, locale, activityLinkBuilder));
        }

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("offset", ((SocialWorkspaceActivityStreamPageProvider) pageProvider).getNextOffset());
        m.put("limit", pageProvider.getPageSize());
        m.put("activities", activities);

        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, m);

        return new InputStreamBlob(new ByteArrayInputStream(writer.toString().getBytes("UTF-8")), "application/json");
    }

}
