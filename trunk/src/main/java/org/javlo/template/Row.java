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
	protected TemplatePart getParent() {
		if (template != null) {
			return template.getStyle();
		} else {
			return null;
		}
	}

}
