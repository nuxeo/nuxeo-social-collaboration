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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
@Entity(name = "Tweet")
@Table(name = "tweets")
public class TweetActivity {

    private Long id;

    private String seenBy;

    private Long activityId;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, columnDefinition = "integer")
    public Long getInternalId() {
        return id;
    }

    public void setInternalId(Long id) {
        this.id = id;
    }

    public Serializable getId() {
        return id;
    }

    @Column(name = "activityId", columnDefinition = "integer")
    public Long getActivityId() {
        return activityId;
    }

    @Column(name = "seenBy")
    public String getSeenBy() {
        return seenBy;
    }

    public void setActivityId(Serializable activityId) {
        this.activityId = (Long) activityId;
    }

    public void setSeenBy(String seenBy) {
        this.seenBy = seenBy;
    }

    public void setId(Serializable id) {
        this.id = (Long) id;
    }

}
