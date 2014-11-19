/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import org.javlo.component.core.ComplexPropertiesLink;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ILink;
import org.javlo.component.core.IReverseLinkComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.service.RequestService;
import org.javlo.service.ReverseLinkService;

/**
 * @author pvandermaesen
 */
public class ExternalLink extends ComplexPropertiesLink implements IReverseLinkComponent, ILink {

	private static final String REVERSE_LINK_KEY = "reverse-link";
	public static final String TYPE = "external-link";

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {
		super.init(bean, newContext);

		/* check if the content of db is correct version */
		if (getValue().trim().length() > 0) {
			properties.load(stringToStream(getValue()));
		} else {
			properties.setProperty(LINK_KEY, "");
			properties.setProperty(LABEL_KEY, "");
			properties.setProperty(REVERSE_LINK_KEY, ReverseLinkService.NONE);
		}
	}

	public String getLabel() {
		String label = properties.getProperty(LABEL_KEY, "");
		String link = properties.getProperty(LINK_KEY, "");
		if (label.trim().length() == 0) {
			label = link;
		}
		return label;
	}

	@Override
	public boolean isHidden(ContentContext ctx) {
		if (getStyle(ctx) != null) {
			return getStyle(ctx).equals("hidden");
		}
		return false;
	}
	
	private String getLink() {
		String link = properties.getProperty(LINK_KEY, "");
		if (link.contains("@") && !link.contains("http://") && !link.contains("https://")) {
			link = "mailto:"+link;
		}
		return link;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		ctx.getRequest().setAttribute("link", getLink());
		ctx.getRequest().setAttribute("label", getLabel());
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (!isHidden(ctx)) {
			String link = getLink();

			StringBuffer res = new StringBuffer();
			String cssClass = getStyle(ctx);
			String insertCssClass = "";
			if (cssClass != null) {
				insertCssClass = cssClass;
			}

			String target = "";
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			if (globalContext.isOpenExernalLinkAsPopup(link)) {
				target = "target=\"_blank\" ";
				String title = I18nAccess.getInstance(ctx).getViewText("global.newwindow","");
				if (title.trim().length() > 0) {
					target = target+"title=\""+title+"\" ";
				}
			}

			res.append("<a" + getSpecialPreviewCssClass(ctx, insertCssClass) + getSpecialPreviewCssId(ctx) + " " + target + "href=\"");
			res.append(link);
			res.append("\">");
			res.append(getLabel());
			if (getConfig(ctx).getProperty("wai.mark-external-link", null) != null && StringHelper.isTrue(getConfig(ctx).getProperty("wai.mark-external-link", "false"))) {
				res.append("<span class=\"wai\">" + I18nAccess.getInstance(ctx.getRequest()).getViewText("wai.external-link") + "</span>");
			}
			res.append("</a>");
			return res.toString();
		} else {
			return "";
		}
	}

	@Override
	public boolean isInline() {
		return true;
	}

	public String getReverseLinkName() {
		return getId() + ID_SEPARATOR + "reverse-lnk";
	}

	public String getDownloadTitleInputName() {
		return "download_title" + ID_SEPARATOR + getId();
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		String link = properties.getProperty(LINK_KEY, "");
		String label = properties.getProperty(LABEL_KEY, "");

		out.println(getSpecialInputTag());

		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String linkTitle = i18nAccess.getText("component.link.link");
			String labelTitle = i18nAccess.getText("component.link.label");
			String reverseLinkLabel = i18nAccess.getText("component.link.reverse");

			out.println("<div class=\"edit three-col-layout\"><div class=\"line\">");
			String reverseLink = properties.getProperty(REVERSE_LINK_KEY, ReverseLinkService.NONE);

			// 1.3 to 1.4 conversion from legacy value "true" to corresponding
			// "all"
			if (StringHelper.isTrue(reverseLink)) {
				reverseLink = ReverseLinkService.ALL;
			}
			out.println("<label for=\"" + getReverseLinkName() + "\">" + reverseLinkLabel + " : </label>");
			out.println(XHTMLHelper.getReverlinkSelectType(ctx, getReverseLinkName(), reverseLink));
			out.println("</div><div class=\"line\">");
			out.println("<label for=\"" + getLinkName() + "\">" + linkTitle + "</label>");
			out.print("<input class=\"form-control\" id=\"" + getLinkName() + "\" name=\"" + getLinkName() + "\" value=\"");
			out.print(link);
			out.println("\"/></div><div class=\"line suffix\">");
			out.print("<label for=\"" + getLinkLabelName() + "\">" + labelTitle + "</label>");
			out.print("<input class=\"form-control\" id=\"" + getLinkLabelName() + "\" name=\"" + getLinkLabelName() + "\" value=\""+label+"\" />");			
			out.println("<input class=\"btn btn-primary\" type=\"submit\" name=\"" + getDownloadTitleInputName() + "\" value=\"" + i18nAccess.getText("action.read-title") + "\" />");
			out.println("</div></div>");
		} catch (Exception e) {
			e.printStackTrace();
		}

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		// validation

		if (link.trim().length() > 0) {
			if (!PatternHelper.EXTERNAL_LINK_PATTERN.matcher(link).matches()) {
				setMessage(new GenericMessage(i18nAccess.getText("component.error.external-link"), GenericMessage.ERROR));
			}
		} else {
			setMessage(new GenericMessage(i18nAccess.getText("component.message.help.external_link"), GenericMessage.HELP));
		}

		return writer.toString();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String label = requestService.getParameter(getLinkLabelName(), null);
		String link = requestService.getParameter(getLinkName(), "");
		String reverseLinkName = requestService.getParameter(getReverseLinkName(), ReverseLinkService.NONE);

		if (requestService.getParameter(getDownloadTitleInputName(), null) != null) {
			String url = link;			
			if (!StringHelper.isURL(url)) {
				url = getLinkURL(ctx);
			}
			if (StringHelper.isURL(url)) {
				try {
					label = NetHelper.getPageTitle(NetHelper.readPage(new URL(url)));
					setNeedRefresh(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if (label != null) {
			if (link != null) {

				link = link.trim();
				if (StringHelper.isURL(link)) {
					// Complete url
				} else if (link.equals("#")) {
					// Dummy url
				} else if (link.startsWith("/")) {
					// Absolute site URL
				} else if (!link.contains(".") && !link.contains("/")) {
					// Page name
				} else {
					// Bad link
					link = "http://" + link;
					setNeedRefresh(true);
				}

				setModify();
				properties.setProperty(LINK_KEY, link);
				properties.setProperty(LABEL_KEY, label);
				if (!reverseLinkName.equals(properties.getProperty(REVERSE_LINK_KEY))) {
					properties.setProperty(REVERSE_LINK_KEY, reverseLinkName);
					setModify();
					GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
					ReverseLinkService reverlinkService = ReverseLinkService.getInstance(globalContext);
					reverlinkService.clearCache();
				}
			}
			storeProperties();
		}
	}

	public static void main(String[] args) {
		String link = "http//www.google.com";
		if (PatternHelper.EXTERNAL_LINK_PATTERN.matcher(link).matches()) {
			System.out.println("[ExternalLink.java]-[main]-MATCH"); /* TRACE */
		} else {
			System.out.println("[ExternalLink.java]-[main]-NOT MATCH"); /* TRACE */
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
	public String getLinkURL(ContentContext ctx) {
		return properties.getProperty(LINK_KEY, "");
	}

	@Override
	public String getLinkText(ContentContext ctx) {
		try {
			return getLabel();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public boolean isReverseLink() {
		String reverseLinkValue = properties.getProperty(REVERSE_LINK_KEY, ReverseLinkService.NONE);
		boolean outIsReverseLink = ReverseLinkService.LINK_TYPES.contains(reverseLinkValue);
		// return StringHelper.isTrue(reverseLinkValue) || outIsReverseLink;
		// //TODO: check why we do that ?
		return outIsReverseLink;
	}

	@Override
	public boolean isOnlyFirstOccurrence() {
		return ReverseLinkService.ONLY_FIRST.equals(properties.getProperty(REVERSE_LINK_KEY));
	}

	@Override
	public boolean isOnlyThisPage() {
		return properties.getProperty(REVERSE_LINK_KEY, "none").equals(ReverseLinkService.ONLY_THIS_PAGE);
	}
	
	@Override
	public boolean isOnlyPreviousComponent() {
		return properties.getProperty(REVERSE_LINK_KEY, "none").equals(ReverseLinkService.ONLY_PREVIOUS_COMPONENT);
	}	

	@Override
	public String getURL(ContentContext ctx) {
		return XHTMLHelper.escapeXHTML(properties.getProperty(LINK_KEY, ""));
	}

	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		super.initContent(ctx);
		properties.setProperty(LINK_KEY, "http://www.javlo.org");
		properties.setProperty(LABEL_KEY, "javlo.org");
		storeProperties();
		setModify();
		return true;
	}
}
