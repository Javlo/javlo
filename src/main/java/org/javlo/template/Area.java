package org.javlo.template;

public class Area extends TemplatePart {
	
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
			return getWidth();
		}
	}
}
