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

import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_REQUEST_INFO;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_REQUEST_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_REQUEST_USERNAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_SOCIAL_IS_RESTRICTED;
import static org.nuxeo.ecm.social.workspace.SocialConstants.REQUEST_ROOT_NAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.REQUEST_TYPE_JOIN;
import static org.nuxeo.ecm.social.workspace.SocialConstants.REQUEST_DOC_TYPE;

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
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.SocialGroupsManagement;

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

        boolean isRestricted = (Boolean) sws.getPropertyValue(FIELD_SOCIAL_IS_RESTRICTED);

        if (isRestricted) {
            if (SocialGroupsManagement.isRequestPending(sws, currentUser)) {
                log.debug(String.format(
                        "there is already a join request from '%s' on '%s' ",
                        currentUser, sws.getPathAsString()));
                return;
            }

            DocumentRef requestRootPath = new PathRef(sws.getPathAsString(),
                    REQUEST_ROOT_NAME);
            DocumentModel request = session.createDocumentModel(
                    requestRootPath.toString(), currentUser, REQUEST_DOC_TYPE);
            request.setPropertyValue(FIELD_REQUEST_USERNAME, currentUser);
            request.setPropertyValue(FIELD_REQUEST_TYPE, REQUEST_TYPE_JOIN);
            request.setPropertyValue(FIELD_REQUEST_INFO, sws.getId());
            request = session.createDocument(request);
            session.save();
            SocialGroupsManagement.notifyAdmins(request);
        } else { // restricted social workspace ; request will be validated by
                 // admin
            SocialGroupsManagement.acceptMember(sws, currentUser);
            SocialGroupsManagement.notifyUser(sws, currentUser, true);
        }
    }

}
