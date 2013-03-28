package org.javlo.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class MapDisplayKeyIfNotFound implements Map<String, String> {

	private final Map<String, String> map;

	public MapDisplayKeyIfNotFound(Map<String, String> inMap) {
		map = inMap;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsKey(value);
	}

	@Override
	public String get(Object key) {
		String value = map.get(key);
		if (value == null) {
			return "?-" + key;
		}
		return value;
	}

	@Override
	public String put(String key, String value) {
		return map.put(key, value);
	}

	@Override
	public String remove(Object key) {
		return map.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		map.putAll(m);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Set<String> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<String> values() {
		return map.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		return map.entrySet();
	}

}
