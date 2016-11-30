package org.javlo.image;

public class EmptyColor extends ExtendedColor {
	
	public static final EmptyColor instance = new EmptyColor();
	
	public EmptyColor() {
		super(0,0,0);
	}
	
	public String getHTMLCode() {
		return "";
	}
	
	public boolean isDark() {
		return true;
	}	
	
	@Override
	public String toString() {
		return "";
	}
	
	public boolean isFilled() {
		return false;
	}

}
