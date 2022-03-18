/*
 * Created on 19-fevr.-2004
 */
package org.javlo.user;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.JavaHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.SecurityHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.image.ImageHelper;
import org.javlo.io.TransactionFile;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
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
		// res.init(globalContext, session);
		return res;
	}

	/**
	 * @param request
	 * @return
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#addUserInfo(org.javlo.user.UserInfos)
	 */
	@Override
	public void addOrModifyUserInfo(IUserInfo userInfo) throws UserAllreadyExistException {
		synchronized (lock) {
			userInfo.setModificationDate(new Date());
			if (getUserInfos(userInfo.getLogin()) != null) {
				IUserInfo currentUserInfo = getUserInfos(userInfo.getLogin());
				if (StringHelper.isEmpty(userInfo.getFirstName())) {
					currentUserInfo.setFirstName(userInfo.getFirstName());
				}
				if (StringHelper.isEmpty(userInfo.getLastName())) {
					currentUserInfo.setFirstName(userInfo.getLastName());
				}
				if (StringHelper.isEmpty(userInfo.getEmail())) {
					currentUserInfo.setFirstName(userInfo.getEmail());
				}
				if (userInfo.getRoles().size() > 0) {
					currentUserInfo.addRoles(userInfo.getRoles());
				}
			} else {
				userInfoList = getUserInfoList();
				if (userInfoList == null) {
					clearUserInfoList();
				}
				userInfoList.add(userInfo);
			}
		}
	}

	@Override
	public User adminFakeLogin(HttpServletRequest request, String login) {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		User user = getUser(login);
		if (user != null) {
			UserSecurity.storeShadowUser(request.getSession());
			;
			user.setContext(globalContext.getContextKey());
			request.getSession().setAttribute(getSessionKey(), user);
		}
		return user;
	}

	@Override
	public User autoLogin(HttpServletRequest request, String login) {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		User currentUser = getCurrentUser(globalContext, request.getSession());
		User user = getUser(login);
		if (currentUser != null && user != null && currentUser.getPassword().equals(user.getPassword())) {
			return null;
		} else if (user != null) {
			user.setContext(globalContext.getContextKey());
			request.getSession().setAttribute(getSessionKey(), user);
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
	/**
	 * public User getCurrentUser(HttpSession session) { User user = (User)
	 * session.getAttribute(SESSION_KEY); String globalContextKey =
	 * GlobalContext.getSessionContextKey(session); if (globalContextKey != null &&
	 * user != null) { if (user.getContext().equals(globalContextKey)) { return
	 * user; } else { if (AdminUserSecurity.getInstance().isGod(user)) { return
	 * user; } try { EditContext.getInstance(GlobalContext.getInstance(session,
	 * globalContextKey), session).setEditUser(null); } catch (Exception e) {
	 * e.printStackTrace(); } logger.info("remove user '"+user.getLogin()+ "'
	 * context does'nt match."); session.removeAttribute(SESSION_KEY); return null;
	 * } } return user; }
	 **/
	public User getCurrentUser(GlobalContext globalContext, HttpSession session) {
		User user = (User) session.getAttribute(getSessionKey());
		return user;
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
		logger.info("try login with token token = " + token);
		if (token == null || token.trim().length() == 0) {
			return null;
		}

		GlobalContext globalContext = GlobalContext.getInstance(request);

		if (!globalContext.getStaticConfig().isLoginWithToken()) {
			return null;
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
			request.getSession().setAttribute(getSessionKey(), outUser);
		}
		if (outUser != null) {
			logger.info("user logged with token : " + outUser.getLogin());
		} else {
			logger.info("fail login with token.");
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

	public static final List<IUserInfo> load(File file) throws IOException, SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		InputStream in = new FileInputStream(file);
		CSVFactory fact;
		try {
			fact = new CSVFactory(in);
		} finally {
			ResourceHelper.closeResource(in);
		}
		String[][] csvArray = fact.getArray();
		List<IUserInfo> userInfoList = new LinkedList<IUserInfo>();
		for (int i = 1; i < csvArray.length; i++) {
			IUserInfo newUserInfo = new UserInfo();
			Map<String, String> values = JavaHelper.createMap(csvArray[0], csvArray[i]);
			BeanHelper.copy(values, newUserInfo, false);
			userInfoList.add(newUserInfo);
		}
		return userInfoList;
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
								BeanHelper.copy(values, newUserInfo, false);
								userInfoList.add(newUserInfo);
							}
						} catch (Exception e) {
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
	public String getSessionKey() {
		return "currentUser";
	}

	@Override
	public User login(HttpServletRequest request, String login, String password) {
		logger.fine("try to log : " + login);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		MaxLoginService maxLoginService = MaxLoginService.getInstance();
		if (!maxLoginService.isLoginAuthorised(globalContext)) {

			logger.severe("to many login request : " + request.getRequestURL());

			I18nAccess i18nAccess;
			try {
				i18nAccess = I18nAccess.getInstance(request);
				MessageRepository.getInstance(request).setGlobalMessage(new GenericMessage(i18nAccess.getText("user.too-many-errors", "Too many login failures, try again later."), GenericMessage.ERROR));
			} catch (Exception e) {
				e.printStackTrace();
			}
			// request.getSession().removeAttribute(SESSION_KEY);
			return null;
		}
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		boolean logged = request.getUserPrincipal() != null && request.getUserPrincipal().getName().equals(login);
		User user = getUser(login);
		boolean passwordEqual = false;
		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
		if (user == null && !globalContext.isMaster()) {
			IUserFactory masterUserFactory;
			try {
				masterUserFactory = AdminUserFactory.createUserFactory(GlobalContext.getMasterContext(request.getSession()), request.getSession());
				user = masterUserFactory.getUser(login);
				UserFactory.createUserFactory(globalContext, request.getSession());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (user != null) {
			if (user.getPassword() != null) {
				passwordEqual = user.getPassword().equals(SecurityHelper.encryptPassword(password));
			}
		}
		if (user == null || (!logged && user.getPassword() != null && !passwordEqual)) {
			if (editCtx.getEditUser(login) != null && (logged || editCtx.hardLogin(login, password))) {
				logger.info("log user with password : " + login + " obtain general admin mode and full control role.");
				user = createUser(login, (new HashSet(Arrays.asList(new String[] { AdminUserSecurity.GENERAL_ADMIN, AdminUserSecurity.FULL_CONTROL_ROLE }))));
				editCtx.setEditUser(user);
				/** reload module **/
				try {
					ModulesContext.getInstance(request.getSession(), globalContext).loadModule(request.getSession(), globalContext);
				} catch (ModuleException e) {
					e.printStackTrace();
				}
				if (user != null) {
					user.setEditor(true);
				}
			} else {
				logger.info("fail to log user with password : " + login + ".");
				user = null;
			}
		} else {
			logger.warning("no login.");
		}
		if (user != null) {
			user.setContext(globalContext.getContextKey());
			request.getSession().setAttribute(getSessionKey(), user);
			if (staticConfig.getDefaultPassword().equals(password)) {
				I18nAccess i18nAccess;
				try {
					i18nAccess = I18nAccess.getInstance(request);
					MessageRepository.getInstance(request).setGlobalMessage(new GenericMessage(i18nAccess.getAllText("user.change-password", "Please change you password."), GenericMessage.ALERT));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (user == null) {
			maxLoginService.addBadPassword();
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
		session.removeAttribute(getSessionKey());
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
				updateUserInfoNoStore(userInfo);
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
		if (getCurrentUser(globalContext, session) != null) {
			User newUser = getUser(getCurrentUser(globalContext, session).getLogin());
			if (newUser != null) {
				newUser.setContext(globalContext.getContextKey());
				session.setAttribute(getSessionKey(), newUser);
			} else {
				session.setAttribute(getSessionKey(), null);
			}
		}
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
			userInfoFile.createNewFile();
		}
		OutputStream out = null;
		TransactionFile transactionFile = new TransactionFile(userInfoFile);
		try {
			CSVFactory fact = new CSVFactory(csvArray);
			out = transactionFile.getOutputStream();
			// out = new FileOutputStream(userInfoFile);
			fact.exportCSV(out);
			transactionFile.commit();
			releaseUserInfoList();
		} finally {
			if (out != null) {
				ResourceHelper.closeResource(out);
			}
		}

	}

	public static void store(List<IUserInfo> userInfoList, File userInfoFile) throws IOException {

		String[][] csvArray = new String[userInfoList.size() + 1][];

		csvArray[0] = new UserInfo().getAllLabels();

		for (int i = 0; i < userInfoList.size(); i++) {
			String[] values = userInfoList.get(i).getAllValues();
			csvArray[i + 1] = values;
		}

		if (!userInfoFile.exists()) {
			userInfoFile.getParentFile().mkdirs();
		}
		FileOutputStream out = null;
		try {
			CSVFactory fact = new CSVFactory(csvArray);
			out = new FileOutputStream(userInfoFile);
			fact.exportCSV(out);
			out.close();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e1) {
					// LocalLogger.log(e1);
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
			if (user != null) {
				IUserInfo currentUserInfo = user.getUserInfo();
				try {
					if (currentUserInfo != null) {
						BeanHelper.copyBean(userInfo, currentUserInfo);
					}
					unlockStore();
				} catch (Exception e) {
					// LocalLogger.log(e);
				}
			}
		}

	}

	private void updateUserInfoNoStore(IUserInfo userInfo) throws IOException {
		synchronized (lock) {
			userInfo.setModificationDate(new Date());
			User user = getUser(userInfo.getLogin());
			if (user != null) {
				IUserInfo currentUserInfo = user.getUserInfo();
				try {
					if (currentUserInfo != null) {
						BeanHelper.copyBean(userInfo, currentUserInfo);
					}
				} catch (Exception e) {
					// LocalLogger.log(e);
				}
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

	protected Set<String> getRoleList(ContentContext ctx) {
		return ctx.getGlobalContext().getUserRoles();
	}

	public RoleWrapper getRoleWrapper(ContentContext ctx, User user) {
		RoleWrapper roleWrapper = new RoleWrapper();
		for (String role : user.getRoles()) {
			if (getRoleList(ctx).contains(role)) {
				roleWrapper.addRole(RolesFactory.getInstance(ctx.getGlobalContext()).getRole(ctx.getGlobalContext(), role));
			}
		}
		return roleWrapper;
	}

	@Override
	public String getTokenCreateIfNotExist(User user) throws IOException {
		String token = user.getUserInfo().getToken();
		if (StringHelper.isEmpty(token)) {
			token = URLEncoder.encode(StringHelper.getRandomIdBase64() + StringHelper.encryptPasswordSHA256(user.getLogin()), ContentContext.CHARACTER_ENCODING);
			user.getUserInfo().setToken(token);
			updateUserInfo(user.getUserInfo());
			store();
		}
		return token;
	}

	public static InternetAddress getInternetAddress(User user) {
		if (user == null) {
			return null;
		} else {
			return getInternetAddress(user.getUserInfo());
		}
	}

	public static InternetAddress getInternetAddress(IUserInfo userinfo) {
		if (userinfo != null) {
			if (StringHelper.isMail(userinfo.getEmail())) {
				try {
					String personal = (StringHelper.neverNull(userinfo.getFirstName()) + ' ' + StringHelper.neverNull(userinfo.getLastName())).trim();
					if (personal.length() == 0) {
						personal = userinfo.getEmail();
					}
					return new InternetAddress(userinfo.getEmail(), personal, ContentContext.CHARACTER_ENCODING);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static List<InternetAddress> userListAsInternetAddressList(ContentContext ctx, List<String> users) {
		IUserFactory userFactory = UserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
		IUserFactory adminUserFactory = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
		List<InternetAddress> outAdd = new LinkedList<InternetAddress>();
		for (String login : users) {
			User user = userFactory.getUser(login);
			InternetAddress add = getInternetAddress(user);
			if (add == null && ctx.getGlobalContext().isCollaborativeMode()) {
				user = adminUserFactory.getUser(login);
				add = getInternetAddress(user);
			}
			if (add != null) {
				outAdd.add(add);
			}
		}
		return outAdd;
	}

	public static void uploadNewAvatar(ContentContext ctx, String userName, InputStream in) throws IOException {
		BufferedImage image = ImageIO.read(in);
		if (image != null) {
			image = ImageHelper.resize(image, 1024);
			File imageFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getUserFolder(ctx.getCurrentUser()), "avatar.jpg"));
			if (!imageFile.exists()) {
				imageFile.getParentFile().mkdirs();
				imageFile.createNewFile();
			}
			ImageIO.write(image, "jpg", imageFile);
			logger.info("new avatar uploaded : "+imageFile);
		}
	}

}
