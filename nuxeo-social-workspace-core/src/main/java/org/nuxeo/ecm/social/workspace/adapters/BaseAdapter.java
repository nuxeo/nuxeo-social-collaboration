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
 *     eugen
 */
package org.nuxeo.ecm.social.workspace.adapters;

import java.io.Serializable;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 */
public abstract class BaseAdapter {

    protected DocumentModel doc;

    protected BaseAdapter(DocumentModel doc) {
        this.doc = doc;
    }

    protected static Object getDocProperty(DocumentModel doc, String xpath) {
        try {
            return doc.getPropertyValue(xpath);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    protected static void setDocProperty(DocumentModel doc, String xpath,
            Serializable value) {
        try {
            doc.setPropertyValue(xpath, value);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

}
