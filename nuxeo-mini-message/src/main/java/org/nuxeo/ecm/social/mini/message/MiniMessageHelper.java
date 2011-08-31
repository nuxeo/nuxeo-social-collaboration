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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nuxeo.ecm.platform.htmlsanitizer.HtmlSanitizerService;
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

}
