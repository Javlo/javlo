package org.javlo.test.javlo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;

public class TestContentContext extends ContentContext {
	
	GlobalContext fakeGlobalContext; 
	
	String contentLang = "en";
	String lang = "en";

	public TestContentContext(ContentContext ctx) {
		super(ctx);
	}
	
	public static TestContentContext getContentContext(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return new TestContentContext(getContentContext(request, response, true));
	}
	
	@Override
	public GlobalContext getGlobalContext() {
		if (fakeGlobalContext == null) {
			fakeGlobalContext = new TestGlobalContext();
		}
		return fakeGlobalContext;
	}
	
	@Override
	public String getContentLanguage() {
		return contentLang;
	}
	
	@Override
	public void setContentLanguage(String lg) {
		contentLang = lg;
	}
	
	@Override
	public void setAllLanguage(String lg) {
		super.setAllLanguage(lg);
		contentLang = lg;
	}

}
