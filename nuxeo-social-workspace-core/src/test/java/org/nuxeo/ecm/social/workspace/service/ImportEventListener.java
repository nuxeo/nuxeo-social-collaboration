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

package org.nuxeo.ecm.social.workspace.service;

import static org.nuxeo.ecm.social.workspace.SocialConstants.CTX_PRINCIPALS_PROPERTY;
import static org.nuxeo.ecm.social.workspace.SocialConstants.EVENT_MEMBERS_ADDED;
import static org.nuxeo.ecm.social.workspace.SocialConstants.EVENT_MEMBERS_REMOVED;

import java.security.Principal;
import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.runtime.api.Framework;

/**
 * Test class to help handling import event.
 * 
 * @author Arnaud Kervern <akervern@nuxeo.com>
 */
public class ImportEventListener implements EventListener {

    private static int memberAddedCount = 0;

    private static int memberRemovedCount = 0;

    private static int lastPrincipalsCount = 0;

    @Override
    public void handleEvent(Event event) throws ClientException {
        List<Principal> principals = (List<Principal>) event.getContext().getProperty(CTX_PRINCIPALS_PROPERTY);
        lastPrincipalsCount = principals.size();

        if (event.getName().equals(EVENT_MEMBERS_REMOVED)) {
            memberRemovedCount += lastPrincipalsCount;
        } else if (event.getName().equals(EVENT_MEMBERS_ADDED)) {
            memberAddedCount += lastPrincipalsCount;
        } else {
            throw new ClientException("Unknown event");
        }
    }

    public static int getMemberRemovedCount() {
        waitAsync();
        return memberRemovedCount;
    }

    public static int getMemberAddedCount() {
        waitAsync();
        return memberAddedCount;
    }

    public static int getLastPrincipalsCount() {
        waitAsync();
        return lastPrincipalsCount;
    }

    protected static void waitAsync() {
        Framework.getLocalService(EventService.class).waitForAsyncCompletion();
    }

    public static void reset() {
        memberAddedCount = 0;
        memberRemovedCount = 0;
        lastPrincipalsCount = 0;
    }
}
