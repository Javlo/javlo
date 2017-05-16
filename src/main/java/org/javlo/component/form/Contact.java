/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.form;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ICSS;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.mailing.MailConfig;
import org.javlo.mailing.MailService;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 */
public class Contact extends AbstractVisualComponent implements ICSS, IAction {

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return "<div id=\""+getPreviewCssId(ctx)+"\" class=\"" + getPreviewCssClass(ctx, getType()) + "\" >";
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "</div>";
	}

	protected static List<String> getFields() {
		List<String> res = new LinkedList<String>();
		res.add("email");
		res.add("subject");
		res.add("body");
		return res;
	}

	protected static List<String> getCompulsoryField() {
		List<String> res = new LinkedList<String>();
		res.add("email");
		return res;
	}

	protected List<String> getSelectedFields() {
		String[] values = StringHelper.stringToArray(getValue());
		List<String> res = new LinkedList<String>();
		if (values.length > 1) {
			for (int i = 1; i < values.length; i++) {
				res.add(values[i]);
			}
		}
		return res;
	}

	protected String getEmail() {
		String[] values = StringHelper.stringToArray(getValue());
		if (values.length >= 1) {
			return values[0];
		}
		return "";
	}

	protected String getFieldInput(String field, String id, String value) {
		if (value == null) {
			value = "";
		}
		value = StringEscapeUtils.escapeXml(value);

		if (field.equals("body")) {
			return "<textarea class=\"form-control\" id=\"" + id + "\" name=\"" + id + "\">" + value + "</textarea>";
		} else {
			return "<input class=\"form-control\" type=\"text\" id=\"" + id + "\" name=\"" + id + "\" value=\"" + value + "\" />";
		}
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		MessageRepository messageRepository = MessageRepository.getInstance(ctx);
		GenericMessage msg = messageRepository.getGlobalMessage();
		String firstMessageClass = "";
		if (msg.getMessage().length() == 0) {
			firstMessageClass = "first-message ";
			msg = new GenericMessage("contact.intro", GenericMessage.INFO);
		}

		out.println("<div class=\"" + firstMessageClass + "alert alert-"+ msg.getBootstrapType() +" message message-" + msg.getTypeLabel() + "\">");
		out.println(i18nAccess.getContentViewText(msg.getMessage()));
		out.println("</div>");

		if ((messageRepository.getGlobalMessage().getMessage().length() == 0) || (messageRepository.getGlobalMessage().getType() == GenericMessage.ERROR)) {
			out.println("<form method=\"post\" action=\"" + URLHelper.createURL(ctx) + "\">");
			out.println("<input type=\"hidden\" name=\"send-to\" value=\"" + StringEscapeUtils.escapeXml(getEmail()) + "\" />");
			out.println("<input type=\"hidden\" name=\"webaction\" value=\"contact.send\" />");
			List<String> fields = getSelectedFields();
			RequestService requestService = RequestService.getInstance(ctx.getRequest());
			boolean compulsoryFound = false;
			for (String field : fields) {
				out.println("<div class=\"line form-group\">");
				out.print("<label for=\"" + field + "\">");
				out.print(i18nAccess.getContentViewText("field." + field));
				if (getCompulsoryField().contains(field)) {
					out.print(" *");
					compulsoryFound = true;
				}
				out.println("</label>");
				out.println(getFieldInput(field, field, requestService.getParameter(field, "")));
				out.println("</div>");
			}
			out.println("<div class=\"action\">");
			out.println("<input type=\"submit\" class=\"btn btn-primary pull-right\" value=\"" + i18nAccess.getContentViewText("global.send") + "\"/>");
			out.println("</div>");
			if (compulsoryFound) {
				out.println("<div class=\"message-permanent\">" + i18nAccess.getContentViewText("global.compulsory-field") + "</div>");
			}
			out.println("</form>");
		}

		out.close();
		return res.toString();
	}

	protected String getInputNameString() {
		return "report-email-" + getId();
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		List<String> fields = getFields();
		List<String> selectedFields = getSelectedFields();
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		out.println("<fieldset>");
		out.println("<legend>");
		out.println(i18nAccess.getText("content.contact.email-to-send"));
		out.println("</legend>");
		out.println("<input style=\"width: 220px;\" type=\"text\" id=\"" + getInputNameString() + "\" name=\"" + getInputNameString() + "\" value=\"" + getEmail() + "\"/>");
		out.println("</fieldset>");
		out.println("<fieldset>");
		out.println("<legend>");
		out.println(i18nAccess.getText("content.contact.fields"));
		out.println("</legend>");

		for (String field : fields) {
			String inputId = field + '-' + getId();
			String checked = "";
			if (selectedFields.contains(field)) {
				checked = "checked=\"checked\"";
			}
			out.println("<input type=\"checkbox\" id=\"" + inputId + "\" name=\"" + inputId + "\" " + checked + "/> <label for=\"" + inputId + "\">" + i18nAccess.getText("field." + field) + "</label> ");

		}
		out.println("</fieldset>");
		out.close();
		return res.toString();
	}

	@Override
	public String getType() {
		return "contact";
	}

	@Override
	public String getCSSCode(ServletContext application) throws Exception {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);
		out.println(".contact label { float: left; width: 150px; }");
		out.println(".contact .line { margin-top: 8px; }");
		out.println(".contact input { border: 1px #aaaaaa solid; }");
		out.println(".contact .action { margin-left: 150px; margin-top: 5px; }");
		out.println(".contact .message { margin-top: 10px; margin-bottom: 10px; padding: 5px; text-align: center; color: #ffffff;}");
		out.println(".contact .message-error { background-color: #ff9999; border: 1px #ff4444 solid; }");
		out.println(".contact .message-info { background-color: #339933; border: 1px #44cc44 solid; }");
		out.println(".contact .message-permanent { padding: 3px 3px 3px 150px; color: #000000; font-size: 0.9em; }");
		out.close();
		return res.toString();
	}

	@Override
	public String performEdit(ContentContext ctx) {

		String rawInfo = "";
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String email = requestService.getParameter(getInputNameString(), "");
		rawInfo = email;

		List<String> fields = getFields();
		for (String field : fields) {
			String inputId = field + '-' + getId();
			if (requestService.getParameter(inputId, null) != null) {
				rawInfo = rawInfo + StringHelper.DEFAULT_SEPARATOR + field;
			}
		}
		if (!getValue().equals(rawInfo)) {
			setModify();
			setValue(rawInfo);
		}
		
		return null;
	}

	@Override
	public String getActionGroupName() {
		return "contact";
	}

	public static GenericMessage validation(String field, String value) {
		if (field.equals("email")) {
			if (!PatternHelper.MAIL_PATTERN.matcher(value).matches()) {
				return new GenericMessage("contact.email-error", GenericMessage.ERROR);
			}
		}
		return null;
	}

	public static String performSend(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);

		String targetMail = requestService.getParameter("send-to", null);				
		if (targetMail != null) {
			List<String> fields = getFields();
			GlobalContext globalContext = GlobalContext.getInstance(request);
			String title = "Contact : " + globalContext.getContextKey();
			Map<String,String> dataMap = new HashMap<String,String>();			
			for (String field : fields) {
				String info = requestService.getParameter(field, null);
				if (info != null) {
					GenericMessage genericMessage = validation(field, info);
					if (genericMessage != null) {
						MessageRepository messageRepository = MessageRepository.getInstance(ctx);
						messageRepository.setGlobalMessage(genericMessage);
						return null;
					}
					dataMap.put(field, info);					
				}
			}
			String mail = XHTMLHelper.createAdminMail(title, "", dataMap, URLHelper.createURL(ctx.getContextForAbsoluteURL()), globalContext.getGlobalTitle(), "");
			MailService mailService = MailService.getInstance(new MailConfig(globalContext, StaticConfig.getInstance(request.getSession()), null));
			try {
				InternetAddress to = new InternetAddress(targetMail);
				InternetAddress from = new InternetAddress(globalContext.getAdministratorEmail());				
				mailService.sendMail(from, to, title, mail, true);
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				GenericMessage genericMessage = new GenericMessage("contact.send-ok", GenericMessage.INFO);
				messageRepository.setGlobalMessage(genericMessage);
			} catch (MessagingException e) {
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				GenericMessage genericMessage = new GenericMessage("contact.technical-error", GenericMessage.ERROR);
				messageRepository.setGlobalMessage(genericMessage);
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_STANDARD;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

	@Override
	public String getHexColor() {
		return WEB2_COLOR;
	}

}