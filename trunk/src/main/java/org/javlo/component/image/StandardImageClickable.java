package org.javlo.component.image;

public class StandardImageClickable extends StandardImage {

	@Override
	public String getType() {
		return "standard-image-clickable";
	}
	
	@Override
	protected boolean isClickable() {
		return true;
	}

}
