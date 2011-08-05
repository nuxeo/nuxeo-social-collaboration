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
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.PostCommitEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper;

/**
 * Class to handle "Social Document" publication after creation or update. It
 * publishes documents in sections define within
 * "social-workspace-content-template-core.xml"
 *
 * @author <a href="mailto:rlegall@nuxeo.com">Ronan Le Gall</a>
 */

public class VisibilitySocialDocumentListener implements
        PostCommitEventListener {

    public static final String ALREADY_PROCESSED = VisibilitySocialDocumentListener.class.getName();

    @Override
    public void handleEvent(EventBundle events) throws ClientException {
        if (events.containsEventName(DOCUMENT_CREATED)
                || events.containsEventName(DOCUMENT_UPDATED)) {
            for (Event event : events) {
                if (DOCUMENT_CREATED.equals(event.getName())
                        || DOCUMENT_UPDATED.equals(event.getName())) {
                    handleEvent(event);
                }
            }
        }
    }

    public static void handleEvent(Event event) throws ClientException {

        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }
        DocumentModel document = ((DocumentEventContext) ctx).getSourceDocument();
        if (ctx.hasProperty(ALREADY_PROCESSED)) {
            return;
        }

        if (!SocialWorkspaceHelper.isSocialDocument(document)) {
            return;
        }

        document.putContextData(ALREADY_PROCESSED, true);

        Boolean isPublic = (Boolean) document.getPropertyValue(SOCIAL_DOCUMENT_IS_PUBLIC_PROPERTY);
        updateSocialDocumentVisibility(document, isPublic == null ? false
                : isPublic);
    }

    protected static void updateSocialDocumentVisibility(DocumentModel document,
            boolean isPublic) throws ClientException {
        if (isPublic) {
            toSocialDocument(document).makePublic();
        } else {
            toSocialDocument(document).restrictToMembers();
        }
    }

}
