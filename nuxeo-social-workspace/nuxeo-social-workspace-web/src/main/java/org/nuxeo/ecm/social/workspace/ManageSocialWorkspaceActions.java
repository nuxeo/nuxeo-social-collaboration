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
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupName;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.getSocialWorkspaceMembersGroupName;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialWorkspace;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.mvel2.optimizers.impl.refl.nodes.ArrayLength;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.computedgroups.SocialWorkspaceGroupComputer;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;

/**
 * Bean to manage social workspace actions.
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.4.1
 */
@Name("manageSocialWorkspaceActions")
@Scope(PAGE)
@Install(precedence = FRAMEWORK)
public class ManageSocialWorkspaceActions implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String GROUPS_SAVE_COMPLETED_LABEL = "label.social.workspace.faces.saveCompleted";

    public static final String GROUPS_SAVE_ERROR_LABEL = "label.social.workspace.faces.saveError";

    public static final String USERS_IMPORTED_COUNT_LABEL = "label.social.workspace.users.imported.count";

    private static final Log log = LogFactory.getLog(ManageSocialWorkspaceActions.class);

    protected List<String> originalAdministrators;

    protected List<String> administrators;

    protected List<String> originalMembers;

    protected List<String> members;

    protected String rawListOfEmails;

    @In(create = true)
    protected transient SocialWorkspaceService socialWorkspaceService;

    @In(create = true)
    protected transient UserManager userManager;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true, required = false)
    protected FacesMessages facesMessages;

    @In(create = true)
    protected ResourcesAccessor resourcesAccessor;

    protected SocialWorkspaceGroupComputer computer = new SocialWorkspaceGroupComputer();

    public List<String> getAdministrators() throws Exception {
        if (administrators == null) {
            DocumentModel currentDocument = navigationContext.getCurrentDocument();
            administrators = ActivityHelper.getUsernames(computer.getGroupMembers(getSocialWorkspaceAdministratorsGroupName(currentDocument)));
            originalAdministrators = administrators;
        }
        return administrators;
    }

    public List<String> getMembers() throws Exception {
        if (members == null) {
            DocumentModel currentDocument = navigationContext.getCurrentDocument();
            members = ActivityHelper.getUsernames(computer.getGroupMembers(getSocialWorkspaceMembersGroupName(currentDocument)));
            originalMembers = members;
        }
        return members;
    }

    public void updateGroups() throws ClientException {
        SocialWorkspace socialWorkspace = toSocialWorkspace(navigationContext.getCurrentDocument());
        for (String administrator : administrators) {
            if (!originalAdministrators.contains(administrator)) {
                socialWorkspace.addAdministrator(userManager.getPrincipal(administrator));
            }
        }
        for (String administrator : originalAdministrators) {
            if (!administrators.contains(administrator)) {
                socialWorkspace.removeAdministrator(userManager.getPrincipal(administrator));
            }
        }
        for (String member : members) {
            if (!originalMembers.contains(member)) {
                socialWorkspace.addMember(userManager.getPrincipal(member));
            }
        }
        for (String member : originalMembers) {
            if (!members.contains(member)) {
                socialWorkspace.removeMember(userManager.getPrincipal(member));
            }
        }

        facesMessages.add(
                StatusMessage.Severity.INFO,
                resourcesAccessor.getMessages().get(GROUPS_SAVE_COMPLETED_LABEL));
    }

    public void setAdministrators(List<String> administrators) {
        this.administrators = administrators;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getRawListOfEmails() {
        return rawListOfEmails;
    }

    public void setRawListOfEmails(String rawListOfEmails) {
        this.rawListOfEmails = rawListOfEmails;
    }

    public void importUserFromListOfEmail() throws ClientException {

        List<String> emails = Arrays.asList(rawListOfEmails.split("\\s"));
        SocialWorkspace socialWorkspace = toSocialWorkspace(navigationContext.getCurrentDocument());
        List<String> emailOfUsersAdded = socialWorkspaceService.addSeveralSocialWorkspaceMembers(
                socialWorkspace, emails);
        facesMessages.add(
                StatusMessage.Severity.INFO,
                resourcesAccessor.getMessages().get(USERS_IMPORTED_COUNT_LABEL),
                emailOfUsersAdded.size());
    }

}
