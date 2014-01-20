/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.text;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.StringRemplacementHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.XMLManipulationHelper;
import org.javlo.helper.XMLManipulationHelper.TagDescription;

/**
 * @author pvandermaesen
 */
public class XHTML extends AbstractVisualComponent {

	public static final String TYPE = "xhtml";

	public static final String XHTML_RESOURCE_FOLDER = "_xhtml_resources";

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_EASY;
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

	/*@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "";
	}*/

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return XHTMLHelper.replaceJSTLData(ctx, XHTMLHelper.replaceLinks(ctx, getValue()));
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return !isRepeat();
	}

}
