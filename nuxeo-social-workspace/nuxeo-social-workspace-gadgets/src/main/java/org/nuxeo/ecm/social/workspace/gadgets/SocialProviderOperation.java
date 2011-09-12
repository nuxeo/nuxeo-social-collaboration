/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 *
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     eugen
 */
package org.nuxeo.ecm.social.workspace.gadgets;

import static org.nuxeo.ecm.platform.query.nxql.NXQLQueryBuilder.prepareStringLiteral;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.operations.services.PaginableDocumentModelListImpl;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.SortInfo;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderService;
import org.nuxeo.ecm.platform.query.core.CoreQueryPageProviderDescriptor;
import org.nuxeo.ecm.platform.query.nxql.CoreQueryDocumentPageProvider;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;
import org.nuxeo.ecm.social.workspace.service.SocialWorkspaceService;

/**
 * @author <a href="mailto:ei@nuxeo.com">Eugen Ionica</a>
 *
 */

@Operation(id = SocialProviderOperation.ID, category = Constants.CAT_FETCH, label = "Social Provider", description = "Social Provider")
public class SocialProviderOperation {

    public static final String ID = "Social.Provider";

    private static final Log log = LogFactory.getLog(SocialProviderOperation.class);

    public static final String CURRENT_USERID_PATTERN = "$currentUser";

    public static final String CURRENT_REPO_PATTERN = "$currentRepository";

    @Context
    protected CoreSession session;

    @Context
    protected PageProviderService pps;

    @Context
    protected SocialWorkspaceService socialWorkspaceService;

    @Param(name = "providerName", required = false)
    protected String providerName;

    @Param(name = "query", required = true)
    protected String query;

    @Param(name = "language", required = false, widget = Constants.W_OPTION, values = { "NXQL" })
    protected String lang = "NXQL";

    @Param(name = "page", required = false)
    protected Integer page = 0;

    @Param(name = "limit", required = false)
    protected Integer pageSize;

    @Param(name = "sortInfo", required = false)
    protected StringList sortInfoAsStringList;

    @Param(name = "contextPath", required = true)
    protected String contextPath;

    @Param(name = "onlyPublicDocuments", required = false)
    protected String onlyPublicDocuments;

    @Param(name = "queryParams", required = false)
    protected StringList strParameters;

    protected static final DocumentModelList EMPTY_LIST = new DocumentModelListImpl();

    @SuppressWarnings("unchecked")
    @OperationMethod
    public DocumentModelList run() throws Exception {
        String principal = session.getPrincipal().getName();

        List<SortInfo> sortInfos = manageSortParameter();

        Object[] parameters = null;

        if (strParameters != null && !strParameters.isEmpty()) {
            parameters = strParameters.toArray(new String[strParameters.size()]);
            // expand specific parameters
            for (int idx = 0; idx < parameters.length; idx++) {
                String value = (String) parameters[idx];
                if (value.equals(CURRENT_USERID_PATTERN)) {
                    parameters[idx] = principal;
                } else if (value.equals(CURRENT_REPO_PATTERN)) {
                    parameters[idx] = session.getRepositoryName();
                }
            }
        }

        Map<String, Serializable> props = new HashMap<String, Serializable>();
        props.put(CoreQueryDocumentPageProvider.CORE_SESSION_PROPERTY,
                (Serializable) session);

        Long targetPageSize = null;
        if (pageSize != null) {
            targetPageSize = new Long(pageSize);
        }

        if (StringUtils.isBlank(contextPath)) {
            return EMPTY_LIST;
        }

        SocialWorkspace socialWorkspace = socialWorkspaceService.getDetachedSocialWorkspaceContainer(
                session, new PathRef(contextPath));

        if (socialWorkspace != null) {
            String finalPath = socialWorkspace.getDocument().getPathAsString();

            if (onlyPublicDocuments != null
                    && Boolean.parseBoolean(onlyPublicDocuments)) {
                finalPath = socialWorkspace.getPublicSectionPath();
            }

            String s = " ecm:path STARTSWITH "
                    + prepareStringLiteral(finalPath, true, true);

            if (query != null) {
                if (query.toUpperCase().contains("WHERE")) {
                    query += " AND " + s;
                } else {
                    query += " WHERE " + s;
                }
            }
        }

        CoreQueryPageProviderDescriptor desc = new CoreQueryPageProviderDescriptor();
        desc.setPattern(query);
        return new PaginableDocumentModelListImpl(
                (PageProvider<DocumentModel>) pps.getPageProvider(providerName,
                        desc, sortInfos, targetPageSize, new Long(page), props,
                        parameters));

    }

    protected List<SortInfo> manageSortParameter() {
        List<SortInfo> sortInfos = new ArrayList<SortInfo>();
        if (sortInfoAsStringList != null) {
            String sortParameterSeparator = " ";
            for (String sortInfoDesc : sortInfoAsStringList) {
                SortInfo sortInfo;
                if (sortInfoDesc.contains(sortParameterSeparator)) {
                    String[] parts = sortInfoDesc.split(sortParameterSeparator);
                    sortInfo = new SortInfo(parts[0],
                            Boolean.parseBoolean(parts[1]));
                } else {
                    sortInfo = new SortInfo(sortInfoDesc, true);
                }
                sortInfos.add(sortInfo);
            }
        }
        return sortInfos;
    }

}
