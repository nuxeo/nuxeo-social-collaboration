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

package org.nuxeo.ecm.activity;

import org.nuxeo.ecm.platform.query.api.AbstractPageProvider;

/**
 * Basic PageProvider used to handle list of Activities.
 * <p>
 * Maintains the next offset to be used to have the next activities.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public abstract class AbstractActivityPageProvider<T> extends AbstractPageProvider<T> {

    protected long nextOffset;

    /**
     * Returns the next offset to use to have the next activities.
     */
    public long getNextOffset() {
        return nextOffset;
    }

}
