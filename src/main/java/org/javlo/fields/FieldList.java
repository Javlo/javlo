package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public class FieldList extends Field {

	public String getListName() {
		return properties.getProperty("field." + getUnicName() + ".list");
	}

	@Override
	public String getDisplayValue(ContentContext ctx, Locale locale) throws Exception {		
		return "<span class=\"" + StringHelper.createFileName(getValue()) + "\">" + StringHelper.neverNull(getList(ctx, getListName(), locale).get(getValue()),"&nbsp;") + "</span>";
	}

	@Override
	public String getEditXHTMLCode(ContentContext ctx) throws Exception {
		if (getListName() == null) {
			return "list not found !";
		}

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		
		Collection<Map.Entry<String, String>> valuesCol = getList(ctx, getListName(), new Locale(ctx.getContextLanguage())).entrySet();
		Collection<Map.Entry<String, String>> values = valuesCol;
		if (values.size() == 0) {
			return "";
		}

		out.println("<div class=\"form-group\">");
		out.println(getEditLabelCode());
		out.println("<div class=\"row field-"+getName()+"\"><div class=\"col-sm-3\"><label for=\"" + getInputName() + "\">" + getLabel(new Locale(ctx.getContextRequestLanguage())) + " : </label></div>");
		out.println("<div class=\"col-sm-9\"><select class=\"form-control\" id=\"" + getInputName() + "\" name=\"" + getInputName() + "\" value=\"" + StringHelper.neverNull(getValue()) + "\">");
		

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
		out.println("</div></div></div>");

		out.close();
		return writer.toString();
	}

	@Override
	public String getType() {
		return "list-one";
	}

}
