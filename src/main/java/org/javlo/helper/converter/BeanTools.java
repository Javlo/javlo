package org.javlo.helper.converter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class BeanTools {

	private static Logger logger = Logger.getLogger(BeanTools.class.getName());

	private static Class<?> getClass(IBeanConverter<?> converter) {
		try {
			return converter.getClass().getMethod("convert", String.class).getReturnType();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static final Collection<IBeanConverter<?>> DEFAULT_CONVERTER = Arrays.asList(new IBeanConverter[] { new StringBeanConverter(), new IntegerBeanConverter(), new LongBeanConverter(), new LocalDateBeanConverter(), new LocalTimeBeanConverter(), new DoubleBeanConverter() });

	private static IBeanConverter<?> getConverter(Class<?> clazz, IBeanConverter<?>... converter) {
		for (IBeanConverter<?> bc : converter) {
			if (clazz.equals(getClass(bc))) {
				return bc;
			}
		}
		for (IBeanConverter<?> bc : DEFAULT_CONVERTER) {
			if (clazz.equals(getClass(bc))) {
				return bc;
			}
		}
		return null;
	}

	private static Class<?> getEffectiveType(Class clazz) {
		if (clazz.isPrimitive()) {
			if (clazz.equals(short.class)) {
				return Short.class;
			}
			if (clazz.equals(boolean.class)) {
				return Boolean.class;
			}
			if (clazz.equals(char.class)) {
				return Character.class;
			}
			if (clazz.equals(byte.class)) {
				return Byte.class;
			}
			if (clazz.equals(int.class)) {
				return Integer.class;
			}
			if (clazz.equals(long.class)) {
				return Long.class;
			}
			if (clazz.equals(double.class)) {
				return Double.class;
			}
			if (clazz.equals(float.class)) {
				return Float.class;
			}
		}
		return clazz;
	}

	public static Object fillBean(Map<String, ? extends Object> src, Object bean, IBeanConverter<?>... converter) {
		for (Method m : bean.getClass().getMethods()) {
			if (m.getName().startsWith("get")) {
				if (m.getParameterCount() == 0) {
					Class<?> returnType = m.getReturnType();
					String name = m.getName().substring(3);
					if (name.length() > 0) {
						String setMethod = "set" + name;
						try {
							Object mapValue = src.get(name.substring(0, 1).toLowerCase() + name.substring(1));
							if (mapValue != null) {
								Method sm = bean.getClass().getMethod(setMethod, returnType);
								IBeanConverter<?> beanConverter = getConverter(getEffectiveType(returnType));
								if (beanConverter == null) {
									logger.warning("converter not found for : " + returnType);
								} else {
									sm.invoke(bean, beanConverter.convert(mapValue.toString()));
								}
							}
						} catch (Exception e) {
							logger.warning("error on field : " + name + " : " + e.getMessage());
							//e.printStackTrace();
						}
					}
				}
			}
		}
		return bean;
	}

	public static void main(String[] args) throws NoSuchMethodException, SecurityException {
		Map<String, String> src = new HashMap<>();
		src.put("name", "Patrick");
		src.put("age", "47");
		src.put("birthDate", "1975-11-27");

		TestBean bean = (TestBean) fillBean(src, new TestBean());

		System.out.println(">>>>>>>>> BeanTools.main : bean id = " + bean.getId()); // TODO: remove debug trace
		System.out.println(">>>>>>>>> BeanTools.main : bean name = " + bean.getName()); // TODO: remove debug trace
		System.out.println(">>>>>>>>> BeanTools.main : bean age = " + bean.getAge()); // TODO: remove debug trace
		System.out.println(">>>>>>>>> BeanTools.main : bean birthDate = " + bean.getBirthDate()); // TODO: remove debug trace
	}

}
