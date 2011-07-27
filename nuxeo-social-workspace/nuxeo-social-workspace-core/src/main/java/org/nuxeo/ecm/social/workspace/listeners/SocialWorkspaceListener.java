/*
 * (C) Copyright 2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 */

package org.nuxeo.ecm.social.workspace.listeners;

import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_CREATED;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_UPDATED;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.isSocialWorkspace;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialWorkspace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;

/**
 * Listener handling creation and modification of a SocialWorkspace.
 * <p>
 *
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class SocialWorkspaceListener implements EventListener {

    private static final Log log = LogFactory.getLog(SocialWorkspaceListener.class);

    public static final String DO_NOT_PROCESS = "doNotProcess";

    @Override
    public void handleEvent(Event event) throws ClientException {
        if (!(event.getContext() instanceof DocumentEventContext)) {
            return;
        }

        DocumentEventContext ctx = (DocumentEventContext) event.getContext();
        if (ctx.hasProperty(DO_NOT_PROCESS)) {
            return;
        }

        DocumentModel doc = ctx.getSourceDocument();
        if (!isSocialWorkspace(doc)) {
            return;
        }

        doc.putContextData(DO_NOT_PROCESS, true);
        SocialWorkspace socialWorkspace = toSocialWorkspace(doc);
        if (DOCUMENT_CREATED.equals(event.getName())) {
            initializeSocialWorkspace(socialWorkspace, ctx);
        } else if (DOCUMENT_UPDATED.equals(event.getName())) {
            updateSocialWorkspaceVisibility(socialWorkspace);
        }
    }

    protected void initializeSocialWorkspace(SocialWorkspace socialWorkspace,
            EventContext ctx) throws ClientException {
        socialWorkspace.initialize(ctx.getPrincipal().getName());
    }

    protected void updateSocialWorkspaceVisibility(
            SocialWorkspace socialWorkspace) {
        if (socialWorkspace.isPublic()) {
            socialWorkspace.makePublic();
        } else if (socialWorkspace.isPrivate()) {
            socialWorkspace.makePrivate();
        }
    }

}
