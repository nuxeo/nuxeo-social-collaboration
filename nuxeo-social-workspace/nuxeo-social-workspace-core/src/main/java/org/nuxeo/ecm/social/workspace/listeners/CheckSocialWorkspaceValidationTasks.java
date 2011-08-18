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
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.platform.jbpm.JbpmListFilter;
import org.nuxeo.ecm.platform.jbpm.JbpmService;
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

    protected JbpmService jbpmService;

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

    private void checkTasksFor(DocumentModel doc) throws ClientException {
        List<TaskInstance> taskInstances = getJbpmService().getTaskInstances(
                doc, null, (JbpmListFilter) null);
        List<TaskInstance> canceledTasks = new ArrayList<TaskInstance>();
        for (TaskInstance task : taskInstances) {
            if (VALIDATE_SOCIAL_WORKSPACE_TASK_NAME.equals(task.getName())
                    && isExpired(task)) {
                OperationContext ctx = new OperationContext(
                        doc.getCoreSession());
                ctx.setInput(doc);
                try {
                    getAutomationService().run(ctx,
                            "SocialWorkspaceNotValidatedChain");
                    task.cancel();
                    canceledTasks.add(task);
                } catch (Exception e) {
                    log.warn(
                            "failed to invalidate social workspace"
                                    + doc.getTitle(), e);
                }
            }
        }
        if (!canceledTasks.isEmpty()) {
            getJbpmService().saveTaskInstances(canceledTasks);
        }
    }

    private static boolean isExpired(TaskInstance task) {
        Date date = task.getDueDate();
        return date != null && date.before(new Date());
    }

    private AutomationService getAutomationService() throws Exception {
        if (automationService == null) {
            automationService = Framework.getService(AutomationService.class);
        }
        return automationService;
    }

    protected JbpmService getJbpmService() {
        if (jbpmService == null) {
            try {
                jbpmService = Framework.getService(JbpmService.class);
            } catch (Exception e) {
                log.warn("failed to get JbpmService service", e);
            }
        }
        return jbpmService;
    }

    protected class UnrestrictedSocialWorkspaceValidationTasksChecker extends
            UnrestrictedSessionRunner {

        protected UnrestrictedSocialWorkspaceValidationTasksChecker()
                throws Exception {
            super(
                    Framework.getService(RepositoryManager.class).getDefaultRepository().getName());
        }

        @Override
        public void run() throws ClientException {
            // get the list with all not validated social workspaces
            DocumentModelList list = session.query(QUERY_SELECT_NOT_VALIDATED_SOCIAL_WORKSPACES);
            for (DocumentModel doc : list) {
                checkTasksFor(doc);
            }
        }
    }

}
