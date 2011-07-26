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
 *     Benjamin JALON <bjalon@nuxeo.com>
 */
package org.nuxeo.ecm.social.workspace;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.runtime.api.Framework;

/**
 * @author Benjamin JALON <bjalon@nuxeo.com>
 *
 */
public class ToolsForTests {

    /**
     * Create document and wait for all post-commit listener execution
     */
    public static DocumentModel createDocumentModel(CoreSession session,
            String pathAsString, String name, String type) throws Exception {
        DocumentModel doc = session.createDocumentModel(pathAsString, name,
                type);
        doc = session.createDocument(doc);
        session.save(); // fire post commit event listener
        session.save(); // flush the session to retrieve document
        Framework.getService(EventService.class).waitForAsyncCompletion();
        return doc;
    }

    /**
     * Create document and wait for all post-commit listener execution
     */
    public static DocumentModel createSocialDocument(CoreSession session,
            String pathAsString, String name, String type, boolean isPublic)
            throws Exception {
        DocumentModel doc = session.createDocumentModel(pathAsString, name,
                type);
        doc.setPropertyValue(SocialConstants.FIELD_SOCIAL_DOCUMENT_IS_PUBLIC,
                isPublic);
        doc = session.createDocument(doc);
        session.save(); // fire post commit event listener
        session.save(); // flush the session to retrieve document
        Framework.getService(EventService.class).waitForAsyncCompletion();
        return doc;
    }

}
