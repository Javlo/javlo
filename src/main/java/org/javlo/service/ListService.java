package org.javlo.service;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.list.DataList;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.exception.ServiceException;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserFactory;
import org.javlo.user.UserInfo;
import org.javlo.utils.TimeMap;

public class ListService {

	public static final String SPECIAL_LIST_VIEW_USERS = "view-users";

	Map<String, List<IListItem>> localLists = new Hashtable<String, List<IListItem>>();
	
	Map<String, List<IListItem>> listCache = new TimeMap<>(30);

	private static final Map<String, List<IListItem>> hardNodeCache = new HashMap<String, List<IListItem>>();

	public static class OrderList implements Comparator<IListItem> {

		@Override
		public int compare(IListItem item1, IListItem item2) {
			if (item1.getKey().startsWith("_")) {
				if (!item2.getKey().startsWith("_")) {
					return 1;
				}
			} else if (item2.getKey().startsWith("_")) {
				if (!item1.getKey().startsWith("_")) {
					return -1;
				}

			}
			int compValue = item1.getValue().compareTo(item2.getValue());
			if (compValue == 0) {
				return item1.getKey().compareTo(item2.getKey());
			} else {
				return compValue;
			}
		}
	}

	public static class MapAllList implements Map<String, List<IListItem>> {

		private final ContentContext ctx;
		private boolean sorted = false;

		public MapAllList(ContentContext ctx, boolean sorted) {
			this.ctx = ctx;
			this.sorted = sorted;
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
			throw new NotImplementedException("containsValue");
		}

		@Override
		public Set<java.util.Map.Entry<String, List<IListItem>>> entrySet() {
			throw new NotImplementedException("entrySet");
		}

		@Override
		public List<IListItem> get(Object key) {
			try {
				List<IListItem> outList = getListService().getList(ctx, "" + key);
				if (sorted) {
					Collections.sort(outList, new OrderList());
				}
				return outList;
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
			throw new NotImplementedException("keySet");
		}

		@Override
		public List<IListItem> put(String key, List<IListItem> value) {
			throw new NotImplementedException("put");
		}

		@Override
		public void putAll(Map<? extends String, ? extends List<IListItem>> m) {
			throw new NotImplementedException("putAll");
		}

		@Override
		public List<IListItem> remove(Object key) {
			throw new NotImplementedException("remove");
		}

		@Override
		public int size() {
			throw new NotImplementedException("size");
		}

		@Override
		public Collection<List<IListItem>> values() {
			throw new NotImplementedException("values");
		}

	}

	public static class ListItem implements IListItem {
		String key;
		String value;

		private ListItem() {
		};

		public ListItem(Map.Entry mapEntry) {
			super();
			this.key = "" + mapEntry.getKey();
			this.value = "" + mapEntry.getValue();
		}

		public ListItem(String key, String value) {
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

	public static class I18nItem extends ListItem {
		private String lg = null;
		private Map<String, String> labels;

		public I18nItem(String key, Map<String, String> labels) {
			this.key = key;
			this.labels = labels;
		}

		public String getLabel() {
			return labels.get(lg);
		}

		public void setLg(String lg) {
			this.lg = lg;
		}
	}

	private static final String KEY = ListService.class.getName();

	public static ListService getInstance(ContentContext ctx) {
		GlobalContext globalContext = ctx.getGlobalContext();
		ListService outService = (ListService) globalContext.getAttribute(KEY);
		if (outService == null) {
			outService = new ListService();
			globalContext.setAttribute(KEY, outService);
		}
		return outService;
	}

	public List<IListItem> getList(ContentContext ctx, String name) throws IOException, Exception {
		List<IListItem> outList = listCache.get(name);
		if (outList != null)  {
			return outList; 
		}
		
		outList = getNavigationList(ctx, name);
		if (outList != null) {
			listCache.put(name, outList);
			return outList;
		}
		outList = getContentList(ctx, name);
		if (outList != null) {
			listCache.put(name, outList);
			return outList;
		}
		outList = localLists.get(name);
		if (outList == null) {
			outList = ctx.getCurrentTemplate().getAllList(ctx.getGlobalContext(), new Locale(ctx.getRequestContentLanguage())).get(name);
			if (outList == null) {
				ContentService content = ContentService.getInstance(ctx.getRequest());
				MenuElement page = content.getNavigation(ctx).searchChildFromName(name);
				if (page != null) {
					outList = new LinkedList<IListItem>();
					Collection<MenuElement> children = page.getChildMenuElements();
					for (MenuElement child : children) {
						outList.add(new ListItem(child.getName(), child.getTitle(ctx)));
					}
				}
			}
		}
		if (outList != null) {
			Collections.sort(outList, new OrderList());			
		} else if (name.contains("-")) {
			String[] numbers = name.split("-");
			try {
				int start = Integer.parseInt(numbers[0]);
				int end = Integer.parseInt(numbers[1]);
				List<IListItem> numberedList = new LinkedList<IListItem>();
				if (start < end) {
					for (int i = start; i <= end; i++) {
						numberedList.add(new ListItem("" + i, "" + i));
					}
					listCache.put(name, numberedList);
					return numberedList;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (outList == null) {
			outList = getSpecialList(ctx, name);
		}
		/** search taxonomy list **/
		if (outList == null) {
			outList = ctx.getGlobalContext().getAllTaxonomy(ctx).getList(ctx, name);
		}
		listCache.put(name, outList);
		return outList;
	}

	private synchronized List<IListItem> getSpecialList(ContentContext ctx, String name) throws IOException, ServiceException, Exception {
		String key = name + '-' + ctx.getRequestContentLanguage();
		if (hardNodeCache.get(key) != null) {
			return hardNodeCache.get(key);
		}
		if (name.equals("countries")) {
			List<IListItem> countriesList = new LinkedList<IListItem>();
			Collection<Map.Entry<Object, Object>> entries = I18nAccess.getInstance(ctx).getCountries(ctx).entrySet();
			for (Map.Entry entry : entries) {
				countriesList.add(new ListService.ListItem(entry));
			}
			Collections.sort(countriesList, new OrderList());
			hardNodeCache.put(key, countriesList);
			return countriesList;
		}
		if (name.equals(SPECIAL_LIST_VIEW_USERS)) {
			List<IListItem> userList = new LinkedList<IListItem>();
			IUserFactory userFactory = UserFactory.createUserFactory(ctx.getRequest());
			for (IUserInfo userInfo : userFactory.getUserInfoList()) {
				if (!StringHelper.isEmpty(userInfo.getLogin())) {
					String label = (StringHelper.neverNull(userInfo.getLastName()) + ' ' + StringHelper.neverNull(userInfo.getFirstName())).trim();
					if (StringHelper.isEmpty(label)) {
						label = userInfo.getLogin();
					}
					if (userInfo instanceof UserInfo) {
						String organization = ((UserInfo) userInfo).getOrganization();
						if (!StringHelper.isEmpty(organization)) {
							label = label + " - " + organization;
						}
					}
					userList.add(new ListItem(userInfo.getLogin(), label));
				}
			}
			return userList;
		}
		return null;
	}

	public List<IListItem> getNavigationList(ContentContext ctx, String name) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement page = content.getNavigation(ctx).searchChildFromName(name);
		if (page == null) {
			return null;
		} else {
			List<IListItem> outList = new LinkedList<IListItem>();
			for (MenuElement child : page.getChildMenuElements()) {
				String key = child.getName();
				String value = child.getTitle(ctx);
				if (value.startsWith(".")) {
					key = "";
					value = value.substring(1);
				}
				outList.add(new ListItem(key, value));
			}
			return outList;
		}
	}
	
	public List<IListItem> getContentList(ContentContext ctx, String name) throws Exception {
		List<IContentVisualComponent> comps = (List<IContentVisualComponent>) ComponentFactory.getAllComponentsFromContext(ctx);
		for (IContentVisualComponent c : comps) {
			if (c instanceof DataList) {
				return ((DataList)c).getList(ctx);
			}
		}
		return null;
	}

	public void addList(String name, Collection<String> list) {
		List<IListItem> finalList = new LinkedList<IListItem>();
		for (String item : list) {
			finalList.add(new ListItem(item, item));
		}
		addList(name, finalList);
	}

	public void addList(String name, List<IListItem> list) {
		localLists.put(name, list);
	}

	public Map<String, List<IListItem>> getAllList(ContentContext ctx) {
		return new MapAllList(ctx, false);
	}

	public Map<String, List<IListItem>> getAllListSorted(ContentContext ctx) {
		return new MapAllList(ctx, true);
	}

	public static Map<String, String> listToStringMap(List<IListItem> list) {
		Map<String, String> outMap = new LinkedHashMap<String, String>();
		for (IListItem item : list) {
			outMap.put(item.getKey(), item.getValue());
		}
		return outMap;
	}

	public void clear() {
		hardNodeCache.clear();
	}
}
