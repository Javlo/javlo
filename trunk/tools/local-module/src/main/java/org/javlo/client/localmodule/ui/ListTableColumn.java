package org.javlo.client.localmodule.ui;

import java.util.Comparator;

import javax.swing.table.TableColumn;

/**
 * Column used by {@link ListTableModel}. 
 * @author Benoit DCH
 *
 * @param <I> the type of the listed datas.
 * @param <V> the type of the column value.
 */
abstract public class ListTableColumn<I extends Object, V extends Object> extends TableColumn {

	private static final long serialVersionUID = -4212870929451870912L;

	private static int modelIndex = 0;

	public ListTableColumn() {
		super();
		this.setModelIndex(modelIndex++); //To avoid duplication of column's content
	}

	public ListTableColumn(Object headerValue) {
		this();
		this.setHeaderValue(headerValue);
	}

	public ListTableColumn(Object headerValue, int preferedSize) {
		this(headerValue);
		this.setPreferredWidth(preferedSize);
	}

	public Class<?> getColumnClass() {
		return Object.class;
	}

	public boolean isCellEditable(I itemData) {
		return false;
	}

	abstract public V getValue(I itemData);

	public void setValue(I itemData, Object value) {
	};


	/**
	 * Used to sort the value returned by{@link #getValue(Object)}.
	 * @return the comparator
	 */
	public Comparator<V> getValueComparator() {
		return null;
	}
	
	/**
	 * Used to sort the list.
	 * @return the comparator
	 */
	public Comparator<I> getComparator() {
		return null;
	}
}