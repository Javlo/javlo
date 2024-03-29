package org.javlo.helper;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class MapHelper {
	
	private static Logger logger = Logger.getLogger(MapHelper.class.getName());

	private MapHelper() {
	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		return sortByValue(map, true);
	}

	/**
	 * sort map on value
	 * @param <K>
	 * @param <V>
	 * @param map
	 * @param ascending
	 * @return a ordered map sorted on value
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean ascending) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue())*(ascending?1:-1);
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	/**
	 * sort map with the same sorting than other map and remove key not found in reference.
	 * @param <K>
	 * @param <V>
	 * @param map map to sort
	 * @param reference ordered map with key order reference
	 * @return new ordered map with same order than reference
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sameSorting(Map<K, V> map, Map<K, V> reference) {
		Map<K, V> result = new LinkedHashMap<K, V>();
		for (K key : reference.keySet()) {
			V val = map.get(key);
			if (val == null) {
				logger.severe("key not found : "+key);
			}
			result.put(key, val);
		}
		return result;
	}
	
	/**
	 * sort map with the same sorting than other map and remove key not found in reference.
	 * @param <String>
	 * @param <V>
	 * @param map map to sort
	 * @param reference ordered map with key order reference
	 * @return new ordered map with same order than reference
	 */
	public static <V extends Comparable<? super V>> Map<String, V> sameSortingNormilized(Map<String, V> map, Map<String, V> reference) {
		Map<String, V> refNormalized = new LinkedHashMap<String, V>();
		for (Map.Entry<String, V> e : reference.entrySet()) {
			refNormalized.put(StringHelper.createComparableString(e.getKey().trim()), e.getValue());
		}
		Map<String, String> mapNormalizedRef = new LinkedHashMap<String, String>();
		for (Map.Entry<String, V> e : map.entrySet()) {
			mapNormalizedRef.put(StringHelper.createComparableString(e.getKey().trim()), e.getKey());
		}
		Map<String, V> result = new LinkedHashMap<String, V>();
		for (String key : refNormalized.keySet()) {
			String realKey =  mapNormalizedRef.get(key);
			V val =  map.get(realKey);
			if (val == null) {
				logger.severe("key not found : "+key+" ["+realKey+"]");
			}
			result.put(key, val);
		}
		return result;
	}
	
	public static void main(String[] args) {
		Map<String,String> map = new HashMap<>();
		Map<String,String> ref = new LinkedHashMap<>();
		
		map.put("P", "Patrick");
		map.put("B", "Barbara");
		map.put("C", "Catherine");
		
		ref.put("B", "Barbara");		
		ref.put("P", "Patrick");
		ref.put("C", "Catherine");
		
		for (Map.Entry<String, String> entry : map.entrySet()) {
			System.out.println(entry.getKey()+" : "+entry.getValue());
		}
		map = sameSorting(map, ref);
		System.out.println("");
		System.out.println("sort:");
		System.out.println("");
		for (Map.Entry<String, String> entry : map.entrySet()) {
			System.out.println(entry.getKey()+" : "+entry.getValue());
		}
	}
	
}
