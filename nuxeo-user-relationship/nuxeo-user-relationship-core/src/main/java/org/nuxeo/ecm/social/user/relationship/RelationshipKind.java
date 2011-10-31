package org.nuxeo.ecm.social.user.relationship;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Pojo to describe a relation's kind
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.5
 */
public class RelationshipKind implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final String group;

    protected final String name;

    protected static final String SEPARATOR = ":";

    protected RelationshipKind(String group, String name) {
        this.group = StringUtils.isBlank(group) ? "" : group;
        this.name = StringUtils.isBlank(name) ? "" : name;
    }

    public static RelationshipKind newInstance(String group, String kindName) {
        return new RelationshipKind(group, kindName);
    }

    public static RelationshipKind fromString(String value) {
        int pos = value.indexOf(SEPARATOR);
        if (pos >= 0) {
            String group = value.substring(0, pos);
            String name = value.substring(pos + 1, value.length());
            return new RelationshipKind(group, name);
        } else {
            return null;
        }
    }

    public static RelationshipKind fromGroup(String group) {
        return newInstance(group, "");
    }

    public static RelationshipKind fromName(String name) {
        return newInstance("", name);
    }

    public boolean isEmpty() {
        return StringUtils.isEmpty(group) && StringUtils.isEmpty(name);
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return group + SEPARATOR + name;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
