package org.javlo.fields;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLNavigationHelper;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

public class FieldInternalLink extends Field {

	protected String getCurrentLabel() {
		return properties.getProperty("field." + getUnicName() + ".value.label", "");
	}

	protected String getCurrentLink() {
		return properties.getProperty("field." + getUnicName() + ".value.link", "");
	}

	protected String getCurrentLinkErrorMessage() {
		return properties.getProperty("field." + getUnicName() + ".message.link", "");
	}

	@Override
	public String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		out.println("<fieldset>");
		out.println("<legend>" + getUserLabel(new Locale(ctx.getRequestContentLanguage())) + "</legend>");

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getInputLinkName() + "\">" + getLinkLabel() + " : </label>");

		ContentService content = ContentService.getInstance(ctx.getRequest());
		out.println(XHTMLNavigationHelper.renderComboNavigation(ctx, content.getNavigation(ctx), getInputLinkName(), getCurrentLink(), true));

		out.println("</div>");

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getInputLabelName() + "\">" + getLabelLabel() + " : </label>");
		out.println("<input class=\"form-control\" id=\"" + getInputLabelName() + "\" name=\"" + getInputLabelName() + "\" value=\"" + StringHelper.neverNull(getCurrentLabel()) + "\"/>");
		out.println("</div>");

		out.println("</fieldset>");

		out.close();
		return writer.toString();
	}

	@Override
	public String getInputLabelName() {
		return getName() + "-label-" + getId();
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
			out.println("<a href=\"" + URLHelper.createURL(ctx, getCurrentLink()) + "\">" + label + "</a>");
			out.println("</span>");
		}

		out.close();
		return writer.toString();
	}

	@Override
	public boolean process(HttpServletRequest request) {
		RequestService requestService = RequestService.getInstance(request);

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

		return modify;
	}

	protected void setCurrentLabel(String label) {
		properties.setProperty("field." + getUnicName() + ".value.label", label);
	}

	protected void setCurrentLink(String link) {
		properties.setProperty("field." + getUnicName() + ".value.link", link);
	}

	protected void setCurrentLinkErrorMessage(String message) {
		properties.setProperty("field." + getUnicName() + ".message.link", message);
	}

}
