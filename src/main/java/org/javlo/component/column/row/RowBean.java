package org.javlo.component.column.row;

import java.util.LinkedList;
import java.util.List;

import org.javlo.helper.IStringSeralizable;
import org.javlo.helper.StringHelper;

public class RowBean implements IStringSeralizable {

	private List<CellBean> cells = new LinkedList<CellBean>();

	public RowBean(String data) {
		if (data != null) {
			loadFromString(data);
		}	
	}

	@Override
	public boolean loadFromString(String data) {
		try {
			cells = StringHelper.stringToStringSeralizableCollection(data, ",", CellBean.class);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
	public List<CellBean> getCells() {
		return cells;
	}

	@Override
	public String storeToString() {
		return StringHelper.collectionStringSeralizableToString(cells, ",");
	}

	public void addCell() {
		cells.add(new CellBean());
	}

}
