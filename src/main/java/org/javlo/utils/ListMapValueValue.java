package org.javlo.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * wrap a list for use as map with. Key is the position in the List (modulo size of the list)
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class ListMapValueValue<K> implements Map<K, K> {
	
	public class Entry implements Map.Entry<K, K> {

		private K key;
		private K value;

		public Entry(K key, K value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
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


	private Collection<K> l;

	public ListMapValueValue(Collection<K> components) {
		l = components;
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
	public K put(K key, K value) {
		l.add(value);
		return value;
	}

	@Override
	public void putAll(Map<? extends K, ? extends K> m) {
		for (K key : m.keySet()) {
			l.add(m.get(key));
		}
	}

	@Override
	public void clear() {
		l.clear();
	}

	@Override
	public Set<K> keySet() {
		return new HashSet<K>(l);
	}

	@Override
	public Collection<K> values() {
		return l;
	}

	@Override
	public Set<java.util.Map.Entry<K, K>> entrySet() {
		Set outSet = new HashSet<Entry>();
		for (K val : l) {
			outSet.add(new Entry(val, val));
		}
		return outSet;
	}

	@Override
	public K get(Object key) {
		if (l.contains(key)) {
			return (K)key;
		} else {
			return null;
		}
	}

	@Override
	public K remove(Object key) {
		if (l.remove(key)) {
			return (K)key;
		} else {
			return null;
		}
	} 

}
