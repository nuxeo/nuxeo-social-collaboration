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
 *     ronan
 */
package org.nuxeo.ecm.social.workspace.listeners;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.platform.jbpm.test.JbpmUTConstants.CORE_BUNDLE_NAME;
import static org.nuxeo.ecm.platform.jbpm.test.JbpmUTConstants.TESTING_BUNDLE_NAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_DOCUMENT_FACET;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_SECTION_NAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.VALIDATE_SOCIAL_WORKSPACE_TASK_NAME;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.event.impl.EventImpl;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.jbpm.JbpmListFilter;
import org.nuxeo.ecm.platform.jbpm.JbpmService;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.SocialConstants;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:rlegall@nuxeo.com">Ronan Le Gall</a>
 *
 */
@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
// no listener configured
@Deploy({
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-core-types-contrib.xml",
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-life-cycle-contrib.xml",
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-content-template-contrib.xml",
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-adapters-contrib.xml",
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-notifications-contrib.xml",
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-operation-chains-contrib.xml",
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-event-handlers-contrib.xml",
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-service-contrib.xml",
        "org.nuxeo.ecm.automation.core",
        "org.nuxeo.ecm.platform.jbpm.automation",
        "org.nuxeo.ecm.automation.features", CORE_BUNDLE_NAME,
        TESTING_BUNDLE_NAME })
@LocalDeploy({ "org.nuxeo.ecm.automation.core:override-social-workspace-operation-chains-contrib.xml" })
public class TestCreateSocialDocumentListener {

    private static final Log log = LogFactory.getLog(TestCreateSocialDocumentListener.class);

    public static final String SOCIAL_WORKSPACE_NAME = "sws";

    @Inject
    protected CoreSession session;

    @Inject
    protected UserManager userManager;

    @Inject
    protected JbpmService jbpmService;

    @Inject
    protected AutomationService automationService;

    @Inject
    protected EventService eventService;

    CreateSocialDocumentListener underTest;

    DocumentModel socialWorkspace;

    @Before
    public void setUp() throws Exception {
        underTest = new CreateSocialDocumentListener();
        socialWorkspace = createDocumentModel(
                session.getRootDocument().getPathAsString(),
                SOCIAL_WORKSPACE_NAME, SOCIAL_WORKSPACE_TYPE);
    }

    protected DocumentModel createDocumentModel(String path, String name,
            String type) throws ClientException {
        DocumentModel doc = session.createDocumentModel(path, name, type);
        doc = session.createDocument(doc);
        session.save();
        eventService.waitForAsyncCompletion();
        return doc;
    }

    @Test
    public void testListener() throws ClientException {
        DocumentModel privateNews = createDocumentModel(
                socialWorkspace.getPathAsString(), "private news",
                SocialConstants.NEWS_TYPE);

        underTest.publishSocialDocumentInPrivateSection(session, privateNews);

        DocumentRef privateNewsSection = new PathRef(
                socialWorkspace.getPathAsString() + "/" + SOCIAL_SECTION_NAME);

        DocumentModel publishedNews = session.getChild(privateNewsSection,
                privateNews.getName());
        assertNotNull(
                "A news called news 1 should be found as published in the private news section.",
                publishedNews);
        assertTrue("", publishedNews.isProxy());

        DocumentModel wrongPlacedNews = createDocumentModel("/",
                "wrong place of creation", SocialConstants.NEWS_TYPE);
        underTest.publishSocialDocumentInPrivateSection(session,
                wrongPlacedNews);

        String query = String.format(
                "SELECT * FROM Note WHERE ecm:path STARTSWITH '%s/' "
                        + "AND  ecm:isProxy =1 AND ecm:name ='%s'",
                socialWorkspace.getPathAsString(), wrongPlacedNews.getName());

        DocumentModelList unpublishedNews = session.query(query);

        assertEquals(
                "There should have no publication of \"wrong place of creation\"",
                0, unpublishedNews.size());

        DocumentModel socialDocumentFacetedNote = session.createDocumentModel(
                socialWorkspace.getPathAsString(), "Social Document Note",
                "Note");
        socialDocumentFacetedNote.addFacet(SOCIAL_DOCUMENT_FACET);
        socialDocumentFacetedNote = session.createDocument(socialDocumentFacetedNote);
        session.save();

        underTest.publishSocialDocumentInPrivateSection(session,
                socialDocumentFacetedNote);

        DocumentModel publishedNote = session.getChild(privateNewsSection,
                socialDocumentFacetedNote.getName());

        assertNotNull(publishedNote);
    }

    @Test
    public void testEventHandle() throws Exception {
        DocumentModel privateNews = createDocumentModel(
                socialWorkspace.getPathAsString(), "private news",
                SocialConstants.NEWS_TYPE);

        assertEquals(2, session.getParentDocuments(privateNews.getRef()).size());

        EventContext context = new DocumentEventContext(session, null,
                privateNews);
        Event createDocumentEvent = new EventImpl("", context, 0);

        underTest.handleEvent(createDocumentEvent);

        DocumentRef privateNewsSectionRef = new PathRef(
                socialWorkspace.getPathAsString() + "/" + SOCIAL_SECTION_NAME);

        DocumentModel publishedNews = session.getChild(privateNewsSectionRef,
                privateNews.getName());

        assertNotNull(
                "A news called news 1 should be found as published in the private news section.",
                publishedNews);
        assertTrue("", publishedNews.isProxy());
    }

    @Test
    public void testModeratedSocialWorkspaceCreation() throws ClientException,
            InterruptedException {
        assertNotNull(jbpmService);

        DocumentModel moderated = createDocumentModel(
                session.getRootDocument().getPathAsString(), "willBeApproved",
                SOCIAL_WORKSPACE_TYPE);
        assertEquals("project", moderated.getCurrentLifeCycleState());
        List<TaskInstance> tasks = jbpmService.getTaskInstances(moderated,
                null, (JbpmListFilter) null);
        assertEquals(1, tasks.size());
        assertEquals(VALIDATE_SOCIAL_WORKSPACE_TASK_NAME,
                tasks.get(0).getName());
        assertTrue(tasks.get(0).isOpen());

        assertTrue(moderated.followTransition("approve"));
        removeValidationTasks(moderated);
        session.save();
        assertEquals("approved", moderated.getCurrentLifeCycleState());

        tasks = jbpmService.getTaskInstances(moderated, null,
                (JbpmListFilter) null);
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());

        moderated = createDocumentModel(
                session.getRootDocument().getPathAsString(), "willBeRejected",
                SOCIAL_WORKSPACE_TYPE);
        assertEquals("project", moderated.getCurrentLifeCycleState());
        assertFalse(jbpmService.getTaskInstances(moderated, null,
                (JbpmListFilter) null).isEmpty());
        assertTrue(moderated.followTransition("delete"));
        removeValidationTasks(moderated);
        session.save();
        assertEquals("deleted", moderated.getCurrentLifeCycleState());

        assertTrue(jbpmService.getTaskInstances(moderated, null,
                (JbpmListFilter) null).isEmpty());
    }

    @Test
    public void testSocialWorkspaceCreationExpiration() throws ClientException,
            InterruptedException {
        DocumentModel socialWorkspace = createDocumentModel(
                session.getRootDocument().getPathAsString(), "willBeExpired",
                SOCIAL_WORKSPACE_TYPE);
        String id = socialWorkspace.getId();
        assertEquals("project", socialWorkspace.getCurrentLifeCycleState());

        // Change task due date at two days before
        List<TaskInstance> tasks = jbpmService.getTaskInstances(
                socialWorkspace, null, (JbpmListFilter) null);
        assertFalse(tasks.isEmpty());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -2);
        tasks.get(0).setDueDate(cal.getTime());
        jbpmService.saveTaskInstances(tasks);

        CheckSocialWorkspaceValidationTasks fakeListener = new CheckSocialWorkspaceValidationTasks();
        DocumentEventContext docCtx = new DocumentEventContext(session,
                session.getPrincipal(), socialWorkspace);
        fakeListener.handleEvent(new EventImpl("checkExpiredTasksSignal",
                docCtx));
        session.save();

        DocumentModel doc = session.getDocument(new IdRef(id));
        assertEquals("deleted", doc.getCurrentLifeCycleState());
    }

    @Test
    public void testTaskDueDate() throws Exception {
        DocumentModel wk1 = createDocumentModel(
                session.getRootDocument().getPathAsString(), "willBeExpired",
                SOCIAL_WORKSPACE_TYPE);

        int validation = Framework.getService(SocialWorkspaceService.class).getValidationDays();
        assertEquals(15, validation);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, validation);

        // Change task due date at two days before
        List<TaskInstance> tasks = jbpmService.getTaskInstances(wk1, null,
                (JbpmListFilter) null);
        assertEquals(1, tasks.size());
        TaskInstance task = tasks.get(0);

        Calendar dueDate = Calendar.getInstance();
        dueDate.setTime(task.getDueDate());

        assertEquals(cal.get(Calendar.DATE), dueDate.get(Calendar.DATE));
    }

    protected void removeValidationTasks(DocumentModel doc) {
        List<TaskInstance> canceledTasks = new ArrayList<TaskInstance>();
        try {
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
