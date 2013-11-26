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
 *     bjalon
 */
package org.nuxeo.ecm.social.workspace.gadgets;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:bjalon@nuxeo.com">Benjamin JALON</a>
 */
@Operation(id = GetPublicSocialWorkspaces.ID, category = Constants.CAT_EXECUTION, label = "Get Public Social Workspaces", description = "Return Social Workspaces that matches the given pattern")
public class GetPublicSocialWorkspaces {

    public static final String ID = "SocialWorkspace.GetPublicSocialWorkspaces";

    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "pattern", required = false)
    protected String pattern;

    @OperationMethod
    public DocumentModelList run() throws Exception {
        SocialWorkspaceService service = Framework.getService(SocialWorkspaceService.class);

        DocumentModelList result = new DocumentModelListImpl();
        for (SocialWorkspace sw : service.searchDetachedPublicSocialWorkspaces(
                session, pattern)) {
            result.add(sw.getDocument());
        }
        return result;
    }
}
