/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.container;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;

/**
 * @author pvandermaesen
 */
public class RepeatContainer extends AbstractVisualComponent {

	private static final String REPEAT = "repeat";

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { REPEAT, "block repeat" };
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}

	@Override
	public String getType() {
		return "repeat-container";
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getEditXHTMLCode()
	 */
	@Override
	public String getEditXHTMLCode(ContentContext ctx) {
		return "<hr />";
	}

	@Override
	public boolean isUnique() {
		return true;
	}

	@Override
	public boolean isRepeatable() {
		return true;
	}

	public boolean isBlockRepeat(ContentContext ctx) {
		if (getArea().equals(ctx.getArea())) {
			return getComponentBean().getStyle() != null && !getComponentBean().getStyle().equals(REPEAT);
		} else {
			return false;
		}
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}

}
