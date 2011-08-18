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

import static org.nuxeo.ecm.social.workspace.SocialConstants.SUBSCRIPTION_REQUEST_INFO_PROPERTY;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SUBSCRIPTION_REQUEST_PROCESSED_COMMENT_PROPERTY;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SUBSCRIPTION_REQUEST_PROCESSED_DATE_PROPERTY;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SUBSCRIPTION_REQUEST_TYPE_PROPERTY;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SUBSCRIPTION_REQUEST_USERNAME_PROPERTY;

import java.util.Calendar;

import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 */
public class SubscriptionRequestAdapter extends BaseAdapter implements
        SubscriptionRequest {

    public SubscriptionRequestAdapter(DocumentModel doc) {
        super(doc);
    }

    @Override
    public String getUsername() {
        return (String) getDocProperty(doc,
                SUBSCRIPTION_REQUEST_USERNAME_PROPERTY);
    }

    @Override
    public void setUserName(String value) {
        setDocProperty(doc, SUBSCRIPTION_REQUEST_USERNAME_PROPERTY, value);
    }

    @Override
    public String getType() {
        return (String) getDocProperty(doc, SUBSCRIPTION_REQUEST_TYPE_PROPERTY);
    }

    @Override
    public void setType(String value) {
        setDocProperty(doc, SUBSCRIPTION_REQUEST_TYPE_PROPERTY, value);
    }

    @Override
    public String getInfo() {
        return (String) getDocProperty(doc, SUBSCRIPTION_REQUEST_INFO_PROPERTY);
    }

    @Override
    public void setInfo(String value) {
        setDocProperty(doc, SUBSCRIPTION_REQUEST_INFO_PROPERTY, value);
    }

    @Override
    public Calendar getProcessedDate() {
        return (Calendar) getDocProperty(doc,
                SUBSCRIPTION_REQUEST_PROCESSED_DATE_PROPERTY);
    }

    @Override
    public void setProcessedDate(Calendar value) {
        setDocProperty(doc, SUBSCRIPTION_REQUEST_PROCESSED_DATE_PROPERTY, value);
    }

    @Override
    public String getProcessedComment() {
        return (String) getDocProperty(doc,
                SUBSCRIPTION_REQUEST_PROCESSED_COMMENT_PROPERTY);
    }

    @Override
    public void setProcessedComment(String value) {
        setDocProperty(doc, SUBSCRIPTION_REQUEST_PROCESSED_COMMENT_PROPERTY,
                value);
    }

    @Override
    public DocumentModel getDocument() {
        return doc;
    }

}
