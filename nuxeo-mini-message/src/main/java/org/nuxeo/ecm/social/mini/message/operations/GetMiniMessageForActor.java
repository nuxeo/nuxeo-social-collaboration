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

package org.nuxeo.ecm.social.mini.message.operations;

import static org.nuxeo.ecm.social.mini.message.MiniMessagePageProvider.ACTOR_PROPERTY;
import static org.nuxeo.ecm.social.mini.message.MiniMessagePageProvider.FOR_ACTOR_STREAM_TYPE;
import static org.nuxeo.ecm.social.mini.message.MiniMessagePageProvider.RELATIONSHIP_KIND_PROPERTY;
import static org.nuxeo.ecm.social.mini.message.MiniMessagePageProvider.STREAM_TYPE_PROPERTY;

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
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.impl.blob.InputStreamBlob;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderService;
import org.nuxeo.ecm.social.mini.message.MiniMessage;

/**
 * Operation to get the mini messages for a given actor.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
@Operation(id = GetMiniMessageForActor.ID, category = Constants.CAT_SERVICES, label = "Get mini messages", description = "Get mini messages for the current user.")
public class GetMiniMessageForActor {

    public static final String ID = "Services.GetMiniMessageForActor";

    public static final String CIRCLE_KIND = "circle";

    @Context
    protected CoreSession session;

    @Context
    protected PageProviderService pageProviderService;

    @Param(name = "actor", required = false)
    protected String actor;

    @Param(name = "relationshipKind", required = false)
    protected String relationshipKind;

    @Param(name = "miniMessagesStreamType", required = false)
    protected String miniMessagesStreamType;

    @Param(name = "language", required = false)
    protected String language;

    @Param(name = "offset", required = false)
    protected Integer offset;

    @Param(name = "limit", required = false)
    protected Integer limit;

    @OperationMethod
    public Blob run() throws Exception {
        if (StringUtils.isBlank(relationshipKind)) {
            relationshipKind = CIRCLE_KIND;
        }

        if (StringUtils.isBlank(actor)) {
            actor = session.getPrincipal().getName();
        }
        if (StringUtils.isBlank(miniMessagesStreamType)) {
            miniMessagesStreamType = FOR_ACTOR_STREAM_TYPE;
        }

        Long targetOffset = 0L;
        if (offset != null) {
            targetOffset = offset.longValue();
        }
        Long targetPageSize = null;
        if (limit != null) {
            targetPageSize = limit.longValue();
        }

        Map<String, Serializable> props = new HashMap<String, Serializable>();
        props.put(STREAM_TYPE_PROPERTY, miniMessagesStreamType);
        props.put(ACTOR_PROPERTY, actor);
        props.put(RELATIONSHIP_KIND_PROPERTY, relationshipKind);

        @SuppressWarnings("unchecked")
        PageProvider<MiniMessage> pageProvider = (PageProvider<MiniMessage>) pageProviderService.getPageProvider(
                "mini_messages", null, targetPageSize, 0L, props);
        pageProvider.setCurrentPageOffset(targetOffset);

        Locale locale = language != null && !language.isEmpty() ? new Locale(
                language) : Locale.ENGLISH;
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM,
                locale);

        List<Map<String, Object>> m = new ArrayList<Map<String, Object>>();
        for (MiniMessage miniMessage : pageProvider.getCurrentPage()) {
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
            m.add(o);
        }

        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, m);

        return new InputStreamBlob(new ByteArrayInputStream(
                writer.toString().getBytes("UTF-8")), "application/json");
    }

}
