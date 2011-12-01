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
import java.util.Collections;
import java.util.List;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
@XObject("activityStream")
public class ActivityStream {

    @XNode("@name")
    String name;

    @XNode("verbs@append")
    boolean appendVerbs;

    @XNodeList(value = "verbs/verb", type = ArrayList.class, componentType = String.class)
    List<String> verbs;

    @XNode("relationshipKinds@append")
    boolean appendRelationshipKinds;

    @XNodeList(value = "relationshipKinds/relationshipKind", type = ArrayList.class, componentType = String.class)
    List<String> relationshipKinds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAppendVerbs() {
        return appendVerbs;
    }

    public void setAppendVerbs(boolean appendVerbs) {
        this.appendVerbs = appendVerbs;
    }

    public List<String> getVerbs() {
        if (verbs == null) {
            return Collections.emptyList();
        }
        return verbs;
    }

    public void setVerbs(List<String> verbs) {
        this.verbs = verbs;
    }

    public boolean isAppendRelationshipKinds() {
        return appendRelationshipKinds;
    }

    public void setAppendRelationshipKinds(boolean appendRelationshipKinds) {
        this.appendRelationshipKinds = appendRelationshipKinds;
    }

    public List<String> getRelationshipKinds() {
        if (relationshipKinds == null) {
            return Collections.emptyList();
        }
        return relationshipKinds;
    }

    public void setRelationshipKinds(List<String> relationshipKinds) {
        this.relationshipKinds = relationshipKinds;
    }

    @Override
    public ActivityStream clone() {
        ActivityStream clone = new ActivityStream();
        clone.setName(getName());
        clone.setAppendVerbs(isAppendVerbs());
        List<String> verbs = getVerbs();
        if (verbs != null) {
            List<String> newVerbs = new ArrayList<String>();
            for (String verb : verbs) {
                newVerbs.add(verb);
            }
            clone.setVerbs(newVerbs);
        }
        clone.setAppendRelationshipKinds(isAppendRelationshipKinds());
        List<String> relationshipKinds = getRelationshipKinds();
        if (relationshipKinds != null) {
            List<String> newRelationshipKinds = new ArrayList<String>();
            for (String kind : relationshipKinds) {
                newRelationshipKinds.add(kind);
            }
            clone.setRelationshipKinds(newRelationshipKinds);
        }
        return clone;
    }
}
