package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.service.RequestService;

public class FieldBoolean extends Field {

	@Override
	public String getType() {
		return "boolean";
	}

	@Override
	public String getEditXHTMLCode(ContentContext ctx) throws Exception {
		
		String refCode = referenceEditCode(ctx);
		if (refCode != null) {
			return refCode;
		}
		
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		out.println("<div class=\"line checkbox\">");
		out.println(getEditLabelCode());
		out.println("	<label>" + getLabel(new Locale(globalContext.getEditLanguage(ctx.getRequest().getSession()))) + " : ");
		String readOnlyHTML = "";
		String checkedHTML = "";
		if (isReadOnly()) {
			readOnlyHTML = " readonly=\"readonly\"";
		}
		if (StringHelper.isTrue(getValue())) {
			checkedHTML = " checked=\"checked\"";
		}
		out.println("	<input" + readOnlyHTML + " id=\"" + getInputName() + "\" name=\"" + getInputName() + "\" type=\"checkbox\" value=\"true\"" + checkedHTML + " />");
		if (getMessage() != null && getMessage().trim().length() > 0) {
			out.println("	<div class=\"message " + getMessageTypeCSSClass() + "\">" + getMessage() + "</div>");
		}
		out.println("</label></div>");

		out.close();
		return writer.toString();
	}

	@Override
	public boolean process(HttpServletRequest request) {
		boolean modify = super.process(request);
		if (!modify) {
			RequestService requestService = RequestService.getInstance(request);
			String value = requestService.getParameter(getInputName(), null);
			if (value == null) {
				setValue("" + false);
				if (!validate()) {
					setNeedRefresh(true);
				}
				modify = true;
			}
		}
		return modify;
	}

}
