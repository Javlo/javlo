package org.javlo.helper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.javlo.service.RequestService;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class RequestParameterMap implements Map<String, Object> {

	RequestService requestService;

	public RequestParameterMap(HttpServletRequest servletRequest) {
		requestService = RequestService.getInstance(servletRequest);
	}

	@Override
	public void clear() {
		throw new NotImplementedException();
	}

	@Override
	public boolean containsKey(Object key) {
		return requestService.getParameterMap().containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		for (Object valuesObj : requestService.getParameterMap().values()) {
			String[] values = (String[]) valuesObj;
			if (values.length > 0) {
				return values[0].equals(values);
			}
		}
		return false;
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		throw new NotImplementedException();
	}

	@Override
	public Object get(Object key) {
		String[] values = requestService.getParameterValues((String) key, null);
		if (values == null || values.length == 0) {
			return null;
		} else if (values.length == 1) {
			return values[0];
		} else {
			return values;
		}
	}

	@Override
	public boolean isEmpty() {
		return requestService.getParameterMap().isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return requestService.getParameterMap().keySet();
	}

	@Override
	public String put(String key, Object value) {
		throw new NotImplementedException();
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		throw new NotImplementedException();
	}

	@Override
	public String remove(Object key) {
		throw new NotImplementedException();
	}

	@Override
	public int size() {
		return requestService.getParameterMap().size();
	}

	@Override
	public Collection<Object> values() {
		Collection<Object> outValues = new LinkedList<Object>();
		for (Object valuesObj : requestService.getParameterMap().values()) {
			Object[] values = (Object[]) valuesObj;
			if (values.length > 0) {
				outValues.add(values[0]);
			}
		}
		return outValues;
	}

}
