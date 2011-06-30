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

import static org.nuxeo.ecm.social.workspace.SocialConstants.NEWS_SECTION_NAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.PUBLIC_NEWS_SECTION_NAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.ROOT_SECTION_NAME;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_DOCUMENT_FACET;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_PUBLICATION_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_TYPE;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.social.workspace.SocialWorkspaceHelper;

/**
 * This class aims to provide information about the social document and manage
 * the creation of proxy in the private or public social section.
 *
 * In the documentation about method of this class, when we reference to a
 * social document, we implicitly talking about the social document handle by
 * the instance of the class at it's creation.
 *
 */
public class SocialDocumentPublicationHandler {

    protected static final int FIRST_AND_ONLY_PROXY = 0;

    private static final int MAXIMAL_NUMBER_OF_PROXY_PER_SOCIAL_DOCUMENT = 1;

    private static final Log log = LogFactory.getLog(SocialDocumentPublicationHandler.class);

    CoreSession session;

    DocumentModel currentSocialDocument;

    DocumentModel socialWorkspace;

    DocumentModel privateSocialSection;

    DocumentModel publicSocialSection;

    DocumentModel currentProxy;

    String endOfPrivateSocialSectionPath = "/" + ROOT_SECTION_NAME + "/"
            + NEWS_SECTION_NAME;

    String endOfPublicSocialSectionPath = endOfPrivateSocialSectionPath + "/"
            + PUBLIC_NEWS_SECTION_NAME;

    /**
     *
     *
     * @param session the current session during which the social document is
     *            created
     * @param currentSocialDocument the social document on which social
     *            publications need to be performed
     */
    public SocialDocumentPublicationHandler(CoreSession session,
            DocumentModel currentSocialDocument) {
        super();
        if (session != null && currentSocialDocument != null) {
            this.session = session;
            this.currentSocialDocument = currentSocialDocument;
            lookForSocialWorkspaceAndSections();
        } else {
            if (log.isInfoEnabled()) {
                log.info("It's not possible to perfom a social document publication management"
                        + "if the session or the social document don't exist.");
            }
        }
    }

    protected void lookForSocialWorkspaceAndSections() {
        try {
            List<DocumentModel> parents = session.getParentDocuments(currentSocialDocument.getRef());
            getSocialWorkspaceParentIfAny(parents, currentSocialDocument);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                String message = String.format(
                        "It's not possible to find the parents of \"%s\"",
                        currentSocialDocument.toString());
                log.error(message);
            }
        }
    }

    protected void getSocialWorkspaceParentIfAny(List<DocumentModel> parents,
            DocumentModel socialDocument) {
        for (DocumentModel currentParent : parents) {
            if (SocialWorkspaceHelper.isSocialWorkspace(currentParent)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format(
                            "There is a %s as parent for the document \"%s\" and it's : \"%s\"",
                            SOCIAL_WORKSPACE_TYPE, socialDocument.toString(),
                            currentParent.toString()));
                }

                socialWorkspace = currentParent;
                initSections();
                return;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format(
                    "There is no %s as parent for the document \"%s\"",
                    SOCIAL_WORKSPACE_TYPE, socialDocument.toString()));
        }
        ;
    }

    protected void initSections() {
        initPrivateSection();
        initPublicSection();
    }

    protected void initPrivateSection() {
        privateSocialSection = initSingleSocialSection(endOfPrivateSocialSectionPath);
    }

    protected DocumentModel initSingleSocialSection(
            String endOfTheSocialSectionPath) {
        DocumentRef socialSectionPathRef = new PathRef(
                socialWorkspace.getPathAsString() + endOfTheSocialSectionPath);
        DocumentModel result = null;
        try {
            result = session.getDocument(socialSectionPathRef);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                String message = String.format(
                        "It's impossible to found the section with the path \"%s\".",
                        socialSectionPathRef.toString());
                log.error(message);
            }
        }
        return result;
    }

    protected void initPublicSection() {
        publicSocialSection = initSingleSocialSection(endOfPublicSocialSectionPath);
    }

    protected boolean isPrivatePublishable() {
        return isSocialyPublishable()
                && isSectionSocialSection(privateSocialSection);
    }

    protected boolean isSectionSocialSection(DocumentModel socialSection) {
        return socialSection != null
                && SOCIAL_PUBLICATION_TYPE.equals(socialSection.getType());
    }

    protected boolean isSocialyPublishable() {
        return currentSocialDocument != null
                && currentSocialDocument.hasFacet(SOCIAL_DOCUMENT_FACET)
                && socialWorkspace != null;
    }

    protected boolean isPublicPublishable() {
        return isSocialyPublishable()
                && isSectionSocialSection(publicSocialSection);
    }

    /**
     * Used to create or update and move a proxy of the handled social document
     * in a private social section
     *
     * @return the proxy created or updated
     */
    public DocumentModel publishPrivatelySocialDocument() {
        if (isPrivatePublishable()) {
            return publishSocialDocument(privateSocialSection);
        }
        if (log.isInfoEnabled()) {
            String message = String.format(
                    "The social document \"%s\" can't be publish privately.",
                    currentSocialDocument.toString());
            log.info(message);
        }
        return null;
    }

    protected DocumentModel publishSocialDocument(
            DocumentModel socialSectionToPublishIn) {
        DocumentModel proxyOfSocialDocument = null;
        try {
            DocumentModel docToPublish = getProxyOrCurrentDoc();
            if (docToPublish.isProxy()) {
                proxyOfSocialDocument = updateCurrentProxy(docToPublish);
                proxyOfSocialDocument = session.move(docToPublish.getRef(),
                        socialSectionToPublishIn.getRef(), null);
            } else {
                proxyOfSocialDocument = session.publishDocument(docToPublish,
                        socialSectionToPublishIn);
            }
            session.save();
        } catch (ClientException e) {
            if (log.isInfoEnabled()) {
                String message = String.format(
                        "The social document \"%s\" can't be publish in the socialSection \"%s\".",
                        currentSocialDocument.toString(),
                        socialSectionToPublishIn.toString());
                log.info(message);
            }
        }
        return proxyOfSocialDocument;
    }

    protected DocumentModel getProxyOrCurrentDoc() throws ClientException {
        DocumentModel proxyOrCurrentDoc = null;
        DocumentModelList curSoclDocProxy = session.getProxies(
                currentSocialDocument.getRef(), publicSocialSection.getRef());
        curSoclDocProxy.addAll(session.getProxies(
                currentSocialDocument.getRef(), privateSocialSection.getRef()));
        int nbrOfProxies = curSoclDocProxy.size();
        if (nbrOfProxies == MAXIMAL_NUMBER_OF_PROXY_PER_SOCIAL_DOCUMENT) {
            proxyOrCurrentDoc = curSoclDocProxy.get(FIRST_AND_ONLY_PROXY);
            currentProxy = proxyOrCurrentDoc;
        } else {
            if (nbrOfProxies > MAXIMAL_NUMBER_OF_PROXY_PER_SOCIAL_DOCUMENT
                    && log.isInfoEnabled()) {
                log.info("Too many proxies. Can't choose one. Proxy created from original document");
            }
            proxyOrCurrentDoc = currentSocialDocument;
        }
        return proxyOrCurrentDoc;
    }

    protected DocumentModel updateCurrentProxy(
            DocumentModel proxyOfSocialDocument) throws ClientException {
        DocumentRef socialSectionRef = proxyOfSocialDocument.getParentRef();
        DocumentModel currentSocialSectionOfPublication = session.getDocument(socialSectionRef);
        proxyOfSocialDocument = session.publishDocument(currentSocialDocument,
                currentSocialSectionOfPublication);
        return proxyOfSocialDocument;
    }

    /**
     * Used to create or update and move a proxy of the handled social document
     * in a public social section
     *
     * @return the proxy created or updated
     */
    public DocumentModel publishPubliclySocialDocument() {
        if (isPublicPublishable()) {
            return publishSocialDocument(publicSocialSection);
        }
        if (log.isInfoEnabled()) {
            String message = String.format(
                    "The social document \"%s\" can't be publish publicly.",
                    currentSocialDocument.toString());
            log.info(message);
        }

        return null;
    }

    public void unpublishSocialDocument() throws ClientException {
        String queryToGetProxy=String.format(
                "Select * from News where ecm:isProxy = 1 and ecm:currentLifeCycleState <> 'deleted' and ecm:name = '%s'",
                currentSocialDocument.getName());
        DocumentModelList newsProxies = session.query(queryToGetProxy);
        currentProxy = newsProxies.get(0);
        if (currentProxy != null) {
            session.removeDocument(currentProxy.getRef());
            currentProxy = null;
            if (log.isInfoEnabled()) {
                String message = String.format(
                        "The proxy of the social document \"%s\" has been remove.",
                        currentSocialDocument.toString());
                log.info(message);
            }
        } else {
            if (log.isInfoEnabled()) {
                String message = "There is no social document proxy to remove.";
                log.info(message);
            }

        }
    }

}
