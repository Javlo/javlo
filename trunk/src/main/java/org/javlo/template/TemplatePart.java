package org.javlo.template;

import java.util.Comparator;

public class TemplatePart {

	public static class SortByName implements Comparator<TemplatePart> {

		@Override
		public int compare(TemplatePart o1, TemplatePart o2) {
			return o1.getName().compareTo(o2.getName());
		}

	}

	private String name = null;
	private String width = null;
	private String height = null;
	private String padding = null;
	private String margin = null;
	private String borderWidth = null;
	private String borderColor = null;
	private String textColor = null;
	private String backgroundColor = null;
	private String font = null;
	private String textSize = null;
	private String titleColor = null;

	public String getWidth() {
		return width;
	}
	
	public String getFinalWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getPadding() {
		return padding;
	}

	public void setPadding(String padding) {
		this.padding = padding;
	}

	public String getMargin() {
		return margin;
	}

	public void setMargin(String margin) {
		this.margin = margin;
	}

	public String getBorderWidth() {
		return borderWidth;
	}

	public void setBorderWidth(String borderWidth) {
		this.borderWidth = borderWidth;
	}

	public String getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(String borderColor) {
		this.borderColor = borderColor;
	}

	public String getTextColor() {
		return textColor;
	}

	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getFont() {
		return font;
	}

	public String getFinalFont() {
		if (getParent() != null && (font == null || font.trim().length() == 0)) {
			return getParent().getFinalFont();
		} else {
			return font;
		}
	}
	
	public String getFinalTitleColor() {
		if (getParent() != null && (titleColor == null || titleColor.trim().length() == 0)) {
			return getParent().getFinalTitleColor();
		} else {
			return titleColor;
		}
	}
	
	public String getFinalTextColor() {
		if (getParent() != null && (textColor == null || textColor.trim().length() == 0)) {
			return getParent().getFinalTextColor();
		} else {
			return textColor;
		}
	}

	/**
	 * get the parent in the html structure (sp. parent of area if row).
	 */
	protected TemplatePart getParent() {
		return null;
	}

	public void setFont(String font) {
		this.font = font;
	}

	public String getTextSize() {
		return textSize;
	}
	
	public String getFinalTextSize() {
		if (getParent() != null && (textSize == null || textSize.trim().length() == 0)) {
			return getParent().getFinalTextSize();
		} else {
			return textSize;
		}
	}

	public void setTextSize(String textSize) {
		this.textSize = textSize;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitleColor() {
		return titleColor;
	}

	public void setTitleColor(String titleColor) {
		this.titleColor = titleColor;
	}
	
	public String getTextStyle() {
		return "color: "+getFinalTextColor()+"; font-size:"+getFinalTextSize()+"; font-family:"+getFinalFont()+';';
	}

}
