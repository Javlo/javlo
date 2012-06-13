/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;

import org.javlo.component.core.ComplexPropertiesLink;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IInternalLink;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.RequestService;


/**
 * @author pvandermaesen
 */
public class ChangeLanguageLink extends ComplexPropertiesLink implements IInternalLink {

	private static final String TITLE = "title";

	private static final String IMAGE = "image";

	private static final String DESCRIPTION = "description";

	private static final String TITLE_IMAGE = TITLE + '+' + IMAGE;

	private static final String TITLE_DESCRIPTION = TITLE + '+' + DESCRIPTION;

	private static final String TITLE_IMAGE_DESCRIPTION = TITLE + '+' + IMAGE + '+' + DESCRIPTION;

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
		return super.getPrefixViewXHTMLCode(ctx) + "<div class=\"" + getType() + "\" >";
	}

	@Override
	public String getSufixViewXHTMLCode(ContentContext ctx) {
		return "</div>" + super.getSufixViewXHTMLCode(ctx);
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String linkId = properties.getProperty(LINK_KEY, globalContext.getDefaultLanguages().iterator().next());
		String label = properties.getProperty(LABEL_KEY, linkId);
		ContentContext ctxLg = new ContentContext(ctx);
		if (globalContext.getLanguages().contains(linkId)) {
			ctxLg.setLanguage(linkId);
			ctxLg.setContentLanguage(linkId);
			ctxLg.setRequestContentLanguage(linkId);
		} else {
			ctxLg.setContentLanguage(linkId);
			ctxLg.setRequestContentLanguage(linkId);
		}

		return "<a href=\""+URLHelper.createURL(ctxLg)+"\" />"+label+"</a>";
	}

	protected String getParam() throws Exception {
		return "";
	}

	@Override
	public String getLinkId() {
		return properties.getProperty(LINK_KEY, null);
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		String link = properties.getProperty(LINK_KEY, globalContext.getDefaultLanguages().iterator().next());
		String label = properties.getProperty(LABEL_KEY, "");

		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String linkTitle = i18nAccess.getText("component.link.link");
			String labelTitle = i18nAccess.getText("component.link.label");

			out.println("<table class=\"edit\"><tr><td style=\"text-align: center;\" width=\"50%\">");
			out.println(linkTitle + " : ");

			Set<String> languages = globalContext.getContentLanguages();

			out.println("<select name=\"" + getLinkName() + "\">");
			for (String lg : languages) {
				if (link.equals(lg)) {
					out.println("<option selected=\"selected\" value=\"" + lg + "\">");
				} else {
					out.println("<option value=\"" + lg + "\">");
				}
				out.println(lg);
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
	public String getType() {
		return "change-language-link";
	}

	@Override
	public void performEdit(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		String label = requestService.getParameter(getLinkLabelName(), null);
		String link = requestService.getParameter(getLinkName(), globalContext.getDefaultLanguages().iterator().next());

		if (link != null) {
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

}
