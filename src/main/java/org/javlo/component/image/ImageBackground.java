package org.javlo.component.image;

import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.helper.StringHelper;

public class ImageBackground extends GlobalImage {

	public static final String TYPE = "image-background";
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	protected boolean isMeta() {
		return false;
	}
	
	@Override
	protected boolean isImageFilter() {
		return false;
	}
	
	@Override
	protected boolean isLink() {
		return true;
	}
	
	@Override
	public int getPriority(ContentContext ctx) {
		if (isRepeat()) {
			return 8;
		} else {
			return 9;
		}
	}
	
	@Override
	public boolean isWithDescription() {
		return false;
	}
	
	@Override
	public boolean isDisplayHidden() {	
		return true;
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		EditContext editContext = EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession());
		if (ctx.isAsPreviewMode() && editContext.isPreviewEditionMode()) {
			return "["+getType()+"]";
		} else {
			return "";
		}
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return !StringHelper.isEmpty(getValue());
	}
	
	@Override
	public String getFontAwesome() {
		return "picture-o";
	}
	
}
