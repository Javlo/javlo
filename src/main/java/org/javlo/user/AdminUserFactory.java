package org.javlo.user;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.URLHelper;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;

public class AdminUserFactory extends UserFactory {

	private static final long serialVersionUID = 1L;

	public static Logger logger = Logger.getLogger(AdminUserFactory.class.getName());

	public String adminUserInfoFile = null;

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
	
	@Override
	protected String getSessionKey() {
		return "adminCurrentUser";
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
		 * StaticConfig staticConfig = StaticConfig.getInstance(session);
		 * AdminUserFactory res = (AdminUserFactory)
		 * session.getServletContext().
		 * getAttribute(staticConfig.getAdminUserFactoryClassName()); if (res ==
		 * null) { try { res = staticConfig.getAdminUserFactory(globalContext,
		 * session); logger.fine("create userFactory : " +
		 * res.getClass().getName()); } catch (Exception e) {
		 * logger.severe(e.getMessage()); e.printStackTrace(); }
		 * res.init(globalContext, session);
		 * session.getServletContext().setAttribute
		 * (staticConfig.getAdminUserFactoryClassName(), res); } return res;
		 */
	}

	@Override
	protected String getFileName() {
		return URLHelper.mergePath(dataFolder, adminUserInfoFile);
	}

	@Override
	public User getUser(String login) {
		if (login == null) {
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
		
		logger.info("admin login trying : "+login);
		
		User outUser = super.login(request, login, password);
		
//		if (DEFAULT_PASSWORD.equals(password)) {
//			I18nAccess i18nAccess;
//			try {
//				i18nAccess = I18nAccess.getInstance(request);
//				MessageRepository.getInstance(request).setGlobalMessage(new GenericMessage(i18nAccess.getText("user.change-password", "Please change you password."), GenericMessage.ALERT));
//			} catch (Exception e) {
//				e.printStackTrace();					
//			}				
//		}
		
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
	

	/*public User googleLogin(HttpServletRequest request, String accessToken) {		
		logger.info("google admin login trying : "+accessToken);
		HttpClient httpClient = null;
		try {
			httpClient = new URLConnectionClient();			
			OAuthClient oAuthClient = new OAuthClient(httpClient);			
			Google google = new Google();
			StaticConfig staticConfig = StaticConfig.getInstance(request.getSession().getServletContext());			
			google.setClientId(staticConfig.getOauthGoogleIdClient());
			google.setClientSecret(staticConfig.getOauthGoogleSecret());
			google.prepare(request);			
			TransientUserInfo.getInstance(request.getSession()).setToken(accessToken);
			SocialUser user = google.getSocialUser(accessToken, oAuthClient);
			if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
				logger.warning("OAuth admin login failed with google provider");				
			} else {
				System.out.println("***** AdminUserFactory.googleLogin : name = "+user.getEmail()); //TODO: remove debug trace
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (httpClient != null) {
				httpClient.shutdown();
			}
		}
		return null;
	}*/


	@Override
	public User autoLogin(HttpServletRequest request, String login) {		
		GlobalContext globalCtx = GlobalContext.getInstance(request);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());

		boolean logged = request.getUserPrincipal() != null && request.getUserPrincipal().getName().equals(login);
		
		User currentUser = getCurrentUser(request.getSession());		
		User user = getUser(login);
		if (currentUser != null && user != null && currentUser.getPassword().equals(user.getPassword())) {
			return null;
		} else if (user == null) {
			// administrator auto login not possible
			if (editCtx.getEditUser(login) != null && (logged || editCtx.hardAutoLogin(login))) {
				user = createUser(login, (new HashSet(Arrays.asList(new String[] { AdminUserSecurity.GENERAL_ADMIN, AdminUserSecurity.FULL_CONTROL_ROLE }))));
				editCtx.setEditUser(user);
			}
		}

		if (user != null && globalCtx.getAdministrator().equals(user.getLogin())) {
			user.getUserInfo().addRoles(new HashSet(Arrays.asList(new String[] { AdminUserSecurity.FULL_CONTROL_ROLE })));
		}
		if (user != null && editCtx.getEditUser(user.getLogin()) != null) {
			user.getUserInfo().addRoles(new HashSet(Arrays.asList(new String[] { AdminUserSecurity.GENERAL_ADMIN, AdminUserSecurity.FULL_CONTROL_ROLE })));
		}
		if (user != null) {
			user.setContext(globalContext.getContextKey());
			request.getSession().setAttribute(getSessionKey(), user);			
		}

		EditContext editContext = EditContext.getInstance(globalContext, request.getSession());
		editContext.setEditUser(user);		
		if (user != null) {
			user.setEditor(true);
			/** reload module **/
			try {
				ModulesContext.getInstance(request.getSession(), globalContext).loadModule(request.getSession(), globalContext);
			} catch (ModuleException e) {
				e.printStackTrace();
			}
			user.setEditor(true);
		}
		return user;
	}

	public User getCurrentUser(HttpSession session) {
		User user = (User) session.getAttribute(getSessionKey());
		if (user != null && user.isEditor()) {			
			return user;
		} else {
			return null;
		}
	}

	/*
	 * @Override public User login(GlobalContext globalContext, String login,
	 * String password) { User outUser = super.login(globalContext, login,
	 * password); EditContext editContext = EditContext.getInstance(session);
	 * editContext.setEditUser(outUser); return outUser; }
	 */

	@Override
	public Set<String> getAllRoles(GlobalContext globalContext, HttpSession session) {
		EditContext editContext = EditContext.getInstance(globalContext, session);
		return editContext.getAdminUserRoles();
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
				UserFactory.createUserFactory(GlobalContext.getInstance(request), request.getSession());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		GlobalContext globalContext = GlobalContext.getInstance(request);
		if (outUser == null) {
			outUser = EditContext.getInstance(GlobalContext.getInstance(request), request.getSession()).hardLoginByToken(token);
		}

		if (outUser != null) {			
			outUser.setContext(globalContext.getContextKey());
			request.getSession().setAttribute(getSessionKey(), outUser);
		}

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
	protected Set<String> getRoleList(ContentContext ctx) {
		return ctx.getGlobalContext().getAdminUserRoles();
	}

}
