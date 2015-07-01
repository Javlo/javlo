/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedList;

import org.javlo.component.core.ComplexPropertiesLink;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 */
public class AnchorLink extends ComplexPropertiesLink {

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {
		super.init(bean, newContext);
		properties.load(stringToStream(getValue()));
	}

	@Override
	public boolean isInline() {
		return true;
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return super.getPrefixViewXHTMLCode(ctx) + "<div" + getSpecialPreviewCssClass(ctx, getStyle(ctx)) + getSpecialPreviewCssId(ctx) + ">";
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "</div>" + super.getSuffixViewXHTMLCode(ctx);
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		String link = "#" + properties.getProperty(LINK_KEY, "/");
		String label = properties.getProperty(LABEL_KEY, "");

		StringBuffer res = new StringBuffer();
		String url = URLHelper.createURL(ctx) + link;
		res.append("<a href=\" ");
		res.append(url);
		res.append("\">");
		res.append(label);
		res.append("</a>");
		return res.toString();
	}

	private String[] searchAnchor(ContentContext ctx, MenuElement elem) throws Exception {
		ContentElementList content = elem.getContent(ctx);

		Collection<String> outAnchor = new LinkedList<String>();

		while (content.hasNext(ctx)) {
			IContentVisualComponent comp = content.next(ctx);
			if (comp.getType().equals("anchor")) {
				outAnchor.add(comp.getValue(ctx));
			}
		}

		String[] outAnchorArray = new String[outAnchor.size()];
		outAnchor.toArray(outAnchorArray);

		return outAnchorArray;
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		ContentService content = ContentService.getInstance(ctx.getRequest());

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		String link = properties.getProperty(LINK_KEY, null);
		String label = properties.getProperty(LABEL_KEY, "");

		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String linkTitle = i18nAccess.getText("component.link.link");
			String labelTitle = i18nAccess.getText("component.link.label");

			out.println("<table class=\"edit\"><tr><td style=\"text-align: center;\" width=\"50%\">");
			out.println(linkTitle + " : ");
			out.println("<select name=\"" + getLinkName() + "\">");
			MenuElement elem = ctx.getCurrentPage();
			String[] values = searchAnchor(ctx, elem);
			String currentLink = null;
			for (String value : values) {
				if ((link != null) && (link.equals(value))) {
					currentLink = value;
					out.println("<option selected=\"true\">");
				} else {
					out.println("<option value=\"" + value + "\">");
				}
				out.println(value);
				out.println("</option>");
			}
			out.println("</select>");
			if (currentLink != null) {
				out.print("<a href=\"#");
				out.print(currentLink);
				out.println("\">&nbsp;&gt;&gt;</a>");
			} else {
				setMessage(new GenericMessage(i18nAccess.getText("component.message.help.choose_link"), GenericMessage.HELP));
			}
			out.println("</td><td style=\"text-align: center;\" align=\"center\">");
			out.print(labelTitle);
			out.print(" : ");
			out.println(XHTMLHelper.getTextInput(getLinkLabelName(), label));
			out.println("</td></tr></table>");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return writer.toString();
	}

	@Override
	public String getType() {
		return "anchor-link";
	}

	@Override
	public void performEdit(ContentContext ctx) {

		RequestService requestSercice = RequestService.getInstance(ctx.getRequest());

		String link = requestSercice.getParameter(getLinkName(), "");
		String label = requestSercice.getParameter(getLinkLabelName(), link);

		if (link.trim().length() > 0) {

			try {
				String linkIdStr = properties.getProperty(LINK_KEY, "");
				String oldLabel = properties.getProperty(LABEL_KEY, "");
				if ((!label.equals(oldLabel)) || (!linkIdStr.equals(link))) {
					setModify();
					properties.setProperty(LINK_KEY, link);
					properties.setProperty(LABEL_KEY, label);
					storeProperties();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

}
