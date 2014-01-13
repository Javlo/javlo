package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;

public class FieldLargeText extends Field {

	@Override
	public String getEditXHTMLCode(ContentContext ctx) {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		out.println("<div class=\"line " + getType() + "\">");
		out.println("<label for=\"" + getInputName() + "\">" + getLabel(new Locale(globalContext.getEditLanguage(ctx.getRequest().getSession()))) + " : </label>");
		String readOnlyHTML = "";
		if (isReadOnly()) {
			readOnlyHTML = " readonly=\"readonly\"";
		}
		out.print("<textarea" + readOnlyHTML + " id=\"" + getInputName() + "\" name=\"" + getInputName() + "\">");
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
	 */
	@Override
	public String getDisplayValue(ContentContext ctx, Locale locale) throws Exception {
		return XHTMLHelper.textToXHTML( super.getDisplayValue(ctx, locale));
	}

	@Override
	public String getType() {
		return "large-text";
	}

}
