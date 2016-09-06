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
	
	private static final String HIDDEN_SEPARATION = "hidden-separation";
	public static final String TYPE = "separation";

	@Override
	public String getLastSufix(ContentContext ctx) {
		// TODO: fix this open/close list globally (this is a quick fix for immediate needs of BDF)
		return "";
	}
	
	protected String getInlineStyle(ContentContext ctx) {
		String inlineStyle = "";
		if (getBackgroundColor() != null && getBackgroundColor().length() > 2) {
			inlineStyle = " overflow: hidden; border-color: " + getBackgroundColor() + ';';
		}
		if (getTextColor() != null && getTextColor().length() > 2) {
			inlineStyle = inlineStyle + " color: " + getTextColor() + ';';
		}

		if (inlineStyle.length() > 0) {
			inlineStyle = " style=\"" + inlineStyle + "\"";
		}
		return inlineStyle;
	}
	
	public String getRadioName() {
		return getContentName();
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		ctx.getRequest().setAttribute("hidden", getStyle().equals(HIDDEN_SEPARATION));
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append("<span>&nbsp;</span>");
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
		String color = "#aaaaaa";
		if (getBackgroundColor() != null && getBackgroundColor().length() > 2) {
			color = getBackgroundColor();
		}
		return "<div style=\"border-bottom: 6px "+color+" solid;\"><span></span></div>";
	}
	
	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { HIDDEN_SEPARATION, "visible-separation", "visible-large"  };
	}
	
	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		String visible = "visible";
		String hidden = "hidden";		
		String large = "large";
		try {
			I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
			visible = i18n.getText("global.visible");
			hidden = i18n.getText("global.hidden");
			large = i18n.getText("global.visible-large", large);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String[] { hidden, visible, large };
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
		if (getStyle().equals(HIDDEN_SEPARATION)) {
			return super.getEmptyXHTMLCode(ctx);
		} else {
			return renderViewXHTMLCode(ctx);
		}
	}
	
	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		if (isEditOnCreate(ctx)) {
			return false;
		}
		setStyle(ctx, "visible-separation");
		return true;
	}

	@Override
	public boolean isEditOnCreate(ContentContext ctx) {	
		return false;
	}

}
