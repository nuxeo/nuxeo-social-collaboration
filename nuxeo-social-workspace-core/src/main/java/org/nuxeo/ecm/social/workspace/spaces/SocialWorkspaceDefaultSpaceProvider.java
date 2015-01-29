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

package org.nuxeo.ecm.social.workspace.spaces;

import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.user.center.dashboard.DefaultDashboardSpaceProvider;

/**
 * Overriding the default {@link org.nuxeo.ecm.spaces.api.Space} for a Social Workspace.
 * {@link org.nuxeo.ecm.user.center.dashboard.DefaultDashboardSpaceProvider}
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.5
 */
public class SocialWorkspaceDefaultSpaceProvider extends DefaultDashboardSpaceProvider {
    @Override
    protected DocumentRef getOrCreateDefaultDashboardSpace(CoreSession session, Map<String, String> parameters)
            throws ClientException {
        SocialWorkspaceDefaultSpaceCreator creator = new SocialWorkspaceDefaultSpaceCreator(session, parameters);
        creator.runUnrestricted();
        return creator.defaultDashboardSpaceRef;
    }
}
