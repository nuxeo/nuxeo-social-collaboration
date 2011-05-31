/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 * Contributors:
 * Nuxeo - initial API and implementation
 */

package org.nuxeo.ecm.social.workspace;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Class to provide some useful methods
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.4.1
 */
public class SocialWorkspaceHelper {

    private static final Log log = LogFactory.getLog(SocialWorkspaceHelper.class);

    public static final String ADMINISTRATORS_SUFFIX = "_administrators";

    public static final String MEMBERS_SUFFIX = "_members";

    public static final String COMMUNITY_FACET = "CommunityFacet";

    private SocialWorkspaceHelper() {
        // Helper class
    }

    public static String getCommunityAdministratorsGroupName(DocumentModel doc) {
        return doc.getId() + ADMINISTRATORS_SUFFIX;
    }

    public static String getCommunityMembersGroupName(DocumentModel doc) {
        return doc.getId() + MEMBERS_SUFFIX;
    }

    public static boolean couldDocumentBePublished(CoreSession session,
            DocumentModel news) throws ClientException {
        List<DocumentModel> parents = session.getParentDocuments(news.getRef());
        return couldDocumentBePublished(parents, news);
    }

    protected static boolean couldDocumentBePublished(List<DocumentModel> parents,
            DocumentModel news) {
        for (DocumentModel currentParent : parents) {
            if (isSocialWorkspace(currentParent)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format(
                            "There is a %s as parent for the document \"%s\" and it's : \"%s\"",
                            SocialConstants.SOCIAL_WORKSPACE_TYPE,
                            news.toString(), currentParent.toString()));
                }
                return news.hasFacet(SocialConstants.COMMUNITY_DOCUMENT_FACET);
            }

        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("There is no %s as parent for the document \"%s\"",
                    SocialConstants.SOCIAL_WORKSPACE_TYPE, news.toString()));
        }
        return false;
    }

    public static boolean isSocialWorkspace(DocumentModel community) {
        return community != null
                && SocialConstants.SOCIAL_WORKSPACE_TYPE.equals(community.getType());
    }


}
