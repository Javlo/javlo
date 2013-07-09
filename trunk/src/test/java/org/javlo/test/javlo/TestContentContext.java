package org.javlo.test.javlo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.test.servlet.FakeHttpContext;

public class TestContentContext extends ContentContext {
	
	GlobalContext fakeGlobalContext; 

	public TestContentContext(ContentContext ctx) {
		super(ctx);
	}
	
	public static TestContentContext getContentContext(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return new TestContentContext(getContentContext(request, response, true));
	}
	
	public static void main(String[] args) {
		
		try {
			FakeHttpContext httpContext = FakeHttpContext.getInstance();
			HttpServletRequest request = httpContext.getRequest("http://demo.javlo.org/en/media.html?webaction=test");
			System.out.println("request = "+request);
			ContentContext ctx = ContentContext.getContentContext(request, httpContext.getResponse());
			MenuElement root = ContentService.getInstance(ctx.getGlobalContext()).getNavigation(ctx);
			System.out.println("***** TestContentContext.main : children = "+root.getChildMenuElements().size()); //TODO: remove debug trace
			System.out.println("***** path = "+ctx.getPath());
			System.out.println("***** url = "+URLHelper.createURL(ctx, "/test"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public GlobalContext getGlobalContext() {
		if (fakeGlobalContext == null) {
			fakeGlobalContext = new TestGlobalContext();
		}
		return fakeGlobalContext;
	}

}
