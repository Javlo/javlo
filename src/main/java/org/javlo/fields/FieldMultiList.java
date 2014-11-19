package org.javlo.fields;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
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
		return getValue();
	}

	@Override
	public String getEditXHTMLCode(ContentContext ctx) throws Exception {
		if (getListName() == null) {
			return "list not found !";
		}

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		out.println("<div class=\"line\">");
		out.println("<label class=\"control-label\">" + getLabel(new Locale(globalContext.getEditLanguage(ctx.getRequest().getSession()))) + " : </label>");
		out.println(getEditLabelCode());
		if (ctx.isAsEditMode()) {
			out.println("<div class=\"checkbox-inline\">");
		} else {
			out.println("<div class=\"checkbox\">");
		}
		// out.println("	<select multiple=\"multiple\" id=\"" + getInputName() +
		// "\" name=\"" + getInputName() + "\" value=\"" +
		// StringHelper.neverNull(getValue()) + "\">");
		Collection<Map.Entry<String, String>> valuesCol = getList(ctx, getListName(), new Locale(ctx.getContextLanguage())).entrySet();
		Collection<Map.Entry<String, String>> values = valuesCol;

		for (Map.Entry<String, String> value : values) {
			String checked = "";
			if (getValue() != null) {
				if (getValue().contains(value.getKey())) {
					checked = " checked=\"checked\"";
				}
			}
			String key = StringHelper.neverNull(value.getKey(), value.getValue());
			String label = StringHelper.neverEmpty(value.getValue(), i18nAccess.getViewText("global.none", "?"));
			out.println("<label><input type=\"checkbox\" name=\"" + getInputName() + "\" id=\"cb-" + key + "\" value=\"" + key + "\"" + checked + "/>" + label + "</label>");
		}

		// out.println("	</select>");
		if (getMessage() != null && getMessage().trim().length() > 0) {
			out.println("	<div class=\"message " + getMessageTypeCSSClass() + "\">" + getMessage() + "</div>");
		}
		out.println("</div></div>");

		out.close();
		return writer.toString();
	}

	@Override
	public String getType() {
		return "list-multi";
	}

	/**
	 * process the field
	 * 
	 * @param request
	 * @return true if the field is modified.
	 */
	@Override
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

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		String value = getValue();
		out.println("<ul>");
		for (String item : value.split(";")) {
			out.println("<li>" + getList(ctx, getListName(), new Locale(ctx.getRequestContentLanguage())).get(item) + "</li>");
		}
		out.println("</ul>");
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public List<String> getValues(ContentContext ctx, Locale locale) throws Exception {
		List<String> out = new LinkedList<String>();
		String val = getValue();
		if (val != null) {
			 Map<String, String> list = getList(ctx, getListName(), locale);
			for (String item : val.split(";")) {
				out.add(list.get(item));
			}
		}		
		return out;
	}

}
