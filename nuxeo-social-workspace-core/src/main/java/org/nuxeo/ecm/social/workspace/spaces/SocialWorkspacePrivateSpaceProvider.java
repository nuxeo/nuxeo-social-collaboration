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
 * Contributors:
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.nuxeo.ecm.social.workspace.spaces;

import static org.nuxeo.ecm.social.workspace.SocialConstants.PRIVATE_DASHBOARD_SPACE_NAME;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.isSocialWorkspace;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialWorkspace;

import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.spaces.api.AbstractSpaceProvider;
import org.nuxeo.ecm.spaces.api.Space;
import org.nuxeo.ecm.spaces.api.exceptions.SpaceException;

/**
 * Creates the default Private {@link Space} for a Social Workspace.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 */
public class SocialWorkspacePrivateSpaceProvider extends AbstractSpaceProvider {

    @Override
    protected Space doGetSpace(CoreSession session, DocumentModel contextDocument, String spaceName,
            Map<String, String> parameters) throws SpaceException {
        try {
            if (isSocialWorkspace(contextDocument)) {
                SocialWorkspace socialWorkspace = toSocialWorkspace(contextDocument);
                DocumentModel doc = session.getDocument(new PathRef(socialWorkspace.getPrivateDashboardSpacePath()));
                return doc.getAdapter(Space.class);
            } else {
                // assume dashboard spaces root
                DocumentModel doc = session.getDocument(new PathRef(contextDocument.getPathAsString() + "/"
                        + PRIVATE_DASHBOARD_SPACE_NAME));
                return doc.getAdapter(Space.class);
            }
        } catch (ClientException e) {
            throw new SpaceException(e);
        }
    }

    @Override
    public boolean isReadOnly(CoreSession session) {
        return false;
    }

}
