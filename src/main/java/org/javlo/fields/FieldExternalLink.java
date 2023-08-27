package org.javlo.fields;

import org.javlo.component.core.ILink;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Logger;

public class FieldExternalLink extends MetaField implements ILink {

	private static Logger logger = Logger.getLogger(FieldExternalLink.class.getName());

	public class ExternalLinkBean extends FieldBean {

		public ExternalLinkBean(ContentContext ctx) {
			super(ctx);
		}
		
		@Override
		public String getUrl() {
			try {
				String url = getCurrentLink();
				url = URLHelper.convertLink(ctx, url);
				return url;
			} catch (Exception e) {
				e.printStackTrace();
				return "error:"+e.getMessage();
			}
		}

		@Override		
		public String getURL() {
			return getUrl();
		}

		public String getTitle() {
			return getCurrentLabel();
		}
		
		public String getLinkAttribute() {
			if (URLHelper.isAbsoluteURL(getUrl()) && ctx.getGlobalContext().isOpenExternalLinkAsPopup()) {
				return "target=\"_blank\"";
			} else {
				return "";
			}
		}

	}

	private Date latestValidDate = null;

	protected String getLabelLabel() {
		return getI18nAccess().getText("global.label");
	}

	protected String getLinkLabel() {
		return getI18nAccess().getText("global.link");
	}

	public String getInputLinkName() {
		return getName() + "-link-" + getId();
	}

	@Override
	public String getInputLabelName() {
		return getName() + "-label-" + getId();
	}
	
	@Override
	public boolean isWrapped() {	
		return true;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		String displayStr = StringHelper.neverNull(getCurrentLink());
		if (displayStr.trim().length() == 0 || !isViewDisplayed()) {			
			return "";
		}

		String label = getCurrentLabel();
		if (label.trim().length() == 0) {
			label = getCurrentLink();
		}

		String link = getCurrentLink().trim();
		link=URLHelper.convertLink(ctx, link);

		if (link.startsWith(URLHelper.ERROR_PREFIX)) {
			if (ctx.isAsPreviewMode()) {
				return XHTMLHelper.getAgnosticMessageHtml("bad link : "+link);
			} else {
				return "<!-- bad link : "+link+" -->";
			}
		}

		if (label.trim().length() > 0) {
			out.println("<span class=\"" + getType() + "\">");
			String target = "";			
			if (!link.startsWith("/") && GlobalContext.getInstance(ctx.getRequest()).isOpenExternalLinkAsPopup(link)) {
				target = " target=\"_blank\"";
			}

			label = XHTMLHelper.replaceJSTLData(ctx, label);
			out.println("<a href=\"" + link + "\"" + target + ">" + label + "</a>");
			out.println("</span>");
		}

		out.close();
		return writer.toString();
	}

	@Override
	public String getEditXHTMLCode(ContentContext ctx, boolean search) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		
		out.println("<div class=\"form-group\"><fieldset>");
		out.println("<legend>" + getUserLabel(ctx, ctx.getLocale()) + "</legend>");
		out.println("<div class=\"row\"><div class=\"col-sm-6\">");
		out.println("<div class=\"row form-group\"><div class=\"col-sm-2\">");
		out.println("<label for=\"" + getInputLinkName() + "\">" + getLinkLabel() + "</label></div>");
		out.println("<div class=\"col-sm-10\"><input class=\"form-control\" id=\"" + getInputLinkName() + "\" name=\"" + getInputLinkName() + "\" value=\"" + StringHelper.neverNull(getCurrentLink()) + "\"/></div></div>");
		if (getCurrentLinkErrorMessage().trim().length() > 0) {
			out.println("<div class=\"alert alert-danger\" role=\"alert\">");
			out.println(getCurrentLinkErrorMessage());
			out.println("</div>");
		}
		out.println("</div><div class=\"col-sm-6\">");
		out.println("<div class=\"row form-group\"><div class=\"col-sm-2\">");
		out.println("<label for=\"" + getInputLabelName() + "\">" + getLabelLabel() + "</label></div>");
		out.println("<div class=\"col-sm-10\"><input class=\"form-control\" id=\"" + getInputLabelName() + "\" name=\"" + getInputLabelName() + "\" value=\"" + StringHelper.neverNull(getCurrentLabel()) + "\"/></div>");
		out.println("</div>");
		out.println("</div></div>");
		out.println("</fieldset></div>");

		out.close();
		return writer.toString();
	}

	@Override
	public boolean process(ContentContext ctx) {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		boolean modify = false;

		String newLabel = requestService.getParameter(getInputLabelName(), "");
		if (!newLabel.equals(getCurrentLabel())) {
			setCurrentLabel(newLabel);
			modify = true;
		}

		String newLink = requestService.getParameter(getInputLinkName(), "");
		if (!newLink.equals(getCurrentLink())) { 			
			if (!StringHelper.isEmpty(newLink) && !URLHelper.isCorrectJavloLink(newLink)) {
				if (getCurrentLinkErrorMessage().trim().length() == 0) {
					setNeedRefresh(true);
				}
				setCurrentLinkErrorMessage(getI18nAccess().getText("component.error.external-link"));
			} else {
				if (getCurrentLinkErrorMessage().trim().length() > 0) {
					setNeedRefresh(true);
				}
				setCurrentLinkErrorMessage("");
			}

			setCurrentLink(newLink);
			modify = true;
		}

		return modify;
	}

	@Override
	public String getType() {
		return "external-link";
	}

	/* values */

	protected String getCurrentLink() {
		return properties.getProperty("field." + getUnicName() + ".value.link", "");
	}

	public void setCurrentLink(String link) {
		properties.setProperty("field." + getUnicName() + ".value.link", link);
	}

	protected String getCurrentLinkErrorMessage() {
		return properties.getProperty("field." + getUnicName() + ".message.link", "");
	}

	protected void setCurrentLinkErrorMessage(String message) {
		properties.setProperty("field." + getUnicName() + ".message.link", message);
	}

	protected String getCurrentLabel() {
		return properties.getProperty("field." + getUnicName() + ".value.label", "");
	}

	protected void setCurrentLabel(String label) {
		properties.setProperty("field." + getUnicName() + ".value.label", label);
	}

	@Override
	public boolean isPertinent(ContentContext ctx) {
		return getCurrentLink() != null && getCurrentLink().trim().length() > 0;
	}

	@Override
	public boolean isContentCachable() {
		return getCurrentLink() == null || !getCurrentLink().trim().startsWith("/");
	}

	@Override
	public boolean isPublished(ContentContext ctx) {
		String link = getCurrentLink().trim();
		if (link.startsWith("/") && !link.startsWith("//")) { // relative link
			try {
				link = XHTMLHelper.replaceJSTLData(ctx, link);
				MenuElement page = ContentService.getInstance(GlobalContext.getInstance(ctx.getRequest())).getNavigation(ctx).searchRealChild(ctx, link);
				boolean published = page != null && page.isRealContent(ctx);
				if (!published) {
					logger.warning("page not found with content : " + link);
				}
				return published;
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}
		return true;
	}

	@Override
	public String getURL(ContentContext ctx) throws Exception {
		return URLHelper.convertLink(ctx, getCurrentLink());
	}
	
	@Override
	public boolean isLinkValid(ContentContext ctx) throws Exception {
		return !StringHelper.isEmpty(getURL(ctx));
	}

	@Override
	protected FieldBean newFieldBean(ContentContext ctx) {
		return new ExternalLinkBean(ctx);
	}
	
	
	@Override
	public void setLatestValidDate(Date date) {
		latestValidDate  = date;
	}

	@Override
	public Date getLatestValidDate() {
		return latestValidDate;
	}

}
