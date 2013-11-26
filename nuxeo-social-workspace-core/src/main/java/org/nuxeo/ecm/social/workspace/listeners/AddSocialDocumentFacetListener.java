/*
 * (C) Copyright 2006-2011 Nuxeo SAS (http://nuxeo.com/) and others.
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
 *     Nuxeo - initial API and implementation
 */
package org.nuxeo.ecm.social.workspace.listeners;

import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.ABOUT_TO_CREATE;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_CREATED_BY_COPY;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_MOVED;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_DOCUMENT_FACET;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.schema.FacetNames;
import org.nuxeo.ecm.platform.types.Type;
import org.nuxeo.ecm.platform.types.TypeManager;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;
import org.nuxeo.runtime.api.Framework;

/**
 * Adds {@code SocialDocument} facet to documents created, copied or moved in a
 * {@code SocialWorkspace}.
 * <p>
 * Do not add the facet if the document has the Folderish facet, or if it's not
 * a
 *
 * @author <a href="mailto:rlegall@nuxeo.com">Ronan Le Gall</a>
 */
public class AddSocialDocumentFacetListener implements EventListener {

    private static final List<String> validEventTypes = Arrays.asList(
            DOCUMENT_CREATED_BY_COPY, DOCUMENT_MOVED, ABOUT_TO_CREATE);

    @Override
    public void handleEvent(Event event) throws ClientException {

        String eventName = event.getName();
        if (!validEventTypes.contains(eventName)) {
            return;
        }

        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }

        List<String> allowedTypes = getSocialWorkspaceAllowedSubTypes();
        DocumentModel document = ((DocumentEventContext) ctx).getSourceDocument();
        if (document.hasFacet(FacetNames.FOLDERISH)
                || !allowedTypes.contains(document.getType())) {
            return;
        }
        CoreSession session = ctx.getCoreSession();
        DocumentModel documentParent = session.getDocument(document.getParentRef());
        SocialWorkspace sws = getSocialWorkspaceService().getSocialWorkspace(
                documentParent);

        if (sws == null) {
            return;
        }

        document.addFacet(SOCIAL_DOCUMENT_FACET);
        if (DOCUMENT_MOVED.equals(eventName)) {
            session.saveDocument(document);
        }
    }

    private List<String> getSocialWorkspaceAllowedSubTypes() {
        try {
            TypeManager typeManager = Framework.getService(TypeManager.class);
            Collection<Type> allowedSubTypes = typeManager.findAllAllowedSubTypesFrom(SOCIAL_WORKSPACE_TYPE);
            List<String> types = new ArrayList<String>();
            for (Type type : allowedSubTypes) {
                types.add(type.getId());
            }
            return types;
        } catch (Exception e) {
            throw new ClientRuntimeException(e);
        }
    }

    private static SocialWorkspaceService getSocialWorkspaceService() {
        try {
            return Framework.getService(SocialWorkspaceService.class);
        } catch (Exception e) {
            throw new ClientRuntimeException(e);
        }
    }

}
