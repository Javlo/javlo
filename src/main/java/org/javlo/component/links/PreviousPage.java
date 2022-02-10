/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.javlo.component.core.ComplexPropertiesLink;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.PageBean;

/**
 * @author pvandermaesen
 */
public class PreviousPage extends ComplexPropertiesLink {

	public static final String TYPE = "previous-page";

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		String label = getValue().trim();
		if (StringHelper.isEmpty(label)) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
			label = i18nAccess.getViewText("global.next");
		}
		ctx.getRequest().setAttribute("label", label);
		ctx.getRequest().setAttribute("previousPage", getPreviousPage(ctx));
	}
	
	public PageBean getPreviousPage(ContentContext ctx) throws Exception {
		PageBean page = getPage().getPageBean(ctx);
		return page.getPreviousPage();
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		PageBean previousPage = getPreviousPage(ctx);
		if (previousPage == null) {
			return "";
		}
		prepareView(ctx);
		if (!isHidden(ctx)) {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			out.println("<a href=\""+previousPage.getUrl()+"\" class=\"btn btn-secondary\">"+ctx.getRequest().getAttribute("label")+"</a>");
			out.close();
			return new String(outStream.toByteArray());
		} else {
			return "";
		}
	}

	@Override
	public String getType() {
		return TYPE;
	}

	
	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

	@Override
	public boolean isListable() {
		return true;
	}

	@Override
	public String getFontAwesome() {
		return "external-link";
	}
	
	@Override
	protected boolean isAutoDeletable() {
		return true;
	}

}
