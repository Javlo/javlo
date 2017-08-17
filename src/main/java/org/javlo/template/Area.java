package org.javlo.template;

import org.javlo.helper.StringHelper;

public class Area extends TemplatePart {
	
	public static final int MAX_WIDTH = 12;
	
	private Row row;
	private String autoWidth = null;
	
	public Row getRow() {
		return row;
	}

	public void setRow(Row row) {
		this.row = row;
	}
	
	@Override
	protected TemplatePart getParent() {
		return row;
	}

	public String getAutoWidth() {
		return autoWidth;
	}

	public void setAutoWidth(String autoWidth) {
		this.autoWidth = autoWidth;
	}


	@Override
	public String getFinalWidth() {
		if (getAutoWidth() != null) {
			return getAutoWidth();
		} else {
			if ("auto".equals(getWidth())) {
				int width = MAX_WIDTH;
				for (Area area : getRow().getAreas()) {
					if (area != this && StringHelper.isDigit(area.getWidth())) {
						width = width - Integer.parseInt(area.getWidth());
					}
				}
				return ""+width;
			} else {
				return getWidth();
			}
		}
	}
	
	@Override
	public String getLevel() {
		return "area";
	}
}
