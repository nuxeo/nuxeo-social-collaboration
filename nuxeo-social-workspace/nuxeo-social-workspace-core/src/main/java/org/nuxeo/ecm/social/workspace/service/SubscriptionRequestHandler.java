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

package org.nuxeo.ecm.social.workspace.service;

import java.security.Principal;

import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.adapters.SubscriptionRequest;

/**
 * Handles Subscription Request for Social Workspaces.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public interface SubscriptionRequestHandler {

    void handleSubscriptionRequestFor(SocialWorkspace socialWorkspace,
            Principal principal);

    boolean isSubscriptionRequestPending(SocialWorkspace socialWorkspace,
            Principal principal);

    String getSubscriptionRequestStatus(SocialWorkspace socialWorkspace,
            Principal principal);

    void acceptSubscriptionRequest(SocialWorkspace socialWorkspace,
            SubscriptionRequest subscriptionRequest);

    void rejectSubscriptionRequest(SocialWorkspace socialWorkspace,
            SubscriptionRequest subscriptionRequest);

}
