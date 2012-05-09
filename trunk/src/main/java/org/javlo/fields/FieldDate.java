package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;


public class FieldDate extends Field {
	
	@Override
	public boolean validate() {
		try {
			StringHelper.parseDate(getValue());
		} catch (ParseException e) {
			setMessage(e.getMessage());
			setMessageType(Field.MESSAGE_ERROR);
			return false;
		}
		return super.validate();
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		
		String displayStr = StringHelper.neverNull(getValue());
		if (displayStr.trim().length() == 0 || !isViewDisplayed()) {
			return "";
		}
		out.println("<p class=\"" + getType() + " " + getName() + "\">");
		out.println(XHTMLHelper.textToXHTML(displayStr));
		out.println("</p>");

		out.close();
		return writer.toString();
	}
	
	public String getType() {
		return "date";
	}

}

