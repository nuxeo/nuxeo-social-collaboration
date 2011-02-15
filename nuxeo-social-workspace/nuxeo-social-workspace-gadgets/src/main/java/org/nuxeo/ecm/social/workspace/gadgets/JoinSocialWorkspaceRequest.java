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
package org.nuxeo.ecm.social.workspace.gadgets;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.SocialWorkspaceHelper;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 *
 */

@Operation(id = JoinSocialWorkspaceRequest.ID, category = Constants.CAT_EXECUTION, label = "Join Social Workspace", description = "Operation that will handle join request")
public class JoinSocialWorkspaceRequest {

    public static final String ID = "Social.Join";

    private static final Log log = LogFactory.getLog(JoinSocialWorkspaceRequest.class);

    @Context
    protected OperationContext ctx;

    @Context
    protected UserManager userManager;

    @Context
    protected CoreSession session;

    @Param(name = "socialWorkspacePath", required = true)
    protected String socialWorkspacePath;

    @OperationMethod
    public void run() throws Exception {
        if (socialWorkspacePath == null
                || socialWorkspacePath.trim().length() == 0) { // nothing to do
            return;
        }
        DocumentModel sws = session.getDocument(new PathRef(socialWorkspacePath));
        String currentUser = session.getPrincipal().getName();

        DocumentModel principal = userManager.getUserModel(currentUser);
        List<String> groups = (List<String>) principal.getProperty(
                userManager.getUserSchemaName(), "groups");

        if (groups.contains(SocialWorkspaceHelper.getCommunityAdministratorsGroupName(sws))) { // already
                                                                                               // an
                                                                                               // admin
            return;
        }

        String membersGroup = SocialWorkspaceHelper.getCommunityMembersGroupName(sws);
        if (groups.contains(membersGroup)) { // already a member
            return;
        }

        // TODO replace this code with join request validation
        groups.add(membersGroup);
        principal.setProperty(userManager.getUserSchemaName(), "groups", groups);
        userManager.updateUser(principal);

    }

}
