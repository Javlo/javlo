package org.javlo.helper;

import junit.framework.TestCase;

public class XHTMLHelperTest extends TestCase {

	public void testContainsLink() throws Exception {
		assertTrue(XHTMLHelper.containsLink("go on : http://www.javlo.be."));
		assertTrue(XHTMLHelper.containsLink("go on : www.javlo.be."));
		assertFalse(XHTMLHelper.containsLink("sample test."));
	}
	
	public void testAutoLink() throws Exception {				
		assertEquals(XHTMLHelper.autoLink("this is a link : www.javlo.org."),"this is a link : <a class=\"auto-link web file-org\" href=\"http://www.javlo.org\">www.javlo.org</a>.");
		assertEquals(XHTMLHelper.autoLink("no link."),"no link.");
		assertEquals(XHTMLHelper.autoLink("this is a link : <a href=\"#\">www.javlo.org.</a>"),"this is a link : <a href=\"#\">www.javlo.org.</a>");
	}

}
