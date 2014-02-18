package org.javlo.template;

import java.util.LinkedList;
import java.util.List;

public class Row extends TemplatePart {
	
	private List<Area> areas = new LinkedList<Area>();
	
	public List<Area> getAreas() {
		return areas;
	}
	
	public void addArea(Area area) {
		areas.add(area);
	}

}
