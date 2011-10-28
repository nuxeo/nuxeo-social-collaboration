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
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_UPDATED;
import static org.nuxeo.ecm.core.schema.FacetNames.HIDDEN_IN_NAVIGATION;
import static org.nuxeo.ecm.social.workspace.SocialConstants.IN_SOCIAL_WORKSPACE_SUFFIX;

import java.security.Principal;

import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityBuilder;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class SocialWorkspaceActivityListener implements EventListener {

    @Override
    public void handleEvent(Event event) throws ClientException {
        if (!DOCUMENT_CREATED.equals(event.getName())
                && !DOCUMENT_UPDATED.equals(event.getName())) {
            return;
        }

        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }

        DocumentModel doc = ((DocumentEventContext) ctx).getSourceDocument();
        if (doc.hasFacet(HIDDEN_IN_NAVIGATION)) {
            // Not really interested if document is not visible.
            return;
        }

        SocialWorkspace socialWorkspace = Framework.getLocalService(
                SocialWorkspaceService.class).getSocialWorkspace(doc);
        if (socialWorkspace != null) {
            addActivity(event, ctx.getPrincipal(), doc, socialWorkspace);
        }
    }

    private void addActivity(Event event, Principal principal,
            DocumentModel doc, SocialWorkspace socialWorkspace) {
        ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
        Activity activity = new ActivityBuilder().verb(
                event.getName() + IN_SOCIAL_WORKSPACE_SUFFIX).actor(
                ActivityHelper.createUserActivityObject(principal)).displayActor(
                ActivityHelper.generateDisplayName(principal)).object(
                ActivityHelper.createDocumentActivityObject(doc)).displayObject(
                ActivityHelper.getDocumentTitle(doc)).target(
                ActivityHelper.createDocumentActivityObject(socialWorkspace.getDocument())).displayTarget(
                socialWorkspace.getTitle()).build();
        activityStreamService.addActivity(activity);
    }

}
