package org.javlo.helper.converter;

import org.javlo.helper.StringHelper;

public class IntegerBeanConverter implements IBeanConverter<Integer> {

	@Override
	public Integer convert(String value) {
		if (StringHelper.isDigit(value)) {
			return Integer.parseInt(value);
		} else {
			return null;
		}
	}

}
