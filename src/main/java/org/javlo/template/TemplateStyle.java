package org.javlo.template;

import java.util.Collections;
import java.util.Map;

import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;

public class TemplateStyle extends TemplatePart {
	
	private Map<String,String> config = Collections.EMPTY_MAP;

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

	public Map<String,String> getConfig() {
		return config;
	}

	public void setConfig(Map<String,String> config) {
		this.config = config;
	}
	
	public String getLevel() {
		return "style";
	}
	
	public String getLineHeight() {
		String textSize = getTextSize();
		String lineHeight = "14px";
		if (textSize != null && textSize.contains("px")) {
			textSize = textSize.replace("px", "").trim();
			if (StringHelper.isDigit(textSize)) {
				lineHeight=(Integer.parseInt(textSize)+2)+"px";
			}
		}
		return lineHeight;			
	}

}
