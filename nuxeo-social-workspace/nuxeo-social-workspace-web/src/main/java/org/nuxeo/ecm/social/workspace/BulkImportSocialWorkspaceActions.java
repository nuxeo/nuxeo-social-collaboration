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
 *     Nuxeo
 */

package org.nuxeo.ecm.social.workspace;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialWorkspace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;

/**
 * Action bean to manage bulk user import in a Social Workspace
 * 
 * @author Arnaud KERVERN <akervern@nuxeo.com>
 * @since 5.4.3
 */
@Name("bulkImportSocialWorkspaceActions")
@Scope(CONVERSATION)
@Install(precedence = FRAMEWORK)
public class BulkImportSocialWorkspaceActions implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String USERS_IMPORTED_LABEL = "label.social.workspace.users.imported";

    public static final String USERS_NOT_IMPORTED_LABEL = "label.social.workspace.users.imported.not";

    public static final String USERS_IMPORTED_ERROR_LABEL = "label.social.workspace.users.imported.error";

    private static final Log log = LogFactory.getLog(BulkImportSocialWorkspaceActions.class);

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

    protected String rawListOfEmails;

    protected List<String> groupsToImport;

    public List<String> getGroupsToImport() {
        return groupsToImport;
    }

    public void setGroupsToImport(List<String> selectedGroups) {
        this.groupsToImport = selectedGroups;
    }

    public String getRawListOfEmails() {
        return rawListOfEmails;
    }

    public void setRawListOfEmails(String rawListOfEmails) {
        this.rawListOfEmails = rawListOfEmails;
    }

    public void importUserFromListOfEmail() {
        List<String> emails = new ArrayList<String>(
                Arrays.asList(rawListOfEmails.split("\\s")));
        SocialWorkspace socialWorkspace = toSocialWorkspace(navigationContext.getCurrentDocument());
        List<String> emailOfUsersAdded = null;
        try {
            emailOfUsersAdded = socialWorkspaceService.addSeveralSocialWorkspaceMembers(
                    socialWorkspace, emails);
            emails.removeAll(emailOfUsersAdded);

            // Display message about new imported users
            facesMessages.add(StatusMessage.Severity.INFO,
                    resourcesAccessor.getMessages().get(USERS_IMPORTED_LABEL),
                    emailOfUsersAdded.size(),
                    getUsersListString(emailOfUsersAdded));
            if (!emails.isEmpty()) {
                // Display message about not imported users if there are.
                facesMessages.add(
                        StatusMessage.Severity.WARN,
                        resourcesAccessor.getMessages().get(
                                USERS_NOT_IMPORTED_LABEL), emails.size(),
                        getUsersListString(emails));
            }

            resetRawListOfEmails();
        } catch (ClientException e) {
            log.warn(e, e);
            facesMessages.add(
                    StatusMessage.Severity.ERROR,
                    resourcesAccessor.getMessages().get(
                            USERS_IMPORTED_ERROR_LABEL));
        }
    }

    public void importUserFromGroups() {
        if (groupsToImport != null) {
            Set<String> importedUsers = new HashSet<String>();
            SocialWorkspace socialWorkspace = toSocialWorkspace(navigationContext.getCurrentDocument());

            try {
                for (String groupName : groupsToImport) {
                    importedUsers.addAll(socialWorkspaceService.addSeveralSocialWorkspaceMembers(
                            socialWorkspace, groupName));
                }

                facesMessages.add(
                        StatusMessage.Severity.INFO,
                        resourcesAccessor.getMessages().get(
                                USERS_IMPORTED_LABEL), importedUsers.size(),
                        getUsersListString(importedUsers));
                resetGroupsToImport();
            } catch (ClientException e) {
                log.warn(e, e);
                facesMessages.add(
                        StatusMessage.Severity.ERROR,
                        resourcesAccessor.getMessages().get(
                                USERS_IMPORTED_ERROR_LABEL));
            }
        }
    }

    protected String getUsersListString(Collection<String> users) {
        StringBuilder sb = new StringBuilder();
        for (String user : users) {
            sb.append(user).append(", ");
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append(".");
        return sb.toString();
    }

    public void resetGroupsToImport() {
        groupsToImport = null;
    }

    public void resetRawListOfEmails() {
        rawListOfEmails = null;
    }
}
