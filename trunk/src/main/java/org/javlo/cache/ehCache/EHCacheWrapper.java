package org.javlo.cache.ehCache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.javlo.cache.ICache;

public class EHCacheWrapper implements ICache {

	private final Cache cache;

	public EHCacheWrapper(Cache ehCache) {
		this.cache = ehCache;
	}

	@Override
	public Object get(String key) {
		Element elem = cache.get(key);
		if (elem != null) {
			return elem.getValue();
		}
		return null;
	}

	@Override
	public void put(String key, Object item) {
		cache.put(new Element(key, item));
	}

	@Override
	public boolean removeItem(String key) {
		return cache.remove(key);
	}

	@Override
	public void removeAll() {
		cache.removeAll();
	}

	@Override
	public int getSize() {
		return cache.getSize();
	}

}
