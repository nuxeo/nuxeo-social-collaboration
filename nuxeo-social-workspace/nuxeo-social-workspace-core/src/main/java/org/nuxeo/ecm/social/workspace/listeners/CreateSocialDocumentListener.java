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
import static org.nuxeo.ecm.social.workspace.SocialConstants.NEWS_SECTION_NAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.NEWS_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.ROOT_SECTION_NAME;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.PostCommitEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.social.workspace.SocialWorkspaceHelper;

/**
 * Class to handle "Social Document" faceted document publication after creation
 * or update. It publishes documents in sections define within
 * "social-workspace-content-template-core.xml"
 *
 * @author <a href="mailto:rlegall@nuxeo.com">Ronan Le Gall</a>
 *
 */

public class CreateSocialDocumentListener implements PostCommitEventListener {

    Log log = LogFactory.getLog(CreateSocialDocumentListener.class);

    @Override
    public void handleEvent(EventBundle events) throws ClientException {
        if (events.containsEventName(DOCUMENT_CREATED)
                || events.containsEventName(DOCUMENT_UPDATED)) {
            for (Event event : events) {
                if (isCreateOrModifiedDocEvent(event)) {
                    handleEvent(event);
                }
            }
        }
    }

    protected boolean isCreateOrModifiedDocEvent(Event event) {
        String eventName = event.getName();
        return DOCUMENT_CREATED.equals(eventName)
                || DOCUMENT_UPDATED.equals(eventName);
    }

    public void handleEvent(Event event) throws ClientException {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }
        DocumentModel socialDocument = ((DocumentEventContext) ctx).getSourceDocument();
        if (socialDocument == null) {
            return;
        }

        CoreSession session = ctx.getCoreSession();
        if (SocialWorkspaceHelper.isSocialDocumentPublishable(session,
                socialDocument)) {
            publishCommunityDocumentInPrivateSection(session, socialDocument);
        }
    }

    protected void publishCommunityDocumentInPrivateSection(
            CoreSession session, DocumentModel socialDocument)
            throws ClientException {

        String sectionName = chooseSocialSection(socialDocument);
        SocialWorkspaceHelper.publishSocialdocument(session, socialDocument,
                sectionName);

    }

    protected String chooseSocialSection(DocumentModel socialDocument) {
        String sectionPath = "";
        if (NEWS_TYPE.equals(socialDocument.getType())) {
            sectionPath = ROOT_SECTION_NAME + "/" + NEWS_SECTION_NAME;
        }
        return sectionPath;
    }

}
