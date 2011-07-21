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
package org.nuxeo.ecm.social.workspace.gadgets.webengine;

import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_DOCUMENT_FACET;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.international.LocaleSelector;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.operations.services.DocumentPageProviderOperation;
import org.nuxeo.ecm.automation.core.util.PaginableDocumentModelList;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.platform.types.Type;
import org.nuxeo.ecm.platform.types.TypeManager;
import org.nuxeo.ecm.social.workspace.helper.SocialDocumentPublicationHandler;
import org.nuxeo.ecm.social.workspace.helper.SocialDocumentStatusInfoHandler;
import org.nuxeo.ecm.webengine.forms.FormData;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;
import org.nuxeo.runtime.api.Framework;

/**
 * WebEngine handler for library gadget requests.
 * 
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 */
@Path("/social")
@WebObject(type = "social")
@Produces("text/html; charset=UTF-8")
public class SocialWebEngineRoot extends ModuleRoot {

    private static final Log log = LogFactory.getLog(SocialWebEngineRoot.class);

    static AutomationService automationService;

    /**
     * Main method that return a html snipped with the list of documents
     * specified in request.
     * <p>
     * parameters:
     * <ul>
     * <li>docRef: parent document identifier (may be a path or an id)
     * <li>pageSize: number of documents per page</li>
     * <li>page: index of the current page</li>
     * <li>queryText (optional): search term</li>
     * </ul>
     */
    @POST
    @Path("documentList")
    public Object documentList(@Context HttpServletRequest request)
            throws Exception {
        FormData formData = new FormData(request);
        setLanguage();

        // get the arguments
        String ref = formData.getString("docRef");
        String queryText = formData.getString("queryText");
        int pageSize = getIntFromString(formData.getString("pageSize"));
        int page = getIntFromString(formData.getString("page"));
        return buildDocumentList(ref, pageSize, page, queryText);
    }

    Object buildDocumentList(String ref, int pageSize, int page,
            String queryText) throws Exception {
        DocumentRef docRef = getDocumentRef(ref);
        return buildDocumentList(docRef, pageSize, page, queryText);
    }

    Object buildDocumentList(DocumentRef docRef, int pageSize, int page,
            String queryText) throws Exception {
        boolean isSearch = queryText != null && queryText.trim().length() > 0;

        // build freemarker arguments map
        Map<String, Object> args = new HashMap<String, Object>();

        CoreSession session = ctx.getCoreSession();

        DocumentModel doc = session.getDocument(docRef);
        args.put("currentDoc", doc);
        // FIXME: typo (documet -> document)
        args.put("documetListPath", getPath() + "/documentList");

        PaginableDocumentModelList docs;
        DocumentModel socialWorkspace = null;
        if (isSearch) {
            docs = search(doc, pageSize, page, queryText);
            socialWorkspace = doc;
            args.put("queryText", queryText);
        } else {
            docs = getChildren(doc, pageSize, page);
            List<DocumentModel> ancestors = getAncestors(doc);

            DocumentModel parent = null;
            if (ancestors.size() > 1) {
                parent = ancestors.get(ancestors.size() - 2);
            }
            args.put("parent", parent);

            if (!ancestors.isEmpty() && isSocialWorkspace(ancestors.get(0))) {
                socialWorkspace = ancestors.remove(0);
            }
            args.put("ancestors", ancestors);

            args.put("queryText", "");
        }

        args.put("socialWorkspace", socialWorkspace);
        args.put("docs", docs);

        args.put("publishablePublic",
                getPublishableDocs(socialWorkspace, docs, true));
        args.put("publishablePrivate",
                getPublishableDocs(socialWorkspace, docs, false));

        // add navigation arguments
        args.put("page", docs.getCurrentPageIndex());
        args.put("maxPage", docs.getNumberOfPages());
        args.put("nextPage", page < docs.getNumberOfPages() - 1 ? page + 1
                : page);
        args.put("prevPage", page > 0 ? page - 1 : page);

        if (isSearch) {
            return getView("search_result").args(args);
        } else {
            return getView("children_list").args(args);
        }
    }

    @POST
    @Path("publishDocument")
    public Object publishDocument(@Context HttpServletRequest request)
            throws Exception {
        FormData formData = new FormData(request);
        CoreSession session = ctx.getCoreSession();
        DocumentRef docRef = getDocumentRef(formData.getString("targetRef"));

        DocumentModel target = session.getDocument(docRef);
        SocialDocumentPublicationHandler handler = new SocialDocumentPublicationHandler(
                session, target);

        boolean isPublic = "true".equals(formData.getString("public"));
        if (isPublic) {
            handler.publishPubliclySocialDocument();
        } else {
            handler.publishPrivatelySocialDocument();
        }
        return documentList(request);
    }

    /**
     * remove the document specified in request
     */
    @POST
    @Path("deleteDocument")
    public Object deleteDocument(@Context HttpServletRequest request)
            throws Exception {
        FormData formData = new FormData(request);
        String target = formData.getString("targetRef");
        DocumentRef docRef = getDocumentRef(target);
        CoreSession session = ctx.getCoreSession();
        session.removeDocument(docRef);
        return documentList(request);
    }

    /**
     * return the html form for creation of a folder
     * @throws Exception 
     */
    @GET
    @Path("createDocumentForm")
    public Object createDocumentForm(@QueryParam("docRef") String ref,
            @QueryParam("doctype") String docTypeId) throws Exception {
        DocumentRef docRef = getDocumentRef(ref);
        CoreSession session = ctx.getCoreSession();
        DocumentModel currentDoc = session.getDocument(docRef);

        return getView("create_document_form").arg("currentDoc", currentDoc).arg(
                "docType", getTypeService().getType(docTypeId));
    }

    /**
     * return the html form that proposes a the type of document the user wants
     * to create.
     */
    @GET
    @Path("selectDocTypeToCreate")
    public Object selectDocTypeToCreate(@QueryParam("docRef") String ref)
            throws ClientException {
        DocumentRef docRef = getDocumentRef(ref);
        CoreSession session = ctx.getCoreSession();
        DocumentModel currentDoc = session.getDocument(docRef);
        TypeManager typeService = getTypeService();
        Map<String, List<Type>> types = typeService.getTypeMapForDocumentType(
                currentDoc.getType(), currentDoc);

        return getView("select_doc_type").arg("currentDoc", currentDoc).arg(
                "docTypes", types).arg("categories", types.keySet());
    }

    protected TypeManager getTypeService() throws ClientException {
        TypeManager typeService;
        try {
            typeService = Framework.getService(TypeManager.class);
        } catch (Exception e) {
            throw new ClientException(e.getMessage(), e);
        }
        if (typeService == null) {
            throw new ClientException(
                    "Can't fetch the typeService, please contact your administrator");
        }
        return typeService;
    }

    @POST
    @Path("createDocument")
    public Object createDocument(@Context HttpServletRequest request)
            throws Exception {
        CoreSession session = ctx.getCoreSession();
        FormData formData = new FormData(request);
        String type = formData.getDocumentType();
        String title = formData.getDocumentTitle();
        DocumentRef docRef = getDocumentRef(formData.getString("docRef"));
        DocumentModel parent = session.getDocument(docRef);
        DocumentModel newDoc = session.createDocumentModel(
                parent.getPathAsString(), title, type);
        formData.fillDocument(newDoc);
        newDoc = session.createDocument(newDoc);
        session.save();
        if (newDoc.isFolder()) {
            return buildDocumentList(newDoc.getId(), 0, 0, null);
        } else {
            return buildDocumentList(parent.getId(), 0, 0, null);
        }
    }

    protected PaginableDocumentModelList getChildren(DocumentModel doc,
            int pageSize, int page) throws Exception {
        CoreSession session = doc.getCoreSession();

        OperationContext ctx = new OperationContext(session);
        OperationChain chain = new OperationChain("getChildren");

        String query = "SELECT * FROM Document "
                + "WHERE ecm:mixinType != 'HiddenInNavigation' "
                + "AND ecm:isCheckedInVersion = 0 "
                + "AND ecm:currentLifeCycleState != 'deleted'"
                + "AND ecm:parentId = '" + doc.getId() + "'";

        chain.add(DocumentPageProviderOperation.ID).set("query", query).set(
                "pageSize", pageSize).set("page", page);

        return (PaginableDocumentModelList) getAutomationService().run(ctx,
                chain);
    }

    protected PaginableDocumentModelList search(DocumentModel doc,
            int pageSize, int page, String queryText) throws Exception {
        CoreSession session = doc.getCoreSession();

        OperationContext ctx = new OperationContext(session);
        OperationChain chain = new OperationChain("search");

        String query = "SELECT * FROM Document "
                + "WHERE ecm:mixinType != 'HiddenInNavigation' "
                + "AND ecm:isCheckedInVersion = 0 "
                + "AND ecm:currentLifeCycleState != 'deleted'"
                + "AND ecm:fulltext = '" + queryText + "'"
                + "AND ecm:path STARTSWITH '" + doc.getPathAsString() + "'";
        chain.add(DocumentPageProviderOperation.ID).set("query", query).set(
                "pageSize", pageSize).set("page", page);

        return (PaginableDocumentModelList) getAutomationService().run(ctx,
                chain);
    }

    /**
     * Computes a list with the ancestors of the document specified within the
     * SocialWorkspace.
     */
    protected List<DocumentModel> getAncestors(DocumentModel doc)
            throws ClientException {
        List<DocumentModel> list = new ArrayList<DocumentModel>();
        CoreSession session = doc.getCoreSession();
        list.add(doc);
        while (doc != null && !isSocialWorkspace(doc)) {
            doc = session.getParentDocument(doc.getRef());
            list.add(0, doc);
        }
        return list;
    }

    protected boolean isSocialWorkspace(DocumentModel doc) {
        if (doc == null) {
            return false;
        }
        return "SocialWorkspace".equals(doc.getType());
    }

    protected DocumentRef getDocumentRef(String ref) {
        DocumentRef docRef;
        if (ref != null && ref.startsWith("/")) { // doc identified by absolute
                                                  // path
            docRef = new PathRef(ref);
        } else { // // doc identified by id
            docRef = new IdRef(ref);
        }
        return docRef;
    }

    protected void setLanguage() {
        try {
            Locale locale = LocaleSelector.instance().getLocale();
            ctx.setLocale(locale);
        } catch (Exception e) {
            log.debug("failed to set language in web context", e);
        }
    }

    protected static int getIntFromString(String value) {
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // nothing to do => return 0
            }
        }
        return 0;
    }

    private static AutomationService getAutomationService() throws Exception {
        if (automationService == null) {
            automationService = Framework.getService(AutomationService.class);
        }
        return automationService;
    }

    // better way to do this ?
    private static List<String> getPublishableDocs(
            DocumentModel socialWorkspace, DocumentModelList docs,
            boolean isPublic) throws ClientException {
        CoreSession session = socialWorkspace.getCoreSession();
        boolean isPublicSocialWorkspace = (Boolean) socialWorkspace.getPropertyValue("social:isPublic");

        List<String> list = new ArrayList<String>();

        if (isPublicSocialWorkspace) {
            if (isPublic) { // is public publication
                for (DocumentModel doc : docs) {
                    SocialDocumentStatusInfoHandler handler = new SocialDocumentStatusInfoHandler(
                            session, doc);
                    if (handler.isPrivate()
                            && doc.hasFacet(SOCIAL_DOCUMENT_FACET)) {
                        list.add(doc.getId());
                    }
                }
            } else { // is private publication
                for (DocumentModel doc : docs) {
                    SocialDocumentStatusInfoHandler handler = new SocialDocumentStatusInfoHandler(
                            session, doc);
                    if (handler.isPublic()
                            && doc.hasFacet(SOCIAL_DOCUMENT_FACET)) {
                        list.add(doc.getId());
                    }
                }
            }
        }
        return list;
    }

}
