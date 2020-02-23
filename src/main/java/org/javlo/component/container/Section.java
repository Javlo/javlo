package org.javlo.component.container;

import java.util.Arrays;
import java.util.Collection;

public class Section extends Box {
	
	private static final String PARALLAX = "parallax";

	private static final String FIX_BACKGROUND = "fix-background";
	
	private static final Collection<String> layouts = Arrays.asList(new String[] {PARALLAX, FIX_BACKGROUND});
	
	private static final String TYPE = "section";
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	protected Collection<String> getLayouts() {
		return layouts;
	}
	
	public boolean isParallax() {
		return PARALLAX.equals(getContainerLayout());
	}
	
	public boolean isFix() {
		return PARALLAX.equals(FIX_BACKGROUND);
	}
}
