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

package org.nuxeo.ecm.social.workspace;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;
import static org.nuxeo.ecm.social.workspace.SocialConstants.DASHBOARD_SPACES_CONTAINER_TYPE;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.isSocialDocument;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.isSocialWorkspace;

import java.io.Serializable;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
@Name("socialWorkspaceActions")
@Scope(CONVERSATION)
@Install(precedence = FRAMEWORK)
public class SocialWorkspaceActions implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String FULLSCREEN_VIEW_ID = "fullscreen";

    @In(create = true)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true)
    protected transient SocialWorkspaceService socialWorkspaceService;

    @In(create = true)
    protected transient NuxeoPrincipal currentUser;

    public SocialWorkspace toSocialWorkspace(DocumentModel doc) {
        return SocialWorkspaceHelper.toSocialWorkspace(doc);
    }

    public boolean isCurrentUserAdministratorOrMemberOfCurrentSocialWorkspace()
            throws ClientException {
        DocumentModel doc = navigationContext.getCurrentDocument();
        SocialWorkspace socialWorkspace = socialWorkspaceService.getDetachedSocialWorkspaceContainer(doc);
        return socialWorkspace.isAdministratorOrMember(currentUser);
    }

    public SocialWorkspace getSocialWorkspaceContainer(DocumentModel doc) {
        return socialWorkspaceService.getDetachedSocialWorkspaceContainer(doc);
    }

    public SocialWorkspace getSocialWorkspaceContainer() {
        DocumentModel doc = navigationContext.getCurrentDocument();
        return getSocialWorkspaceContainer(doc);
    }

    /**
     * Navigate to the Dashboard of the Social Workspace if the document belong
     * to one of it, else navigate to the default view of the current document.
     */
    public String backToDashboard() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        DocumentModel superSpace = documentManager.getSuperSpace(currentDocument);

        if (isSocialWorkspace(superSpace)) {
            SocialWorkspace socialWorkspace = toSocialWorkspace(superSpace);
            DocumentModel dashboardSpacesRoot = documentManager.getDocument(new PathRef(
                    socialWorkspace.getDashboardSpacesRootPath()));
            return navigationContext.navigateToDocument(dashboardSpacesRoot,
                    FULLSCREEN_VIEW_ID);
        } else {
            return navigationContext.navigateToDocument(superSpace);
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

}
