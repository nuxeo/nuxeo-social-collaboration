/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     vpasquier <vpasquier@nuxeo.com>
 */
package org.nuxeo.ecm.social.workspace.gadgets.webengine;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.nuxeo.ecm.core.api.LifeCycleConstants.DELETE_TRANSITION;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.ADD_CHILDREN;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.REMOVE;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.REMOVE_CHILDREN;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_FACET;
import static org.nuxeo.ecm.social.workspace.SocialConstants.SOCIAL_WORKSPACE_IS_PUBLIC_PROPERTY;
import static org.nuxeo.ecm.social.workspace.helper.SocialWorkspaceHelper.toSocialDocument;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.i18n.I18NUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.operations.services.DocumentPageProviderOperation;
import org.nuxeo.ecm.automation.core.util.PaginableDocumentModelList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.TypeService;
import org.nuxeo.ecm.platform.comment.api.CommentableDocument;
import org.nuxeo.ecm.platform.comment.workflow.utils.CommentsConstants;
import org.nuxeo.ecm.platform.comment.workflow.utils.FollowTransitionUnrestricted;
import org.nuxeo.ecm.platform.query.nxql.NXQLQueryBuilder;
import org.nuxeo.ecm.platform.types.Type;
import org.nuxeo.ecm.platform.types.TypeManager;
import org.nuxeo.ecm.platform.types.TypeView;
import org.nuxeo.ecm.platform.types.adapter.TypeInfo;
import org.nuxeo.ecm.platform.ui.web.tag.fn.DocumentModelFunctions;
import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.platform.url.api.DocumentViewCodecManager;
import org.nuxeo.ecm.platform.web.common.vh.VirtualHostHelper;
import org.nuxeo.ecm.rating.api.LikeService;
import org.nuxeo.ecm.social.workspace.adapters.SocialDocument;
import org.nuxeo.ecm.user.center.profile.UserProfileService;
import org.nuxeo.ecm.webengine.forms.FormData;
import org.nuxeo.ecm.webengine.model.Template;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;
import org.nuxeo.runtime.api.Framework;

/**
 * WebEngine handler for gadgets requests.
 *
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 */
/**
 * @author rlegall
 *
 */
@Path("/social")
@WebObject(type = "social")
@Produces("text/html; charset=UTF-8")
public class SocialWebEngineRoot extends ModuleRoot {

    private static final Log log = LogFactory.getLog(SocialWebEngineRoot.class);

    static AutomationService automationService;

    protected static final String AVATAR_PROPERTY = "userprofile:avatar";

    @GET
    public Object index(@Context HttpServletRequest request) throws Exception {
        FormData formData = new FormData(request);

        String lang = formData.getString("lang");
        setLanguage(lang);
        setDocumentLinkBuilder(formData.getString("documentLinkBuilder"));

        // get the arguments
        String ref = formData.getString("docRef");
        String queryText = formData.getString("queryText");
        int pageSize = getIntFromString(formData.getString("limit"));
        int page = getIntFromString(formData.getString("page"));
        return buildDocumentList(ref, pageSize, page, queryText).arg(
                "renderFullHtml", true);
    }

    /**
     * Main method that return a html snipped with the list of documents
     * specified in request.
     * <p>
     * parameters:
     * <ul>
     * <li>docRef: parent document identifier (may be a path or an id)
     * <li>limit: number of documents per page</li>
     * <li>page: index of the current page</li>
     * <li>queryText (optional): search term</li>
     * </ul>
     */
    @POST
    @Path("documentList")
    public Object documentList(@Context HttpServletRequest request)
            throws Exception {
        FormData formData = new FormData(request);

        String lang = formData.getString("lang");
        setLanguage(lang);
        setDocumentLinkBuilder(formData.getString("documentLinkBuilder"));

        // get the arguments
        String ref = formData.getString("docRef");
        int pageSize = getIntFromString(formData.getString("limit"));
        int page = getIntFromString(formData.getString("page"));
        return buildDocumentList(ref, pageSize, page, null);
    }

    @GET
    @Path("documentListGet")
    public Object documentListGet(@Context HttpServletRequest request)
            throws Exception {
        FormData formData = new FormData(request);

        String lang = formData.getString("lang");
        setLanguage(lang);
        setDocumentLinkBuilder(formData.getString("documentLinkBuilder"));

        // get the arguments
        String ref = formData.getString("docRef");
        int pageSize = getIntFromString(formData.getString("limit"));
        int page = getIntFromString(formData.getString("page"));
        return buildDocumentList(ref, pageSize, page, null);
    }

    /**
     * @since 5.6
     */
    @POST
    @Path("search")
    public Object search(@Context HttpServletRequest request)
            throws Exception {
        FormData formData = new FormData(request);

        String lang = formData.getString("lang");
        setLanguage(lang);
        setDocumentLinkBuilder(formData.getString("documentLinkBuilder"));

        // get the arguments
        String ref = formData.getString("docRef");
        int pageSize = getIntFromString(formData.getString("limit"));
        int page = getIntFromString(formData.getString("page"));
        String queryText = formData.getString("queryText");
        return buildDocumentList(ref, pageSize, page, queryText);
    }

    Template buildDocumentList(String ref, int pageSize, int page,
            String queryText) throws Exception {
        DocumentRef docRef = getDocumentRef(ref);
        return buildDocumentList(docRef, pageSize, page, queryText);
    }

    Template buildDocumentList(DocumentRef docRef, int pageSize, int page,
            String queryText) throws Exception {
        boolean isSearch = queryText != null && queryText.trim().length() > 0;

        // build freemarker arguments map
        Map<String, Object> args = new HashMap<String, Object>();

        CoreSession session = ctx.getCoreSession();

        DocumentModel doc = session.getDocument(docRef);
        args.put("currentDoc", doc);

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
        args.put("removable", getDocsWithDeleteRight(docs));
        args.put(
                "isPublicSocialWorkspace",
                socialWorkspace.getPropertyValue(SOCIAL_WORKSPACE_IS_PUBLIC_PROPERTY));
        args.put("collaboration_views", getCollaborationViews(docs));

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
        SocialDocument socialDocument = toSocialDocument(target);

        if (socialDocument == null) {
            throw new ClientException("Can't fetch social document.");
        }

        boolean isPublic = "true".equals(formData.getString("public"));
        if (isPublic) {
            socialDocument.makePublic();
        } else {
            socialDocument.restrictToMembers();
        }
        return documentList(request);
    }

    /**
     * Remove the document specified in request
     */
    @POST
    @Path("deleteDocument")
    public Object deleteDocument(@Context HttpServletRequest request)
            throws Exception {
        FormData formData = new FormData(request);
        String target = formData.getString("targetRef");
        DocumentRef docRef = getDocumentRef(target);
        CoreSession session = ctx.getCoreSession();

        if (session.getAllowedStateTransitions(docRef).contains(
                DELETE_TRANSITION)) {
            session.followTransition(docRef, DELETE_TRANSITION);
        } else {
            session.removeDocument(docRef);
        }
        return documentList(request);
    }

    /**
     * Return the html form for creation of a document
     */
    @GET
    @Path("createDocumentForm")
    public Object createDocumentForm(@QueryParam("docRef") String ref,
            @QueryParam("doctype") String docTypeId,
            @QueryParam("lang") String lang) throws Exception {
        setLanguage(lang);
        DocumentRef docRef = getDocumentRef(ref);
        CoreSession session = ctx.getCoreSession();
        DocumentModel currentDoc = session.getDocument(docRef);

        DocumentType coreType = TypeService.getSchemaManager().getDocumentType(
                docTypeId);

        return getView("create_document_form").arg("currentDoc", currentDoc).arg(
                "docType", getTypeService().getType(docTypeId)).arg("coreType",
                coreType);
    }

    /**
     * return a form to select a doctype the user wants to create
     */
    @GET
    @Path("selectDocTypeToCreate")
    public Object selectDocTypeToCreate(@QueryParam("docRef") String ref,
            @QueryParam("lang") String lang) throws ClientException {
        setLanguage(lang);
        DocumentRef docRef = getDocumentRef(ref);
        CoreSession session = ctx.getCoreSession();
        DocumentModel currentDoc = session.getDocument(docRef);
        TypeManager typeService = getTypeService();
        Map<String, List<Type>> types = typeService.getTypeMapForDocumentType(
                currentDoc.getType(), currentDoc);
        filterAllowedTypes(types);

        return getView("select_doc_type").arg("currentDoc", currentDoc).arg(
                "docTypes", types).arg("categories", types.keySet()).arg(
                "lang", lang);
    }

    protected void filterAllowedTypes(Map<String, List<Type>> typesMap) {
        for (String key : typesMap.keySet()) {
            List<Type> toRemoved = new ArrayList<Type>();
            List<Type> types = typesMap.get(key);

            for (Type type : types) {
                if (!isAllowedLibraryType(type)) {
                    toRemoved.add(type);
                }
            }
            types.removeAll(toRemoved);
        }
    }

    protected boolean isAllowedLibraryType(Type type) {
        return !type.getId().equals("VEVENT");
    }

    protected static TypeManager getTypeService() throws ClientException {
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

    /**
     * Create the document given a post request where input item name is the
     * field name ("dc:title", ...) and "docRef" value is the target document
     * where to create the given document
     */
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

    protected static PaginableDocumentModelList getChildren(DocumentModel doc,
            int pageSize, int page) throws Exception {
        CoreSession session = doc.getCoreSession();

        OperationContext ctx = new OperationContext(session);
        OperationChain chain = new OperationChain("getChildren");

        String query = "SELECT * FROM Document "
                + "WHERE ecm:mixinType != 'HiddenInNavigation' "
                + "AND ecm:isCheckedInVersion = 0 "
                + "AND ecm:currentLifeCycleState != 'deleted'"
                + "AND ecm:parentId = '" + doc.getId() + "' "
                + "AND ecm:primaryType != 'VEVENT'"
                + "ORDER BY dc:title";

        chain.add(DocumentPageProviderOperation.ID).set("query", query).set(
                "page", page).set("pageSize", pageSize > 0 ? pageSize : 5);

        return (PaginableDocumentModelList) getAutomationService().run(ctx,
                chain);
    }

    protected static PaginableDocumentModelList search(DocumentModel doc,
            int pageSize, int page, String queryText) throws Exception {
        CoreSession session = doc.getCoreSession();

        OperationContext ctx = new OperationContext(session);
        OperationChain chain = new OperationChain("search");

        String escapedQueryText = NXQLQueryBuilder.prepareStringLiteral(
                queryText.trim(), false, true);

        String query = "SELECT * FROM Document "
                + "WHERE ecm:mixinType != 'HiddenInNavigation' "
                + "AND ecm:isCheckedInVersion = 0 " + "AND ecm:isProxy = 0 "
                + "AND ecm:currentLifeCycleState != 'deleted' "
                + "AND ecm:fulltext = '" + escapedQueryText + "%' "
                + "AND ecm:path STARTSWITH '" + doc.getPathAsString() + "' "
                + "ORDER BY dc:title";
        chain.add(DocumentPageProviderOperation.ID).set("query", query).set(
                "page", page).set("pageSize", pageSize > 0 ? pageSize : 5);

        return (PaginableDocumentModelList) getAutomationService().run(ctx,
                chain);
    }

    /**
     * Computes a list with the ancestors of the document specified within the
     * SocialWorkspace.
     */
    protected static List<DocumentModel> getAncestors(DocumentModel doc)
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

    protected static boolean isSocialWorkspace(DocumentModel doc) {
        return doc != null && doc.hasFacet(SOCIAL_WORKSPACE_FACET);
    }

    protected static DocumentRef getDocumentRef(String ref) {
        DocumentRef docRef;
        if (ref != null && ref.startsWith("/")) { // doc identified by absolute
            // path
            docRef = new PathRef(ref);
        } else { // // doc identified by id
            docRef = new IdRef(ref);
        }
        return docRef;
    }

    protected void setLanguage(String lang) {
        if (lang != null && lang.trim().length() > 0) {
            Locale locale = new Locale(lang);
            ctx.setLocale(locale);
        }
    }

    protected void setDocumentLinkBuilder(String documentLinkBuilder) {
        if (!StringUtils.isBlank(documentLinkBuilder)) {
            ctx.setProperty("documentLinkBuilder", documentLinkBuilder);
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

    protected static List<String> getPublishableDocs(
            DocumentModel socialWorkspace, DocumentModelList docs,
            boolean isPublic) throws ClientException {
        boolean isPublicSocialWorkspace = (Boolean) socialWorkspace.getPropertyValue(SOCIAL_WORKSPACE_IS_PUBLIC_PROPERTY);

        List<String> list = new ArrayList<String>();

        if (isPublicSocialWorkspace) {
            if (isPublic) { // is public publication
                for (DocumentModel doc : docs) {
                    SocialDocument socialDocument = toSocialDocument(doc);
                    if (socialDocument != null
                            && socialDocument.isRestrictedToMembers()) {
                        list.add(doc.getId());
                    }
                }
            } else { // is private publication
                for (DocumentModel doc : docs) {
                    SocialDocument socialDocument = toSocialDocument(doc);
                    if (socialDocument != null && socialDocument.isPublic()) {
                        list.add(doc.getId());
                    }
                }
            }
        }
        return list;
    }

    protected static List<String> getDocsWithDeleteRight(DocumentModelList docs)
            throws ClientException {
        List<String> docsIdResult = new ArrayList<String>();
        if (docs.isEmpty()) {
            return docsIdResult;
        }

        CoreSession session = docs.get(0).getCoreSession();
        for (DocumentModel doc : docs) {
            if (session.hasPermission(doc.getRef(), REMOVE)
                    && session.hasPermission(
                            session.getParentDocumentRef(doc.getRef()),
                            REMOVE_CHILDREN)) {
                docsIdResult.add(doc.getId());
            }
        }

        return docsIdResult;
    }

    protected static Map<String, String> getCollaborationViews(
            DocumentModelList docs) throws ClientException {
        Map<String, String> viewResults = new HashMap<String, String>();
        if (docs.isEmpty()) {
            return viewResults;
        }

        TypeManager typeManager;
        try {
            typeManager = Framework.getService(TypeManager.class);
        } catch (Exception e) {
            throw new ClientException(e.getMessage(), e);
        }

        for (DocumentModel doc : docs) {
            Type type = typeManager.getType(doc.getType());
            TypeView view = type.getView("collaboration");
            if (view != null) {
                viewResults.put(doc.getId(), view.getValue());
            } else {
                viewResults.put(doc.getId(), type.getDefaultView());
            }
        }

        return viewResults;
    }

    public String getTranslatedLabel(String label) {
        String newLabel = I18NUtils.getMessageString("messages", label, null,
                ctx.getLocale());
        if (newLabel == null) {
            return label;
        }
        return newLabel;
    }

    /**
     * Returns the main blob for the given {@code doc}, {@code null} if there is
     * no main file available.
     */
    public Blob getAttachment(DocumentModel doc) throws ClientException {
        BlobHolder bh = doc.getAdapter(BlobHolder.class);
        return bh != null ? bh.getBlob() : null;
    }

    public String escape(String text) {
        text = StringEscapeUtils.escapeJavaScript(text);
        text = StringEscapeUtils.escapeHtml(text);
        text = StringEscapeUtils.escapeXml(text);
        return text;
    }

    public String escapePath(String text) {
        // text = text.replaceAll("\"", Matcher.quoteReplacement("\\\\\""));
        text = text.replaceAll("'", Matcher.quoteReplacement("\\\'"));
        try {
            text = URIUtil.encodePath(text);
        } catch (Exception e) {
            log.debug("failed to encode:" + text, e);
        }
        return text;
    }

    /**
     * Indicates if the current user has the right to Add Children to the
     * current Document
     *
     * @param docId the reference of the document
     * @return true if the current user has the right to Add Children to the
     *         current Document and false otherwise
     */
    public boolean hasAddChildrenRight(String docId) {
        try {
            IdRef docRef = new IdRef(docId);
            boolean hasPermission = ctx.getCoreSession().hasPermission(docRef,
                    ADD_CHILDREN);
            return hasPermission;
        } catch (Exception e) {
            return false;
        }
    }

    public List<DocumentModel> getComments(DocumentModel doc)
            throws ClientException {
        // Load document comments if exist
        List<DocumentModel> comments = null;
        CommentableDocument commentableDoc = doc.getAdapter(CommentableDocument.class);
        if (commentableDoc != null) {
            comments = commentableDoc.getComments();
        }
        return comments;
    }

    @GET
    @Path("documentCommentList")
    public Object documentCommentList(@QueryParam("docRef") String ref)
            throws Exception {
        // build freemarker arguments map
        Map<String, Object> args = new HashMap<String, Object>();
        CoreSession session = ctx.getCoreSession();
        IdRef docRef = new IdRef(ref);
        DocumentModel doc = session.getDocument(docRef);
        args.put("doc", doc);
        return Response.ok(getView("document_comments_template").args(args)).header(
                "docRef", ref).build();
    }

    public List<DocumentModel> getCommentChildren(DocumentModel doc,
            DocumentModel parent) throws ClientException {
        // Load all comment children of the document doc
        List<DocumentModel> comments = null;
        CommentableDocument commentableDoc = doc.getAdapter(CommentableDocument.class);
        if (commentableDoc != null) {
            comments = commentableDoc.getComments(parent);
        }
        return comments;
    }

    /**
     * Add comment to the related document and parent comment
     */
    @POST
    @Path("addComment")
    public Object addComment() throws ClientException {
        try {
            HttpServletRequest request = ctx.getRequest();
            CoreSession session = ctx.getCoreSession();
            // Create pending comment
            DocumentModel myComment = session.createDocumentModel("Comment");
            // Set comment properties
            myComment.setProperty("comment", "author",
                    ctx.getPrincipal().getName());
            myComment.setProperty("comment", "text",
                    request.getParameter("commentContent"));
            myComment.setProperty("comment", "creationDate",
                    Calendar.getInstance());
            // Retrieve document to comment
            String docToCommentRef = request.getParameter("docToCommentRef");
            DocumentModel docToComment = session.getDocument(new IdRef(
                    docToCommentRef));
            String commentParentRef = request.getParameter("commentParentRef");
            // Create comment
            CommentableDocument commentableDoc = null;
            if (docToComment != null) {
                commentableDoc = docToComment.getAdapter(CommentableDocument.class);
            }
            DocumentModel newComment;
            if (commentParentRef != null) {
                // if exists retrieve comment parent
                DocumentModel commentParent = session.getDocument(new IdRef(
                        commentParentRef));
                newComment = commentableDoc.addComment(commentParent, myComment);
            } else {
                newComment = commentableDoc.addComment(myComment);
            }
            // automatically validate the comments
            if (CommentsConstants.COMMENT_LIFECYCLE.equals(newComment.getLifeCyclePolicy())) {
                new FollowTransitionUnrestricted(ctx.getCoreSession(),
                        newComment.getRef(),
                        CommentsConstants.TRANSITION_TO_PUBLISHED_STATE).runUnrestricted();
            }
            // Return the new comment view
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("doc", docToComment);
            args.put("comment", newComment);
            return Response.ok(getView("bricks/document_comments").args(args)).header(
                    "docRef", docToCommentRef).header("parentCommentRef",
                    commentParentRef).build();
        } catch (Throwable t) {
            log.error("failed to add comment", t);
            throw ClientException.wrap(t);
        }
    }

    /**
     * Like/Unlike the related document
     */
    @POST
    @Path("docLike")
    public Object docLike(@FormParam("docRef") String docRef) throws Exception {
        // Get document
        CoreSession session = ctx.getCoreSession();
        DocumentModel docToLike = session.getDocument(new IdRef(docRef));
        // Get Like Services
        LikeService likeService = Framework.getLocalService(LikeService.class);
        // Get user name
        String userName = ctx.getPrincipal().getName();
        if (likeService.hasUserLiked(userName, docToLike)) {
            likeService.dislike(userName, docToLike);
        } else {
            likeService.like(userName, docToLike);
        }
        return Response.ok(
                getView("bricks/document_like").arg("doc", docToLike)).header(
                "docRef", docRef).build();
    }

    public boolean hasUserLiked(DocumentModel doc) {
        LikeService likeService = Framework.getLocalService(LikeService.class);
        String userName = ctx.getPrincipal().getName();
        return likeService.hasUserLiked(userName, doc);
    }

    public long getLikesCount(DocumentModel doc) {
        LikeService likeService = Framework.getLocalService(LikeService.class);
        return likeService.getLikesCount(doc);
    }

    /**
     * Get the related user avatar to display in the UI comment
     */
    public String getAvatarURL(String commentUser) throws ClientException {
        String url = VirtualHostHelper.getContextPathProperty()
                + "/icons/missing_avatar.png";
        UserProfileService userProfileService = Framework.getLocalService(UserProfileService.class);
        DocumentModel userProfileDoc = userProfileService.getUserProfileDocument(
                commentUser, ctx.getCoreSession());
        if (userProfileDoc == null) {
            return url;
        }

        if (userProfileDoc.getPropertyValue(AVATAR_PROPERTY) != null) {
            url = VirtualHostHelper.getContextPathProperty()
                    + "/"
                    + DocumentModelFunctions.fileUrl("downloadFile",
                            userProfileDoc, AVATAR_PROPERTY, "avatar");
        }
        return url;
    }

    /**
     * Computes and returns the URL for the given {@code doc}, using the
     * document link builder sets in the
     * {@link org.nuxeo.ecm.webengine.model.WebContext}.
     */
    public String computeDocumentURL(DocumentModel doc) {
        DocumentLocation docLoc = new DocumentLocationImpl(
                doc.getRepositoryName(), new IdRef(doc.getId()));
        DocumentView docView = new DocumentViewImpl(docLoc, doc.getAdapter(
                TypeInfo.class).getDefaultView());
        DocumentViewCodecManager documentViewCodecManager = Framework.getLocalService(DocumentViewCodecManager.class);
        String documentLinkBuilder = (String) ctx.getProperty("documentLinkBuilder");
        String codecName = isBlank(documentLinkBuilder) ? documentViewCodecManager.getDefaultCodecName()
                : documentLinkBuilder;
        String baseURL = VirtualHostHelper.getBaseURL(ctx.getRequest());
        return documentViewCodecManager.getUrlFromDocumentView(codecName,
                docView, true, baseURL);
    }
}
