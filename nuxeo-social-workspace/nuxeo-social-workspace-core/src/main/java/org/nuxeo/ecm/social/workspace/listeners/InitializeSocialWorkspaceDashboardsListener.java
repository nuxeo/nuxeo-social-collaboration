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

package org.nuxeo.ecm.social.workspace.listeners;

import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_CREATED;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.spaces.api.Constants;
import org.nuxeo.ecm.spaces.api.Space;
import org.nuxeo.opensocial.container.shared.layout.api.LayoutHelper;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class InitializeSocialWorkspaceDashboardsListener implements
        EventListener {

    @Override
    public void handleEvent(Event event) throws ClientException {
        if (!DOCUMENT_CREATED.equals(event.getName())) {
            return;
        }

        EventContext eventContext = event.getContext();
        if (eventContext instanceof DocumentEventContext) {
            DocumentEventContext documentEventContext = (DocumentEventContext) eventContext;
            DocumentModel doc = documentEventContext.getSourceDocument();
            if (Constants.SPACE_DOCUMENT_TYPE.equals(doc.getType())) {
                initializeDashboards(documentEventContext.getCoreSession(), doc);
            }
        }
    }

    private static void initializeDashboards(CoreSession session,
            DocumentModel doc) throws ClientException {
        Space space = doc.getAdapter(Space.class);
        space.initLayout(LayoutHelper.buildLayout(LayoutHelper.Preset.X_2_66_33));
        // add initial gadgets
    }

}
