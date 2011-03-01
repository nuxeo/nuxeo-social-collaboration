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

import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_DC_AUTHOR;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_DC_CREATED;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_DC_TITLE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_NOTE_NOTE;

import java.util.Calendar;

import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 *
 */
public class ArticleAdapterImpl extends BaseAdapter implements ArticleAdapter {

    public ArticleAdapterImpl(DocumentModel doc) {
        super(doc);
    }

    @Override
    public String getAuthor() {
        return (String) getDocProperty(doc, FIELD_DC_AUTHOR);
    }

    @Override
    public Calendar getCreated() {
        return (Calendar) getDocProperty(doc, FIELD_DC_CREATED);
    }

    @Override
    public String getTitle() {
        return (String) getDocProperty(doc, FIELD_DC_TITLE);
    }

    @Override
    public String getContent() {
        return (String) getDocProperty(doc, FIELD_NOTE_NOTE);
    }

    @Override
    public String getFirstNCharacters(int n) {
        String s = getContent();
        if (s != null) {
            int length = s.length();
            s.substring(0, n < length ? n : length);
        }
        return "";
    }

    @Override
    public void setTitle(String text) {
        setDocProperty(doc, FIELD_DC_TITLE, text);
    }

    @Override
    public void setContent(String text) {
        setDocProperty(doc, FIELD_NOTE_NOTE, text);
    }



}
