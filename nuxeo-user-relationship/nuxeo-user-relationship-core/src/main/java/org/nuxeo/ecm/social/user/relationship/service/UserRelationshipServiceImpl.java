/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Nuxeo
 */

package org.nuxeo.ecm.social.user.relationship.service;

import static org.nuxeo.ecm.social.user.relationship.UserRelationshipConstants.RELATIONSHIP_FIELD_ACTOR;
import static org.nuxeo.ecm.social.user.relationship.UserRelationshipConstants.RELATIONSHIP_FIELD_TARGET;
import static org.nuxeo.ecm.social.user.relationship.UserRelationshipConstants.RELATIONSHIP_FIELD_TYPE;
import static org.nuxeo.ecm.social.user.relationship.UserRelationshipConstants.RELATIONSHIP_SCHEMA_NAME;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.list.UnmodifiableList;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * Default implementation of {@see UserRelationshipService}.
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.4.3
 */
public class UserRelationshipServiceImpl extends DefaultComponent implements
        UserRelationshipService {

    public static final String TYPES_EXTENSION_POINT = "types";

    protected static final String RELATIONSHIP_DIRECTORY_NAME = "actorRelationshipDirectory";

    protected static final String KIND_DIRECTORY_NAME = "actorRelationshipKindDirectory";

    private static final Log log = LogFactory.getLog(UserRelationshipServiceImpl.class);

    protected DirectoryService directoryService;

    protected UserManager userManager;

    protected Map<String, DocumentModel> registeredTypes;

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {
        if (TYPES_EXTENSION_POINT.equals(extensionPoint)) {
            addFriendshipType((UserRelationshipKindDescriptor) contribution);
        }
    }

    protected void addFriendshipType(UserRelationshipKindDescriptor type) {
        Session typeDirectory = null;
        try {
            typeDirectory = getDirectoryService().open(KIND_DIRECTORY_NAME);

            // Add a new friendships type if not exists
            if (null == typeDirectory.getEntry(type.getName())) {
                typeDirectory.createEntry(type.getMap());
                typeDirectory.commit();
            }
        } catch (ClientException e) {
            throw new ClientRuntimeException(
                    "Unable to create a new friendship type", e);
        } finally {
            if (typeDirectory != null) {
                try {
                    typeDirectory.close();
                } catch (DirectoryException e) {
                    log.error("Error while trying to close type directory");
                    log.debug("Exception occurred", e);
                }
            }
        }
    }

    @Override
    public List<String> getRelationshipKinds(String actorId, String targetId) {
        Map<String, Serializable> filters = new HashMap<String, Serializable>();
        filters.put(RELATIONSHIP_FIELD_ACTOR, actorId);
        filters.put(RELATIONSHIP_FIELD_TARGET, targetId);

        return buildListFromProperty(
                queryRelationshipsDirectory(filters, false),
                RELATIONSHIP_SCHEMA_NAME, RELATIONSHIP_FIELD_TYPE);
    }

    @Override
    public List<String> getTargetsOfKind(String actorId, String kind) {
        Map<String, Serializable> filters = new HashMap<String, Serializable>();
        filters.put(RELATIONSHIP_FIELD_ACTOR, actorId);
        if (!StringUtils.isBlank(kind)) {
            filters.put(RELATIONSHIP_FIELD_TYPE, kind);
        }
        return buildListFromProperty(
                queryRelationshipsDirectory(filters, false),
                RELATIONSHIP_SCHEMA_NAME, RELATIONSHIP_FIELD_TARGET);
    }

    @Override
    public List<String> getTargets(String actorId) {
        return getTargetsOfKind(actorId, null);
    }

    @Override
    public List<String> getTargetsWithPrefix(String actorId, String targetPrefix) {
        Map<String, Serializable> filters = new HashMap<String, Serializable>();
        filters.put(RELATIONSHIP_FIELD_ACTOR, actorId);
        filters.put(RELATIONSHIP_FIELD_TARGET, targetPrefix);
        return buildListFromProperty(
                queryRelationshipsDirectory(filters, true),
                RELATIONSHIP_SCHEMA_NAME, RELATIONSHIP_FIELD_TARGET);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getKinds() {
        if (registeredTypes == null) {
            registeredTypes = new HashMap<String, DocumentModel>();
            for (DocumentModel type : getAllTypesFromDirectory()) {
                registeredTypes.put(type.getId(), type);
            }
        }
        return UnmodifiableList.decorate(new ArrayList(registeredTypes.keySet()));
    }

    private DocumentModelList getAllTypesFromDirectory() {
        Session typesDirectory = null;
        try {
            typesDirectory = getDirectoryService().open(KIND_DIRECTORY_NAME);
            return typesDirectory.getEntries();
        } catch (ClientException e) {
            throw new ClientRuntimeException(
                    "Unable to fetch all friendship types", e);
        } finally {
            if (typesDirectory != null) {
                try {
                    typesDirectory.close();
                } catch (DirectoryException e) {
                    log.error("Error while trying to close types directory");
                    log.debug("Exception occurred", e);
                }
            }
        }
    }

    @Override
    public Boolean addRelation(String actorId, String targetId, String kind) {
        if (kind == null) {
            throw new ClientRuntimeException("Type cannot be null");
        }

        Session relationshipsDirectory = null;
        try {
            relationshipsDirectory = getDirectoryService().open(
                    RELATIONSHIP_DIRECTORY_NAME);
            // try to get an existing entry
            Map<String, String> relationship = new HashMap<String, String>();
            relationship.put(RELATIONSHIP_FIELD_ACTOR, actorId);
            relationship.put(RELATIONSHIP_FIELD_TARGET, targetId);
            relationship.put(RELATIONSHIP_FIELD_TYPE, kind);

            DocumentModelList relationships = relationshipsDirectory.query(new HashMap<String, Serializable>(
                    relationship));
            if (relationships.isEmpty()) {
                relationshipsDirectory.createEntry(new HashMap<String, Object>(
                        relationship));
                relationshipsDirectory.commit();

                return true;
            } else {
                return false;
            }
        } catch (ClientException e) {
            throw new ClientRuntimeException("Unable to create a new relation",
                    e);
        } finally {
            if (relationshipsDirectory != null) {
                try {
                    relationshipsDirectory.close();
                } catch (DirectoryException e) {
                    log.error("Error while trying to close relationships directory");
                    log.debug("Exception occurred", e);
                }
            }
        }
    }

    @Override
    public Boolean removeRelation(String actorId, String targetId, String kind) {
        Session relationshipDirectory = null;
        try {
            relationshipDirectory = getDirectoryService().open(
                    RELATIONSHIP_DIRECTORY_NAME);

            Map<String, Serializable> filter = new HashMap<String, Serializable>();
            filter.put(RELATIONSHIP_FIELD_ACTOR, actorId);
            filter.put(RELATIONSHIP_FIELD_TARGET, targetId);
            filter.put(RELATIONSHIP_FIELD_TYPE, kind);

            DocumentModelList relations = relationshipDirectory.query(filter);
            if (relations.isEmpty()) {
                log.warn("Trying to delete a relationship that doesn't exists");
                return false;
            } else {
                for (DocumentModel relation : relations) {
                    relationshipDirectory.deleteEntry(relation.getId());
                }
                relationshipDirectory.commit();
                return true;
            }
        } catch (ClientException e) {
            throw new ClientRuntimeException("Unable to remove a relationship",
                    e);
        } finally {
            if (relationshipDirectory != null) {
                try {
                    relationshipDirectory.close();
                } catch (DirectoryException e) {
                    log.error("Error while trying to close relationships directory");
                    log.debug("Exception occured", e);
                }
            }
        }
    }

    protected DocumentModelList queryRelationshipsDirectory(
            Map<String, Serializable> filter, Boolean withFulltext) {
        Session relationshipsDirectory = null;
        try {
            relationshipsDirectory = getDirectoryService().open(
                    RELATIONSHIP_DIRECTORY_NAME);
            Set<String> fulltext = withFulltext ? filter.keySet()
                    : Collections.<String> emptySet();
            return relationshipsDirectory.query(filter, fulltext,
                    getRelationshipsOrderBy());
        } catch (ClientException e) {
            throw new ClientRuntimeException(
                    "Unable to query through relationships directory", e);
        } finally {
            if (relationshipsDirectory != null) {
                try {
                    relationshipsDirectory.close();
                } catch (DirectoryException e) {
                    log.error("Error while trying to close relationships directory");
                    log.debug("Exception occured", e);
                }
            }
        }
    }

    protected static List<String> buildListFromProperty(DocumentModelList docs,
            String schema, String property) {
        Set<String> values = new HashSet<String>();
        for (DocumentModel doc : docs) {
            try {
                values.add(doc.getProperty(schema, property).toString());
            } catch (ClientException e) {
                log.debug("Property " + property + " is not accessible");
            }
        }
        return new ArrayList<String>(values);
    }

    protected static Map<String, String> getRelationshipsOrderBy() {
        Map<String, String> order = new HashMap<String, String>();
        order.put(RELATIONSHIP_FIELD_TARGET, "desc");
        return order;
    }

    protected DocumentModel getUserModelFromFriendshipModel(
            DocumentModel friendship, String field) throws ClientException {
        return getUserManager().getUserModel(
                friendship.getProperty(RELATIONSHIP_SCHEMA_NAME, field).toString());
    }

    protected UserManager getUserManager() {
        if (userManager == null) {
            try {
                userManager = Framework.getService(UserManager.class);
            } catch (Exception e) {
                log.warn("Unable to get UserManager", e);
            }
        }
        return userManager;
    }

    protected DirectoryService getDirectoryService() {
        if (directoryService == null) {
            try {
                directoryService = Framework.getService(DirectoryService.class);
            } catch (Exception e) {
                log.warn("Unable to get DirectoryService", e);
            }
        }
        return directoryService;
    }
}
