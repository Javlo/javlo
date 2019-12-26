package org.javlo.context;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.javlo.config.StaticConfig;
import org.javlo.helper.StringHelper;
import org.javlo.mailing.MailConfig;
import org.javlo.module.core.IMainModuleName;
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
	
	private boolean model = false;
	
	private boolean admin = true;
	
	private boolean navigation = true;
	
	private boolean mailing = false;
	
	private boolean sendMailing = true;
	
	private boolean ticket = false;
	
	private boolean search = false;
	
	private boolean mobilePreview = true;
	
	private boolean addOnly = false;

	private String currentModule = null;

	private boolean minimalInterface = false;
	
	private String login = "";

	public static final String KEY = "userInterface";

	private static final UserInterfaceContext FAKE_INSTACE = new UserInterfaceContext();
	
	public static final UserInterfaceContext getInstance(ContentContext ctx) {
		return getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext());
	}

	public static final UserInterfaceContext getInstance(HttpSession session, GlobalContext globalContext) {
		UserInterfaceContext instance = (UserInterfaceContext) session.getAttribute(KEY);
		AdminUserFactory userFact = AdminUserFactory.createUserFactory(globalContext, session);
		User user = userFact.getCurrentUser(session);
		String login = null;
		if (user != null) {
			login = user.getLogin();
		}
		if (instance == null || instance.globalContext != globalContext || !instance.login.equals(login)) {
			if (userFact == null || user == null) {
				return FAKE_INSTACE;
			}
			instance = new UserInterfaceContext();
			instance.login = user.getLogin();
			instance.session = session;
			instance.globalContext = globalContext;			
			MailConfig config = new MailConfig(globalContext, globalContext.getStaticConfig(), null);
			if ((StringHelper.isEmpty(config.getSMTPHost()) || config.getSMTPPort() == null || config.getSMTPPort().equals("0")) && StringHelper.isEmpty(globalContext.getDMZServerIntra())) {
				instance.sendMailing = false;
			}	
			if (globalContext.getModules().contains(IMainModuleName.MAILING) && AdminUserSecurity.getInstance().canRole(user, AdminUserSecurity.MAILING_ROLE)) {
				instance.mailing = true;
			} else {
				instance.mailing = false;
			}
			if (AdminUserSecurity.getInstance().canRole(user, AdminUserSecurity.NAVIGATION_ROLE) || AdminUserSecurity.getInstance().isAdmin(user)) {
				instance.navigation = true;
			} else {
				instance.navigation = false;
			}
			if (globalContext.getModules().contains(IMainModuleName.TICKET)) {
				instance.setTicket(true);
			} else {
				instance.setTicket(false);
			}
			if (globalContext.getModules().contains(IMainModuleName.SEARCH)) {
				instance.setSearch(true);
			} else {
				instance.setSearch(false);
			}
			if (user.getRoles().contains(AdminUserSecurity.ADD_ONLY) && globalContext.getStaticConfig().isAddButton()) {
				instance.addOnly = true;
			}
			instance.fromString(user.getUserInfo().getInfo());
			session.setAttribute(KEY, instance);
		}
		StaticConfig stConf = globalContext.getStaticConfig();
		instance.lightInterface = AdminUserSecurity.getInstance().haveRole(user, AdminUserSecurity.LIGHT_INTERFACE_ROLE);
		instance.setMinimalInterface(instance.lightInterface && !AdminUserSecurity.getInstance().haveRole(user, AdminUserSecurity.NAVIGATION_ROLE) && stConf.isAddButton());
		instance.contributor = AdminUserSecurity.getInstance().haveRole(user, AdminUserSecurity.CONTRIBUTOR_ROLE);
		instance.setModel(AdminUserSecurity.getInstance().canRole(user, AdminUserSecurity.MODEL_ROLE)); 
		instance.setAdmin(AdminUserSecurity.getInstance().isAdmin(user));
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
		if (globalContext != null) {
		AdminUserFactory userFact = AdminUserFactory.createUserFactory(globalContext, session);
		User user = userFact.getCurrentUser(session);
		if (user != null) {
			user = userFact.getUser(user.getLogin()); // get real user
			if (user != null) {
				// not god user, so storable user
				IUserInfo ui = user.getUserInfo();
				//ui.setUserInfo(toString());
				userFact.updateUserInfo(ui);
			}
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

	public boolean isMailing() {
		return mailing;
	}

	public void setMailing(boolean mailing) {
		this.mailing = mailing;
	}

	public boolean isTicket() {
		return ticket;
	}

	public void setTicket(boolean ticket) {
		this.ticket = ticket;
	}
	
	public boolean isPreviewResourcesTab() {
		return globalContext.getModules().contains(IMainModuleName.SHARED_CONTENT);
	}

	public boolean isSendMailing() {
		return sendMailing;
	}

	public void setSendMailing(boolean sendMailing) {
		this.sendMailing = sendMailing;
	}

	public boolean isSearch() {
		return search;
	}

	public void setSearch(boolean search) {
		this.search = search;
	}

	public boolean isNavigation() {
		return navigation;
	}

	public void setNavigation(boolean navigation) {
		this.navigation = navigation;
	}

	public boolean isModel() {
		return model;
	}

	public void setModel(boolean model) {
		this.model = model;
	}
	
	public boolean isIM() {
		return globalContext.getStaticConfig().isIM();
	}

	public boolean isMobilePreview() {
		return mobilePreview;
	}

	public void setMobilePreview(boolean mobilePreview) {
		this.mobilePreview = mobilePreview;
	}

	public boolean isMinimalInterface() {
		return minimalInterface;
	}

	public void setMinimalInterface(boolean minimalInterface) {
		this.minimalInterface = minimalInterface;
	}
	
	public boolean isAddOnly() {
		return addOnly;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	
}
