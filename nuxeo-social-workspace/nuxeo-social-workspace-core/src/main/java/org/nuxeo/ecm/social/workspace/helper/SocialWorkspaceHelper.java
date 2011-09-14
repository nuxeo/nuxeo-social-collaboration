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
 *     Nuxeo - initial API and implementation
 */

package org.nuxeo.ecm.social.workspace.helper;

import static org.nuxeo.ecm.social.workspace.SocialConstants.NEWS_ROOT_RELATIVE_PATH;
import static org.nuxeo.ecm.social.workspace.SocialConstants.PRIVATE_SECTION_RELATIVE_PATH;
import static org.nuxeo.ecm.social.workspace.SocialConstants.PUBLIC_SECTION_RELATIVE_PATH;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_DOCUMENT_FACET;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_CONTAINER_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_FACET;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.social.user.relationship.RelationshipKind;
import org.nuxeo.ecm.social.workspace.adapters.SocialDocument;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;

/**
 * Class to provide around Social Workspace.
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.4.1
 */
public class SocialWorkspaceHelper {

    private static final Log log = LogFactory.getLog(SocialWorkspaceHelper.class);

    private static final String KIND_GROUP = "socialworkspace";

    private static final String SEPARATOR = "_";

    private static final String ADMINISTRATORS_SUFFIX = "administrators";

    private static final String MEMBERS_SUFFIX = "members";

    private static final String ADMINISTRATORS_LABEL_PREFIX = "Administrators of ";

    private static final String MEMBERS_LABEL_PREFIX = "Members of ";

    private SocialWorkspaceHelper() {
        // Helper class
    }

    public static String getSocialWorkspaceAdministratorsGroupName(
            String activityObject) {
        return activityObject + SEPARATOR + ADMINISTRATORS_SUFFIX;
    }

    public static String getSocialWorkspaceAdministratorsGroupName(
            DocumentModel doc) {
        return ActivityHelper.createDocumentActivityObject(doc) + SEPARATOR
                + ADMINISTRATORS_SUFFIX;
    }

    public static String getSocialWorkspaceMembersGroupName(
            String activityObject) {
        return activityObject + SEPARATOR + MEMBERS_SUFFIX;
    }

    public static String getSocialWorkspaceMembersGroupName(DocumentModel doc) {
        return ActivityHelper.createDocumentActivityObject(doc) + SEPARATOR
                + MEMBERS_SUFFIX;
    }

    public static String getSocialWorkspaceAdministratorsGroupLabel(
            String docTitle) {
        return ADMINISTRATORS_LABEL_PREFIX + docTitle;
    }

    public static String getSocialWorkspaceMembersGroupLabel(String docTitle) {
        return MEMBERS_LABEL_PREFIX + docTitle;
    }

    public static boolean isValidSocialWorkspaceGroupName(String groupName) {
        return !StringUtils.isBlank(groupName)
                && (groupName.endsWith(getSocialWorkspaceAdministratorsGroupName("")) || groupName.endsWith(getSocialWorkspaceMembersGroupName("")));
    }

    public static RelationshipKind buildRelationKindFromGroupName(
            String groupName) {
        String name = StringUtils.split(groupName, SEPARATOR)[1];
        if (MEMBERS_SUFFIX.equals(name)) {
            return buildRelationMemberKind();
        } else if (ADMINISTRATORS_SUFFIX.equals(name)) {
            return buildRelationAdministratorKind();
        }
        log.warn("Trying to instantiate RelationshipKind from an unknown group: "
                + groupName);
        return null;
    }

    public static String getRelationDocActivityObjectFromGroupName(
            String groupName) {
        return StringUtils.split(groupName, SEPARATOR)[0];
    }

    public static RelationshipKind buildRelationMemberKind() {
        return RelationshipKind.newInstance(KIND_GROUP, MEMBERS_SUFFIX);
    }

    public static RelationshipKind buildRelationAdministratorKind() {
        return RelationshipKind.newInstance(KIND_GROUP, ADMINISTRATORS_SUFFIX);
    }

    public static RelationshipKind buildRelationKind() {
        return RelationshipKind.fromGroup(KIND_GROUP);
    }

    public static boolean isSocialWorkspaceContainer(DocumentModel doc) {
        return doc != null && doc.getType().equals(SOCIAL_WORKSPACE_CONTAINER_TYPE);
    }

    public static boolean isSocialWorkspace(DocumentModel doc) {
        return doc != null && doc.hasFacet(SOCIAL_WORKSPACE_FACET);
    }

    public static boolean isSocialDocument(DocumentModel doc) {
        return doc != null && !doc.isProxy()
                && doc.hasFacet(SOCIAL_DOCUMENT_FACET);
    }

    public static SocialWorkspace toSocialWorkspace(DocumentModel doc) {
        return doc.getAdapter(SocialWorkspace.class);
    }

    public static SocialDocument toSocialDocument(DocumentModel doc) {
        return doc.getAdapter(SocialDocument.class);
    }

    public static String getPrivateSectionPath(DocumentModel socialWorkspace) {
        if (socialWorkspace == null) {
            throw new IllegalArgumentException(
                    "Given social workspace is null, can't return the private section");
        }
        return socialWorkspace.getPathAsString() + "/"
                + PRIVATE_SECTION_RELATIVE_PATH;
    }

    public static String getPublicSectionPath(DocumentModel socialWorkspace) {
        if (socialWorkspace == null) {
            throw new IllegalArgumentException(
                    "Given social workspace is null, can't return the private section");
        }

        return socialWorkspace.getPathAsString() + "/"
                + PUBLIC_SECTION_RELATIVE_PATH;
    }

    public static String getNewsItemsRootPath(DocumentModel socialWorkspace) {
        if (socialWorkspace == null) {
            throw new IllegalArgumentException(
                    "Given social workspace is null, can't return the private section");
        }

        return socialWorkspace.getPathAsString() + "/"
                + NEWS_ROOT_RELATIVE_PATH;
    }

}
