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
package org.nuxeo.ecm.social.workspace.listeners;

import static org.nuxeo.ecm.social.workspace.SocialConstants.VALIDATE_SOCIAL_WORKSPACE_TASK_NAME;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.TaskService;
import org.nuxeo.runtime.api.Framework;

/**
 * Listener that processes "checkExpiredTasksSignal" events which are generated
 * each day by Scheduler Service. If there is a Social Workspace that wasn't
 * validated and associated validation task is expired, then it will be marked
 * as deleted.
 *
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 */
public class CheckSocialWorkspaceValidationTasks implements EventListener {

    protected TaskService taskService;

    protected AutomationService automationService;

    private static final Log log = LogFactory.getLog(CheckSocialWorkspaceValidationTasks.class);

    public static final String QUERY_SELECT_NOT_VALIDATED_SOCIAL_WORKSPACES = "SELECT * FROM SocialWorkspace "
            + "WHERE ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState = 'project'";

    @Override
    public void handleEvent(Event event) throws ClientException {
        if (!"checkExpiredTasksSignal".equals(event.getName())) {
            return;
        }
        try {
            UnrestrictedSocialWorkspaceValidationTasksChecker checker = new UnrestrictedSocialWorkspaceValidationTasksChecker();
            checker.runUnrestricted();
        } catch (Exception e) {
            log.debug("failed to open session", e);
        }
    }

    private void checkTasksFor(DocumentModel doc, CoreSession coreSession)
            throws ClientException {
        List<Task> taskInstances = getTaskService().getTaskInstances(doc,
                (NuxeoPrincipal) null, coreSession);
        List<Task> canceledTasks = new ArrayList<Task>();
        for (Task task : taskInstances) {
            if (VALIDATE_SOCIAL_WORKSPACE_TASK_NAME.equals(task.getName())
                    && isExpired(task)) {
                OperationContext ctx = new OperationContext(
                        doc.getCoreSession());
                ctx.setInput(doc);
                try {
                    task.cancel(coreSession);
                    getAutomationService().run(ctx,
                            "SocialWorkspaceNotValidatedChain");
                    task.cancel(coreSession);
                    canceledTasks.add(task);
                } catch (Exception e) {
                    log.warn(
                            "failed to invalidate social workspace"
                                    + doc.getTitle(), e);
                }
            }
        }
        if (!canceledTasks.isEmpty()) {
            DocumentModel[] docToSave = new DocumentModel[canceledTasks.size()];
            canceledTasks.toArray(docToSave);
            coreSession.saveDocuments(docToSave);
        }
    }

    private static boolean isExpired(Task task) throws ClientException {
        Date date = task.getDueDate();
        return date != null && date.before(new Date());
    }

    private AutomationService getAutomationService() throws Exception {
        if (automationService == null) {
            automationService = Framework.getService(AutomationService.class);
        }
        return automationService;
    }

    protected TaskService getTaskService() {
        if (taskService == null) {
            try {
                taskService = Framework.getService(TaskService.class);
            } catch (Exception e) {
                log.warn("failed to get JbpmService service", e);
            }
        }
        return taskService;
    }

    protected class UnrestrictedSocialWorkspaceValidationTasksChecker extends
            UnrestrictedSessionRunner {

        protected UnrestrictedSocialWorkspaceValidationTasksChecker()
                throws Exception {
            super(
                    Framework.getService(RepositoryManager.class).getDefaultRepositoryName());
        }

        @Override
        public void run() throws ClientException {
            // get the list with all not validated social workspaces
            DocumentModelList list = session.query(QUERY_SELECT_NOT_VALIDATED_SOCIAL_WORKSPACES);
            for (DocumentModel doc : list) {
                checkTasksFor(doc, session);
            }
        }
    }

}
