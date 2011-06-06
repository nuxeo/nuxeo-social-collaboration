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
 *     eugen
 */
package org.nuxeo.ecm.social.workspace;

import static org.nuxeo.ecm.social.workspace.SocialConstants.REQUEST_TYPE_JOIN;

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
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.adapters.RequestAdapter;
import org.nuxeo.runtime.api.Framework;

/**
 * Helper class for group members management
 *
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 */
public class SocialGroupsManagement {

    public static final String TEMPLATE_JOIN_REQUEST_RECEIVED = "templates/joinRequestReceived.ftl";

    public static final String TEMPLATE_JOIN_REQUEST_ACCEPTED = "templates/joinRequestAccepted.ftl";

    public static final String TEMPLATE_JOIN_REQUEST_REJECTED = "templates/joinRequestRejected.ftl";

    static AutomationService automationService = null;

    static UserManager userManager = null;

    private static final Log log = LogFactory.getLog(SocialGroupsManagement.class);

    private SocialGroupsManagement() {
    }

    public static boolean acceptMember(DocumentModel sws, String user)
            throws Exception {
        DocumentModel principal = getUserManager().getUserModel(user);

        @SuppressWarnings("unchecked")
        List<String> groups = (List<String>) principal.getProperty(
                getUserManager().getUserSchemaName(), "groups");
        if (groups.contains(SocialWorkspaceHelper.getCommunityAdministratorsGroupName(sws))) {
            log.info(String.format("%s is already an administrator of %s (%s)",
                    user, sws.getTitle(), sws.getPathAsString()));
            return false;
        }
        String membersGroup = SocialWorkspaceHelper.getCommunityMembersGroupName(sws);
        if (groups.contains(membersGroup)) { // already a member
            log.info(String.format("%s is already a member of %s (%s)", user,
                    sws.getTitle(), sws.getPathAsString()));
            return false;
        }
        groups.add(membersGroup);
        principal.setProperty(getUserManager().getUserSchemaName(), "groups",
                groups);
        getUserManager().updateUser(principal);
        return true;
    }

    public static boolean isMember(DocumentModel sws, String user)
            throws Exception {
        NuxeoPrincipal nuxeoPrincipal = getUserManager().getPrincipal(user);
        List<String> groups = nuxeoPrincipal.getGroups();
        if (groups.contains(SocialWorkspaceHelper.getCommunityAdministratorsGroupName(sws))) {
            return true;
        }
        String membersGroup = SocialWorkspaceHelper.getCommunityMembersGroupName(sws);
        if (groups.contains(membersGroup)) {
            return true;
        }
        return false;
    }

    public static boolean isRequestPending(DocumentModel sws, String user)
            throws Exception {
        CoreSession session = CoreInstance.getInstance().getSession(
                sws.getSessionId());
        String queryTemplate = "SELECT * FROM Request WHERE req:type = '%s' AND req:username = '%s' AND req:info = '%s'";
        String query = String.format(queryTemplate, REQUEST_TYPE_JOIN, user,
                sws.getId());
        DocumentModelList list = session.query(query);
        if (list != null && list.size() > 0) {
            return true;
        }
        return false;
    }

    public static void notifyUser(DocumentModel socialWorkspace,
            String username, boolean accepted) throws Exception {
        CoreSession session = CoreInstance.getInstance().getSession(
                socialWorkspace.getSessionId());
        NuxeoPrincipal principal = getUserManager().getPrincipal(username);
        String email = principal.getEmail();

        if (email == null || email.trim().length() == 0) {
            log.debug("email not defined for user:" + username);
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
        ctx.setInput(socialWorkspace);
        OperationChain chain = new OperationChain("sendEMail");
        chain.add(SendMail.ID).set("from", "admin@nuxeo.org").set("to", email).set(
                "subject", subject).set("HTML", true).set("message", template);
        try {
            getAutomationService().run(ctx, chain);
        } catch (Exception e) {
            log.warn("failed to notify users", e);
        }
    }

    public static void notifyAdmins(DocumentModel request) throws Exception {
        CoreSession session = CoreInstance.getInstance().getSession(
                request.getSessionId());
        RequestAdapter requestAdapter = request.getAdapter(RequestAdapter.class);

        DocumentModel socialWorkspace = session.getDocument(new IdRef(
                requestAdapter.getInfo()));
        String adminGroupName = SocialWorkspaceHelper.getCommunityAdministratorsGroupName(socialWorkspace);
        NuxeoGroup adminGroup = getUserManager().getGroup(adminGroupName);
        List<String> admins = adminGroup.getMemberUsers();
        if (admins == null || admins.size() == 0) {
            log.warn(String.format(
                    "No admin users for social workspace %s (%s) ",
                    socialWorkspace.getTitle(),
                    socialWorkspace.getPathAsString()));
            return;
        }
        StringList toList = new StringList();
        for (String adminName : admins) {
            NuxeoPrincipal admin = userManager.getPrincipal(adminName);
            String email = admin.getEmail();
            if (email != null) {
                toList.add(email);
            }
        }

        if (toList.size() == 0) {
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
            log.warn("failed to notify admins", e);
        }
    }

    private static AutomationService getAutomationService() throws Exception {
        if (automationService == null) {
            automationService = Framework.getService(AutomationService.class);
        }
        return automationService;
    }

    private static UserManager getUserManager() throws Exception {
        if (userManager == null) {
            userManager = Framework.getService(UserManager.class);
        }
        return userManager;
    }

    private static String loadTemplate(String key) throws Exception {
        InputStream io = SocialGroupsManagement.class.getClassLoader().getResourceAsStream(
                key);
        if (io != null) {
            try {
                return FileUtils.read(io);
            } finally {
                io.close();
            }
        }
        return null;
    }

}
