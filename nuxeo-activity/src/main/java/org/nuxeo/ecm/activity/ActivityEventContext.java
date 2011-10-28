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
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.nuxeo.ecm.activity;

import java.security.Principal;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.event.impl.EventContextImpl;

/**
 * Specialized implementation to be used when firing event for an Activity.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class ActivityEventContext extends EventContextImpl {

    private static final long serialVersionUID = 1L;

    public ActivityEventContext(CoreSession session, Principal principal,
            Activity activity) {
        super(session, principal, activity);
    }

    public ActivityEventContext(Activity activity) {
        super(null, null, activity);
    }

    public Activity getActivity() {
        return (Activity) args[0];
    }

}
