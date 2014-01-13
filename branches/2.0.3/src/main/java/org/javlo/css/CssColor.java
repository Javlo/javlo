package org.javlo.css;

import java.awt.Color;
import java.awt.color.ColorSpace;

/**
 * awt color wrapper with toString return the CSS color.
 * @author pvandermaesen
 *
 */
public class CssColor extends Color {
	
	Color color = null;

	public CssColor(int rgb) {
		super(rgb);
		color = new Color(rgb);
	}

	public CssColor(int rgba, boolean hasalpha) {
		super(rgba, hasalpha);
		color = new Color(rgba, hasalpha);
	}

	public CssColor(int r, int g, int b) {
		super(r, g, b);
		color = new Color(r, g, b);
	}

	public CssColor(float r, float g, float b) {
		super(r, g, b);
		color = new Color(r, g, b);
	}

	public CssColor(ColorSpace cspace, float[] components, float alpha) {
		super(cspace, components, alpha);
		color = new Color(cspace, components, alpha);
	}

	public CssColor(int r, int g, int b, int a) {
		super(r, g, b, a);
		color = new Color(r, g, b, a);
	}

	public CssColor(float r, float g, float b, float a) {
		super(r, g, b, a);
		color = new Color(r, g, b, a);
	}
	
	public static CssColor getInstance(Color color) {
		if (color == null) {
			return null;
		}
		return new CssColor(color.getRGB());
	}
	
	@Override
	public String toString() {
		return ""+'#'+(Integer.toHexString(getRGB()).substring(2)); // remove alpha
	}

}
