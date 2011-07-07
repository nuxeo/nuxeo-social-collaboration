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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.platform.jbpm.JbpmListFilter;
import org.nuxeo.ecm.platform.jbpm.JbpmService;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 * 
 *         lister that process "checkExpiredTasksSignal" event which is
 *         generated each day by Scheduler Service. if there is a Social
 *         Workspace that wasn't validated and associated validation task is
 *         expired, then the access will be blocked.
 * 
 */
public class CheckSocialWorkspaceValidationTasks implements EventListener {

    protected JbpmService jbpmService;

    protected AutomationService automationService = null;

    private static final Log log = LogFactory.getLog(CheckSocialWorkspaceValidationTasks.class);

    public void handleEvent(Event event) throws ClientException {
        if (!"checkExpiredTasksSignal".equals(event.getName())) {
            return;
        }
        CoreSession session = null;
        try {
            session = openSession();
            // get the list with all social workspaces
            DocumentModelList list = session.query("Select * FROM SocialWorkspace");
            for (DocumentModel doc : list) {
                checkTasksFor(doc);
            }
        } catch (Exception e) {
            log.debug("failed to open session", e);
        } finally {
            if (session != null) {
                Repository.close(session);
            }
        }

    }

    private void checkTasksFor(DocumentModel doc) throws ClientException {
        List<TaskInstance> taskInstances = getJbpmService().getTaskInstances(
                doc, null, (JbpmListFilter) null);
        List<TaskInstance> canceledTasks = new ArrayList<TaskInstance>();
        for (TaskInstance task : taskInstances) {
            if ("validateSocialWorkspace".equals(task.getName())
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
        if (canceledTasks.size() > 0) {
            getJbpmService().saveTaskInstances(canceledTasks);
        }
    }

    private boolean isExpired(TaskInstance task) {
        Date date = task.getDueDate();
        return (date != null && date.before(new Date()));
    }

    private AutomationService getAutomationService() throws Exception {
        if (automationService == null) {
            automationService = Framework.getService(AutomationService.class);
        }
        return automationService;
    }

    protected CoreSession openSession() throws Exception {
        RepositoryManager repositoryManager = Framework.getService(RepositoryManager.class);
        Repository repository = repositoryManager.getDefaultRepository();
        return repository.open();
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

}
