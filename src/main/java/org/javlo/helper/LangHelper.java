package org.javlo.helper;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.javlo.component.core.ComponentContext;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.UserInterfaceContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ClipBoard;
import org.javlo.service.ContentService;
import org.javlo.service.NotificationService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.shared.SharedContentService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.ztatic.FileCache;
import org.javlo.ztatic.StaticContext;

public class LangHelper {

	/**
	 * return the instance of a class.
	 * 
	 * @return list of class returned :
	 *         <ul>
	 *         <li>HttpServletRequest</li>
	 *         <li>HttpServletResponse</li>
	 *         <li>HttpSession</li>
	 *         <li>ServletContext</li>
	 *         <li>StaticConfig</li>
	 *         <li>ContentContext</li>
	 *         <li>GlobalContext</li>
	 *         <li>I18nAccess</li>
	 *         <li>RequestService</li>
	 *         <li>EditContext</li>
	 *         <li>ContentService</li>
	 *         <li>ComponentContext</li>
	 *         <li>MenuElement : return the current page.</li>
	 *         <li>UserFactory</li>
	 *         <li>AdminUserFactory</li>
	 *         <li>AdminUserSecurity</li>
	 *         <li>PageConfiguration</li>
	 *         <li>ModuleContext</li>
	 *         <li>Module : current module.</li>
	 *         <li>MessageRepository</li>
	 *         <li>FileCache</li>
	 *         <li>StaticContext</li>
	 *         <li>UserInterfaceContext</li>
	 *         <li>ClipBoard</li>
	 *         <li>PersistenceService</li>
	 *         <li>User</li>
	 *         <li>AbstractModuleContext : return the current module context</li>
	 *         <li>? extends AbstractModuleContext : instanciate a abstract module</li>>
	 *         <li>String : the query parameter (when user make a search)</li>
	 *         <li>NotificationService</li>
	 *         <li>SharedContentService</li>
	 *         </ul>
	 * @throws Exception
	 */
	public static Object smartInstance(HttpServletRequest request, HttpServletResponse response, Class c) throws Exception {
		if (c.equals(HttpServletRequest.class)) {
			return request;
		} else if (c.equals(HttpServletResponse.class)) {
			return response;
		} else if (c.equals(HttpServletResponse.class)) {
			return response;
		} else if (c.equals(HttpSession.class)) {
			return request.getSession();
		} else if (c.equals(ServletContext.class)) {
			return request.getSession().getServletContext();
		} else if (c.equals(ContentContext.class)) {
			return ContentContext.getContentContext(request, response);
		} else if (c.equals(GlobalContext.class)) {
			return GlobalContext.getInstance(request);
		} else if (c.equals(StaticConfig.class)) {
			return StaticConfig.getInstance(request.getSession());
		} else if (c.equals(I18nAccess.class)) {
			return I18nAccess.getInstance(ContentContext.getContentContext(request, response));
		} else if (c.equals(RequestService.class)) {
			return RequestService.getInstance(request);
		} else if (c.equals(EditContext.class)) {
			return EditContext.getInstance(GlobalContext.getInstance(request), request.getSession());
		} else if (c.equals(ContentService.class)) {
			return ContentService.getInstance(GlobalContext.getInstance(request));
		} else if (c.equals(ComponentContext.class)) {
			return ComponentContext.getInstance(request);
		} else if (c.equals(MenuElement.class)) {
			return ContentContext.getContentContext(request, response).getCurrentPage();
		} else if (c.equals(UserFactory.class)) {
			return UserFactory.createUserFactory(GlobalContext.getInstance(request), request.getSession());
		} else if (c.equals(AdminUserFactory.class)) {
			return AdminUserFactory.createUserFactory(GlobalContext.getInstance(request), request.getSession());
		} else if (c.equals(AdminUserSecurity.class)) {
			return AdminUserSecurity.getInstance();
		} else if (c.equals(ModulesContext.class)) {
			return ModulesContext.getInstance(request.getSession(), GlobalContext.getInstance(request));
		} else if (c.equals(Module.class)) {
			return ModulesContext.getInstance(request.getSession(), GlobalContext.getInstance(request)).getCurrentModule();
		} else if (c.equals(MessageRepository.class)) {
			return MessageRepository.getInstance(request);
		} else if (c.equals(FileCache.class)) {
			return FileCache.getInstance(request.getSession().getServletContext());
		} else if (c.equals(StaticContext.class)) {
			return StaticContext.getInstance(request.getSession());
		} else if (c.equals(ClipBoard.class)) {
			return ClipBoard.getInstance(request);
		} else if (c.equals(PersistenceService.class)) {
			return PersistenceService.getInstance(GlobalContext.getInstance(request));
		} else if (c.equals(UserInterfaceContext.class)) {
			return UserInterfaceContext.getInstance(request.getSession(), GlobalContext.getInstance(request));
		} else if (c.equals(String.class)) {
			return RequestService.getInstance(request).getParameter("query", null);
		} else if (c.equals(AbstractModuleContext.class)) {
			return AbstractModuleContext.getCurrentInstance(request.getSession());
		} else if (AbstractModuleContext.class.isAssignableFrom(c)) {
			return AbstractModuleContext.getInstance(request.getSession(), GlobalContext.getInstance(request), ModulesContext.getInstance(request.getSession(), GlobalContext.getInstance(request)).getCurrentModule(), c);
		} else if (c.equals(User.class)) {
			return EditContext.getInstance(GlobalContext.getInstance(request), request.getSession()).getEditUser();
		} else if (c.equals(NotificationService.class)) {
			return NotificationService.getInstance(GlobalContext.getInstance(request));
		} else if (c.equals(SharedContentService.class)) {
			return SharedContentService.getInstance(ContentContext.getContentContext(request, response));
		}
		return null;
	}

	public static class MapEntry {
		private final String name;
		private final Object value;

		public MapEntry(String name, Object value) {
			this.name = name;
			this.value = value;
		}
	}

	public static MapEntry entry(String key, Object value) {
		return new MapEntry(key, value);
	}

	public static Map<String, Object> obj(MapEntry... props) {
		Map<String, Object> out = new LinkedHashMap<String, Object>();
		for (MapEntry prop : props) {
			out.put(prop.name, prop.value);
		}
		return out;
	}

	public static Map<String, String> objStr(MapEntry... props) {
		Map<String, String> out = new LinkedHashMap<String, String>();
		for (MapEntry prop : props) {
			if (prop.value != null) {
				out.put(prop.name, "" + prop.value);
			} else {
				out.put(prop.name, null);
			}
		}
		return out;
	}

	public static ObjectBuilder object() {
		return new ObjectBuilder();
	}

	public static ListBuilder list() {
		return new ListBuilder();
	}

	public static class ObjectBuilder {

		Map<String, Object> map = new LinkedHashMap<String, Object>();

		public ObjectBuilder prop(String name, Object value) {
			map.put(name, value);
			return this;
		}

		public ObjectBuilder child(String name) {
			ObjectBuilder child = new ObjectBuilder();
			prop(name, child.getMap());
			return child;
		}

		public ListBuilder list(String name) {
			ListBuilder child = new ListBuilder();
			prop(name, child.getList());
			return child;
		}

		public Map<String, Object> getMap() {
			return map;
		}

	}

	public static final int unsigned(byte b) {
		return b & 0xff;
	}

	public static class ListBuilder {

		List<Object> list = new LinkedList<Object>();

		public ListBuilder add(Object value) {
			list.add(value);
			return this;
		}

		public ObjectBuilder addObject() {
			ObjectBuilder item = new ObjectBuilder();
			add(item.getMap());
			return item;
		}

		public ListBuilder addList() {
			ListBuilder item = new ListBuilder();
			add(item.getList());
			return item;
		}

		public List<Object> getList() {
			return list;
		}

	}

	/**
	 * save access to array.
	 * 
	 * @param arrays
	 * @param i
	 *            index.
	 * @param defaultValue
	 * @return default value if array is smaller than index.
	 */
	public static Object arrays(Object[] arrays, int i, Object defaultValue) {
		if (arrays.length <= i) {
			return defaultValue;
		} else {
			return arrays[i];
		}
	}

}
