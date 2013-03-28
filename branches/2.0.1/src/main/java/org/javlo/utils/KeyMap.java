package org.javlo.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * fake map, return always the key as value.
 * 
 * @author Patrick Vandermaesen
 * 
 * @param <K>
 */
public class KeyMap<K> implements Map<K, K> {

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		return true;
	}

	@Override
	public boolean containsValue(Object value) {
		return false;
	}

	@Override
	public K get(Object key) {
		return (K) key;
	}

	@Override
	public K put(K key, K value) {
		return key;
	}

	@Override
	public K remove(Object key) {
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends K> m) {
	}

	@Override
	public void clear() {
	}

	@Override
	public Set<K> keySet() {
		return null;
	}

	@Override
	public Collection<K> values() {
		return null;
	}

	@Override
	public Set<java.util.Map.Entry<K, K>> entrySet() {
		return null;
	}

}
