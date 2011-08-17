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

import java.util.Date;

import org.nuxeo.ecm.activity.Activity;

/**
 * Immutable object representing a mini message.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public final class MiniMessage {

    private long id;

    private String actor;

    private String displayActor;

    private String message;

    private Date publishedDate;

    private MiniMessage(long id, String actor, String displayActor,
            String message, Date publishedDate) {
        this.id = id;
        this.actor = actor;
        this.displayActor = displayActor;
        this.message = message;
        this.publishedDate = publishedDate;
    }

    public static MiniMessage fromActivity(Activity activity) {
        return new MiniMessage(activity.getId(), activity.getActor(),
                activity.getDisplayActor(), activity.getObject(),
                activity.getPublishedDate());
    }

    public long getId() {
        return id;
    }

    public String getActor() {
        return actor;
    }

    public String getDisplayActor() {
        return displayActor;
    }

    public String getMessage() {
        return message;
    }

    public Date getPublishedDate() {
        return publishedDate;
    }

}
