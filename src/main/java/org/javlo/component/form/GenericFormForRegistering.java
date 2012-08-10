package org.javlo.component.form;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserFactory;
import org.javlo.user.UserInfo;

public class GenericFormForRegistering extends GenericForm {
	
	@Override
	protected boolean isCaptcha() {
		return false;
	}
	
	public static String performSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		System.out.println("***** GenericFormForRegistering.performSubmit : START"); //TODO: remove debug trace
		
		String msg = GenericForm.performSubmit(request, response);
		
		ContentContext ctx = ContentContext.getContentContext(request, response);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		IUserFactory userFactory = UserFactory.createUserFactory(globalContext, request.getSession());
		
		IUserInfo userInfo = userFactory.createUserInfos();
		
		String[] labels = userInfo.getAllLabels();
		Map<String,String> requestMap = new HashMap<String, String>();
		for (String label : labels) {
			String value = request.getParameter(label);
			requestMap.put(label, value);
		}		
		((UserInfo)userInfo).setAllValues(requestMap);
		
		if (userInfo.getLogin() == null || userInfo.getLogin().trim().length() == 0) {
			userInfo.setLogin(userInfo.getEmail());
		}
		
		userFactory.addUserInfo(userInfo);
		
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
