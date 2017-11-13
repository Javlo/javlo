package org.javlo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.javlo.helper.LocalLogger;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.servlet.IVersion;

/**
 * map with item stay a specified time inside.
 * 
 * @author Patrick Vandermaesen
 *
 * @param <K>
 * @param <V>
 */
public class TimeMap<K, V> implements Map<K, V> {

	private final Map<K, V> internalMap;
	private final Map<K, Calendar> internalTimeMap = new HashMap<K, Calendar>();
	private final List<K> order = new LinkedList<K>();
	private int defaultTimeLiveValue = Integer.MAX_VALUE;
	private int maxSize = 0;

	public TimeMap() {
		this(Integer.MAX_VALUE);
	}

	/**
	 * set the default time of the attribute live in second
	 */
	public TimeMap(int inDefaultTimeLiveValueSecond) {
		this(new HashMap<K, V>(), inDefaultTimeLiveValueSecond);
	}

	public TimeMap(int inDefaultTimeLiveValueSecond, int maxSize) {
		this(new HashMap<K, V>(), inDefaultTimeLiveValueSecond, maxSize);
	}

	public TimeMap(Map<K, V> internalMap) {
		this(internalMap, Integer.MAX_VALUE);
	}

	public TimeMap(Map<K, V> internalMap, int inDefaultTimeLiveValueSecond) {
		this.internalMap = internalMap;
		this.defaultTimeLiveValue = inDefaultTimeLiveValueSecond;
	}

	public TimeMap(Map<K, V> internalMap, int inDefaultTimeLiveValueSecond, int maxSize) {
		this.internalMap = internalMap;
		this.defaultTimeLiveValue = inDefaultTimeLiveValueSecond;
		this.maxSize = maxSize;
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
		if (maxSize > 0 && internalTimeMap.size() == maxSize) {
			internalMap.remove(order.get(0));
			internalTimeMap.remove(order.get(0));
			order.remove(0);
		}
		synchronized (internalMap) {
			V previousValue = internalMap.put(key, value);
			if (liveTime != Integer.MAX_VALUE) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.SECOND, liveTime);
				internalTimeMap.put(key, cal);
				order.add(key);
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
		order.remove(key);
		return internalMap.remove(key);
	}

	@Override
	public V remove(Object key) {
		clearCache();
		return internalRemove(key);
	}

	@Override
	public int size() {
		clearCache();
		return internalMap.size();
	}

	@Override
	public Collection<V> values() {
		return internalMap.values();
	}

	public static void main(String[] args) throws Exception {
		TimeMap map = new TimeMap();
		map.setDefaultLiveTimeValue(4);
		map.put("test", "test value");
		map.put("test2", "value2", 1000);
		System.out.println("value before : " + map.get("test"));
		map.store(new File("c:/trans/map1.properties"));
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
		map.store(new File("c:/trans/map2.properties"));
		System.out.println("value after 2 : " + map.get("test"));
	}

	/**
	 * clear value invalided by the time
	 */
	public void clearCache() {
		Collection keys = this.keySet();
		Collection toBoRemoved = new LinkedList();
		synchronized (internalMap) {
			Calendar now = Calendar.getInstance();
			for (Object key : keys) {
				Calendar cal = internalTimeMap.get(key);
				if (cal != null && now.after(cal)) {
					toBoRemoved.add(key);
				}
			}
			order.removeAll(toBoRemoved);
			for (Object key : toBoRemoved) {
				internalRemove(key);
			}
		}
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public void store(File file) throws Exception {
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		OutputStream out = new FileOutputStream(file);
		try {
			store(out);
		} finally {
			ResourceHelper.closeResource(out);
		}
	}

	public void store(OutputStream out) throws Exception {
		clearCache();
		Properties prop = new Properties();
		for (K key : internalMap.keySet()) {
			Collection<String> storeVal = new LinkedList();
			V val = (V) internalMap.get(key);
			if (!(val instanceof String)) {
				throw new Exception("not storabe type in TimeMap : " + val.getClass().getCanonicalName());
			}
			storeVal.add("" + val);
			storeVal.add("" + internalTimeMap.get(key).getTimeInMillis());
			storeVal.add("" + order.indexOf(key));
			prop.setProperty("" + key, StringHelper.collectionToString(storeVal));
		}
		prop.store(out, "TimeMap - " + IVersion.VERSION);
	}

	public void load(File file) throws Exception {
		InputStream in = new FileInputStream(file);
		try {
			load(in);
		} finally {
			ResourceHelper.closeResource(in);
		}
	}

	public void load(InputStream in) throws IOException {
		Properties prop = new Properties();
		prop.load(in);
		String[] ordreArray = new String[prop.size()];
		for (Object key : prop.keySet()) {
			List<String> storeVal = StringHelper.stringToCollection(prop.getProperty(key.toString()));
			if (storeVal.size() == 3) {
				internalMap.put((K) key, (V) storeVal.get(0));
				long time = Long.parseLong(storeVal.get(1));
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(time);
				internalTimeMap.put((K) key, cal);
				int orderPos = Integer.parseInt(storeVal.get(2));
				if (orderPos>=0) {
					ordreArray[orderPos] = key.toString();
				}
			}
		}
		order.addAll((Collection<? extends K>) Arrays.asList(ordreArray));
		clearCache();
	}

}
