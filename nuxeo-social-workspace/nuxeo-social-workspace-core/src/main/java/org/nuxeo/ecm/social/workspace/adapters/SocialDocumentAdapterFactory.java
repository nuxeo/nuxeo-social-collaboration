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
package org.nuxeo.ecm.social.workspace.adapters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.adapter.DocumentAdapterFactory;
import org.nuxeo.ecm.social.workspace.SocialConstants;

/**
 * Factory instantiating {@link SocialDocumentAdapterImpl} adapter if the
 * document type is {@code SocialDocument}.
 *
 * @author Benjamin JALON <bjalon@nuxeo.com>
 *
 */
public class SocialDocumentAdapterFactory implements DocumentAdapterFactory {

    protected static final Log log = LogFactory.getLog(SocialDocumentAdapterFactory.class);

    @Override
    public Object getAdapter(DocumentModel doc, Class<?> itf) {

        if (doc.hasFacet(SocialConstants.SOCIAL_DOCUMENT_FACET)) {
            try {
                return new SocialDocumentAdapterImpl(doc);
            } catch (ClientException e) {
                log.error(e.getMessage() + " : Adapter returned is null");
            }
        }
        return null;
    }

}
