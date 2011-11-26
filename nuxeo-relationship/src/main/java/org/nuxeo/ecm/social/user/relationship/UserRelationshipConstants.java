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
