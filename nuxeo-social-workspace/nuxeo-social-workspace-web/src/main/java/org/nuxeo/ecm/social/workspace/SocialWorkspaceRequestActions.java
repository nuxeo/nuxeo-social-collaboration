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
package org.nuxeo.ecm.social.workspace;

import static org.jboss.seam.annotations.Install.FRAMEWORK;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_REQUEST_USERNAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.REQUEST_DOC_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.VALIDATE_SOCIAL_WORKSPACE_TASK_NAME;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.jbpm.JbpmListFilter;
import org.nuxeo.ecm.platform.jbpm.JbpmService;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 *
 */
@Name("socialWorkspaceRequestActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = FRAMEWORK)
public class SocialWorkspaceRequestActions implements Serializable {

    private static final long serialVersionUID = -7362146679190186610L;

    private static final Log log = LogFactory.getLog(SocialWorkspaceRequestActions.class);

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient DocumentsListsManager documentsListsManager;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    public void accept() throws ClientException {
        List<DocumentModel> list = documentsListsManager.getWorkingList(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION);
        processSelectedRequests(list, "accept");
    }

    public void reject() throws ClientException {
        List<DocumentModel> list = documentsListsManager.getWorkingList(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION);
        processSelectedRequests(list, "reject");
    }

    protected void processSelectedRequests(List<DocumentModel> list,
            String transition) throws ClientException {
        DocumentModel sws = navigationContext.getCurrentDocument();
        for (DocumentModel doc : list) {
            try {
                if (REQUEST_DOC_TYPE.equals(doc.getType())) {
                    String userName = (String) doc.getPropertyValue(FIELD_REQUEST_USERNAME);
                    boolean ok = true;
                    boolean accept = "accept".equals(transition);
                    if (accept) {
                        ok = SocialGroupsManagement.acceptMember(sws, userName);
                    }
                    if (ok) {
                        doc.followTransition(transition);
                        SocialGroupsManagement.notifyUser(sws, userName, accept);
                    }

                }
            } catch (Exception e) {
                log.debug("failed to process the request ... " + doc.getId(), e);
            }
        }
        documentManager.save();
    }

    public boolean enableRequestActions() throws ClientException {
        List<DocumentModel> list = documentsListsManager.getWorkingList(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION);
        if (list.size() == 0) {
            return false;
        }
        for (DocumentModel doc : list) {
            if (!"pending".equals(doc.getCurrentLifeCycleState())) {
                return false;
            }
        }
        return true;
    }

    public boolean enableSocialWorkspaceActions() throws ClientException {
        List<DocumentModel> list = documentsListsManager.getWorkingList(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION);
        if (list.size() == 0) {
            return false;
        }
        for (DocumentModel doc : list) {
            if (!"project".equals(doc.getCurrentLifeCycleState())) {
                return false;
            }
        }
        return true;
    }

    public void acceptSocialWorkspaces() throws ClientException {
        List<DocumentModel> list = documentsListsManager.getWorkingList(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION);
        processSocialWorkspaces(list, "approve");
    }

    public void rejectSocialWorkspaces() throws ClientException {
        List<DocumentModel> list = documentsListsManager.getWorkingList(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION);
        processSocialWorkspaces(list, "delete");
    }

    protected void processSocialWorkspaces(List<DocumentModel> list,
            String transition) throws ClientException {
        for (DocumentModel doc : list) {
            try {
                if (SOCIAL_WORKSPACE_TYPE.equals(doc.getType())) {
                    doc.followTransition(transition);
                    removeValidationTasks(doc);
                }
            } catch (Exception e) {
                log.debug(
                        "failed to process the social workspace ... "
                                + doc.getId(), e);
            }
        }
        documentManager.save();
    }

    private void removeValidationTasks(DocumentModel doc) {
        List<TaskInstance> canceledTasks = new ArrayList<TaskInstance>();
        try {
            JbpmService jbpmService = Framework.getService(JbpmService.class);
            List<TaskInstance> taskInstances = jbpmService.getTaskInstances(
                    doc, null, (JbpmListFilter) null);
            for (TaskInstance task : taskInstances) {
                if (VALIDATE_SOCIAL_WORKSPACE_TASK_NAME.equals(task.getName())) {
                    task.cancel();
                    canceledTasks.add(task);
                }
            }
            if (canceledTasks.size() > 0) {
                jbpmService.saveTaskInstances(canceledTasks);
            }
        } catch (Exception e) {
            log.warn(
                    "failed cancel tasks for accepted/rejected SocialWorkspace",
                    e);
        }

    }
}
