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
import java.util.List;
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
import org.nuxeo.ecm.social.mini.message.MiniMessage;
import org.nuxeo.ecm.social.mini.message.MiniMessageService;
import org.nuxeo.ecm.social.user.relationship.RelationshipKind;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
@Operation(id = GetSocialWorkspaceMiniMessages.ID, category = Constants.CAT_SERVICES, label = "Get social workspace mini messages", description = "Get social workspace mini messages.")
public class GetSocialWorkspaceMiniMessages {

    public static final String ID = "Services.GetSocialWorkspaceMiniMessages";

    public static final RelationshipKind SOCIAL_WORKSPACE_MEMBER_KIND = RelationshipKind.fromGroup("socialworkspace:member");

    @Context
    protected CoreSession session;

    @Context
    protected SocialWorkspaceService socialWorkspaceService;

    @Context
    protected MiniMessageService miniMessageService;

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
        if (limit == null) {
            limit = 0;
        }
        if (offset == null) {
            offset = 0;
        }

        RelationshipKind kind;
        if (StringUtils.isBlank(relationshipKind)) {
            kind = SOCIAL_WORKSPACE_MEMBER_KIND;
        } else {
            kind = RelationshipKind.fromString(relationshipKind);
        }

        SocialWorkspace socialWorkspace = socialWorkspaceService.getDetachedSocialWorkspaceContainer(
                session, new PathRef(contextPath));

        Locale locale = language != null && !language.isEmpty() ? new Locale(
                language) : Locale.ENGLISH;
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM,
                locale);

        String socialWorkspaceActivityObject = ActivityHelper.createDocumentActivityObject(
                socialWorkspace.getDocument().getRepositoryName(),
                socialWorkspace.getId());

        List<Map<String, Object>> miniMessages = new ArrayList<Map<String, Object>>();
        for (MiniMessage miniMessage : miniMessageService.getMiniMessageFor(
                socialWorkspaceActivityObject, kind, offset, limit)) {
            Map<String, Object> o = new HashMap<String, Object>();
            o.put("id", miniMessage.getId());
            o.put("actor", miniMessage.getActor());
            o.put("displayActor", miniMessage.getDisplayActor());
            o.put("message", miniMessage.getMessage());
            o.put("publishedDate",
                    dateFormat.format(miniMessage.getPublishedDate()));
            o.put("isCurrentUserMiniMessage",
                    session.getPrincipal().getName().equals(
                            miniMessage.getActor()));
            miniMessages.add(o);
        }

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("offset", offset + miniMessages.size());
        m.put("limit", limit);
        m.put("miniMessages", miniMessages);

        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, m);

        return new InputStreamBlob(new ByteArrayInputStream(
                writer.toString().getBytes("UTF-8")), "application/json");
    }

}
