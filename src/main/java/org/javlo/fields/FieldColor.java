package org.javlo.fields;

import org.javlo.context.ContentContext;
import org.javlo.css.CssColor;
import org.javlo.helper.StringHelper;
import org.owasp.encoder.Encode;

import java.io.PrintWriter;
import java.io.StringWriter;

public class FieldColor extends Field {

	public static final String TYPE = "color";

	public String getType() {
		return TYPE;
	}

	public String getEditXHTMLCode(ContentContext ctx, boolean search) throws Exception {
		String refCode = referenceEditCode(ctx);
		if (refCode != null) {
			return refCode;
		}
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println("<div class=\"row field-" + getName() + "\"><div class=\"" + LABEL_CSS + "\">");
		out.println(getEditLabelCode());
		String label = null;
		;
		if (search) {
			label = getSearchLabel(ctx, ctx.getLocale());
		}
		if (StringHelper.isEmpty(label)) {
			label = getLabel(ctx, ctx.getLocale());
		}
		out.println("	<label for=\"" + getInputName() + "\">" + label + " : </label>");
		String readOnlyHTML = "";
		if (isReadOnly()) {
			readOnlyHTML = " readonly=\"readonly\"";
		}
		String value = Encode.forHtmlAttribute(StringHelper.neverNull(getValue()));
		out.println("</div><div class=\"" + VALUE_SIZE + "\"><div class=\"d-flex justify-content-start\"><input" + readOnlyHTML + " id=\"" + getInputName() + "\" class=\"color form-control" + getSpecialClass() + "\" name=\"" + getInputName() + "\" value=\"" + value + "\"/>");		
		if (ctx.getGlobalContext().getTemplateData().getColorList() != null && ctx.getGlobalContext().getTemplateData().getColorList().length > 0) {
			for (CssColor c : ctx.getGlobalContext().getTemplateData().getColorList()) {
				if (c != null) {
					String js = "document.getElementById('"+getInputName()+"').style.backgroundColor='"+c+"'; document.getElementById('"+getInputName()+"').value='"+c+"'; return false;";
					out.println("	<button type=\"button\" class=\"btn-color\" style=\"background-color: "+c+"\" onclick=\""+js+"\">&nbsp;</button>");
				}
			}
		}
		if (getMessage() != null && getMessage().trim().length() > 0) {
			out.println("	<div class=\"message " + getMessageTypeCSSClass() + "\">" + getMessage() + "</div>");
		}
		out.println("</div></div></div>");
		out.close();
		return writer.toString();
	}

}
