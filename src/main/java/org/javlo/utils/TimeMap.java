package org.javlo.utils;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * map with item stay a specified time inside.
 * @author Patrick Vandermaesen
 *
 * @param <K>
 * @param <V>
 */
public class TimeMap<K, V> implements Map<K, V> {

	private final Map<K, V> internalMap;
	private final Map<K, Calendar> internalTimeMap = new HashMap<K, Calendar>();
	private int defaultTimeLiveValue = Integer.MAX_VALUE;

	public TimeMap() {
		this(Integer.MAX_VALUE);
	}

	/**
	 * set the default time of the attribute live in second
	 */
	public TimeMap(int inDefaultTimeLiveValueSecond) {
		this(new HashMap<K, V>(), inDefaultTimeLiveValueSecond);
	}

	public TimeMap(Map<K, V> internalMap) {
		this(internalMap, Integer.MAX_VALUE);
	}

	public TimeMap(Map<K, V> internalMap, int inDefaultTimeLiveValueSecond) {
		this.internalMap = internalMap;
		this.defaultTimeLiveValue = inDefaultTimeLiveValueSecond;
	}

	public int getDefaultTimeValue() {
		return defaultTimeLiveValue;
	}

	/**
	 * set the default time of the attribute live of in second
	 * 
	 * @param defaultTimeValue
	 */
	public void setDefaultLiveTimeValue(int defaultTimeValue) {
		this.defaultTimeLiveValue = defaultTimeValue;
	}

	@Override
	public void clear() {
		internalMap.clear();
		internalTimeMap.clear();
	}

	@Override
	public boolean containsKey(Object arg0) {
		return internalMap.containsKey(arg0);
	}

	@Override
	public boolean containsValue(Object arg0) {
		return internalMap.containsValue(arg0);
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return internalMap.entrySet();
	}

	@Override
	public V get(Object key) {
		synchronized (internalMap) {
			Calendar cal = internalTimeMap.get(key);
			if (cal != null && Calendar.getInstance().after(cal)) {
				internalRemove(key);
			}
			return internalMap.get(key);
		}
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
	public V put(K key, V value) {
		return put(key, value, getDefaultTimeValue());
	}

	public V put(K key, V value, int liveTime) {
		synchronized (internalMap) {
			V previousValue = internalMap.put(key, value);
			if (liveTime != Integer.MAX_VALUE) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.SECOND, liveTime);
				internalTimeMap.put(key, cal);
			}
			clearCache();
			return previousValue;
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
			put(e.getKey(), e.getValue());
	}
		
	private V internalRemove(Object key) {		
		internalTimeMap.remove(key);
		return internalMap.remove(key);
	}

	
	@Override
	public V remove(Object key) {
		clearCache();
		return internalRemove(key);
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
		TimeMap map = new TimeMap();
		map.setDefaultLiveTimeValue(4);
		map.put("test", "test value");
		System.out.println("value before : " + map.get("test"));
		try {
			Thread.sleep(3 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("value after 1 : " + map.get("test"));
		try {
			Thread.sleep(3 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("value after 2 : " + map.get("test"));
	}

	/**
	 * clear value invalided by the time
	 */
	public void clearCache() {
		Collection keys = this.keySet();
		Collection toBoRemoved = new LinkedList();		
		synchronized (internalMap) {
			Calendar now =  Calendar.getInstance();
			for (Object key : keys) {
				Calendar cal = internalTimeMap.get(key);
				if (cal != null && now.after(cal)) {
					toBoRemoved.add(key);
				}
			}
			for (Object key : toBoRemoved) {
				internalRemove(key);
			}
		}
	}

}
