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
 *     eugen
 */
package org.nuxeo.ecm.social.workspace;

import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_REQUEST_USERNAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.TYPE_REQUEST;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 *
 */

@Name("requestActions")
@Scope(ScopeType.CONVERSATION)
public class RequestActions implements Serializable {

    private static final long serialVersionUID = -7362146679190186610L;

    private static Log log = LogFactory.getLog(RequestActions.class);

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient DocumentsListsManager documentsListsManager;

    @In(create = true)
    protected transient UserManager userManager;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    public void accept() throws ClientException {
        List<DocumentModel> list = documentsListsManager.getWorkingList(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION);
        processSelectedRequests(list, "accept");
    }

    public void reject() throws ClientException {
        List<DocumentModel> list = documentsListsManager.getWorkingList(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION);
        processSelectedRequests(list, "reject");
    }

    protected void processSelectedRequests(List<DocumentModel> list,
            String transition) throws ClientException {
        DocumentModel sws = navigationContext.getCurrentDocument();
        for (DocumentModel doc : list) {
            try {
                if (TYPE_REQUEST.equals(doc.getType())) {
                    String userName = (String) doc.getPropertyValue(FIELD_REQUEST_USERNAME);
                    boolean ok = SocialGroupsManagement.acceptMember(sws,
                            userName, userManager);
                    if (ok) {
                        doc.followTransition(transition);
                        // TODO send notification mail ECP-117
                    }
                }
            } catch (ClientException e) {
                log.debug("failed to accept request " + doc.getId(), e);
            }
        }
        documentManager.save();
    }

}
