package org.javlo.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.user.exception.UserAllreadyExistException;

public class HardUserFactory extends AdminUserFactory {

	IUserInfo[] userInfoList;
	User currentUser;

	public HardUserFactory() {
		userInfoList = new IUserInfo[2];
		UserInfos ui = new UserInfos();
		ui.setId("test1");
		ui.setLogin("test1");
		ui.setEmail("test1@noctis.be");
		ui.setPassword("test1");
		ui.setRoles(new String[0]);
		ui.setPreferredLanguageRaw("fr");
		userInfoList[0] = ui;
		ui = new UserInfos();
		ui.setId("test2");
		ui.setLogin("test2");
		ui.setEmail("test2@noctis.be");
		ui.setPassword("test2");
		ui.setRoles(new String[0]);
		ui.setPreferredLanguageRaw("fr");
		userInfoList[1] = ui;
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
	public UserInfos createUserInfos() {
		return new UserInfos();
	}

	@Override
	public void deleteUser(String login) {
	}

	@Override
	public String[] getAllRoles(GlobalContext globalContext, HttpSession session) {
		return new String[0];
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
	public IUserInfo[] getUserInfoForRoles(String[] inRoles) {
		return getUserInfoList();
	}

	@Override
	public IUserInfo[] getUserInfoList() {
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
