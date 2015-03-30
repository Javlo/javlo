package org.javlo.component.column;

public class OpenRow extends OpenCell {
	
	public static final String TYPE = "open-row";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public boolean isRowBreak() {	
		return true;
	}

}
