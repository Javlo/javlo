package org.javlo.context;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.javlo.helper.StringHelper;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserInfo;
import org.javlo.user.User;

public class UserInterfaceContext {

	private static Logger logger = Logger.getLogger(UserInterfaceContext.class.getName());

	private HttpSession session;

	private GlobalContext globalContext;

	private boolean componentsList = true;

	private boolean lightInterface = true;

	private boolean contributor = true;

	private String currentModule = null;

	public static final String KEY = "userInterface";

	private static final UserInterfaceContext FAKE_INSTACE = new UserInterfaceContext();

	public static final UserInterfaceContext getInstance(HttpSession session, GlobalContext globalContext) {
		UserInterfaceContext instance = (UserInterfaceContext) session.getAttribute(KEY);
		AdminUserFactory userFact = AdminUserFactory.createUserFactory(globalContext, session);
		User user = userFact.getCurrentUser(session);

		if (instance == null) {
			if (userFact == null || user == null) {
				return FAKE_INSTACE;
			}

			instance = new UserInterfaceContext();
			instance.session = session;
			instance.globalContext = globalContext;

			instance.fromString(user.getUserInfo().getInfo());

			session.setAttribute(KEY, instance);
		}

		instance.lightInterface = AdminUserSecurity.getInstance().haveRole(user, AdminUserSecurity.LIGHT_INTERFACE_ROLE);
		instance.contributor = AdminUserSecurity.getInstance().haveRole(user, AdminUserSecurity.CONTRIBUTOR_ROLE);

		return instance;
	}

	@Override
	public String toString() {
		return "" + isComponentsList() + ';' + StringHelper.neverNull(getCurrentModule());
	}

	public void fromString(String content) {
		if (content == null || content.trim().length() > 0) {
			String[] data = content.split(";");
			componentsList = StringHelper.isTrue(data[0]);
			if (data.length > 1 && data[1].trim().length() > 0) {
				currentModule = data[1];
			}
		}
	}

	public boolean isComponentsList() {
		return componentsList;
	}

	public void setComponentsList(boolean componentList) throws ContextException {
		this.componentsList = componentList;
		try {
			store();
		} catch (IOException e) {
			throw new ContextException(e.getMessage());
		}
	}

	private void store() throws IOException {
		AdminUserFactory userFact = AdminUserFactory.createUserFactory(globalContext, session);
		User user = userFact.getCurrentUser(session);
		if (user != null) {
			user = userFact.getUser(user.getLogin()); // get real user
			if (user != null) {
				// not god user, so storable user
				IUserInfo ui = user.getUserInfo();
				ui.setInfo(toString());
				userFact.updateUserInfo(ui);
			}
		}
	}

	public String getCurrentModule() {
		return currentModule;
	}

	public void setCurrentModule(String context) throws IOException {
		if (this.currentModule == null || !this.currentModule.equals(context)) {
			this.currentModule = context;
			store();
		}
	}

	public boolean isLight() {
		return lightInterface;
	}

	public boolean isContributor() {
		return contributor;
	}
}
