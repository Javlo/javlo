/*
 * Created on 19-fevr.-2004
 */
package org.javlo.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.config.StaticConfig;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.JavaHelper;
import org.javlo.helper.Logger;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.user.exception.UserAllreadyExistException;
import org.javlo.utils.CSVFactory;
import org.javlo.utils.TimeMap;

/**
 * @author pvandermaesen
 */
public class UserFactory implements IUserFactory, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * create a static logger.
	 */
	public static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(UserFactory.class.getName());

	static Object lock = new Object();

	static Object mergeLock = new Object();

	static Object lockStore = new Object();

	public static final String USER_FACTORY_KEY = "_user_factory_";

	private String userInfoFile = null;

	private static Map<String, IUserInfo> changePasswordReference = new TimeMap<String, IUserInfo>();

	protected List<IUserInfo> userInfoList = null; // TODO: create a external
	// application scope class

	public static final String SESSION_KEY = "currentUser";

	// private StaticConfig staticConfig = null;

	public static IUserFactory createUserFactory(GlobalContext globalContext, HttpSession session) {
		IUserFactory res = null;
		try {
			res = globalContext.getUserFactory(session);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (res == null) {
			res = new UserFactory();
		}
		res.init(globalContext, session);
		return res;
	}

	/**
	 * @deprecated use createUserFactory(GlobalContext, HttpSession)
	 * @param request
	 * @return
	 */
	@Deprecated
	public static final IUserFactory createUserFactory(HttpServletRequest request) {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		return createUserFactory(globalContext, request.getSession());
	}

	private String dataFolder = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#addUserInfo(org.javlo.user.UserInfos)
	 */
	@Override
	public void addUserInfo(IUserInfo userInfo) throws UserAllreadyExistException {
		synchronized (lock) {
			userInfo.setModificationDate(new Date());
			if (getUserInfos(userInfo.getLogin()) != null) {
				throw new UserAllreadyExistException(userInfo.getLogin() + " allready exist.");
			}
			userInfoList = getUserInfoList();
			if (userInfoList == null) {
				clearUserInfoList();
			}
			userInfoList.add(userInfo);
		}
	}

	@Override
	public User autoLogin(HttpServletRequest request, String login) {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		User currentUser = getCurrentUser(request.getSession());	
		User user = getUser(login);
		if (currentUser != null && user != null && currentUser.getPassword().equals(user.getPassword())) {
			return null;
		} else if (user != null) {
			user.setContext(globalContext.getContextKey());
			request.getSession().setAttribute(SESSION_KEY, user);
		}
		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#clearUserInfoList()
	 */
	@Override
	public void clearUserInfoList() {
		userInfoList = new LinkedList<IUserInfo>();
	}

	protected User createUser(String login, Set<String> roles) {
		UserInfo ui = createUserInfos();
		ui.setLogin(login);
		ui.setRoles(roles);

		User user = new User(ui);
		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#createUserInfos()
	 */
	@Override
	public UserInfo createUserInfos() {
		return new UserInfo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#deleteUser(java.lang.String)
	 */
	@Override
	public void deleteUser(String login) {
		IUserInfo tobeDeleted = null;
		synchronized (lock) {
			userInfoList = getUserInfoList();

			for (IUserInfo user : userInfoList) {
				if (user.getLogin().equals(login)) {
					tobeDeleted = user;
				}
			}

			if (tobeDeleted != null) {
				userInfoList.remove(tobeDeleted);
			}
		}
	}

	@Override
	public Set<String> getAllRoles(GlobalContext globalContext, HttpSession session) {
		EditContext editContext = EditContext.getInstance(globalContext, session);
		return editContext.getUserRoles();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#getCurrentUser()
	 */
	@Override
	/**public User getCurrentUser(HttpSession session) {
		User user = (User) session.getAttribute(SESSION_KEY);
		String globalContextKey = GlobalContext.getSessionContextKey(session);
		if (globalContextKey != null && user != null) {
			if (user.getContext().equals(globalContextKey)) {
				return user;
			} else {
				if (AdminUserSecurity.getInstance().isGod(user)) {
					return user;
				}
				try {
					EditContext.getInstance(GlobalContext.getInstance(session, globalContextKey), session).setEditUser(null);					
				} catch (Exception e) {
					e.printStackTrace();
				}
				logger.info("remove user '"+user.getLogin()+"' context does'nt match.");
				session.removeAttribute(SESSION_KEY);
				return null;
			}
		}		
		return user;
	}**/
	public User getCurrentUser(HttpSession session) {
		User user = (User) session.getAttribute(SESSION_KEY);
		if (user != null) {
			return user;
		} else {
			return null;
		}
	}

	protected String getFileName() {
		return URLHelper.mergePath(dataFolder, userInfoFile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#getUser(java.lang.String)
	 */
	@Override
	public User getUser(String login) {
		List<IUserInfo> users = getUserInfoList();
		for (IUserInfo user : users) {
			if (user.getLogin().equals(login)) {
				return new User(user);
			}
		}
		return null;
	}

	@Override
	public User getUserByEmail(String email) {
		List<IUserInfo> users = getUserInfoList();
		for (IUserInfo user : users) {
			if (user.getEmail().equals(email)) {
				return new User(user);
			}
		}
		return null;
	}

	@Override
	public User login(HttpServletRequest request, String token) {
		if (token == null || token.trim().length() == 0) {
			return null;
		}

		GlobalContext globalContext = GlobalContext.getInstance(request);
		String realToken = globalContext.convertOneTimeToken(token);
		if (realToken != null) {
			token = realToken;
		}

		User outUser = null;
		List<IUserInfo> users = getUserInfoList();
		for (IUserInfo user : users) {
			if (user.getToken() != null && user.getToken().equals(token)) {
				outUser = new User(user);
			}
		}

		if (outUser != null) {
			outUser.setContext(globalContext.getContextKey());
			request.getSession().setAttribute(SESSION_KEY, outUser);
		}

		return outUser;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#getUserInfoForRoles(java.lang.String[])
	 */
	@Override
	public List<IUserInfo> getUserInfoForRoles(String[] inRoles) {
		Set<String> roles = new HashSet<String>();
		for (String inRole : inRoles) {
			roles.add(inRole);
		}
		List<IUserInfo> outUserList = new LinkedList<IUserInfo>();
		List<IUserInfo> allUserInfo = getUserInfoList();
		for (IUserInfo element : allUserInfo) {
			Set<String> userRoles = new HashSet<String>(element.getRoles());
			userRoles.retainAll(roles);
			if (userRoles.size() > 0) {
				outUserList.add(element);
			}
		}
		return outUserList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#getUserInfoList()
	 */
	@Override
	public List<IUserInfo> getUserInfoList() {
		if (userInfoList == null) {
			synchronized (lock) {
				if (userInfoList == null) {
					String userInfoPath = getFileName();
					File userInfoFile = new File(userInfoPath);

					if (!userInfoFile.exists()) {
						logger.fine(userInfoFile.getPath() + " not found.");
						return new LinkedList<IUserInfo>();
					} else {
						try {
							InputStream in = new FileInputStream(userInfoFile);
							CSVFactory fact;
							try {
								fact = new CSVFactory(in);
							} finally {
								ResourceHelper.closeResource(in);
							}
							String[][] csvArray = fact.getArray();
							userInfoList = new LinkedList<IUserInfo>();
							for (int i = 1; i < csvArray.length; i++) {
								IUserInfo newUserInfo = createUserInfos();
								Map<String, String> values = JavaHelper.createMap(csvArray[0], csvArray[i]);
								BeanHelper.copy(values, newUserInfo);
								userInfoList.add(newUserInfo);
							}
						} catch (Exception e) {
							Logger.log(e);
							userInfoList = new LinkedList<IUserInfo>();
						}
					}
				}
			}
		}
		return userInfoList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#getUserInfos(java.lang.String)
	 */
	@Override
	public IUserInfo getUserInfos(String id) {
		Collection<IUserInfo> userInfoList = getUserInfoList();
		synchronized (lock) {
			for (IUserInfo userInfo : userInfoList) {
				if (userInfo.getLogin().equals(id)) {
					return userInfo;
				}
			}
		}
		return null;
	}

	@Override
	public void init(GlobalContext globalContext, HttpSession newSession) {
		dataFolder = globalContext.getDataFolder();
		StaticConfig staticConfig = StaticConfig.getInstance(newSession.getServletContext());
		userInfoFile = staticConfig.getUserInfoFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#isStandardStorage()
	 */
	@Override
	public boolean isStandardStorage() {
		return true;
	}

	@Override
	public User login(HttpServletRequest request, String login, String password) {

		logger.fine("try to log : " + login);

		GlobalContext globalCtx = GlobalContext.getInstance(request);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());

		boolean logged = request.getUserPrincipal() != null && request.getUserPrincipal().getName().equals(login);
		User user = getUser(login);
		if (user == null) {
			user = getUserByEmail(login);
		}

		boolean passwordEqual = false;
		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());

		if (user == null && !globalContext.isMaster()) { // check if user is in
															// master
															// globalContext
			IUserFactory masterUserFactory;
			try {
				masterUserFactory = AdminUserFactory.createUserFactory(GlobalContext.getMasterContext(request.getSession()), request.getSession());
				user = masterUserFactory.getUser(login);
				UserFactory.createUserFactory(globalContext, request.getSession()); // reset
																					// the
																					// "real"
																					// user
																					// factory
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (user != null) {
			if (staticConfig.isPasswordEncryt()) {
				if (user.getPassword() != null) {
					passwordEqual = user.getPassword().equals(StringHelper.encryptPassword(password));
				}
			} else {
				if (user.getPassword() != null) {
					passwordEqual = user.getPassword().equals(password);
				}
			}
		}

		if (user == null || (!logged && user.getPassword() != null && !passwordEqual)) {
			if (globalCtx.getAdministrator().equals(login) && (logged || globalCtx.administratorLogin(login, password))) {
				logger.fine("log user with password : " + login + " obtain full control role.");
				user = createUser(login, (new HashSet(Arrays.asList(new String[] { AdminUserSecurity.FULL_CONTROL_ROLE }))));
			} else if (editCtx.getEditUser(login) != null && (logged || editCtx.hardLogin(login, password))) {
				logger.fine("log user with password : " + login + " obtain general addmin mode and full control role.");
				user = createUser(login, (new HashSet(Arrays.asList(new String[] { AdminUserSecurity.GENERAL_ADMIN, AdminUserSecurity.FULL_CONTROL_ROLE }))));
				editCtx.setEditUser(user);
			} else {
				logger.fine("fail to log user with password : " + login + ".");
				user = null;
			}
		}
		if (user != null && globalCtx.getAdministrator().equals(user.getLogin())) {
			user.getUserInfo().addRoles((new HashSet(Arrays.asList(new String[] { AdminUserSecurity.FULL_CONTROL_ROLE }))));
		}
		if (user != null && editCtx.getEditUser(user.getLogin()) != null) {
			user.getUserInfo().addRoles((new HashSet(Arrays.asList(new String[] { AdminUserSecurity.GENERAL_ADMIN, AdminUserSecurity.FULL_CONTROL_ROLE }))));
		}

		if (user != null) {
			user.setContext(globalContext.getContextKey());
			request.getSession().setAttribute(SESSION_KEY, user);
		}

		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#logout()
	 */
	@Override
	public void logout(HttpSession session) {
		session.removeAttribute(SESSION_KEY);
	}

	/*
	 * 
	 * @see org.javlo.user.IUserFactory#addUserInfo(org.javlo.user.UserInfos)
	 */
	@Override
	public void mergeUserInfo(IUserInfo userInfo) throws IOException {

		synchronized (mergeLock) {

			IUserInfo currentUserInfo = getUserInfos(userInfo.getLogin());
			if (currentUserInfo == null) {
				try {
					addUserInfo(userInfo);
				} catch (UserAllreadyExistException e) {
					e.printStackTrace();
				}
			} else {
				Collection<String> currentRoles = currentUserInfo.getRoles();
				List<String> rolesList = new LinkedList<String>();
				rolesList.addAll(userInfo.getRoles());
				for (String role : currentRoles) {
					if (!rolesList.contains(role)) {
						rolesList.add(role);
					}
				}
				Set<String> newRoles = new HashSet<String>();
				newRoles.addAll(rolesList);
				userInfo.setRoles(newRoles);
				updateUserInfo(userInfo);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#releaseUserInfoList()
	 */
	@Override
	public void releaseUserInfoList() {
		synchronized (lock) {
			userInfoList = null;
		}
	}

	@Override
	public void reload(GlobalContext globalContext, HttpSession session) {
		releaseUserInfoList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#store()
	 */
	@Override
	public void store() throws IOException {
		synchronized (lock) {
			unlockStore();
		}
	}

	private void unlockStore() throws IOException {

		List<IUserInfo> userInfoList = getUserInfoList();

		if (userInfoList == null) {
			clearUserInfoList();
		}

		String[][] csvArray = new String[userInfoList.size() + 1][];

		csvArray[0] = createUserInfos().getAllLabels();

		for (int i = 0; i < userInfoList.size(); i++) {
			String[] values = userInfoList.get(i).getAllValues();
			csvArray[i + 1] = values;
		}

		String userInfoPath = getFileName();
		File userInfoFile = new File(userInfoPath);
		if (!userInfoFile.exists()) {
			userInfoFile.getParentFile().mkdirs();
			Logger.log(Logger.WARNING, userInfoFile.getPath() + " not found.");
		}
		FileOutputStream out = null;
		try {
			CSVFactory fact = new CSVFactory(csvArray);
			out = new FileOutputStream(userInfoFile);
			fact.exportCSV(out);
			out.close();
			releaseUserInfoList();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e1) {
					Logger.log(e1);
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#updateUserInfo(org.javlo.user.UserInfos)
	 */
	@Override
	public void updateUserInfo(IUserInfo userInfo) throws IOException {

		synchronized (lock) {
			userInfo.setModificationDate(new Date());
			User user = getUser(userInfo.getLogin());
			IUserInfo currentUserInfo = user.getUserInfo();
			try {
				if (currentUserInfo != null) {
					BeanHelper.copy(userInfo, currentUserInfo);
				}
				unlockStore();
			} catch (Exception e) {
				Logger.log(e);
			}
		}

	}

	/**
	 * retrieve user for change password width special code. Used when user had
	 * forget password.
	 * 
	 * @param passwordChangeCode
	 * @return
	 */
	public IUserInfo getPasswordChangeWidthKey(String passwordChangeCode) {
		return changePasswordReference.get(passwordChangeCode);
	}

	public String createPasswordChangeKey(IUserInfo user) {
		String passwordCode = StringHelper.getRandomString(32, "0123456789abcdefghijklmnopqrstuvwxyz") + StringHelper.getRandomId();
		changePasswordReference.put(passwordCode, user);
		return passwordCode;
	}

}
