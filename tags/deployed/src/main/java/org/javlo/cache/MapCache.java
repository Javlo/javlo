package org.javlo.cache;

import java.util.Collections;
import java.util.Map;

public class MapCache implements ICache {

	private final Map cache;

	public MapCache(Map map) {
		this.cache = Collections.synchronizedMap(map);
	}

	@Override
	public Object get(String key) {
		return cache.get(key);
	}

	@Override
	public void put(String key, Object item) {
		cache.put(key, item);
	}

	@Override
	public boolean removeItem(String key) {
		return cache.remove(key) != null;
	}

	@Override
	public void removeAll() {
		cache.clear();
	}

	@Override
	public int getSize() {
		return cache.size();
	}

}
