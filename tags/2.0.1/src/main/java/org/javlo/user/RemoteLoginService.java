package org.javlo.user;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.utils.TimeMap;

/**
 * service for login from a other application on javlo.
 * 
 * @author pvandermaesen
 * 
 */
public class RemoteLoginService {

	public static final String PARAM_NAME = "login_id";

	public static RemoteLoginService getInstance(ServletContext application) {
		RemoteLoginService outService = (RemoteLoginService) application.getAttribute(RemoteLoginService.class.getName());
		if (outService == null) {
			outService = new RemoteLoginService();
			application.setAttribute(RemoteLoginService.class.getName(), outService);
		}
		return outService;
	}

	TimeMap<String, User> connectedUsers = new TimeMap<String, User>(60 * 30); // 30 minutes default time for a remote session

	public String login(HttpServletRequest request, String login, String password) {
		if (login == null || password == null) {
			return "";
		}
		GlobalContext globalContext = GlobalContext.getInstance(request);
		IUserFactory userFactory = UserFactory.createUserFactory(globalContext, request.getSession());
		User user = userFactory.login(request, login, password);
		if (user != null) {
			String outId = StringHelper.getRandomId();
			connectedUsers.put(outId, user);
			return outId;
		} else {
			return null;
		}

	}

	public User login(String id) {
		return connectedUsers.get(id);
	}

}
