package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public class FieldNumber extends Field {

	@Override
	public String getType() {
		return "number";
	}
	
	public int getMin(ContentContext ctx) {
		return Integer.parseInt(properties.getProperty("field." + getUnicName() + ".min", ""+Integer.MIN_VALUE));
	}

	public int getMax(ContentContext ctx) {
		return Integer.parseInt(properties.getProperty("field." + getUnicName() + ".max", ""+Integer.MAX_VALUE));
	}

	public String getEditXHTMLCode(ContentContext ctx) throws Exception {
		String refCode = referenceEditCode(ctx);
		if (refCode != null) {
			return refCode;
		}

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		out.println("<div class=\"line form-group\">");
		out.println(getEditLabelCode());

		out.println("	<label for=\"" + getInputName() + "\">" + getLabel(new Locale(ctx.getContextRequestLanguage())) + " : </label>");
		String readOnlyHTML = "";
		if (isReadOnly()) {
			readOnlyHTML = " readonly=\"readonly\"";
		}
		out.println("	<input type=\"number\" min=\""+getMin(ctx)+"\" max=\""+getMax(ctx)+"\"" + readOnlyHTML + " id=\"" + getInputName() + "\" class=\"form-control\" name=\"" + getInputName() + "\" value=\"" + StringHelper.neverNull(getValue()) + "\"/>");
		if (getMessage() != null && getMessage().trim().length() > 0) {
			out.println("	<div class=\"message " + getMessageTypeCSSClass() + "\">" + getMessage() + "</div>");
		}
		out.println("</div>");

		out.close();
		return writer.toString();
	}
	
	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		int min = getMin(ctx);
		if (min<0) {
			min=0;
		}
		setValue(""+min);
		return true;
	}

}
