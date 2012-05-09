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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;
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

	protected IUserInfo[] userInfoList = null; // TODO: create a external
	// application scope class

	static final String SESSION_KEY = "currentUser";

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

			if (getUserInfos(userInfo.id()) != null) {
				throw new UserAllreadyExistException(userInfo.id() + " allready exist.");
			}

			userInfoList = getUserInfoList();

			if (userInfoList == null) {
				clearUserInfoList();
			}

			IUserInfo[] newUserInfoList = new UserInfos[userInfoList.length + 1];

			newUserInfoList[newUserInfoList.length - 1] = userInfo;
			System.arraycopy(userInfoList, 0, newUserInfoList, 0, userInfoList.length);
			userInfoList = newUserInfoList;

		}
	}

	@Override
	public User autoLogin(HttpServletRequest request, String login) {
		GlobalContext globalCtx = GlobalContext.getInstance(request);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());

		boolean logged = request.getUserPrincipal() != null && request.getUserPrincipal().getName().equals(login);
		User user = getUser(login);

		if (user == null) {
			// administrator auto login not possible
			if (editCtx.getEditUser(login) != null && (logged || editCtx.hardAutoLogin(login))) {
				user = createUser(login, new String[] { AdminUserSecurity.GENERAL_ADMIN, AdminUserSecurity.FULL_CONTROL_ROLE });
				editCtx.setEditUser(user);
			} else {
				user = null;
			}
		}
		if (user != null && globalCtx.getAdministrator().equals(user.getLogin())) {
			user.getUserInfo().addRoles(new String[] { AdminUserSecurity.FULL_CONTROL_ROLE });
		}
		if (user != null && editCtx.getEditUser(user.getLogin()) != null) {
			user.getUserInfo().addRoles(new String[] { AdminUserSecurity.GENERAL_ADMIN, AdminUserSecurity.FULL_CONTROL_ROLE });
		}
		request.getSession().setAttribute(SESSION_KEY, user);
		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#clearUserInfoList()
	 */
	@Override
	public void clearUserInfoList() {
		synchronized (lock) {
			userInfoList = new IUserInfo[0];
		}
	}

	protected User createUser(String login, String[] roles) {
		UserInfos ui = createUserInfos();
		ui.setLogin(login);
		ui.setRoles(roles);

		User user = new User(ui);
		user.setId(login);

		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#createUserInfos()
	 */
	@Override
	public UserInfos createUserInfos() {
		return new UserInfos();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#deleteUser(java.lang.String)
	 */
	@Override
	public void deleteUser(String login) {

		synchronized (lock) {

			userInfoList = getUserInfoList();

			int toDel = -1;
			for (int i = 0; i < userInfoList.length; i++) {
				if (userInfoList[i].getLogin().equals(login)) {
					toDel = i;
				}
			}
			if (toDel > -1) {
				IUserInfo[] newList = new UserInfos[userInfoList.length - 1];
				for (int i = 0; i < toDel; i++) {
					newList[i] = userInfoList[i];
				}
				for (int i = toDel + 1; i < userInfoList.length; i++) {
					newList[i - 1] = userInfoList[i];
				}
				userInfoList = newList;
				unlockStore();
			}

		}

	}

	@Override
	public String[] getAllRoles(GlobalContext globalContext, HttpSession session) {
		EditContext ctx = EditContext.getInstance(globalContext, session);
		return ctx.getUserRoles();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#getCurrentUser()
	 */
	@Override
	public User getCurrentUser(HttpSession session) {
		return (User) session.getAttribute(SESSION_KEY);
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
		IUserInfo[] users = getUserInfoList();
		for (IUserInfo user : users) {
			if (user.getLogin().equals(login)) {
				return new User(user);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#getUserInfoForRoles(java.lang.String[])
	 */
	@Override
	public IUserInfo[] getUserInfoForRoles(String[] inRoles) {
		Set<String> roles = new HashSet<String>();
		for (String inRole : inRoles) {
			roles.add(inRole);
		}
		Collection<IUserInfo> outUserList = new ArrayList<IUserInfo>();
		IUserInfo[] allUserInfo = getUserInfoList();
		for (IUserInfo element : allUserInfo) {
			Set<String> userRoles = new TreeSet<String>(Arrays.asList(element.getRoles()));
			userRoles.retainAll(roles);
			if (userRoles.size() > 0) {
				outUserList.add(element);
			}
		}
		UserInfos[] finalResult = new UserInfos[outUserList.size()];
		outUserList.toArray(finalResult);
		return finalResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#getUserInfoList()
	 */
	@Override
	public IUserInfo[] getUserInfoList() {

		synchronized (lock) {
			if (userInfoList == null) {
				String userInfoPath = getFileName();
				File userInfoFile = new File(userInfoPath);
				if (!userInfoFile.exists()) {
					Logger.log(Logger.WARNING, userInfoFile.getPath() + " not found.");
					userInfoList = new IUserInfo[0];
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
						userInfoList = new IUserInfo[csvArray.length - 1];
						for (int i = 1; i < csvArray.length; i++) {
							userInfoList[i - 1] = createUserInfos();
							Map<String, String> values = JavaHelper.createMap(csvArray[0], csvArray[i]);
							BeanHelper.copy(values, userInfoList[i - 1]);
						}
					} catch (Exception e) {
						Logger.log(e);
						userInfoList = new UserInfos[0];
					}
				}
			}
			Arrays.sort(userInfoList);
			return userInfoList;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#getUserInfos(java.lang.String)
	 */
	@Override
	public IUserInfo getUserInfos(String id) {
		IUserInfo res = null;
		IUserInfo[] userInfoList = getUserInfoList();
		synchronized (lock) {
			for (int i = 0; (i < userInfoList.length) && (res == null); i++) {
				if (userInfoList[i].id().equals(id)) {
					res = userInfoList[i];
				}
			}
		}
		return res;
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

		boolean passwordEqual = false;
		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());

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
				user = createUser(login, new String[] { AdminUserSecurity.FULL_CONTROL_ROLE });
			} else if (editCtx.getEditUser(login) != null && (logged || editCtx.hardLogin(login, password))) {
				logger.fine("log user with password : " + login + " obtain general addmin mode and full control role.");
				user = createUser(login, new String[] { AdminUserSecurity.GENERAL_ADMIN, AdminUserSecurity.FULL_CONTROL_ROLE });				
				editCtx.setEditUser(user);
			} else {
				logger.fine("fail to log user with password : " + login + ".");
				user = null;
			}
		}
		if (user != null && globalCtx.getAdministrator().equals(user.getLogin())) {
			user.getUserInfo().addRoles(new String[] { AdminUserSecurity.FULL_CONTROL_ROLE });
		}
		if (user != null && editCtx.getEditUser(user.getLogin()) != null) {
			user.getUserInfo().addRoles(new String[] { AdminUserSecurity.GENERAL_ADMIN, AdminUserSecurity.FULL_CONTROL_ROLE });
		}
		request.getSession().setAttribute(SESSION_KEY, user);

		return user;
	}

	/**
	 * @deprecated, use login(HttpServletRequest request, String login, String password) instead
	 */
	/*
	 * @Override public User login(GlobalContext globalContext, String login, String password) { User user = null; synchronized (lock) { IUserInfo[] users = getUserInfoList(); for (int i = 0; i < users.length; i++) { if (users[i].getLogin() == null) { logger.severe("bad user structure : login not found."); } else if (users[i].getPassword() == null) { logger.severe("bad user structure : password not found ["+users[i].getLogin()+"]"); } else if ((users[i].getLogin().equals(login)) && (users[i].getPassword().equals(password))) { user = new User(users[i]); logger.info("login: " + login + " are logged."); } } } if (user == null) { if (globalContext.administratorLogin(login, password)) { user = createUser(login, new String[] { AdminUserSecurity.FULL_CONTROL_ROLE }); } } if (user == null) { logger.info("login: " + login + " fail, try hard login."); EditContext editCtx = EditContext.getInstance(session); editCtx.hardLogin(login, password); user = editCtx.getEditUser(); } session.setAttribute(SESSION_KEY, user); return user; }
	 */

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
	public void mergeUserInfo(IUserInfo userInfo) {

		synchronized (mergeLock) {

			IUserInfo currentUserInfo = getUserInfos(userInfo.id());
			if (currentUserInfo == null) {
				try {
					addUserInfo(userInfo);
				} catch (UserAllreadyExistException e) {
					e.printStackTrace();
				}
			} else {
				String[] roles = userInfo.getRoles();
				String[] currentRoles = currentUserInfo.getRoles();
				List<String> rolesList = new LinkedList<String>();
				rolesList.addAll(Arrays.asList(roles));
				for (int i = 0; i < currentRoles.length; i++) {
					if (!rolesList.contains(currentRoles[i])) {
						rolesList.add(currentRoles[i]);
					}
				}
				String[] newRoles = new String[rolesList.size()];
				rolesList.toArray(newRoles);
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
	}

	public void setContext(ServletContext application) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCurrentUser(HttpSession session, User user) {
		session.setAttribute(SESSION_KEY, user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#store()
	 */
	@Override
	public void store() {
		synchronized (lock) {
			unlockStore();
		}
	}

	private void unlockStore() {

		IUserInfo[] userInfoList = getUserInfoList();

		if (userInfoList == null) {
			clearUserInfoList();
		}

		String[][] csvArray = new String[userInfoList.length + 1][];

		csvArray[0] = createUserInfos().getAllLabels();

		for (int i = 1; i < userInfoList.length + 1; i++) {
			// UserInfos userInfo = (UserInfos)userInfoList[i-1];
			String[] values = userInfoList[i - 1].getAllValues();
			csvArray[i] = values;
		}

		String userInfoPath = getFileName();
		File userInfoFile = new File(userInfoPath);
		if (!userInfoFile.exists()) {
			userInfoFile.getParentFile().mkdirs();
			Logger.log(Logger.WARNING, userInfoFile.getPath() + " not found.");
			userInfoList = new UserInfos[0];
		}
		FileOutputStream out = null;
		try {
			CSVFactory fact = new CSVFactory(csvArray);
			out = new FileOutputStream(userInfoFile);
			fact.exportCSV(out);
			out.close();
			releaseUserInfoList();
		} catch (Exception e) {
			Logger.log(e);
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
	public void updateUserInfo(IUserInfo userInfo) {

		synchronized (lock) {

			userInfo.setModificationDate(new Date());

			IUserInfo oldUserInfo = getUserInfos(userInfo.id());
			try {
				if (oldUserInfo != null) {
					BeanHelper.copy(userInfo, oldUserInfo);
				}
				unlockStore();
			} catch (Exception e) {
				Logger.log(e);
			}

		}

	}

}
