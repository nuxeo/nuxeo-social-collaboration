/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 * Contributors:
 * Nuxeo - initial API and implementation
 */

package org.nuxeo.ecm.social.workspace;

import static org.jboss.seam.ScopeType.PAGE;
import static org.jboss.seam.annotations.Install.FRAMEWORK;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;

/**
 * Bean to manage both groups of a community.
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.4.1
 */
@Name("socialWorkspaceGroupManagerActions")
@Scope(PAGE)
@Install(precedence = FRAMEWORK)
public class ManageSocialWorkspaceActions {

    public static final String GROUPS_SAVE_COMPLETED_LABEL = "label.social.workspace.faces.saveCompleted";

    public static final String GROUPS_SAVE_ERROR_LABEL = "label.social.workspace.faces.saveError";

    private static final Log log = LogFactory.getLog(ManageSocialWorkspaceActions.class);

    protected DocumentModel administratorsGroup;

    protected DocumentModel membersGroup;

    @In(create = true)
    protected UserManager userManager;

    @In(create = true)
    protected NavigationContext navigationContext;

    @In(create = true, required = false)
    protected FacesMessages facesMessages;

    @In(create = true)
    protected ResourcesAccessor resourcesAccessor;

    public DocumentModel getAdministratorsGroup() throws ClientException {
        if (administratorsGroup == null) {
            administratorsGroup = userManager.getGroupModel(SocialWorkspaceHelper.getCommunityAdministratorsGroupName(navigationContext.getCurrentDocument()));
        }
        return administratorsGroup;
    }

    public DocumentModel getMembersGroup() throws ClientException {
        if (membersGroup == null) {
            membersGroup = userManager.getGroupModel(SocialWorkspaceHelper.getCommunityMembersGroupName(navigationContext.getCurrentDocument()));
        }
        return membersGroup;
    }

    public void resetGroups() {
        administratorsGroup = null;
        membersGroup = null;
    }

    public void updateGroups() {
        try {
            userManager.updateGroup(administratorsGroup);
            userManager.updateGroup(membersGroup);

            resetGroups();
            facesMessages.add(
                    StatusMessage.Severity.INFO,
                    resourcesAccessor.getMessages().get(
                            GROUPS_SAVE_COMPLETED_LABEL));
        } catch (ClientException e) {
            log.error("Cannot update group", e);
            facesMessages.add(
                    StatusMessage.Severity.FATAL,
                    resourcesAccessor.getMessages().get(GROUPS_SAVE_ERROR_LABEL));
        }
    }

}
