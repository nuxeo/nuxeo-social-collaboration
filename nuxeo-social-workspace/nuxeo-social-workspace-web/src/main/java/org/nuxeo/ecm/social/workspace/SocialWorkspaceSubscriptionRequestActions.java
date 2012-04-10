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
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SUBSCRIPTION_REQUEST_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.VALIDATE_SOCIAL_WORKSPACE_TASK_NAME;
import static org.nuxeo.ecm.social.workspace.userregistration.SocialRegistrationConstant.REQUEST_PENDING_STATE;
import static org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager.CURRENT_DOCUMENT_SELECTION;
import static org.nuxeo.ecm.webapp.helpers.EventNames.DOCUMENT_CHANGED;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.contentview.seam.ContentViewActions;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.TaskService;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.adapters.SubscriptionRequest;
import org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper;
import org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 */
@Name("socialWorkspaceSubscriptionRequestActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = FRAMEWORK)
public class SocialWorkspaceSubscriptionRequestActions implements Serializable {

    public static final String SUBSCRIPTION_REQUESTS_UPDATED = "subscriptionRequestsUpdated";

    private static final long serialVersionUID = -7362146679190186610L;

    private static final Log log = LogFactory.getLog(SocialWorkspaceSubscriptionRequestActions.class);

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient DocumentsListsManager documentsListsManager;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true)
    protected transient ContentViewActions contentViewActions;

    public void accept() throws ClientException {
        List<DocumentModel> list = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);
        processSelectedSubscriptionRequests(list, true);
    }

    public void reject() throws ClientException {
        List<DocumentModel> list = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);
        processSelectedSubscriptionRequests(list, false);
    }

    protected void processSelectedSubscriptionRequests(
            List<DocumentModel> list, boolean accept) throws ClientException {
        SocialWorkspace socialWorkspace = SocialWorkspaceHelper.toSocialWorkspace(navigationContext.getCurrentDocument());
        for (DocumentModel doc : list) {
            if (SUBSCRIPTION_REQUEST_TYPE.equals(doc.getType())) {
                SubscriptionRequest subscriptionRequest = doc.getAdapter(SubscriptionRequest.class);
                processSubscriptionRequest(socialWorkspace,
                        subscriptionRequest, accept);
            }
        }
        documentManager.save();
        Events.instance().raiseEvent(SUBSCRIPTION_REQUESTS_UPDATED);
    }

    protected static void processSubscriptionRequest(
            SocialWorkspace socialWorkspace,
            SubscriptionRequest subscriptionRequest, boolean accept) {
        try {
            if (accept) {
                socialWorkspace.acceptSubscriptionRequest(subscriptionRequest);
            } else {
                socialWorkspace.rejectSubscriptionRequest(subscriptionRequest);
            }
        } catch (Exception e) {
            log.warn(String.format("Unable to process request for %s: %s",
                    subscriptionRequest.getDocument(), e.getMessage()));
            log.debug(e, e);
        }
    }

    public boolean enableRequestActions() throws ClientException {
        List<DocumentModel> list = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);
        if (list.isEmpty()) {
            return false;
        }
        for (DocumentModel doc : list) {
            if (!REQUEST_PENDING_STATE.equals(doc.getCurrentLifeCycleState())) {
                return false;
            }
        }
        return true;
    }

    @Observer(SUBSCRIPTION_REQUESTS_UPDATED)
    public void onSubscriptionRequestsUpdated() {
        contentViewActions.refreshOnSeamEvent(SUBSCRIPTION_REQUESTS_UPDATED);
        contentViewActions.resetPageProviderOnSeamEvent(SUBSCRIPTION_REQUESTS_UPDATED);
    }

    public boolean enableSocialWorkspaceActions() throws ClientException {
        List<DocumentModel> list = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);
        if (list.isEmpty()) {
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
        List<DocumentModel> list = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);
        processSocialWorkspaces(list, "approve");
    }

    public void rejectSocialWorkspaces() throws ClientException {
        List<DocumentModel> list = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);
        processSocialWorkspaces(list, "delete");
        Events.instance().raiseEvent(DOCUMENT_CHANGED);
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
        List<Task> canceledTasks = new ArrayList<Task>();
        try {
            TaskService taskService = Framework.getService(TaskService.class);
            List<Task> taskInstances = taskService.getTaskInstances(doc,
                    (NuxeoPrincipal) null, documentManager);
            for (Task task : taskInstances) {
                if (VALIDATE_SOCIAL_WORKSPACE_TASK_NAME.equals(task.getName())) {
                    task.cancel(documentManager);
                    canceledTasks.add(task);
                }
            }
            if (!canceledTasks.isEmpty()) {
                DocumentModel[] docToSave = new DocumentModel[canceledTasks.size()];
                canceledTasks.toArray(docToSave);
                documentManager.saveDocuments(docToSave);
            }
        } catch (Exception e) {
            log.warn(
                    "failed cancel tasks for accepted/rejected SocialWorkspace",
                    e);
        }

    }
}
