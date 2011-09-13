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

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.nuxeo.ecm.activity.ActivityHelper;
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
import org.nuxeo.ecm.social.mini.message.MiniMessage;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;

/**
 * Operation to get the mini messages for a given Social Workspace.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
@Operation(id = GetSocialWorkspaceMiniMessages.ID, category = Constants.CAT_SERVICES, label = "Get social workspace mini messages", description = "Get social workspace mini messages.")
public class GetSocialWorkspaceMiniMessages {

    public static final String ID = "Services.GetSocialWorkspaceMiniMessages";

    public static final String SOCIAL_WORKSPACE_MEMBER_KIND = "socialworkspace:member";

    public static final String PROVIDER_NAME = "social_workspace_mini_messages";

    @Context
    protected CoreSession session;

    @Context
    protected PageProviderService pageProviderService;

    @Context
    protected SocialWorkspaceService socialWorkspaceService;

    @Param(name = "contextPath", required = true)
    protected String contextPath;

    @Param(name = "relationshipKind", required = false)
    protected String relationshipKind;

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

        String kind;
        if (StringUtils.isBlank(relationshipKind)) {
            kind = SOCIAL_WORKSPACE_MEMBER_KIND;
        } else {
            kind = relationshipKind;
        }

        SocialWorkspace socialWorkspace = socialWorkspaceService.getDetachedSocialWorkspaceContainer(
                session, new PathRef(contextPath));

        Map<String, Serializable> props = new HashMap<String, Serializable>();
        props.put(SOCIAL_WORKSPACE_ID_PROPERTY, socialWorkspace.getId());
        props.put(REPOSITORY_NAME_PROPERTY,
                socialWorkspace.getDocument().getRepositoryName());
        props.put(RELATIONSHIP_KIND_PROPERTY, kind);

        @SuppressWarnings("unchecked")
        PageProvider<MiniMessage> pageProvider = (PageProvider<MiniMessage>) pageProviderService.getPageProvider(
                PROVIDER_NAME, null, targetLimit, 0L, props);
        pageProvider.setCurrentPageOffset(targetOffset);

        String json = toJSON(pageProvider, locale, session);
        return new InputStreamBlob(new ByteArrayInputStream(
                json.getBytes("UTF-8")), "application/json");
    }

}
