package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;

public class FieldTextList extends FieldLargeText {

	/**
	 * return the value "displayable"
	 * 
	 * @param locale
	 * @return
	 */
	@Override
	public String getDisplayValue(ContentContext ctx, Locale locale) {
		return XHTMLHelper.textToXHTML(getValue());
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		if (getValue() == null || getValue().trim().length() == 0) {
			return "";
		}

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		out.println("<p class=\"" + getType() + " " + getName() + "\">");
		out.println(StringHelper.textToList(StringHelper.neverNull(getValue()), getMetaData("sep"), null, true, null));
		out.println("</p>");

		out.close();
		return writer.toString();
	}

	@Override
	public String getType() {
		return "list";
	}

}
