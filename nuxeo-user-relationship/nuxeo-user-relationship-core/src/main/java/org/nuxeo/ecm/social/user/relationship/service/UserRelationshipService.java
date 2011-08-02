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

import java.util.List;

/**
 * Service to manage relations between entities.
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.4.3
 */
public interface UserRelationshipService {

    /**
     * Gets all existing type's relationship between an actor and a target.
     */
    List<String> getRelationshipKinds(String actorId, String targetId);

    /**
     * Gets all targets of an actor.
     */
    List<String> getTargets(String actorId);

    /**
     * Gets all targets that match the targetPrefix.
     */
    List<String> getTargetsWithPrefix(String actorId, String targetPrefix);

    /**
     * Gets all targets of a specific relation.
     *
     * @param kind if null, it will return all targets {@see #getTargets}
     */
    List<String> getTargetsOfKind(String actorId, String kind);

    /**
     * Gets all declared user relationship types.
     *
     * @return an UnmodifiableList with all user relationship types
     */
    List<String> getKinds();

    /**
     * Adds a relation between two entities. If the relation already exists,
     * false will be returned.
     *
     * @return true if a new relation is create, false otherwise
     */
    Boolean addRelation(String actorId, String targetId, String kind);

    /**
     * Removes a relationship composed by parameters.
     *
     * @return true if a relation has been deleted, false otherwise
     */
    Boolean removeRelation(String actorId, String targetId, String kind);

}
