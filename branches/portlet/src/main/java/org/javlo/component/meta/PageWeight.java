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
		StringBuffer finalCode = new StringBuffer();
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		finalCode.append(getSpecialInputTag());

		String lowSelected = "";
		String middleSelected = "";
		String highSelected = "";

		if (getWeight() > 0.6) {
			highSelected = " checked=\"checked\"";
		} else if (getWeight() < 0.4) {
			lowSelected = " checked=\"checked\"";
		} else {
			middleSelected = " checked=\"checked\"";
		}

		String weightId = "low-" + getId();
		finalCode.append("<input type=\"radio\" name=\"" + getContentName() + "\" id=\"" + weightId + "\" value=\"0.1\"" + lowSelected + ">");
		finalCode.append("<label for=\"" + weightId + "\">" + i18nAccess.getText("global.low") + "</label><br />");
		weightId = "middle-" + getId();
		finalCode.append("<input type=\"radio\" name=\"" + getContentName() + "\" id=\"" + weightId + "\" value=\"0.5\"" + middleSelected + ">");
		finalCode.append("<label for=\"" + weightId + "\">" + i18nAccess.getText("global.middle") + "</label><br />");
		weightId = "high-" + getId();
		finalCode.append("<input type=\"radio\" name=\"" + getContentName() + "\" id=\"" + weightId + "\" value=\"0.9\"" + highSelected + ">");
		finalCode.append("<label for=\"" + weightId + "\">" + i18nAccess.getText("global.high") + "</label><br />");

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
	public boolean isEmpty(ContentContext ctx) {
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
