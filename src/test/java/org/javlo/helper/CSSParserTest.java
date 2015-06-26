package org.javlo.helper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

public class CSSParserTest extends TestCase {

	public CSSParserTest(String name) {
		super(name);		
	}
	
	public void testCSSInliner() throws Exception {
		 	ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			out.println("<html>");
			out.println("<style>");
			out.println(".red {color: red;}");
			out.println("body .green {color: green;}");
			out.println("p.blue {color: blue;}");
			out.println("</style>");
			out.println("<body>");
			out.println("<p class=\"red\">test</p>");
			out.println("<p class=\"green\">test</p>");
			out.println("<p class=\"blue\">test</p>");
			out.println("</body>");
			out.println("</html>");
			out.close();
			String html =  new String(outStream.toByteArray());
			String inlineHtmlMerge = CSSParser.mergeCSS(html, false);
			assertTrue(inlineHtmlMerge.contains("<style>"));
			assertTrue(inlineHtmlMerge.contains("style="));
			inlineHtmlMerge = CSSParser.mergeCSS(html, true);
			assertTrue(!inlineHtmlMerge.contains("<style>"));
			assertTrue(inlineHtmlMerge.contains("style="));
	  }

}
