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
 *     ronan
 */
package org.nuxeo.ecm.social.workspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.platform.jbpm.test.JbpmUTConstants.CORE_BUNDLE_NAME;
import static org.nuxeo.ecm.platform.jbpm.test.JbpmUTConstants.TESTING_BUNDLE_NAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.ARTICLE_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_SOCIAL_DOCUMENT_IS_PUBLIC;
import static org.nuxeo.ecm.social.workspace.SocialConstants.NEWS_ITEM_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_DOCUMENT_FACET;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.VALIDATE_SOCIAL_WORKSPACE_TASK_NAME;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialDocument;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
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
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;
import org.nuxeo.ecm.social.workspace.adapters.SocialDocument;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.listeners.CheckSocialWorkspaceValidationTasks;
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
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-listener-contrib.xml",
        "org.nuxeo.ecm.social.workspace.core:OSGI-INF/social-workspace-service-contrib.xml",
        "org.nuxeo.ecm.automation.core",
        "org.nuxeo.ecm.platform.jbpm.automation",
        "org.nuxeo.ecm.automation.features", CORE_BUNDLE_NAME,
        TESTING_BUNDLE_NAME })
@LocalDeploy({ "org.nuxeo.ecm.automation.core:override-social-workspace-operation-chains-contrib.xml" })
public class TestVisibilityManagement extends AbstractSocialWorkspaceTest {

    private static final Log log = LogFactory.getLog(TestVisibilityManagement.class);

    public static final String SOCIAL_WORKSPACE_NAME = "sws";

    @Inject
    protected JbpmService jbpmService;

    @Inject
    protected EventService eventService;

    protected DocumentModel socialWorkspaceDoc;

    protected SocialWorkspace socialWorkspace;

    protected NuxeoPrincipal notMember;

    protected NuxeoPrincipal member;

    protected NuxeoPrincipal administrator;

    @Before
    public void setUp() throws Exception {
        socialWorkspace = createSocialWorkspace(SOCIAL_WORKSPACE_NAME, true);
        socialWorkspaceDoc = socialWorkspace.getDocument();

        String membersGroup = socialWorkspace.getMembersGroupName();
        String administratorsGroup = socialWorkspace.getAdministratorsGroupName();

        notMember = new NuxeoPrincipalImpl("notMember");
        member = createUserWithGroup("member", membersGroup);
        assertTrue(member.getAllGroups().contains(membersGroup));
        administrator = createUserWithGroup("administratorOfSW",
                administratorsGroup);
    }

    @Test
    public void testShouldNotBeSocialDocument() throws Exception {
        DocumentModel note = createDocument(
                socialWorkspaceDoc.getPathAsString(),
                "wrong place of creation", "Note");
        assertNull(toSocialDocument(note));

        DocumentModel outOfSocialWorkspace = createDocument("/",
                "wrong place of creation", ARTICLE_TYPE);
        assertNull(toSocialDocument(outOfSocialWorkspace));
    }

    @Test
    public void shouldCreatePrivateProxyForSocialDocumentPrivate()
            throws Exception {
        DocumentModel privateNews = createSocialDocument(
                socialWorkspaceDoc.getPathAsString(), "private news",
                NEWS_ITEM_TYPE, false);
        SocialDocument socialDocument = toSocialDocument(privateNews);

        assertTrue(socialDocument.isDocumentInSocialWorkspace());
        assertTrue(socialDocument.isRestrictedToMembers());
        assertFalse(socialDocument.isPublic());
        assertNull(socialDocument.getPublicDocument());
        assertNotNull(socialDocument.getRestrictedDocument());
        assertTrue(socialDocument.getRestrictedDocument().isProxy());
        checkRestricted(socialDocument.getRestrictedDocument());

        socialDocument.makePublic();
        assertFalse(socialDocument.isRestrictedToMembers());
        assertTrue(socialDocument.isPublic());
        assertNotNull(socialDocument.getPublicDocument());
        assertNull(socialDocument.getRestrictedDocument());
        assertTrue(socialDocument.getPublicDocument().isProxy());
        checkPublic(socialDocument.getPublicDocument());

        socialDocument.restrictToMembers();
        assertTrue(socialDocument.isRestrictedToMembers());
        assertFalse(socialDocument.isPublic());
        assertNull(socialDocument.getPublicDocument());
        assertNotNull(socialDocument.getRestrictedDocument());
        assertTrue(socialDocument.getRestrictedDocument().isProxy());
        checkRestricted(socialDocument.getRestrictedDocument());
    }

    @Test
    public void shouldCreatePrivateProxyForSocialDocumentWithFacetAdded()
            throws Exception {
        DocumentModel socialDocumentFacetedNotePrivate = session.createDocumentModel(
                socialWorkspaceDoc.getPathAsString(), "Social Document Note1",
                "Note");
        socialDocumentFacetedNotePrivate.addFacet(SOCIAL_DOCUMENT_FACET);
        socialDocumentFacetedNotePrivate = session.createDocument(socialDocumentFacetedNotePrivate);
        session.save();
        eventService.waitForAsyncCompletion();
        session.save();
        socialDocumentFacetedNotePrivate = session.getDocument(socialDocumentFacetedNotePrivate.getRef());

        SocialDocument socialDocument = toSocialDocument(socialDocumentFacetedNotePrivate);
        assertTrue(socialDocument.isRestrictedToMembers());
        assertFalse(socialDocument.isPublic());
        assertNull(socialDocument.getPublicDocument());
        assertNotNull(socialDocument.getRestrictedDocument());
        assertTrue(socialDocument.getRestrictedDocument().isProxy());
        checkRestricted(socialDocument.getRestrictedDocument());

        DocumentModel socialDocumentFacetedNotePublic = session.createDocumentModel(
                socialWorkspaceDoc.getPathAsString(), "Social Document Note2",
                "Note");
        socialDocumentFacetedNotePublic.addFacet(SOCIAL_DOCUMENT_FACET);
        socialDocumentFacetedNotePublic.setPropertyValue(
                FIELD_SOCIAL_DOCUMENT_IS_PUBLIC, true);
        socialDocumentFacetedNotePublic = session.createDocument(socialDocumentFacetedNotePublic);
        session.save();
        eventService.waitForAsyncCompletion();
        session.save();
        socialDocumentFacetedNotePublic = session.getDocument(socialDocumentFacetedNotePublic.getRef());

        socialDocument = toSocialDocument(socialDocumentFacetedNotePublic);
        assertFalse(socialDocument.isRestrictedToMembers());
        assertTrue(socialDocument.isPublic());
        assertNotNull(socialDocument.getPublicDocument());
        assertTrue(socialDocument.getPublicDocument().isProxy());
        assertNull(socialDocument.getRestrictedDocument());
        checkPublic(socialDocument.getPublicDocument());

    }

    @Test
    public void shouldCreatePrivateProxyForSocialDocumentPublic()
            throws Exception {
        DocumentModel privateNews = createSocialDocument(
                socialWorkspaceDoc.getPathAsString(), "private news",
                NEWS_ITEM_TYPE, true);

        SocialDocument socialDocument = toSocialDocument(privateNews);

        assertFalse(socialDocument.isRestrictedToMembers());
        assertTrue(socialDocument.isPublic());
        assertNotNull(socialDocument.getPublicDocument());
        assertNull(socialDocument.getRestrictedDocument());
        assertTrue(socialDocument.getPublicDocument().isProxy());
        checkPublic(socialDocument.getPublicDocument());

        socialDocument.restrictToMembers();
        assertTrue(socialDocument.isDocumentInSocialWorkspace());
        assertTrue(socialDocument.isRestrictedToMembers());
        assertFalse(socialDocument.isPublic());
        assertNull(socialDocument.getPublicDocument());
        assertNotNull(socialDocument.getRestrictedDocument());
        assertTrue(socialDocument.getRestrictedDocument().isProxy());
        checkRestricted(socialDocument.getRestrictedDocument());

        socialDocument.makePublic();
        assertFalse(socialDocument.isRestrictedToMembers());
        assertTrue(socialDocument.isPublic());
        assertNotNull(socialDocument.getPublicDocument());
        assertNull(socialDocument.getRestrictedDocument());
        assertTrue(socialDocument.getPublicDocument().isProxy());
        checkPublic(socialDocument.getPublicDocument());
    }

    @Test
    public void shouldNotCreatePrivateProxyForArticlePrivate() throws Exception {
        DocumentModel privateArticle = createSocialDocument(
                socialWorkspaceDoc.getPathAsString(), "privatenews",
                ARTICLE_TYPE, false);

        SocialDocument socialDocument = toSocialDocument(privateArticle);

        assertTrue(socialDocument.isDocumentInSocialWorkspace());
        assertTrue(socialDocument.isRestrictedToMembers());
        assertFalse(socialDocument.isPublic());
        assertNull(socialDocument.getPublicDocument());
        assertNotNull(socialDocument.getRestrictedDocument());
        assertFalse(socialDocument.getRestrictedDocument().isProxy());
        checkRestricted(socialDocument.getRestrictedDocument());

        socialDocument.makePublic();
        assertFalse(socialDocument.isRestrictedToMembers());
        assertTrue(socialDocument.isPublic());
        assertNull(socialDocument.getRestrictedDocument());
        assertNotNull(socialDocument.getPublicDocument());
        assertTrue(socialDocument.getPublicDocument().isProxy());
        checkPublic(socialDocument.getPublicDocument());

        socialDocument.restrictToMembers();
        assertTrue(socialDocument.isRestrictedToMembers());
        assertFalse(socialDocument.isPublic());
        assertNull(socialDocument.getPublicDocument());
        assertNotNull(socialDocument.getRestrictedDocument());
        assertFalse(socialDocument.getRestrictedDocument().isProxy());
        checkRestricted(socialDocument.getRestrictedDocument());

    }

    @Test
    public void shouldNotCreatePrivateProxyForArticlePublic() throws Exception {
        DocumentModel privateArticle = createSocialDocument(
                socialWorkspaceDoc.getPathAsString(), "privatenews",
                ARTICLE_TYPE, true);

        SocialDocument socialDocument = toSocialDocument(privateArticle);

        assertFalse(socialDocument.isRestrictedToMembers());
        assertTrue(socialDocument.isPublic());
        assertNull(socialDocument.getRestrictedDocument());
        assertNotNull(socialDocument.getPublicDocument());
        assertTrue(socialDocument.getPublicDocument().isProxy());
        checkPublic(socialDocument.getPublicDocument());

        socialDocument.restrictToMembers();
        assertTrue(socialDocument.isDocumentInSocialWorkspace());
        assertTrue(socialDocument.isRestrictedToMembers());
        assertFalse(socialDocument.isPublic());
        assertNull(socialDocument.getPublicDocument());
        assertNotNull(socialDocument.getRestrictedDocument());
        assertFalse(socialDocument.getRestrictedDocument().isProxy());
        checkRestricted(socialDocument.getRestrictedDocument());

        socialDocument.makePublic();
        assertFalse(socialDocument.isRestrictedToMembers());
        assertTrue(socialDocument.isPublic());
        assertNull(socialDocument.getRestrictedDocument());
        assertNotNull(socialDocument.getPublicDocument());
        assertTrue(socialDocument.getPublicDocument().isProxy());
        checkPublic(socialDocument.getPublicDocument());
    }

    @Test
    public void testModeratedSocialWorkspaceCreation() throws Exception {
        assertNotNull(jbpmService);

        DocumentModel moderated = createDocument(
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

        moderated = createDocument(session.getRootDocument().getPathAsString(),
                "willBeRejected", SOCIAL_WORKSPACE_TYPE);
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
    public void testSocialWorkspaceCreationExpiration() throws Exception {
        DocumentModel socialWorkspace = createDocument(
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
        DocumentModel wk1 = createDocument(
                session.getRootDocument().getPathAsString(), "willBeExpired",
                SOCIAL_WORKSPACE_TYPE);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 15);

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

    protected void checkPublic(DocumentModel doc) throws ClientException {
        assertTrue(session.hasPermission(notMember, doc.getRef(),
                SecurityConstants.READ));
        assertFalse(session.hasPermission(notMember, doc.getRef(),
                SecurityConstants.READ_WRITE));
        assertTrue(session.hasPermission(member, doc.getRef(),
                SecurityConstants.READ_WRITE));
        assertTrue(session.hasPermission(administrator, doc.getRef(),
                SecurityConstants.EVERYTHING));
    }

    protected void checkRestricted(DocumentModel doc) throws ClientException {
        assertFalse(session.hasPermission(notMember, doc.getRef(),
                SecurityConstants.READ));
        assertTrue(session.hasPermission(member, doc.getRef(),
                SecurityConstants.READ_WRITE));
        assertTrue(session.hasPermission(administrator, doc.getRef(),
                SecurityConstants.EVERYTHING));
    }

}
