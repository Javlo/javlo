package org.javlo.helper;

import java.net.MalformedURLException;

import junit.framework.TestCase;

public class URLHelperTest extends TestCase {
	
	public void testChangeMode() throws MalformedURLException {		
		assertEquals(URLHelper.changeMode("http://www.javlo.org/view/test.html", "edit"),"http://www.javlo.org/edit/test.html");
		assertEquals(URLHelper.changeMode("http://www.javlo.org/view/test.html", "view"),"http://www.javlo.org/view/test.html");
		assertEquals(URLHelper.changeMode("http://www.javlo.org/view/test.html", "preview"),"http://www.javlo.org/preview/test.html");
		assertEquals(URLHelper.changeMode("http://www.javlo.org/edit/test.html", "view"),"http://www.javlo.org/view/test.html");
		assertEquals(URLHelper.changeMode("http://www.javlo.org/edit-content/test.html", "view"),"http://www.javlo.org/view/test.html");
		assertEquals(URLHelper.changeMode("http://localhost:8080/test/edit-users/fr/root.html?j_token=lUnForoyuXklZYwYTdwhm1ZfMFJfn6I1135780933467136815128", "ajax"),"http://localhost:8080/test/ajax/fr/root.html?j_token=lUnForoyuXklZYwYTdwhm1ZfMFJfn6I1135780933467136815128");		
	}
	
	public void testAddParam() {
		assertTrue(URLHelper.addParam("http://www.javlo.org/test.html", "val", "test").equals("http://www.javlo.org/test.html?val=test"));
		assertTrue(URLHelper.addParam("http://www.javlo.org/test.html?user=admin", "val", "test").equals("http://www.javlo.org/test.html?user=admin&val=test"));
	}

}
