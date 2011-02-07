package org.nuxeo.ecm.social.workspace;

import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_DC_AUTHOR;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_DC_CREATED;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_DC_TITLE;
import static org.nuxeo.ecm.social.workspace.SocialConstants.FIELD_NOTE_NOTE;

import java.util.Calendar;

import org.nuxeo.ecm.core.api.DocumentModel;

public class ArticleAdapterImpl implements ArticleAdapter{


	DocumentModel doc;

	public ArticleAdapterImpl(DocumentModel doc) {
		this.doc = doc;
	}

	@Override
	public String getAuthor() {
		return (String) SocialWorkspaceHelper.getDocProperty(doc,FIELD_DC_AUTHOR);
	}

	@Override
	public Calendar getCreated() {
		return (Calendar) SocialWorkspaceHelper.getDocProperty(doc,FIELD_DC_CREATED);
	}

	@Override
	public String getTitle() {
		return (String) SocialWorkspaceHelper.getDocProperty(doc,FIELD_DC_TITLE);
	}


	@Override
	public String getContent() {
		return (String) SocialWorkspaceHelper.getDocProperty(doc,FIELD_NOTE_NOTE);
	}

	@Override
	public String getFirstNCharacters(int n) {
		String s = getContent();
		if (s != null ){
			int length = s.length();
			s.substring(0, n<length?n:length);
		}
		return "";
	}

	@Override
	public void setTitle(String text) {
		SocialWorkspaceHelper.setDocProperty(doc, FIELD_DC_TITLE, text);
	}

	@Override
	public void setContent(String text) {
		SocialWorkspaceHelper.setDocProperty(doc, FIELD_NOTE_NOTE, text);
	}

}
