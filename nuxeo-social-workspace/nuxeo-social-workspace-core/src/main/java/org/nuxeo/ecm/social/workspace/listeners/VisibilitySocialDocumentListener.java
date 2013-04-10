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
 * Nuxeo - initial API and implementation
 */
package org.nuxeo.ecm.social.workspace.listeners;

import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_CREATED;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_UPDATED;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_DOCUMENT_IS_PUBLIC_PROPERTY;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialDocument;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.DeletedDocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;
import org.nuxeo.runtime.api.Framework;

/**
 * Class to handle "Social Document" publication after creation or update. It
 * publishes documents in sections define within
 * "social-workspace-content-template-core.xml"
 *
 * @author <a href="mailto:rlegall@nuxeo.com">Ronan Le Gall</a>
 */

public class VisibilitySocialDocumentListener implements EventListener {

    public static final String ALREADY_PROCESSED = VisibilitySocialDocumentListener.class.getName();

    @Override
    public void handleEvent(Event event) throws ClientException {
        String eventName = event.getName();
        if (!DOCUMENT_CREATED.equals(eventName)
                && DOCUMENT_UPDATED.equals(eventName)) {
            return;
        }

        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }
        DocumentModel document = ((DocumentEventContext) ctx).getSourceDocument();
        if (document instanceof DeletedDocumentModel) {
            return;
        }

        if (ctx.hasProperty(ALREADY_PROCESSED)) {
            return;
        }

        if (!SocialWorkspaceHelper.isSocialDocument(document)) {
            return;
        }

        SocialWorkspace socialWorkspace = getSocialWorkspaceService().getDetachedSocialWorkspace(
                document);
        if (socialWorkspace == null) {
            // not in a social workspace
            return;
        }

        document.putContextData(ALREADY_PROCESSED, true);

        Boolean isPublic = (Boolean) document.getPropertyValue(SOCIAL_DOCUMENT_IS_PUBLIC_PROPERTY);
        updateSocialDocumentVisibility(document, isPublic == null ? false
                : isPublic);
    }

    private static void updateSocialDocumentVisibility(DocumentModel document,
            boolean isPublic) throws ClientException {
        if (isPublic) {
            toSocialDocument(document).makePublic();
        } else {
            toSocialDocument(document).restrictToMembers();
        }
    }

    private static SocialWorkspaceService getSocialWorkspaceService() {
        try {
            return Framework.getService(SocialWorkspaceService.class);
        } catch (Exception e) {
            throw new ClientRuntimeException(e);
        }
    }

}
