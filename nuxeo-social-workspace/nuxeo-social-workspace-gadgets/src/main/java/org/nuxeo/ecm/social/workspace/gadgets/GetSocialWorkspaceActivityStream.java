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

import static org.nuxeo.ecm.social.workspace.gadgets.SocialWorkspaceActivityStreamPageProvider.CORE_SESSION_PROPERTY;
import static org.nuxeo.ecm.social.workspace.gadgets.SocialWorkspaceActivityStreamPageProvider.LOCALE_PROPERTY;
import static org.nuxeo.ecm.social.workspace.gadgets.SocialWorkspaceActivityStreamPageProvider.REPOSITORY_NAME_PROPERTY;
import static org.nuxeo.ecm.social.workspace.gadgets.SocialWorkspaceActivityStreamPageProvider.SOCIAL_WORKSPACE_ID_PROPERTY;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.DateFormat;
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
 * @since 5.4.3
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

    @Param(name = "page", required = false)
    protected Integer page;

    @Param(name = "pageSize", required = false)
    protected Integer pageSize;

    @SuppressWarnings("unchecked")
    @OperationMethod
    public Blob run() throws Exception {
        Long targetPage = null;
        if (page != null) {
            targetPage = page.longValue();
        }
        Long targetPageSize = null;
        if (pageSize != null) {
            targetPageSize = pageSize.longValue();
        }

        SocialWorkspace socialWorkspace = socialWorkspaceService.getDetachedSocialWorkspaceContainer(
                session, new PathRef(contextPath));

        Locale locale = language != null && !language.isEmpty() ? new Locale(
                language) : Locale.ENGLISH;

        Map<String, Serializable> props = new HashMap<String, Serializable>();
        props.put(SOCIAL_WORKSPACE_ID_PROPERTY, socialWorkspace.getId());
        props.put(REPOSITORY_NAME_PROPERTY,
                socialWorkspace.getDocument().getRepositoryName());
        props.put(LOCALE_PROPERTY, locale);
        props.put(CORE_SESSION_PROPERTY, (Serializable) session);
        PageProvider<ActivityMessage> pageProvider = (PageProvider<ActivityMessage>) pageProviderService.getPageProvider(
                PROVIDER_NAME, null, targetPageSize, targetPage, props);

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM,
                locale);
        List<Map<String, Object>> m = new ArrayList<Map<String, Object>>();
        for (ActivityMessage activityMessage : pageProvider.getCurrentPage()) {
            Map<String, Object> o = new HashMap<String, Object>();
            o.put("id", activityMessage.getActivityId());
            o.put("activityMessage", activityMessage.getMessage());
            o.put("publishedDate",
                    dateFormat.format(activityMessage.getPublishedDate()));
            m.add(o);
        }

        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, m);

        return new InputStreamBlob(new ByteArrayInputStream(
                writer.toString().getBytes("UTF-8")), "application/json");
    }

}
