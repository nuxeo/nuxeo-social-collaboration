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

package org.nuxeo.ecm.social.workspace.service;

import static org.nuxeo.ecm.social.workspace.SocialConstants.SUBSCRIPTION_REQUEST_ACCEPT_TRANSITION;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SUBSCRIPTION_REQUEST_INFO_PROPERTY;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SUBSCRIPTION_REQUEST_REJECT_TRANSITION;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SUBSCRIPTION_REQUEST_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SUBSCRIPTION_REQUEST_TYPE_JOIN;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SUBSCRIPTION_REQUEST_TYPE_PROPERTY;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SUBSCRIPTION_REQUEST_USERNAME_PROPERTY;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SUBSCRIPTION_REQUEST_USER_EMAIL_PROPERTY;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.adapters.SubscriptionRequest;
import org.nuxeo.runtime.api.Framework;

/**
 * Default implementation of @{link SubscriptionRequestHandler}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class DefaultSubscriptionRequestHandler implements
        SubscriptionRequestHandler {

    private static final Log log = LogFactory.getLog(DefaultSubscriptionRequestHandler.class);

    public static final String SUBSCRIPTION_REQUESTS_ROOT_NAME = "socialWorkspaceSubscriptionRequests";

    public static final String SUBSCRIPTION_REQUESTS_ROOT_TYPE = "HiddenFolder";

    public static final String SUBSCRIPTION_REQUEST_CREATED_EVENT = "subscriptionRequestCreated";

    public static final String SUBSCRIPTION_REQUEST_ACCEPTED_EVENT = "subscriptionRequestAccepted";

    public static final String SUBSCRIPTION_REQUEST_REJECTED_EVENT = "subscriptionRequestRejected";

    public static final String PRINCIPAL_NAME_PROPERTY = "principalName";

    private UserManager userManager;

    private EventService eventService;

    @Override
    public void handleSubscriptionRequestFor(
            final SocialWorkspace socialWorkspace, final Principal principal) {
        try {
            String repositoryName = socialWorkspace.getDocument().getRepositoryName();
            new UnrestrictedSessionRunner(repositoryName, principal.getName()) {
                @Override
                public void run() throws ClientException {
                    handleSubscriptionRequestFor(session, socialWorkspace,
                            principal);
                }
            }.runUnrestricted();
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    private void handleSubscriptionRequestFor(CoreSession session,
            SocialWorkspace socialWorkspace, Principal principal)
            throws ClientException {
        if (socialWorkspace.mustApproveSubscription()) {
            if (isSubscriptionRequestPending(socialWorkspace, principal)) {
                return;
            }

            String subscriptionRequestName = socialWorkspace.getId() + "-"
                    + principal.getName();
            DocumentModel subscriptionRequestsRoot = getSubscriptionRequestsRoot(
                    session, socialWorkspace);
            DocumentModel request = session.createDocumentModel(
                    subscriptionRequestsRoot.getPathAsString(),
                    subscriptionRequestName, SUBSCRIPTION_REQUEST_TYPE);
            request.setPropertyValue(SUBSCRIPTION_REQUEST_USERNAME_PROPERTY,
                    principal.getName());
            NuxeoPrincipal nuxeoPrincipal = (NuxeoPrincipal) principal;
            request.setPropertyValue(SUBSCRIPTION_REQUEST_USER_EMAIL_PROPERTY,
                    nuxeoPrincipal.getEmail());
            request.setPropertyValue(SUBSCRIPTION_REQUEST_TYPE_PROPERTY,
                    SUBSCRIPTION_REQUEST_TYPE_JOIN);
            request.setPropertyValue(SUBSCRIPTION_REQUEST_INFO_PROPERTY,
                    socialWorkspace.getId());
            request = session.createDocument(request);
            session.save();

            EventContext ctx = new DocumentEventContext(session, null, request);
            getEventService().fireEvent(SUBSCRIPTION_REQUEST_CREATED_EVENT, ctx);
        } else {
            if (socialWorkspace.addMember(principal)) {
                EventContext ctx = new DocumentEventContext(session, null,
                        socialWorkspace.getDocument());
                ctx.setProperty(PRINCIPAL_NAME_PROPERTY, principal.getName());
                getEventService().fireEvent(
                        SUBSCRIPTION_REQUEST_ACCEPTED_EVENT, ctx);
            }
        }
    }

    /**
     * Returns the dashboard management document, creates it if needed.
     */
    private static DocumentModel getSubscriptionRequestsRoot(
            CoreSession session, SocialWorkspace socialWorkspace)
            throws ClientException {
        String subscriptionRequestsRootPath = new Path(
                socialWorkspace.getPath()).append(
                SUBSCRIPTION_REQUESTS_ROOT_NAME).toString();
        DocumentRef subscriptionRequestsRootRef = new PathRef(
                subscriptionRequestsRootPath);

        if (session.exists(subscriptionRequestsRootRef)) {
            return session.getDocument(subscriptionRequestsRootRef);
        } else {
            DocumentModel subscriptionRequestsRoot = session.createDocumentModel(
                    socialWorkspace.getPath(), SUBSCRIPTION_REQUESTS_ROOT_NAME,
                    SUBSCRIPTION_REQUESTS_ROOT_TYPE);
            return session.createDocument(subscriptionRequestsRoot);
        }
    }

    private UserManager getUserManager() {
        if (userManager == null) {
            try {
                userManager = Framework.getService(UserManager.class);
            } catch (Exception e) {
                throw new ClientRuntimeException(e);
            }
        }
        return userManager;
    }

    @Override
    public boolean isSubscriptionRequestPending(
            final SocialWorkspace socialWorkspace, final Principal principal) {
        final List<DocumentModel> subscriptionRequests = new ArrayList<DocumentModel>();
        try {
            String repositoryName = socialWorkspace.getDocument().getRepositoryName();
            new UnrestrictedSessionRunner(repositoryName, principal.getName()) {
                @Override
                public void run() throws ClientException {
                    String queryTemplate = "SELECT * FROM SubscriptionRequest WHERE req:type = '%s' AND req:username = '%s' AND req:info = '%s'";
                    String query = String.format(queryTemplate,
                            SUBSCRIPTION_REQUEST_TYPE_JOIN,
                            principal.getName(), socialWorkspace.getId());
                    subscriptionRequests.addAll(session.query(query));
                }
            }.runUnrestricted();
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
        return !subscriptionRequests.isEmpty();
    }

    @Override
    public void acceptSubscriptionRequest(
            final SocialWorkspace socialWorkspace,
            final SubscriptionRequest subscriptionRequest) {

        try {
            String repositoryName = subscriptionRequest.getDocument().getRepositoryName();
            new UnrestrictedSessionRunner(repositoryName) {
                @Override
                public void run() throws ClientException {
                    acceptSubscriptionRequest(session, socialWorkspace,
                            subscriptionRequest);
                }
            }.runUnrestricted();
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    private void acceptSubscriptionRequest(CoreSession session,
            SocialWorkspace socialWorkspace,
            SubscriptionRequest subscriptionRequest) throws ClientException {
        String principalName = subscriptionRequest.getUsername();
        if (socialWorkspace.addMember(getUserManager().getPrincipal(
                principalName))) {
            DocumentModel request = session.getDocument(subscriptionRequest.getDocument().getRef());
            request.followTransition(SUBSCRIPTION_REQUEST_ACCEPT_TRANSITION);
            session.saveDocument(request);
            session.save();
            EventContext ctx = new DocumentEventContext(session, null,
                    socialWorkspace.getDocument());
            ctx.setProperty(PRINCIPAL_NAME_PROPERTY, principalName);
            getEventService().fireEvent(SUBSCRIPTION_REQUEST_ACCEPTED_EVENT,
                    ctx);
        }
    }

    @Override
    public void rejectSubscriptionRequest(
            final SocialWorkspace socialWorkspace,
            final SubscriptionRequest subscriptionRequest) {
        try {
            String repositoryName = subscriptionRequest.getDocument().getRepositoryName();
            new UnrestrictedSessionRunner(repositoryName) {
                @Override
                public void run() throws ClientException {
                    rejectSubscriptionRequest(session, socialWorkspace,
                            subscriptionRequest);
                }
            }.runUnrestricted();
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    private void rejectSubscriptionRequest(CoreSession session,
            SocialWorkspace socialWorkspace,
            SubscriptionRequest subscriptionRequest) throws ClientException {
        String principalName = subscriptionRequest.getUsername();
        DocumentModel request = session.getDocument(subscriptionRequest.getDocument().getRef());
        request.followTransition(SUBSCRIPTION_REQUEST_REJECT_TRANSITION);
        session.saveDocument(request);
        session.save();
        EventContext ctx = new DocumentEventContext(session, null,
                socialWorkspace.getDocument());
        ctx.setProperty(PRINCIPAL_NAME_PROPERTY, principalName);
        getEventService().fireEvent(SUBSCRIPTION_REQUEST_REJECTED_EVENT, ctx);
    }

    private EventService getEventService() {
        if (eventService == null) {
            try {
                eventService = Framework.getService(EventService.class);
            } catch (Exception e) {
                throw new ClientRuntimeException(e);
            }
        }
        return eventService;
    }

}
