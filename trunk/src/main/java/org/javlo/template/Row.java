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

	public void addArea(Area inArea) {		
		areas.add(inArea);
		boolean freeWidth = true;
		for (Area area : areas) {
			area.setAutoWidth(null); // reset with
			if (area.getWidth() != null && area.getWidth().trim().length() > 0) {
				freeWidth = false;
			}
		}
		if (freeWidth) {
			if (areas.size() == 3) {
				areas.get(0).setAutoWidth("33%");
				areas.get(1).setAutoWidth("34%");
				areas.get(2).setAutoWidth("33%");
			} else {
				int autoWidth = 100/areas.size();
				int modWidth = 100%areas.size();
				for (Area area : areas) {
					if (modWidth>0) {
						area.setAutoWidth(""+(autoWidth+1)+'%');
						modWidth--;
					} else {
						area.setAutoWidth(""+autoWidth+'%');
					}
				}				
			}
		}
	}

	@Override
	protected TemplatePart getParent() {
		if (template != null) {
			return template.getStyle();
		} else {
			return null;
		}
	}
	
	@Override
	public int hashCode() {	
		int hash = super.hashCode();
		int prime = 15;
		for (Area area : getAreas()) {
			hash = hash+prime*area.hashCode();
		}
		return hash;
	}

}
