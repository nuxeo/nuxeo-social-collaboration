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
 * @author Benjamin JALON <bjalon@nuxeo.com>
 *
 */
public interface SocialDocumentAdapter {

    public abstract DocumentModel restrictToSocialWorkspaceMembers()
            throws ClientException;

    public abstract DocumentModel makePublic() throws ClientException;

    /**
     * Return the public proxy of the source document if the source document is
     * public else return null.
     * 
     */
    public abstract DocumentModel getDocumentPublic() throws ClientException;

    public abstract boolean isPublic() throws ClientException;

    /**
     * If source document is not an Article return the private proxy of the
     * source document if the source document is private else return null
     * 
     * If is the source document is an Article return the source document if
     * private else return null.
     * 
     */
    public abstract DocumentModel getDocumentRestrictedToMembers()
            throws ClientException;

    public abstract boolean isRestrictedToMembers() throws ClientException;

    public abstract boolean isDocumentInSocialWorkspace();

    /**
     * @return core type name of source document.
     */
    public abstract String getType();

}