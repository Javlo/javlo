package org.javlo.helper;

import jakarta.servlet.http.HttpServletRequest;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.test.servlet.FakeHttpContext;

import junit.framework.TestCase;

public class NavigationTest extends TestCase {
	
	public void testGetPageIfExist() throws Exception {
		FakeHttpContext httpContext = new FakeHttpContext("http://demo.javlo.org/view/en/media.html?webaction=test");
		HttpServletRequest request = httpContext.getRequest();		
		ContentContext ctx = ContentContext.getContentContext(request, httpContext.getResponse());		
		GlobalContext globalContext = GlobalContext.getInstance(request);
		
		assertNull(globalContext.getPageIfExist(ctx, "/no_exist", true));
		assertNotNull(globalContext.getPageIfExist(ctx, "/page1", true));
		assertNotNull(globalContext.getPageIfExist(ctx, "/page2/media", true));
	}

}
