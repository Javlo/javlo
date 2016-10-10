/*
 * Created on 06-janv.-2004
 */
package org.javlo.component.form;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserFactory;
import org.javlo.user.UserInfo;
import org.javlo.user.exception.UserAllreadyExistException;

/**
 * @author pvandermaesen
 */
public class AddressFormRegisterComponent extends FormComponent {

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getType()
	 */
	@Override
	public String getType() {
		return "form-register";
	}

	@Override
	protected Form getForm(ContentContext ctx) {
		return AddressFormRegister.getFormRegister(ctx.getRequest().getSession());
	}

	/**
	 * @see org.javlo.component.AbstractVisualComponent#init(java.lang.String, java.lang.String, org.javlo.ContentContext)
	 */
	@Override
	protected void init(ComponentBean bean, ContentContext ctx) throws Exception {
		super.init(bean, ctx);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		IUserFactory fact = UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());

		if (fact.getCurrentUser(ctx.getRequest().getSession()) != null) {
			IUserInfo userInfo = fact.getCurrentUser(ctx.getRequest().getSession()).getUserInfo();
			getForm(ctx).setValues(BeanHelper.bean2Map(userInfo));
			getForm(ctx).setValue("password2", userInfo.getPassword());
		}

	}

	public static String performSubmit(HttpServletRequest request, HttpServletResponse response) {

		String msg;
		try {
			msg = FormComponent.performSubmit(request, response);
			ContentContext ctx = ContentContext.getContentContext(request, response);
			GlobalContext globalContext = GlobalContext.getInstance(request);
			if (msg == null) {
				Form form = Form.getCurrentSessionForm(request.getSession());
				IUserFactory fact = UserFactory.createUserFactory(globalContext, request.getSession());
				Collection<IUserInfo> userInfos = fact.getUserInfoList();
				for (IUserInfo iUserInfo : userInfos) {
					String login = form.getValue("login");
					if ((fact.getCurrentUser(request.getSession()) == null) && (iUserInfo.getLogin().equals(login))) {
						msg = "user.error.allready-exist";
					}
				}
				if (msg == null) {
					UserInfo userInfo;
					if (fact.getCurrentUser(request.getSession()) == null) {
						userInfo = (UserInfo) fact.createUserInfos();
						userInfo.setId(StringHelper.getRandomId());
						userInfo.setLogin(form.getValue("email"));

						RequestService service = RequestService.getInstance(request);
						EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());

						/* retreive user roles */
						Set<String> roles = editCtx.getUserRolesDefault();
						String componentId = service.getParameter("component_id", null);
						if (componentId != null) {
							ContentService content = ContentService.getInstance(globalContext);
							IContentVisualComponent comp = content.getComponent(ctx, componentId);
							if ((comp != null) && (comp.getValue(ctx).trim().length() > 0)) {
								roles = new HashSet<String>(StringHelper.stringToCollection(comp.getValue(ctx)));
							}
						}

						userInfo.setRoles(roles);

						if (service.getParameter("mailing", null) != null) {
							roles = userInfo.getRoles();
							Set<String> rolesMailing = new HashSet<String>();
							for (String role : roles) {
								rolesMailing.add(role);
							}
							rolesMailing.add("mailing");
							userInfo.setRoles(rolesMailing);
						}
					} else {
						userInfo = (UserInfo) fact.getCurrentUser(request.getSession()).getUserInfo();
					}
					userInfo.setPassword("");
					userInfo.setEmail(form.getValue("email"));
					userInfo.setFirstName(form.getValue("firstName"));
					userInfo.setLastName(form.getValue("lastName"));

					userInfo.setAddress(form.getValue("address"));
					userInfo.setPostCode(form.getValue("postcode"));
					userInfo.setCity(form.getValue("city"));
					try {
						if (fact.getCurrentUser(request.getSession()) == null) {
							fact.addUserInfo(userInfo);
							fact.login(request, userInfo.getLogin(), userInfo.getPassword());
						} else {

							fact.updateUserInfo(userInfo);
						}
						fact.store();
					} catch (UserAllreadyExistException e) {
						msg = "user.error.allready-exist";
					}
					if (fact.getCurrentUser(request.getSession()) == null) {
						fact.login(request, userInfo.getLogin(), userInfo.getPassword());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			msg = e.getMessage();
		}
		return msg;
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
		out.print("<label for=\"");
		out.print(getContentName());
		out.print("\">");
		out.print(i18n.getText("form.choose-role"));
		out.println("</label>");

		out.println("<div>");
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		out.print(XHTMLHelper.getInputMultiSelect(getContentName(), editCtx.getUserRoles(), StringHelper.stringToCollection(getValue())));
		out.println("</div>");

		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		RequestService service = RequestService.getInstance(ctx.getRequest());

		String specialParam = service.getParameter(getTypeInputName(), null);
		if (specialParam != null) {
			String newContent = service.getParameter(getContentName(), "");
			if (!getValue().equals(newContent)) {
				setValue(newContent);
				setModify();
			}
		}
		return null;
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}
}
