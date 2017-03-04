package org.javlo.component.files;

import org.javlo.helper.StringHelper;

public class Cell {
	private String value = "";
	private int rowSpan = 1;
	private int colSpan = 1;
	private Cell[][] array;
	private int x;
	private int y;
	ColInfo info = new ColInfo();
	
	public static class ColInfo {
		public Double total = null;
		public Double max = Double.MIN_VALUE;

		public Double getMax() {
			return max;
		}
		public Double getTotal() {
			return total;
		}
	}

	public Cell(String value, Cell[][] arrays, int x, int y) {
		this.value = value;
		this.array = arrays;
		this.x = x;
		this.y = y;			
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getRowSpan() {
		return rowSpan;
	}

	public void setRowSpan(int rowSpan) {
		this.rowSpan = rowSpan;
	}

	public int getColSpan() {
		return colSpan;
	}
	
	public boolean isDigit() {
		return StringHelper.isDigit(getValue());
	}

	public void setColSpan(int colSpan) {
		this.colSpan = colSpan;
	}

	public String getSpanAttributes() {
		String span = "";
		if (colSpan > 1) {
			span = " colspan=\"" + colSpan + "\"";
		}
		if (rowSpan > 1) {
			span = span + " rowspan=\"" + rowSpan + "\"";
		}
		return span;
	}

	@Override
	public String toString() {
		return value;
	}

	public Cell[][] getArray() {
		return array;
	}

	public int getRowTitleWidth() {
		int rowTitleHeight = 1;
		for (int r = 0; r < array.length; r++) {
			if (array[r][0] != null && array[r][0].getColSpan() > rowTitleHeight) {
				rowTitleHeight = array[r][0].getColSpan();
			}
		}
		return rowTitleHeight;
	}

	public int getColTitleHeight() {
		int colTitleHeight = 1;
		for (int c = 0; c < array[0].length; c++) {
			if (array[0][c] != null && array[0][c].getRowSpan() > colTitleHeight) {
				colTitleHeight = array[0][c].getRowSpan();
			}
		}
		return colTitleHeight;
	}

	public boolean isFirstCol() {
		if (x <= (getRowTitleWidth() - 1)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isFirstRow() {
		if (y <= (getColTitleHeight() - 1)) {
			return true;
		} else {
			return false;
		}
	}

	public String getType() {
		String content = getValue();
		String type;
		if (content == null || content.trim().length() == 0) {
			type = "empty";
		} else if (StringHelper.isLikeNumber(getValue())) {
			type = "number";
		} else {
			type = "text";
		}
		if (getValue() != null && getValue().trim().length() == 1) {
			type = type + " char";
		}
		return type;
	}

	public ColInfo getInfo() {
		return info;
	}
}