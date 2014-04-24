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
	protected TemplatePart getParent() {
		return row;
	}
}
