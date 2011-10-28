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

import java.util.Date;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public final class ActivityBuilder {

    private String actor;

    private String displayActor;

    private String verb;

    private String object;

    private String displayObject;

    private String target;

    private String displayTarget;

    private Date publishedDate;

    public ActivityBuilder actor(String actor) {
        this.actor = actor;
        return this;
    }

    public ActivityBuilder displayActor(String displayActor) {
        this.displayActor = displayActor;
        return this;
    }

    public ActivityBuilder verb(String verb) {
        this.verb = verb;
        return this;
    }

    public ActivityBuilder object(String object) {
        this.object = object;
        return this;
    }

    public ActivityBuilder displayObject(String displayObject) {
        this.displayObject = displayObject;
        return this;
    }

    public ActivityBuilder target(String target) {
        this.target = target;
        return this;
    }

    public ActivityBuilder displayTarget(String displayTarget) {
        this.displayTarget = displayTarget;
        return this;
    }

    public ActivityBuilder publishedDate(Date publishedDate) {
        this.publishedDate = publishedDate;
        return this;
    }

    public Activity build() {
        Activity activity = new ActivityImpl();
        activity.setActor(actor);
        activity.setDisplayActor(displayActor);
        activity.setObject(object);
        activity.setDisplayObject(displayObject);
        activity.setTarget(target);
        activity.setDisplayTarget(displayTarget);
        activity.setVerb(verb);
        activity.setPublishedDate(publishedDate != null ? publishedDate
                : new Date());
        return activity;
    }

}
