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

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.buildRelationAdministratorKind;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.buildRelationMemberKind;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupName;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.getSocialWorkspaceMembersGroupName;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.user.relationship.RelationshipKind;
import org.nuxeo.ecm.social.user.relationship.service.UserRelationshipService;
import org.nuxeo.ecm.social.workspace.computedgroups.SocialWorkspaceGroupComputer;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;

/**
 * Bean to manage social workspace actions.
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.4.1
 */
@Name("manageSocialWorkspaceActions")
@Scope(CONVERSATION)
@Install(precedence = FRAMEWORK)
public class ManageSocialWorkspaceActions implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String GROUPS_SAVE_COMPLETED_LABEL = "label.social.workspace.faces.saveCompleted";

    public static final String GROUPS_SAVE_ERROR_LABEL = "label.social.workspace.faces.saveError";

    private static final Log log = LogFactory.getLog(ManageSocialWorkspaceActions.class);

    protected List<String> originalAdministrators;

    protected List<String> administrators;

    protected List<String> originalMembers;

    protected List<String> members;

    @In(create = true)
    protected transient UserManager userManager;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true, required = false)
    protected FacesMessages facesMessages;

    @In(create = true)
    protected ResourcesAccessor resourcesAccessor;

    @In(create = true)
    private UserRelationshipService userRelationshipService;

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

    public void resetGroups() {
        administrators = null;
        members = null;
    }

    public void updateGroups() {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        RelationshipKind relationKind = buildRelationAdministratorKind();
        String documentActivityObject = ActivityHelper.createDocumentActivityObject(currentDocument);
        for (String administrator : administrators) {
            if (!originalAdministrators.contains(administrator)) {
                String userActivityObject = ActivityHelper.createUserActivityObject(administrator);
                userRelationshipService.addRelation(userActivityObject,
                        documentActivityObject, relationKind);
                userRelationshipService.addRelation(documentActivityObject,
                        userActivityObject, relationKind);
            }
        }
        for (String administrator : originalAdministrators) {
            if (!administrators.contains(administrator)) {
                String userActivityObject = ActivityHelper.createUserActivityObject(administrator);
                userRelationshipService.removeRelation(userActivityObject,
                        documentActivityObject, relationKind);
                userRelationshipService.removeRelation(documentActivityObject,
                        userActivityObject, relationKind);
            }
        }
        relationKind = buildRelationMemberKind();
        for (String member : members) {
            if (!originalMembers.contains(member)) {
                String userActivityObject = ActivityHelper.createUserActivityObject(member);
                userRelationshipService.addRelation(userActivityObject,
                        documentActivityObject, relationKind);
                userRelationshipService.addRelation(documentActivityObject,
                        userActivityObject, relationKind);
            }
        }
        for (String member : originalMembers) {
            if (!members.contains(member)) {
                String userActivityObject = ActivityHelper.createUserActivityObject(member);
                userRelationshipService.removeRelation(userActivityObject,
                        documentActivityObject, relationKind);
                userRelationshipService.removeRelation(documentActivityObject,
                        userActivityObject, relationKind);
            }
        }

        resetGroups();
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

}
