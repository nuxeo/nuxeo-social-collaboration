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

package org.nuxeo.ecm.activity;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.platform.ui.web.rest.api.URLPolicyService;
import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.platform.web.common.vh.VirtualHostHelper;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class ActivityHelper {

    public static final String SEPARATOR = ":";

    public static final String USER_PREFIX = "user" + SEPARATOR;

    public static final String DOC_PREFIX = "doc" + SEPARATOR;

    private ActivityHelper() {
        // helper class
    }

    public static boolean isUser(String activityObject) {
        return activityObject != null && activityObject.startsWith(USER_PREFIX);
    }

    public static boolean isDocument(String activityObject) {
        return activityObject != null && activityObject.startsWith(DOC_PREFIX);
    }

    public static String getUsername(String activityObject) {
        if (!isUser(activityObject)) {
            throw new IllegalArgumentException(activityObject
                    + " is not a user activity object");
        }
        return activityObject.replaceAll(USER_PREFIX, "");
    }

    public static List<String> getUsernames(List<String> activityObjects) {
        List<String> usernames = new ArrayList<String>();
        for (String activityObject : activityObjects) {
            usernames.add(getUsername(activityObject));
        }
        return usernames;
    }

    public static String getDocumentId(String activityObject) {
        if (isDocument(activityObject)) {
            String[] v = activityObject.split(":");
            return v[2];
        }
        return "";
    }

    public static String getRepositoryName(String activityObject) {
        if (isDocument(activityObject)) {
            String[] v = activityObject.split(":");
            return v[1];
        }
        return "";
    }

    public static String createDocumentActivityObject(DocumentModel doc) {
        return createDocumentActivityObject(doc.getRepositoryName(),
                doc.getId());
    }

    public static String createDocumentActivityObject(String repositoryName,
            String docId) {
        return DOC_PREFIX + repositoryName + SEPARATOR + docId;
    }

    public static String createUserActivityObject(Principal principal) {
        return createUserActivityObject(principal.getName());
    }

    public static String createUserActivityObject(String username) {
        return USER_PREFIX + username;
    }

    public static String generateDisplayName(Principal principal) {
        if (principal instanceof NuxeoPrincipal) {
            NuxeoPrincipal nuxeoPrincipal = (NuxeoPrincipal) principal;
            if (!StringUtils.isBlank(nuxeoPrincipal.getFirstName())
                    || !StringUtils.isBlank(nuxeoPrincipal.getLastName())) {
                return nuxeoPrincipal.getFirstName() + " "
                        + nuxeoPrincipal.getLastName();
            }
        }
        return principal.getName();
    }

    public static String getDocumentTitle(DocumentModel doc) {
        try {
            return doc.getTitle();
        } catch (ClientException e) {
            return doc.getId();
        }
    }

    public static String getDocumentURL(String repositoryName, String documentId) {
        DocumentLocation docLoc = new DocumentLocationImpl(repositoryName,
                new IdRef(documentId));
        DocumentView docView = new DocumentViewImpl(docLoc, "view_documents");
        return VirtualHostHelper.getContextPathProperty()
                + "/"
                + getURLPolicyService().getUrlFromDocumentView("id", docView,
                        null);
    }

    public static String getUserProfileURL(String username) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("username", username);
        DocumentView docView = new DocumentViewImpl(null, null, params);
        return VirtualHostHelper.getContextPathProperty()
                + "/"
                + getURLPolicyService().getUrlFromDocumentView("user", docView,
                        null);
    }

    public static String getDocumentLink(String documentActivityObject,
            String displayValue) {
        documentActivityObject = StringEscapeUtils.escapeHtml(documentActivityObject);
        displayValue = StringEscapeUtils.escapeHtml(displayValue);
        String link = "<a href=\"%s\" target=\"_top\">%s</a>";
        return String.format(
                link,
                getDocumentURL(
                        ActivityHelper.getRepositoryName(documentActivityObject),
                        ActivityHelper.getDocumentId(documentActivityObject)),
                displayValue);
    }

    public static String getUserProfileLink(String userActivityObject,
            String displayValue) {
        userActivityObject = StringEscapeUtils.escapeHtml(userActivityObject);
        displayValue = StringEscapeUtils.escapeHtml(displayValue);
        String link = "<a href=\"%s\" target=\"_top\" title=\"%s\">%s</a>";
        String username = ActivityHelper.getUsername(userActivityObject);
        return String.format(link, getUserProfileURL(username), username,
                displayValue);
    }

    private static URLPolicyService getURLPolicyService() {
        URLPolicyService urlPolicyService;
        try {
            urlPolicyService = Framework.getService(URLPolicyService.class);
        } catch (Exception e) {
            throw new ClientRuntimeException(e);
        }

        if (urlPolicyService == null) {
            throw new ClientRuntimeException(
                    "URLPolicyService service is not registered.");
        }
        return urlPolicyService;
    }

}
