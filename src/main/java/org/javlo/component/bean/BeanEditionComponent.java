package org.javlo.component.bean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.helper.StringHelper;

public class BeanEditionComponent extends AbstractVisualComponent {

	public static final String TYPE = "bean_edition";

	@Override
	public String getType() {
		return TYPE;
	}
	
	protected BeanField createField (Method m, Object obj) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		BeanField out = new BeanField();
		out.setName(m.getName().substring(3));
		
		String type = "text";
		if (m.getReturnType() == Integer.class || m.getReturnType() == Long.class) {
			type = "number";
		} else if (m.getReturnType() == LocalDate.class || m.getReturnType() == Date.class) {
			type = "date";
		}
		
		if (obj != null) {
			out.setValue(""+m.invoke(obj, null));
		}
		return out;
	}
	
	public List<BeanField> extractField() throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (StringHelper.isEmpty(getValue())) {
			return Collections.EMPTY_LIST;
		}
		Class clazz = Class.forName(getValue());
		List<BeanField> out = new LinkedList<>();
		for (Method m : clazz.getMethods()) {
			if (m.getName().startsWith("get")) {
				out.add(createField(m, null));
			}
		}
		return out;
	}

}
