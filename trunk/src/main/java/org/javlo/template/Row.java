package org.javlo.template;

import java.util.LinkedList;
import java.util.List;

public class Row extends TemplatePart {
	
	private Template template = null;
	
	private List<Area> areas = new LinkedList<Area>();
	
	public Row(Template inTemplate) {
		template = inTemplate;
	}
	
	public List<Area> getAreas() {
		return areas;
	}
	
	public void addArea(Area area) {
		areas.add(area);
	}
	
	@Override
	public String getFont() {	
		String font = super.getFont();
		if (font == null || font.trim().length() == 0 && template != null) {
			return template.getStyle().getFont();
		} else {
			return font;
		}
	}
	
	@Override
	public String getTextSize() {	
		String textSize = super.getTextSize();
		if (textSize == null || textSize.trim().length() == 0 && template != null) {
			return template.getStyle().getTextSize();
		} else {
			return textSize;
		}
	}	

}
