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

import static org.nuxeo.ecm.activity.ActivityHelper.getUsername;

import java.io.Serializable;
import java.util.Date;

import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityMessageHelper;

/**
 * Immutable object representing a mini message.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public final class MiniMessage {

    private Serializable id;

    private String actor;

    private String displayActor;

    private String message;

    private Date publishedDate;

    private MiniMessage(Serializable id, String actor, String displayActor, String message, Date publishedDate) {
        this.id = id;
        this.actor = actor;
        this.displayActor = displayActor;
        this.message = message;
        this.publishedDate = publishedDate;
    }

    public static MiniMessage fromActivity(Activity activity) {
        String message = MiniMessageHelper.replaceURLsByLinks(activity.getObject());
        return new MiniMessage(activity.getId(), getUsername(activity.getActor()),
                ActivityMessageHelper.getUserProfileLink(activity.getActor(), activity.getDisplayActor()), message,
                activity.getPublishedDate());
    }

    public Serializable getId() {
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
