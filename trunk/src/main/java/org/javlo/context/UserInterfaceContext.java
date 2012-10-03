package org.javlo.context;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.javlo.helper.StringHelper;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.User;

public class UserInterfaceContext {

	private static Logger logger = Logger.getLogger(UserInterfaceContext.class.getName());

	private HttpSession session;

	private GlobalContext globalContext;

	private boolean componentsList = true;

	private String currentModule = null;

	public static final String KEY = "userInterfaceContext";

	private static final UserInterfaceContext FAKE_INSTACE = new UserInterfaceContext();

	public static final UserInterfaceContext getInstance(HttpSession session, GlobalContext globalContext) {
		UserInterfaceContext instance = (UserInterfaceContext) globalContext.getSessionAttribute(session, KEY);

		if (instance == null) {
			AdminUserFactory userFact = AdminUserFactory.createUserFactory(globalContext, session);
			User user = userFact.getCurrentUser(session);
			if (userFact == null || user == null) {
				return FAKE_INSTACE;
			}

			instance = new UserInterfaceContext();
			instance.session = session;
			instance.globalContext = globalContext;

			globalContext.setSessionAttribute(session, KEY, instance);

			if (userFact.getUser(user.getLogin()) != null) { // not god user, so storable user
				IUserInfo ui = user.getUserInfo();
				instance.fromString(ui.getInfo());
			}
		}
		return instance;
	}

	@Override
	public String toString() {
		return "" + isComponentsList() + ';' + StringHelper.neverNull(getCurrentModule());
	}

	public void fromString(String content) {
		String[] data = content.split(";");
		componentsList = StringHelper.isTrue(data[0]);
		if (data.length > 1 && data[1].trim().length() > 0) {
			currentModule = data[1];
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
		if (userFact.getUser(user.getLogin()) != null) { // not god user, so storable user
			IUserInfo ui = user.getUserInfo();
			ui.setInfo(toString());
			userFact.updateUserInfo(ui);
		} else {
			logger.warning("can not store user interface information for user " + user.getLogin());
		}
	}

	public String getCurrentModule() {
		return currentModule;
	}

	public void setCurrentModule(String context) throws IOException {
		this.currentModule = context;
		store();
	}

}
