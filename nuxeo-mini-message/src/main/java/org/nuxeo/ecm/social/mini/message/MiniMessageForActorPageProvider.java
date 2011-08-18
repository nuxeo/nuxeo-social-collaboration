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

package org.nuxeo.ecm.social.mini.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.platform.query.api.AbstractPageProvider;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.social.user.relationship.RelationshipKind;
import org.nuxeo.runtime.api.Framework;

/**
 * Page provider listing mini messages for a given actor
 * <p>
 * This page provider requires two properties: the first one to be filled with
 * the actor, and the second one to be filled with the relationship kind to use
 * between the actor and.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class MiniMessageForActorPageProvider extends
        AbstractPageProvider<MiniMessage> implements PageProvider<MiniMessage> {

    private static final long serialVersionUID = 1L;

    public static final String ACTOR_PROPERTY = "actor";

    public static final String RELATIONSHIP_KIND_PROPERTY = "relationshipKind";

    protected MiniMessageService miniMessageService;

    protected List<MiniMessage> pageMiniMessages;

    @Override
    public List<MiniMessage> getCurrentPage() {
        if (pageMiniMessages == null) {
            pageMiniMessages = new ArrayList<MiniMessage>();
            long pageSize = getMinMaxPageSize();
            pageMiniMessages.addAll(getMiniMessageService().getMiniMessageFor(
                    getActor(), getRelationshipKind(), (int) pageSize,
                    (int) getCurrentPageIndex()));
            resultsCount = Integer.MAX_VALUE - 1;
        }
        return pageMiniMessages;
    }

    protected MiniMessageService getMiniMessageService()
            throws ClientRuntimeException {
        if (miniMessageService == null) {
            try {
                miniMessageService = Framework.getService(MiniMessageService.class);
            } catch (Exception e) {
                final String errMsg = "Error connecting to MiniMessageService. "
                        + e.getMessage();
                throw new ClientRuntimeException(errMsg, e);
            }
            if (miniMessageService == null) {
                throw new ClientRuntimeException(
                        "MiniMessageService service not bound");
            }
        }
        return miniMessageService;
    }

    protected String getActor() {
        Map<String, Serializable> props = getProperties();
        String actor = (String) props.get(ACTOR_PROPERTY);
        if (actor == null) {
            throw new ClientRuntimeException("Cannot find " + ACTOR_PROPERTY
                    + " property.");
        }
        return actor;
    }

    protected RelationshipKind getRelationshipKind() {
        Map<String, Serializable> props = getProperties();
        String relationshipKind = (String) props.get(RELATIONSHIP_KIND_PROPERTY);
        if (relationshipKind == null) {
            throw new ClientRuntimeException("Cannot find "
                    + RELATIONSHIP_KIND_PROPERTY + " property.");
        }
        return RelationshipKind.fromString(relationshipKind);
    }

    @Override
    public boolean isSortable() {
        return false;
    }

    @Override
    protected void pageChanged() {
        super.pageChanged();
        pageMiniMessages = null;
    }

    @Override
    public void refresh() {
        super.refresh();
        pageMiniMessages = null;
    }

}
