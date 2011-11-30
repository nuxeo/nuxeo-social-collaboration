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

package org.nuxeo.ecm.social.mini.message;

import java.security.Principal;
import java.util.Date;
import java.util.List;

import org.nuxeo.ecm.social.relationship.RelationshipKind;

/**
 * Service handling mini messages.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public interface MiniMessageService {

    /**
     * Add a new mini message.
     *
     * @return the newly created MiniMessage object.
     */
    MiniMessage addMiniMessage(Principal principal, String message,
            Date publishedDate);

    /**
     * Add a new mini message for the given {@code target}.
     *
     * @return the newly created MiniMessage object.
     */
    MiniMessage addMiniMessage(Principal principal, String message,
            Date publishedDate, String target);

    /**
     * Add a new mini message.
     *
     * @return the newly created MiniMessage object.
     */
    MiniMessage addMiniMessage(Principal principal, String message);

    /**
     * Remove a mini message.
     */
    void removeMiniMessage(MiniMessage miniMessage);

    /**
     * Returns the mini messages for the given {@code actorActivityObject}. The
     * {@code relationshipKind} is used to find people with whom the actor has a
     * relation.
     *
     * @param offset the offset (starting at 0) into the list of mini messages.
     * @param limit the maximum number of mini messages to retrieve, or 0 for
     *            all of them.
     */
    List<MiniMessage> getMiniMessageFor(String actorActivityObject,
            RelationshipKind relationshipKind, long offset, long limit);

    /**
     * Returns the mini messages for the given {@code actorActivityObject} and
     * {@code targetActivityObject}. The {@code relationshipKind} is used to
     * find people with whom the actor has a relation.
     *
     * @param offset the offset (starting at 0) into the list of mini messages.
     * @param limit the maximum number of mini messages to retrieve, or 0 for
     *            all of them.
     */
    List<MiniMessage> getMiniMessageFor(String actorActivityObject,
            RelationshipKind relationshipKind, String targetActivityObject,
            long offset, long limit);

    /**
     * Returns the mini messages from the given {@code actorActivityObject}.
     *
     * @param offset the offset (starting at 0) into the list of mini messages.
     * @param limit the maximum number of mini messages to retrieve, or 0 for
     *            all of them.
     */
    List<MiniMessage> getMiniMessageFrom(String actorActivityObject,
            long offset, long limit);

}
