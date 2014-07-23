package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;


public class Heading extends Field {
	
	String tag = null;
	
	Heading (String inType) {
		tag = inType;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		
		String displayStr = StringHelper.neverNull(getValue());
		if (displayStr.trim().length() == 0 || !isViewDisplayed()) {
			return "";
		}

		out.println("<"+tag+" class=\"" + getType() + " " + getName() + "\">");
		out.println(XHTMLHelper.textToXHTML(StringHelper.neverNull(getValue())));
		out.println("</"+tag+">");

		out.close();
		return writer.toString();
	}
	
	public String getType() {
		return tag;
	}
	
	public boolean isTitle() {
		return tag.equalsIgnoreCase("h1");
	}

}

