package org.javlo.helper.converter;

public class StringBeanConverter implements IBeanConverter<String> {

	@Override
	public String convert(String value) {
		return value;
	}

}
