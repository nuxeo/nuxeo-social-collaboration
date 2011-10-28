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

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.ecm.core.api.ClientException;

/**
 * Descriptor object for registering {@link ActivityStreamFilter}s.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
@XObject("activityStreamFilter")
public class ActivityStreamFilterDescriptor {

    @XNode("@enabled")
    protected boolean enabled = true;

    @XNode("@class")
    protected Class<? extends ActivityStreamFilter> activityStreamFilterClass;

    public ActivityStreamFilterDescriptor() {
    }

    public ActivityStreamFilterDescriptor(
            Class<? extends ActivityStreamFilter> activityStreamFilterClass,
            boolean enabled) {
        this.activityStreamFilterClass = activityStreamFilterClass;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ActivityStreamFilter getActivityStreamFilter()
            throws ClientException {
        try {
            return activityStreamFilterClass.newInstance();
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

}
