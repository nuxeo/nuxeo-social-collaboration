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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.PostCommitEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.social.workspace.SocialConstants;
import org.nuxeo.ecm.social.workspace.SocialWorkspaceHelper;

/**
 * @author <a href="mailto:rlegall@nuxeo.com">Ronan Le Gall</a>
 *
 */

public class CreateSocialDocumentListener implements PostCommitEventListener {

    private static final int NUMBER_OF_SOCIAL_SECTIONS_IN_THE_ROOT_SOCIAL_SECTION = 2;

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
        DocumentModel doc = ((DocumentEventContext) ctx).getSourceDocument();
        if (doc == null) {
            return;
        }

        CoreSession session = ctx.getCoreSession();
        if (SocialWorkspaceHelper.couldDocumentBePublished(session, doc)) {
            publishCommunityDocumentInPrivateSection(session, doc);
        }
    }

    protected void publishCommunityDocumentInPrivateSection(
            CoreSession session, DocumentModel news) throws ClientException {
        // the news are created as direct children of the socialworkspace
        Path socialWorkspacePath = news.getPath().removeLastSegments(1);
        String queryGettingNewsSection = String.format(
                "select * from %s where ecm:path STARTSWITH '%s/%s'",
                SocialConstants.SOCIAL_PUBLICATION_TYPE, socialWorkspacePath,
                SocialConstants.ROOT_SECTION_NAME);

        DocumentModelList docs = session.query(queryGettingNewsSection);
        if (docs.size() == NUMBER_OF_SOCIAL_SECTIONS_IN_THE_ROOT_SOCIAL_SECTION) {
            DocumentModel privateNewsSection = docs.get(0);
            session.publishDocument(news, privateNewsSection);
            session.save();
            if (log.isDebugEnabled()) {
                String msg = String.format(
                        "The News named \"%s\" have been published in the private section called \"%s\"",
                        news.getName(), privateNewsSection.getName());
                log.debug(msg);
            }
        } else {
            if (log.isDebugEnabled()) {
                String msg = String.format(
                        "The News named \"%s\" can't be published in the private section ",
                        news.getName());
                log.debug(msg);
            }
        }
    }

}
