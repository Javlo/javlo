package org.javlo.utils;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.javlo.context.ContentContext;
import org.javlo.helper.ServletHelper;
import org.javlo.utils.SmartMap.SmartValue;
import org.json.JSONObject;

public class SmartMap implements Map<String, SmartValue> {

	public static abstract class SmartValue {

		private ContentContext ctx;

		protected ContentContext getContentContext() {
			return ctx;
		}

		public abstract String getValue();

	}

	public static final class JspSmartValue extends SmartValue {

		private final String renderer;
		private final String jsonObj;

		public JspSmartValue(String inRenderer, String inJsonObj) {
			this.renderer = inRenderer;
			this.jsonObj = inJsonObj;
		}

		@Override
		public String getValue() {
			try {
				JSONObject jsonObj = new JSONObject(this.jsonObj);
				Map map = new JSONMap(jsonObj);
				getContentContext().getRequest().setAttribute("obj", map);
				return ServletHelper.executeJSP(getContentContext(), renderer);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

	}

	private final HashMap<String, SmartValue> internalMap;

	public WeakReference<ContentContext> ctxRef = new WeakReference(null);

	public SmartMap() {
		internalMap = new HashMap<String, SmartValue>();
	};

	public SmartMap(ContentContext ctx, SmartMap map) {
		ctxRef = new WeakReference<ContentContext>(ctx);
		internalMap = map.internalMap;
	}

	@Override
	public int size() {
		return internalMap.size();
	}

	@Override
	public boolean isEmpty() {
		return internalMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return internalMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return internalMap.containsValue(value);
	}

	@Override
	public SmartValue get(Object key) {
		ContentContext ctx = ctxRef.get();
		if (ctx == null) {
			return null;
		}
		SmartValue sValue = internalMap.get(key);
		if (sValue != null) {
			sValue.ctx = ctx;
		}
		return sValue;
	}

	@Override
	public SmartValue put(String key, SmartValue value) {
		return internalMap.put(key, value);
	}

	@Override
	public SmartValue remove(Object key) {
		return internalMap.remove(key);
	}

	@Override
	public void putAll(Map m) {
		internalMap.putAll(m);
	}

	@Override
	public void clear() {
		internalMap.clear();
	}

	@Override
	public Set<String> keySet() {
		return internalMap.keySet();
	}

	@Override
	public Collection<SmartValue> values() {
		return internalMap.values();
	}

	@Override
	public Set entrySet() {
		return internalMap.entrySet();
	}

}
