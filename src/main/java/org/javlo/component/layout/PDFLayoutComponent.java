package org.javlo.component.layout;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public class PDFLayoutComponent extends AbstractVisualComponent {
	
	public static final String TYPE = "pdf-layout";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	protected void init(ComponentBean bean, ContentContext ctx) throws Exception {
		super.init(bean, ctx);
		if (StringHelper.isEmpty(getValue())) {
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
			setValue(staticConfig.getDefaultPDFLayout());
		}
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return false;
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_STANDARD;
	}
	
	@Override
	public String getFontAwesome() {
		return "file-pdf-o";
	}
	
}
