package org.javlo.fields;

import java.util.Locale;

import org.javlo.context.ContentContext;
import org.javlo.helper.XHTMLHelper;

public class FieldHelp extends Field {

	@Override
	public String getEditXHTMLCode(ContentContext ctx, boolean search) {
		return "<div class=\"alert alert-info\" role=\"alert\">"+XHTMLHelper.autoLink(getLabel(), ctx.getGlobalContext()) +"</div>";
	}

	/**
	 * return the value "displayable"
	 * 
	 * @param locale
	 * @return
	 */
	@Override
	public String getDisplayValue(ContentContext ctx, Locale locale) throws Exception {
		return "";
	}

	@Override
	public String getType() {
		return "help";
	}
}
