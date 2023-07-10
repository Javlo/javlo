package org.javlo.component.container;

import org.javlo.context.ContentContext;

import java.util.Arrays;
import java.util.Collection;

public class Section extends Box {
	
	private static final String PARALLAX_KEY = "parallax";
	
	private static final String PARALLAX_NEUTRAL = PARALLAX_KEY+"-neutral";
	
	private static final String PARALLAX_LIGHT = PARALLAX_KEY+"-light";
	
	private static final String PARALLAX_DARK = PARALLAX_KEY+"-dark";

	private static final String FIX_BACKGROUND = "fix-background";
	
	private static final String COLOR_LIGHT = "light";
	
	private static final String COLOR_DARK = "dark";
	
	private static final String NEUTRAL = "neutral";
	
	private static final Collection<String> layouts = Arrays.asList(new String[] {PARALLAX_NEUTRAL, PARALLAX_LIGHT, PARALLAX_DARK, FIX_BACKGROUND});
	private static final Collection<String> colors = Arrays.asList(new String[] {COLOR_LIGHT, COLOR_DARK, NEUTRAL});
	
	private static final String TYPE = "section";
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	protected Collection<String> getLayouts() {
		return layouts;
	}
	
	@Override
	protected Collection<String> getColors() {
		return colors;
	}
	
	public boolean isParallax() {
		return getContainerLayout().contains(PARALLAX_KEY);
	}
	
	public boolean isFix() {
		return PARALLAX_NEUTRAL.equals(FIX_BACKGROUND);
	}

	protected String getCSSClass(ContentContext ctx) {
		if (getComponentCssClass(ctx) == null || getComponentCssClass(ctx).trim().length() == 0) {
			return getType().toLowerCase() + " " + getComponentCssClass(ctx) + " content-section";
		} else {
			return getType().toLowerCase() + " " + getComponentCssClass(ctx) + " content-section";
		}
	}
}
