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

import static org.jboss.seam.ScopeType.PAGE;
import static org.jboss.seam.annotations.Install.FRAMEWORK;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.nuxeo.common.collections.ScopeType;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.api.WebActions;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.adapters.SocialDocumentAdapter;
import org.nuxeo.ecm.social.workspace.adapters.SocialDocumentAdapterImpl;
import org.nuxeo.ecm.webapp.helpers.EventNames;

/**
 * This seam action bean is used to create or update a proxy of the current
 * social document.
 * 
 * @author rlegall
 */
@Name("SocialPublishing")
@Scope(PAGE)
@Install(precedence = FRAMEWORK)
public class SocialPublishingActions {

    @In(create = true)
    protected UserManager userManager;

    @In(create = true)
    protected NavigationContext navigationContext;

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient WebActions webActions;

    // FIXME: find a better name
    protected Boolean privatelyPublish;

    /**
     * create or update a proxy of the current social document in the public
     * social section associated to its type.
     */
    public void publishAsPublic() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        SocialDocumentAdapter socialDocument = currentDocument.getAdapter(SocialDocumentAdapter.class);
        socialDocument.makePublic();
        Events.instance().raiseEvent(EventNames.DOCUMENT_CHANGED,
                currentDocument);
    }

    /**
     * create or update a proxy of the current social document in the private
     * social section associated to its type.
     */
    public void publishAsPrivate() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        SocialDocumentAdapter socialDocument = currentDocument.getAdapter(SocialDocumentAdapter.class);
        socialDocument.restrictToSocialWorkspaceMembers();
        Events.instance().raiseEvent(EventNames.DOCUMENT_CHANGED,
                currentDocument);
    }

    public boolean isPrivate() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();

        SocialDocumentAdapter socialDocument = currentDocument.getAdapter(SocialDocumentAdapter.class);
        return socialDocument.isRestrictedToMembers();
    }

    /**
     * Used to specify if the current social document is publish in private
     * social section.
     * 
     * @return true if the current social document got a proxy in a private
     *         social section or if it's newly created, false if the current
     *         social document got a proxy in a public social section.
     */
    public boolean isPrivatelyPublish() throws ClientException {
        if (privatelyPublish == null) {
            privatelyPublish = Boolean.valueOf(isPrivate());
        }
        return privatelyPublish.booleanValue();
    }

    /**
     * Sets the type of publication for the current social document. True to
     * publish it in a private social section, false to publish it in a public
     * social section.
     * 
     * @param privatelyPublish true to choose a private publication, false to
     *            choose a public publication.
     */
    // FIXME: it this really the intended behaviour?
    // FIXME: find a better name
    public void setPrivatelyPublish(boolean privatelyPublish) {
        privatelyPublish = new Boolean(privatelyPublish);
        if (privatelyPublish) {
            removeMarkAsPublic();
        } else {
            markAsPublic();
        }
    }

    protected void removeMarkAsPublic() {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        if (currentDocument.getContextData().containsKey("Public")) {
            currentDocument.getContextData().remove("Public");
        }
    }

    protected void markAsPublic() {
        DocumentModel documentToMark = navigationContext.getChangeableDocument();
        if (documentToMark == null) {
            documentToMark = navigationContext.getCurrentDocument();
        }

        documentToMark.putContextData(ScopeType.REQUEST, "Public", Boolean.TRUE);
    }

}
