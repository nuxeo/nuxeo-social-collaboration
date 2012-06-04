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

package org.nuxeo.ecm.social.mini.message;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityMessage;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.runtime.api.Framework;

/**
 * Page provider listing mini messages, as a list of {@link Activity}, for a
 * given actor
 * <p>
 * This page provider requires two properties: the first one to be filled with
 * the actor, and the second one to be filled with the relationship kind to use.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public class MiniMessageActivityPageProvider extends
        AbstractMiniMessagePageProvider<ActivityMessage> implements
        PageProvider<ActivityMessage> {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(MiniMessageActivityPageProvider.class);

    @Override
    public List<ActivityMessage> getCurrentPage() {
        if (pageMiniMessages == null) {
            pageMiniMessages = new ArrayList<ActivityMessage>();
            long pageSize = getMinMaxPageSize();

            MiniMessageService miniMessageService = Framework.getLocalService(MiniMessageService.class);
            String streamType = getStreamType();
            if (FOR_ACTOR_STREAM_TYPE.equals(streamType)) {
                pageMiniMessages.addAll(miniMessageService.getMiniMessageActivitiesFor(
                        getActor(), getRelationshipKind(),
                        getCurrentPageOffset(), pageSize).toActivityMessages(
                        getLocale()));
            } else if (FROM_ACTOR_STREAM_TYPE.equals(streamType)) {
                pageMiniMessages.addAll(miniMessageService.getMiniMessageActivitiesFrom(
                        getActor(), getCurrentPageOffset(), pageSize).toActivityMessages(
                        getLocale()));
            } else {
                log.error("Unknown stream type: " + streamType);
            }
            nextOffset = offset + pageMiniMessages.size();
            setResultsCount(UNKNOWN_SIZE_AFTER_QUERY);
        }
        return pageMiniMessages;
    }

}
