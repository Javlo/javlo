package org.javlo.utils;

public class BooleanBean {
	
	private boolean value = true;
	
	public boolean isTrue() {
		return value == true;
	}
	
	public BooleanBean() {
	}
	
	public BooleanBean(boolean value) {
		this.value = value;
	}

	public boolean isFalse() {
		return value == false;
	}

	public boolean isValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}
}
