package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.JavaHelper;
import org.javlo.helper.StringHelper;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

public class OpenMultiList extends Field {

	public String getListName() {
		return properties.getProperty("field." + getUnicName() + ".list");
	}

	@Override
	public String getDisplayValue(ContentContext ctx, Locale locale) throws Exception {
		return "<span class=\"" + StringHelper.createFileName(getValue()) + "\">" + StringHelper.neverNull(getList(ctx, getListName(), locale).get(getValue()), "&nbsp;") + "</span>";
	}

	public Map<String, String> getList(ContentContext ctx) throws Exception {
		Map<String, String> outList = new HashMap<String, String>();
		ContentService content = ContentService.getInstance(ctx.getRequest());
		for (IContentVisualComponent component : content.getComponentByType(ctx, comp.getType())) {
			if (component instanceof DynamicComponent) {
				DynamicComponent dynComp = (DynamicComponent) component;
				if (dynComp.getField(ctx, getName()) != null) {
					Collection<String> val = dynComp.getField(ctx, getName()).getValues();
					if (val != null) {
						for (String v : val) {
							outList.put(v, v);
						}
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

	public String getInputName(String field) {
		if (getCurrentLocale() != null) {
			return getName() + "-F_" + field + "-" + getId() + '-' + getCurrentLocale().getLanguage();
		} else {
			return getName() + "-F_" + field + "-" + getId();
		}
	}

	@Override
	public String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		Map<String,String> valuesMap = getList(ctx);
		if (ctx.isVisualMode()) {
			valuesMap.put("", "");
		}

		List<Map.Entry<String, String>> values = new LinkedList(valuesMap.entrySet());
		Collections.sort(values, new JavaHelper.MapEntriesSortOnValue());
		out.println("<div class=\"form-group\">");
		out.println(getEditLabelCode());
		out.println("<div class=\"row\"><div class=\"col-sm-3\"><label for=\"" + getInputName() + "\">" + getLabel(new Locale(ctx.getContextRequestLanguage())) + " : </label></div>");
		if (!ctx.isVisualMode()) {
			out.println("<div class=\"col-sm-7\">");
			for (Map.Entry<String, String> value : values) {
				String selected = "";
				if (getValue() != null) {
					if (getValue().equals(value.getKey())) {
						selected = " checked=\"checked\"";
					}
				}
				if (value.getKey() != null) {
					out.println("<label class=\"checkbox-inline\"><input type=\"checkbox\" value=\"" + value.getKey() + "\"" + selected + " />" + value.getValue() + "</label>");
				} else {
					out.println("<label class=\"checkbox-inline\"><input type=\"checkbox\" value=\"" + value.getValue() + "\"" + selected + " />" + value.getValue() + "</label>");
				}
			}
			out.println("	</select>");
			if (getMessage() != null && getMessage().trim().length() > 0) {
				out.println("	<div class=\"message " + getMessageTypeCSSClass() + "\">" + getMessage() + "</div>");
			}
			out.println("</div>");
		} else {
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
			out.println("</div>");
		}

		if (!ctx.isVisualMode()) {			
			out.println("<div class=\"col-sm-2\"><input class=\"form-control\" id=\"" + getInputNewName() + "\" name=\"" + getInputNewName() + "\" type=\"text\" placeholder=\""+i18nAccess.getText("global.create")+"...\" /></div>");
		}
		out.println("</div></div>");

		out.close();
		return writer.toString();
	}

	@Override
	public String getType() {
		return "open-multi-list";
	}

	/**
	 * process the field
	 * 
	 * @param request
	 * @return true if the field is modified.
	 */
	public boolean process(HttpServletRequest request) {

		super.process(request);

		RequestService requestService = RequestService.getInstance(request);
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
