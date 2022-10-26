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
	
	public void testReplaceLink() throws Exception {
		assertEquals(XHTMLHelper.replaceLinks(null, "<a href=\"test\">test</a>"), "<a href=\"[TEST]-url:test\">test</a>");
		assertEquals(XHTMLHelper.replaceLinks(null, "<script>var a=\"<a href=\\\"mailto:test@test.com\\\">test<\\/a>\";</script><a href=\\\"mailto:test@test.com\\\">test</a>"), "<script>var a=\"<a href=\"mailto:test@test.com\">test</a>\";</script><a href=\"[TEST]-url:test\">test</a>");
		assertEquals(XHTMLHelper.replaceLinks(null, "<script src=\"http://cdn.com/script.js\"></script><script>var a=\"<a href='mailto:test@test.com'>test</a>\";</script><a href=\"test\">test</a>"), "<script src=\"http://cdn.com/script.js\"></script><script>var a=\"<a href=\"mailto:test@test.com\">test</a>\";</script><a href=\"[TEST]-url:test\">test</a>");
		assertEquals(XHTMLHelper.replaceLinks(null, "<a href=\"page:test\">test</a>"), "<a href=\"[TEST]-page:test\">test</a>");
		assertEquals(XHTMLHelper.replaceLinks(null, "<a href=\"page:test\">test</a><a href=\"mailto:info@javlo.org\">mail</a>"), "<a href=\"[TEST]-page:test\">test</a><a href=\"mailto:info@javlo.org\">mail</a>");
		assertEquals(XHTMLHelper.replaceLinks(null, "<img src=\"test.jpg\">"), "<img src=\"[TEST]-src:test.jpg\" />");
	}
	
//	public void testToHTML() {
//		System.out.println("XHTMLHelper.textToXHTML(\"line1\\\\nline2\") = "+XHTMLHelper.textToXHTML("line1\\nline2"));
//		assertEquals(XHTMLHelper.textToXHTML("line1\\nline2"), "line1<br />line2");
//		assertEquals(XHTMLHelper.textToXHTMLWidthParagraph("line1\\nline2"), "<p>line1</p><p>line2</p>");
//	}

}
