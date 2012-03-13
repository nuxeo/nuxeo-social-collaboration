/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
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

package org.nuxeo.ecm.social.mini.message.operations;

import java.security.Principal;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.social.mini.message.MiniMessage;
import org.nuxeo.ecm.social.mini.message.MiniMessageService;

/**
 * Operation to remove a mini message.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
@Operation(id = RemoveMiniMessage.ID, category = Constants.CAT_SERVICES, label = "Remove a mini message", description = "Remove a mini message.")
public class RemoveMiniMessage {

    public static final String ID = "Services.RemoveMiniMessage";

    @Context
    protected CoreSession session;

    @Context
    protected MiniMessageService miniMessageService;

    @Param(name = "miniMessageId")
    protected String miniMessageId;

    @OperationMethod
    public void run() throws Exception {
        MiniMessage miniMessage = miniMessageService.getMiniMessage(Long.valueOf(miniMessageId));
        if (miniMessage != null) {
            Principal principal = session.getPrincipal();
            if (principal.getName().equals(miniMessage.getActor())) {
                miniMessageService.removeMiniMessage(miniMessage);
            }
        }
    }

}
