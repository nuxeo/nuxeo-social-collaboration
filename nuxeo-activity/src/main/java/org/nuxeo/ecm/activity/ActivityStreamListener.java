/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.nuxeo.ecm.activity;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.PostCommitEventListener;
import org.nuxeo.runtime.api.Framework;

/**
 * Listener called asynchronously to save events as activities through the
 * {@link ActivityStreamService}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class ActivityStreamListener implements PostCommitEventListener {

    @Override
    public void handleEvent(EventBundle events) throws ClientException {
        for (Event event : events) {
            handleEvent(event);
        }
    }

    protected void handleEvent(Event event) throws ClientException {
        EventContext eventContext = event.getContext();
        // if (eventContext instanceof ActivityEventContext) {
        // ActivityEventContext activityEventContext = (ActivityEventContext)
        // eventContext;
        // Activity activity = activityEventContext.getActivity();
        // getActivityStreamService().addActivity(activity);
        // }
        if (eventContext.getArguments().length > 0) {
            Object o = eventContext.getArguments()[0];
            if (o instanceof Activity) {
                getActivityStreamService().addActivity((Activity) o);
            }
        }
    }

    protected Activity toActivity(Event event) {
        return null;
    }

    protected ActivityStreamService getActivityStreamService()
            throws ClientException {
        try {
            return Framework.getService(ActivityStreamService.class);
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

}
