package org.javlo.image;

import java.awt.Color;
import java.awt.color.ColorSpace;

public class ExtendedColor extends Color {

	public ExtendedColor(int rgb) {
		super(rgb);
	}
	
	public ExtendedColor(Color color) {
		super(color.getRGB());
	}

	public ExtendedColor(int rgba, boolean hasalpha) {
		super(rgba, hasalpha);
	}

	public ExtendedColor(int r, int g, int b) {
		super(r, g, b);
	}

	public ExtendedColor(float r, float g, float b) {
		super(r, g, b);
	}

	public ExtendedColor(ColorSpace cspace, float[] components, float alpha) {
		super(cspace, components, alpha);
	}

	public ExtendedColor(int r, int g, int b, int a) {
		super(r, g, b, a);
	}

	public ExtendedColor(float r, float g, float b, float a) {
		super(r, g, b, a);
	}
	
	public float getGreenProportion() {
		return (float)getGreen()/(float)(getGreen()+getRed()+getBlue());
	}
	
	public float getRedProportion() {
		return (float)getRed()/(float)(getGreen()+getRed()+getBlue());
	}
	
	public float getBlueProportion() {
		return (float)getRed()/(float)(getGreen()+getRed()+getBlue());
	}
	

}
