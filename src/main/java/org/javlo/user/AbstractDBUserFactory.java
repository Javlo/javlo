package org.javlo.user;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.user.exception.UserAllreadyExistException;

public abstract class AbstractDBUserFactory extends UserFactory {

	public static Logger logger = Logger.getLogger(AbstractDBUserFactory.class.getName());

	protected static Connection getConnection(GlobalContext globalContext) throws ClassNotFoundException, SQLException, NamingException {
		if (globalContext.getDBResourceName() != null) {
			InitialContext ic = new InitialContext();
			// DataSource ds = (DataSource) ic.lookup("java:comp/env/jdbc/WallyDB");
			logger.info("retreive connection from Resource : " + globalContext.getDBResourceName());
			DataSource ds = (DataSource) ic.lookup(globalContext.getDBResourceName());
			return ds.getConnection();
		} else {
			Class.forName(globalContext.getDBDriver());
			// Define the data source for the driver
			String dbURL = globalContext.getDBURL();
			// Create a connection through the DriverManager
			logger.info("retreive connection from url : " + globalContext.getDBURL());
			return DriverManager.getConnection(dbURL, globalContext.getDBLogin(), globalContext.getDBPassword());
		}
	}

	User currentUser;

	List<IUserInfo> allUsers = Collections.EMPTY_LIST;

	public AbstractDBUserFactory() {
	}

	@Override
	public void addUserInfo(IUserInfo userInfo) throws UserAllreadyExistException {
	}

	@Override
	public User autoLogin(HttpServletRequest request, String login) {
		return null;
	}

	/*
	 * @Override public User login(GlobalContext globalContext, String login, String password) { return login((HttpServletRequest) null, login, password); }
	 */

	@Override
	public void clearUserInfoList() {
		// TODO Auto-generated method stub
	}

	@Override
	public UserInfos createUserInfos() {
		return new UserInfos();
	}

	@Override
	public void deleteUser(String login) {
	}

	@Override
	public Set<String> getAllRoles(GlobalContext globalContext, HttpSession session) {
		return new HashSet<String>();
	}

	@Override
	public User getCurrentUser(HttpSession session) {
		return null;
	}

	@Override
	public User getUser(String login) {
		for (IUserInfo userInfo : getUserInfoList()) {
			if (userInfo.getLogin().equals(login)) {
				currentUser = new User(userInfo);
				logger.fine("load user : " + login);
				return currentUser;
			}
		}
		logger.fine("fail to load user : " + login);
		return null;
	}

	@Override
	public List<IUserInfo> getUserInfoForRoles(String[] inRoles) {
		return getUserInfoList();
	}

	@Override
	public List<IUserInfo> getUserInfoList() {

		if (userInfoList == null) {
			userInfoList = allUsers;			
		}

		return userInfoList;
	}

	@Override
	public IUserInfo getUserInfos(String id) {
		return null;
	}

	@Override
	public void init(GlobalContext globalContext, HttpSession session) {
		if (allUsers.size() == 0) {
			Connection connection = null;
			try {
				connection = getConnection(globalContext);
				Statement stte = connection.createStatement();
				synchronized (allUsers) {
					allUsers = statementToUserInfoList(stte);
					logger.info("load : " + allUsers.size() + " users.");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (connection != null) {
					try {
						connection.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public boolean isStandardStorage() {
		return false;
	}

	@Override
	public User login(HttpServletRequest request, String login, String password) {
		User user = super.login(request, login, password);
		if (user == null) {
			for (IUserInfo userInfo : getUserInfoList()) {
				if (userInfo.getLogin().equals(login) && userInfo.getPassword().equals(password)) {
					logger.fine("log user with password : " + login);
					currentUser = new User(userInfo);
					GlobalContext globalContext = GlobalContext.getInstance(request);
					EditContext editContext = EditContext.getInstance(globalContext, request.getSession());
					editContext.setEditUser(currentUser);
					user = currentUser;
				}
			}
			logger.fine("fail to log user with password : " + login);
		} else {
			currentUser = user;
		}
		return user;
	}

	@Override
	public void mergeUserInfo(IUserInfo userInfo) {
	}

	@Override
	public void releaseUserInfoList() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reload(GlobalContext globalContext, HttpSession session) {
		logger.info("reload DB uselist.");
		userInfoList = null;
		allUsers = new LinkedList<IUserInfo>();
		init(globalContext, session);
	}

	abstract protected List<IUserInfo> statementToUserInfoList(Statement statement) throws SQLException;

	@Override
	public void store() {
	}

	@Override
	public void updateUserInfo(IUserInfo userInfo) {
	}

}
