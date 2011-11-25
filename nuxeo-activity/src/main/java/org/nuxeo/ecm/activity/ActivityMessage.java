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

package org.nuxeo.ecm.activity;

import java.io.Serializable;
import java.util.Date;

/**
 * Immutable object representing an Activity message.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public final class ActivityMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Serializable activityId;

    private final String message;

    private final Date publishedDate;

    public ActivityMessage(Serializable activityId, String message,
            Date publishedDate) {
        this.activityId = activityId;
        this.message = message;
        this.publishedDate = publishedDate;
    }

    public ActivityMessage(Activity activity, String message) {
        this(activity.getId(), message, activity.getPublishedDate());
    }

    public Serializable getActivityId() {
        return activityId;
    }

    public String getMessage() {
        return message;
    }

    public Date getPublishedDate() {
        return publishedDate;
    }

}
