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

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.ADD_CHILDREN;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ;
import static org.nuxeo.ecm.social.workspace.SocialConstants.DASHBOARD_SPACES_CONTAINER_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.NEWS_ITEM_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.isSocialDocument;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.isSocialWorkspace;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.isSocialWorkspaceContainer;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialWorkspace;
import static org.nuxeo.ecm.webapp.helpers.EventNames.DOCUMENT_CHILDREN_CHANGED;

import java.io.Serializable;

import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.api.WebActions;
import org.nuxeo.ecm.platform.ui.web.rest.RestHelper;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;
import org.nuxeo.ecm.webapp.contentbrowser.DocumentActions;
import org.nuxeo.ecm.webapp.helpers.EventManager;

/**
 * @author Benjamin JALON <bjalon@nuxeo.com>
 */
@Name("collaborationActions")
@Scope(CONVERSATION)
@Install(precedence = FRAMEWORK)
public class CollaborationActions implements Serializable {

    private static final Log log = LogFactory.getLog(CollaborationActions.class);

    private static final String AFTER_SOCIAL_COLLABORATION_EDITION_VIEW = "after-social-collaboration-edition";

    private static final String AFTER_SOCIAL_COLLABORATION_CREATION_VIEW = "after-social-collaboration-creation";

    private static final String EDIT_SOCIAL_DOCUMENT_VIEW = "edit_social_document";

    private static final String CREATE_SOCIAL_DOCUMENT_VIEW = "create_social_document";

    private static final String DELETE_TRANSITION = "delete";

    private static final String NEWS_ITEMS_VIEW = "news_items";

    private static final String ARTICLES_VIEW = "articles";

    private static final String FILES_VIEW = "files";

    private static final long serialVersionUID = 1L;

    public static final String COLLABORATION_VIEW_ID = "collaboration";

    public static final String DASHBOARD_VIEW_ID = "dashboard";

    public static final String MAIN_TABS_DOCUMENTS = "MAIN_TABS:documents";

    @In(create = true)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true)
    protected transient DocumentActions documentActions;

    @In(create = true)
    protected transient SocialWorkspaceActions socialWorkspaceActions;

    @In(create = true)
    protected transient SocialWorkspaceService socialWorkspaceService;

    @In(create = true)
    protected transient WebActions webActions;

    @In(create = true)
    protected transient RestHelper restHelper;

    protected DocumentModel previous;

    public DocumentModel getPrevious() {
        return previous;
    }

    /**
     * Navigate to the Dashboard of the Social Workspace if the document belong to one of it, else navigate to the
     * default view of the current document.
     */
    public String backToDashboard() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        DocumentModel sourceDocument = currentDocument;
        if (currentDocument.isProxy()) {
            sourceDocument = documentManager.getSourceDocument(currentDocument.getRef());
        }

        SocialWorkspace socialWorkspace = socialWorkspaceService.getDetachedSocialWorkspace(sourceDocument);

        if (socialWorkspace != null) {
            DocumentModel dashboardSpacesRoot = documentManager.getDocument(new PathRef(
                    socialWorkspace.getDashboardSpacesRootPath()));
            webActions.setCurrentTabIds(SocialWorkspaceActions.MAIN_TABS_COLLABORATION);
            return navigationContext.navigateToDocument(dashboardSpacesRoot, COLLABORATION_VIEW_ID);
        } else {
            return navigationContext.navigateToDocument(currentDocument);
        }
    }

    public String getCurrentDashboardUrl() throws ClientException {
        SocialWorkspace sw = socialWorkspaceActions.getSocialWorkspace();
        DocumentModel dashboard;
        String view = DASHBOARD_VIEW_ID;
        if (sw == null) {
            dashboard = navigationContext.getCurrentDocument();
            view = "view_collaboration";
        } else {
            dashboard = documentManager.getDocument(new PathRef(sw.getDashboardSpacesRootPath()));
        }

        return restHelper.getDocumentUrl(dashboard, view, false);
    }

    public String navigateToDMView() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        if (DASHBOARD_SPACES_CONTAINER_TYPE.equals(currentDocument.getType())) {
            DocumentModel superSpace = documentManager.getSuperSpace(currentDocument);
            webActions.setCurrentTabIds(MAIN_TABS_DOCUMENTS);
            return navigationContext.navigateToDocument(superSpace);
        }
        return navigationContext.navigateToDocument(currentDocument);
    }

    public String navigateToCollaborationView() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        if (isSocialWorkspace(currentDocument)) {
            SocialWorkspace socialWorkspace = toSocialWorkspace(currentDocument);
            DocumentModel dashboardSpacesRoot = documentManager.getDocument(new PathRef(
                    socialWorkspace.getDashboardSpacesRootPath()));
            webActions.setCurrentTabIds(SocialWorkspaceActions.MAIN_TABS_COLLABORATION);
            return navigationContext.navigateToDocument(dashboardSpacesRoot, COLLABORATION_VIEW_ID);
        } else if (isSocialDocument(currentDocument)) {
            webActions.setCurrentTabIds(SocialWorkspaceActions.MAIN_TABS_COLLABORATION);
            return navigationContext.navigateToDocument(currentDocument, COLLABORATION_VIEW_ID);
        } else {
            return navigationContext.navigateToDocument(currentDocument);
        }
    }

    public String navigateToArticles() throws ClientException {
        return navigateToListing(ARTICLES_VIEW);
    }

    protected String navigateToListing(String listingView) throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();

        SocialWorkspace socialWorkspace = socialWorkspaceService.getDetachedSocialWorkspace(currentDocument);
        if (socialWorkspace != null) {
            DocumentModel dashboardSpacesRoot = documentManager.getDocument(new PathRef(
                    socialWorkspace.getDashboardSpacesRootPath()));
            return navigationContext.navigateToDocument(dashboardSpacesRoot, listingView);
        } else {
            return navigationContext.navigateToDocument(currentDocument);
        }
    }

    public String createNewDocument(String type) throws ClientException {
        DocumentModel parent = documentManager.getDocument(getParentDocumentRef(type));
        navigationContext.navigateToDocument(parent);
        documentActions.createDocument(type);
        return CREATE_SOCIAL_DOCUMENT_VIEW;
    }

    public DocumentModel getChangeableSocialDocument() throws ClientException {
        if (navigationContext.getChangeableDocument() == null) {
            // by default, we admit to create a new social workspace
            createNewDocument(SOCIAL_WORKSPACE_TYPE);
        }
        return navigationContext.getChangeableDocument();
    }

    protected DocumentRef getParentDocumentRef(String type) {
        DocumentModel currentDoc = navigationContext.getCurrentDocument();
        if (isSocialWorkspaceContainer(currentDoc)) {
            return currentDoc.getRef();
        }

        SocialWorkspace socialWorkspace = socialWorkspaceActions.getSocialWorkspace();
        if (NEWS_ITEM_TYPE.equals(type)) {
            return new PathRef(socialWorkspace.getNewsItemsRootPath());
        }

        if (SOCIAL_WORKSPACE_TYPE.equals(type)) {
            return socialWorkspaceActions.getSocialWorkspaceContainer().getRef();
        }

        return socialWorkspace.getDocument().getRef();
    }

    public String createSameTypeDocument() throws ClientException {
        String type = navigationContext.getCurrentDocument().getType();
        return createNewDocument(type);
    }

    public void deleteSocialDocument(DocumentModel document) throws ClientException {
        document.followTransition(DELETE_TRANSITION);
        documentManager.saveDocument(document);
        DocumentModel parentDoc = documentManager.getDocument(document.getParentRef());
        Events.instance().raiseEvent(DOCUMENT_CHILDREN_CHANGED, parentDoc);
    }

    public String navigateToNewsItems() throws ClientException {
        return navigateToListing(NEWS_ITEMS_VIEW);
    }

    public String editSocialDocument() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        return editSocialDocument(currentDocument);
    }

    public String editSocialDocument(DocumentModel document) throws ClientException {
        if (isSocialDocument(document)) {
            return navigationContext.navigateToDocument(document, EDIT_SOCIAL_DOCUMENT_VIEW);
        } else {
            DocumentModel currentDocument = navigationContext.getCurrentDocument();
            return navigationContext.navigateToDocument(currentDocument);
        }

    }

    public String goBack() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        if (currentDocument != null) {
            navigationContext.setChangeableDocument(null);
        } else {
            navigationContext.resetCurrentContext();
            EventManager.raiseEventsOnGoingHome();
        }
        return backToDashboard();
    }

    public String saveOncreate() throws ClientException {
        documentActions.saveDocument();
        DocumentModel currentDoc = navigationContext.getCurrentDocument();
        return navigationContext.navigateToDocument(currentDoc, AFTER_SOCIAL_COLLABORATION_CREATION_VIEW);
    }

    public String updateCurrentDocument() throws ClientException {
        documentActions.updateCurrentDocument();
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        return navigationContext.navigateToDocument(currentDocument, AFTER_SOCIAL_COLLABORATION_EDITION_VIEW);
    }

    public String displayCreateSocialWorkspaceForm() throws ClientException {
        previous = navigationContext.getCurrentDocument();
        if (previous != null && DASHBOARD_SPACES_CONTAINER_TYPE.equals(previous.getType())) {
            if (documentManager.hasPermission(previous.getParentRef(), READ)) {
                previous = documentManager.getDocument(previous.getParentRef());
            }
        }
        return createNewDocument(SOCIAL_WORKSPACE_TYPE);
    }

    public String goToPreviousDocument() throws ClientException {
        if (previous != null) {
            navigationContext.setCurrentDocument(previous);
            previous = null;
            return navigateToCollaborationView();
        }
        return null;
    }

    public String getViewId() {
        return FacesContext.getCurrentInstance().getViewRoot().getViewId();
    }

    public boolean canCreateSocialWorkspace() {
        try {
            DocumentModel parent = socialWorkspaceService.getSocialWorkspaceContainer(documentManager);
            if (parent != null) {
                return (documentManager.hasPermission(parent.getRef(), ADD_CHILDREN));
            }
        } catch (Exception e) {
            log.debug(e, e);
            // do nothing, user cannot create SW
        }
        return false;
    }

    public String navigateToFiles() throws ClientException {
        return navigateToListing(FILES_VIEW);
    }

}
