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

/**
 * * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 *
 */
public class SocialConstants {

    public static final String REQUEST_DOC_TYPE = "Request";

    public static final String REQUEST_ROOT_NAME = "requests";

    public static final String REQUEST_SCHEMA = "request";

    public static final String REQUEST_TYPE_JOIN = "joinRequest";

    public static final String REQUEST_TYPE_INVITATION = "invitation";

    public static final String FIELD_REQUEST_USERNAME = "req:username";

    public static final String FIELD_REQUEST_TYPE = "req:type";

    public static final String FIELD_REQUEST_INFO = "req:info";

    public static final String FIELD_REQUEST_PROCESSED_DATE = "req:processedDate";

    public static final String FIELD_REQUEST_PROCESSED_COMMENT = "req:processedComment";

    public static final String VALIDATE_SOCIAL_WORKSPACE_TASK_NAME = "validateSocialWorkspace";

    public static final String ARTICLE_TYPE = "Article";

    public static final String ARTICLE_SCHEMA = "article";
    
    public static final String ARTICLE_PICTURE_FIELD = "art:picture";

    public static final String FIELD_DC_TITLE = "dc:title";

    public static final String FIELD_NOTE_NOTE = "note:note";

    public static final String FIELD_DC_CREATED = "dc:created";

    public static final String FIELD_DC_AUTHOR = "dc:author";

    public static final String FIELD_SOCIAL_IS_RESTRICTED = "soc:isRestricted";

    public static final String SOCIAL_WORKSPACE_ACL_NAME = "socialWorkspaceAcl";

    public static final String SOCIAL_WORKSPACE_FACET = "SocialWorkspace";

    public static final String SOCIAL_WORKSPACE_TYPE = "SocialWorkspace";

    public static final String SOCIAL_DOCUMENT_FACET = "SocialDocument";

    public static final String NEWS_TYPE = "News";
    
    public static final String SOCIAL_SECTION_TYPE = "SocialSection";

    public static final String PRIVATE_SECTION_RELATIVE_PATH = "private-section/";

    public static final String PUBLIC_SECTION_RELATIVE_PATH = "public-section/";

    private SocialConstants() {
    }

}
