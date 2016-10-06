/**
 * Created on 07-mars-2004
 */
package org.javlo.helper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.javlo.utils.CSVFactory;

/**
 * @author pvandermaesen
 */
public class BeanHelper {

	public class Bean1 {
		String login;

		String password;

		String email;

		/**
		 * @return
		 */
		public String getEmail() {
			return email;
		}

		/**
		 * @return
		 */
		public String getLogin() {
			return login;
		}

		/**
		 * @return
		 */
		public String getPassword() {
			return password;
		}

		/**
		 * @param string
		 */
		public void setEmail(String string) {
			email = string;
		}

		/**
		 * @param string
		 */
		public void setLogin(String string) {
			login = string;
		}

		/**
		 * @param string
		 */
		public void setPassword(String string) {
			password = string;
		}

	}

	public class Bean2 {
		String login;

		String password;

		/**
		 * @return
		 */
		public String getLogin() {
			return login;
		}

		/**
		 * @return
		 */
		public String getPassword() {
			return password;
		}

		/**
		 * @param string
		 */
		public void setLogin(String string) {
			login = string;
		}

		/**
		 * @param string
		 */
		public void setPassword(String string) {
			password = string;
		}

	}

	public static Map bean2Map(Object bean) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Map res = new Hashtable();
		Method[] methods = bean.getClass().getMethods();
		for (Method method : methods) {
			if (method.getName().startsWith("get")) {
				if (method.getReturnType().equals(String.class)) {
					String name = method.getName().substring(3);
					name = StringHelper.firstLetterLower(name);
					if (method.getParameterTypes().length == 0) {
						String value = (String) method.invoke(bean, (Object[]) null);
						if (value == null) {
							value = "";
						}
						res.put(name, value);
					}
				}
			}
		}
		return res;
	}

	public static String beanToString(Object bean) {
		StringBuffer outStr = new StringBuffer();
		Method[] methods = bean.getClass().getMethods();
		for (Method method : methods) {
			if (method.getName().startsWith("get")) {
				if (method.getReturnType().equals(String.class)) {
					String name = method.getName().substring(3);
					name = StringHelper.firstLetterLower(name);
					String value = null;
					try {
						value = (String) method.invoke(bean, (Object[]) null);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (value == null) {
						value = "";
					}
					outStr.append(value);
					outStr.append(' ');
				}
			}
		}
		return outStr.toString();
	}

	/**
	 * copy map in bean, call set[key] ( value ).
	 * 
	 * @param a
	 *            generic map
	 * @param bean
	 *            a class with set and get method.
	 * @return the number of key found in bean without set equivalent method in
	 *         bean.
	 */
	public static int copy(Map map, Object bean) throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Iterator keys = map.keySet().iterator();
		int notFound = 0;
		while (keys.hasNext()) {
			Object key = keys.next();
			if (map.get(key) instanceof String) {
				String name = (String) key;
				name = StringHelper.firstLetterLower(name);
				String value = (String) map.get(key);

				String methodName = "set" + StringHelper.firstLetterUpper(name);

				try {
					Method method = bean.getClass().getMethod(methodName, new Class[] { String.class });
					method.invoke(bean, new Object[] { value });
				} catch (NoSuchMethodException e) {
					notFound++;
				}
			}
			if (map.get(key) instanceof String[]) {
				String name = (String) key;
				name = StringHelper.firstLetterLower(name);
				String[] value = (String[]) map.get(key);
				String methodName = "set" + StringHelper.firstLetterUpper(name);
				try {
					Method method = bean.getClass().getMethod(methodName, new Class[] { String[].class });
					method.invoke(bean, new Object[] { value });
				} catch (NoSuchMethodException e) {
					notFound++;
				}
			}
		}
		return notFound;
	}

	public static void extractPropertiesAsString(Map<String, Object> out, Object bean, String... propertyNames) {
		Class<? extends Object> beanClass = bean.getClass();
		for (String propertyName : propertyNames) {
			String getterName = "get" + StringHelper.firstLetterUpper(propertyName);
			try {
				Method getter = beanClass.getMethod(getterName);
				Object value = getter.invoke(bean);
				if (value != null && !(value instanceof String)) {
					value = value.toString();
				}
				out.put(propertyName, value);
			} catch (Exception e) {
				throw new RuntimeException("Exception when reading property: '" + propertyName + "' on class: " + beanClass, e);
			}
		}
	}

	/**
	 * copy bean1 in bean2, if method set exist in bean2.
	 * 
	 * @param bean1
	 *            a class with set and get method.
	 * @param bean2
	 *            a class with set and get method.
	 */
	public static void copy(Object bean1, Object bean2) throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		BeanUtils.copyProperties(bean2, bean1);

		/*
		 * Method[] methods = bean1.getClass().getDeclaredMethods(); int
		 * notFound = 0; for (int i = 0; i < methods.length; i++) { if
		 * (methods[i].getName().startsWith("get")||methods[i].getName().
		 * startsWith("is")) {
		 * 
		 * String name = methods[i].getName().substring(3); name =
		 * StringHelper.firstLetterLower(name); Object value =
		 * methods[i].invoke(bean1, (Object[]) null); String method2 = "set" +
		 * StringHelper.firstLetterUpper(name); try { Method method =
		 * bean2.getClass().getMethod(method2, new Class[] { String.class });
		 * method.invoke(bean2, new Object[] { value }); } catch
		 * (NoSuchMethodException e) { notFound++; }
		 * 
		 * } }
		 */
	}

	public static String[] getAllLabels(Object bean) {
		List<String> labels = new LinkedList<String>();
		Method[] methods = bean.getClass().getMethods();

		boolean rolesFound = false;

		int j = 0;
		for (Method method : methods) {
			if (method.getName().startsWith("get")) {
				if (method.getReturnType().equals(String.class)) {
					String name = method.getName().substring(3);
					if (name.equals("RolesRaw")) {
						rolesFound = true;
					}
					name = StringHelper.firstLetterLower(name);
					j++;
					labels.add(name);
				}
			}
		}
		if (!rolesFound) {
			labels.add("rolesRaw");
		}
		String[] res = new String[labels.size()];
		labels.toArray(res);
		return res;
	}

	/**
	 * sort label by buisness importance, start with, login, email, firstName,
	 * lastName
	 * 
	 * @param bean
	 * @return
	 */
	public static String[] getAllLabelsSorted(Object bean) {
		List<String> labels = new LinkedList<String>();
		Method[] methods = bean.getClass().getMethods();

		boolean rolesFound = false;

		int j = 0;
		for (Method method : methods) {
			if (method.getName().startsWith("get")) {
				if (method.getReturnType().equals(String.class)) {
					String name = method.getName().substring(3);
					if (name.equals("RolesRaw")) {
						rolesFound = true;
					}
					name = StringHelper.firstLetterLower(name);
					j++;
					labels.add(name);
				}
			}
		}
		if (!rolesFound) {
			labels.add("rolesRaw");
		}
		String[] res = new String[labels.size()];

		LangHelper.asFirst(labels, "country");
		LangHelper.asFirst(labels, "mobile");
		LangHelper.asFirst(labels, "phone");
		LangHelper.asFirst(labels, "organization");
		LangHelper.asFirst(labels, "lastName");
		LangHelper.asFirst(labels, "firstName");
		LangHelper.asFirst(labels, "email");
		LangHelper.asFirst(labels, "login");

		labels.toArray(res);
		return res;
	}

	public static void storeBeanToCSV(File file, Collection<Object> beans) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {
		List<Map<String, String>> allMap = new LinkedList<Map<String, String>>();
		for (Object object : beans) {
			Map map = bean2Map(object);
			allMap.add(map);
		}
		CSVFactory.storeContentAsMap(file, allMap);
	}

	public static Object[] getAllValues(Object bean) {
		Collection<Object> values = new LinkedList<Object>();
		Method[] methods = bean.getClass().getMethods();

		boolean rolesFound = false;

		for (Method method : methods) {
			if (method.getName().startsWith("get")) {
				if (method.getReturnType().equals(String.class)) {
					String name = method.getName().substring(3);
					try {
						if (name.equals("RolesRaw")) {
							rolesFound = true;
						}
						name = StringHelper.firstLetterLower(name);
						values.add(method.invoke(bean, new Object[0]));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (!rolesFound) {
			values.add("?");
		}
		String[] res = new String[values.size()];
		values.toArray(res);
		return res;
	}

	public static void main(String[] args) {
		BeanHelper help = new BeanHelper();
		try {
			help.test();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void test() throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		BeanHelper.Bean1 bean1 = new BeanHelper.Bean1();
		BeanHelper.Bean2 bean2 = new BeanHelper.Bean2();

		bean1.setLogin("pvandermaesen");
		bean1.setPassword("AZE");
		bean1.setEmail("p@bean.com");

		System.out.println("[BeanHelper.java]-[test NC]-bean2.getLogin()="
				+ bean2.getLogin()); /* TODO: REMOVE TRACE */
		System.out.println("[BeanHelper.java]-[test NC]-bean2.getPassword()="
				+ bean2.getPassword()); /* TODO: REMOVE TRACE */

		copy(bean1, bean2);

		System.out.println("[BeanHelper.java]-[test]-bean2.getLogin()="
				+ bean2.getLogin()); /* TODO: REMOVE TRACE */
		System.out.println("[BeanHelper.java]-[test]-bean2.getPassword()="
				+ bean2.getPassword()); /* TODO: REMOVE TRACE */

		Map map = new Hashtable();
		map.put("login", "plemarchand");
		map.put("password", "456");

		copy(map, bean2);

		System.out.println("[BeanHelper.java]-[test]-bean2.getLogin()="
				+ bean2.getLogin()); /* TODO: REMOVE TRACE */
		System.out.println("[BeanHelper.java]-[test]-bean2.getPassword()="
				+ bean2.getPassword()); /* TODO: REMOVE TRACE */

	}
	
	
	public static Object getProperty (Object bean, String property) {
		try {
			return (String)bean.getClass().getMethod("get"+WordUtils.capitalize(property), null).invoke(bean, null);
		} catch (Exception e) {		
			return null;
		}
	}
	
	/*private static Map<Class, Map<String,String>> beanDescribeCache = Collections.synchronizedMap(new HashMap<Class, Map<String,String>>());
	
	public static Map<String,String> cachedDescribe(Object bean) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (bean == null) {
			return null;
		}
		Map<String,String> beanDescription = beanDescribeCache.get(bean.getClass());
		if (beanDescription == null) {
			beanDescription = BeanUtils.describe(bean);
			beanDescribeCache.put(bean.getClass(), beanDescription);
		}
		return beanDescription;
	}*/

}
