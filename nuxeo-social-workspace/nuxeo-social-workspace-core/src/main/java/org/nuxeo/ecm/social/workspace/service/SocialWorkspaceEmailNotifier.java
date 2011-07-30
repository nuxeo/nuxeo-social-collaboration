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

import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialWorkspace;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.operations.notification.SendMail;
import org.nuxeo.ecm.automation.core.scripting.Expression;
import org.nuxeo.ecm.automation.core.scripting.Scripting;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.adapters.SubscriptionRequest;
import org.nuxeo.runtime.api.Framework;

/**
 * Sends notifications for things related to Social Workspaces
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class SocialWorkspaceEmailNotifier {

    private static final Log log = LogFactory.getLog(SocialWorkspaceEmailNotifier.class);

    private static final String TEMPLATE_JOIN_REQUEST_RECEIVED = "templates/joinRequestReceived.ftl";

    private static final String TEMPLATE_JOIN_REQUEST_ACCEPTED = "templates/joinRequestAccepted.ftl";

    private static final String TEMPLATE_JOIN_REQUEST_REJECTED = "templates/joinRequestRejected.ftl";

    private AutomationService automationService;

    private UserManager userManager;

    /**
     * Sends email to the administrators of the Social Workspace
     * referenced by the {@code subscriptionRequest}.
     */
    public void notifyAdministratorsForNewSubscriptionRequest(
            CoreSession session, DocumentModel subscriptionRequest)
            throws ClientException {
        SubscriptionRequest requestAdapter = subscriptionRequest.getAdapter(SubscriptionRequest.class);
        DocumentModel socialWorkspace = session.getDocument(new IdRef(
                requestAdapter.getInfo()));
        String adminGroupName = toSocialWorkspace(socialWorkspace).getAdministratorsGroupName();
        NuxeoGroup adminGroup = getUserManager().getGroup(adminGroupName);
        List<String> admins = adminGroup.getMemberUsers();
        if (admins == null || admins.isEmpty()) {
            log.warn(String.format(
                    "No admin users for social workspace %s (%s) ",
                    socialWorkspace.getTitle(),
                    socialWorkspace.getPathAsString()));
            return;
        }
        StringList toList = new StringList();
        for (String adminName : admins) {
            NuxeoPrincipal admin = getUserManager().getPrincipal(adminName);
            String email = admin.getEmail();
            if (email != null) {
                toList.add(email);
            }
        }

        if (toList.isEmpty()) {
            log.warn("no admin email found ...");
            return;
        }

        Expression subject = Scripting.newTemplate("Join request received from ${Context.principal.firstName} ${Context.principal.lastName} ");
        String template = loadTemplate(TEMPLATE_JOIN_REQUEST_RECEIVED);

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(socialWorkspace);
        OperationChain chain = new OperationChain("sendEMail");
        chain.add(SendMail.ID).set("from", "admin@nuxeo.org").set("to", toList).set(
                "subject", subject).set("HTML", true).set("message", template);
        try {
            getAutomationService().run(ctx, chain);
        } catch (Exception e) {
            String message = String.format(
                    "Failed to notify administrators of Social Workspace '%s': %s",
                    socialWorkspace.getPath(), e.getMessage());
            log.warn(message);
            log.debug(e, e);
        }
    }

    /**
     * Sends email to the user referenced by the {@code principalName}
     * when his request is accepted.
     */
    public void notifyUserForSubscriptionRequestAccepted(CoreSession sesion,
            SocialWorkspace socialWorkspace, String principalName)
            throws ClientException {
        notifyUser(sesion, socialWorkspace, principalName, true);
    }

    /**
     * Sends email to the user referenced by the {@code principalName}
     * when his request is rejected.
     */
    public void notifyUserForSubscriptionRequestRejected(CoreSession sesion,
            SocialWorkspace socialWorkspace, String principalName)
            throws ClientException {
        notifyUser(sesion, socialWorkspace, principalName, false);
    }

    private void notifyUser(CoreSession session,
            SocialWorkspace socialWorkspace, String principalName,
            boolean accepted) throws ClientException {
        NuxeoPrincipal principal = getUserManager().getPrincipal(principalName);
        String email = principal.getEmail();

        if (email == null || email.trim().length() == 0) {
            log.debug("email not defined for user:" + principalName);
            return;
        }

        String subject;
        String template;
        if (accepted) {
            template = loadTemplate(TEMPLATE_JOIN_REQUEST_ACCEPTED);
            subject = "Join request accepted";
        } else {
            template = loadTemplate(TEMPLATE_JOIN_REQUEST_REJECTED);
            subject = "Join request rejected";
        }

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(socialWorkspace.getDocument());
        OperationChain chain = new OperationChain("sendEMail");
        chain.add(SendMail.ID).set("from", "admin@nuxeo.org").set("to", email).set(
                "subject", subject).set("HTML", true).set("message", template);
        try {
            getAutomationService().run(ctx, chain);
        } catch (Exception e) {
            String message = String.format(
                    "Failed to notify '%s' user for Social Workspace '%s': %s",
                    principal, socialWorkspace.getPath(), e.getMessage());
            log.warn(message);
            log.debug(e, e);
        }
    }

    private String loadTemplate(String key) {
        InputStream io = SocialWorkspaceComponent.class.getClassLoader().getResourceAsStream(
                key);
        if (io != null) {
            try {
                return FileUtils.read(io);
            } catch (IOException e) {
                throw new ClientRuntimeException(e);
            } finally {
                try {
                    io.close();
                } catch (IOException e) {
                    // nothing to do
                }
            }
        }
        return null;
    }

    private AutomationService getAutomationService() {
        if (automationService == null) {
            try {
                automationService = Framework.getService(AutomationService.class);
            } catch (Exception e) {
                throw new ClientRuntimeException(e);
            }
        }
        return automationService;
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

}
