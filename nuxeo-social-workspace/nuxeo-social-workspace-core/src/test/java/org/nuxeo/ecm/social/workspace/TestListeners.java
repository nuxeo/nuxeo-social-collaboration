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
 *     ronan
 */
package org.nuxeo.ecm.social.workspace;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 *
 */
@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.social.workspace.core" })
public class TestListeners {

    public static final String NAME_SOCIAL_WORKSPACE = "sws";

    @Inject
    protected CoreSession session;

    @Inject
    protected UserManager userManager;

    protected DocumentModel createDocumentModel(String path, String name,
            String type) throws ClientException {
        DocumentModel doc = session.createDocumentModel(path, name, type);
        doc = session.createDocument(doc);
        session.save();
        return doc;
    }

    // @Test
    public void testGroupListeners() throws Exception {

        DocumentModel sws = createDocumentModel(
                session.getRootDocument().getPathAsString(),
                NAME_SOCIAL_WORKSPACE, "SocialWorkspace");

        assertNotNull(userManager);
        String adminGroupName = SocialWorkspaceHelper.getCommunityAdministratorsGroupName(sws);
        NuxeoGroup adminGroup = userManager.getGroup(adminGroupName);
        assertNotNull(adminGroup);

        String membersGroupName = SocialWorkspaceHelper.getCommunityMembersGroupName(sws);
        NuxeoGroup membersGroup = userManager.getGroup(membersGroupName);
        assertNotNull(membersGroup);

        session.removeDocument(sws.getRef());
        session.save();

        adminGroup = userManager.getGroup(adminGroupName);
        assertNull(adminGroup);
        membersGroup = userManager.getGroup(membersGroupName);
        assertNull(membersGroup);
    }

    @Test(expected = ClientException.class)
    public void testCommunityDocumentManagement() throws Exception {
        DocumentModel sws = createDocumentModel(
                session.getRootDocument().getPathAsString(),
                NAME_SOCIAL_WORKSPACE, SocialConstants.SOCIAL_WORKSPACE_TYPE);
        DocumentModel publicationSection = session.getChild(sws.getRef(),
                SocialConstants.ROOT_SECTION_NAME);
        DocumentModel privateNewsSection = session.getChild(
                publicationSection.getRef(), SocialConstants.NEWS_SECTION_NAME);

        DocumentModel news1 = createDocumentModel(sws.getPathAsString(),
                "news 1", SocialConstants.NEWS_TYPE);
        assertNotNull(news1);
        assertFalse("ae",news1.isProxy());
        DocumentRef privateNewsSectionRef = privateNewsSection.getRef();

        DocumentModel publishedNews = session.getChild(privateNewsSectionRef,
                news1.getName());
        assertNotNull(
                "A news called news 1 should be found as published in the private news section.",
                publishedNews);
//        assertFalse("ae",publishedNews.isProxy());
        assertTrue("", publishedNews.isProxy());

        DocumentModel wrongPlacedNews = createDocumentModel("/",
                "wrong place of creation", SocialConstants.NEWS_TYPE);

        session.getChild(privateNewsSectionRef, wrongPlacedNews.getName());
        fail(String.format(
                "The news called \"%s\" shouldn't been plublished in the private news section.",
                wrongPlacedNews.getName()));
    }

}
