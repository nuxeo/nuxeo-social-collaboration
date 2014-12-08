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

package org.nuxeo.ecm.social.relationship.service;

import java.util.List;

import org.nuxeo.ecm.social.relationship.RelationshipKind;

/**
 * Service to manage relations between entities.
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.5
 */
public interface RelationshipService {

    /**
     * Gets all existing relationship kinds between an actor and a target.
     */
    List<RelationshipKind> getRelationshipKinds(String actorId, String targetId);

    /**
     * Gets all targets of an actor.
     */
    List<String> getTargets(String actorId);

    /**
     * Gets all targets that match the targetPattern.
     */
    List<String> getTargetsWithFulltext(String actorId, String targetPattern);

    /**
     * Gets all targets that match the targetPattern with the given kind.
     */
    List<String> getTargetsWithFulltext(String actorId, RelationshipKind kind, String targetPattern);

    /**
     * Gets all targets of a specific relation.
     *
     * @param kind if null, it will return all targets {@see #getTargets}, it can be only filled with the group or the
     *            name
     */
    List<String> getTargetsOfKind(String actorId, RelationshipKind kind);

    /**
     * Gets registered (contributed with the extension point) relationship kinds depending of a group
     *
     * @return an UnmodifiableList with all user relationship types
     * @param group can be null, or empty if you want to get all kinds
     */
    List<RelationshipKind> getRegisteredKinds(String group);

    /**
     * Adds a relation between two entities.
     *
     * @return {@code true} if a new relation is created, {@code false} otherwise.
     */
    Boolean addRelation(String actorId, String targetId, RelationshipKind kind);

    /**
     * Removes a relationship composed by parameters
     *
     * @return true if a relation has been deleted, false otherwise
     */
    Boolean removeRelation(String actorId, String targetId, RelationshipKind kind);

}
