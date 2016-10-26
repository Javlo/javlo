package org.javlo.navigation.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.context.ContentContext;
import org.javlo.fields.Field.FieldBean;

public class ComponentMap implements Map<String, Object> {

	private ContentContext ctx = null;
	private IContentVisualComponent comp;
	private List<String> KEYS = Arrays.asList(new String[] { "value", "type", "bean" });

	public ComponentMap(ContentContext ctx, IContentVisualComponent comp) {
		this.ctx = ctx;
		this.comp = comp;
	}

	@Override
	public int size() {
		return KEYS.size();
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		return KEYS.contains(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return false;
	}

	@Override
	public Object get(Object key) {
		if (key == null) {
			return null;
		}
		if (key.equals("value")) {
			return comp.getValue(ctx);
		} else if (key.equals("type")) {
			return comp.getType();
		} else if (key.equals("bean")) {
			return comp.getComponentBean();
		}
		if (comp instanceof DynamicComponent) {
			try {
				return ((DynamicComponent)comp).getField(ctx, key.toString()).getBean(ctx);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public Object put(String key, Object value) {
		return null;
	}

	@Override
	public Object remove(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<String> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Object> values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

}
