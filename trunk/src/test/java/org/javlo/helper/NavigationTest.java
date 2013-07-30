package org.javlo.helper;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.test.servlet.FakeHttpContext;

public class NavigationTest extends TestCase {
	
	public void testGetPageIfExist() throws Exception {
		FakeHttpContext httpContext = FakeHttpContext.getInstance();
		HttpServletRequest request = httpContext.getRequest("http://demo.javlo.org/view/en/media.html?webaction=test");		
		ContentContext ctx = ContentContext.getContentContext(request, httpContext.getResponse());		
		GlobalContext globalContext = GlobalContext.getInstance(request);
		
		assertNull(globalContext.getPageIfExist(ctx, "/no_exist", true));
		assertNotNull(globalContext.getPageIfExist(ctx, "/page1", true));
		assertNotNull(globalContext.getPageIfExist(ctx, "/page2/media", true));
	}

}
