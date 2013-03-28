package org.javlo.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * implement a Map with Collection as value.
 * 
 * @author Patrick Vandermaesen
 * 
 * @param <K>
 *            the type of the key
 * @param <V>
 *            the type of element content in the collection
 */
public class MapCollectionWrapper<K, V> {

	private final Map<K, List<V>> map = new HashMap<K, List<V>>();

	public Map<K, List<V>> getMap() {
		return map;
	}

	public List<V> getValues(K key, List<V> defaultValue) {
		List<V> out = map.get(key);
		if (out == null) {
			out = defaultValue;
		}
		return out;
	}

	/**
	 * get the list
	 * 
	 * @param key
	 * @return return a list and never null
	 */
	public List<V> get(K key) {
		if (map.get(key) == null) {
			map.put(key, new LinkedList<V>());
		}
		return map.get(key);
	}

	public V get(K key, V defaultValue) {
		List<V> values = map.get(key);
		if (values == null || values.size() == 0) {
			return defaultValue;
		} else {
			return values.iterator().next();
		}
	}

	public boolean contains(K key) {
		List<V> values = map.get(key);
		return !(values == null || values.size() == 0);
	}

	public List<V> add(K key, V value) {
		List<V> collection = map.get(key);
		if (collection == null) {
			collection = new LinkedList<V>();
			map.put(key, collection);
		}
		collection.add(value);
		return collection;
	}

	/**
	 * update the value, if hashcode allready exist in the List the oldest is removed.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public List<V> addOrChange(K key, V value) {
		List<V> collection = map.get(key);
		if (collection == null) {
			collection = new LinkedList<V>();
			map.put(key, collection);
		}
		if (collection.contains(value)) {
			collection.remove(value);
		}
		collection.add(value);
		return collection;
	}

	public List<V> remove(K key, V value) {
		List<V> collection = map.get(key);
		if (collection == null) {
			return Collections.EMPTY_LIST;
		}
		collection.remove(value);
		return collection;
	}

	public void addValues(K key, List<V> newValues) {
		List<V> values = map.get(key);
		if (values == null) {
			values = newValues;
		} else {
			List<V> list = new LinkedList<V>(values);
			list.addAll(values);
		}
		map.put(key, values);
	}

	public void addAll(Map<? extends K, ? extends List<V>> m) {
		for (Entry<? extends K, ? extends List<V>> entry : m.entrySet()) {
			addValues(entry.getKey(), entry.getValue());
		}
	}

	public List<V> removeValues(K key) {
		return map.remove(key);
	}

	public List<List<V>> values() {
		List<List<V>> ouList = new LinkedList<List<V>>();
		ouList.addAll(map.values());
		return ouList;
	}

	public Set<K> keySet() {
		return map.keySet();
	}

	public static void main(String[] args) {
		MapCollectionWrapper<String, String> testMap = new MapCollectionWrapper<String, String>();

		testMap.add("test", "patrick");
		testMap.add("test", "benoit");

		List<String> names = testMap.get("test");
		for (String string : names) {
			System.out.println(string);
		}

	}

	public void clear() {
		map.clear();
	}

}
