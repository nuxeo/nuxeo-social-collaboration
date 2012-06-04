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

package org.nuxeo.ecm.social.workspace.gadgets;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.nuxeo.ecm.activity.AbstractActivityPageProvider;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.social.mini.message.MiniMessage;
import org.nuxeo.ecm.social.relationship.RelationshipKind;

/**
 * Base class for page providers
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public abstract class AbstractSocialWorkspaceMiniMessagePageProvider<T> extends
        AbstractActivityPageProvider<T> {

    public static final String LOCALE_PROPERTY = "locale";

    public static final String SOCIAL_WORKSPACE_ID_PROPERTY = "socialWorkspaceId";

    public static final String REPOSITORY_NAME_PROPERTY = "repositoryName";

    public static final String RELATIONSHIP_KIND_PROPERTY = "relationshipKind";

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

    protected String getSocialWorkspaceId() {
        Map<String, Serializable> props = getProperties();
        String socialWorkspaceId = (String) props.get(SOCIAL_WORKSPACE_ID_PROPERTY);
        if (socialWorkspaceId == null) {
            throw new ClientRuntimeException("Cannot find "
                    + SOCIAL_WORKSPACE_ID_PROPERTY + " property.");
        }
        return socialWorkspaceId;
    }

    protected String getRepositoryName() {
        Map<String, Serializable> props = getProperties();
        String repositoryName = (String) props.get(REPOSITORY_NAME_PROPERTY);
        if (repositoryName == null) {
            throw new ClientRuntimeException("Cannot find "
                    + REPOSITORY_NAME_PROPERTY + " property.");
        }
        return repositoryName;
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
