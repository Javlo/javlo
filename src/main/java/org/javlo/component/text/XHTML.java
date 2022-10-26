/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.text;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.service.ReverseLinkService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * @author pvandermaesen
 */
public class XHTML extends AbstractVisualComponent {

	public static final String RAW_STYLE = "raw";

	private static final String[] STYLES = new String[] { "parsed", RAW_STYLE };

	public static final String TYPE = "xhtml";

	public static final String XHTML_RESOURCE_FOLDER = "_xhtml_resources";

	private Boolean cachable = null;

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		out.println(getSpecialInputTag());
		out.println("<fieldset><legend>XHMTL</legend><textarea class=\"form-control text-editor\" data-mode=\"text/html\" data-ext=\"html\" id=\"" + getContentName() + "\" name=\"" + getContentName() + "\"");
		out.println(" rows=\"" + (countLine() + 1) + "\">");
		out.println(XHTMLHelper.escapeXHTML(getValue()));
		out.println("</textarea></fieldset>");

		out.close();
		return writer.toString();
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
	 * @Override public String getPrefixViewXHTMLCode(ContentContext ctx) { return
	 * ""; }
	 * 
	 * @Override public String getSuffixViewXHTMLCode(ContentContext ctx) { return
	 * ""; }
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
		if (!getStyle().equals(RAW_STYLE)) {
			return getValue();
		} else {
			String xhtml = getValue();
			if (xhtml.toLowerCase().contains("<body")) {
				Document doc = Jsoup.parse(xhtml);
				Elements body = doc.select("body");
				xhtml = body.html();
			}
			xhtml = ReverseLinkService.getInstance(ctx.getGlobalContext()).replaceLink(ctx, null, xhtml);
			return XHTMLHelper.replaceLinks(ctx, XHTMLHelper.replaceJSTLData(ctx, xhtml));
		}
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

	@Override
	public String getStyleLabel(ContentContext ctx) {
		return "mode";
	}

	public String[] getStyleList(ContentContext ctx) {
		return STYLES;
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		return STYLES;
	}

}
