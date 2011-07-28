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
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_FACET;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
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

    public static final String ADMINISTRATORS_SUFFIX = "_administrators";

    public static final String MEMBERS_SUFFIX = "_members";

    public static final String ADMINISTRATORS_LABEL_PREFIX = "Administrators of ";

    public static final String MEMBERS_LABEL_PREFIX = "Members of ";

    private SocialWorkspaceHelper() {
        // Helper class
    }

    public static String getSocialWorkspaceAdministratorsGroupName(
            DocumentModel doc) {
        return doc.getId() + ADMINISTRATORS_SUFFIX;
    }

    public static String getSocialWorkspaceMembersGroupName(DocumentModel doc) {
        return doc.getId() + MEMBERS_SUFFIX;
    }

    public static String getSocialWorkspaceAdministratorsGroupLabel(
            DocumentModel doc) {
        try {
            return ADMINISTRATORS_LABEL_PREFIX + doc.getTitle();
        } catch (ClientException e) {
            log.warn("Cannot retrieve document title to build administrators group label: "
                    + doc.getId());
            log.debug(e, e);
            return null;
        }
    }

    public static String getSocialWorkspaceMembersGroupLabel(DocumentModel doc) {
        try {
            return MEMBERS_LABEL_PREFIX + doc.getTitle();
        } catch (ClientException e) {
            log.warn("Cannot retrieve document title to build members group label: "
                    + doc.getId());
            log.debug(e, e);
            return null;
        }
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
