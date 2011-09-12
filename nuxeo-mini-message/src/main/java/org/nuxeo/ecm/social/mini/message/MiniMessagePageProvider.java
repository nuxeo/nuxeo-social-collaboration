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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.activity.AbstractActivityPageProvider;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.social.user.relationship.RelationshipKind;
import org.nuxeo.runtime.api.Framework;

/**
 * Page provider listing mini messages for a given actor
 * <p>
 * This page provider requires two properties: the first one to be filled with
 * the actor, and the second one to be filled with the relationship kind to use.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class MiniMessagePageProvider extends
        AbstractActivityPageProvider<MiniMessage> implements
        PageProvider<MiniMessage> {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(MiniMessagePageProvider.class);

    public static final String ACTOR_PROPERTY = "actor";

    public static final String RELATIONSHIP_KIND_PROPERTY = "relationshipKind";

    public static final String STREAM_TYPE_PROPERTY = "streamType";

    public static final String FOR_ACTOR_STREAM_TYPE = "forActor";

    public static final String FROM_ACTOR_STREAM_TYPE = "fromActor";

    protected MiniMessageService miniMessageService;

    protected List<MiniMessage> pageMiniMessages;

    @Override
    public List<MiniMessage> getCurrentPage() {
        if (pageMiniMessages == null) {
            pageMiniMessages = new ArrayList<MiniMessage>();
            long pageSize = getMinMaxPageSize();

            String streamType = getStreamType();
            if (FOR_ACTOR_STREAM_TYPE.equals(streamType)) {
                pageMiniMessages.addAll(getMiniMessageService().getMiniMessageFor(
                        getActor(), getRelationshipKind(),
                        getCurrentPageOffset(), pageSize));
            } else if (FROM_ACTOR_STREAM_TYPE.equals(streamType)) {
                pageMiniMessages.addAll(getMiniMessageService().getMiniMessageFrom(
                        getActor(), getCurrentPageOffset(), pageSize));
            } else {
                log.error("Unknown stream type: " + streamType);
            }
            nextOffset = offset + pageMiniMessages.size();
            setResultsCount(UNKNOWN_SIZE_AFTER_QUERY);
        }
        return pageMiniMessages;
    }

    protected MiniMessageService getMiniMessageService()
            throws ClientRuntimeException {
        if (miniMessageService == null) {
            try {
                miniMessageService = Framework.getService(MiniMessageService.class);
            } catch (Exception e) {
                final String errMsg = "Error connecting to MiniMessageService. "
                        + e.getMessage();
                throw new ClientRuntimeException(errMsg, e);
            }
            if (miniMessageService == null) {
                throw new ClientRuntimeException(
                        "MiniMessageService service not bound");
            }
        }
        return miniMessageService;
    }

    protected String getActor() {
        Map<String, Serializable> props = getProperties();
        String actor = (String) props.get(ACTOR_PROPERTY);
        if (actor == null) {
            throw new ClientRuntimeException("Cannot find " + ACTOR_PROPERTY
                    + " property.");
        }
        return ActivityHelper.createUserActivityObject(actor);
    }

    protected String getStreamType() {
        Map<String, Serializable> props = getProperties();
        String streamType = (String) props.get(STREAM_TYPE_PROPERTY);
        if (streamType == null) {
            streamType = FOR_ACTOR_STREAM_TYPE;
        }
        return streamType;
    }

    protected RelationshipKind getRelationshipKind() {
        Map<String, Serializable> props = getProperties();
        String relationshipKind = (String) props.get(RELATIONSHIP_KIND_PROPERTY);
        if (relationshipKind == null) {
            throw new ClientRuntimeException("Cannot find "
                    + RELATIONSHIP_KIND_PROPERTY + " property.");
        }
        return RelationshipKind.fromString(relationshipKind);
    }

    @Override
    public boolean isSortable() {
        return false;
    }

    @Override
    protected void pageChanged() {
        super.pageChanged();
        pageMiniMessages = null;
    }

    @Override
    public void refresh() {
        super.refresh();
        pageMiniMessages = null;
    }

}
