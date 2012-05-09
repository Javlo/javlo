/*
 * Created on 06-janv.-2004
 */
package org.javlo.component.form;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.RequestHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.StringSecurityUtil;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.mailing.MailingManager;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserFactory;
import org.javlo.user.UserInfos;
import org.javlo.user.exception.UserAllreadyExistException;

/**
 * @author pvandermaesen
 */
public class UserRegistrationComponent extends AbstractVisualComponent implements IAction {

	private boolean needForm = true;

	private static final String DS = "<<>>";
	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(UserRegistrationComponent.class.getName());

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getType()
	 */
	@Override
	public String getType() {
		return "form-mailing";
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ctx.getRequest().setAttribute("comp", this);
		includePage(ctx, "/jsp/registration/registration.jsp?comp-id=" + getId());
		needForm = true;
		return "";
	}

	/**
	 *
	 * @see org.javlo.component.AbstractVisualComponent#init(java.lang.String, java.lang.String, org.javlo.ContentContext)
	 */
	@Override
	protected void init(ComponentBean bean, ContentContext ctx) throws Exception {
		super.init(bean, ctx);
		needForm = true;
	}

	/*
	 * edit management
	 */

	protected String getInputAddRolesName() {
		return "__" + getId() + ID_SEPARATOR + "add-roles";
	}

	protected String getInputRemoveRolesName() {
		return "__" + getId() + ID_SEPARATOR + "remove-roles";
	}

	protected String getInputNameName() {
		return "__" + getId() + ID_SEPARATOR + "name";
	}

	protected String getInputNameNameNeeded() {
		return "__" + getId() + ID_SEPARATOR + "name_needed";
	}

	protected String getInputPasswordName() {
		return "__" + getId() + ID_SEPARATOR + "pwd";
	}

	protected String getInputPasswordNameNeeded() {
		return "__" + getId() + ID_SEPARATOR + "pwd_needed";
	}

	protected String getInputOrganizationName() {
		return "__" + getId() + ID_SEPARATOR + "organization";
	}

	protected String getInputOrganizationNameNeeded() {
		return "__" + getId() + ID_SEPARATOR + "organization_needed";
	}

	protected String getInputPhoneName() {
		return "__" + getId() + ID_SEPARATOR + "phone";
	}

	protected String getInputPhoneNameNeeded() {
		return "__" + getId() + ID_SEPARATOR + "phone_needed";
	}

	protected String getInputFreeName() {
		return "__" + getId() + ID_SEPARATOR + "free";
	}

	protected String getInputCommentName() {
		return "__" + getId() + ID_SEPARATOR + "comment";
	}

	protected String getInputFreeNameNeeded() {
		return "__" + getId() + ID_SEPARATOR + "free_needed";
	}

	protected String getInputFreeNameLabel() {
		return "__" + getId() + ID_SEPARATOR + "free_label";
	}

	protected String getInputAdressName() {
		return "__" + getId() + ID_SEPARATOR + "adresse";
	}

	protected String getInputAdressNameNeeded() {
		return "__" + getId() + ID_SEPARATOR + "adresse_needed";
	}

	protected String getInputConfirmEmailName() {
		return "__" + getId() + ID_SEPARATOR + "confirm_email";
	}

	protected String getInputEmailSubjectName() {
		return "__" + getId() + ID_SEPARATOR + "email_subject";
	}

	protected String getInputIntroductionName() {
		return "__" + getId() + ID_SEPARATOR + "introduction";
	}

	private static int BOOLEAN_POS = 5;

	public boolean isUseName() {
		try {
			return StringHelper.isTrue(getValue().split(DS)[BOOLEAN_POS]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	public boolean isNeedName() {
		try {
			return StringHelper.isTrue(getValue().split(DS)[BOOLEAN_POS + 1]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	public boolean isUseAdress() {
		try {
			return StringHelper.isTrue(getValue().split(DS)[BOOLEAN_POS + 2]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	public boolean isNeedAdress() {
		try {
			return StringHelper.isTrue(getValue().split(DS)[BOOLEAN_POS + 3]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	public boolean isUsePassword() {
		try {
			return StringHelper.isTrue(getValue().split(DS)[BOOLEAN_POS + 4]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	public boolean isNeedPassword() {
		try {
			return StringHelper.isTrue(getValue().split(DS)[BOOLEAN_POS + 5]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	public boolean isUsePhone() {
		try {
			return StringHelper.isTrue(getValue().split(DS)[BOOLEAN_POS + 6]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	public boolean isNeedPhone() {
		try {
			return StringHelper.isTrue(getValue().split(DS)[BOOLEAN_POS + 7]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	public boolean isUseFree() {
		try {
			return StringHelper.isTrue(getValue().split(DS)[BOOLEAN_POS + 8]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}


	public boolean isNeedFree() {
		try {
			return StringHelper.isTrue(getValue().split(DS)[BOOLEAN_POS + 9]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	public boolean isNeedForm() {
		return needForm;
	}

	public String getEmailSubject() {
		if (getValue().split(DS).length > 2) {
			String subject = getValue().split(DS)[2];
			if (subject == null) {
				return "";
			} else {
				return subject;
			}
		}
		return "";
	}

	public String getConfirmEmail() {
		if (getValue().split(DS).length > 3) {
			String email = getValue().split(DS)[3];
			if (email.trim().length() == 0) {
				return null;
			} else {
				return email;
			}
		} else {
			return "";
		}
	}

	public String getFreeLabel() {
		if (getValue().split(DS).length > 4) {
			String email = getValue().split(DS)[4];
			if (email.trim().length() == 0) {
				return null;
			} else {
				return email;
			}
		} else {
			return "";
		}
	}

	public String getIntroduction() {
		if (getValue().split(DS).length > 15) {
			String intro = getValue().split(DS)[15];
			if (intro.trim().length() == 0) {
				return null;
			} else {
				return intro;
			}
		} else {
			return "";
		}
	}

	public boolean isUseOrganization() {
		try {
			return StringHelper.isTrue(getValue().split(DS)[BOOLEAN_POS + 11]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	public boolean isNeedOrganization() {
		try {
			return StringHelper.isTrue(getValue().split(DS)[BOOLEAN_POS + 12]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	public boolean isUseComment() {
		try {
			return StringHelper.isTrue(getValue().split(DS)[BOOLEAN_POS + 13]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	/**
	 * @see org.javlo.component.AbstractVisualComponent#getEditXHTMLCode()
	 */
	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
		out.println(getSpecialInputTag());

		out.println(getDebugHeader(ctx));

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getInputIntroductionName() + "\">");
		out.print(i18n.getText("content.form-mailing.introduction"));
		out.println("</label>");
		out.println("<textarea id=\"" + getInputIntroductionName() + "\" name=\"" + getInputIntroductionName() + "\">");
		out.println(StringHelper.neverEmpty(getIntroduction(), ""));
		out.println("</textarea>");
		out.println("</div>");

		out.println("<div class=\"line\">");
		out.print("<label for=\"");
		out.print(getInputAddRolesName());
		out.print("\">");
		out.print(i18n.getText("form.add-role"));
		out.println("</label>");
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		out.print(XHTMLHelper.getInputMultiSelect(getInputAddRolesName(), editCtx.getUserRoles(), getAddRolesAsArray()));
		out.print("<label for=\"");
		out.print(getInputAddRolesName());
		out.print("\">");
		out.print(i18n.getText("form.remove-role"));
		out.println("</label>");
		out.print(XHTMLHelper.getInputMultiSelect(getInputRemoveRolesName(), editCtx.getUserRoles(), getRemoveRolesAsArray()));
		out.println("</div>");

		out.println("<fieldset>");
		out.println("<legend>" + "fields" + "</legend>");
		out.println("<table>");
		out.println("<tr><th>field</th><th>use in form</th><th>requier</th></tr>");
		out.println("<tr>");
		out.println("<td><label for=\"" + getInputNameName() + "\">" + i18n.getText("content.form-mailing.use-name") + "</label></td>");
		out.println("<td><input type=\"checkbox\" id=\"" + getInputNameName() + "\" name=\"" + getInputNameName() + "\"" + (isUseName() ? " checked=\"checked\"" : "") + " /></td>");
		out.println("<td><input type=\"checkbox\" id=\"" + getInputNameNameNeeded() + "\" name=\"" + getInputNameNameNeeded() + "\"" + (isNeedName() ? " checked=\"checked\"" : "") + " /></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td><label for=\"" + getInputOrganizationName() + "\">" + i18n.getText("content.form-mailing.organization") + "</label></td>");
		out.println("<td><input type=\"checkbox\" id=\"" + getInputOrganizationName() + "\" name=\"" + getInputOrganizationName() + "\"" + (isUseOrganization() ? " checked=\"checked\"" : "") + " /></td>");
		out.println("<td><input type=\"checkbox\" id=\"" + getInputOrganizationNameNeeded() + "\" name=\"" + getInputOrganizationNameNeeded() + "\"" + (isNeedOrganization() ? " checked=\"checked\"" : "") + " /></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td><label for=\"" + getInputAdressName() + "\">" + i18n.getText("content.form-mailing.adress") + "</label></td>");
		out.println("<td><input type=\"checkbox\" id=\"" + getInputAdressName() + "\" name=\"" + getInputAdressName() + "\"" + (isUseAdress() ? " checked=\"checked\"" : "") + " /></td>");
		out.println("<td><input type=\"checkbox\" id=\"" + getInputAdressNameNeeded() + "\" name=\"" + getInputAdressNameNeeded() + "\"" + (isNeedAdress() ? " checked=\"checked\"" : "") + " /></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td><label for=\"" + getInputPasswordName() + "\">" + i18n.getText("content.form-mailing.password") + "</label></td>");
		out.println("<td><input type=\"checkbox\" id=\"" + getInputPasswordName() + "\" name=\"" + getInputPasswordName() + "\"" + (isUsePassword() ? " checked=\"checked\"" : "") + " /></td>");
		out.println("<td><input type=\"checkbox\" id=\"" + getInputPasswordNameNeeded() + "\" name=\"" + getInputPasswordNameNeeded() + "\"" + (isNeedPassword() ? " checked=\"checked\"" : "") + " /></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td><label for=\"" + getInputPhoneName() + "\">" + i18n.getText("content.form-mailing.phone") + "</label></td>");
		out.println("<td><input type=\"checkbox\" id=\"" + getInputPhoneName() + "\" name=\"" + getInputPhoneName() + "\"" + (isUsePhone() ? " checked=\"checked\"" : "") + " /></td>");
		out.println("<td><input type=\"checkbox\" id=\"" + getInputPhoneNameNeeded() + "\" name=\"" + getInputPhoneNameNeeded() + "\"" + (isNeedPhone() ? " checked=\"checked\"" : "") + " /></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td><input type=\"text\" name=\"" + getInputFreeNameLabel() + "\" value=\"" + (getFreeLabel() == null ? "" : getFreeLabel()) + "\" /></td>");
		out.println("<td><input type=\"checkbox\" id=\"" + getInputFreeName() + "\" name=\"" + getInputFreeName() + "\"" + (isUseFree() ? " checked=\"checked\"" : "") + " /></td>");
		out.println("<td><input type=\"checkbox\" id=\"" + getInputFreeNameNeeded() + "\" name=\"" + getInputFreeNameNeeded() + "\"" + (isNeedFree() ? " checked=\"checked\"" : "") + " /></td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<td><label for=\"" + getInputCommentName() + "\">" + i18n.getText("content.form-mailing.comment") + "</label></td>");
		out.println("<td><input type=\"checkbox\" id=\"" + getInputCommentName() + "\" name=\"" + getInputCommentName() + "\"" + (isUseComment() ? " checked=\"checked\"" : "") + " /></td>");
		out.println("<td>&nbsp;</td>");
		out.println("</tr>");

		out.println("</table>");
		out.println("</fieldset>");

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getInputEmailSubjectName() + "\">");
		out.print(i18n.getText("field.subject"));
		out.println("</label>");
		out.println("<input id=\"" + getInputEmailSubjectName() + "\" name=\"" + getInputEmailSubjectName() + "\" value=\"" + getEmailSubject() + "\" />");
		out.println("</div>");

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getInputConfirmEmailName() + "\">");
		out.print(i18n.getText("content.form-mailing.confirm-email"));
		out.println("</label>");
		out.println("<textarea id=\"" + getInputConfirmEmailName() + "\" name=\"" + getInputConfirmEmailName() + "\">");
		out.println(StringHelper.neverEmpty(getConfirmEmail(), ""));
		out.println("</textarea>");
		out.println("</div>");
		out.close();

		return new String(outStream.toByteArray());
	}

	@Override
	public void refresh(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String[] addRoles = requestService.getParameterValues(getInputAddRolesName(), new String[0]);
		String[] removeRoles = requestService.getParameterValues(getInputRemoveRolesName(), new String[0]);

		boolean useName = requestService.getParameter(getInputNameName(), null) != null;
		boolean needName = requestService.getParameter(getInputNameNameNeeded(), null) != null;
		boolean useAdress = requestService.getParameter(getInputAdressName(), null) != null;
		boolean needAdress = requestService.getParameter(getInputAdressNameNeeded(), null) != null;
		boolean usePassword = requestService.getParameter(getInputPasswordName(), null) != null;
		boolean needPassword = requestService.getParameter(getInputPasswordNameNeeded(), null) != null;
		boolean usePhone = requestService.getParameter(getInputPhoneName(), null) != null;
		boolean needPhone = requestService.getParameter(getInputPhoneNameNeeded(), null) != null;
		boolean useFree = requestService.getParameter(getInputFreeName(), null) != null;
		boolean needFree = requestService.getParameter(getInputFreeNameNeeded(), null) != null;
		boolean useComment = requestService.getParameter(getInputCommentName(), null) != null;

		String emailConfirm = requestService.getParameter(getInputConfirmEmailName(), "");

		String emailSubject = requestService.getParameter(getInputEmailSubjectName(), null);

		String freeLabel = requestService.getParameter(getInputFreeNameLabel(), "");

		String introduction = requestService.getParameter(getInputIntroductionName(), "");

		boolean useOrganization = requestService.getParameter(getInputOrganizationName(), null) != null;
		boolean needOrganization = requestService.getParameter(getInputOrganizationNameNeeded(), null) != null;

		if (emailSubject != null) {
			String value = StringHelper.arrayToString(addRoles) + DS + StringHelper.arrayToString(removeRoles) + DS + emailSubject + DS + emailConfirm + DS + freeLabel;
			value = value + DS + useName + DS + needName + DS + useAdress + DS + needAdress + DS + usePassword + DS + needPassword + DS + usePhone + DS + needPhone + DS + useFree + DS + needFree;
			value = value + DS + introduction + DS + useOrganization + DS + needOrganization + DS + useComment;

			if (!getValue().equals(value)) {
				setValue(value);
				setModify();
			}
		}
	}

	protected String getAddRolesAsRaw() {
		if (!getValue().contains(DS)) { // old version of the
			// component
			return getValue();
		} else {
			return getValue().split(DS)[0];
		}
	}

	protected Set<String> getAddRoles() {
		Set<String> roles = new HashSet<String>(StringHelper.stringToCollection(getAddRolesAsRaw()));
		return roles;
	}

	protected String[] getAddRolesAsArray() {
		return StringHelper.stringToArrayRemoveEmpty(getAddRolesAsRaw());
	}

	protected String getRemoveRolesAsRaw() {
		if (!getValue().contains(DS)) { // old version of the component
			return "";
		} else {
			return getValue().split(DS)[1];
		}
	}

	protected Set<String> getRemoveRoles() {
		Set<String> roles = new HashSet<String>(StringHelper.stringToCollection(getRemoveRolesAsRaw()));
		return roles;
	}

	protected String[] getRemoveRolesAsArray() {
		return StringHelper.stringToArrayRemoveEmpty(getRemoveRolesAsRaw());
	}

	public boolean isRemoveRoles() {
		return getRemoveRolesAsArray().length > 0;
	}

	/*
	 * ACTION
	 */

	public static String performSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception {

		RequestService requestService = RequestService.getInstance(request);

		String email = requestService.getParameter("em", null);
		String firstName = requestService.getParameter("fn", null);
		String lastName = requestService.getParameter("ln", null);
		String street = requestService.getParameter("s", null);
		String organization = requestService.getParameter("o", null);
		String postcode = requestService.getParameter("z", null);
		String city = requestService.getParameter("c", null);
		String country = requestService.getParameter("ct", null);
		String phone = requestService.getParameter("p", null);
		String password = requestService.getParameter("pw", null);
		String password2 = requestService.getParameter("pw2", "");
		String free = requestService.getParameter("f", "");
		String comment = requestService.getParameter("cm", "");
		String fakefiled = requestService.getParameter("fk", "");
		String compId = requestService.getParameter(COMP_ID_REQUEST_PARAM, null);

		ContentContext ctx = ContentContext.getContentContext(request, response);
		UserRegistrationComponent comp = (UserRegistrationComponent) ContentService.createContent(request).getComponent(ctx, compId);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
		MessageRepository messageRepository = MessageRepository.getInstance(ctx);

		boolean storeUser = comp.getAddRolesAsArray().length > 0 || comp.getRemoveRolesAsArray().length > 0; // if no role define we juste send a email.
		if (!storeUser) {
			request.setAttribute("nostore", "true"); // display other message if no store user.
		}

		if (fakefiled.length() > 0) {
			GenericMessage msg = new GenericMessage("special field is not empty.", GenericMessage.ERROR);
			messageRepository.setGlobalMessage(msg);
			return null;
		}

		/* validation */

		if (email != null && !PatternHelper.MAIL_PATTERN.matcher(email).matches()) {
			GenericMessage msg = new GenericMessage(i18nAccess.getContentViewText("contact.email-error"), GenericMessage.ERROR);
			messageRepository.setGlobalMessage(msg);
			return null;
		}

		if (password != null) {
			if (comp.isNeedPassword() && password.trim().length() == 0) {
				GenericMessage msg = new GenericMessage(i18nAccess.getContentViewText("form.error.password.needed"), GenericMessage.ERROR);
				messageRepository.setGlobalMessage(msg);
				return null;
			}
			if (comp.isNeedPassword() && !password.equals(password2)) {
				GenericMessage msg = new GenericMessage(i18nAccess.getContentViewText("form.error.password2"), GenericMessage.ERROR);
				messageRepository.setGlobalMessage(msg);
				return null;
			}
		}

		if ((comp.isUseOrganization() && comp.isNeedOrganization()) && (organization.length() == 0)) {
			GenericMessage msg = new GenericMessage(i18nAccess.getContentViewText("form.error.organization.needed"), GenericMessage.ERROR);
			messageRepository.setGlobalMessage(msg);
			return null;
		}

		if ((comp.isUseAdress() && comp.isNeedAdress()) && ((street.length() == 0) || (postcode.length() == 0) || (city.length() == 0) || (country.length() == 0))) {
			GenericMessage msg = new GenericMessage(i18nAccess.getContentViewText("form.error.address.needed"), GenericMessage.ERROR);
			messageRepository.setGlobalMessage(msg);
			return null;
		}

		if ((comp.isUsePhone() && comp.isNeedPhone()) && (phone.length() == 0)) {
			GenericMessage msg = new GenericMessage(i18nAccess.getContentViewText("form.error.phone.needed"), GenericMessage.ERROR);
			messageRepository.setGlobalMessage(msg);
			return null;
		}

		String confirmEmail = comp.getConfirmEmail();
		if (confirmEmail != null && request.getParameter("d") == null) {
			StringBuffer urlParam = new StringBuffer("?webaction=" + comp.getActionGroupName() + ".submit");
			logger.info("send email confirmation to : " + email + " (" + firstName + ' ' + lastName + ' ' + organization + ' ' + phone + ')');
			urlParam.append("&em=" + email);
			urlParam.append("&fn=" + firstName);
			urlParam.append("&ln=" + lastName);
			urlParam.append("&s=" + street);
			urlParam.append("&z=" + postcode);
			urlParam.append("&c=" + city);
			urlParam.append("&ct=" + country);
			urlParam.append("&o=" + organization);
			urlParam.append("&p=" + phone);
			urlParam.append("&pw=" + password);
			urlParam.append("&f=" + free);
			urlParam.append("&cm=" + comment);
			urlParam.append("&" + COMP_ID_REQUEST_PARAM + '=' + compId);
			urlParam.append("&d=d");
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext());
			// String encodedParam = StringHelper.encodeBase64ToURLParam(StringSecurityUtil.encode(urlParam.toString(), staticConfig.getSecretKey()));
			String encodedParam = StringSecurityUtil.encode(urlParam.toString(), staticConfig.getSecretKey());
			ContentContext absURLCtx = new ContentContext(ctx);
			absURLCtx.setAbsoluteURL(true);
			String registerURL = URLHelper.createURL(absURLCtx) + '?' + RequestHelper.CRYPTED_PARAM_NAME + "=" + encodedParam + "#reg_" + comp.getId();
			confirmEmail = confirmEmail.replace("##URL##", registerURL);
			logger.info("registration url : " + registerURL);
			if (firstName != null) {
				confirmEmail = confirmEmail.replace("##firstname##", firstName);
			}
			if (lastName != null) {
				confirmEmail = confirmEmail.replace("##lastname##", lastName);
			}

			InternetAddress bcc = null;
			if (comment != null && comment.length() > 0) {
				StringWriter sw = new StringWriter();
				PrintWriter out = new PrintWriter(sw);
				
				out.println(confirmEmail);
				out.println();
				
				out.println(i18nAccess.getViewText("form.comment"));
				out.println(comment);
				
				out.close();
				confirmEmail = sw.toString();
				bcc = new InternetAddress(globalContext.getAdministratorEmail());
			}
			// DEBUG = FileUtils.writeStringToFile(new File ("/tmp/mail.html"),
			// confirmEmail);

			MailingManager mailingManager = MailingManager.getInstance(staticConfig);			
			if (!mailingManager.sendMail(new InternetAddress(globalContext.getAdministratorEmail()), new InternetAddress(email), bcc, comp.getEmailSubject(), confirmEmail, true)) {
				GenericMessage msg = new GenericMessage(i18nAccess.getContentViewText("contact.technical-error"), GenericMessage.ERROR);
				messageRepository.setGlobalMessage(msg);
			} else {
				GenericMessage msg = new GenericMessage(i18nAccess.getContentViewText("user.error.email-send"), GenericMessage.INFO);
				messageRepository.setGlobalMessage(msg);
			}

			comp.needForm = false;

		} else if (comp.getConfirmEmail() == null || request.getAttribute(StringSecurityUtil.REQUEST_ATT_FOR_SECURITY_FORWARD) != null) { // only from crypted param
			if (!PatternHelper.MAIL_PATTERN.matcher(email).matches()) {
				GenericMessage msg = new GenericMessage(i18nAccess.getContentViewText("mailing.error.email"), GenericMessage.ERROR);
				messageRepository.setGlobalMessage(msg);
				logger.warning("user not registred : " + email);
				return null;
			}

			if (storeUser) {
				IUserInfo userInfo = null;
				IUserFactory fact = UserFactory.createUserFactory(globalContext, request.getSession());
				IUserInfo[] userInfos = fact.getUserInfoList();
				for (int i = 0; (i < userInfos.length); i++) {
					String login = email;
					if (userInfos[i].getLogin().equals(login)) {
						Set<String> roles = new HashSet<String>(Arrays.asList(userInfos[i].getRoles()));
						if (roles.containsAll(comp.getAddRoles())) {
							GenericMessage msg = new GenericMessage(i18nAccess.getContentViewText("user.error.email-allready-exist"), GenericMessage.ERROR);
							messageRepository.setGlobalMessage(msg);
							logger.warning("user not registred : " + email);
							return "";
						} else {
							userInfo = userInfos[i];
						}

					}
				}

				// if (fact.getCurrentUser() == null) {
				boolean update = true;
				if (userInfo == null) {
					update = false;
					userInfo = fact.createUserInfos();
					userInfo.setLogin(email);
				}
				userInfo.addRoles(comp.getAddRolesAsArray());

				userInfo.removeRoles(comp.getRemoveRolesAsArray());

				if (comp.getRemoveRolesAsArray().length > 0) {
					for (String role : comp.getRemoveRolesAsArray()) {
						if (role.trim().length() > 0) {
							request.setAttribute("unsubscribe", "true");
						}
					}
				}
				// } else {
				// userInfo = fact.getCurrentUser().getUserInfo();
				// }
				userInfo.setEmail(email);
				userInfo.setFirstName(StringHelper.neverNull(firstName));
				userInfo.setLastName(StringHelper.neverNull(lastName));
				userInfo.setPassword(StringHelper.neverNull(password));
				if (userInfo instanceof UserInfos) {
					UserInfos ui = (UserInfos) userInfo;
					ui.setOrganization(StringHelper.neverNull(organization));
					ui.setAddress(StringHelper.neverNull(street));
					ui.setPostCode(StringHelper.neverNull(postcode));
					ui.setCity(StringHelper.neverNull(city));
					ui.setCountry(StringHelper.neverNull(country));
					ui.setPhone(StringHelper.neverNull(phone));
					ui.setInfo(free);
				}

				try {
					if (update) {
						fact.updateUserInfo(userInfo);
					} else {
						fact.addUserInfo(userInfo);
					}
					request.setAttribute(StringHelper.REQUEST_KEY_FORM_VALID, "true");
					fact.store();
				} catch (UserAllreadyExistException e) {
					GenericMessage msg = new GenericMessage(i18nAccess.getContentViewText("user.error.email-allready-exist"), GenericMessage.ERROR);
					messageRepository.setGlobalMessage(msg);
					logger.warning("user not registred : " + email);
					return "";
				}
			}

			try {
				/** send email to administrator **/

				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				PrintStream out = new PrintStream(outStream);

				out.println("mail send at : " + StringHelper.renderTime(new Date()));
				out.println("context : " + globalContext.getContextKey());
				out.println("");
				out.println("email : " + email);
				out.println("first name : " + firstName);
				out.println("last name : " + lastName);
				out.println("organization : " + StringHelper.neverNull(organization));
				out.println("address : " + StringHelper.neverNull(street));
				out.println("post code : " + StringHelper.neverNull(postcode));
				out.println("city : " + StringHelper.neverNull(city));
				out.println("country : " + StringHelper.neverNull(country));
				out.println("phone : " + StringHelper.neverNull(phone));
				out.println(comp.getFreeLabel() + " : " + StringHelper.neverNull(free));
				out.println("comment : " + StringHelper.neverNull(comment));
				out.println("");
				out.println("--");
				ContentContext absCtx = new ContentContext(ctx);
				absCtx.setAbsoluteURL(true);
				out.println(URLHelper.createURL(absCtx));

				out.close();
				NetHelper.sendMailToAdministrator(ctx, "registration on " + globalContext.getContextKey() + " : " + firstName + " " + lastName, new String(outStream.toByteArray()));


				if (!messageRepository.haveGlobalMessage() && !storeUser) {
					comp.needForm = false;
					request.setAttribute(StringHelper.REQUEST_KEY_FORM_VALID, "true");
				}

			} catch (Throwable t) {
				logger.warning(t.getMessage());
				t.printStackTrace();
			}

			/*
			 * if (fact.getCurrentUser() == null) { fact.login(GlobalContext.getInstance(request), userInfo.getLogin(), userInfo.getPassword()); }
			 */

		}

		return null;
	}

	@Override
	public String getActionGroupName() {
		return "registration";
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

}