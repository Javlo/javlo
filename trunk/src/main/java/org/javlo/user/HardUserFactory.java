package org.javlo.user;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.user.exception.UserAllreadyExistException;

public class HardUserFactory extends AdminUserFactory {

	List<IUserInfo> userInfoList;
	User currentUser;

	public HardUserFactory() {
		userInfoList = new LinkedList<IUserInfo>();
		UserInfo ui = new UserInfo();
		ui.setId("test1");
		ui.setLogin("test1");
		ui.setEmail("test1@noctis.be");
		ui.setPassword("test1");
		ui.setRoles(new HashSet<String>());
		ui.setPreferredLanguageRaw("fr");
		userInfoList.add(ui);
		ui = new UserInfo();
		ui.setId("test2");
		ui.setLogin("test2");
		ui.setEmail("test2@noctis.be");
		ui.setPassword("test2");
		ui.setRoles(new HashSet<String>());
		ui.setPreferredLanguageRaw("fr");
		userInfoList.add(ui);
	}

	@Override
	public void addUserInfo(IUserInfo userInfo) throws UserAllreadyExistException {
	}

	@Override
	public void clearUserInfoList() {
		// TODO Auto-generated method stub

	}

	/*
	 * @Override public User login(GlobalContext globalContext, String login, String password) { return login((HttpServletRequest) null, login, password); }
	 */

	@Override
	public UserInfo createUserInfos() {
		return new UserInfo();
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
		return currentUser;
	}

	@Override
	public User getUser(String login) {
		for (IUserInfo userInfo : userInfoList) {
			if (userInfo.getLogin().equals(login)) {
				currentUser = new User(userInfo);
				return currentUser;
			}
		}
		return null;
	}

	@Override
	public List<IUserInfo> getUserInfoForRoles(String[] inRoles) {
		return getUserInfoList();
	}

	@Override
	public List<IUserInfo> getUserInfoList() {
		return userInfoList;
	}

	@Override
	public IUserInfo getUserInfos(String id) {
		return null;
	}

	@Override
	public void init(GlobalContext globalContext, HttpSession session) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isStandardStorage() {
		return false;
	}

	@Override
	public User login(HttpServletRequest request, String login, String password) {
		User user = super.login(request, login, password);
		if (user == null) {
			for (IUserInfo userInfo : userInfoList) {
				if (userInfo.getLogin().equals(login) && userInfo.getPassword().equals(password)) {
					currentUser = new User(userInfo);
					GlobalContext globalContext = GlobalContext.getInstance(request);
					EditContext editContext = EditContext.getInstance(globalContext, request.getSession());
					editContext.setEditUser(currentUser);
					user = currentUser;
				}
			}
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
	public void store() {
	}

	@Override
	public void updateUserInfo(IUserInfo userInfo) {
	}

}
