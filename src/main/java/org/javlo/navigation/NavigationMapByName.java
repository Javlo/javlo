package org.javlo.navigation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.javlo.context.ContentContext;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * special map, the key is the name of a page, and the value if the MenuElement.
 * 
 * @author pvandermaesen
 * 
 */
public class NavigationMapByName implements Map<String, PageBean> {

	private MenuElement root;
	private ContentContext ctx;

	public NavigationMapByName(ContentContext ctx, MenuElement root) {
		this.ctx = ctx;
		this.root = root;
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsKey(Object name) {
		return get(name) != null;
	}

	@Override
	public boolean containsValue(Object arg0) {
		return false;
	}

	@Override
	public Set<java.util.Map.Entry<String, PageBean>> entrySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public PageBean get(Object name) {
		try {
			if (root.getName().equals(name)) {
				return root.getPageBean(ctx);
			} else {
				MenuElement page = root.searchChildFromName((String) name);
				if (page != null) {
					return page.getPageBean(ctx);
				} else {
					return null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean isEmpty() {
		return root == null;
	}

	@Override
	public Set<String> keySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public PageBean put(String arg0, PageBean arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends String, ? extends PageBean> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PageBean remove(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		try {
			return root.getAllChildren().length + 1;
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public Collection<PageBean> values() {
		throw new UnsupportedOperationException();
	}

}
