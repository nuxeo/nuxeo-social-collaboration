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

package org.nuxeo.ecm.social.workspace.gadgets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.activity.AbstractActivityPageProvider;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.social.mini.message.MiniMessage;
import org.nuxeo.ecm.social.mini.message.MiniMessageService;
import org.nuxeo.ecm.social.user.relationship.RelationshipKind;
import org.nuxeo.runtime.api.Framework;

/**
 * Page provider listing mini messages for a given social workspace
 * <p>
 * This page provider requires three properties:
 * <ul>
 * <li>the social workspace ID</li>
 * <li>the repository name</li>
 * <li>the relationship kind to use to find social workspace members</li>
 * </ul>
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class SocialWorkspaceMiniMessagePageProvider extends
        AbstractActivityPageProvider<MiniMessage> {

    private static final long serialVersionUID = 1L;

    public static final String SOCIAL_WORKSPACE_ID_PROPERTY = "socialWorkspaceId";

    public static final String REPOSITORY_NAME_PROPERTY = "repositoryName";

    public static final String RELATIONSHIP_KIND_PROPERTY = "relationshipKind";

    protected List<MiniMessage> pageMiniMessages;

    @Override
    public List<MiniMessage> getCurrentPage() {
        if (pageMiniMessages == null) {
            pageMiniMessages = new ArrayList<MiniMessage>();
            long pageSize = getMinMaxPageSize();

            String socialWorkspaceActivityObject = ActivityHelper.createDocumentActivityObject(
                    getRepositoryName(), getSocialWorkspaceId());
            RelationshipKind relationshipKind = getRelationshipKind();
            MiniMessageService miniMessageService = Framework.getLocalService(MiniMessageService.class);
            pageMiniMessages.addAll(miniMessageService.getMiniMessageFor(
                    socialWorkspaceActivityObject, relationshipKind,
                    getCurrentPageOffset(), pageSize));
            nextOffset = offset + pageMiniMessages.size();

            setResultsCount(UNKNOWN_SIZE_AFTER_QUERY);
        }
        return pageMiniMessages;
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
