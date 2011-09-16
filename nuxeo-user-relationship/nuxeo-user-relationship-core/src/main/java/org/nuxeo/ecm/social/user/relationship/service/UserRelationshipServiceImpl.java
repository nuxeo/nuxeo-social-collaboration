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

import static org.nuxeo.ecm.social.user.relationship.UserRelationshipConstants.KIND_PROPERTY_GROUP;
import static org.nuxeo.ecm.social.user.relationship.UserRelationshipConstants.KIND_PROPERTY_NAME;
import static org.nuxeo.ecm.social.user.relationship.UserRelationshipConstants.RELATIONSHIP_FIELD_ACTOR;
import static org.nuxeo.ecm.social.user.relationship.UserRelationshipConstants.RELATIONSHIP_FIELD_KIND;
import static org.nuxeo.ecm.social.user.relationship.UserRelationshipConstants.RELATIONSHIP_FIELD_TARGET;
import static org.nuxeo.ecm.social.user.relationship.UserRelationshipConstants.RELATIONSHIP_PROPERTY_KIND;
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
import org.nuxeo.ecm.social.user.relationship.RelationshipKind;
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

    public static final String KINDS_EXTENSION_POINT = "types";

    protected static final String RELATIONSHIP_DIRECTORY_NAME = "actorRelationshipDirectory";

    protected static final String KIND_DIRECTORY_NAME = "actorRelationshipKindDirectory";

    private static final Log log = LogFactory.getLog(UserRelationshipServiceImpl.class);

    protected DirectoryService directoryService;

    protected UserManager userManager;

    protected HashMap<String, List<RelationshipKind>> registeredKinds;

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {
        if (KINDS_EXTENSION_POINT.equals(extensionPoint)) {
            addRelationshipKind((UserRelationshipKindDescriptor) contribution);
        }
    }

    protected void addRelationshipKind(UserRelationshipKindDescriptor type) {
        Session typeDirectory = null;
        try {
            typeDirectory = getDirectoryService().open(KIND_DIRECTORY_NAME);

            // Add a new relationship kind if not exists
            if (null == typeDirectory.getEntry(type.getName())) {
                typeDirectory.createEntry(type.getMap());
                typeDirectory.commit();
            }
        } catch (ClientException e) {
            throw new ClientRuntimeException(
                    "Unable to create a new relationship kind", e);
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
    public List<RelationshipKind> getRelationshipKinds(String actorId,
            String targetId) {
        Map<String, Serializable> filters = new HashMap<String, Serializable>();
        filters.put(RELATIONSHIP_FIELD_ACTOR, actorId);
        filters.put(RELATIONSHIP_FIELD_TARGET, targetId);

        Set<RelationshipKind> kinds = new HashSet<RelationshipKind>();
        for (DocumentModel relation : queryRelationshipsDirectory(filters,
                false)) {
            try {
                kinds.add(buildKindFromRelationshipModel(relation));
            } catch (ClientException e) {
                throw new ClientRuntimeException(e);
            }
        }
        return new ArrayList<RelationshipKind>(kinds);
    }

    @Override
    public List<String> getTargetsOfKind(String actorId, RelationshipKind kind) {
        Map<String, Serializable> filters = new HashMap<String, Serializable>();
        filters.put(RELATIONSHIP_FIELD_ACTOR, actorId);
        if (!(kind == null || kind.isEmpty())) {
            filters.put(RELATIONSHIP_FIELD_KIND, kind.toString());
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
    public List<String> getTargetsWithFulltext(String actorId,
            String targetPattern) {
        return getTargetsWithFulltext(actorId, null, targetPattern);
    }

    @Override
    public List<String> getTargetsWithFulltext(String actorId,
            RelationshipKind kind, String targetPattern) {
        Map<String, Serializable> filters = new HashMap<String, Serializable>();
        filters.put(RELATIONSHIP_FIELD_ACTOR, actorId);
        if (!(kind == null || kind.isEmpty())) {
            filters.put(RELATIONSHIP_FIELD_KIND, kind.toString());
        }
        if (!StringUtils.isBlank(targetPattern)) {
            filters.put(RELATIONSHIP_FIELD_TARGET, targetPattern);
        }
        return buildListFromProperty(
                queryRelationshipsDirectory(filters, true),
                RELATIONSHIP_SCHEMA_NAME, RELATIONSHIP_FIELD_TARGET);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RelationshipKind> getRegisteredKinds(String group) {
        if (registeredKinds == null) {
            registeredKinds = new HashMap<String, List<RelationshipKind>>();
            for (DocumentModel type : getAllTypesFromDirectory()) {
                try {
                    String kindGroup = (String) type.getPropertyValue(KIND_PROPERTY_GROUP);
                    String kindName = (String) type.getPropertyValue(KIND_PROPERTY_NAME);
                    RelationshipKind kind = RelationshipKind.newInstance(
                            kindGroup, kindName);

                    if (!registeredKinds.containsKey(kindGroup)) {
                        registeredKinds.put(kindGroup,
                                new ArrayList<RelationshipKind>());
                    }
                    registeredKinds.get(kindGroup).add(kind);
                } catch (ClientException e) {
                    throw new ClientRuntimeException(e);
                }
            }
        }

        List<RelationshipKind> allKinds = new ArrayList<RelationshipKind>();
        if (StringUtils.isEmpty(group)) {
            // Fill returned list with all registered RelationKind
            for (List<RelationshipKind> kinds : registeredKinds.values()) {
                allKinds.addAll(kinds);
            }
        } else {
            allKinds = registeredKinds.get(group);
        }

        return allKinds != null ? UnmodifiableList.decorate(allKinds)
                : Collections.<RelationshipKind> emptyList();
    }

    private DocumentModelList getAllTypesFromDirectory() {
        Session typesDirectory = null;
        try {
            typesDirectory = getDirectoryService().open(KIND_DIRECTORY_NAME);
            return typesDirectory.getEntries();
        } catch (ClientException e) {
            throw new ClientRuntimeException(
                    "Unable to fetch all relationsip kinds", e);
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
    public Boolean addRelation(String actorId, String targetId,
            RelationshipKind kind) {
        if (kind == null) {
            throw new ClientRuntimeException("Type cannot be null");
        }

        Session relationshipsDirectory = null;
        try {
            relationshipsDirectory = getDirectoryService().open(
                    RELATIONSHIP_DIRECTORY_NAME);
            // try to get an existing entry
            Map<String, Serializable> relationship = new HashMap<String, Serializable>();
            relationship.put(RELATIONSHIP_FIELD_ACTOR, actorId);
            relationship.put(RELATIONSHIP_FIELD_TARGET, targetId);
            relationship.put(RELATIONSHIP_FIELD_KIND, kind.toString());

            DocumentModelList relationships = relationshipsDirectory.query(relationship);
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
    public Boolean removeRelation(String actorId, String targetId,
            RelationshipKind kind) {
        Session relationshipDirectory = null;
        try {
            relationshipDirectory = getDirectoryService().open(
                    RELATIONSHIP_DIRECTORY_NAME);

            Map<String, Serializable> filter = new HashMap<String, Serializable>();
            filter.put(RELATIONSHIP_FIELD_ACTOR, actorId);
            if (!StringUtils.isBlank(targetId)) {
                filter.put(RELATIONSHIP_FIELD_TARGET, targetId);
            }
            if (!(kind == null || kind.isEmpty())) {
                filter.put(RELATIONSHIP_FIELD_KIND, kind.toString());
            }

            DocumentModelList relations = relationshipDirectory.query(filter,
                    filter.keySet());
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
                    log.debug("Exception occurred", e);
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

            Set<String> fulltextFields = new HashSet<String>();
            fulltextFields.add(RELATIONSHIP_FIELD_KIND);
            if (withFulltext) {
                fulltextFields.addAll(filter.keySet());
            }

            return relationshipsDirectory.query(filter, fulltextFields,
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
                    log.debug("Exception occurred", e);
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

    protected static RelationshipKind buildKindFromRelationshipModel(
            DocumentModel relation) throws ClientException {
        return RelationshipKind.fromString((String) relation.getPropertyValue(RELATIONSHIP_PROPERTY_KIND));
    }

    protected static Map<String, String> getRelationshipsOrderBy() {
        Map<String, String> order = new HashMap<String, String>();
        order.put(RELATIONSHIP_FIELD_TARGET, "desc");
        return order;
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
