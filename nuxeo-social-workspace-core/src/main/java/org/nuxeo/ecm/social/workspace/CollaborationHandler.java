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
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.nuxeo.ecm.social.workspace;

import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_CONTAINER_TYPE;

import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.platform.content.template.service.PostContentCreationHandler;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceContainerDescriptor;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class CollaborationHandler implements PostContentCreationHandler {

    public static final String DC_TITLE = "dc:title";

    public static final String DC_DESCRIPTION = "dc:description";

    @Override
    public void execute(CoreSession session) {
        try {
            SocialWorkspaceService socialWorkspaceService = Framework.getLocalService(SocialWorkspaceService.class);
            SocialWorkspaceContainerDescriptor socialWorkspaceContainer = socialWorkspaceService.getSocialWorkspaceContainerDescriptor();
            DocumentRef docRef = new PathRef(socialWorkspaceContainer.getPath());
            if (!session.exists(docRef)) {
                Path path = new Path(socialWorkspaceContainer.getPath());
                String parentPath = path.removeLastSegments(1).toString();
                String name = path.lastSegment();

                DocumentModel container = session.createDocumentModel(
                        parentPath, name, SOCIAL_WORKSPACE_CONTAINER_TYPE);
                container.setPropertyValue(DC_TITLE,
                        socialWorkspaceContainer.getTitle());
                container.setPropertyValue(DC_DESCRIPTION,
                        socialWorkspaceContainer.getDescription());
                session.createDocument(container);
            }
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

}
