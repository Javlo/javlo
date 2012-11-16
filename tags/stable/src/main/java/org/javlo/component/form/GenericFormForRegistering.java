package org.javlo.component.form;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.message.GenericMessage;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserFactory;
import org.javlo.user.UserInfo;
import org.javlo.user.exception.UserAllreadyExistException;

public class GenericFormForRegistering extends GenericForm {

	@Override
	protected boolean isCaptcha() {
		return false;
	}

	public static String performSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String msg = null;
		String email = "?";
		ContentContext ctx = ContentContext.getContentContext(request, response);
		RequestService requestService = RequestService.getInstance(request);
		ContentService content = ContentService.getInstance(request);
		try {
			msg = GenericForm.performSubmit(request, response);

			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			IUserFactory userFactory = UserFactory.createUserFactory(globalContext, request.getSession());

			IUserInfo userInfo = userFactory.createUserInfos();

			String[] labels = userInfo.getAllLabels();
			Map<String, String> requestMap = new HashMap<String, String>();
			for (String label : labels) {
				String value = request.getParameter(label);
				requestMap.put(label, value);
			}
			((UserInfo) userInfo).setAllValues(requestMap);
			email = userInfo.getEmail();

			if (userInfo.getLogin() == null || userInfo.getLogin().trim().length() == 0) {
				userInfo.setLogin(userInfo.getEmail());
			}

			userFactory.addUserInfo(userInfo);
			userFactory.store();
		} catch (UserAllreadyExistException e) {
			request.removeAttribute("valid");
			request.setAttribute("userExist", email);
			GenericForm comp = (GenericForm) content.getComponent(ctx, requestService.getParameter("comp_id", null));
			request.setAttribute("msg", new GenericMessage(comp.getLocalConfig(false).getProperty("message.user-allready-exist"), GenericMessage.ERROR));
			request.setAttribute("error", e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			request.removeAttribute("valid");
			request.setAttribute("error", e.getMessage());
			e.printStackTrace();
		}

		return msg;
	}

	@Override
	public String getType() {
		return "generic-form-for-registering";
	}

	@Override
	public String getActionGroupName() {
		return "gform-registering";
	}
}
