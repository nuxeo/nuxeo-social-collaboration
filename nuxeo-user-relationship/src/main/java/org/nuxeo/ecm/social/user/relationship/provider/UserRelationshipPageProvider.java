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

import static org.nuxeo.ecm.social.user.relationship.UserRelationshipConstants.CIRCLE_RELATIONSHIP_KIND_GROUP;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.platform.query.api.AbstractPageProvider;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.relationship.RelationshipKind;
import org.nuxeo.ecm.social.relationship.service.RelationshipService;
import org.nuxeo.ecm.user.center.profile.UserProfileService;
import org.nuxeo.runtime.api.Framework;

/**
 * Page provider listing user's relationships
 * <p>
 * This page provider requires two parameters: the first one to be filled with
 * the username of which user you want the relations, and the second one to be
 * filled with a search string.
 * <p>
 *
 * @since 5.5
 */
public class UserRelationshipPageProvider extends
        AbstractPageProvider<DocumentModel> {

    private static final Log log = LogFactory.getLog(UserRelationshipPageProvider.class);

    private static final long serialVersionUID = 1L;

    protected List<String> relationships;

    protected List<DocumentModel> relationshipPage;

    protected RelationshipService relationshipService;

    protected UserManager userManager;

    protected UserProfileService userProfileService;

    public static final String CORE_SESSION_PROPERTY = "coreSession";

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
                for (String relationship : relationships) {
                    addUsernameToRelationshipPage(ActivityHelper.getUsername(relationship));
                }
            } else {
                // handle offset
                if (offset <= resultsCount) {
                    for (int i = Long.valueOf(offset).intValue(); i < resultsCount
                            && i < offset + pageSize; i++) {
                        addUsernameToRelationshipPage(ActivityHelper.getUsername(relationships.get(i)));
                    }
                }
            }
        }
        return relationshipPage;
    }

    protected void addUsernameToRelationshipPage(String username) {
        try {
            DocumentModel userProfile = getUserProfileService().getUserProfile(
                    getUserManager().getUserModel(username), getCoreSession());
            relationshipPage.add(userProfile);
        } catch (ClientException e) {
            log.warn("Cannot get user model for:" + username, e);
        }
    }

    protected void fillRelationshipsForCurrentUser() {
        relationships = new ArrayList<String>();
        relationships.addAll(getRelationshipService().getTargetsOfKind(
                getCurrentUser(),
                RelationshipKind.fromGroup(CIRCLE_RELATIONSHIP_KIND_GROUP)));
    }

    protected RelationshipService getRelationshipService() {
        if (relationshipService == null) {
            try {
                relationshipService = Framework.getService(RelationshipService.class);
            } catch (Exception e) {
                log.warn("Failed to get UserRelationshipService", e);
            }
        }
        return relationshipService;
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

    protected UserProfileService getUserProfileService() {
        if (userProfileService == null) {
            try {
                userProfileService = Framework.getService(UserProfileService.class);
            } catch (Exception e) {
                log.warn("Failed to get UserProfileService", e);
            }
        }
        return userProfileService;
    }

    protected String getCurrentUser() {
        Object[] params = getParameters();
        if (params.length >= 1) {
            return ActivityHelper.createUserActivityObject((String) params[0]);
        }
        return null;
    }

    @Override
    public void refresh() {
        super.refresh();
        relationships = null;
    }

    protected CoreSession getCoreSession() {
        Map<String, Serializable> props = getProperties();
        CoreSession coreSession = (CoreSession) props.get(CORE_SESSION_PROPERTY);
        if (coreSession == null) {
            throw new ClientRuntimeException("cannot find core session");
        }
        return coreSession;
    }
}
