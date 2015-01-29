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

package org.nuxeo.ecm.social.relationship.service;

import static org.nuxeo.ecm.social.relationship.RelationshipConstants.RELATIONSHIP_FIELD_ACTOR;
import static org.nuxeo.ecm.social.relationship.RelationshipConstants.RELATIONSHIP_FIELD_KIND;
import static org.nuxeo.ecm.social.relationship.RelationshipConstants.RELATIONSHIP_FIELD_TARGET;
import static org.nuxeo.ecm.social.relationship.RelationshipConstants.RELATIONSHIP_PROPERTY_KIND;
import static org.nuxeo.ecm.social.relationship.RelationshipConstants.RELATIONSHIP_SCHEMA_NAME;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.nuxeo.ecm.social.relationship.RelationshipKind;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * Default implementation of {@see RelationshipService}.
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.5
 */
public class RelationshipServiceImpl extends DefaultComponent implements RelationshipService {

    public static final String KINDS_EXTENSION_POINT = "relationshipKinds";

    protected static final String RELATIONSHIP_DIRECTORY_NAME = "actorRelationshipDirectory";

    private static final Log log = LogFactory.getLog(RelationshipServiceImpl.class);

    protected RelationshipKindRegistry relationshipKindRegistry;

    @Override
    public void activate(ComponentContext context) throws Exception {
        relationshipKindRegistry = new RelationshipKindRegistry();
    }

    @Override
    public void deactivate(ComponentContext context) throws Exception {
        relationshipKindRegistry = null;
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor)
            throws Exception {
        if (KINDS_EXTENSION_POINT.equals(extensionPoint)) {
            relationshipKindRegistry.addContribution((RelationshipKindDescriptor) contribution);
        }
    }

    @Override
    public List<RelationshipKind> getRelationshipKinds(String actorId, String targetId) {
        Map<String, Serializable> filters = new HashMap<String, Serializable>();
        filters.put(RELATIONSHIP_FIELD_ACTOR, actorId);
        filters.put(RELATIONSHIP_FIELD_TARGET, targetId);

        Set<RelationshipKind> kinds = new HashSet<RelationshipKind>();
        for (DocumentModel relation : queryRelationshipsDirectory(filters, false)) {
            try {
                kinds.add(buildKindFromRelationshipModel(relation));
            } catch (ClientException e) {
                throw new ClientRuntimeException(e);
            }
        }
        kinds = relationshipKindRegistry.filterUnregisteredRelationshipKinds(kinds);
        return new ArrayList<RelationshipKind>(kinds);
    }

    @Override
    public List<String> getTargetsOfKind(String actorId, RelationshipKind kind) {
        Map<String, Serializable> filters = new HashMap<String, Serializable>();
        filters.put(RELATIONSHIP_FIELD_ACTOR, actorId);
        if (!(kind == null || kind.isEmpty())) {
            filters.put(RELATIONSHIP_FIELD_KIND, kind.toString());
        }
        return buildListFromProperty(queryRelationshipsDirectory(filters, false), RELATIONSHIP_SCHEMA_NAME,
                RELATIONSHIP_FIELD_TARGET);
    }

    @Override
    public List<String> getTargets(String actorId) {
        return getTargetsOfKind(actorId, null);
    }

    @Override
    public List<String> getTargetsWithFulltext(String actorId, String targetPattern) {
        return getTargetsWithFulltext(actorId, null, targetPattern);
    }

    @Override
    public List<String> getTargetsWithFulltext(String actorId, RelationshipKind kind, String targetPattern) {
        Map<String, Serializable> filters = new HashMap<String, Serializable>();
        filters.put(RELATIONSHIP_FIELD_ACTOR, actorId);
        if (!(kind == null || kind.isEmpty())) {
            filters.put(RELATIONSHIP_FIELD_KIND, kind.toString());
        }
        if (!StringUtils.isBlank(targetPattern)) {
            filters.put(RELATIONSHIP_FIELD_TARGET, targetPattern);
        }
        return buildListFromProperty(queryRelationshipsDirectory(filters, true), RELATIONSHIP_SCHEMA_NAME,
                RELATIONSHIP_FIELD_TARGET);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RelationshipKind> getRegisteredKinds(String group) {
        Set<RelationshipKind> kinds = relationshipKindRegistry.getRegisteredKinds(group);
        if (kinds == null) {
            return Collections.emptyList();
        }
        return new ArrayList<RelationshipKind>(kinds);
    }

    @Override
    public Boolean addRelation(String actorId, String targetId, RelationshipKind kind) {
        if (kind == null) {
            throw new ClientRuntimeException("Type cannot be null");
        }

        DirectoryService directoryService = Framework.getLocalService(DirectoryService.class);
        Session relationshipsDirectory = null;
        try {
            relationshipsDirectory = directoryService.open(RELATIONSHIP_DIRECTORY_NAME);
            // try to get an existing entry
            Map<String, Serializable> relationship = new HashMap<String, Serializable>();
            relationship.put(RELATIONSHIP_FIELD_ACTOR, actorId);
            relationship.put(RELATIONSHIP_FIELD_TARGET, targetId);
            relationship.put(RELATIONSHIP_FIELD_KIND, kind.toString());

            DocumentModelList relationships = relationshipsDirectory.query(relationship);
            if (relationships.isEmpty()) {
                relationshipsDirectory.createEntry(new HashMap<String, Object>(relationship));
                return true;
            } else {
                return false;
            }
        } catch (ClientException e) {
            throw new ClientRuntimeException("Unable to create a new relation", e);
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
    public Boolean removeRelation(String actorId, String targetId, RelationshipKind kind) {
        DirectoryService directoryService = Framework.getLocalService(DirectoryService.class);
        Session relationshipDirectory = null;
        try {
            relationshipDirectory = directoryService.open(RELATIONSHIP_DIRECTORY_NAME);

            Map<String, Serializable> filter = new HashMap<String, Serializable>();
            filter.put(RELATIONSHIP_FIELD_ACTOR, actorId);
            if (!StringUtils.isBlank(targetId)) {
                filter.put(RELATIONSHIP_FIELD_TARGET, targetId);
            }
            if (!(kind == null || kind.isEmpty())) {
                filter.put(RELATIONSHIP_FIELD_KIND, kind.toString());
            }

            DocumentModelList relations = relationshipDirectory.query(filter, filter.keySet());
            if (relations.isEmpty()) {
                log.warn("Trying to delete a relationship that doesn't exists");
                return false;
            } else {
                for (DocumentModel relation : relations) {
                    relationshipDirectory.deleteEntry(relation.getId());
                }
                return true;
            }
        } catch (ClientException e) {
            throw new ClientRuntimeException("Unable to remove a relationship", e);
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

    protected DocumentModelList queryRelationshipsDirectory(Map<String, Serializable> filter, Boolean withFulltext) {
        DirectoryService directoryService = Framework.getLocalService(DirectoryService.class);
        Session relationshipsDirectory = null;
        try {
            relationshipsDirectory = directoryService.open(RELATIONSHIP_DIRECTORY_NAME);

            Set<String> fulltextFields = new HashSet<String>();
            fulltextFields.add(RELATIONSHIP_FIELD_KIND);
            if (withFulltext) {
                fulltextFields.addAll(filter.keySet());
            }

            return relationshipsDirectory.query(filter, fulltextFields, getRelationshipsOrderBy());
        } catch (ClientException e) {
            throw new ClientRuntimeException("Unable to query through relationships directory", e);
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

    protected static List<String> buildListFromProperty(DocumentModelList docs, String schema, String property) {
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

    protected static RelationshipKind buildKindFromRelationshipModel(DocumentModel relation) throws ClientException {
        return RelationshipKind.fromString((String) relation.getPropertyValue(RELATIONSHIP_PROPERTY_KIND));
    }

    protected static Map<String, String> getRelationshipsOrderBy() {
        Map<String, String> order = new HashMap<String, String>();
        order.put(RELATIONSHIP_FIELD_TARGET, "desc");
        return order;
    }

}
