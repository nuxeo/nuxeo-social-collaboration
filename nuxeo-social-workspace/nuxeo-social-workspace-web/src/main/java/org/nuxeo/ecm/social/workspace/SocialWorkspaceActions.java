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

package org.nuxeo.ecm.social.workspace;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.io.Serializable;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.api.WebActions;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
@Name("socialWorkspaceActions")
@Scope(CONVERSATION)
@Install(precedence = FRAMEWORK)
public class SocialWorkspaceActions implements Serializable {

    public static final String MAIN_TABS_COLLABORATION = "MAIN_TABS:collaboration";

    private static final long serialVersionUID = 1L;

    @In(create = true)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true)
    protected transient SocialWorkspaceService socialWorkspaceService;

    @In(create = true)
    protected transient NuxeoPrincipal currentUser;

    @In(create = true)
    protected transient WebActions webActions;

    public static SocialWorkspace toSocialWorkspace(DocumentModel doc) {
        return SocialWorkspaceHelper.toSocialWorkspace(doc);
    }

    public boolean isCurrentUserAdministratorOrMemberOfCurrentSocialWorkspace() {
        DocumentModel doc = navigationContext.getCurrentDocument();
        SocialWorkspace socialWorkspace = socialWorkspaceService.getDetachedSocialWorkspace(doc);
        return socialWorkspace != null && socialWorkspace.isAdministratorOrMember(currentUser);
    }

    public SocialWorkspace getSocialWorkspace(DocumentModel doc) {
        return socialWorkspaceService.getDetachedSocialWorkspace(doc);
    }

    public SocialWorkspace getSocialWorkspace() {
        DocumentModel doc = navigationContext.getCurrentDocument();
        return getSocialWorkspace(doc);
    }

    public DocumentModel getSocialWorkspaceContainer() {
        return socialWorkspaceService.getSocialWorkspaceContainer(documentManager);
    }

    public String getCollaborationMainTab(){
        return MAIN_TABS_COLLABORATION;
    }

    public void setCollaborationMainTab(String string) {
        webActions.setCurrentTabIds(MAIN_TABS_COLLABORATION);
    }
}
