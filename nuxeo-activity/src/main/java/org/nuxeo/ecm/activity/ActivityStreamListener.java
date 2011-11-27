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
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.nuxeo.ecm.activity;

import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_CREATED;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_REMOVED;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_UPDATED;
import static org.nuxeo.ecm.core.schema.FacetNames.HIDDEN_IN_NAVIGATION;

import java.security.Principal;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.PostCommitEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.event.impl.ShallowDocumentModel;
import org.nuxeo.runtime.api.Framework;

/**
 * Listener called asynchronously to save events as activities through the
 * {@link ActivityStreamService}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class ActivityStreamListener implements PostCommitEventListener {

    @Override
    public void handleEvent(EventBundle events) throws ClientException {
        if (events.containsEventName(DOCUMENT_CREATED)
                || events.containsEventName(DOCUMENT_UPDATED)
                || events.containsEventName(DOCUMENT_REMOVED)) {
            for (Event event : events) {
                handleEvent(event);
            }
        }
    }

    private void handleEvent(Event event) throws ClientException {
        EventContext eventContext = event.getContext();
        if (eventContext instanceof DocumentEventContext) {
            if (DOCUMENT_CREATED.equals(event.getName())
                    || DOCUMENT_REMOVED.equals(event.getName())
                    || DOCUMENT_UPDATED.equals(event.getName())) {
                DocumentEventContext docEventContext = (DocumentEventContext) eventContext;
                DocumentModel doc = docEventContext.getSourceDocument();
                if (doc instanceof ShallowDocumentModel
                        || doc.hasFacet(HIDDEN_IN_NAVIGATION)) {
                    // Not really interested if document cannot be reconnected
                    // or if not visible
                    return;
                }
                Activity activity = toActivity(docEventContext, event);
                ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
                activityStreamService.addActivity(activity);
            }
        }
    }

    private Activity toActivity(DocumentEventContext docEventContext,
            Event event) {
        Principal principal = docEventContext.getPrincipal();
        DocumentModel doc = docEventContext.getSourceDocument();
        return new ActivityBuilder().actor(
                ActivityHelper.createUserActivityObject(principal)).displayActor(
                ActivityHelper.generateDisplayName(principal)).verb(
                event.getName()).object(
                ActivityHelper.createDocumentActivityObject(doc)).displayObject(
                ActivityHelper.getDocumentTitle(doc)).target(
                ActivityHelper.createDocumentActivityObject(
                        doc.getRepositoryName(), doc.getParentRef().toString())).displayTarget(
                getDocumentTitle(docEventContext.getCoreSession(),
                        doc.getParentRef())).build();
    }

    private String getDocumentTitle(CoreSession session, DocumentRef docRef) {
        try {
            DocumentModel doc = session.getDocument(docRef);
            return ActivityHelper.getDocumentTitle(doc);
        } catch (ClientException e) {
            return docRef.toString();
        }
    }

}
