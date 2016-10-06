package org.javlo.component.text;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.XHTMLHelper;

import de.java2html.Java2Html;

public class Code extends AbstractVisualComponent {

	public static final String TYPE = "code";
	private static final String[] STYLES = new String[] { "default", "java", "html", "css" };

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return STYLES;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (getStyle(ctx).equals("java")) {
			return Java2Html.convertToHtml(getValue());
		} else {
			return XHTMLHelper.textToXHTML(XHTMLHelper.escapeXHTML(getValue()));
		}
	}

	@Override
	protected String getTag(ContentContext ctx) {
		return "code";
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}

}
