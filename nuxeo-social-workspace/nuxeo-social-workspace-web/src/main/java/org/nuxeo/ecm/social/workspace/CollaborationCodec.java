/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
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

import static org.nuxeo.ecm.social.workspace.SocialConstants.DASHBOARD_SPACES_CONTAINER_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_FACET;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.core.schema.FacetNames;
import org.nuxeo.ecm.platform.types.Type;
import org.nuxeo.ecm.platform.types.TypeManager;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.platform.url.codec.DocumentIdCodec;
import org.nuxeo.ecm.platform.url.codec.DocumentPathCodec;
import org.nuxeo.ecm.platform.url.codec.api.DocumentViewCodec;
import org.nuxeo.ecm.platform.url.service.AbstractDocumentViewCodec;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionHelper;

/**
 * Codec for Collaboration, handle id and path URLs.
 * <p>
 * When using id URLs, check if the document can be seen in the Collaboration
 * view, if not, display the default view of the document.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public class CollaborationCodec extends AbstractDocumentViewCodec {

    private static final Log log = LogFactory.getLog(CollaborationCodec.class);

    public static final String PREFIX = "collaboration";

    public static final String ID_URL_PATTERN = "/(\\w+)/([a-zA-Z_0-9\\-]+)(/([a-zA-Z_0-9\\-\\.]*))?(/)?(\\?(.*)?)?";

    public static final String PATH_URL_PATTERN = "/" // slash
            + "([\\w\\.]+)" // server name (group 1)
            + "(?:/(.*))?" // path (group 2) (optional)
            + "@([\\w\\-\\.]+)" // view id (group 3)
            + "/?" // final slash (optional)
            + "(?:\\?(.*)?)?"; // query (group 4) (optional)

    @Override
    public String getPrefix() {
        if (prefix != null) {
            return prefix;
        }
        return PREFIX;
    }

    @Override
    public DocumentView getDocumentViewFromUrl(String url) {
        Pattern pattern = Pattern.compile(getPrefix() + ID_URL_PATTERN);
        DocumentViewCodec codec = null;
        Matcher m = pattern.matcher(url);
        if (m.matches()) {
            codec = new DocumentIdCodec();
        } else {
            pattern = Pattern.compile(getPrefix() + PATH_URL_PATTERN);
            m = pattern.matcher(url);
            if (m.matches()) {
                codec = new DocumentPathCodec();
            }
        }
        if (codec != null) {
            codec.setPrefix(getPrefix());
            DocumentView docView = codec.getDocumentViewFromUrl(url);
            updateDocumentView(docView);
            return docView;
        }
        return null;
    }

    protected DocumentView updateDocumentView(final DocumentView docView) {
        boolean transactionStarted = false;
        if (!TransactionHelper.isTransactionActive()) {
            TransactionHelper.startTransaction();
            transactionStarted = true;
        }
        try {
            final DocumentLocation docLoc = docView.getDocumentLocation();
            new UnrestrictedSessionRunner(docLoc.getServerName()) {
                @Override
                public void run() throws ClientException {
                    computeDocumentView(session, docView);
                }
            }.runUnrestricted();
        } catch (ClientException e) {
            // do nothing
        } finally {
            if (transactionStarted) {
                TransactionHelper.commitOrRollbackTransaction();
            }
        }
        return docView;
    }

    protected DocumentView computeDocumentView(CoreSession session,
            DocumentView docView) throws ClientException {
        DocumentModel doc = session.getDocument(docView.getDocumentLocation().getDocRef());
        // do nothing for the 'public' dashboard (sw/social document)
        if (DASHBOARD_SPACES_CONTAINER_TYPE.equals(doc.getType())) {
            return null;
        }

        if (doc.hasFacet(SOCIAL_WORKSPACE_FACET)) {
            docView.setDocumentLocation(new DocumentLocationImpl(
                    session.getChild(doc.getRef(), "social")));
            docView.setViewId("social_dashboard");
        } else {
            TypeManager typeService = Framework.getLocalService(TypeManager.class);
            Type type = typeService.getType(doc.getType());
            if (doc.hasFacet(FacetNames.FOLDERISH)
                    || !typeService.getAllowedSubTypes(SOCIAL_WORKSPACE_TYPE).contains(
                            type)) {
                docView.setViewId(type.getDefaultView());
                docView.addParameter("tabIds", "MAIN_TAB:documents");
            }
        }
        return docView;
    }

    @Override
    public String getUrlFromDocumentView(DocumentView docView) {
        DocumentLocation documentLocation = docView.getDocumentLocation();
        if (documentLocation.getPathRef() != null) {
            DocumentPathCodec pathCodec = new DocumentPathCodec();
            pathCodec.setPrefix(getPrefix());
            return pathCodec.getUrlFromDocumentView(docView);
        } else {
            DocumentIdCodec idCodec = new DocumentIdCodec();
            idCodec.setPrefix(getPrefix());
            docView.setViewId("view_social_document");
            return idCodec.getUrlFromDocumentView(docView);
        }
    }

}
