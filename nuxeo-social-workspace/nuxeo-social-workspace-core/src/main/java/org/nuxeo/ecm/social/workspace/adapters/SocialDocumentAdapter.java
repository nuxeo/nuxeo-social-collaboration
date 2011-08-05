/*
 * (C) Copyright 2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 */
package org.nuxeo.ecm.social.workspace.adapters;

import static org.nuxeo.ecm.social.workspace.SocialConstants.ARTICLE_TYPE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_DOCUMENT_IS_PUBLIC_PROPERTY;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.social.workspace.SocialConstants;
import org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper;
import org.nuxeo.ecm.social.workspace.listeners.VisibilitySocialDocumentListener;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;
import org.nuxeo.runtime.api.Framework;

/**
 * Default implementation of {@link SocialDocument}.
 *
 * @author Benjamin JALON <bjalon@nuxeo.com>
 *
 */
public class SocialDocumentAdapter implements SocialDocument {

    protected DocumentModel sourceDocument;

    protected SocialWorkspace socialWorkspace;

    protected DocumentModel privateSocialSection;

    protected DocumentModel publicSocialSection;

    private CoreSession session;

    /**
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

        socialWorkspace = getSocialWorkspaceService().getDetachedSocialWorkspaceContainer(
                sourceDocument);
        if (socialWorkspace == null) {
            throw new ClientException(
                    "Given document is not into a social workspace");
        }
    }

    protected DocumentModel getPrivateSection() throws ClientException {
        DocumentRef pathRef = new PathRef(
                socialWorkspace.getPrivateSectionPath());
        if (privateSocialSection == null) {
            privateSocialSection = getSession().getDocument(pathRef);
        }
        return privateSocialSection;
    }

    protected DocumentModel getPublicSection() throws ClientException {
        DocumentRef pathRef = new PathRef(
                socialWorkspace.getPublicSectionPath());
        if (publicSocialSection == null) {
            publicSocialSection = getSession().getDocument(pathRef);
        }
        return publicSocialSection;
    }

    @Override
    public DocumentModel restrictToMembers() throws ClientException {
        setIsPublicField(false);

        DocumentModel privateProxy = getPrivateProxy();
        if (privateProxy != null) {
            return updateExposedDocument(privateProxy, false);
        }

        DocumentModel publicProxy = getPublicProxy();
        if (publicProxy != null) {
            if (ARTICLE_TYPE.equals(sourceDocument.getType())) {
                getSession().removeDocument(publicProxy.getRef());
            } else {
                return updateExposedDocument(publicProxy, false);
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

    @Override
    public DocumentModel makePublic() throws ClientException {
        setIsPublicField(true);

        DocumentModel publicProxy = getPublicProxy();
        if (publicProxy != null) {
            return updateExposedDocument(publicProxy, true);
        }

        DocumentModel privateProxy = getPrivateProxy();
        if (privateProxy != null) {
            return updateExposedDocument(privateProxy, true);
        }

        // private Article or new social document
        DocumentModel exposedDocument = getSession().publishDocument(
                sourceDocument, getPublicSection());
        getSession().save();
        return exposedDocument;
    }

    protected void setIsPublicField(boolean value) throws ClientException {
        sourceDocument.setPropertyValue(
                SocialConstants.SOCIAL_DOCUMENT_IS_PUBLIC_PROPERTY, value);
        sourceDocument.putContextData(
                VisibilitySocialDocumentListener.ALREADY_PROCESSED, true);
        sourceDocument = session.saveDocument(sourceDocument);
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
            String message = String.format("Too many published document: %s, please check."
                    + sourceDocument.getPathAsString());
            throw new ClientException(message);
        }

        if (!isPublicProxies && ARTICLE_TYPE.equals(sourceDocument.getType())
                && proxies.size() == 1) {
            String message = String.format("Article can't have a private proxy: %s, please check."
                    + sourceDocument.getPathAsString());
            throw new ClientException(message);
        }

    }

    @Override
    public DocumentModel getPublicDocument() throws ClientException {
        return getPublicProxy();
    }

    @Override
    public boolean isPublic() throws ClientException {
        return (Boolean) sourceDocument.getPropertyValue(SOCIAL_DOCUMENT_IS_PUBLIC_PROPERTY);
    }

    @Override
    public DocumentModel getRestrictedDocument() throws ClientException {
        if (ARTICLE_TYPE.equals(sourceDocument.getType())) {
            if (isPublic()) {
                return null;
            } else {
                return sourceDocument;
            }
        }

        return getPrivateProxy();
    }

    @Override
    public boolean isRestrictedToMembers() throws ClientException {
        return !((Boolean) sourceDocument.getPropertyValue(SOCIAL_DOCUMENT_IS_PUBLIC_PROPERTY));
    }

    @Override
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
            DocumentModel exposedDocument, boolean isPublic)
            throws ClientException {

        if (!exposedDocument.isProxy() && isPublic) {
            // => Article
            exposedDocument = getSession().publishDocument(sourceDocument,
                    getPublicProxy());
            getSession().save();
            return exposedDocument;
        }

        if (!exposedDocument.isProxy() && !isPublic) {
            return exposedDocument;
        }

        DocumentModel targetSection;
        if (isPublic) {
            targetSection = getPublicSection();
        } else {
            targetSection = getPrivateSection();
        }

        DocumentModel currentTarget = getSession().getDocument(
                exposedDocument.getParentRef());
        exposedDocument = getSession().publishDocument(sourceDocument,
                currentTarget, true);
        if (!currentTarget.getId().equals(targetSection.getId())) {
            exposedDocument = getSession().move(exposedDocument.getRef(),
                    targetSection.getRef(), exposedDocument.getName());
        }
        getSession().save();
        return exposedDocument;
    }

    @Override
    public String getType() {
        return sourceDocument.getType();
    }

    protected CoreSession getSession() {
        if (session == null) {
            session = sourceDocument.getCoreSession();
        }
        return session;
    }

    private static SocialWorkspaceService getSocialWorkspaceService() {
        try {
            return Framework.getService(SocialWorkspaceService.class);
        } catch (Exception e) {
            throw new ClientRuntimeException(e);
        }
    }

}
