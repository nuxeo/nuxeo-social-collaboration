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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.nuxeo.ecm.social.workspace.SocialConstants.NEWS_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.PRIVATE_SECTION_RELATIVE_PATH;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;

import java.io.Serializable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.core.versioning.VersioningService;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper;
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
@Deploy("org.nuxeo.ecm.social.workspace.core")
public class TestListeners {

    public static final String SOCIAL_WORKSPACE_NAME = "sws";

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

    @Test
    public void testGroupListeners() throws Exception {

        DocumentModel sws = createDocumentModel(
                session.getRootDocument().getPathAsString(),
                SOCIAL_WORKSPACE_NAME, "SocialWorkspace");

        assertNotNull(userManager);
        String adminGroupName = SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupName(sws);
        DocumentModel adminGroup = userManager.getGroupModel(adminGroupName);
        assertNotNull(adminGroup);
        assertEquals(
                SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupLabel(sws),
                adminGroup.getProperty(userManager.getGroupSchemaName(),
                        userManager.getGroupLabelField()));

        String membersGroupName = SocialWorkspaceHelper.getSocialWorkspaceMembersGroupName(sws);
        DocumentModel membersGroup = userManager.getGroupModel(membersGroupName);
        assertNotNull(membersGroup);
        assertEquals(
                SocialWorkspaceHelper.getSocialWorkspaceMembersGroupLabel(sws),
                membersGroup.getProperty(userManager.getGroupSchemaName(),
                        userManager.getGroupLabelField()));

        session.removeDocument(sws.getRef());
        session.save();

        adminGroup = userManager.getGroupModel(adminGroupName);
        assertNull(adminGroup);
        membersGroup = userManager.getGroupModel(membersGroupName);
        assertNull(membersGroup);
    }

    @Test
    public void testNewsPublication() throws Exception {
        DocumentModel sws = createDocumentModel(
                session.getRootDocument().getPathAsString(),
                SOCIAL_WORKSPACE_NAME, SOCIAL_WORKSPACE_TYPE);

        DocumentModel nominalNews = createDocumentModel(sws.getPathAsString(),
                "nominal news", NEWS_TYPE);
        DocumentModel publicationOfTheNews = getTheProxyOfTheNews(sws);
        assertNotNull("There should exist a proxy of the doc \"nominal news\"",
                publicationOfTheNews);
        String publicationOfTheNewsId = publicationOfTheNews.getId();

        Serializable originalNewsDcCreator = nominalNews.getProperty(
                "dc:creator").getValue();
        assertNotNull("The \"dc:creator\" of the original news should exist",
                originalNewsDcCreator);
        assertEquals(
                "The \"dc:creator\" of the original news should be \"Administrator\"",
                "Administrator", originalNewsDcCreator);

        Serializable publicationDcCreator = publicationOfTheNews.getPropertyValue("dc:creator");
        assertNotNull("The \"dc:creator\" of the original news should exist",
                publicationDcCreator);
        assertEquals(
                "The \"dc:creator\" of the original news and of its publication should be the same",
                originalNewsDcCreator, publicationDcCreator);

        Property publicationLastModifiedDate = publicationOfTheNews.getProperty("dc:modified");
        assertNotNull(
                "For the news publication the date of the last modification should exists",
                publicationLastModifiedDate);

        nominalNews.putContextData(VersioningService.VERSIONING_OPTION,
                VersioningOption.MAJOR);
        session.saveDocument(nominalNews);

        publicationOfTheNews = getTheProxyOfTheNews(sws);
        assertEquals(publicationOfTheNewsId, publicationOfTheNews.getId());
    }

    protected DocumentModel getTheProxyOfTheNews(DocumentModel sws)
            throws ClientException {
        String path = sws.getPathAsString() + "/"
                + PRIVATE_SECTION_RELATIVE_PATH;
        DocumentRef pathRef = new PathRef(path);
        return session.getDocument(pathRef);
    }
}
