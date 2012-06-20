package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;

public class FieldList extends Field {

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
		out.println("	<select id=\"" + getInputName() + "\" name=\"" + getInputName() + "\" value=\"" + StringHelper.neverNull(getValue()) + "\">");
		Collection<Map.Entry<String, String>> valuesCol = getList(ctx, getListName(), new Locale(ctx.getContextLanguage())).entrySet();
		//Collection<Map.Entry<String, String>> values = new TreeSet(new JavaHelper.MapEntriesSortOnValue());
		Collection<Map.Entry<String, String>> values = valuesCol;
		//values.addAll(valuesCol);
		
		for (Map.Entry<String, String> value : values) {
			String selected = "";
			if (getValue() != null) {
				if (getValue().equals(value.getKey())) {
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
		return "list-one";
	}

}
