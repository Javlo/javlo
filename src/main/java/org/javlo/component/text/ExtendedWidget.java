/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.text;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * @author pvandermaesen
 */
public class ExtendedWidget extends AbstractVisualComponent {

	public static final String TYPE = "extendedWidget";

	public static final String XHTML_RESOURCE_FOLDER = "_xhtml_resources";

	private Boolean cachable = null;

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}

	@Override
	public String getEditRenderer(ContentContext ctx) {
		return "/jsp/edit/component/extendedWidget/edit_extendedWidget.jsp";
	}

	@Override
	public String getHexColor() {
		return GRAPHIC_COLOR;
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		cachable = null;
		return super.performEdit(ctx);
	}

	/*
	 * @Override public String getPrefixViewXHTMLCode(ContentContext ctx) {
	 * return ""; }
	 * 
	 * @Override public String getSuffixViewXHTMLCode(ContentContext ctx) {
	 * return ""; }
	 */

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getHeaderContent(ContentContext ctx) {
		String xhtml = getValue();
		if (xhtml.toLowerCase().contains("<head")) {
			Document doc = Jsoup.parse(xhtml);
			Elements head = doc.select("head");
			return head.html();
		} else {
			return null;
		}
	}


	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		String xhtml = getValue();
		if (xhtml.toLowerCase().contains("<body")) {
			Document doc = Jsoup.parse(xhtml);
			Elements body = doc.select("body");
			xhtml = body.html();
		}
		return XHTMLHelper.replaceLinks(ctx, XHTMLHelper.replaceJSTLData(ctx, xhtml));
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		if (!isRepeat()) {
			return true;
		} else {
			if (cachable == null) {
				cachable = !getValue().contains("${");				
			}
			return cachable;
		}
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return !StringHelper.isEmpty(getValue());
	}
	
	@Override
	protected boolean isXML() {
		return true;
	}
	
	@Override
	public String getFontAwesome() {	
		return "code";
	}
	
	@Override
	protected boolean getColumnableDefaultValue() {
		return true;
	}
	
}
