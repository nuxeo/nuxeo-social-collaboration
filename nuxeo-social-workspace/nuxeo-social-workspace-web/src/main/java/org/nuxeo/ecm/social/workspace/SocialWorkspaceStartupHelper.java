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
 *     Nuxeo
 */

package org.nuxeo.ecm.social.workspace;

import static org.jboss.seam.ScopeType.SESSION;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.webapp.helpers.StartupHelper;

/**
 * Overriding default {@see org.nuxeo.ecm.webapp.helpers.StartupHelper} to
 * redirect user into their dashboards after login in.
 * 
 * @author Arnaud Kervern <akervern@nuxeo.com>
 * @since 5.4.3
 */
@Name("startupHelper")
@Scope(SESSION)
public class SocialWorkspaceStartupHelper extends StartupHelper {
    private static final long serialVersionUID = 3248232383219879845L;

    private static final Log log = LogFactory.getLog(StartupHelper.class);

    @Override
    public String initDomainAndFindStartupPage(String domainTitle, String viewId) {
        String result = super.initDomainAndFindStartupPage(domainTitle, viewId);

        if (((NuxeoPrincipal)documentManager.getPrincipal()).isAdministrator()) {
            return result;
        } else {
            return dashboardNavigationHelper.navigateToDashboard();
        }
    }
}
