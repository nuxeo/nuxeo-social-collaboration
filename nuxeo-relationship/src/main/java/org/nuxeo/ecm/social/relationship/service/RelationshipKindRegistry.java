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

package org.nuxeo.ecm.social.relationship.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nuxeo.ecm.social.relationship.RelationshipKind;
import org.nuxeo.runtime.model.ContributionFragmentRegistry;

/**
 * Registry for relationship kinds, handling merge of registered
 * {@link RelationshipKind} elements.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class RelationshipKindRegistry extends
        ContributionFragmentRegistry<RelationshipKindDescriptor> {

    private Map<String, Set<RelationshipKind>> registeredKinds = new HashMap<String, Set<RelationshipKind>>();

    /**
     * Returns all the registered {@link RelationshipKind}s.
     */
    public Set<RelationshipKind> getRegisteredKinds() {
        return getRegisteredKinds(null);
    }

    /**
     * Returns the registered {@link RelationshipKind}s for the given
     * {@code group}.
     * <p>
     * If {@code group} is {@code null}, returns all the registered
     * {@link RelationshipKind}s.
     */
    public Set<RelationshipKind> getRegisteredKinds(String group) {
        if (group != null) {
            return registeredKinds.get(group);
        } else {
            Set<RelationshipKind> allKinds = new HashSet<RelationshipKind>();
            for (Set<RelationshipKind> kinds : registeredKinds.values()) {
                allKinds.addAll(kinds);
            }
            return allKinds;
        }
    }

    /**
     * Removes all the unregistered {@link RelationshipKind}s from the
     * {@code kindsToFilter} Set.
     */
    public Set<RelationshipKind> filterUnregisteredRelationshipKinds(
            Set<RelationshipKind> kindsToFilter) {
        Set<RelationshipKind> filteredKinds = new HashSet<RelationshipKind>();
        for (RelationshipKind kind : kindsToFilter) {
            Set<RelationshipKind> kinds = registeredKinds.get(kind.getGroup());
            if (kinds != null && kinds.contains(kind)) {
                filteredKinds.add(kind);
            }
        }
        return filteredKinds;
    }

    @Override
    public String getContributionId(RelationshipKindDescriptor contrib) {
        return contrib.getId();
    }

    @Override
    public void contributionUpdated(String id,
            RelationshipKindDescriptor contrib,
            RelationshipKindDescriptor newOrigContrib) {
        String group = contrib.getGroup();
        String name = contrib.getName();
        if (contrib.isEnabled()) {
            Set<RelationshipKind> kinds = registeredKinds.get(group);
            if (kinds == null) {
                kinds = new HashSet<RelationshipKind>();
                registeredKinds.put(group, kinds);
            }
            RelationshipKind kind = RelationshipKind.newInstance(group, name);
            kinds.add(kind);
        } else {
            removeRelationshipKind(contrib);
        }
    }

    private void removeRelationshipKind(RelationshipKindDescriptor contrib) {
        Set<RelationshipKind> kinds = registeredKinds.get(contrib.getGroup());
        RelationshipKind kind = RelationshipKind.newInstance(
                contrib.getGroup(), contrib.getName());
        kinds.remove(kind);
    }

    @Override
    public void contributionRemoved(String id,
            RelationshipKindDescriptor origContrib) {
        removeRelationshipKind(origContrib);
    }

    @Override
    public RelationshipKindDescriptor clone(RelationshipKindDescriptor orig) {
        return orig.clone();
    }

    @Override
    public void merge(RelationshipKindDescriptor src,
            RelationshipKindDescriptor dst) {
        dst.setName(src.getName());
        dst.setGroup(src.getGroup());
        boolean enabled = src.isEnabled();
        if (enabled != dst.isEnabled()) {
            dst.setEnabled(enabled);
        }
    }

}
