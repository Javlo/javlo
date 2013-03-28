package org.javlo.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReadOnlyMultiMap<K, V> implements Map<K, V> {

	private final List<Map<K, V>> maps = new LinkedList<Map<K, V>>();

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsKey(Object key) {
		for (Map<K, V> map : maps) {
			if (map.containsKey(key)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		for (Map<K, V> map : maps) {
			if (map.containsValue(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		Set<java.util.Map.Entry<K, V>> outSet = new HashSet<java.util.Map.Entry<K, V>>();
		for (Map<K, V> map : maps) {
			outSet.addAll(map.entrySet());
		}
		return outSet;
	}

	@Override
	public V get(Object key) {
		int i = 0;
		for (Map<K, V> map : maps) {
			i++;
			if (map.get(key) != null) {
				return map.get(key);
			}
		}
		return null;
	}

	@Override
	public boolean isEmpty() {
		for (Map<K, V> map : maps) {
			if (!map.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Set<K> keySet() {
		Set<K> outSet = new HashSet<K>();
		for (Map<K, V> map : maps) {
			outSet.addAll(map.keySet());
		}
		return outSet;
	}

	@Override
	public V put(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		int size = 0;
		for (Map<K, V> map : maps) {
			size = size + map.size();
		}
		return size;
	}

	@Override
	public Collection<V> values() {
		Collection<V> outValues = new LinkedList<V>();
		for (Map<K, V> map : maps) {
			outValues.addAll(map.values());
		}
		return outValues;
	}

	public void addMap(Map<K, V> inMap) {
		maps.add(0, inMap);
	}

	public void clearMaps() {
		maps.clear();
	}

	public int mapListSize() {
		return maps.size();
	}

}
