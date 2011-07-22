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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import net.sf.json.JSONObject;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.impl.blob.InputStreamBlob;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.SocialGroupsManagement;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 */
@Operation(id = GetUserSocialWorkspaceStatus.ID, category = Constants.CAT_EXECUTION, label = "Get User Social Workspace Status", description = "Return user status")
public class GetUserSocialWorkspaceStatus {

    enum Status {
        NOT_MEMBER, REQUEST_PENDING, MEMBER
    }

    public static final String ID = "SocialWorkspace.UserStatus";

    protected OperationContext ctx;

    @Context
    protected UserManager userManager;

    @Context
    protected CoreSession session;

    @Param(name = "socialWorkspacePath", required = true)
    protected String socialWorkspacePath;

    @OperationMethod
    public Blob run() throws Exception {
        String currentUser = session.getPrincipal().getName();
        DocumentModel sws = session.getDocument(new PathRef(socialWorkspacePath));
        if (SocialGroupsManagement.isMember(sws, currentUser)) {
            return buildResponse(sws, Status.MEMBER);
        }
        if (SocialGroupsManagement.isRequestPending(sws, currentUser)) {
            return buildResponse(sws, Status.REQUEST_PENDING);
        }
        return buildResponse(sws, Status.NOT_MEMBER);
    }

    protected Blob buildResponse(DocumentModel sws, Status status)
            throws ClientException, UnsupportedEncodingException {
        JSONObject obj = new JSONObject();
        obj.element("status", status);
        obj.element("title", sws.getPropertyValue("dc:title"));
        obj.element("description", sws.getPropertyValue("dc:description"));
        return new InputStreamBlob(new ByteArrayInputStream(
                obj.toString().getBytes("UTF-8")), "application/json");
    }

}
