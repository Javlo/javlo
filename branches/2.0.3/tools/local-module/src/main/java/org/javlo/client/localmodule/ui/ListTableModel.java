package org.javlo.client.localmodule.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

/**
 * Model for a {@link JTable}.
 * Features:
 * <ul>
 * <li>The columns manage the value of themselves.</li>
 * <li>Clicks on headers trigger sort.</li>
 * <li>The columns manage the sort.</li>
 * </ul>
 * @author Benoit DCH
 * @param <I> the type of the listed datas.
 */
public class ListTableModel<I extends Object> extends AbstractTableModel {

	private static final long serialVersionUID = -4480872636510324997L;

	private List<I> items;
	private JTable tbl;
	private ListTableColumn<? super Object, ? super Object> sortColumn = null;
	private boolean sortAscending = true;

	private boolean sorting = false;

	/**
	 * Construct the model and add required listeners.
	 * @param tbl {@link JTable} associé.
	 */
	public ListTableModel(JTable tbl) {
		this.tbl = tbl;
		items = new ListWrapper(new LinkedList<I>());
		initializeListeners();
	}

	private void initializeListeners() {
		tbl.getTableHeader().addMouseListener(new MouseAdapter() {

			//Sort column on click
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					int columnIndex = ListTableModel.this.tbl.getColumnModel().getColumnIndexAtX(e.getX());
					if (columnIndex >= 0) {
						setSortColumn(getCol(columnIndex, false));
						sort();
					}
				}
			}
		});

		this.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if (!sorting)
					setSortColumn(null);
			}
		});
	}

	public List<I> getItems() {
		return items;
	}

	public ListTableColumn<? super Object, ? super Object> getSortColumn() {
		return sortColumn;
	}
	public void setSortColumn(ListTableColumn<? super Object, ? super Object> sortColumn) {
		if (this.sortColumn == sortColumn) {
			setSortAscending(!getSortAscending());
		} else {
			this.sortColumn = sortColumn;
			setSortAscending(true);
		}
	}

	public boolean getSortAscending() {
		return sortAscending;
	}
	public void setSortAscending(boolean sortAscending) {
		this.sortAscending = sortAscending;
	}

	/**
	 * Sort the list regarding {@link ListTableModel#getSortColumn()} and {@link ListTableModel#getSortAscending()}.
	 */
	public void sort() {
		final ListTableColumn<? super Object, ? super Object> col = this.getSortColumn();
		if (col == null) {
			return;
		}

		final Comparator<? super Object> valueComparator = col.getValueComparator();
		final Comparator<? super Object> comparator = col.getComparator();
		if (valueComparator != null || comparator != null) {
			sorting = true;
			Collections.sort(items, new Comparator<I>() {
				@Override
				public int compare(I o1, I o2) {
					int out = 0;
					if (valueComparator != null) {
						out = valueComparator.compare(col.getValue(o1), col.getValue(o2));
					}
					if (out == 0 && comparator != null) {
						out = comparator.compare(col.getValue(o1), col.getValue(o2));
					}
					return out;
				}
			});
			if (!getSortAscending()) {
				Collections.reverse(items);
			}
			this.fireTableDataChanged();
			sorting = false;
		}
	}

	private ListTableColumn<? super Object, ? super Object> getCol(int index) {
		return getCol(index, true);
	}
	@SuppressWarnings("unchecked")
	private ListTableColumn<? super Object, ? super Object> getCol(int index, boolean convert) {
		if (convert)
			index = tbl.convertColumnIndexToView(index);
		return (ListTableColumn<? super Object, ? super Object>) tbl.getColumnModel().getColumn(index);
	}

	// AbstractTableModel

	@Override
	public void fireTableChanged(TableModelEvent e) {
		if (!sorting) {
			super.fireTableChanged(e);
		} else {
			System.out.println("Event prevented: " + e.getType());
		}
	}

	public void fireTableRowsUpdated(I itemData) {
		int index = items.indexOf(itemData);
		if (index >= 0) {
			fireTableRowsUpdated(index, index);
		}
	}

	@Override
	public int getColumnCount() {
		return 0;
	}

	@Override
	public int getRowCount() {
		return items.size();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return getCol(columnIndex).getColumnClass();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return getCol(columnIndex).isCellEditable(items.get(rowIndex));
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return getCol(columnIndex).getValue(items.get(rowIndex));
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		getCol(columnIndex).setValue(items.get(rowIndex), value);
	}

	public class ListWrapper implements List<I> {

		private List<I> wrapped;

		public ListWrapper(List<I> wrapped) {
			this.wrapped = wrapped;
		}

		public I getItem(int index) {
			return wrapped.get(index);
		}

		@Override
		public boolean add(I o) {
			int i = wrapped.size();
			boolean out = wrapped.add(o);
			if (out) {
				fireTableRowsInserted(i, i);
			}
			return out;
		}

		@Override
		public void add(int index, I element) {
			wrapped.add(index, element);
			fireTableRowsInserted(index, index);
		}

		@Override
		public boolean addAll(Collection<? extends I> c) {
			int index = wrapped.size();
			boolean out = wrapped.addAll(c);
			if (out) {
				int to = wrapped.size() - 1;
				fireTableRowsInserted(index, to);
			}
			return out;
		}

		@Override
		public boolean addAll(int index, Collection<? extends I> c) {
			int size = wrapped.size();
			boolean out = wrapped.addAll(index, c);
			if (out) {
				int to = index + (wrapped.size() - size);
				fireTableRowsInserted(index, to);
			}
			return out;
		}

		@Override
		public void clear() {
			wrapped.clear();
			fireTableDataChanged();
		}

		@Override
		public boolean contains(Object o) {
			return wrapped.contains(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return wrapped.containsAll(c);
		}

		@Override
		public I get(int index) {
			return wrapped.get(index);
		}

		@Override
		public int indexOf(Object o) {
			return wrapped.indexOf(o);
		}

		@Override
		public boolean isEmpty() {
			return wrapped.isEmpty();
		}

		@Override
		public Iterator<I> iterator() {
			return wrapped.iterator();
		}

		@Override
		public int lastIndexOf(Object o) {
			return wrapped.lastIndexOf(o);
		}

		@Override
		public ListIterator<I> listIterator() {
			return wrapped.listIterator();
		}

		@Override
		public ListIterator<I> listIterator(int index) {
			return wrapped.listIterator(index);
		}

		@Override
		public boolean remove(Object o) {
			int index = wrapped.indexOf(o);
			boolean out = wrapped.remove(o);
			if (out) {
				fireTableRowsDeleted(index, index);
			}
			return out;
		}

		@Override
		public I remove(int index) {
			I out = wrapped.remove(index);
			fireTableRowsDeleted(index, index);
			return out;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			boolean out = wrapped.removeAll(c);
			if (out) {
				fireTableDataChanged();
			}
			return out;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			boolean out = wrapped.retainAll(c);
			if (out) {
				fireTableDataChanged();
			}
			return out;
		}

		@Override
		public I set(int index, I element) {
			I out = wrapped.set(index, element);
			fireTableRowsUpdated(index, index);
			return out;
		}

		@Override
		public int size() {
			return wrapped.size();
		}

		@Override
		public List<I> subList(int fromIndex, int toIndex) {
			return wrapped.subList(fromIndex, toIndex);
		}

		@Override
		public Object[] toArray() {
			return wrapped.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return wrapped.toArray(a);
		}
	}

}