/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.meta;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.LoremIpsumGenerator;

/**
 * @author pvandermaesen
 */
public class Slogan extends AbstractVisualComponent {

	public static final String TYPE = "slogan";

	protected boolean isPrefixed() {
		return false;
	}

	protected String getContent(ContentContext ctx) {
		return getValue();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getHexColor() {
		return META_COLOR;
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean initContent(ContentContext ctx) {
		setValue(LoremIpsumGenerator.getParagraph(5, true, true));
		setModify();
		return true;
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}
	
	@Override
	protected boolean isWrapped(ContentContext ctx) {	
		return ctx.isEditPreview();
	}
	
	@Override
	public String getEmptyXHTMLCode(ContentContext ctx) throws Exception {
		if (getPage().getId().equals(ctx.getCurrentPage().getId())) {
			return super.getEmptyXHTMLCode(ctx);
		} else {
			return "";
		}
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}
	
	@Override
	public String getFontAwesome() {
		return "quote-left";
	}

}
