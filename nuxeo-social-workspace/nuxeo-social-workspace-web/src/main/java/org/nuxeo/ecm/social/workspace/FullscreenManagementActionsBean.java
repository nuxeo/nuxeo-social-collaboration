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
 *     Benjamin JALON <bjalon@nuxeo.com>
 */
package org.nuxeo.ecm.social.workspace;

import java.io.Serializable;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.webapp.contentbrowser.DocumentActions;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;

import static org.nuxeo.ecm.social.workspace.SocialConstants.DASHBOARD_SPACES_CONTAINER_TYPE;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.isSocialDocument;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.isSocialWorkspace;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialWorkspace;

/**
 * @author Benjamin JALON <bjalon@nuxeo.com>
 *
 */
@Name("fullscreenManagementActions")
@Scope(CONVERSATION)
@Install(precedence = FRAMEWORK)
public class FullscreenManagementActionsBean implements Serializable{

    private static final long serialVersionUID = 1L;

    public static final String FULLSCREEN_VIEW_ID = "fullscreen";

    public static final String FULLSCREEN_CREATE_VIEW = "create_social_workspace_document";

    @In(create = true)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true)
    protected transient DocumentActions documentActions;

    /**
     * Navigate to the Dashboard of the Social Workspace if the document belong
     * to one of it, else navigate to the default view of the current document.
     */
    public String backToDashboard() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        DocumentModel sourceDocument = currentDocument;
        if (currentDocument.isProxy()) {
            sourceDocument = documentManager.getSourceDocument(currentDocument.getRef());
        }

        DocumentModel superSpace = documentManager.getSuperSpace(sourceDocument);

        if (isSocialWorkspace(superSpace)) {
            SocialWorkspace socialWorkspace = toSocialWorkspace(superSpace);
            DocumentModel dashboardSpacesRoot = documentManager.getDocument(new PathRef(
                    socialWorkspace.getDashboardSpacesRootPath()));
            return navigationContext.navigateToDocument(dashboardSpacesRoot,
                    FULLSCREEN_VIEW_ID);
        } else {
            return navigationContext.navigateToDocument(currentDocument);
        }
    }

    public String navigateToDMView() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        if (DASHBOARD_SPACES_CONTAINER_TYPE.equals(currentDocument.getType())) {
            DocumentModel superSpace = documentManager.getSuperSpace(currentDocument);
            return navigationContext.navigateToDocument(superSpace);
        }
        return navigationContext.navigateToDocument(currentDocument);
    }

    public String navigateToFullscreenView() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        if (isSocialWorkspace(currentDocument)) {
            SocialWorkspace socialWorkspace = toSocialWorkspace(currentDocument);
            DocumentModel dashboardSpacesRoot = documentManager.getDocument(new PathRef(
                    socialWorkspace.getDashboardSpacesRootPath()));
            return navigationContext.navigateToDocument(dashboardSpacesRoot,
                    FULLSCREEN_VIEW_ID);
        } else if (isSocialDocument(currentDocument)) {
            return navigationContext.navigateToDocument(currentDocument,
                    FULLSCREEN_VIEW_ID);
        } else {
            return navigationContext.navigateToDocument(currentDocument);
        }
    }

    public String createNewDocument(String type) throws ClientException {
        DocumentModel currentDoc = navigationContext.getCurrentDocument();
        DocumentModel parentContainer = documentManager.getDocument(currentDoc.getParentRef());
        navigationContext.navigateToDocument(parentContainer);
        documentActions.createDocument(type);
        return FULLSCREEN_CREATE_VIEW;
    }

    public String createSameTypeDocument() throws ClientException {
        String type = navigationContext.getCurrentDocument().getType();
        return createNewDocument(type);
    }
}
