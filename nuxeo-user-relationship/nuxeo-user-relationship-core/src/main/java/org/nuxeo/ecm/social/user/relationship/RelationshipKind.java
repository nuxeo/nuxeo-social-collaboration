package org.nuxeo.ecm.social.user.relationship;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * Pojo to describe a relation's kind
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
public class RelationshipKind implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String group;

    protected String name;

    protected static final String SEPARATOR = ":";

    protected RelationshipKind(String group, String name) {
        this.group = group;
        this.name = name;
    }

    public static RelationshipKind build(String group, String kindName) {
        return new RelationshipKind(group, kindName);
    }

    public static RelationshipKind buildEmpty() {
        return new RelationshipKind("", "");
    }

    public static RelationshipKind buildFromGroup(String group) {
        return build(group, "");
    }

    public static RelationshipKind buildFromName(String name) {
        return build("", name);
    }

    public boolean isEmpty() {
        return StringUtils.isEmpty(group) && StringUtils.isEmpty(name);
    }

    @Override
    public String toString() {
        return group + SEPARATOR + name;
    }
}
