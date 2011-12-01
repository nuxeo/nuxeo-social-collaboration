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

import static org.nuxeo.opensocial.container.shared.layout.api.LayoutHelper.Preset.X_2_DEFAULT;

import java.util.Locale;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.spaces.api.Space;
import org.nuxeo.ecm.spaces.helper.WebContentHelper;
import org.nuxeo.ecm.user.center.dashboard.DefaultDashboardSpaceCreator;
import org.nuxeo.opensocial.container.shared.layout.api.LayoutHelper;

/**
 * Create the default social dashboard {@code Space} in an Unrestricted Session.
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
public class SocialWorkspaceDefaultSpaceCreator extends
        DefaultDashboardSpaceCreator {
    public SocialWorkspaceDefaultSpaceCreator(CoreSession session,
            Map<String, String> parameters) {
        super(session, parameters);
    }

    @Override
    protected void initializeLayout(Space space) throws ClientException {
        space.initLayout(LayoutHelper.buildLayout(X_2_DEFAULT));
    }

    @Override
    protected void initializeGadgets(Space space, CoreSession session,
            Locale locale) throws ClientException {
        WebContentHelper.createOpenSocialGadget(space, session, locale,
                "usersocialworkspaces", 0, 0, 0);
        WebContentHelper.createOpenSocialGadget(space, session, locale, "news",
                0, 0, 1);
        WebContentHelper.createOpenSocialGadget(space, session, locale,
                "publicsocialworkspaces", 0, 0, 2);
        WebContentHelper.createOpenSocialGadget(space, session, locale,
                "userworkspaces", 0, 0, 3);
        WebContentHelper.createOpenSocialGadget(space, session, locale,
                "userdocuments", 0, 0, 4);

        WebContentHelper.createOpenSocialGadget(space, session, locale,
                "minimessages", 0, 1, 0);
        WebContentHelper.createOpenSocialGadget(space, session, locale,
                "useractivitystream", 0, 1, 1);
        WebContentHelper.createOpenSocialGadget(space, session, locale,
                "waitingfor", 0, 1, 2);
        WebContentHelper.createOpenSocialGadget(space, session, locale,
                "tasks", 0, 1, 3);
    }
}
