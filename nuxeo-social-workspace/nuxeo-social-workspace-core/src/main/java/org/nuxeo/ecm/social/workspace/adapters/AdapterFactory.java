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
package org.nuxeo.ecm.social.workspace.adapters;

import static org.nuxeo.ecm.social.workspace.SocialConstants.ARTICLE_TYPE;import static org.nuxeo.ecm.social.workspace.SocialConstants.REQUEST_SCHEMA;

import org.apache.commons.logging.Log;import org.apache.commons.logging.LogFactory;import org.nuxeo.ecm.core.api.ClientException;import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.adapter.DocumentAdapterFactory;import org.nuxeo.ecm.social.workspace.SocialConstants;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 *
 */
public class AdapterFactory implements DocumentAdapterFactory {

    private static final Log log = LogFactory.getLog(AdapterFactory.class);

    @Override
    public Object getAdapter(DocumentModel doc, Class<?> itf) {
        if (itf == Article.class && ARTICLE_TYPE.equals(doc.getType())) {
            return new ArticleAdapter(doc);
        }
        if (itf == SubscriptionRequest.class && doc.hasSchema(REQUEST_SCHEMA)) {
            return new SubscriptionRequestAdapter(doc);
        }
        if (doc.hasFacet(SocialConstants.SOCIAL_DOCUMENT_FACET)) {
            try {
                return new SocialDocumentAdapter(doc);
            } catch (ClientException e) {
                log.error(e.getMessage() + " : Adapter returned is null");
                log.debug(e, e);
            }
        }
        return null;
    }

}
