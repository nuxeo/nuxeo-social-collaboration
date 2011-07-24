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
package org.nuxeo.ecm.social.workspace.adapters;

import static org.nuxeo.ecm.social.workspace.SocialConstants.ARTICLE_TYPE;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper;

/**
 * This class gives method to make the document public or private into the
 * constrains of social workspace. The document must be part of a social
 * workspace and be a social document.
 * 
 * A document is public if it's published into the public section. Except for an
 * article render a social document visible only for the community is to publish
 * the document into the private section.
 * 
 * For an article this is just to have no proxy into the public section. The
 * source document himself will be the document exposed.
 * 
 * In the documentation about method of this class, when we reference to a
 * social document, we implicitly talking about the social document handle by
 * the instance of the class set into the contructor.
 * 
 * @author Benjamin JALON <bjalon@nuxeo.com>
 * 
 */
public class SocialDocumentAdapter {

    protected DocumentModel sourceDocument;

    protected DocumentModel socialWorkspace;

    protected DocumentModel privateSocialSection;

    protected DocumentModel publicSocialSection;

    private CoreSession session;

    /**
     * @param getSession() all documents modification will be made through this
     *            getSession().
     * @param sourceDocument on which social publications need to be performed
     */
    public SocialDocumentAdapter(DocumentModel sourceDocument)
            throws ClientException {

        this.sourceDocument = sourceDocument;

        if (sourceDocument != null && getSession() == null) {
            throw new ClientException(
                    "All action will be impossible as the given getSession() is null");
        }
        if (sourceDocument == null) {
            throw new ClientException("Give document model is null");
        }

        if (!SocialWorkspaceHelper.isSocialDocument(sourceDocument)) {
            throw new ClientException(
                    "Make public a document is restricted to social document only not for :"
                            + sourceDocument.getPathAsString());
        }

        socialWorkspace = SocialWorkspaceHelper.getSocialWorkspaceContainer(
                getSession(), sourceDocument.getRef());
        if (socialWorkspace == null) {
            throw new ClientException(
                    "Given document is not into a social workspace");
        }

    }

    protected DocumentModel getPrivateSection() throws ClientException {

        DocumentRef pathRef = SocialWorkspaceHelper.getPrivateSectionPath(socialWorkspace);

        if (privateSocialSection == null) {
            if (!getSession().exists(pathRef)) {
                throw new ClientException(
                        "Private section of the following social "
                                + "workspace has not been created, please check : "
                                + socialWorkspace.getPathAsString());
            }
            privateSocialSection = getSession().getDocument(pathRef);
        }
        return privateSocialSection;
    }

    protected DocumentModel getPublicSection() throws ClientException {

        DocumentRef pathRef = SocialWorkspaceHelper.getPublicSectionPath(socialWorkspace);

        if (publicSocialSection == null) {
            if (!getSession().exists(pathRef)) {
                throw new ClientException(
                        "Public section of the following social "
                                + "workspace has not been created, please check : "
                                + socialWorkspace.getPathAsString());
            }
            publicSocialSection = getSession().getDocument(pathRef);
        }
        return publicSocialSection;
    }

    public DocumentModel restrictToSocialWorkspaceMembers()
            throws ClientException {

        DocumentModel privateProxy = getPrivateProxy();
        if (privateProxy != null) {
            return privateProxy;
        }

        DocumentModel publicProxy = getPublicProxy();
        if (publicProxy != null) {
            if (!ARTICLE_TYPE.equals(sourceDocument.getType())) {
                DocumentModel exposedDocument = getSession().move(
                        publicProxy.getRef(), getPrivateSection().getRef(),
                        sourceDocument.getName());
                getSession().save();
                return exposedDocument;
            } else {
                getSession().removeDocument(publicProxy.getRef());
            }
        }

        if (ARTICLE_TYPE.equals(sourceDocument.getType())) {
            return sourceDocument;
        }

        DocumentModel exposedDocument = getSession().publishDocument(
                sourceDocument, getPrivateSection());
        getSession().save();
        return exposedDocument;

    }

    public DocumentModel makePublic() throws ClientException {

        DocumentModel publicProxy = getPublicProxy();
        if (publicProxy != null) {
            return publicProxy;
        }

        DocumentModel privateProxy = getPrivateProxy();
        if (privateProxy != null) {
            DocumentModel exposedDocument = getSession().move(
                    privateProxy.getRef(), getPublicSection().getRef(),
                    sourceDocument.getName());
            getSession().save();
            return exposedDocument;
        }

        DocumentModel exposedDocument = getSession().publishDocument(
                sourceDocument, getPublicSection());
        getSession().save();
        return exposedDocument;
    }

    protected DocumentModel getPublicProxy() throws ClientException {
        DocumentModelList proxies = getSession().getProxies(
                sourceDocument.getRef(), getPublicSection().getRef());

        validateDocumentVisibility(proxies, true);

        if (proxies.size() == 1) {
            return proxies.get(0);
        }

        return null;
    }

    protected DocumentModel getPrivateProxy() throws ClientException {
        DocumentModelList proxies = getSession().getProxies(
                sourceDocument.getRef(), getPrivateSection().getRef());

        validateDocumentVisibility(proxies, false);

        if (proxies.size() == 1) {
            return proxies.get(0);
        }

        return null;
    }

    protected void validateDocumentVisibility(DocumentModelList proxies,
            boolean isPublicProxies) throws ClientException {

        if (proxies.size() > 1) {
            String message = String.format("Too many published document :%s, please check."
                    + sourceDocument.getPathAsString());
            throw new ClientException(message);
        }

        if (!isPublicProxies && ARTICLE_TYPE.equals(sourceDocument.getType())
                && proxies.size() == 1) {
            String message = String.format("Article can't have a private proxy :%s, please check."
                    + sourceDocument.getPathAsString());
            throw new ClientException(message);
        }

    }

    /**
     * Return the public proxy of the source document if the source document is
     * public else return null.
     * 
     */
    public DocumentModel getDocumentPublic() throws ClientException {
        return getPublicProxy();
    }

    public boolean isPublic() throws ClientException {
        return getDocumentPublic() != null;
    }

    /**
     * If source document is not an Article return the private proxy of the
     * source document if the source document is private else return null
     * 
     * If is the source document is an Article return the source document if
     * private else return null.
     * 
     */
    public DocumentModel getDocumentRestrictedToMembers()
            throws ClientException {
        if (ARTICLE_TYPE.equals(sourceDocument.getType())) {
            if (!isPublic()) {
                return sourceDocument;
            } else {
                return null;
            }
        }

        return getPrivateProxy();
    }

    public boolean isRestrictedToMembers() throws ClientException {
        return getDocumentRestrictedToMembers() != null;
    }

    public boolean isDocumentInSocialWorkspace() {
        return socialWorkspace != null;
    }

    /**
     * This method will update the exposed document to the social workspace. If
     * the exposed document is not a proxy (private articles for instance) this
     * method will do nothing return the document. But if the document is a
     * proxy, it will be remove and recreate into the same section but will
     * point to the last version of the target document.
     */
    protected DocumentModel updateExposedDocument(
            DocumentModel exposedDocument, DocumentModel targetSection)
            throws ClientException {

        if (!exposedDocument.isProxy()) {
            return exposedDocument;
        }

        // TODO : make the id not changed
        session.removeDocument(exposedDocument.getRef());
        return session.publishDocument(sourceDocument, targetSection);
    }

    /**
     * @return core type name of source document.
     */
    public String getType() {
        return sourceDocument.getType();
    }

    protected CoreSession getSession() {
        if (session == null) {
            session = sourceDocument.getCoreSession();
        }
        return session;
    }

}
