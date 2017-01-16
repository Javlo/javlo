package org.javlo.component.meta;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public class ComplexEventDefinitionComponent extends EventDefinitionComponent {

	public static final String TYPE = "event-definition";
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getSpecialInputTag());
		finalCode.append("<div class=\"row\">");
		finalCode.append("<div class=\"col-sm-4\"><div class=\"form-group date\"><label>Start : <input class=\"form-control\" id=\"contentdate\" style=\"width: 120px;\" type=\"text\" id=\"" + getInputStartDateName() + "\" name=\"" + getInputStartDateName() + "\" value=\"" + StringHelper.renderDateWithDefaultValue(getStartDate(), "") + "\"/></label></div></div>");
		finalCode.append("<div class=\"col-sm-6\"><div class=\"form-group\"><label>End : <input class=\"form-control date\" style=\"width: 120px;\" type=\"text\" id=\"" + getInputEndDateName() + "\" name=\"" + getInputEndDateName() + "\" value=\"" + StringHelper.renderDateWithDefaultValue(getEndDate(), "") + "\"/></label></div></div>");
		finalCode.append("</div>");
		return finalCode.toString();
	}

}
