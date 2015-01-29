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

import static org.nuxeo.ecm.social.workspace.SocialConstants.DC_AUTHOR_PROPERTY;
import static org.nuxeo.ecm.social.workspace.SocialConstants.DC_CREATED_PROPERTY;
import static org.nuxeo.ecm.social.workspace.SocialConstants.DC_TITLE_PROPERTY;
import static org.nuxeo.ecm.social.workspace.SocialConstants.NOTE_NOTE_PROPERTY;

import java.util.Calendar;

import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 */
public class ArticleAdapter extends BaseAdapter implements Article {

    public ArticleAdapter(DocumentModel doc) {
        super(doc);
    }

    @Override
    public String getAuthor() {
        return (String) getDocProperty(doc, DC_AUTHOR_PROPERTY);
    }

    @Override
    public Calendar getCreated() {
        return (Calendar) getDocProperty(doc, DC_CREATED_PROPERTY);
    }

    @Override
    public String getTitle() {
        return (String) getDocProperty(doc, DC_TITLE_PROPERTY);
    }

    @Override
    public String getContent() {
        return (String) getDocProperty(doc, NOTE_NOTE_PROPERTY);
    }

    @Override
    public String getFirstNCharacters(int n) {
        String s = getContent();
        if (s != null) {
            int length = s.length();
            return s.substring(0, n < length ? n : length);
        }
        return "";
    }

    @Override
    public void setTitle(String text) {
        setDocProperty(doc, DC_TITLE_PROPERTY, text);
    }

    @Override
    public void setContent(String text) {
        setDocProperty(doc, NOTE_NOTE_PROPERTY, text);
    }

}
