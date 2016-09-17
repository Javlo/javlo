package org.javlo.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;

public class ReadOnlyPropertiesConfigurationMap implements Map<String, String> {

	private final ConfigurationProperties prop;
	private boolean displayKey = false;

	public ReadOnlyPropertiesConfigurationMap(ConfigurationProperties inProp, boolean displayKey) {
		this.prop = inProp;
		this.displayKey = displayKey;
	}

	@Override
	public void clear() {
		throw new NotImplementedException("clear");
	}

	@Override
	public boolean containsKey(Object key) {
		return prop.containsKey((String) key);
	}

	@Override
	public boolean containsValue(Object value) {
		Iterator<String> keys = prop.getKeys();
		while (keys.hasNext()) {
			if (prop.getString(keys.next()).equals(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		Map<String, String> outMap = new HashMap<String, String>();
		Iterator<String> keys = prop.getKeys();
		while (keys.hasNext()) {
			String key = keys.next();
			outMap.put(key, get(key));
		}
		return outMap.entrySet();
	}

	@Override
	public String get(Object key) {
		if (displayKey) {
			return (String) key;
		} else {
			return prop.getString((String) key);
		}
	}

	@Override
	public boolean isEmpty() {
		return prop.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		Set<String> outKeys = new HashSet<String>();
		Iterator<String> keys = prop.getKeys();
		while (keys.hasNext()) {
			outKeys.add(keys.next());
		}
		return outKeys;
	}

	@Override
	public String put(String key, String value) {
		throw new NotImplementedException("put");
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		throw new NotImplementedException("putAll");
	}

	@Override
	public String remove(Object key) {
		throw new NotImplementedException("remove");
	}

	@Override
	public int size() {
		int size = 0;
		Iterator<String> keys = prop.getKeys();
		while (keys.hasNext()) {
			keys.next();
			size++;
		}
		return size;
	}

	@Override
	public Collection<String> values() {
		Collection<String> outValues = new LinkedList<String>();
		Iterator<String> keys = prop.getKeys();
		while (keys.hasNext()) {
			String key = keys.next();
			outValues.add(prop.getString(key));
		}
		return outValues;
	}

}
