/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 * Nuxeo - initial API and implementation
 */

package org.nuxeo.ecm.social.workspace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Class to provide some useful methods
 * 
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.4.1
 */
public class SocialWorkspaceHelper {

    private static final Log log = LogFactory.getLog(SocialWorkspaceHelper.class);

    public static final String ADMINISTRATORS_SUFFIX = "_administrators";

    public static final String MEMBERS_SUFFIX = "_members";

    public static final String ADMINISTRATORS_LABEL_PREFIX = "Administrators of ";

    public static final String MEMBERS_LABEL_PREFIX = "Members of ";

    public static final String COMMUNITY_FACET = "CommunityFacet";

    private SocialWorkspaceHelper() {
        // Helper class
    }

    public static String getCommunityAdministratorsGroupName(DocumentModel doc) {
        return doc.getId() + ADMINISTRATORS_SUFFIX;
    }

    public static String getCommunityMembersGroupName(DocumentModel doc) {
        return doc.getId() + MEMBERS_SUFFIX;
    }

    public static String getCommunityAdministratorsGroupLabel(DocumentModel doc) {
        try {
            return ADMINISTRATORS_LABEL_PREFIX + doc.getTitle();
        } catch (ClientException e) {
            log.warn("Cannot retrieve document title to build administrators group label: "
                    + doc.getId());
            log.debug("Exception occurred:", e);
            return null;
        }
    }

    public static String getCommunityMembersGroupLabel(DocumentModel doc) {
        try {
            return MEMBERS_LABEL_PREFIX + doc.getTitle();
        } catch (ClientException e) {
            log.warn("Cannot retrieve document title to build members group label: "
                    + doc.getId());
            log.debug("Exception occurred:", e);
            return null;
        }
    }

}
