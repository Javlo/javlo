package org.javlo.fields;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.JavaHelper;
import org.javlo.helper.StringHelper;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class OpenList extends Field {

	public String getListName() {
		return properties.getProperty("field." + getUnicName() + ".list", getUnicName());
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

	public Map<String, String> getList(ContentContext ctx) throws Exception {
		Map<String, String> outList = new HashMap<String, String>();
		ContentService content = ContentService.getInstance(ctx.getRequest());
		for (IContentVisualComponent component : content.getComponentByType(ctx, comp.getType())) {
			if (component instanceof DynamicComponent) {
				DynamicComponent dynComp = (DynamicComponent) component;
				if (dynComp.getField(ctx, getName()) != null) {
					String val = dynComp.getField(ctx, getName()).getValue();
					if (val != null) {
						outList.put(val, val);
					}
				}
			}
		}
		return outList;
	}

	public String getInputNewName() {
		if (getCurrentLocale() != null) {
			return getName() + "-new-" + getId() + '-' + getCurrentLocale().getLanguage();
		} else {
			return getName() + "-new-" + getId();
		}
	}
	
	@Override
	public String getSearchEditXHTMLCode(ContentContext ctx) throws Exception {
		return getEditXHTMLCode(ctx, true);
	}
	
	
	@Override
	public String getEditXHTMLCode(ContentContext ctx, boolean search) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		Map<String,String> valuesMap = getList(ctx);
		if (ctx.isVisualMode()) {
			valuesMap.put("", "");
		}		
		List<Map.Entry<String, String>> values = new LinkedList(valuesMap.entrySet());
		Collections.sort(values, new JavaHelper.MapEntriesSortOnValue());

		out.println("<div class=\"form-group field-"+getName()+"\">");
		out.println(getEditLabelCode());
		out.println("<div class=\"row\"><div class=\""+LABEL_CSS+"\"><label for=\"" + getInputName() + "\">" + getLabel(ctx, ctx.getLocale()) + " : </label></div>");
		out.println("<div class=\""+(ctx.isVisualMode()?VALUE_SIZE:SMALL_VALUE_SIZE)+"\"><select class=\"form-control\" id=\"" + getInputName() + "\" name=\"" + getInputName() + "\" value=\"" + StringHelper.neverNull(getValue()) + "\">");
 
		if (search) {
			out.println("		<option></option>");
		}

		Set<String> displayed = new HashSet<>();
		displayed.add("");
		for (Map.Entry<String, String> value : values) {
			String selected = "";
			if (getValue() != null) {
				if (getValue().equals(value.getKey())) {
					selected = " selected=\"selected\"";
				}
				if (!displayed.contains(value.getValue().trim())) {
					displayed.add(value.getValue().trim());
					if (value.getKey() != null) {
						out.println("		<option value=\"" + value.getKey() + "\"" + selected + ">" + value.getValue() + "</option>");
					} else {
						out.println("		<option" + selected + ">" + value.getValue() + "</option>");
					}
				}
			}

		}

		out.println("	</select>");
		if (getMessage() != null && getMessage().trim().length() > 0) {
			out.println("	<div class=\"message " + getMessageTypeCSSClass() + "\">" + getMessage() + "</div>");
		}
		out.println("</div>");
		if (!ctx.isVisualMode()) {
			out.println("<div class=\"col-sm-2\"><input class=\"form-control\" id=\"" + getInputNewName() + "\" name=\"" + getInputNewName() + "\" type=\"text\" placeholder=\""+i18nAccess.getText("global.create")+"...\" /></div>");
			out.println("<input class=\"hidden\" type=\"submit\" />");
		}
		out.println("</div></div>");

		out.close();
		return writer.toString();
	}

	@Override
	public String getType() {
		return "open-list";
	}

	/**
	 * process the field
	 * 
	 * @param request
	 * @return true if the field is modified.
	 */
	public boolean process(ContentContext ctx) {
		
		super.process(ctx);
		
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		boolean modify = false;

		String value = requestService.getParameter(getInputNewName(), "");
		if (value.trim().length() > 0) {
			modify = true;
			setValue(value);
			setNeedRefresh(true);
		}

		return modify;
	}

}
