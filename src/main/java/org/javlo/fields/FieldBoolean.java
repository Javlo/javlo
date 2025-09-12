package org.javlo.fields;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.service.RequestService;

import java.io.PrintWriter;
import java.io.StringWriter;

public class FieldBoolean extends Field {

	public static String TYPE = "boolean";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getEditXHTMLCode(ContentContext ctx, boolean search) throws Exception {
		
		String refCode = referenceEditCode(ctx);
		if (refCode != null) {
			return refCode;
		}		
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println("<div class=\"form-check\"><div class=\"checkbox\">");
		out.println(getEditLabelCode());
		out.println("<label class=\"form-check-label\">");
		String readOnlyHTML = "";
		String checkedHTML = "";
		if (isReadOnly()) {
			readOnlyHTML = " readonly=\"readonly\"";
		}
		if (StringHelper.isTrue(getValue())) {
			checkedHTML = " checked=\"checked\"";
		}
		
		String label=null;;
		if (search) {
			label = getSearchLabel(ctx, ctx.getLocale());
		}
		if (StringHelper.isEmpty(label)) {
			label = getLabel(ctx, ctx.getLocale());
		}

		
		out.print("<input" + readOnlyHTML + " id=\"" + getInputName() + "\" name=\"" + getInputName() + "\" type=\"checkbox\" value=\"true\"" + checkedHTML + " class=\"form-check-input\" />");
		out.println(label);
		if (getMessage() != null && getMessage().trim().length() > 0) {
			out.println("	<div class=\"message " + getMessageTypeCSSClass() + "\">" + getMessage() + "</div>");
		}
		out.println("</label></div></div>");
		out.close();
		return writer.toString();
	}

	@Override
	public boolean process(ContentContext ctx) {
		boolean modify = super.process(ctx);
		if (!modify) {
			RequestService requestService = RequestService.getInstance(ctx.getRequest());
			String value = requestService.getParameter(getInputName(), null);
			if (value == null) {
				setValue(ctx, "" + false);
				if (!validate()) {
					setNeedRefresh(true);
				}
				modify = true;
			}
		}
		return modify;
	}
	

}
