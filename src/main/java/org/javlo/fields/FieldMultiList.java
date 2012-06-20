package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.service.RequestService;

public class FieldMultiList extends Field {

	public String getListName() {
		return properties.getProperty("field." + getUnicName() + ".list");
	}

	@Override
	public String getDisplayValue(ContentContext ctx, Locale locale) throws Exception {
		return getList(ctx, getListName(), locale).get(getValue());
	}

	public String getEditXHTMLCode(ContentContext ctx) throws Exception {
		if (getListName() == null) {
			return "list not found !";
		}

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		out.println("<div class=\"line\">");
		out.println(getEditLabelCode());
		out.println("	<label for=\"" + getInputName() + "\">" + getLabel(new Locale(globalContext.getEditLanguage())) + " : </label>");
		out.println("	<select multiple=\"multiple\" id=\"" + getInputName() + "\" name=\"" + getInputName() + "\" value=\"" + StringHelper.neverNull(getValue()) + "\">");
		Collection<Map.Entry<String, String>> valuesCol = getList(ctx, getListName(), new Locale(ctx.getContextLanguage())).entrySet();
		Collection<Map.Entry<String, String>> values = valuesCol;

		for (Map.Entry<String, String> value : values) {
			String selected = "";
			if (getValue() != null) {
				if (getValue().contains(value.getKey())) {
					selected = " selected=\"selected\"";
				}
			}
			if (value.getKey() != null) {
				out.println("		<option value=\"" + value.getKey() + "\"" + selected + ">" + value.getValue() + "</option>");
			} else {
				out.println("		<option" + selected + ">" + value.getValue() + "</option>");
			}
		}

		out.println("	</select>");
		if (getMessage() != null && getMessage().trim().length() > 0) {
			out.println("	<div class=\"message " + getMessageTypeCSSClass() + "\">" + getMessage() + "</div>");
		}
		out.println("</div>");

		out.close();
		return writer.toString();
	}

	public String getType() {
		return "list-multi";
	}

	/**
	 * process the field
	 * 
	 * @param request
	 * @return true if the field is modified.
	 */
	public boolean process(HttpServletRequest request) {
		RequestService requestService = RequestService.getInstance(request);
		String[] values = requestService.getParameterValues(getInputName(), null);

		boolean modify = false;
		if (values != null) {
			String finalValue = "";
			String sep = "";
			for (String value : values) {
				finalValue = finalValue + sep + value;
				sep = ";";
			}
			if (!finalValue.equals(getValue())) {
				setValue(finalValue);
				if (!validate()) {
					setNeedRefresh(true);
				}
				modify = true;
			}
		}
		return modify;
	}

}
