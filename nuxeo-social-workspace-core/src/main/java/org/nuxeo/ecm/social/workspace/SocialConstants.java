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
 *     eugen,ronan
 */
package org.nuxeo.ecm.social.workspace;

import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_CREATED;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_UPDATED;

/**
 * * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 */
public class SocialConstants {

    // Social Workspace
    public static final String SOCIAL_WORKSPACE_FACET = "SocialWorkspace";

    public static final String SOCIAL_WORKSPACE_TYPE = "SocialWorkspace";

    public static final String FIELD_SOCIAL_WORKSPACE_APPROVE_SUBSCRIPTION = "socialw:approveSubscription";

    public static final String SOCIAL_WORKSPACE_IS_PUBLIC_PROPERTY = "socialw:isPublic";

    public static final String PRIVATE_SECTION_RELATIVE_PATH = "private-section/";

    public static final String PUBLIC_SECTION_RELATIVE_PATH = "public-section/";

    public static final String DASHBOARD_SPACES_ROOT_NAME = "social";

    public static final String PRIVATE_DASHBOARD_SPACE_NAME = "privateDashboardSpace";

    public static final String PUBLIC_DASHBOARD_SPACE_NAME = "publicDashboardSpace";

    public static final String COLLABORATION_DASHBOARD_SPACE_NAME = "collaborationDashboardSpace";

    public static final String DASHBOARD_SPACES_CONTAINER_TYPE = "DashboardSpacesContainer";

    public static final String SOCIAL_WORKSPACE_CONTAINER_TYPE = "SocialDomain";

    public static final String NEWS_ITEM_ROOT_TYPE = "NewsItemsRoot";

    // Social Document
    public static final String SOCIAL_DOCUMENT_FACET = "SocialDocument";

    public static final String SOCIAL_DOCUMENT_IS_PUBLIC_PROPERTY = "socialdoc:isPublic";

    public static final String ARTICLE_TYPE = "Article";

    public static final String NEWS_ITEM_TYPE = "NewsItem";

    public static final String NEWS_ROOT_RELATIVE_PATH = "news-root/";

    public static final String CONTENT_PICTURE_PICTURE_PROPERTY = "contentpict:picture";

    public static final String DC_TITLE_PROPERTY = "dc:title";

    public static final String NOTE_NOTE_PROPERTY = "note:note";

    public static final String DC_CREATED_PROPERTY = "dc:created";

    public static final String DC_AUTHOR_PROPERTY = "dc:author";

    // Activity stuff
    public static final String MAKE_DOCUMENT_PUBLIC_VERB = "makedocumentpublic";

    public static final String IN_SOCIAL_WORKSPACE_SUFFIX = "InSocialWorkspace";

    public static final String DOCUMENT_CREATED_IN_SOCIAL_WORKSPACE_VERB = DOCUMENT_CREATED
            + IN_SOCIAL_WORKSPACE_SUFFIX;

    public static final String DOCUMENT_UPDATED_IN_SOCIAL_WORKSPACE_VERB = DOCUMENT_UPDATED
            + IN_SOCIAL_WORKSPACE_SUFFIX;

    // Event stuff
    public static final String EVENT_MEMBERS_ADDED = "newMembersAdded";

    public static final String EVENT_MEMBERS_REMOVED = "newMembersRemoved";

    public static final String CTX_PRINCIPALS_PROPERTY = "socialWorkspacePrincipals";

    // Social Workspace groups
    public static final String SEPARATOR = "_";

    public static final String ADMINISTRATORS_SUFFIX = "administrators";

    public static final String MEMBERS_SUFFIX = "members";

    public static final String ADMINISTRATORS_GROUP_SUFFIX = SEPARATOR
            + ADMINISTRATORS_SUFFIX;

    public static final String MEMBERS_GROUP_SUFFIX = SEPARATOR
            + MEMBERS_SUFFIX;

    private SocialConstants() {
        // Constants class
    }

}
