package org.javlo.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.service.database.DataBaseService;
import org.javlo.user.exception.UserAllreadyExistException;

public class InternalDBUserFactory extends UserFactory {

	DataBaseService databaseService;

	public static final String DB_NAME = "javlo_users";

	public static Logger logger = Logger.getLogger(InternalDBUserFactory.class.getName());

	protected Connection getConnection() {
		try {
			return databaseService.getConnection(DB_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void releaseConnection(Connection conn) {
		try {
			databaseService.releaseConnection(conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static final InternalDBUserFactory getTestInstance() {
		InternalDBUserFactory instance = new InternalDBUserFactory();
		instance.init(null, null);
		return instance;
	}

	User currentUser;

	public InternalDBUserFactory() {
	}

	private IUserInfo getUserInfoFromResultSet(ResultSet rs, IUserInfo userInfo) throws SQLException {
		userInfo.setLogin(rs.getString("login"));
		userInfo.setPassword(rs.getString("password"));
		userInfo.setEmail(rs.getString("email"));
		userInfo.setFirstName(rs.getString("firstname"));
		userInfo.setLastName(rs.getString("lastname"));
		userInfo.setRoles(StringHelper.stringToSet(rs.getString("roles"), ","));
		return userInfo;
	}

	@Override
	public void addUserInfo(IUserInfo userInfo) throws UserAllreadyExistException {
		Connection conn = getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement("insert into users (login, password, email, firstname, lastname, roles) values (?,?,?,?,?,?)");
			ps.setString(1, StringHelper.neverNull(userInfo.getLogin()));
			ps.setString(2, StringHelper.neverNull(userInfo.getPassword()));
			ps.setString(3, StringHelper.neverNull(userInfo.getEmail()));
			ps.setString(4, StringHelper.neverNull(userInfo.getFirstName()));
			ps.setString(5, StringHelper.neverNull(userInfo.getLastName()));
			ps.setString(6, StringHelper.collectionToString(userInfo.getRoles(), ","));
			ps.execute();
			clearUserInfoList();
		} catch (SQLException e) {
			throw new UserAllreadyExistException(e.getMessage());
		} finally {
			releaseConnection(conn);
		}

	}

	@Override
	public User autoLogin(HttpServletRequest request, String login) {
		return null;
	}

	/*
	 * @Override public User login(GlobalContext globalContext, String login, String
	 * password) { return login((HttpServletRequest) null, login, password); }
	 */

	@Override
	public void clearUserInfoList() {
		userInfoList = null;
	}

	@Override
	public UserInfo createUserInfos() {
		return new UserInfo();
	}

	@Override
	public void deleteUser(String login) {
		Connection conn = getConnection();
		Statement st;
		try {
			st = conn.createStatement();
			st.execute("delete from users where login = '" + login + "'");
			clearUserInfoList();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			releaseConnection(conn);
		}
	}

	@Override
	public User getUser(String login) {
		Connection conn = getConnection();
		Statement st;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("select * from users where login = '" + login + "'");
			if (rs.next()) {
				return new User(getUserInfoFromResultSet(rs, createUserInfos()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

	@Override
	public List<IUserInfo> getUserInfoList() {
		if (userInfoList == null) {
			Connection conn = getConnection();
			List<IUserInfo> newList = new LinkedList<IUserInfo>();
			try {
				Statement st = conn.createStatement();
				ResultSet rs = st.executeQuery("select * from users order by login");
				while (rs.next()) {
					newList.add(getUserInfoFromResultSet(rs, createUserInfos()));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				releaseConnection(conn);
			}
			userInfoList = newList;
		}

		return userInfoList;
	}

	@Override
	public IUserInfo getUserInfos(String id) {
		return null;
	}

	@Override
	public void init(GlobalContext globalContext, HttpSession session) {
		databaseService = DataBaseService.getInstance(globalContext);
		Connection conn = getConnection();
		try {
			Statement st = conn.createStatement();
			st.execute("create table users (login varchar(255) PRIMARY KEY, password varchar(255), email varchar(255), firstname varchar(255), lastname varchar(255), roles varchar(255))");
		} catch (SQLException e) {
			e.printStackTrace();
			logger.warning(e.getMessage());
		} finally {
			releaseConnection(conn);
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
		logger.info("reload H2 DB uselist.");
		userInfoList = null;
	}

	@Override
	public void store() {
	}

	@Override
	public void updateUserInfo(IUserInfo userInfo) {
		Connection conn = getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement("update users set password=?, email=?, firstname=?, lastname=?, roles=? where login=?");
			ps.setString(6, StringHelper.neverNull(userInfo.getLogin()));
			ps.setString(1, StringHelper.neverNull(userInfo.getPassword()));
			ps.setString(2, StringHelper.neverNull(userInfo.getEmail()));
			ps.setString(3, StringHelper.neverNull(userInfo.getFirstName()));
			ps.setString(4, StringHelper.neverNull(userInfo.getLastName()));
			ps.setString(5, StringHelper.collectionToString(userInfo.getRoles(), ","));
			ps.execute();
			clearUserInfoList();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			releaseConnection(conn);
		}
	}

	@Override
	public RoleWrapper getRoleWrapper(ContentContext ctx, User user) {
		return super.getRoleWrapper(ctx, user);
	}
}
