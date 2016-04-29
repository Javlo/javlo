package org.javlo.helper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.javlo.helper.XMLManipulationHelper.BadXMLException;
import org.javlo.helper.XMLManipulationHelper.TagDescription;

import junit.framework.TestCase;

public class XMLManipulationHelperTest extends TestCase {

	public void testParser() throws BadXMLException {

		TagDescription[] tags = XMLManipulationHelper.searchAllTag("<p>test <b>bold</b> suffix</p>", true);

		assertEquals(tags.length, 2);
		assertEquals(tags[0].getName(), "p");
		assertEquals(tags[1].getName(), "b");

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		out.println("<html>");
		out.println("<head>");
		out.println("<meta charset=\"utf-8\" >");
		out.println("</head>");
		out.println("<body>");
		out.println("<ul>");
		out.println("<li><a href=\"link1.html\">click 1</a></li>");
		out.println("<li><a href=\"link2.html\">click 2</a></li>");
		out.println("</ul>");
		out.println("<div class=\"wrapped-content\">");
		out.println("<p>");
		out.println("We're not living in the post-PC era. Not by a long shot. As more of us work from home, or the plane, or the <a href=\"#\">coffee shop</a>, laptops might be more. Aside post format. Important than they've ever been. They've also <strong>become harder</strong> and harder to buy, as hardware specs have hit.");
		out.println("</p>");
		out.println("<hr>");
		out.println("<p class=\"read-more\"><a href=\"blog-single.html\">Read More</a></p>");
		out.println("</div>");
		out.println("</body>");
		out.println("</html>");

		out.close();
		tags = XMLManipulationHelper.searchAllTag(outStream.toString(), true);
		
		assertEquals(tags.length, 16);
		assertEquals(tags[2].getAttribute("charset", ""), "utf-8");
		assertEquals(tags[6].getAttribute("href", ""), "link1.html");
		assertEquals(tags[8].getInside(outStream.toString()), "click 2");
	}

}
