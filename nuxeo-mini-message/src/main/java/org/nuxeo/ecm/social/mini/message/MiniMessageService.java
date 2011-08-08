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

import java.util.Date;
import java.util.List;

import org.nuxeo.ecm.social.user.relationship.RelationshipKind;

/**
 * Service handling mini messages.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public interface MiniMessageService {

    /**
     * Add a new mini message.
     *
     * @return the newly created MiniMessage object.
     */
    MiniMessage addMiniMessage(String actor, String message, Date publishedDate);

    /**
     * Add a new mini message.
     *
     * @return the newly created MiniMessage object.
     */
    MiniMessage addMiniMessage(String actor, String message);

    /**
     * Returns the mini messages for the given {@code actor}. The
     * {@code relationshipKind} is used to find people with whom the actor has a
     * relation.
     *
     * @param pageSize the wanted page size.
     * @param currentPage the current page index.
     */
    List<MiniMessage> getMiniMessageFor(String actor,
            RelationshipKind relationshipKind, int pageSize, int currentPage);

    /**
     * Returns the mini messages from the given {@code actor}.
     *
     * @param pageSize the wanted page size.
     * @param currentPage the current page index.
     */
    List<MiniMessage> getMiniMessageFrom(String actor, int pageSize,
            int currentPage);

}
