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

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.nuxeo.common.collections.ScopeType;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.social.workspace.adapters.SocialDocumentAdapter;
import org.nuxeo.ecm.webapp.helpers.EventNames;

/**
 * This seam action bean is used to create or update a proxy of the current
 * social document.
 *
 * @author rlegall
 */
@Name("socialPublishingActions")
@Scope(CONVERSATION)
@Install(precedence = FRAMEWORK)
public class SocialPublishingActions implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(SocialPublishingActions.class);

    @In(create = true)
    protected transient NavigationContext navigationContext;

    // FIXME: find a better name
    protected Boolean privatelyPublish;

    /**
     * create or update a proxy of the current social document in the public
     * social section associated to its type.
     */
    public void makePublic() throws ClientException {
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
    public void restrictToSocialWorkspace() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        SocialDocumentAdapter socialDocument = currentDocument.getAdapter(SocialDocumentAdapter.class);
        socialDocument.restrictToSocialWorkspaceMembers();
        Events.instance().raiseEvent(EventNames.DOCUMENT_CHANGED,
                currentDocument);
    }

}
