package org.javlo.component.text;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentLayout;
import org.javlo.context.ContentContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.LoremIpsumGenerator;
import org.javlo.i18n.I18nAccess;

public class Text extends AbstractVisualComponent {

	public static final String TYPE = "text";
	
	@Override
	protected void init() throws ResourceNotFoundException {
		super.init();
		if (getLayout() == null) {
			getComponentBean().setLayout(new ComponentLayout(""));
		}
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			return new String[] { i18nAccess.getText("content.text.small", "small"), i18nAccess.getText("content.text.normal", "normal"), i18nAccess.getText("content.text.big", "big") };
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getStyleList(ctx);
	}

	@Override
	public String getStyleLabel(ContentContext ctx) {
		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			return i18nAccess.getText("content.text.size", "size");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "size";
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "small", "normal", "big" };
	}

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public boolean initContent(ContentContext ctx) throws Exception {	
		boolean out =  super.initContent(ctx);
		if (isEditOnCreate(ctx)) {
			return out;
		}
		setStyle(ctx, "normal");
		setValue(LoremIpsumGenerator.getParagraph(3, false, false));
		return out;
	}
	
	protected String getInlineStyle(ContentContext ctx) {
		String inlineStyle = "";
		if (getBackgroundColor() != null && getBackgroundColor().length() > 2) {
			inlineStyle = " overflow: hidden; border: 1px "+getBackgroundColor()+" solid; background-color: " + getBackgroundColor() + ';';
		}
		if (getTextColor() != null && getTextColor().length() > 2) {
			inlineStyle = inlineStyle + " color: " + getTextColor() + ';';
		}
		if (getLayout() != null) {
			inlineStyle = inlineStyle + ' ' + getLayout().getStyle();
		}
		inlineStyle = inlineStyle.trim();
		if (inlineStyle.length() > 0) {
			inlineStyle = " style=\"" + inlineStyle + "\"";
		}
		return inlineStyle;
	}

}
