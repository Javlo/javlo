/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.portlet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.portlet.PortletManager;
import org.javlo.portlet.PortletWindowImpl;
import org.javlo.service.RequestService;

/**
 * @author plemarchand
 */
public class PortletWrapperComponent extends AbstractPortletWrapperComponent {

	protected static Logger logger = Logger.getLogger(PortletWrapperComponent.class.getName());

	public static final String PORTLET_NAME_FIELD = "portlet_name";

	// useless, actually, or maybe for a call to super for raw data...	
	static final List<String> FIELDS = Arrays.asList(new String[] {  PORTLET_NAME_FIELD, AbstractPortletWrapperComponent.PORTLET_VALUE_FIELD });

	@Override
	public String getType() {
		return "portlet-wrapper";
	}

	@Override
	public final List<String> getFields(ContentContext ctx) {
		return FIELDS;
	}

	@Override
	public String getPortletName() {
		return getFieldValue(PORTLET_NAME_FIELD);
	}

	@Override
	public String getInitPortletValueEventName() {
		return "initXMLValue";
	}

	@Override
	public String getPortletValueChangedEventName() {
		return "xmlValueChanged";
	}

	@Override
	public String getDeletePortletEventName() {
		return "deleteInstance";
	}

	@Override
	public void refresh(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String portletName = requestService.getParameter(getInputName(PORTLET_NAME_FIELD), null);
		if (portletName != null && portletName.trim().length() > 0) {
			PortletManager pm = PortletManager.getInstance(ctx.getRequest().getSession().getServletContext());

			PortletWindowImpl pw = pm.getPortletWindow(this, ctx);
			if (pw != null && !portletName.equals(pw.getPortletDefinition().getPortletName())) {
				pm.releasePortletWindows(pw.getComponent(), pw.getSession());
			}
			setFieldValue(PORTLET_NAME_FIELD, portletName);
			storeProperties();
			setModify();
		}
		super.refresh(ctx);
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		ServletContext application = ctx.getRequest().getSession().getServletContext();

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		out.println("<div class=\"" + getType() + "\">");
		out.println("<fieldset>");
		out.println("<legend>" + i18nAccess.getText("portlet.meta-config") + "</legend>");
		out.println("<div>");

		String portletInputName = getInputName("portlet_name");
		out.println("<label for=\"" + portletInputName + "\">" + i18nAccess.getText("portlet.name") + " : </label>");
		out.println("<select id=\"" + portletInputName + "\" name=\"" + portletInputName + "\" onchange=\"this.form.submit();\">");

		String thisPortletName = getPortletName();
		for (String portletName : PortletManager.getInstance(application).getPortlets().keySet()) {
			String selected = portletName.equals(thisPortletName) ? " selected=\"selected\"" : "";
			out.println("<option value=\"" + portletName + "\"" + selected + ">" + portletName + "</option>");
		}

		out.println("</select>");
		out.println("</div>");
		out.println("</fieldset>");
		out.println("<fieldset>");
		out.println("<legend>" + i18nAccess.getText("portlet.config") + "</legend>");
		out.println("<div>");

		PortletWindowImpl pw = getPortletWindow(ctx);
		
		String editXHTML = renderPortlet(ctx);
		if (editXHTML != null) {
			out.println(editXHTML);
		} else {
			out.println("<p>no portlet found, try to refresh page, select another one or contact administrator</p>");
		}

		out.println("</div>");
		out.println("</fieldset>");
		out.println("</div>");
		out.close();
		return writer.toString();
	}
}
