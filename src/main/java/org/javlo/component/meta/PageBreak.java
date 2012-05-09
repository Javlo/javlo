/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.meta;

import java.text.NumberFormat;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

/**
 * @author pvandermaesen
 */
public class PageBreak extends AbstractVisualComponent {

	private static final String PAGE_NUMBER = "page-number-"+PageBreak.class.getName();
	private static final String NORMAL = "normal";
	private static final String LAST = "last";
	public static final String TYPE = "page-break";

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { NORMAL, LAST };
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		return getStyleList(ctx);
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		return "";
	}

	public String getType() {
		return TYPE;
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}

	public int getPageNumber(ContentContext ctx) {
		if ((Integer) ctx.getRequest().getAttribute(PAGE_NUMBER) == null) {
			return 1;
		} else {
			return (Integer) ctx.getRequest().getAttribute(PAGE_NUMBER);
		}
	}

	public void setPageNumber(ContentContext ctx, int page) {
		ctx.getRequest().setAttribute(PAGE_NUMBER, page);
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (getStyle(ctx).equals(LAST)) {
			return "</div> <!-- last page -->";
		} else {
			String out = "</div><div id=\"page-" + StringHelper.renderNumber(getPageNumber(ctx),4) + "\" class=\"page\">";
			if (getPageNumber(ctx) == 1) {
				NumberFormat nf = NumberFormat.getInstance();
				nf.setMinimumFractionDigits(4);
				out = "<div id=\"page-" + StringHelper.renderNumber(getPageNumber(ctx),4) + "\" class=\"page\">";
			}
			setPageNumber(ctx, getPageNumber(ctx) + 1);
			return out;
		}
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return true;
	}

	@Override
	public String getHexColor() {
		return DEFAULT_COLOR;
	}

}
