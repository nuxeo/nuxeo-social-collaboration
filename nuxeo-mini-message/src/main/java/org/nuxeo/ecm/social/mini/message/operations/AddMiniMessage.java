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

package org.nuxeo.ecm.social.mini.message.operations;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.impl.blob.InputStreamBlob;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.social.mini.message.MiniMessage;
import org.nuxeo.ecm.social.mini.message.MiniMessageService;

/**
 * Operation to add a mini message.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
@Operation(id = AddMiniMessage.ID, category = Constants.CAT_SERVICES, label = "Add a mini message", description = "Add a mini message.")
public class AddMiniMessage {

    public static final String ID = "Services.AddMiniMessage";

    @Context
    protected CoreSession session;

    @Context
    protected MiniMessageService miniMessageService;

    @Context
    protected UserManager userManager;

    @Param(name = "language", required = false)
    protected String language;

    @Param(name = "message")
    protected String message;

    @Param(name = "publishedDate", required = false)
    protected Date publishedDate;

    @Param(name = "contextPath", required = false)
    protected String contextPath;

    @OperationMethod
    public Blob run() throws Exception {
        if (publishedDate == null) {
            publishedDate = new Date();
        }

        Locale locale = language != null && !language.isEmpty() ? new Locale(
                language) : Locale.ENGLISH;
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM,
                locale);

        String target = null;
        if (contextPath != null) {
            TargetActivityObject targetActivityObject = new TargetActivityObject(
                    session, contextPath);
            targetActivityObject.runUnrestricted();
            if (targetActivityObject.documentActivityObject != null) {
                target = targetActivityObject.documentActivityObject;
            }
        }

        MiniMessage miniMessage = miniMessageService.addMiniMessage(
                session.getPrincipal(), message, publishedDate, target);

        NuxeoPrincipal principal = userManager.getPrincipal(miniMessage.getActor());
        String fullName = principal == null ? "" : principal.getFirstName()
                + " " + principal.getLastName();

        Map<String, Object> o = new HashMap<String, Object>();
        o.put("id", miniMessage.getId());
        o.put("actor", miniMessage.getActor());
        o.put("fullName", fullName);
        o.put("message", miniMessage.getMessage());
        o.put("publishedDate",
                dateFormat.format(miniMessage.getPublishedDate()));
        o.put("isCurrentUserMiniMessage",
                session.getPrincipal().getName().equals(miniMessage.getActor()));

        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, o);

        return new InputStreamBlob(new ByteArrayInputStream(
                writer.toString().getBytes("UTF-8")), "application/json");
    }

    private static class TargetActivityObject extends UnrestrictedSessionRunner {

        private String contextPath;

        public String documentActivityObject;

        public TargetActivityObject(CoreSession session, String contextPath) {
            super(session);
            this.contextPath = contextPath;
        }

        @Override
        public void run() throws ClientException {
            DocumentRef docRef = new PathRef(contextPath);
            if (session.exists(docRef)) {
                DocumentModel doc = session.getDocument(docRef);
                documentActivityObject = ActivityHelper.createDocumentActivityObject(doc);
            }
        }

    }

}
