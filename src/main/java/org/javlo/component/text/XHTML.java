/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.text;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.StringRemplacementHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.XMLManipulationHelper;
import org.javlo.helper.XMLManipulationHelper.TagDescription;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 */
public class XHTML extends AbstractVisualComponent {

	public static final String TYPE = "xhtml";

	public static final String XHTML_RESOURCE_FOLDER = "_xhtml_resources";

	@Override
	public int getComplexityLevel() {
		return COMPLEXITY_ADMIN;
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
		return TEXT_COLOR;
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	@Override
	public String getSufixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	public String getType() {
		return TYPE;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		String outContent = XHTMLHelper.replaceJSTLData(ctx, getValue());

		TagDescription[] tags = XMLManipulationHelper.searchAllTag(outContent, false);
		StringRemplacementHelper remplacement = new StringRemplacementHelper();

		for (TagDescription tag : tags) {
			if (tag.getName().equalsIgnoreCase("a")) {
				if (tag.getName().equalsIgnoreCase("a")) {
					String hrefValue = tag.getAttributes().get("href");
					if (hrefValue != null) {
						if (hrefValue.toLowerCase().startsWith("rss")) {
							String channel = "";
							if (hrefValue.contains(":")) {
								channel = hrefValue.split(":")[1];
							}
							hrefValue = URLHelper.createRSSURL(ctx, channel);
							tag.getAttributes().put("href", hrefValue);
						} else if ((hrefValue != null) && (!StringHelper.isURL(hrefValue)) && (!StringHelper.isMailURL(hrefValue))) {
							hrefValue = URLHelper.createURLCheckLg(ctx, hrefValue);
							tag.getAttributes().put("href", hrefValue);
						}
						remplacement.addReplacement(tag.getOpenStart(), tag.getOpenEnd() + 1, tag.toString());
					}
				}
			}
		}
		
		return remplacement.start(outContent);
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return !isRepeat();
	}

}
