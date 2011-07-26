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

import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_REMOVED;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper;
import org.nuxeo.runtime.api.Framework;

/**
 * Remove social workspace associated groups
 * <ul>
 * <li>{doc_id}_administrators</li>
 * <li>{doc_id}_members</li>
 * </ul>
 *
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 * @since 5.4.1
 *
 */
public class RemoveSocialWorkspaceGroupListener implements EventListener {

    private UserManager userManager;

    private static final Log log = LogFactory.getLog(RemoveSocialWorkspaceGroupListener.class);

    public void handleEvent(Event event) throws ClientException {
        if (!DOCUMENT_REMOVED.equals(event.getName())) {
            return;
        }

        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }

        DocumentModel doc = ((DocumentEventContext) ctx).getSourceDocument();
        if (!SocialWorkspaceHelper.isSocialWorkspace(doc)) {
            return;
        }

        deleteGroup(SocialWorkspaceHelper.getSocialWorkspaceAdministratorsGroupName(doc));
        deleteGroup(SocialWorkspaceHelper.getSocialWorkspaceMembersGroupName(doc));
    }

    private void deleteGroup(String groupName) {
        try {
            getUserManager().deleteGroup(groupName);
        } catch (ClientException e) {
            log.warn("Cannot delete group: " + groupName, e);
        }
    }

    private UserManager getUserManager() throws ClientException {
        if (userManager == null) {
            try {
                userManager = Framework.getService(UserManager.class);
            } catch (Exception e) {
                throw new ClientException(e);
            }
        }
        return userManager;
    }

}
