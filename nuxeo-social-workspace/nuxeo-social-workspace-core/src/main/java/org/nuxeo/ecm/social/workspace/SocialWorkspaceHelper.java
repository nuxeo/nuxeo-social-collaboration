/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 * Contributors:
 * Nuxeo - initial API and implementation
 */

package org.nuxeo.ecm.social.workspace;

import java.io.Serializable;

import javax.print.Doc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;

/**
 * Class to provide some useful methods
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.4.1
 */
public class SocialWorkspaceHelper {

	private static final Log log = LogFactory.getLog(SocialWorkspaceHelper.class);

    public static final String ADMINISTRATORS_SUFFIX = "_administrators";

    public static final String MEMBERS_SUFFIX = "_members";

    private SocialWorkspaceHelper() {
        // Helper class
    }

    public static String getCommunityAdministratorsGroupName(DocumentModel doc) {
        return doc.getId() + ADMINISTRATORS_SUFFIX;
    }

    public static String getCommunityMembersGroupName(DocumentModel doc) {
        return doc.getId() + MEMBERS_SUFFIX;
    }

    public static Object getDocProperty(DocumentModel doc, String xpath) {
    	try {
			return doc.getPropertyValue(xpath);
		} catch (PropertyException e) {
			log.debug(e);
		} catch (ClientException e) {
			log.debug(e);
		}
		return null;
    }

    public static void setDocProperty(DocumentModel doc, String xpath, Serializable value){
    	try {
			doc.setPropertyValue(xpath, value);
		} catch (PropertyException e) {
			log.debug(e);
		} catch (ClientException e) {
			log.debug(e);
		}
    }


}
