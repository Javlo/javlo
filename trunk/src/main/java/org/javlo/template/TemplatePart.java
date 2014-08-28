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
	private String h1Size = null;
	private String h2Size = null;
	private String h3Size = null;
	private String h4Size = null;
	private String h5Size = null;
	private String h6Size = null;

	public String getDefaultH1Size() {
		return null;
	}

	public String getDefaultH2Size() {
		return null;
	}

	public String getDefaultH3Size() {
		return null;
	}

	public String getDefaultH4Size() {
		return null;
	}

	public String getDefaultH5Size() {
		return null;
	}

	public String getDefaultH6Size() {
		return null;
	}

	public String getDefaultFont() {
		return null;
	}

	public String getDefaultTextColor() {
		return null;
	}

	public String getDefaultTextSize() {
		return null;
	}

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
		return "color: " + getFinalTextColor() + "; font-size:" + getFinalTextSize() + "; font-family:" + getFinalFont() + ';';
	}

	public String getH1Size() {
		return h1Size;
	}
	
	public String getFinalH1Size() {
		if (getParent() != null && (h1Size == null || h1Size.trim().length() == 0)) {
			return getParent().getFinalH1Size();
		} else {
			return h1Size;
		}
	}

	public String getFinalH2Size() {
		if (getParent() != null && (h2Size == null || h2Size.trim().length() == 0)) {
			return getParent().getFinalH2Size();
		} else {
			return h2Size;
		}
	}
	
	public String getFinalH3Size() {
		if (getParent() != null && (h3Size == null || h3Size.trim().length() == 0)) {
			return getParent().getFinalH3Size();
		} else {
			return h3Size;
		}
	}
	
	public String getFinalH4Size() {
		if (getParent() != null && (h4Size == null || h4Size.trim().length() == 0)) {
			return getParent().getFinalH4Size();
		} else {
			return h4Size;
		}
	}
	
	public String getFinalH5Size() {
		if (getParent() != null && (h5Size == null || h5Size.trim().length() == 0)) {
			return getParent().getFinalH5Size();
		} else {
			return h5Size;
		}
	}
	
	public String getFinalH6Size() {
		if (getParent() != null && (h6Size == null || h6Size.trim().length() == 0)) {
			return getParent().getFinalH6Size();
		} else {
			return h6Size;
		}
	}

	public void setH1Size(String h1Size) {
		this.h1Size = h1Size;
	}

	public String getH2Size() {
		return h2Size;
	}

	public void setH2Size(String h2Size) {
		this.h2Size = h2Size;
	}

	public String getH3Size() {
		return h3Size;
	}

	public void setH3Size(String h3Size) {
		this.h3Size = h3Size;
	}

	public String getH4Size() {
		return h4Size;
	}

	public void setH4Size(String h4Size) {
		this.h4Size = h4Size;
	}

	public String getH5Size() {
		return h5Size;
	}

	public void setH5Size(String h5Size) {
		this.h5Size = h5Size;
	}

	public String getH6Size() {
		return h6Size;
	}

	public void setH6Size(String h6Size) {
		this.h6Size = h6Size;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((backgroundColor == null) ? 0 : backgroundColor.hashCode());
		result = prime * result + ((borderColor == null) ? 0 : borderColor.hashCode());
		result = prime * result + ((borderWidth == null) ? 0 : borderWidth.hashCode());
		result = prime * result + ((font == null) ? 0 : font.hashCode());
		result = prime * result + ((h1Size == null) ? 0 : h1Size.hashCode());
		result = prime * result + ((h2Size == null) ? 0 : h2Size.hashCode());
		result = prime * result + ((h3Size == null) ? 0 : h3Size.hashCode());
		result = prime * result + ((h4Size == null) ? 0 : h4Size.hashCode());
		result = prime * result + ((h5Size == null) ? 0 : h5Size.hashCode());
		result = prime * result + ((h6Size == null) ? 0 : h6Size.hashCode());
		result = prime * result + ((height == null) ? 0 : height.hashCode());
		result = prime * result + ((margin == null) ? 0 : margin.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((padding == null) ? 0 : padding.hashCode());
		result = prime * result + ((textColor == null) ? 0 : textColor.hashCode());
		result = prime * result + ((textSize == null) ? 0 : textSize.hashCode());
		result = prime * result + ((titleColor == null) ? 0 : titleColor.hashCode());
		result = prime * result + ((width == null) ? 0 : width.hashCode());		
		return result;
	}

}
