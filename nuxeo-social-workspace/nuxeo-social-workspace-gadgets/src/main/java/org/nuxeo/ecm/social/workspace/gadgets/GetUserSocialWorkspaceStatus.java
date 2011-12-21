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
import java.util.List;

import net.sf.json.JSONObject;

import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.impl.blob.InputStreamBlob;
import org.nuxeo.ecm.social.relationship.RelationshipKind;
import org.nuxeo.ecm.social.relationship.service.RelationshipService;
import org.nuxeo.ecm.social.workspace.SocialConstants;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 */
@Operation(id = GetUserSocialWorkspaceStatus.ID, category = Constants.CAT_EXECUTION, label = "Get User Social Workspace Status", description = "Return user status")
public class GetUserSocialWorkspaceStatus {

    enum Status {
        MEMBER, NOT_MEMBER, REQUEST_PENDING, REQUEST_REJECTED
    }

    public static final String ID = "SocialWorkspace.UserStatus";

    @Context
    protected CoreSession session;

    @Context
    protected SocialWorkspaceService socialWorkspaceService;

    @Context
    protected RelationshipService relationshipService;

    @Param(name = "contextPath", required = true)
    protected String contextPath;

    @OperationMethod
    public Blob run() throws Exception {
        NuxeoPrincipal currentUser = (NuxeoPrincipal) session.getPrincipal();
        SocialWorkspace socialWorkspace = socialWorkspaceService.getDetachedSocialWorkspace(
                session, new PathRef(contextPath));

        List<String> targets = relationshipService.getTargetsOfKind(
                ActivityHelper.createDocumentActivityObject(
                        socialWorkspace.getDocument().getRepositoryName(),
                        socialWorkspace.getId()),
                RelationshipKind.fromString("socialworkspace:members"));
        if (targets.contains(ActivityHelper.createUserActivityObject(currentUser))) {
            return buildResponse(socialWorkspace.getDocument(), Status.MEMBER);
        } else if (socialWorkspace.isAdministratorOrMember(currentUser)) {
            return buildResponse(socialWorkspace.getDocument(), Status.MEMBER);
        } else {
            String reqestStatus = socialWorkspace.getSubscriptionRequestStatus(currentUser);
            if ( reqestStatus == null ){ // no subscrition requests
                return buildResponse(socialWorkspace.getDocument(), Status.NOT_MEMBER);
            } else if ( SocialConstants.SUBSCRIPTION_REQUEST_PENDING_STATE.equals(reqestStatus)){
                return buildResponse(socialWorkspace.getDocument(), Status.REQUEST_PENDING);
            } else if ( SocialConstants.SUBSCRIPTION_REQUEST_REJECTED_STATE.equals(reqestStatus)){
                return buildResponse(socialWorkspace.getDocument(), Status.REQUEST_REJECTED);
            } else if ( SocialConstants.SUBSCRIPTION_REQUEST_ACCEPTED_STATE.equals(reqestStatus)){
                // the case when user !isAdministratorOrMember but he has a accepted subscription request
                // TODO: review the behavior for this case
                return buildResponse(socialWorkspace.getDocument(), Status.MEMBER);
            } else {
                return buildResponse(socialWorkspace.getDocument(), Status.NOT_MEMBER);
            }
        }
    }

    protected static Blob buildResponse(DocumentModel sws, Status status)
            throws ClientException, UnsupportedEncodingException {
        JSONObject obj = new JSONObject();
        obj.element("status", status);
        obj.element("title", sws.getPropertyValue("dc:title"));
        obj.element("description", sws.getPropertyValue("dc:description"));
        return new InputStreamBlob(new ByteArrayInputStream(
                obj.toString().getBytes("UTF-8")), "application/json");
    }

}
