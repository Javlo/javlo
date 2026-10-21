package org.javlo.utils;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * map with values computed only when they are read (and memorized after). Iterate
 * on the map (ex: json serialization) compute all the values.
 * The loaders keep reference to the context of creation : don't store this map
 * outside the current request.
 */
public class LazyMap extends AbstractMap<String, Object> {

	public interface Loader {
		Object load() throws Exception;
	}

	private static final class Lazy {
		private final Loader loader;

		private Lazy(Loader loader) {
			this.loader = loader;
		}
	}

	private final Map<String, Object> data = new LinkedHashMap<String, Object>();

	public synchronized void putLazy(String key, Loader loader) {
		data.put(key, new Lazy(loader));
	}

	@Override
	public synchronized Object put(String key, Object value) {
		return data.put(key, value);
	}
	
	@Override
	public synchronized Object get(Object key) {
		Object value = data.get(key);
		if (value instanceof Lazy) {
			try {
				value = ((Lazy) value).loader.load();
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException("error on load lazy value : " + key, e);
			}
			data.put((String) key, value);
		}
		return value;
	}

	@Override
	public synchronized boolean containsKey(Object key) {
		return data.containsKey(key);
	}

	@Override
	public synchronized int size() {
		return data.size();
	}

	@Override
	public synchronized Set<Entry<String, Object>> entrySet() {
		for (String key : data.keySet().toArray(new String[0])) {
			get(key);
		}
		return data.entrySet();
	}
}
