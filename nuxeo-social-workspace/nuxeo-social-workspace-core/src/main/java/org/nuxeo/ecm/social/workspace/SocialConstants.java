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
 *
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

    public static final String VALIDATE_SOCIAL_WORKSPACE_TASK_NAME = "validateSocialWorkspace";

    public static final String DASHBOARD_SPACES_CONTAINER_TYPE = "DashboardSpacesContainer";

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

    // Subscription Request
    public static final String SUBSCRIPTION_REQUEST_TYPE = "SubscriptionRequest";

    public static final String SUBSCRIPTION_REQUEST_SCHEMA = "subscription_request";

    public static final String SUBSCRIPTION_REQUEST_TYPE_JOIN = "joinRequest";

    public static final String SUBSCRIPTION_REQUEST_USERNAME_PROPERTY = "req:username";

    public static final String SUBSCRIPTION_REQUEST_USER_EMAIL_PROPERTY = "req:userEmail";

    public static final String SUBSCRIPTION_REQUEST_TYPE_PROPERTY = "req:type";

    public static final String SUBSCRIPTION_REQUEST_INFO_PROPERTY = "req:info";

    public static final String SUBSCRIPTION_REQUEST_PROCESSED_DATE_PROPERTY = "req:processedDate";

    public static final String SUBSCRIPTION_REQUEST_PROCESSED_COMMENT_PROPERTY = "req:processedComment";

    public static final String SUBSCRIPTION_REQUEST_PENDING_STATE = "pending";

    public static final String SUBSCRIPTION_REQUEST_ACCEPTED_STATE = "accepted";

    public static final String SUBSCRIPTION_REQUEST_REJECTED_STATE = "rejected";

    public static final String SUBSCRIPTION_REQUEST_ACCEPT_TRANSITION = "accept";

    public static final String SUBSCRIPTION_REQUEST_REJECT_TRANSITION = "reject";

    // Activity stuff
    public static final String MAKE_DOCUMENT_PUBLIC_VERB = "makedocumentpublic";

    public static final String IN_SOCIAL_WORKSPACE_SUFFIX = "InSocialWorkspace";

    public static final String DOCUMENT_CREATED_IN_SOCIAL_WORKSPACE_VERB = DOCUMENT_CREATED
            + IN_SOCIAL_WORKSPACE_SUFFIX;

    public static final String DOCUMENT_UPDATED_IN_SOCIAL_WORKSPACE_VERB = DOCUMENT_UPDATED
            + IN_SOCIAL_WORKSPACE_SUFFIX;

    private SocialConstants() {
    }

}
