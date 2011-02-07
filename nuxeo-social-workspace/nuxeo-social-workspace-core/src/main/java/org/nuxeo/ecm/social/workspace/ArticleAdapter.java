package org.nuxeo.ecm.social.workspace;

import java.util.Calendar;

interface ArticleAdapter {


	String getTitle();
	void setTitle(String text);
	String getContent();
	void setContent(String text);
	String getAuthor();
	Calendar getCreated();
	String getFirstNCharacters(int n);
}
