/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Contributors:
 *      Nuxeo
 */

package org.nuxeo.ecm.social.user.relationship.provider;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.platform.query.api.AbstractPageProvider;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.user.relationship.service.UserRelationshipService;
import org.nuxeo.runtime.api.Framework;

/**
 * Page provider listing user's relationships
 * <p>
 * This page provider requires two parameters: the first one to be filled with
 * the username of which user you want the relations, and the second one to be
 * filled with a search string.
 * <p>
 *
 * @since 5.4.3
 */
public class UserRelationshipPageProvider extends
        AbstractPageProvider<DocumentModel> implements
        PageProvider<DocumentModel> {

    private static final Log log = LogFactory.getLog(UserRelationshipPageProvider.class);

    private static final long serialVersionUID = 1L;

    protected List<String> relationships;

    protected List<DocumentModel> relationshipPage;

    protected UserRelationshipService service;

    protected UserManager userManager;

    @Override
    public List<DocumentModel> getCurrentPage() {
        if (relationships == null) {
            fillRelationshipsForCurrentUser();
        }
        if (!hasError()) {
            relationshipPage = new DocumentModelListImpl();
            resultsCount = relationships.size();
            // post-filter the results "by hand" to handle pagination
            long pageSize = getMinMaxPageSize();
            if (pageSize == 0) {
                for (String username : relationships) {
                    addUsernameToRelationshipPage(username);
                }
            } else {
                // handle offset
                if (offset <= resultsCount) {
                    for (int i = Long.valueOf(offset).intValue(); i < resultsCount
                            && i < offset + pageSize; i++) {
                        addUsernameToRelationshipPage(relationships.get(i));
                    }
                }
            }
        }
        return relationshipPage;
    }

    protected void addUsernameToRelationshipPage(String username) {
        try {
            relationshipPage.add(getUserManager().getUserModel(username));
        } catch (ClientException e) {
            log.warn("Cannot get user model for:" + username, e);
        }
    }

    protected void fillRelationshipsForCurrentUser() {
        String searchString = getSearchString();
        relationships = new ArrayList<String>();

        if (StringUtils.isBlank(searchString) || "*".equals(searchString)) {
            relationships.addAll(getUserRelationshipService().getTargets(
                    getCurrentUser()));
        } else {
            relationships.addAll(getUserRelationshipService().getTargetsWithFulltext(
                    getCurrentUser(), searchString.trim()));
        }
    }

    protected UserRelationshipService getUserRelationshipService() {
        if (service == null) {
            try {
                service = Framework.getService(UserRelationshipService.class);
            } catch (Exception e) {
                log.warn("Failed to get UserRelationshipService", e);
            }
        }
        return service;
    }

    protected UserManager getUserManager() {
        if (userManager == null) {
            try {
                userManager = Framework.getService(UserManager.class);
            } catch (Exception e) {
                log.warn("Failed to get UserManager", e);
            }
        }
        return userManager;
    }

    protected String getCurrentUser() {
        Object[] params = getParameters();
        if (params.length >= 1) {
            return (String)params[0];
        }
        return null;
    }

    protected String getSearchString() {
        Object[] params = getParameters();
        if (params.length >= 2) {
            return (String)params[1];
        }
        return null;
    }

    @Override
    public void refresh() {
        super.refresh();
        relationships = null;
    }
}
