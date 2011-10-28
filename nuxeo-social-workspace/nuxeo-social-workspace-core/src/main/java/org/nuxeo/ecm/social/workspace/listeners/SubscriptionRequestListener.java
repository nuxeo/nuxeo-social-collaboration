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

package org.nuxeo.ecm.social.workspace.listeners;

import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialWorkspace;
import static org.nuxeo.ecm.social.workspace.service.DefaultSubscriptionRequestHandler.PRINCIPAL_NAME_PROPERTY;
import static org.nuxeo.ecm.social.workspace.service.DefaultSubscriptionRequestHandler.SUBSCRIPTION_REQUEST_ACCEPTED_EVENT;
import static org.nuxeo.ecm.social.workspace.service.DefaultSubscriptionRequestHandler.SUBSCRIPTION_REQUEST_CREATED_EVENT;
import static org.nuxeo.ecm.social.workspace.service.DefaultSubscriptionRequestHandler.SUBSCRIPTION_REQUEST_REJECTED_EVENT;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.PostCommitEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceEmailNotifier;

/**
 * Listener handling Subscription requests to send emails to administrators or
 * users.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class SubscriptionRequestListener implements PostCommitEventListener {

    private final SocialWorkspaceEmailNotifier emailNotifier = new SocialWorkspaceEmailNotifier();

    @Override
    public void handleEvent(EventBundle eventBundle) throws ClientException {
        if (eventBundle.containsEventName(SUBSCRIPTION_REQUEST_CREATED_EVENT)
                || eventBundle.containsEventName(SUBSCRIPTION_REQUEST_ACCEPTED_EVENT)
                || eventBundle.containsEventName(SUBSCRIPTION_REQUEST_REJECTED_EVENT)) {
            for (Event event : eventBundle) {
                handleEvent(event);
            }
        }
    }

    private void handleEvent(Event event) throws ClientException {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }
        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();
        CoreSession session = docCtx.getCoreSession();

        if (SUBSCRIPTION_REQUEST_CREATED_EVENT.equals(event.getName())) {
            emailNotifier.notifyAdministratorsForNewSubscriptionRequest(
                    session, doc);
        } else if (SUBSCRIPTION_REQUEST_ACCEPTED_EVENT.equals(event.getName())) {
            String principalName = (String) docCtx.getProperty(PRINCIPAL_NAME_PROPERTY);
            emailNotifier.notifyUserForSubscriptionRequestAccepted(session,
                    toSocialWorkspace(doc), principalName);
        } else if (SUBSCRIPTION_REQUEST_REJECTED_EVENT.equals(event.getName())) {
            String principalName = (String) docCtx.getProperty(PRINCIPAL_NAME_PROPERTY);
            emailNotifier.notifyUserForSubscriptionRequestRejected(session,
                    toSocialWorkspace(doc), principalName);
        }
    }

}
