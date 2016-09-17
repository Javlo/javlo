/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.layout;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;

/**
 * @author pvandermaesen
 */
public class Space extends AbstractVisualComponent {

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append("<div class=\"" + getType() + "\" style=\"font-size: 0px;height: " + getStyle(ctx) + "px;\">&nbsp;.</div>");
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
		return new String[] { "1", "3", "5", "9", "20" };
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		String[] outSize = new String[5];
		try {
			I18nAccess i18n = I18nAccess.getInstance(ctx);
			outSize[0] = i18n.getText("size.tiny");
			outSize[1] = i18n.getText("size.small");
			outSize[2] = i18n.getText("size.normal");
			outSize[3] = i18n.getText("size.large");
			outSize[4] = i18n.getText("size.enormous");
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
}
