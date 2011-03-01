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

import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_REQUEST_INFO;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_REQUEST_PROCESSED_COMMENT;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_REQUEST_PROCESSED_DATE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_REQUEST_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_REQUEST_USERNAME;

import java.util.Calendar;

import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 *
 */
public class RequestAdapterImpl extends BaseAdapter implements RequestAdapter {

    /**
     * @param doc
     */
    public RequestAdapterImpl(DocumentModel doc) {
        super(doc);
        // TODO Auto-generated constructor stub
    }

    public String getUsername() {
        return (String) getDocProperty(doc, FIELD_REQUEST_USERNAME);
    }

    public void setUserNanme(String value) {
        setDocProperty(doc, FIELD_REQUEST_USERNAME, value);
    }

    public String getType() {
        return (String) getDocProperty(doc, FIELD_REQUEST_TYPE);
    }

    public void setType(String value) {
        setDocProperty(doc, FIELD_REQUEST_TYPE, value);
    }

    public String getInfo() {
        return (String) getDocProperty(doc, FIELD_REQUEST_INFO);
    }

    public void setInfo(String value) {
        setDocProperty(doc, FIELD_REQUEST_INFO, value);
    }

    public Calendar getProcessedDate() {
        return (Calendar) getDocProperty(doc, FIELD_REQUEST_PROCESSED_DATE);
    }

    public void setProcessedDate(Calendar value) {
        setDocProperty(doc, FIELD_REQUEST_PROCESSED_DATE, value);
    }

    public String getProcessedComment() {
        return (String) getDocProperty(doc, FIELD_REQUEST_PROCESSED_COMMENT);
    }

    @Override
    public void setProcessdComment(String value) {
        setDocProperty(doc, FIELD_REQUEST_PROCESSED_COMMENT, value);
    }

}
