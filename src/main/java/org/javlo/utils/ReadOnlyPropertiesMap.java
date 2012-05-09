package org.javlo.utils;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

public class ReadOnlyPropertiesMap implements Map<String, String> {

	private Properties prop;

	public ReadOnlyPropertiesMap(Properties inProp) {
		prop = inProp;
	}

	@Override
	public void clear() {
		throw new NotImplementedException();
	}

	@Override
	public boolean containsKey(Object key) {
		return prop.containsKey((String)key);
	}

	@Override
	public boolean containsValue(Object value) {
		Enumeration keys = prop.keys();
		while (keys.hasMoreElements()) {
			if (prop.get(keys.nextElement()).equals(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		Map<String, String> outMap = new HashMap<String,String>();
		Enumeration keys = prop.keys();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			outMap.put(key, prop.getProperty(key));
		}
		return outMap.entrySet();
	}

	@Override
	public String get(Object key) {
		return prop.getProperty((String)key);
	}

	@Override
	public boolean isEmpty() {
		return prop.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		Set<String> outKeys = new HashSet<String>();
		Enumeration keys = prop.keys();
		while (keys.hasMoreElements()) {
			outKeys.add((String)keys.nextElement());			
		}
		return outKeys;
	}

	@Override
	public String put(String key, String value) {
		throw new NotImplementedException();
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		throw new NotImplementedException();
	}

	@Override
	public String remove(Object key) {
		throw new NotImplementedException();
	}

	@Override
	public int size() {
		int size = 0;
		Enumeration keys = prop.keys();
		while (keys.hasMoreElements()) {
			keys.nextElement();
			size++;
		}
		return size;
	}

	@Override
	public Collection<String> values() {
		Collection<String> outValues = new LinkedList<String>();
		Enumeration keys = prop.keys();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			outValues.add(prop.getProperty(key));
		}
		return outValues;
	}


}
