/*
 * Created on 06-janv.-2004
 */
package org.javlo.component.form;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.BeanHelper;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserFactory;
import org.javlo.user.exception.UserAllreadyExistException;

/**
 * @author pvandermaesen
 */
public class FormRegisterComponent extends FormComponent {

	public static String performSubmit(HttpServletRequest request, HttpServletResponse response) {

		String msg = FormComponent.performSubmit(request, response);
		if (msg == null) {
			Form form = Form.getCurrentSessionForm(request.getSession());
			GlobalContext globalContext = GlobalContext.getInstance(request);
			IUserFactory fact = UserFactory.createUserFactory(globalContext, request.getSession());
			IUserInfo[] userInfos = fact.getUserInfoList();
			for (int i = 0; (i < userInfos.length) && (msg == null); i++) {
				String login = form.getValue("login");
				if ((fact.getCurrentUser(request.getSession()) == null) && (userInfos[i].getLogin().equals(login))) {
					msg = "user.error.allready-exist";
				}
			}
			if (msg == null) {
				IUserInfo userInfo;
				if (fact.getCurrentUser(request.getSession()) == null) {
					userInfo = fact.createUserInfos();
					userInfo.setLogin(form.getValue("login"));
					userInfo.setRoles(new String[] { "guest" });
				} else {
					userInfo = fact.getCurrentUser(request.getSession()).getUserInfo();
				}
				userInfo.setPassword(form.getValue("password"));
				userInfo.setEmail(form.getValue("email"));
				userInfo.setFirstName(form.getValue("firstName"));
				userInfo.setLastName(form.getValue("lastName"));
				try {
					if (fact.getCurrentUser(request.getSession()) == null) {
						fact.addUserInfo(userInfo);
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
		return msg;
	}

	@Override
	protected Form getForm(ContentContext ctx) {
		return FormRegister.getFormRegister(ctx.getRequest().getSession());
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getType()
	 */
	@Override
	public String getType() {
		return "form-register";
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

}
