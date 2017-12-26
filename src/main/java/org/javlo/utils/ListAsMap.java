package org.javlo.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * wrap a list for use as map with. Key is the position in the List (modulo size of the list)
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class ListAsMap<K> implements Map<Integer, K> {
	
	public class Entry implements Map.Entry<Integer, K> {

		private Integer key;
		private K value;

		public Entry(Integer key, K value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public Integer getKey() {
			return key;
		}

		@Override
		public K getValue() {
			return value;
		}

		@Override
		public K setValue(K value) {
			this.value = value;
			return value;
		}

	}


	private List<K> l;

	public ListAsMap(List<K> list) {
		l = list;
	}

	@Override
	public int size() {
		return l.size();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		return true;
	}

	@Override
	public boolean containsValue(Object value) {
		return l.contains(value);
	}

	@Override
	public K get(Object key) {
		int index = ((Integer)key).intValue();
		index = index % size();
		return l.get(index);
	}

	@Override
	public K put(Integer key, K value) {
		l.add(key.intValue(),  value);
		return value;
	}

	@Override
	public K remove(Object key) {
		int index = ((Integer)key).intValue();
		K val = l.get(index);
		if (val != null) {
			l.remove(index);
		}
		return val;
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends K> m) {
		for (Integer key : m.keySet()) {
			l.add(m.get(key));
		}
	}

	@Override
	public void clear() {
		l.clear();
	}

	@Override
	public Set<Integer> keySet() {
		Set<Integer> outInt = new HashSet<Integer>();
		for (int i=0; i<l.size(); i++) {
			outInt.add(i);
		}
		return outInt;
	}

	@Override
	public Collection<K> values() {
		return l;
	}

	@Override
	public Set<java.util.Map.Entry<Integer, K>> entrySet() {
		Set outSet = new HashSet<Entry>();
		for (K val : l) {
			outSet.add(new Entry(l.indexOf(val), val));
		}
		return outSet;
	} 

	public void setCollecion(Set<K> col) {
		if (col==null) {
			col = Collections.emptySet();
		}
		l = new LinkedList(col);
	}
}
