package org.javlo.fields;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.XHTMLNavigationHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.service.google.translation.ITranslator;

import java.io.PrintWriter;
import java.io.StringWriter;

public class FieldInternalLink extends Field {
	
	public class InternalLinkBean extends FieldBean {
		
		public InternalLinkBean(ContentContext ctx) {
			super(ctx);
		}

		private String internalLink = null;
		private String linkLabel = null;
		private String param;
		private String linkOn;

		public String getLink() {
			return internalLink;
		}

		public String getUrl() {
			return internalLink;
		}

		public void setLink(String internalLink) {
			this.internalLink = internalLink;
		}

		public String getLinkLabel() {
			return linkLabel;
		}

		public void setLinkLabel(String linkLabel) {
			this.linkLabel = linkLabel;
		}

		public String getParam() {
			return param;
		}

		public void setParam(String param) {
			this.param = param;
		}

		public String getLinkOn() {
			return linkOn;
		}

		public void setLinkOn(String linkOn) {
			this.linkOn = linkOn;
		}

	}

	@Override
	protected boolean isValueTranslatable() {
		return true;
	}

	public boolean transflateFrom(ContentContext ctx, ITranslator translator, String lang) {
		if (!isValueTranslatable()) {
			return false;
		} else {
			boolean translated = false;
			String newValue="";
			if (!StringHelper.isEmpty(getCurrentLabel())) {
				translated = true;
				newValue = translator.translate(ctx, getCurrentLabel(), lang, ctx.getRequestContentLanguage());
				if (newValue == null) {
					translated=false;
					newValue = ITranslator.ERROR_PREFIX+getCurrentLabel();
				}
				setCurrentLabel(newValue);
				try {
					getReferenceComponent(ctx).storeProperties();
					getReferenceComponent(ctx).setModify();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
            return translated;
		}
	}

	protected FieldBean newFieldBean(ContentContext ctx) {
		InternalLinkBean bean = new InternalLinkBean(ctx);		
		bean.setLinkLabel(getCurrentLabel());
		if (!StringHelper.isEmpty(getCurrentLink())) {
			String url = createLink(ctx);
			bean.setLink(url);
            try {
                MenuElement page = ctx.getCurrentPage().getRoot().searchChildFromName(getCurrentLink());
				if (page.isRealContent(ctx)) {
					bean.setLinkOn(url);
				} else {
					bean.setLinkOn(page.getLinkOn(ctx));
				}
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
		bean.setParam(getCurrentParam());
		return bean;
	}
	
	protected String createLink(ContentContext ctx) {
		String param = getCurrentParam();
		String url = null;
		try {
			MenuElement page = ctx.getCurrentPage().getRoot().searchChildFromName(getCurrentLink());
			if (page == null) {
				page = ctx.getGlobalContext().getPageIfExist(ctx, getCurrentLink(), false);
			}
			if (page != null) {
				url = URLHelper.createURL(ctx, page);
			}
			if (url == null) {
				url = URLHelper.createURL(ctx.getContentContextForInternalLink(), getCurrentLink());
			}
			if (!StringHelper.isEmpty(param)) {
				if (url.contains("?")) {
					param = param.replace('?', '&');
				}
				try {
					param = XHTMLHelper.replaceJSTLData(ctx, param);
				} catch (Exception e) {
					e.printStackTrace();
				}
				url += param;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return url;
	}
	
	protected String getCurrentLabel() {
		return properties.getProperty("field." + getUnicName() + ".value.label", "");
	}
	
	protected String getCurrentParam() {
		return properties.getProperty("field." + getUnicName() + ".value.param", "");
	}

	protected String getCurrentLink() {
		return properties.getProperty("field." + getUnicName() + ".value.link", "");
	}

	protected String getCurrentLinkErrorMessage() {
		return properties.getProperty("field." + getUnicName() + ".message.link", "");
	}

	@Override
	public String getEditXHTMLCode(ContentContext ctx, boolean search) throws Exception {
		
		String refCode = referenceEditCode(ctx);
		if (refCode != null) {
			return refCode;
		}	
		
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		out.println("<fieldset>");
		out.println("<legend>" + getUserLabel(ctx, ctx.getLocale()) + "</legend>");

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getInputLinkName() + "\">" + getLinkLabel() + " : </label>");

		ContentService content = ContentService.getInstance(ctx.getRequest());
		String pageName =  getCurrentLink();

		/** transform link to page name (old content version) **/
		if (pageName.contains("/")) {
			MenuElement page = ctx.getGlobalContext().getPageIfExist(ctx, pageName, false);
			if (page != null) {
				pageName = page.getName();
			}
		}

		out.println(XHTMLNavigationHelper.renderComboNavigationWidthName(ctx, content.getNavigation(ctx), getInputLinkName(), pageName, true));

		out.println("</div>");

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getInputLabelName() + "\">" + getLabelLabel() + " : </label>");
		out.println("<input class=\"form-control\" id=\"" + getInputLabelName() + "\" name=\"" + getInputLabelName() + "\" value=\"" + StringHelper.neverNull(getCurrentLabel()) + "\"/>");
		out.println("</div>");
		
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getInputParamName() + "\">" + getParamLabel() + " : </label>");
		out.println("<input class=\"form-control\" id=\"" + getInputParamName() + "\" name=\"" + getInputParamName() + "\" value=\"" + StringHelper.neverNull(getCurrentParam()) + "\"/>");
		out.println("</div>");

		out.println("</fieldset>");

		out.close();
		return writer.toString();
	}

	@Override
	public String getInputLabelName() {
		return getName() + "-label-" + getId();
	}
	
	public String getInputParamName() {
		return getName() + "-param-" + getId();
	}

	public String getInputLinkName() {
		return getName() + "-link-" + getId();
	}

	protected String getLabelLabel() {
		return getI18nAccess().getText("global.label");
	}

	protected String getLinkLabel() {
		return getI18nAccess().getText("global.link");
	}

	protected String getParamLabel() {
		return getI18nAccess().getText("global.param", "params");
	}

	/* values */

	@Override
	public String getType() {
		return "internal-link";
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

		if (label.trim().length() > 0) {
			out.println("<span class=\"" + getType() + "\">");
			out.println("<a href=\"" + createLink(ctx) + "\">" + label + "</a>");
			out.println("</span>");
		} else {
			out.println("<!-- no label on internal-link : "+getId()+" -->");
		}

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
			setCurrentLink(newLink);
			modify = true;
		}
		
		String newParam = requestService.getParameter(getInputParamName(), "");
		if (!newLink.equals(getCurrentParam())) {
			setCurrentParam(newParam);
			modify = true;
		}

		return modify;
	}

	protected void setCurrentLabel(String label) {
		properties.setProperty("field." + getUnicName() + ".value.label", label);
	}

	protected void setCurrentLink(String link) {
		properties.setProperty("field." + getUnicName() + ".value.link", link);
	}
	
	protected void setCurrentParam(String link) {
		properties.setProperty("field." + getUnicName() + ".value.param", link);
	}


	protected void setCurrentLinkErrorMessage(String message) {
		properties.setProperty("field." + getUnicName() + ".message.link", message);
	}

}
