package org.javlo.navigation.data;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;

public class PageContentMap implements Map<String, List<ComponentMap>> {
	
	private ContentContext ctx = null;
	private MenuElement page;	

	public PageContentMap(ContentContext ctx, MenuElement page) {
		this.ctx = ctx;
		this.page = page;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		return false;
	}

	@Override
	public List<ComponentMap> get(Object key) {	
		if (key == null) {
			return null;
		}
		List<ComponentMap> outMap = new LinkedList<ComponentMap>();
		try {
			for (IContentVisualComponent comp : page.getContentByType(ctx, key.toString())) {
				outMap.add(new ComponentMap(ctx, comp));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return outMap;
	}

	@Override
	public List<ComponentMap> put(String key, List<ComponentMap> value) {
		return null;
	}

	@Override
	public List<ComponentMap> remove(Object key) {
		return null;
	}

	@Override
	public void putAll(Map<? extends String, ? extends List<ComponentMap>> m) {
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
	public Collection<List<ComponentMap>> values() {
		return null;
	}

	@Override
	public Set<java.util.Map.Entry<String, List<ComponentMap>>> entrySet() {
		return null;
	}

	@Override
	public boolean containsKey(Object key) {	
		return false;
	}

}
