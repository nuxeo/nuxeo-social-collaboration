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

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.SocialGroupsManagement;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 *
 */

@Operation(id = GetUserSocialWorkspaceStatute.ID, category = Constants.CAT_EXECUTION, label = "Get User Social Workspace Statute", description = "Return user statut")
public class GetUserSocialWorkspaceStatute {

    enum Statute {
        NOT_MEMBER, REQUEST_PENDING, MEMBER
    };

    public static final String ID = "SocialWorkspace.Statute";

    private static final Log log = LogFactory.getLog(GetUserSocialWorkspaceStatute.class);

    @Context
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
            return buildResponse(sws, Statute.MEMBER);
        }
        if (SocialGroupsManagement.isRequestPending(sws, currentUser)) {
            return buildResponse(sws, Statute.REQUEST_PENDING);
        }
        return buildResponse(sws, Statute.NOT_MEMBER);
    }

    protected Blob buildResponse(DocumentModel sws, Statute statute)
            throws PropertyException, ClientException {
        JSONObject obj = new JSONObject();
        obj.element("statute", statute);
        obj.element("title", sws.getPropertyValue("dc:title"));
        obj.element("description", sws.getPropertyValue("dc:description"));
        return new StringBlob(obj.toString(), "application/json");
    }

}
