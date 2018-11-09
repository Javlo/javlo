package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public class FieldList extends Field {

	public String getListName() {
		return properties.getProperty("field." + getUnicName() + ".list");
	}

	@Override
	public String getDisplayValue(ContentContext ctx, Locale locale) throws Exception {		
		try {
			return StringHelper.neverNull(getList(ctx, getListName(), locale).get(getValue()));
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	@Override
	public String getEditXHTMLCode(ContentContext ctx, boolean search) throws Exception {
		
		String refCode = referenceEditCode(ctx);
		if (refCode != null) {
			return refCode;
		}
		
		if (getListName() == null) {
			return "list not found !";
		}

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		
		Collection<Map.Entry<String, String>> valuesCol = getList(ctx, getListName(), new Locale(ctx.getContextLanguage())).entrySet();
		Collection<Map.Entry<String, String>> values = valuesCol;
		if (values.size() == 0) {
			return "<div class=\"alert alert-danger\" role=\"alert\">Error on field '"+getName()+"' list empty or not found : "+getListName()+"</div>";
		}

		out.println("<div class=\"form-group\">");
		out.println(getEditLabelCode());
		out.println("<div class=\"row field-"+getName()+"\"><div class=\""+LABEL_CSS+"\"><label for=\"" + getInputName() + "\">" + getLabel(ctx, new Locale(ctx.getContextRequestLanguage())) + " : </label></div>");
		out.println("<div class=\""+VALUE_SIZE+"\"><select class=\"form-control\" id=\"" + getInputName() + "\" name=\"" + getInputName() + "\" value=\"" + StringHelper.neverNull(getValue()) + "\">");
		
		if (isFirstEmpty()) {
			out.println("		<option></option>");
		};
		
		List<Map.Entry<String,String>> datas = new LinkedList<>();
		datas.addAll(values);
		Collections.sort(datas, new Comparator<Map.Entry<String,String>>() {
			@Override
			public int compare(Entry<String, String> o1, Entry<String, String> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		
		for (Map.Entry<String, String> value : datas) {
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
	
	private boolean isFirstEmpty() {
		return StringHelper.isTrue(properties.getProperty("field." + getUnicName() + ".first-empty"));
	}

	@Override
	public boolean search(ContentContext ctx, String query) {
		if (query != null && query.contains(";")) {
			for (String subq : query.split(";")) {
				if (super.search(ctx, subq.trim())) {
					return true;
				}
			}
			return false;
		} else {
			return super.search(ctx, query);
		}
	}

	@Override
	public String getType() {
		return "list-one";
	}

}
