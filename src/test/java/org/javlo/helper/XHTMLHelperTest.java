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
	
	public void testRemoveComment() throws Exception {
		assertEquals(XHTMLHelper.removeComment("border: 2px; /* cmt */"), "border: 2px; ");
		assertEquals(XHTMLHelper.removeComment("/* cmt */border: 2px; "), "border: 2px; ");
		assertEquals(XHTMLHelper.removeComment("border: 2px;"), "border: 2px;");
	}
	
	public void testReplaceOutTag() throws Exception {		
		assertEquals(XHTMLHelper.replaceOutTag("I love my cat", "cat", "wife"), "I love my wife");
		assertEquals(XHTMLHelper.replaceOutTag("<p class=\"test\">test</p>", "test", "replace"), "<p class=\"test\">replace</p>");
		assertEquals(XHTMLHelper.replaceOutTag("<p class=\"test\">table</p>", "test", "replace"), "<p class=\"test\">table</p>");		
	}
	
	public void testToHTML() {
		assertEquals(XHTMLHelper.textToXHTML("line1\\nline2"), "line1<br />line2");
		assertEquals(XHTMLHelper.textToXHTMLWidthParagraph("line1\\nline2"), "<p>line1</p><p>line2</p>");
	}

}
