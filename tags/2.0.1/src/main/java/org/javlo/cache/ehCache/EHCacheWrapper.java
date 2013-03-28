package org.javlo.cache.ehCache;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.javlo.cache.ICache;

public class EHCacheWrapper implements ICache {

	private final Cache cache;
	private final String name;
	private final String keyPrefix;

	public EHCacheWrapper(String prefix, String name, Cache ehCache) {
		this.keyPrefix = prefix + '.';
		this.cache = ehCache;
		this.name = name;
	}

	@Override
	public Object get(String key) {
		Element elem = cache.get(keyPrefix + key);
		if (elem != null) {
			return elem.getValue();
		}
		return null;
	}

	@Override
	public void put(String key, Object item) {
		cache.put(new Element(keyPrefix + key, item));
	}

	@Override
	public boolean removeItem(String key) {
		return cache.remove(keyPrefix + key);

	}

	@Override
	public void removeAll() {
		List keys = cache.getKeys();
		for (Object key : keys) {
			if (key.toString().startsWith(keyPrefix)) {
				cache.remove(key);
			}
		}
	}

	@Override
	public int getSize() {
		int i = 0;
		List keys = cache.getKeys();
		for (Object key : keys) {
			if (key.toString().startsWith(keyPrefix)) {
				i++;
			}
		}
		return i;
	}

	@Override
	public Collection<String> getKeys() {
		List<String> allKeys = new LinkedList<String>();
		List keys = cache.getKeys();
		for (Object key : keys) {
			if (key.toString().startsWith(keyPrefix)) {
				allKeys.add(key.toString());
			}
		}
		return allKeys;
	}

	@Override
	public String getName() {
		return name;
	}

}
