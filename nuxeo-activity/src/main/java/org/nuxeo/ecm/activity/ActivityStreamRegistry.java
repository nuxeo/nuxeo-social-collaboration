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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.runtime.model.ContributionFragmentRegistry;

/**
 * Registry for activity streams, handling merge of registered
 * {@link ActivityStream} elements.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class ActivityStreamRegistry extends
        ContributionFragmentRegistry<ActivityStream> {

    protected Map<String, ActivityStream> activityStreams = new HashMap<String, ActivityStream>();

    public ActivityStream get(String name) {
        return activityStreams.get(name);
    }

    @Override
    public String getContributionId(ActivityStream contrib) {
        return contrib.getName();
    }

    @Override
    public void contributionUpdated(String id, ActivityStream contrib,
            ActivityStream newOrigContrib) {
        activityStreams.put(id, contrib);
    }

    @Override
    public void contributionRemoved(String id, ActivityStream origContrib) {
        activityStreams.remove(id);
    }

    @Override
    public ActivityStream clone(ActivityStream orig) {
        return orig.clone();
    }

    @Override
    public void merge(ActivityStream src, ActivityStream dst) {
        List<String> newVerbs = src.getVerbs();
        if (newVerbs != null) {
            List<String> merged = new ArrayList<String>();
            merged.addAll(newVerbs);
            boolean keepOld = src.isAppendVerbs()
                    || (newVerbs.isEmpty() && !src.isAppendVerbs());
            if (keepOld) {
                // add back old contributions
                List<String> oldVerbs = dst.getVerbs();
                if (oldVerbs != null) {
                    merged.addAll(0, oldVerbs);
                }
            }
            dst.setVerbs(merged);
        }

        List<String> newRelationshipKinds = src.getRelationshipKinds();
        if (newRelationshipKinds != null) {
            List<String> merged = new ArrayList<String>();
            merged.addAll(newRelationshipKinds);
            boolean keepOld = src.isAppendRelationshipKinds()
                    || (newRelationshipKinds.isEmpty() && !src.isAppendRelationshipKinds());
            if (keepOld) {
                // add back old contributions
                List<String> oldRelationshipKinds = dst.getRelationshipKinds();
                if (oldRelationshipKinds != null) {
                    merged.addAll(0, oldRelationshipKinds);
                }
            }
            dst.setRelationshipKinds(merged);
        }
    }
}
