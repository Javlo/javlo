package org.javlo.service;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;

public class ListService {
	
	public static class OrderList implements Comparator<Item> {

		@Override
		public int compare(Item item1, Item item2) {
			if (item1.getKey().startsWith("_")) {
				if (!item2.getKey().startsWith("_")) {
					return 1;
				}
			} else if (item2.getKey().startsWith("_")) {
				if (!item1.getKey().startsWith("_")) {
					return -1;
				}

			}
			return item1.getValue().compareTo(item2.getValue());
		}
	}

	public static class MapAllList implements Map<String, List<Item>> {

			private final ContentContext ctx;

		public MapAllList(ContentContext ctx) {
			this.ctx = ctx;
		}

		private ListService getListService() {
			return ListService.getInstance(ctx);
		}

		@Override
		public void clear() {
		}

		@Override
		public boolean containsKey(Object key) {
			try {
				return getListService().getList(ctx, "" + key) != null;
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean containsValue(Object value) {
			throw new NotImplementedException();
		}

		@Override
		public Set<java.util.Map.Entry<String, List<Item>>> entrySet() {
			throw new NotImplementedException();
		}

		@Override
		public List<Item> get(Object key) {
			try {
				return getListService().getList(ctx, "" + key);
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public Set<String> keySet() {
			throw new NotImplementedException();
		}

		@Override
		public List<Item> put(String key, List<Item> value) {
			throw new NotImplementedException();
		}

		@Override
		public void putAll(Map<? extends String, ? extends List<Item>> m) {
			throw new NotImplementedException();
		}

		@Override
		public List<Item> remove(Object key) {
			throw new NotImplementedException();
		}

		@Override
		public int size() {
			throw new NotImplementedException();
		}

		@Override
		public Collection<List<Item>> values() {
			throw new NotImplementedException();
		}

	}

	public static class Item {
		private String key;
		private String value;

		public Item(Map.Entry mapEntry) {
			super();
			this.key = "" + mapEntry.getKey();
			this.value = "" + mapEntry.getValue();
		}

		public Item(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}

	private static final String KEY = "list";

	public static ListService getInstance(ContentContext ctx) {
		GlobalContext globalContext = ctx.getGlobalContext();
		ListService outService = (ListService) globalContext.getAttribute(KEY);
		if (outService == null) {
			outService = new ListService();
			globalContext.setAttribute(KEY, outService);
		}
		return outService;
	}

	public List<Item> getList(ContentContext ctx, String name) throws IOException, Exception {
		List<Item> outList = ctx.getCurrentTemplate().getAllList(ctx.getGlobalContext(), new Locale(ctx.getRequestContentLanguage())).get(name);
		if (outList == null) {
			ContentService content = ContentService.getInstance(ctx.getRequest());
			MenuElement page = content.getNavigation(ctx).searchChildFromName(name);
			if (page != null) {
				outList = new LinkedList<Item>();
				Collection<MenuElement> children = page.getChildMenuElements();
				for (MenuElement child : children) {
					outList.add(new Item(child.getName(), child.getTitle(ctx)));
				}
			}
		}
		if (outList != null) {
			Collections.sort(outList, new OrderList());
		}
		return outList;
	}

	public Map<String, List<Item>> getAllList(ContentContext ctx) {
		return new MapAllList(ctx);
	}
	
	public static Map<String,String> listToStringMap(List<Item> list) {
		Map<String,String> outMap = new LinkedHashMap<String, String>();
		for (Item item : list) {
			outMap.put(item.getKey(), item.getValue());
		}
		return outMap;
	}
}
