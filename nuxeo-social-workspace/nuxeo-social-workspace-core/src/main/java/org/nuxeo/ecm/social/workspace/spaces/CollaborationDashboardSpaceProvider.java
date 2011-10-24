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
package org.nuxeo.ecm.social.workspace.spaces;

import static org.nuxeo.ecm.social.workspace.SocialConstants.COLLABORATION_DASHBOARD_SPACE_NAME;
import static org.nuxeo.ecm.spaces.api.Constants.SPACE_DOCUMENT_TYPE;

import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;
import org.nuxeo.ecm.spaces.api.AbstractSpaceProvider;
import org.nuxeo.ecm.spaces.api.Space;
import org.nuxeo.ecm.spaces.api.exceptions.SpaceException;
import org.nuxeo.ecm.spaces.helper.WebContentHelper;
import org.nuxeo.ecm.user.center.dashboard.AbstractDashboardSpaceCreator;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 * @since 5.4.3
 */
public class CollaborationDashboardSpaceProvider extends AbstractSpaceProvider {

    private static final Log log = LogFactory.getLog(CollaborationDashboardSpaceProvider.class);

    @Override
    public boolean isReadOnly(CoreSession session) {
        return false;
    }

    @Override
    protected Space doGetSpace(CoreSession session,
            DocumentModel contextDocument, String spaceName,
            Map<String, String> parameters) throws SpaceException {
        try {
            return getOrCreateSpace(session, parameters);
        } catch (ClientException e) {
            log.error("Unable to create or get global dashboard", e);
            return null;
        }
    }

    protected Space getOrCreateSpace(CoreSession session,
            Map<String, String> parameters) throws ClientException {
        DocumentModel socialWorkspaceContainer = getSocialWorkspaceService().getOrCreateSocialWorkspaceContainer(
                session);
        DocumentRef globalDashboardSpaceRef = new PathRef(
                socialWorkspaceContainer.getPathAsString(),
                COLLABORATION_DASHBOARD_SPACE_NAME);
        if (session.exists(globalDashboardSpaceRef)) {
            DocumentModel globalDashboardSpace = session.getDocument(globalDashboardSpaceRef);
            return globalDashboardSpace.getAdapter(Space.class);
        } else {
            DocumentModel globalDashboardSpace = getOrCreateGlobalDashboardSpace(
                    session, parameters);
            return globalDashboardSpace.getAdapter(Space.class);
        }

    }

    protected DocumentModel getOrCreateGlobalDashboardSpace(
            CoreSession session, Map<String, String> parameters)
            throws ClientException {
        CollaborationDashBoardSpaceCreator creator = new CollaborationDashBoardSpaceCreator(
                session, parameters);
        creator.runUnrestricted();
        return session.getDocument(creator.getGlobalDashboardSpaceRef());

    }

    protected SocialWorkspaceService getSocialWorkspaceService()
            throws ClientException {
        try {
            return Framework.getService(SocialWorkspaceService.class);
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

    class CollaborationDashBoardSpaceCreator extends
            AbstractDashboardSpaceCreator {

        DocumentRef globalDashboardSpaceRef;

        protected CollaborationDashBoardSpaceCreator(CoreSession session,
                Map<String, String> parameters) {
            super(session, parameters);
        }

        public DocumentRef getGlobalDashboardSpaceRef() {
            return globalDashboardSpaceRef;
        }

        public void run() throws ClientException {
            DocumentModel socialWorkspaceContainer = getSocialWorkspaceService().getOrCreateSocialWorkspaceContainer(
                    session);
            DocumentModel globalDashboardSpace = session.createDocumentModel(
                    socialWorkspaceContainer.getPathAsString(),
                    COLLABORATION_DASHBOARD_SPACE_NAME, SPACE_DOCUMENT_TYPE);
            globalDashboardSpace.setPropertyValue("dc:title",
                    "global dashboard space");
            globalDashboardSpace.setPropertyValue("dc:description",
                    "global dashboard space");
            globalDashboardSpace = session.createDocument(globalDashboardSpace);

            addInitialGadgets(globalDashboardSpace);
            globalDashboardSpaceRef = globalDashboardSpace.getRef();

        }

        protected void initializeGadgets(Space space, CoreSession session,
                Locale locale) throws ClientException {
            WebContentHelper.createOpenSocialGadget(space, session, locale,
                    "publicsocialworkspaces", 0, 0, 0);
            WebContentHelper.createOpenSocialGadget(space, session, locale,
                    "news", 0, 0, 1);
            WebContentHelper.createOpenSocialGadget(space, session, locale,
                    "userworkspaces", 0, 1, 0);
            WebContentHelper.createOpenSocialGadget(space, session, locale,
                    "useractivitystream", 0, 1, 1);
        }


    }

}
