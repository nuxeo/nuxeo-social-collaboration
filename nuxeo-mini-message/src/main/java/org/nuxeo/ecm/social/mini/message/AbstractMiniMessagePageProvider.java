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

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.nuxeo.ecm.activity.AbstractActivityPageProvider;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.social.relationship.RelationshipKind;

/**
 *
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public abstract class AbstractMiniMessagePageProvider<T> extends
        AbstractActivityPageProvider<T> {

    public static final String LOCALE_PROPERTY = "locale";

    public static final String ACTOR_PROPERTY = "actor";

    public static final String RELATIONSHIP_KIND_PROPERTY = "relationshipKind";

    public static final String STREAM_TYPE_PROPERTY = "streamType";

    public static final String FOR_ACTOR_STREAM_TYPE = "forActor";

    public static final String FROM_ACTOR_STREAM_TYPE = "fromActor";

    protected List<T> pageMiniMessages;

    protected Locale getLocale() {
        Map<String, Serializable> props = getProperties();
        Locale locale = (Locale) props.get(LOCALE_PROPERTY);
        if (locale == null) {
            throw new ClientRuntimeException("Cannot find " + LOCALE_PROPERTY
                    + " property.");
        }
        return locale;
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
        pageMiniMessages = null;
        super.pageChanged();
    }

    @Override
    public void refresh() {
        pageMiniMessages = null;
        super.refresh();
    }
}
