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
 *     Nuxeo
 */

package org.nuxeo.ecm.social.workspace.listeners;

import static org.nuxeo.ecm.social.workspace.SocialConstants.CTX_PRINCIPALS_PROPERTY;
import static org.nuxeo.ecm.social.workspace.SocialConstants.EVENT_MEMBERS_ADDED;
import static org.nuxeo.ecm.social.workspace.SocialConstants.EVENT_MEMBERS_REMOVED;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.PostCommitEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.ui.web.tag.fn.Functions;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceComponent;
import org.nuxeo.runtime.api.Framework;

/**
 * Listener to handle added or removed social workspace members
 * 
 * @author Arnaud Kervern <akervern@nuxeo.com>
 * @since 5.4.3
 */
public class MembersManagementSocialWorkspaceListener implements
        PostCommitEventListener {

    private static Log log = LogFactory.getLog(MembersManagementSocialWorkspaceListener.class);

    private static final String TEMPLATE_REMOVED = "templates/memberNotificationRemove.ftl";

    private static final String TEMPLATE_ADDED = "templates/memberNotificationNew.ftl";

    @Override
    public void handleEvent(EventBundle eventBundle) throws ClientException {
        if (eventBundle.containsEventName(EVENT_MEMBERS_ADDED)
                || eventBundle.containsEventName(EVENT_MEMBERS_REMOVED)) {
            for (Event event : eventBundle) {
                handleEvent(event);
            }
        }
    }

    public void handleEvent(Event event) throws ClientException {
        DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
        SocialWorkspace sw = docCtx.getSourceDocument().getAdapter(
                SocialWorkspace.class);
        List<Principal> principals = (List<Principal>) docCtx.getProperty(CTX_PRINCIPALS_PROPERTY);

        if (sw == null) {
            log.info("Event is handling a non social workspace document");
            return;
        }

        OperationContext ctx = new OperationContext(docCtx.getCoreSession());
        ctx.setInput(docCtx.getSourceDocument());
        ctx.put("principalsList", buildPrincipalsString(principals));

        Expression from = Scripting.newExpression("Env[\"mail.from\"]");
        StringList to = buildRecipientsList(sw, principals);

        String subject;
        String template;
        if (EVENT_MEMBERS_ADDED.equals(event.getName())) {
            subject = "New Members into: " + sw.getTitle();
            template = TEMPLATE_ADDED;
        } else {
            subject = "Members removed in: " + sw.getTitle();
            template = TEMPLATE_REMOVED;
        }
        String message = loadTemplate(template);

        try {
            OperationChain chain = new OperationChain("SendMail");
            chain.add(SendMail.ID).set("from", from).set("to", to).set("HTML",
                    true).set("subject", subject).set("message", message);
            Framework.getLocalService(AutomationService.class).run(ctx, chain);
        } catch (Exception e) {
            log.warn("Unable to notify about a member management.");
            log.debug(e, e);
        }
    }

    private List<String> buildPrincipalsString(List<Principal> principals) {
        List<String> ret = new ArrayList<String>();
        for (Principal principal : principals) {
            ret.add(Functions.principalFullName((NuxeoPrincipal) principal));
        }
        Collections.sort(ret);
        return ret;
    }

    private StringList buildRecipientsList(SocialWorkspace socialWorkspace,
            List<Principal> principals) {
        Set<String> emails = new HashSet<String>();
        List<String> members = socialWorkspace.getMembers();

        // Cleanup members list, to remove affected user
        for (Principal principal : principals) {
            members.remove(principal.getName());
        }

        UserManager userManager = Framework.getLocalService(UserManager.class);
        for (String username : members) {
            try {
                String email = userManager.getPrincipal(username).getEmail();
                if (!StringUtils.isBlank(email)) {
                    emails.add(email);
                }
            } catch (ClientException e) {
                log.info(String.format("Trying to fetch unknown user: %s",
                        username));
                log.debug(e, e);
            }
        }

        return new StringList(emails);
    }

    private static String loadTemplate(String key) {
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
}
