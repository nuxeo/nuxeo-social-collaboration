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
package org.nuxeo.ecm.social.workspace;

import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.usermanager.UserManager;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a> helper class for group
 *         members management ... (convert to operation ? )
 */

public class SocialGroupsManagement {

    public static boolean acceptMember(DocumentModel sws, String user,
            UserManager userManager) throws ClientException {
        DocumentModel principal = userManager.getUserModel(user);
        List<String> groups = (List<String>) principal.getProperty(
                userManager.getUserSchemaName(), "groups");
        if (groups.contains(SocialWorkspaceHelper.getCommunityAdministratorsGroupName(sws))) {
            return false;
        }
        String membersGroup = SocialWorkspaceHelper.getCommunityMembersGroupName(sws);
        if (groups.contains(membersGroup)) { // already a member
            return false;
        }
        groups.add(membersGroup);
        principal.setProperty(userManager.getUserSchemaName(), "groups", groups);
        userManager.updateUser(principal);
        // TODO send mail notification
        return true;
    }

}
