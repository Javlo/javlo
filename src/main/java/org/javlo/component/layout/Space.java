/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.layout;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author pvandermaesen
 */
public class Space extends AbstractVisualComponent {

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		String cssClass = "space-";
		if (getType().equals("1px")) {
			cssClass += "tiny";
		} else if (getType().equals("3px")) {
			cssClass += "small";
		} else if (getType().equals("5px")) {
			cssClass += "normal";
		} else if (getType().equals("9px")) {
			cssClass += "large";
		} else {
			cssClass += "rem";
		}

		StringBuffer finalCode = new StringBuffer();
		if (ctx.getCurrentTemplate().isMailing()) {
			finalCode.append("<table class=\"" + getType() + " " + cssClass + "\"><tr><td height=\""+getStyle()+"\"  style=\"font-size: 0px; height: " + getStyle() + "px;\">&nbsp;.</td></tr></table>");
		} else {
			finalCode.append("<div class=\"" + getType() + " " + cssClass +  "\" style=\"font-size: 0px; height: " + getStyle() + ";\">&nbsp;.</div>");
		}
		return finalCode.toString();
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getSpecialInputTag());
		finalCode.append("<br />");
		return finalCode.toString();
	}

	@Override
	public String getType() {
		return "space";
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "1px", "3px", "5px", "9px", "20px", "1rem", "2rem", "4rem" };
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		String[] outSize = new String[8];
		try {
			I18nAccess i18n = I18nAccess.getInstance(ctx);
			outSize[0] = i18n.getText("size.tiny");
			outSize[1] = i18n.getText("size.small");
			outSize[2] = i18n.getText("size.normal");
			outSize[3] = i18n.getText("size.large");
			outSize[4] = i18n.getText("size.enormous");
			outSize[5] = i18n.getText("size.1line");
			outSize[6] = i18n.getText("size.2lines");
			outSize[7] = i18n.getText("size.4lines");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outSize;
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		String title = null;
		try {
			I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
			title = i18n.getText("size");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return title;
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}
	
	@Override
	public String getFontAwesome() {
		return "arrows-v";
	}
	
	@Override
	protected boolean getColumnableDefaultValue() {
		return true;
	}
	
	@Override
	public String getComponentLabel(ContentContext ctx, String lg) {
		String label = super.getComponentLabel(ctx,lg);
		return label+" ["+getStyleLabel(ctx)+']';
	}

}
