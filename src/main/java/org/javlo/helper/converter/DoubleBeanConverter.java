package org.javlo.helper.converter;

import org.javlo.helper.StringHelper;

public class DoubleBeanConverter implements IBeanConverter<Double> {

	@Override
	public Double convert(String value) {
		if (StringHelper.isFloat(value)) {
			return Double.parseDouble(value);
		} else {
			return null;
		}
	}

}
