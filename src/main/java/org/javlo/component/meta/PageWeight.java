/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.meta;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IPageRank;
import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;

/**
 * @author pvandermaesen
 */
public class PageWeight extends AbstractVisualComponent implements IPageRank {

	public static final String TYPE = "page-weight";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuilder finalCode = new StringBuilder();
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		finalCode.append(getSpecialInputTag());

		finalCode.append("<div class=\"btn-group\">");
		for (int i = 0; i<11; i++) {
			String weightId = "weight-" + getId() + "-" + i;
			String selected = "";
			if (Math.abs(getWeight() - ((float)i)/10) < 0.01) {
				selected = " checked=\"checked\"";
			}

			finalCode.append("<div class=\"_jv_btn-check\"><input type=\"radio\" name=\"" + getContentName() + "\" id=\"" + weightId + "\" value=\"0." + i + "\"" + selected + ">");
			finalCode.append("<label for=\"" + weightId + "\">" + i + "</label></div>");


		}
		finalCode.append("</div>");

		return finalCode.toString();
	}

	public double getWeight() {
		try {
			return Double.parseDouble(getValue());
		} catch (Throwable t) {
			return 0.5;
		}
	}

	public void setWeight(double weight) {
		setValue("" + weight);
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		/*
		 * if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) { return "<div class=\"info\">page rank : "+getPage().getPageRank(ctx)+"</div>"; }
		 */
		return "";
	}

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_NONE;
	}

	@Override
	public boolean isUnique() {
		return true;
	}

	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public String getHexColor() {
		return META_COLOR;
	}

	@Override
	public int getRankValue(ContentContext ctx, String path) {
		return (int) Math.round(getWeight() * getVotes(ctx, path));
	}

	@Override
	public int getVotes(ContentContext ctx, String path) {
		return 1000;
	}
}
