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

import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_DOCUMENT_FACET;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_PUBLICATION_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_FACET;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;

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

    /**
     * Return true if one of the document parents is a SocialWorkspace and if
     * the document got the facet "Social Document"
     *
     * @param session : the current session in which the document is handled
     * @param socialDocument : the document to be tested
     * @return true if the document could be published in a social section.
     * @throws ClientException
     */
    public static boolean isSocialDocumentPublishable(CoreSession session,
            DocumentModel socialDocument) throws ClientException {
        DocumentRef socialDocumentRef = socialDocument.getRef();
        List<DocumentModel> parents = session.getParentDocuments(socialDocumentRef);
        return isDocumentPublishable(parents, socialDocument);
    }

    protected static boolean isDocumentPublishable(List<DocumentModel> parents,
            DocumentModel socialDocument) {
        return socialDocument.hasFacet(SOCIAL_DOCUMENT_FACET)
                && getSocialWorkspaceParentIfAny(parents, socialDocument) != null;
    }

    protected static DocumentModel getSocialWorkspaceParentIfAny(
            List<DocumentModel> parents, DocumentModel socialDocument) {
        for (DocumentModel currentParent : parents) {
            if (isSocialWorkspace(currentParent)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format(
                            "There is a %s as parent for the document \"%s\" and it's : \"%s\"",
                            SOCIAL_WORKSPACE_TYPE, socialDocument.toString(),
                            currentParent.toString()));
                }

                return currentParent;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format(
                    "There is no %s as parent for the document \"%s\"",
                    SOCIAL_WORKSPACE_TYPE, socialDocument.toString()));
        }
        return null;
    }

    /**
     * Returns true if socialWorkspace is not null and if it's got the facet
     * SocialWorkspace.
     *
     * @param socialWorkspace : the document to be tested
     * @return true if socialWorkspace exists with the facet SocialWorkspace.
     */
    public static boolean isSocialWorkspace(DocumentModel socialWorkspace) {
        return socialWorkspace != null
                && socialWorkspace.hasFacet(SOCIAL_WORKSPACE_FACET);
    }

    /**
     * Tries to published socialDocument within the section referenced by
     * sectionPath. Returns the socialDocument proxy if it exists.
     *
     * @param session current CoreSession in which the document is handled.
     * @param socialDocument the document to publish which must have the facet
     *            SocialDocument and be a child of a socialWorkspace
     * @param sectionPath the path of the section where the document must be
     *            published
     * @return the proxy of the social document as publication, if it exists.
     * @throws ClientException if the document doesn't posses the facet
     *             "Social Document" and if the document can't be published
     */
    public static DocumentModel publishSocialdocument(CoreSession session,
            DocumentModel socialDocument, String sectionName)
            throws ClientException {
        if (session == null || socialDocument == null || sectionName == null) {
            return null;
        }
        if (!socialDocument.hasFacet(SOCIAL_DOCUMENT_FACET)) {
            String message = String.format(
                    "The document \"%s\" has no %s facet.",
                    socialDocument.toString(), SOCIAL_DOCUMENT_FACET);
            throw new ClientException(message);
        }

        List<DocumentModel> parents = session.getParentDocuments(socialDocument.getRef());
        DocumentModel parentSocialWorkspace = getSocialWorkspaceParentIfAny(
                parents, socialDocument);

        if (isSocialWorkspace(parentSocialWorkspace)) {

            DocumentModel doc = findTheSocialSection(session,
                    parentSocialWorkspace, sectionName);
            return publishSocialDocumentInOneSocialSection(session,
                    socialDocument, doc);
        } else {
            if (log.isErrorEnabled()) {
                String msg = String.format(
                        "The SocialDocument (%s) named \"%s\" is not a child of a document of %s type.",
                        socialDocument.getType(), socialDocument.getName(),
                        SOCIAL_WORKSPACE_TYPE);
                log.error(msg);
            }
        }
        return null;
    }

    protected static DocumentModel findTheSocialSection(CoreSession session,
            DocumentModel socialWorkspace, String socialSectionPath) {

        DocumentRef socialSectionRef = new PathRef(
                socialWorkspace.getPathAsString(), socialSectionPath);
        try {
            DocumentModel section = session.getDocument(socialSectionRef);
            if (SOCIAL_PUBLICATION_TYPE.equals(section.getType())) {
                return section;
            } else {
                if (log.isInfoEnabled()) {
                    String message = String.format(
                            "The section \"%s\" is not of the %s type.",
                            socialSectionRef, SOCIAL_PUBLICATION_TYPE);
                    log.info(message);
                }
            }
        } catch (ClientException e) {
            if (log.isInfoEnabled()) {
                String message = String.format(
                        "The social section \"%s\" can't be found.",
                        socialSectionRef);
                log.info(message);
            }
        }
        return null;
    }

    protected static DocumentModel publishSocialDocumentInOneSocialSection(
            CoreSession session, DocumentModel socialDocument,
            DocumentModel socialSection) throws ClientException {
        if (socialSection == null) {
            return null;
        }

        DocumentModel newsPublication = session.publishDocument(socialDocument,
                socialSection);
        session.save();
        if (log.isDebugEnabled()) {
            String msg = String.format(
                    "The SocialDocument (%s) named \"%s\" have been published in the private section called \"%s\"",
                    socialDocument.getType(), socialDocument.getName(),
                    socialSection.getName());
            log.debug(msg);
        }
        return newsPublication;
    }

}
