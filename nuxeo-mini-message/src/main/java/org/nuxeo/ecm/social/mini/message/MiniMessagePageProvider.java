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
import org.nuxeo.ecm.social.relationship.RelationshipKind;
import org.nuxeo.runtime.api.Framework;

/**
 * Page provider listing mini messages for a given actor
 * <p>
 * This page provider requires two properties: the first one to be filled with the actor, and the second one to be
 * filled with the relationship kind to use.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class MiniMessagePageProvider extends AbstractMiniMessagePageProvider<MiniMessage> implements
        PageProvider<MiniMessage> {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(MiniMessagePageProvider.class);

    @Override
    public List<MiniMessage> getCurrentPage() {
        if (pageMiniMessages == null) {
            pageMiniMessages = new ArrayList<MiniMessage>();
            long pageSize = getMinMaxPageSize();

            MiniMessageService miniMessageService = Framework.getLocalService(MiniMessageService.class);
            String streamType = getStreamType();
            if (FOR_ACTOR_STREAM_TYPE.equals(streamType)) {
                pageMiniMessages.addAll(miniMessageService.getMiniMessageFor(getActor(), getRelationshipKind(),
                        getCurrentPageOffset(), pageSize));
            } else if (FROM_ACTOR_STREAM_TYPE.equals(streamType)) {
                pageMiniMessages.addAll(miniMessageService.getMiniMessageFrom(getActor(), getCurrentPageOffset(),
                        pageSize));
            } else {
                log.error("Unknown stream type: " + streamType);
            }
            nextOffset = offset + pageMiniMessages.size();
            setResultsCount(UNKNOWN_SIZE_AFTER_QUERY);
        }
        return pageMiniMessages;
    }

}
