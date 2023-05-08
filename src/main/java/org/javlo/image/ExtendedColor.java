package org.javlo.image;

import java.awt.Color;
import java.awt.color.ColorSpace;

public class ExtendedColor extends Color {
	
	public static final ExtendedColor WHITE = new ExtendedColor(Color.WHITE);
	public static final ExtendedColor BLACK = new ExtendedColor(Color.BLACK);

	public ExtendedColor(int rgb) {
		super(rgb);
	}
	
	public ExtendedColor(Color c) {		
		this((float)c.getRed()/255, (float)c.getGreen()/255, (float)c.getBlue()/255, (float)c.getAlpha()/255);
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
	
	public String getHTMLCode() {
		return String.format("#%02x%02x%02x", getRed(), getGreen(), getBlue());
	}
	
	public String getAlphaHTMLCode() {
		return "rgba("+getRed()+','+getGreen()+','+getBlue()+','+String.format("%s",getAlpha()/255)+')';
	}
	
	public boolean isDark() {
		return ImageEngine.getColorDistance(this, Color.BLACK)<0.5;
	}

	public boolean isDarker() {
		return ImageEngine.getColorDistance(this, Color.BLACK)<0.25;
	}

	public boolean isLighter() {
		return ImageEngine.getColorDistance(this, Color.BLACK)>0.75;
	}
	
	public ExtendedColor getText() {
		if (isDark()) {
			return WHITE;
		} else {
			return BLACK;
		}
	}
	
	public static ExtendedColor decode(String str) {
		float alpha = 1;
		if (str.length() == "#000000a00".length()) {			
			alpha = (float)Integer.parseInt(str.substring("#000000".length()+1, "#000000a00".length()), 16)/255;
			str = str.substring(0, "#000000".length());
		}
		Color c = Color.decode(str);
		if (alpha < 1) {		
			c = new Color((float)c.getRed()/255, (float)c.getGreen()/255, (float)c.getBlue()/255, alpha);
		}
		return new ExtendedColor(c);
	}
	
	public boolean isFilled() {
		return true;
	}
	
	@Override
	public String toString() {
		return getHTMLCode();
	}
	
	public static void main(String[] args) {
		ExtendedColor c = ExtendedColor.decode("#123456aFF");
		System.out.println(">>>>>>>>> ExtendedColor.main : c     = "+c.getHTMLCode()); //TODO: remove debug trace
		System.out.println(">>>>>>>>> ExtendedColor.main : alpha = "+c.getAlpha()); //TODO: remove debug trace
	}

}
