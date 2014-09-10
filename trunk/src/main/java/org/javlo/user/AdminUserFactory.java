package org.javlo.user;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.URLHelper;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;

public class AdminUserFactory extends UserFactory {

	private static final long serialVersionUID = 1L;

	public static Logger logger = Logger.getLogger(AdminUserFactory.class.getName());

	private static final String ADMIN_USER_INFO_FILE = "/users/admin/edit-users-list.csv";

	private static final Set<String> MASTER_ROLES = new HashSet<String>(Arrays.asList(new String[] { AdminUserSecurity.MASTER }));

	private String dataFolder = null;

	private boolean master = false;

	public static AdminUserFactory createUserFactory(GlobalContext globalContext, HttpSession session) {
		AdminUserFactory res = null;
		try {
			res = globalContext.getAdminUserFactory(session);
			res.master = globalContext.isMaster();
			logger.fine("create userFactory : " + res.getClass().getName());
		} catch (Exception e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
		}
		res.dataFolder = globalContext.getDataFolder();
		res.init(globalContext, session);
		session.setAttribute(globalContext.getAdminUserFactoryClassName(), res);
		return res;
	}

	/**
	 * @deprecated use createUserFactory
	 * @param globalContext
	 * @param session
	 * @return
	 */
	@Deprecated
	public static AdminUserFactory createAdminUserFactory(GlobalContext globalContext, HttpSession session) {
		return createUserFactory(globalContext, session);
		/*
		 * StaticConfig staticConfig = StaticConfig.getInstance(session); AdminUserFactory res = (AdminUserFactory) session.getServletContext().getAttribute(staticConfig.getAdminUserFactoryClassName()); if (res == null) { try { res = staticConfig.getAdminUserFactory(globalContext, session); logger.fine("create userFactory : " + res.getClass().getName()); } catch (Exception e) { logger.severe(e.getMessage()); e.printStackTrace(); } res.init(globalContext, session); session.getServletContext().setAttribute(staticConfig.getAdminUserFactoryClassName(), res); } return res;
		 */
	}

	@Override
	protected String getFileName() {
		return URLHelper.mergePath(dataFolder, ADMIN_USER_INFO_FILE);
	}

	@Override
	public User getUser(String login) {
		if (login == null)  {
			return null;
		}
		List<IUserInfo> users = getUserInfoList();
		for (IUserInfo user : users) {
			if (user.getLogin().equals(login)) {
				if (master) {
					user.addRoles(MASTER_ROLES);
				}
				User outUser = new User(user);
				outUser.setEditor(true);
				return outUser;
			}
		}
		return null;
	}

	@Override
	public User login(HttpServletRequest request, String login, String password) {
		User outUser = super.login(request, login, password);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editContext = EditContext.getInstance(globalContext, request.getSession());
		editContext.setEditUser(outUser);

		/** reload module **/
		try {
			ModulesContext.getInstance(request.getSession(), globalContext).loadModule(request.getSession(), globalContext);
		} catch (ModuleException e) {
			e.printStackTrace();
		}
		if (outUser != null) {
			outUser.setEditor(true);
		}
		return outUser;
	}

	@Override
	public User autoLogin(HttpServletRequest request, String login) {
		User outUser = super.autoLogin(request, login);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editContext = EditContext.getInstance(globalContext, request.getSession());
		editContext.setEditUser(outUser);
		if (outUser != null) {
			outUser.setEditor(true);
		}
		return outUser;
	}
	
	public User getCurrentUser(HttpSession session) {
		User user = (User) session.getAttribute(SESSION_KEY);
		if (user != null && user.isEditor()) {
			return user;
		} else {
			return null;
		}
	}

	/*
	 * @Override public User login(GlobalContext globalContext, String login, String password) { User outUser = super.login(globalContext, login, password); EditContext editContext = EditContext.getInstance(session); editContext.setEditUser(outUser); return outUser; }
	 */

	@Override
	public Set<String> getAllRoles(GlobalContext globalContext, HttpSession session) {
		EditContext ctx = EditContext.getInstance(globalContext, session);
		return ctx.getAdminUserRoles();
	}	

	@Override
	public UserInfo createUserInfos() {
		return new AdminUserInfo();
	}

	@Override
	public User login(HttpServletRequest request, String token) {
		User outUser = super.login(request, token);

		if (outUser == null && !master) {
			IUserFactory masterUserFactory;
			try {
				masterUserFactory = AdminUserFactory.createUserFactory(GlobalContext.getMasterContext(request.getSession()), request.getSession());
				outUser = masterUserFactory.login(request, token);
				UserFactory.createUserFactory(GlobalContext.getInstance(request), request.getSession()); // reset the "real" user factory
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (outUser != null) {
			GlobalContext globalContext = GlobalContext.getInstance(request);
			outUser.setContext(globalContext.getContextKey());
			request.getSession().setAttribute(SESSION_KEY, outUser);
		}

		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editContext = EditContext.getInstance(globalContext, request.getSession());
		editContext.setEditUser(outUser);

		/** reload module **/
		try {
			ModulesContext.getInstance(request.getSession(), globalContext).loadModule(request.getSession(), globalContext);
		} catch (ModuleException e) {
			e.printStackTrace();
		}
		if (outUser != null) {
			outUser.setEditor(true);
		}
		return outUser;
	}

}
