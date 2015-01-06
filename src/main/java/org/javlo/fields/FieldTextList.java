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
	 * @throws Exception 
	 */
	@Override
	public String getDisplayValue(ContentContext ctx, Locale locale) throws Exception {
		return XHTMLHelper.textToXHTML(super.getDisplayValue(ctx, locale));
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		if (getValue() == null || getValue().trim().length() == 0) {
			return "";
		}

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		out.println("<div class=\"" + getType() + " " + getName() + "\">");
		out.println(StringHelper.textToList(null, StringHelper.neverNull(getValue()), getMetaData("sep"), null, true, getCSSClass()));
		out.println("</div>");

		out.close();		
		return writer.toString();
	}

	@Override
	public String getType() {
		return "list";
	}

}
