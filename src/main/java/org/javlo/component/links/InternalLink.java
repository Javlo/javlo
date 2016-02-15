/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import org.javlo.component.core.ComplexPropertiesLink;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IInternalLink;
import org.javlo.component.core.ILink;
import org.javlo.component.core.IReverseLinkComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.XHTMLNavigationHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.module.mailing.MailingAction;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.NavigationService;
import org.javlo.service.RequestService;
import org.javlo.service.ReverseLinkService;

/**
 * @author pvandermaesen
 */
public class InternalLink extends ComplexPropertiesLink implements IInternalLink, IReverseLinkComponent, ILink {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(InternalLink.class.getName());

	private static final String HIDDEN = "hidden";

	private static final String TITLE = "title";

	private static final String IMAGE = "image";

	private static final String DESCRIPTION = "description";

	private static final String TITLE_IMAGE = TITLE + '+' + IMAGE;

	private static final String TITLE_DESCRIPTION = TITLE + '+' + DESCRIPTION;

	private static final String TITLE_IMAGE_DESCRIPTION = TITLE + '+' + IMAGE + '+' + DESCRIPTION;

	private static final String REVERSE_LINK_KEY = "reverse-link";

	public static final String TYPE = "internal-link";

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		ContentService content = ContentService.getInstance(ctx.getRequest());

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		String linkIdStr = properties.getProperty(LINK_KEY, null);
		String label = getLabel();

		String link = "/";
		if (linkIdStr != null) {
			// MenuElement elemChild =
			// content.getNavigation(ctx).searchChildFromId(linkIdStr);
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			NavigationService navigationService = NavigationService.getInstance(globalContext);
			MenuElement elemChild = navigationService.getPage(ctx, linkIdStr);
			if (elemChild != null) {
				link = elemChild.getPath();
			}
		}

		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String linkTitle = i18nAccess.getText("component.link.link");
			String labelTitle = i18nAccess.getText("component.link.label");
			String reverseLinkLabel = i18nAccess.getText("component.link.reverse");

			if (isReversedLink(ctx)) {
				String reverseLink = properties.getProperty(REVERSE_LINK_KEY, null);
				if (reverseLink == null) {
					reverseLink = "none";
				}
				out.println("<div class=\"input-group\"><label for=\"" + getReverseLinkName() + "\">" + reverseLinkLabel + " : </label>");
				out.println(XHTMLHelper.getReverlinkSelectType(ctx, getReverseLinkName(), reverseLink));
				out.println("</div>");
			}

			out.println("<label for=\"" + getLinkName() + "\">");

			out.println(linkTitle + " : ");
			out.println("</label>");
			out.println(XHTMLNavigationHelper.renderComboNavigation(ctx, content.getNavigation(ctx), getLinkName(), link, true));
			/*out.println("<select class=\"form-control\" id=\"" + getLinkName() + "\" name=\"" + getLinkName() + "\">");
			MenuElement elem = content.getNavigation(ctx);
			String[] values = elem.getChildList();
			String currentLink = null;
			for (String value : values) {
				if (link.equals(value)) {
					currentLink = value;
					out.println("<option selected=\"selected\" value=\"" + value + "\">");
				} else {
					out.println("<option value=\"" + value + "\">");
				}
				out.println(value);
				out.println("</option>");
			}
			out.println("</select>");*/
			/*if (currentLink != null) {
				out.print("<a href=\"");
				out.print(URLHelper.createURL(ctx, currentLink));
				out.println("\">&nbsp;&gt;&gt;</a>");
				setMessage(null);
			} else {
				setMessage(new GenericMessage(i18nAccess.getText("component.message.help.choose_link"), GenericMessage.HELP));
			}*/
			out.println("<div class=\"input-group\"><label for=\"" + getLinkLabelName() + "\">");
			out.print(labelTitle);
			out.print(" : </label>");
			out.println(XHTMLHelper.getTextInput(getLinkLabelName(), label, "form-control"));
			out.println("</div>");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return writer.toString();
	}

	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

	public String getLabel() {
		return properties.getProperty(LABEL_KEY, "");
	}

	@Override
	public String getLinkId() {
		return properties.getProperty(LINK_KEY, null);
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
	public String getLinkURL(ContentContext ctx) {
		String linkId = properties.getProperty(LINK_KEY, "/");
		NavigationService navigationService;
		try {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			navigationService = NavigationService.getInstance(globalContext);
			MenuElement child = navigationService.getPage(ctx, linkId);
			if (child != null) {
				String link = "#";
				link = child.getPath();
				return URLHelper.createURL(ctx, link);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	protected String getParam() throws Exception {
		return "";
	}

	public String getReverseLinkName() {
		return getId() + ID_SEPARATOR + "reverse-lnk";
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		try {
			if (super.getStyleLabelList(ctx).length > 0) {
				return super.getStyleLabelList(ctx);
			} else {
				I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				String pf = "content.internal-link.";
				String title = i18nAccess.getText(pf + TITLE);
				String titleImage = i18nAccess.getText(pf + TITLE_IMAGE);
				String titleCreation = i18nAccess.getText(pf + TITLE_DESCRIPTION);
				String titleImageDescription = i18nAccess.getText(pf + TITLE_IMAGE_DESCRIPTION);
				String image = i18nAccess.getText(pf + IMAGE);
				return new String[] { title, titleImage, titleCreation, titleImageDescription, image, i18nAccess.getText("global.hidden") };
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String[] { TITLE, TITLE_IMAGE, TITLE_DESCRIPTION, TITLE_IMAGE_DESCRIPTION, IMAGE, HIDDEN };
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		if (super.getStyleList(ctx).length > 0) {
			return super.getStyleList(ctx);
		} else {
			return new String[] { TITLE, TITLE_IMAGE, TITLE_DESCRIPTION, TITLE_IMAGE_DESCRIPTION, IMAGE, HIDDEN };
		}
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
		String style = getStyle();
		if (style == null) {
			style = TITLE;
		}

		if (style.equals(HIDDEN)) {
			return "";
		}

		StringBuffer res = new StringBuffer();

		String linkId = properties.getProperty(LINK_KEY, "/");

		// MenuElement child = nav.searchChildFromId(linkId);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		NavigationService navigationService = NavigationService.getInstance(globalContext);
		MenuElement linkedPage = navigationService.getPage(ctx, linkId);
		if (linkedPage != null) {
			String link = "#";
			link = linkedPage.getPath();
			String label = properties.getProperty(LABEL_KEY, "");
			if (label.trim().length() == 0) {
				label = linkedPage.getLabel(ctx);
			}			
			String url = URLHelper.createURL(ctx, linkedPage);
			if (style.contains(DESCRIPTION) || style.contains(IMAGE)) {
				res.append("<div class=\"details\">");
				res.append("<div class=\"title\">");
			}

			String title = I18nAccess.getInstance(ctx).getViewText("global.gotopage", "");
			if (title.trim().length() > 0) {
				title = " title=\"" + StringHelper.toXMLAttribute(title) + "\"";
			}

			res.append("<a " + getSpecialPreviewCssClass(ctx, "") + getSpecialPreviewCssId(ctx) + " href=\" ");
			if (ctx.getRenderMode() != ContentContext.PAGE_MODE) {
				res.append(StringHelper.toXMLAttribute(url));
			} else {
				ContentContext viewCtx = new ContentContext(ctx);
				viewCtx.setRenderMode(ContentContext.VIEW_MODE);
				url = URLHelper.createURL(viewCtx, link);
				if (getParam().trim().length() == 0) {
					res.append(StringHelper.toXMLAttribute(url) + "?" + MailingAction.MAILING_FEEDBACK_PARAM_NAME + "=" + MailingAction.MAILING_FEEDBACK_VALUE_NAME);
				} else {
					res.append(StringHelper.toXMLAttribute(url) + getParam() + "&" + MailingAction.MAILING_FEEDBACK_PARAM_NAME + "=" + MailingAction.MAILING_FEEDBACK_VALUE_NAME);
				}
			}
			res.append("\"" + title + ">");
			res.append(label);
			res.append("</a>");
			if (style.contains(DESCRIPTION) || style.contains(IMAGE)) {
				res.append("</div>");
				res.append("<div class=\"body\"><p>");
			}
			if (style.contains(IMAGE)) {
				res.append("<a " + getSpecialPreviewCssClass(ctx, getStyle(ctx)) + getSpecialPreviewCssId(ctx) + " href=\"");
				if (ctx.getRenderMode() != ContentContext.PAGE_MODE) {
					res.append(url);
				} else {
					ContentContext viewCtx = new ContentContext(ctx);
					viewCtx.setRenderMode(ContentContext.VIEW_MODE);
					url = URLHelper.createURL(viewCtx, link);
					if (getParam().trim().length() == 0) {
						res.append(url + "?" + MailingAction.MAILING_FEEDBACK_PARAM_NAME + "=" + MailingAction.MAILING_FEEDBACK_VALUE_NAME);
					} else {
						res.append(url + getParam() + "&" + MailingAction.MAILING_FEEDBACK_PARAM_NAME + "=" + MailingAction.MAILING_FEEDBACK_VALUE_NAME);
					}
				}
				res.append("\">");
				if (linkedPage.getImage(ctx) != null) {
					res.append("<img src=\"" + ElementaryURLHelper.createTransformURL(ctx, linkedPage, linkedPage.getImage(ctx).getResourceURL(ctx), "internal-link") + "\" alt=\"" + linkedPage.getImage(ctx).getImageDescription(ctx) + "\" />");
				}
				res.append("</a>");
			}
			if (style.contains(DESCRIPTION) && linkedPage.getDescription(ctx).trim().length() > 0) {
				res.append(linkedPage.getDescription(ctx));
			}
			if (style.contains(DESCRIPTION) || style.contains(IMAGE)) {
				res.append("</p></div>");
				res.append("<div class=\"content_clear\"><span>&nbsp;</span></div>");
				res.append("</div>");
			}
		}
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

	@Override
	public boolean isOnlyFirstOccurrence() {
		return properties.getProperty(REVERSE_LINK_KEY, "none").equals(ReverseLinkService.ONLY_FIRST);
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
	public boolean isReverseLink() {
		boolean outIsReverseLink = !(properties.getProperty(REVERSE_LINK_KEY, "none").equals("none") || properties.getProperty(REVERSE_LINK_KEY, "").equals(""));
		return outIsReverseLink;
	}

	@Override
	public String performEdit(ContentContext ctx) {

		ContentService content = ContentService.getInstance(ctx.getRequest());

		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		String label = requestService.getParameter(getLinkLabelName(), null);
		String link = requestService.getParameter(getLinkName(), "/");

		if (label != null) {

			try {
				MenuElement elem = content.getNavigation(ctx).searchRealChild(ctx, link);
				String idLink = "";
				if (elem != null) {
					idLink = elem.getId();
				} else {
					logger.warning("link not found : " + link);
				}
				String linkIdStr = properties.getProperty(LINK_KEY, "");
				String oldLabel = getLabel();
				if ((!label.equals(oldLabel)) || (!linkIdStr.equals(idLink))) {

					if (linkIdStr.trim().length() == 0) {
						setNeedRefresh(true);
					}

					setModify();
					properties.setProperty(LINK_KEY, idLink);
					properties.setProperty(LABEL_KEY, label.replace("\"", "&quot;"));
				}
				String reverseLinkValue = requestService.getParameter(getReverseLinkName(), null);
				if (reverseLinkValue != null && !properties.getProperty(REVERSE_LINK_KEY, "").equals(reverseLinkValue)) {
					properties.setProperty(REVERSE_LINK_KEY, reverseLinkValue);
					setModify();
					GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
					ReverseLinkService reverselinkService = ReverseLinkService.getInstance(globalContext);
					reverselinkService.clearCache();
				}
				if (isModify()) {
					storeProperties();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String getURL(ContentContext ctx) throws Exception {
		String linkId = properties.getProperty(LINK_KEY, "/");

		// MenuElement child = nav.searchChildFromId(linkId);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		NavigationService navigationService = NavigationService.getInstance(globalContext);
		MenuElement child = navigationService.getPage(ctx, linkId);
		if (child != null) {
			return URLHelper.createURL(ctx, child);
		} else {
			return null;
		}
	}

	@Override
	protected Object getLock(ContentContext ctx) {
		return ctx.getGlobalContext().getLockLoadContent();
	}
	
	@Override
	public String getListGroup() {
		return "link";
	}

}
