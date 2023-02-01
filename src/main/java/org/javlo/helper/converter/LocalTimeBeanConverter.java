package org.javlo.helper.converter;

import java.time.LocalTime;

import org.javlo.helper.StringHelper;

public class LocalTimeBeanConverter implements IBeanConverter<LocalTime> {

	@Override
	public LocalTime convert(String value) {
		return StringHelper.parseInputLocalTime(value);
	}

}
