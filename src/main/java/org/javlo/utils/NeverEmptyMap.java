package org.javlo.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * map with item stay a specified time inside.
 * 
 * @author Patrick Vandermaesen
 *
 * @param <K>
 * @param <V>
 */
public class NeverEmptyMap<K, V> implements Map<K, V> {

	private Class<V> clazz;
	
	private Class<K> clazzKey = null;

	private final Map<K, V> internalMap;

	/**
	 * set the default time of the attribute live in second
	 */
	public NeverEmptyMap(Class defaultInstance) {
		internalMap = new HashMap<K, V>();
		clazz = defaultInstance;
	}
	
	/**
	 * set the default time of the attribute live in second
	 */
	public NeverEmptyMap(Class clazzKey, Class defaultInstance) {
		internalMap = new HashMap<K, V>();
		this.clazz = defaultInstance;
		this.clazzKey = clazzKey;
	}

	public NeverEmptyMap(Map<K, V> internalMap) {
		this(internalMap, Integer.MAX_VALUE);
	}

	public NeverEmptyMap(Map<K, V> internalMap, int inDefaultTimeLiveValueSecond) {
		this.internalMap = internalMap;
	}

	@Override
	public void clear() {
		internalMap.clear();
	}

	@Override
	public boolean containsKey(Object arg0) {
		return true;
	}

	@Override
	public boolean containsValue(Object arg0) {
		return internalMap.containsValue(arg0);
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return internalMap.entrySet();
	}

	private V newInstance() {
		if (clazz == Integer.class) {
			return (V) new Integer(0);
		} else if (clazz == Float.class) {
			return (V) new Float(0);
		} else if (clazz == Double.class) {
			return (V) new Double(0);
		} else if (clazz == Long.class) {
			return (V) new Long(0);
		} else {
			try {
				return clazz.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private K newKeyInstance(Object value) {
		if (clazzKey == null) {
			return (K)value;
		}
		if (Number.class.isAssignableFrom(clazzKey) && value.getClass() == String.class) {
			if (clazzKey == Integer.class || clazzKey == Long.class) {
				value = Long.parseLong((String)value);
			} else {
				value = Double.parseDouble((String)value);
			}
		}
		if (clazzKey == Integer.class) {
			return (K) new Integer(((Number)value).intValue());
		} else if (clazzKey == Float.class) {
			return (K) new Float(((Number)value).floatValue());
		} else if (clazzKey == Double.class) {
			return (K) new Double(((Number)value).doubleValue());
		} else if (clazzKey == Long.class) {
			return (K) new Long(((Number)value).longValue());
		} else {
			return (K)value;
		}
	}

	@Override
	public V get(Object inKey) {
		K key = newKeyInstance(inKey);
		V val = internalMap.get(key);
		if (val == null) {
			val = newInstance();
			put((K)key,val);
		}
		return val;
	}

	@Override
	public boolean isEmpty() {
		return internalMap.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return internalMap.keySet();
	}

	@Override
	public V put(K inKey, V value) {
		K key = newKeyInstance(inKey);
		return internalMap.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
			put(e.getKey(), e.getValue());
	}

	@Override
	public V remove(Object key) {
		return internalMap.remove(key);
	}

	@Override
	public int size() {
		return internalMap.size();
	}

	@Override
	public Collection<V> values() {
		return internalMap.values();
	}

	public static void main(String[] args) {
		NeverEmptyMap<Long, Integer> map = new NeverEmptyMap<Long, Integer>(Long.class, Integer.class);
		Long l = new Long(125);
		
		System.out.println(Number.class.isAssignableFrom(Long.class));
		
		map.put(l, 9);
		System.out.println(map.get(l));
		System.out.println(map.get(125));
		System.out.println(map.get("125"));
	}

}
