package org.nuxeo.ecm.social.workspace;

import static org.nuxeo.ecm.social.workspace.SocialConstants.ARTICLE_SCHEMA;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.adapter.DocumentAdapterFactory;

public class AdapterFactory implements DocumentAdapterFactory{

	@Override
	public Object getAdapter(DocumentModel doc, Class<?> itf) {
		if (itf == ArticleAdapter.class &&  doc.hasSchema(ARTICLE_SCHEMA)){
			return new ArticleAdapterImpl(doc);
		}
		return null;
	}

}
