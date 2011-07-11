/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 * Contributors:
 * Nuxeo - initial API and implementation
 */
package org.nuxeo.ecm.social.workspace.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.social.workspace.SocialConstants;

/**
 * * This class aims to provide information about the social document and mainly
 * about the kind of its publication status(i.e. if it is public or private).
 * 
 * In the documentation about method of this class, when we reference to a
 * social document, we implicitly talking about the social document handle by
 * the instance of the class at it's creation.
 * 
 * @author rlegall
 * 
 */
public class SocialDocumentStatusInfoHandler extends
        SocialDocumentPublicationHandler {

    private static final Log log = LogFactory.getLog(SocialDocumentStatusInfoHandler.class);

    /**
     * 
     * @param session the current session during which the social document is
     *            created
     * @param currentSocialDocument the social document on which social
     *            informations are needed
     */
    public SocialDocumentStatusInfoHandler(CoreSession session,
            DocumentModel currentSocialDocument) {
        super(session, currentSocialDocument);
        if (currentSocialDocument != null) {
            try {
                setCurrentProxy();
            } catch (ClientException e) {
                if (log.isErrorEnabled()) {
                    log.error("No proxy can be found");
                }
            }
        }
    }

    protected void setCurrentProxy() throws ClientException {
        DocumentModelList curSoclDocProxy = session.getProxies(
                currentSocialDocument.getRef(), publicSocialSection.getRef());
        curSoclDocProxy.addAll(session.getProxies(
                currentSocialDocument.getRef(), privateSocialSection.getRef()));
        if (curSoclDocProxy.isEmpty()) {
            return;
        }
        currentProxy = curSoclDocProxy.get(FIRST_AND_ONLY_PROXY);

    }

    /**
     * Look up for the existence of a proxy in the public social section. In
     * that case, the method returns true, false other wise.
     * 
     * @return true if the social document handled by the instance got a proxy
     *         in the public social section for its type. false, it there no
     *         proxy in this public social section or no proxy at all.
     */
    public boolean isPublic() {
        boolean currentProxyIsPublic = false;
        if (currentProxy == null) {
            return false;
        }
        try {
            currentProxyIsPublic = isPublicationSectionPublic();
        } catch (ClientException e) {
            if (log.isErrorEnabled()) {
                String message = String.format(
                        "No social section of publication found for \"%s\"",
                        currentProxy.toString());
                log.error(message);
            }
        }
        return currentProxyIsPublic;
    }

    /**
     * By default, a social document is private. So this method return false
     * only when a social document got a proxy in a public social section. At
     * it's creation, a social document is private.
     * 
     * @return true if the social document got no proxy in a public social
     *         section or if newly created. false in case the document handle by
     *         the current instance got a proxy in a public social section.
     */
    public boolean isPrivate() {
        return !isPublic();
    }

    protected boolean isPublicationSectionPublic() throws ClientException {
        DocumentModel publicNewsSection = lookForPublicationSection();
        return SocialConstants.PUBLIC_NEWS_SECTION_NAME.equals(publicNewsSection.getName());
    }

    protected DocumentModel lookForPublicationSection() throws ClientException {
        return session.getDocument(currentProxy.getParentRef());
    }
}
