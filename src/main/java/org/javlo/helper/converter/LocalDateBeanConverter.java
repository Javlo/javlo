package org.javlo.helper.converter;

import java.time.LocalDate;

import org.javlo.helper.StringHelper;

public class LocalDateBeanConverter implements IBeanConverter<LocalDate> {

	@Override
	public LocalDate convert(String value) {
		return StringHelper.parseInputLocalDate(value);
	}

}
