/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Benjamin JALON <bjalon@nuxeo.com>
 */
package org.nuxeo.ecm.social.workspace.adapters;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * This class gives method to make the document public or private into the
 * constrains of social workspace. The document must be part of a social
 * workspace and be a social document.
 *
 * A document is public if it's published into the public section. Except for an
 * article render a social document visible only for the community is to publish
 * the document into the private section.
 *
 * For an article this is just to have no proxy into the public section. The
 * source document himself will be the document exposed.
 *
 * In the documentation about method of this class, when we reference to a
 * social document, we implicitly talking about the social document handle by
 * the instance of the class set into the constructor.
 *
 * @author Benjamin JALON <bjalon@nuxeo.com>
 *
 */
public interface SocialDocument {

    /**
     * Returns {@code true} if this document is inside a {@code SocialWorkspace}
     * , {@code false} otherwise.
     */
    boolean isDocumentInSocialWorkspace();

    /**
     * Returns {@code true} if this document is public, {@code false} otherwise.
     */
    boolean isPublic() throws ClientException;

    /**
     * Returns {@code true} if this document is restricted to a
     * {@code SocialWorkspace} members, {@code false} otherwise.
     */
    boolean isRestrictedToMembers() throws ClientException;

    /**
     * Make this document public. It will be visible from non-members of the
     * {@code SocialWorkspace}.
     */
    DocumentModel makePublic() throws ClientException;

    /**
     * Make this document restricted to a {@code SocialWorkspace} members.
     */
    DocumentModel restrictToMembers() throws ClientException;

    /**
     * Return the public proxy of the source document if the source document is
     * public else return null.
     *
     */
    DocumentModel getPublicDocument() throws ClientException;

    /**
     * If source document is not an Article return the private proxy of the
     * source document if the source document is private else return null
     *
     * If is the source document is an Article return the source document if
     * private else return null.
     *
     */
    DocumentModel getRestrictedDocument() throws ClientException;

    /**
     * @return core type name of source document.
     */
    String getType();

}
