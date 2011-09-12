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

import java.io.StringWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.map.ObjectMapper;
import org.nuxeo.ecm.activity.AbstractActivityPageProvider;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.platform.htmlsanitizer.HtmlSanitizerService;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.runtime.api.Framework;

/**
 * Helper methods to deal with mini messages.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class MiniMessageHelper {

    public static Pattern HTTP_URL_PATTERN = Pattern.compile("\\b(https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])");

    private MiniMessageHelper() {
        // Helper class
    }

    public static String replaceURLsByLinks(String message) {
        Matcher m = HTTP_URL_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer(message.length());
        while (m.find()) {
            String url = m.group(1);
            m.appendReplacement(sb, computeLinkFor(url));
        }
        m.appendTail(sb);
        return Framework.getLocalService(HtmlSanitizerService.class).sanitizeString(
                sb.toString(), null);
    }

    private static String computeLinkFor(String url) {
        return "<a href=\"" + url + "\" target=\"_top\">" + url + "</a>";
    }

    public static String toJSON(PageProvider<MiniMessage> pageProvider,
            Locale locale, CoreSession session) {
        try {
            DateFormat dateFormat = DateFormat.getDateInstance(
                    DateFormat.MEDIUM, locale);

            List<Map<String, Object>> miniMessages = new ArrayList<Map<String, Object>>();
            for (MiniMessage miniMessage : pageProvider.getCurrentPage()) {
                Map<String, Object> o = new HashMap<String, Object>();
                o.put("id", miniMessage.getId());
                o.put("actor", miniMessage.getActor());
                o.put("displayActor", miniMessage.getDisplayActor());
                o.put("message", miniMessage.getMessage());
                o.put("publishedDate",
                        dateFormat.format(miniMessage.getPublishedDate()));
                o.put("isCurrentUserMiniMessage",
                        session.getPrincipal().getName().equals(
                                miniMessage.getActor()));
                miniMessages.add(o);
            }

            Map<String, Object> m = new HashMap<String, Object>();
            m.put("offset",
                    ((AbstractActivityPageProvider) pageProvider).getNextOffset());
            m.put("limit", pageProvider.getCurrentPageSize());
            m.put("miniMessages", miniMessages);

            ObjectMapper mapper = new ObjectMapper();
            StringWriter writer = new StringWriter();
            mapper.writeValue(writer, m);

            return writer.toString();
        } catch (Exception e) {
            throw new ClientRuntimeException(e);
        }
    }

}
