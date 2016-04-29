package org.javlo.utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JSONMap implements Map<String, Object> {

	public static final Gson JSON = new GsonBuilder().setDateFormat("yyyy-MM-dd_HH-mm-ss-SSS ").create();

	public static JSONMap parseMap(String jsonStr) {
		return transformMap(JSON.fromJson(jsonStr, JsonElement.class));
	}

	public static Object parse(String jsonStr) {
		JsonElement jsonElement = JSON.fromJson(jsonStr, JsonElement.class);
		return transform(jsonElement);
	}

	public static JSONMap transformMap(JsonElement element) {
		return new JSONMap(element.getAsJsonObject());
	}

	public static Object transform(JsonElement element) {
		if (element == null || element.isJsonNull()) {
			return null;
		} else if (element.isJsonPrimitive()) {
			return JSON.fromJson(element, Object.class);
		} else if (element.isJsonObject()) {
			return new JSONMap(element.getAsJsonObject());
		} else if (element.isJsonArray()) {
			JsonArray array = element.getAsJsonArray();
			List<Object> outCol = new ArrayList<Object>();
			for (int i = 0; i < array.size(); i++) {
				outCol.add(transform(array.get(i)));
			}
			return outCol;
		} else {
			throw new IllegalStateException("Unmanaged case, fail to parse JSON string: " + element);
		}
	}

	public static class JSONMapEntry implements java.util.Map.Entry<String, Object> {

		private String key;
		private Object value;

		public JSONMapEntry(String key, Object value) {
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

	private JsonObject object;

	public JSONMap(JsonObject object) {
		object.getClass(); //Assert not null
		this.object = object;
	}

	@Override
	public int size() {
		return object.entrySet().size();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		return object.has("" + key);
	}

	@Override
	public boolean containsValue(Object value) {
		throw new NotImplementedException();
	}

	@Override
	public Object get(Object key) {
		return transform(object.get("" + key));
	}

	public JSONMap getMap(Object key) {
		JsonElement p = object.get("" + key);
		if (p != null) {
			return transformMap(p);
		} else {
			return null;
		}
	}

	public <T> T getValue(Object key, Class<T> classOfT) {
		return JSON.fromJson(object.get("" + key), classOfT);
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue(Object key, Type typeOfT) {
		return (T) JSON.fromJson(object.get("" + key), typeOfT);
	}

	@Override
	public Object put(String key, Object value) {
		Object o = remove(key);
		object.add(key, JSON.toJsonTree(value));
		return o;
	}

	@Override
	public Object remove(Object key) {
		return transform(object.remove("" + key));
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		for (Entry<? extends String, ? extends Object> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void clear() {
		object = new JsonObject();
	}

	@Override
	public Set<String> keySet() {
		Set<String> keys = new LinkedHashSet<String>();
		for (Entry<String, JsonElement> entry : object.entrySet()) {
			keys.add("" + entry.getKey());
		}
		return keys;
	}

	@Override
	public Collection<Object> values() {
		Collection<Object> outValues = new LinkedList<Object>();
		for (Entry<String, JsonElement> entry : object.entrySet()) {
			outValues.add(transform(entry.getValue()));
		}
		return outValues;
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		Set<java.util.Map.Entry<String, Object>> outSet = new LinkedHashSet<Map.Entry<String, Object>>();
		for (Entry<String, JsonElement> entry : object.entrySet()) {
			outSet.add(new JSONMapEntry(entry.getKey(), transform(entry.getValue())));
		}
		return outSet;
	}
	
}
