package org.javlo.template;

public class Area extends TemplatePart {
	
	private Row row;

	public Row getRow() {
		return row;
	}

	public void setRow(Row row) {
		this.row = row;
	}
	
	@Override
	public String getFont() {	
		String font = super.getFont();
		if (font == null || font.trim().length() == 0) {
			return getRow().getFont();
		} else {
			return font;
		}
	}
	
	@Override
	public String getTextSize() {	
		String textSize = super.getTextSize();
		if (textSize == null || textSize.trim().length() == 0) {
			return getRow().getTextSize();
		} else {
			return textSize;
		}
	}	
}
