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
import java.security.Principal;
import java.util.List;

import net.sf.json.JSONArray;
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
import org.nuxeo.ecm.platform.ui.web.tag.fn.DocumentModelFunctions;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.platform.web.common.vh.VirtualHostHelper;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;
import org.nuxeo.ecm.user.center.profile.UserProfileService;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 *
 */
@Operation(id = GetSocialWorkspaceMembers.ID, category = Constants.CAT_EXECUTION, label = "Social Workspace Members", description = "return members of a social workspace")
public class GetSocialWorkspaceMembers {

    public static final String ID = "SocialWorkspace.Members";

    public static final String AVATAR_PROPERTY = "userprofile:avatar";

    @Context
    protected CoreSession session;

    @Context
    protected SocialWorkspaceService socialWorkspaceService;

    @Context
    protected UserProfileService userProfileService;

    @Context
    protected UserManager userManager;

    @Param(name = "pageSize")
    protected int pageSize = 5;

    @Param(name = "page")
    protected int page = 0;

    @Param(name = "contextPath", required = true)
    protected String contextPath;

    @Param(name = "pattern", required = true)
    protected String pattern;

    @OperationMethod
    public Blob run() throws Exception {
        SocialWorkspace socialWorkspace = socialWorkspaceService.getDetachedSocialWorkspaceContainer(
                session, new PathRef(contextPath));
        List<String> users = socialWorkspace.searchMembers(pattern);
        return buildResponse(users);
    }

    protected Blob buildResponse(List<String> users)
            throws UnsupportedEncodingException, ClientException {
        JSONObject result = new JSONObject();
        result.put("page", page);
        result.put("pageMax", Math.ceil((float) users.size() / pageSize));

        int startIndex = page * pageSize;
        users = users.subList(page * pageSize,
                Math.min(startIndex + pageSize, users.size()));

        JSONArray array = new JSONArray();
        for (String user : users) {
            NuxeoPrincipal principal = userManager.getPrincipal(user);
            JSONObject o = new JSONObject();
            o.element("id", principal.getName());
            o.element("firstName", principal.getFirstName());
            o.element("lastName", principal.getLastName());
            o.element("profileURL",
                    ActivityHelper.getUserProfileURL(principal.getName()));
            o.element("avatarURL", getAvatarURL(principal));
            array.add(o);
        }

        result.put("users", array);

        return new InputStreamBlob(new ByteArrayInputStream(
                result.toString().getBytes("UTF-8")), "application/json");
    }

    protected String getAvatarURL(Principal principal) throws ClientException {
        DocumentModel userProfileDoc = userProfileService.getUserProfileDocument(
                principal.getName(), session);
        String url = VirtualHostHelper.getContextPathProperty()
                + "/icons/missing_avatar.png";
        if (userProfileDoc.getPropertyValue(AVATAR_PROPERTY) != null) {
            url = DocumentModelFunctions.fileUrl("downloadFile",
                    userProfileDoc, AVATAR_PROPERTY, "avatar");
        }
        return url;
    }

}
