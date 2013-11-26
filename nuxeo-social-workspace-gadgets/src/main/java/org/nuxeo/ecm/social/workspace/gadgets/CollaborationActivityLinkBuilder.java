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

package org.nuxeo.ecm.social.workspace.gadgets;

import org.nuxeo.ecm.activity.DefaultActivityLinkBuilder;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.platform.ui.web.rest.api.URLPolicyService;
import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.platform.web.common.vh.VirtualHostHelper;
import org.nuxeo.runtime.api.Framework;

/**
 * Activity link builder for Collaboration tab, use the {@code collaboration}
 * URL pattern to generate documents URL.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public class CollaborationActivityLinkBuilder extends
        DefaultActivityLinkBuilder {

    @Override
    public String getDocumentURL(String repositoryName, String documentId) {
        DocumentLocation docLoc = new DocumentLocationImpl(repositoryName,
                new IdRef(documentId));
        DocumentView docView = new DocumentViewImpl(docLoc,
                "view_social_document");
        URLPolicyService urlPolicyService = Framework.getLocalService(URLPolicyService.class);
        return VirtualHostHelper.getContextPathProperty()
                + "/"
                + urlPolicyService.getUrlFromDocumentView("collaboration",
                        docView, null);
    }
}
