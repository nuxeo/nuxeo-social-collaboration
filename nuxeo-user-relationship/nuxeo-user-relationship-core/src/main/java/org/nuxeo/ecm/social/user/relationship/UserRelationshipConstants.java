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

package org.nuxeo.ecm.social.user.relationship;

/**
 * User relationship constants class
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.5
 */
public class UserRelationshipConstants {

    private UserRelationshipConstants() {
        // Constants class
    }

    public static final String RELATIONSHIP_SCHEMA_NAME = "actor_relationship";

    public static final String RELATIONSHIP_FIELD_ACTOR = "actor";

    public static final String RELATIONSHIP_FIELD_TARGET = "target";

    public static final String RELATIONSHIP_FIELD_KIND = "kind";

    public static final String KIND_SCHEMA_NAME = "actor_relationship_kind";

    public static final String KIND_FIELD_GROUP = "group";

    public static final String KIND_FIELD_NAME = "name";

    public static final String KIND_FIELD_LABEL = "label";

    public static final String RELATIONSHIP_PROPERTY_KIND = RELATIONSHIP_SCHEMA_NAME
            + ":" + RELATIONSHIP_FIELD_KIND;

    public static final String KIND_PROPERTY_GROUP = KIND_SCHEMA_NAME + ":"
            + KIND_FIELD_GROUP;

    public static final String KIND_PROPERTY_NAME = KIND_SCHEMA_NAME + ":"
            + KIND_FIELD_NAME;

    // Default group for circle relations
    public static final String CIRCLE_RELATIONSHIP_KIND_GROUP = "circle";
}
