package org.javlo.template;

import org.javlo.helper.XHTMLHelper;

public class TemplateStyle extends TemplatePart {

	public TemplateStyle() {
	}
	
	public String getDefaultFont() {
		return XHTMLHelper.WEB_FONTS.iterator().next();
	}
	
	public String getDefaultTextColor() {
		return "#000000";
	}
	
	public String getDefaultTextSize() {
		return "12px";
	}
	
	public String getDefaultH1Size() {
		return "22px";
	}

	public String getDefaultH2Size() {
		return "18px";
	}

	public String getDefaultH3Size() {
		return "16px";
	}

	public String getDefaultH4Size() {
		return "14px";
	}

	public String getDefaultH5Size() {
		return "13px";
	}

	public String getDefaultH6Size() {
		return "12px";
	}
	
	public String getDefaultPadding() {
		return "5px";
	}
	
	public String getDefaultWidth() {
		return "550px";
	}

}
