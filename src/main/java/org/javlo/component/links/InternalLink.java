/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
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
import org.javlo.module.mailing.MailingAction;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.PageBean;
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

	public static final String TYPE = "internal-link";

	private Date latestValidDate;

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		ContentService content = ContentService.getInstance(ctx.getRequest());

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		String linkIdStr = properties.getProperty(LINK_KEY, null);
		String label = getLabel();

		String link = "/";
		MenuElement page = null;
		if (linkIdStr != null) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			NavigationService navigationService = NavigationService.getInstance(globalContext);
			page = navigationService.getPage(ctx, linkIdStr);
			if (page != null) {
				link = page.getPath();
			}
		}

		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String reverseLinkLabel = i18nAccess.getText("component.link.reverse");
			out.println("<div class=\"row\">");
			if (isReversedLink(ctx)) {
				String reverseLink = properties.getProperty(REVERSE_LINK_KEY, null);
				if (reverseLink == null) {
					reverseLink = "none";
				}
				out.println("<div class=\"col-md-3\">");
				out.println("<div class=\"form-group\"><label for=\"" + getReverseLinkName() + "\">" + reverseLinkLabel + " : </label>");
				out.println(XHTMLHelper.getReverlinkSelectType(ctx, getReverseLinkName(), reverseLink));
				out.println("</div>");
				out.println("</div>");
			}
			
			out.println("<div class=\"col-md-3\">");
			out.println("<label for=\""+getLinkLabelName()+"\">"+i18nAccess.getText("component.link.label")+"</label>");
			out.println(XHTMLHelper.getTextInput(getLinkLabelName(), label, "form-control"));
			out.println("</div>");
			
			out.println("<div class=\"col-md-3\">");
			out.println("<label for=\""+getLinkName()+"\">"+i18nAccess.getText("component.link.id", "page id")+"</label>");
			String value="";
			if (!StringHelper.isEmpty(getLinkId())) {
				value = getLinkId(); 
			}
			out.println("<input class=\"form-control\" type=\"text\" name=\""+getLinkName()+"\" id=\""+getLinkName()+"\" value=\""+value+"\" />");
			out.println("</div><div class=\"col-md-3\">");
			out.println("<label for=\""+getInputName("anchor")+"\">"+i18nAccess.getText("component.anchor.label", "anchor")+"</label>");
			out.println(XHTMLHelper.getTextInput(getInputName("anchor"), getField("anchor"), "form-control"));
			out.println("</div></div>");
			
			out.println("<script>function selectPage"+getId()+"(id) {jQuery('#"+getLinkName()+"').val(id); jQuery('#form-content').submit(); }</script>");
			if (page != null) {
				out.println("<div class=\"current-page\">");
				out.println(XHTMLNavigationHelper.renderPageResult(ctx, page, null));
				out.println("</div>");
			}
			out.println(XHTMLNavigationHelper.renderComboNavigationAjax(ctx, content.getNavigation(ctx), getLinkName(), link, "selectPage"+getId()));
			
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
				return URLHelper.addAnchor(URLHelper.createURL(ctx.getContentContextForInternalLink(), link), getField("anchor"));
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
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		NavigationService navigationService = NavigationService.getInstance(globalContext);
		String linkId = properties.getProperty(LINK_KEY, "/");
		MenuElement linkedPage = navigationService.getPage(ctx, linkId);
		if (linkedPage != null) {
			PageBean page = linkedPage.getPageBean(ctx);
			ctx.getRequest().setAttribute("page", page);
			ctx.getRequest().setAttribute("linkedPage", linkedPage);
			String label = properties.getProperty(LABEL_KEY, "");
			if (label.trim().length() == 0) {
				label = linkedPage.getLabel(ctx);
			}
			ctx.getRequest().setAttribute("textLabel", properties.getProperty(LABEL_KEY, ""));
			ctx.getRequest().setAttribute("label", label);
			String url = URLHelper.createURL(ctx.getContentContextForInternalLink(), linkedPage);
			ctx.getRequest().setAttribute("url", URLHelper.addAnchor(url, getField("anchor")));
		}
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

			res.append("<a " + getPrefixCssClass(ctx, "") + getSpecialPreviewCssId(ctx) + getDataAttributes(ctx) + " href=\" ");
			if (ctx.getRenderMode() != ContentContext.PAGE_MODE) {
				res.append(StringHelper.toXMLAttribute(url));
			} else {
				ContentContext viewCtx = new ContentContext(ctx);
				viewCtx.setRenderMode(ContentContext.VIEW_MODE);
				url = URLHelper.createURL(viewCtx, link);
				url = URLHelper.addAnchor(url, getField("anchor"));
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
				res.append("<a " + getPrefixCssClass(ctx, getComponentCssClass(ctx)) + getSpecialPreviewCssId(ctx) + " href=\"");
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
			if (style.contains(DESCRIPTION) && linkedPage.getDescriptionAsText(ctx).trim().length() > 0) {
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
		performColumnable(ctx);

		String label = requestService.getParameter(getLinkLabelName(), null);
		String link = requestService.getParameter(getLinkName(), "/");
		String anchor = requestService.getParameter(getInputName("anchor"), "");

		if (label != null) {

			try {
				MenuElement elem = content.getNavigation(ctx).searchChildFromId(link);
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
				
				if (!anchor.equals(getField("anchor"))) {
					setField("anchor", anchor);
					setModify();
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
	public boolean isLinkValid(ContentContext ctx) throws Exception {
		return !StringHelper.isEmpty(getURL(ctx));
	}

	@Override
	protected Object getLock(ContentContext ctx) {
		return ctx.getGlobalContext().getLockLoadContent();
	}
	
	@Override
	public String getListGroup() {
		return "link";
	}
	
	@Override
	public boolean isMirroredByDefault(ContentContext ctx) {	
		return true;
	}
	
	
	@Override
	public void setLatestValidDate(Date date) {
		latestValidDate = date;
	}

	@Override
	public Date getLatestValidDate() {
		return latestValidDate;
	}
	
//	@Override
//	public String getFontAwesome() {	
//		return "link";
//	}
	
	@Override
	public String getIcon() {
		return "bi bi-link";
	}
	
	@Override
	protected boolean getColumnableDefaultValue() {
		return true;
	}
	

}
