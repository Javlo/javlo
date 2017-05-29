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
import org.javlo.helper.XMLHelper;
import org.javlo.helper.XMLManipulationHelper;
import org.javlo.helper.XMLManipulationHelper.BadXMLException;
import org.jsoup.Jsoup;
import org.owasp.encoder.Encode;

/**
 * @author pvandermaesen
 */
public class XHTML extends AbstractVisualComponent {

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
		out.println("<textarea class=\"resizable-textarea\" id=\"" + getContentName() + "\" name=\"" + getContentName() + "\"");
		out.println(" rows=\"" + (countLine() + 1) + "\">");
		out.println(XHTMLHelper.escapeXHTML(getValue()));
		out.println("</textarea>");

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
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return XHTMLHelper.replaceLinks(ctx, XHTMLHelper.replaceJSTLData(ctx, getValue()));
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
	
}
