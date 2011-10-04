/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Contributors:
 *      Nuxeo
 */

package org.nuxeo.ecm.social.user.relationship;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.nuxeo.ecm.social.user.relationship.UserRelationshipActions.USER_RELATIONSHIP_CHANGED;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.platform.contentview.seam.ContentViewActions;

/**
 * Social User Relationship Network action bean.
 * 
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.4.3
 */
@Name("userRelationshipNetworkActions")
@Scope(CONVERSATION)
public class UserRelationshipNetworkActions implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(UserRelationshipNetworkActions.class);

    @In
    protected transient ContentViewActions contentViewActions;

    @Observer(USER_RELATIONSHIP_CHANGED)
    public void resetContentView() {
        log.debug("Resetting current user relationship content view");
        contentViewActions.refreshOnSeamEvent(USER_RELATIONSHIP_CHANGED);
        contentViewActions.resetPageProviderOnSeamEvent(USER_RELATIONSHIP_CHANGED);
    }
}
