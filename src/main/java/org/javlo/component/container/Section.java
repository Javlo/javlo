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
	
	private static final String COLOR_PRIMARY = "primary";
	
	private static final String COLOR_SECONDARY = "secondary";
	
	private static final String NEUTRAL = "neutral";

	private static final String[][] LAYOUTS = {
			{ PARALLAX_NEUTRAL, PARALLAX_NEUTRAL },
			{ PARALLAX_LIGHT, PARALLAX_LIGHT },
			{ PARALLAX_DARK, PARALLAX_DARK },
			{ FIX_BACKGROUND, FIX_BACKGROUND }
	};

	private static final Collection<String> colors = Arrays.asList(new String[] {COLOR_PRIMARY, COLOR_SECONDARY, NEUTRAL});
	
	private static final String TYPE = "section";
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	protected String[][] getLayouts(ContentContext ctx) {
		String layoutConfig = getConfig(ctx).getProperty("layout", null);
		String[][] out = super.getLayouts(ctx);
		if (out.length==0) {
			out = LAYOUTS;
		}
		return out;
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
