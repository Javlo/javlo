package org.javlo.helper.converter;

import org.javlo.helper.StringHelper;

public class LongBeanConverter implements IBeanConverter<Long> {

	@Override
	public Long convert(String value) {
		if (StringHelper.isDigit(value)) {
			return Long.parseLong(value);
		} else {
			return null;
		}
	}

}
