package org.javlo.fields;

public class FieldDescription extends FieldLargeText {

	@Override
	public String getPageDescription() {	
		return getValue();
	}
	
	@Override
	public String getType() {
		return "description";
	}

}
