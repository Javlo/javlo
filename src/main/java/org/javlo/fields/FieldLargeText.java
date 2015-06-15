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
		
		try {
			String refCode = referenceEditCode(ctx);
			if (refCode != null) {
				return refCode;
			}
		} catch (Exception e) { 
			e.printStackTrace();
		}
		
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		out.println("<div class=\"line " + getType() + "\">");
		out.println("<label for=\"" + getInputName() + "\">" + getLabel(new Locale(ctx.getContextRequestLanguage())) + " : </label>");
		String readOnlyHTML = "";
		if (isReadOnly()) {
			readOnlyHTML = " readonly=\"readonly\"";
		}
		out.print("<textarea class=\"form-control\"" + readOnlyHTML + " id=\"" + getInputName() + "\" name=\"" + getInputName() + "\" rows=\"10\" cols=\"20\">");
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
		String refCode = referenceViewCode(ctx);
		if (refCode != null) {
			return refCode;
		}
		return XHTMLHelper.textToXHTML( super.getDisplayValue(ctx, locale));
	}

	@Override
	public String getType() {
		return "large-text";
	}

}
