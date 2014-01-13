package org.javlo.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * wrap a collection for use as map with. Key and value have same value.
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class CollectionAsMap<K> implements Map<K, K> {

	public class Entry implements Map.Entry<K, K> {

		private K value;

		public Entry(K value) {
			this.value = value;
		}

		@Override
		public K getKey() {
			return value;
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

	private final Collection<K> c;

	public CollectionAsMap(Collection<K> collection) {
		c = collection;
	}

	@Override
	public int size() {
		return c.size();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		return c.contains(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return c.contains(value);
	}

	@Override
	public K get(Object key) {
		if (containsKey(key)) {
			return (K) key;
		} else {
			return null;
		}
	}

	@Override
	public K put(K key, K value) {
		c.add(key);
		return key;
	}

	@Override
	public K remove(Object key) {
		c.remove(key);
		return (K) key;
	}

	@Override
	public void putAll(Map<? extends K, ? extends K> m) {
		for (K key : m.keySet()) {
			c.add(key);
		}
	}

	@Override
	public void clear() {
		c.clear();
	}

	@Override
	public Set<K> keySet() {
		return new HashSet<K>(c);
	}

	@Override
	public Collection<K> values() {
		return c;
	}

	@Override
	public Set<java.util.Map.Entry<K, K>> entrySet() {
		Set outSet = new HashSet<Entry>();
		for (K key : c) {
			outSet.add(new Entry(key));
		}
		return outSet;
	}
}
