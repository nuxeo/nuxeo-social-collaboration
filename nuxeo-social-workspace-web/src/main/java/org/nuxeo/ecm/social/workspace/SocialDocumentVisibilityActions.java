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

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialDocument;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.social.workspace.adapters.SocialDocument;
import org.nuxeo.ecm.webapp.helpers.EventNames;

/**
 * This seam action bean is used to create or update a proxy of the current
 * social document.
 *
 * @author rlegall
 */
@Name("socialDocumentVisibilityActions")
@Scope(CONVERSATION)
@Install(precedence = FRAMEWORK)
public class SocialDocumentVisibilityActions implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(SocialDocumentVisibilityActions.class);

    @In(create = true)
    protected transient NavigationContext navigationContext;

    /**
     * create or update a proxy of the current social document in the public
     * social section of the social workspace.
     */
    public void makePublic() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        makePublic(currentDocument);
    }

    /**
     * create or update a proxy of the social document passed as an argument in
     * the public social section of the social workspace.
     */
    public void makePublic(DocumentModel document) throws ClientException {
        SocialDocument socialDocument = toSocialDocument(document);
        socialDocument.makePublic();
        Events.instance().raiseEvent(EventNames.DOCUMENT_CHANGED, document);
    }

    /**
     * hide the current document to non members of the social collaboration
     * workspace
     */
    public void restrictToMembers() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        restrictToMembers(currentDocument);
    }

    /**
     * Hide the social document passed as parameter to non members of the social
     * collaboration workspace
     */
    public void restrictToMembers(DocumentModel document)
            throws ClientException {
        SocialDocument socialDocument = toSocialDocument(document);
        socialDocument.restrictToMembers();
        Events.instance().raiseEvent(EventNames.DOCUMENT_CHANGED, document);
    }

    /**
     * Indicates if the current document is visible by everybody
     *
     * @return true if the current document is public, false otherwise.
     * @throws ClientException
     */
    public boolean isPublic() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        return isPublic(currentDocument);
    }

    /**
     * Indicates if the document passed as a parameter is visible by everybody
     *
     * @return true if the current document is public, false otherwise.
     * @throws ClientException
     */
    public boolean isPublic(DocumentModel document) throws ClientException {
        SocialDocument socialDocument = toSocialDocument(document);
        return socialDocument.isPublic();
    }

    /**
     * Indicates if the current document is only visible by members of the
     * community
     *
     * @return true if the current document is public, false otherwise.
     * @throws ClientException
     */
    public boolean isRestricted() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        return isRestricted(currentDocument);
    }

    /**
     * Indicates if the document passed as a parameter is only visible by
     * members of the community
     *
     * @return true if the current document is public, false otherwise.
     * @throws ClientException
     */
    public boolean isRestricted(DocumentModel document) throws ClientException {
        SocialDocument socialDocument = toSocialDocument(document);
        return socialDocument.isRestrictedToMembers();
    }

}
