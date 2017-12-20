package org.javlo.component.column.row;

import org.javlo.helper.IStringSeralizable;

public class CellBean implements IStringSeralizable {

	private int width = 1;
	
	public CellBean() {		
	}
	
	public CellBean(String data) {
		loadFromString(data);
	}

	@Override
	public boolean loadFromString(String data) { 
		setWidth(Integer.parseInt(data));
		return true;
	}

	@Override
	public String storeToString() {
		return ""+width;
	}
	
	@Override
	public String toString() {
		return storeToString();
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}
	
}
