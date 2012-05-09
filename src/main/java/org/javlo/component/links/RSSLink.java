/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.javlo.component.core.ComplexPropertiesLink;
import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.helper.NavigationHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 */
public class RSSLink extends ComplexPropertiesLink {

	public static final String TYPE = "rss-link";

	public static final String NOT_VISIBLE = "not-visible";

	public static final String VISIBLE = "visible";

	public String getChannel() {
		return properties.getProperty(LINK_KEY, "");
	}

	@Override
	public int getComplexityLevel() {
		return COMPLEXITY_STANDARD;
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		ContentService content = ContentService.createContent(ctx.getRequest());

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		String currentChannel = properties.getProperty(LINK_KEY, "");
		String label = properties.getProperty(LABEL_KEY, "");

		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String linkTitle = i18nAccess.getText("component.rss.channel");
			String labelTitle = i18nAccess.getText("component.link.label");

			out.println("<table class=\"edit\"><tr><td style=\"text-align: center;\" width=\"50%\">");
			out.println(linkTitle + " : ");
			out.println("<select name=\"" + getLinkName() + "\">");
			MenuElement elem = content.getNavigation(ctx);
			List<String> channels = NavigationHelper.getAllRSSChannels(ctx, elem);
			for (String channel : channels) {
				if (currentChannel.equals(channel)) {
					out.println("<option selected=\"true\">");
				} else {
					out.println("<option>");
				}
				out.println(channel);
				out.println("</option>");
			}
			out.println("</select>");
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
	public String getHexColor() {
		return LINK_COLOR;
	}

	public String getLinkId() {
		return properties.getProperty(LINK_KEY, null);
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		if (getStyle(ctx).equals(NOT_VISIBLE)) {
			return "";
		}
		return super.getPrefixViewXHTMLCode(ctx) + "<div class=\"" + getType() + "\" >";
	}

	public String getRSSURL(ContentContext ctx) throws Exception {
		ContentContext viewCtx = new ContentContext(ctx);
		String channel = getChannel();
		if (ctx.getRenderMode() != ContentContext.PAGE_MODE) {
			viewCtx.setRenderMode(ContentContext.VIEW_MODE);
		}
		return URLHelper.createRSSURL(viewCtx, channel);
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		try {
			I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
			return new String[] { i18n.getText("global.hidden"), i18n.getText("global.visible") };
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return getStyleList(ctx);
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { NOT_VISIBLE, VISIBLE };
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		return "";
	}

	@Override
	public String getSufixViewXHTMLCode(ContentContext ctx) {
		if (getStyle(ctx).equals(NOT_VISIBLE)) {
			return "";
		}
		return "</div>" + super.getSufixViewXHTMLCode(ctx);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		if (getStyle(ctx).equals(NOT_VISIBLE)) {
			return "";
		}

		String label = properties.getProperty(LABEL_KEY, "");

		StringBuffer res = new StringBuffer();
		res.append("<a " + getSpecialPreviewCssClass(ctx, getStyle(ctx)) + getSpecialPreviewCssId(ctx) + " href=\" ");
		res.append(getRSSURL(ctx));
		res.append("\">");
		res.append(XHTMLHelper.getIconesCode(ctx, "feed.png", "rss"));
		res.append(label);
		res.append("</a>");
		return res.toString();
	}

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
	public boolean isListable() {
		return true;
	}

	/*
	 * public String getHelpURL(String lang) { return HELP_URL_HOST+"/view/"+lang+"/components/internlink.html"; }
	 */

	@Override
	public void refresh(ContentContext ctx) {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		String label = requestService.getParameter(getLinkLabelName(), null);
		String channel = requestService.getParameter(getLinkName(), "");

		if (label != null) {
			String oldChannel = properties.getProperty(LINK_KEY, "");
			String oldLabel = properties.getProperty(LABEL_KEY, "");
			if ((!label.equals(oldLabel)) || (!oldChannel.equals(channel))) {
				properties.setProperty(LINK_KEY, channel);
				properties.setProperty(LABEL_KEY, label);
				storeProperties();
				setModify();
			}
		}
	}

}
