package org.javlo.helper;

import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.javlo.context.ContentContext;
import org.javlo.test.servlet.FakeHttpContext;

public class URLHelperTest extends TestCase {
	
	public void testChangeMode() throws MalformedURLException {		
		assertEquals(URLHelper.changeMode("http://www.javlo.org/view/test.html", "edit"),"http://www.javlo.org/edit/test.html");
		assertEquals(URLHelper.changeMode("http://www.javlo.org/view/test.html", "view"),"http://www.javlo.org/view/test.html");
		assertEquals(URLHelper.changeMode("http://www.javlo.org/view/test.html", "preview"),"http://www.javlo.org/preview/test.html");
		assertEquals(URLHelper.changeMode("http://www.javlo.org/edit/test.html", "view"),"http://www.javlo.org/view/test.html");
		assertEquals(URLHelper.changeMode("http://www.javlo.org/edit-content/test.html", "view"),"http://www.javlo.org/view/test.html");
		// local module test
		assertEquals(URLHelper.changeMode("http://localhost:8080/test/edit-users/fr/root.html?j_token=lUnForoyuXklZYwYTdwhm1ZfMFJfn6I1135780933467136815128", "ajax"),"http://localhost:8080/test/ajax/fr/root.html?j_token=lUnForoyuXklZYwYTdwhm1ZfMFJfn6I1135780933467136815128");		
	}
	
	public void testAddParam() {
		assertTrue(URLHelper.addParam("http://www.javlo.org/test.html", "val", "test").equals("http://www.javlo.org/test.html?val=test"));
		assertTrue(URLHelper.addParam("http://www.javlo.org/test.html?user=admin", "val", "test").equals("http://www.javlo.org/test.html?user=admin&val=test"));
	}
	
	public void testCreateURL() throws Exception {
		FakeHttpContext httpContext = FakeHttpContext.getInstance();
		HttpServletRequest request = httpContext.getRequest("http://demo.javlo.org/view/en/media.html?webaction=test");		
		ContentContext ctx = ContentContext.getContentContext(request, httpContext.getResponse());
		
		assertEquals(URLHelper.createURL(ctx, "/page"), "/en/page.html");
		assertEquals(URLHelper.createURL(ctx, "/page/subpage"), "/en/page/subpage.html");
		ctx.setAllLangauge("fr");		
		assertEquals(URLHelper.createURL(ctx, "/page"), "/fr/page.html");
		ctx.setLanguage("en");
		assertEquals(URLHelper.createURL(ctx, "/page"), "/en-fr/page.html");
	}

}
