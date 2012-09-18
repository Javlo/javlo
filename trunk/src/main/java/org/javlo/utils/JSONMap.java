package org.javlo.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class JSONMap implements Map<String, Object> {

	public static class JSONMapEntry implements java.util.Map.Entry<String, Object> {

		private String key;
		private Object value;

		public JSONMapEntry(String key, Object value) {
			System.out.println("***** JSONMap.JSONMapEntry.JSONMapEntry : key = " + key); // TODO: remove debug trace
			System.out.println("***** JSONMap.JSONMapEntry.JSONMapEntry : value =  " + value); // TODO: remove debug trace
			this.key = key;
			this.value = value;
		}

		@Override
		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		@Override
		public Object getValue() {
			return value;
		}

		@Override
		public Object setValue(Object value) {
			this.value = value;
			return this.value;
		}

	}

	private JSONObject obj;

	public JSONMap(JSONObject obj) {
		this.obj = obj;
	}

	@Override
	public int size() {
		return obj.length();
	}

	@Override
	public boolean isEmpty() {
		return obj.length() == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		try {
			return obj.get("" + key) != null;
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean containsValue(Object value) {
		throw new NotImplementedException();
	}

	private Object transform(Object obj) throws JSONException {
		if (obj instanceof JSONObject) {
			return new JSONMap((JSONObject) obj);
		} else if (obj instanceof JSONArray) {
			Collection outCol = new LinkedList();
			Set<String> keys = new HashSet<String>();
			JSONArray jsonArray = (JSONArray) obj;
			for (int i = 0; i < jsonArray.length(); i++) {
				outCol.add(transform(jsonArray.get(i)));
			}
			return outCol;
		} else {
			return obj;
		}
	}

	@Override
	public Object get(Object key) {
		try {
			JSONObject jsonObj = obj.getJSONObject("" + key);
			if (jsonObj != null) {
				return transform(jsonObj);
			}
		} catch (JSONException e) {
		}
		try {
			JSONArray jsonArray = obj.getJSONArray("" + key);
			if (jsonArray != null) {
				return transform(jsonArray);
			}
		} catch (JSONException e) {
		}
		try {
			return obj.get("" + key);
		} catch (JSONException e) {
			return null;
		}
	}

	@Override
	public Object put(String key, Object value) {
		try {
			obj.put(key, value);
			return value;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Object remove(Object key) {
		return obj.remove("" + key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		Collection<? extends String> keys = m.keySet();
		for (String key : keys) {
			put(key, m.get(key));
		}
	}

	@Override
	public void clear() {
		obj = new JSONObject();
	}

	@Override
	public Set<String> keySet() {
		Set<String> keys = new HashSet<String>();
		Iterator objKeys = obj.keys();
		while (objKeys.hasNext()) {
			keys.add("" + objKeys.next());
		}
		return keys;
	}

	@Override
	public Collection<Object> values() {
		Collection<Object> outValues = new LinkedList<Object>();
		Iterator objKeys = obj.keys();
		while (objKeys.hasNext()) {
			outValues.add(get(objKeys.next()));
		}
		return outValues;
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		Set<java.util.Map.Entry<String, Object>> outSet = new HashSet<Map.Entry<String, Object>>();
		Iterator objKeys = obj.keys();
		while (objKeys.hasNext()) {
			String key = "" + objKeys.next();
			java.util.Map.Entry<String, Object> entry = new JSONMapEntry(key, get(key));
		}

		return outSet;
	}

	public static void main(String[] args) {
		try {
			Map test = new HashMap();
			Map[] maps = new Map[2];
			maps[0] = new HashMap();
			maps[1] = new HashMap();

			maps[0].put("url", "http://123fetiche.com/photos/MetArt-35-203//002.jpg");
			maps[1].put("url", "http://123fetiche.com/photos/MetArt-35-203//004.jpg");
			test.put("images", maps);
			JSONObject testObj = new JSONObject(test);
			System.out.println("test : " + testObj);

			String objStr = "" + testObj;

			// {"images":{"url1":"url 2","url":"url 1"}}

			JSONObject obj = new JSONObject("{'images':[{'url':'http://123fetiche.com/photos/MetArt-35-203//004.jpg'},{'url':'http://123fetiche.com/photos/MetArt-35-203//004.jpg'}]}");
			System.out.println("***** JSONMap.main : images = " + obj.get("images").getClass()); // TODO: remove debug trace
			System.out.println("***** JSONMap.main : images = " + obj.getJSONObject("images").getClass()); // TODO: remove debug trace
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
