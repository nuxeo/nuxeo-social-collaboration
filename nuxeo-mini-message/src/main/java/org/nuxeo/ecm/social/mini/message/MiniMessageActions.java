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

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.io.Serializable;
import java.security.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.platform.contentview.seam.ContentViewActions;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;
import org.nuxeo.runtime.api.Framework;

/**
 * Handles MiniMessage related web actions.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
@Name("miniMessageActions")
@Scope(CONVERSATION)
@Install(precedence = FRAMEWORK)
public class MiniMessageActions implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String MINI_MESSAGE_CREATED_EVENT = "miniMessageCreated";

    @In(create = true)
    protected transient ContentViewActions contentViewActions;

    @In(create = true, required = false)
    protected FacesMessages facesMessages;

    @In(create = true)
    protected ResourcesAccessor resourcesAccessor;

    @In(required = true, create = true)
    protected transient Principal currentUser;

    protected String newMessage;

    protected static final Log log = LogFactory.getLog(MiniMessageActions.class);

    public String getNewMessage() {
        return newMessage;
    }

    public void setNewMessage(String newMessage) {
        this.newMessage = newMessage;
    }

    public void createNewMiniMessage() {
        MiniMessageService miniMessageService = Framework.getLocalService(MiniMessageService.class);
        miniMessageService.addMiniMessage(currentUser, newMessage);
        Events.instance().raiseEvent(MINI_MESSAGE_CREATED_EVENT);
        newMessage = null;
    }

    public void deleteMiniMessage(MiniMessage miniMessage) {
        MiniMessageService miniMessageService = Framework.getLocalService(MiniMessageService.class);
        miniMessageService.removeMiniMessage(miniMessage);
        facesMessages.add(
                StatusMessage.Severity.INFO,
                resourcesAccessor.getMessages().get("info.mini.message.deleted"));
    }

    @Observer(MINI_MESSAGE_CREATED_EVENT)
    public void onMiniMessageCreated() {
        contentViewActions.refreshOnSeamEvent(MINI_MESSAGE_CREATED_EVENT);
        contentViewActions.resetPageProviderOnSeamEvent(MINI_MESSAGE_CREATED_EVENT);
    }

}
