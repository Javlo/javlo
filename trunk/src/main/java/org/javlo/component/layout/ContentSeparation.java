/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.layout;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;


/**
 * @author pvandermaesen 
 */
public class ContentSeparation extends AbstractVisualComponent {
	
	public static final String TYPE = "separation";

	@Override
	public String getLastSufix(ContentContext ctx) {
		// TODO: fix this open/close list globally (this is a quick fix for immediate needs of BDF)
		return "";
	}
	
	public String getRadioName() {
		return getContentName();
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		ctx.getRequest().setAttribute("hidden", getStyle().equals("hidden-separation"));
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append("<div class=\"separation\"><div class=\""+getCSSClassName(ctx)+"\"><span>&nbsp;</span></div></div>");
		return finalCode.toString();
	}

	public String getType() {
		return TYPE;
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getEditXHTMLCode()
	 */
	@Override
	public String getEditXHTMLCode(ContentContext ctx) {		
		return "<div style=\"border-bottom: 6px #aaaaaa solid;\"><span></span></div>";
	}
	
	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "hidden-separation", "visible-separation"  };
	}
	
	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		String visible = "visible";
		String hidden = "hidden";		
		try {
			I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
			visible = i18n.getText("global.visible");
			hidden = i18n.getText("global.hidden");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String[] { hidden, visible };
	}
	
	public String getCSSClassName(ContentContext ctx) {
		return getStyle(ctx);
	}
	
	@Override
	public String getStyleTitle(ContentContext ctx) {
		return "";
	}
	
	@Override
	public boolean isEmpty(ContentContext ctx) {
		return true;
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}
	
	@Override
	public String getEmptyXHTMLCode(ContentContext ctx) throws Exception {
		if (getStyle().equals("hidden-separation")) {
			return super.getEmptyXHTMLCode(ctx);
		} else {
			return renderViewXHTMLCode(ctx);
		}
	}


}
