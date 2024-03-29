package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;

public class FieldEnormousText extends Field {

	@Override
	public String getEditXHTMLCode(ContentContext ctx, boolean search) {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);		

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getInputName() + "\">" + getLabel(ctx, ctx.getLocale()) + " : </label>");
		out.print("<textarea class=\"form-control\" rows=\"30\" id=\"" + getInputName() + "\" name=\"" + getInputName() + "\">");
		out.print(StringHelper.neverNull(getValue()));
		out.println("</textarea>");
		out.println("</div>");

		out.close();
		return writer.toString();
	}

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
	public String getType() {
		return "large-text";
	}
	
	@Override
	protected boolean isValueTranslatable() {
		return true;
	}
}
