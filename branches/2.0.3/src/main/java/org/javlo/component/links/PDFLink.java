package org.javlo.component.links;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;

public class PDFLink extends AbstractVisualComponent {
	
	public static final String TYPE = "pdf-link";

	@Override
	public String getType() {
		return TYPE;
	}
	
	protected String getURL(ContentContext ctx) {
		ContentContext pdfCtx = new ContentContext(ctx);
		pdfCtx.setFormat("pdf");
		return URLHelper.createURL(pdfCtx);
	}
	
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		ctx.getRequest().setAttribute("url", getURL(ctx));
	};
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		
		if (!ctx.getCurrentTemplate().isPDFRenderer()) {
			return "";
		}
		
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		String linkTitle = "";		
		if (i18nAccess.getViewText("pdf-link.title", (String)null) != null) {
			linkTitle = " title=\""+StringHelper.toHTMLAttribute(i18nAccess.getViewText("pdf-link.title", ""))+"\"";
		}
		return "<a href=\""+getURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE))+"\""+linkTitle+">"+getValue()+"</a>";
	}

}
