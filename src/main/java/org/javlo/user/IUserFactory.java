package org.javlo.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.context.GlobalContext;
import org.javlo.user.exception.UserAllreadyExistException;


public interface IUserFactory {

	public static final int AUTO_LOGIN_AGE_SEC = 60*60*24*30; // 1 month for autologin live cycle

	public abstract User getUser(String login);

	public abstract User autoLogin(HttpServletRequest request, String login);

	public abstract User login(HttpServletRequest request, String login, String password);

	//public abstract User login(GlobalContext globalContext, String login, String password);

	public abstract void logout(HttpSession session);

	public abstract User getCurrentUser(HttpSession session);

	public abstract void setCurrentUser(HttpSession session, User user);

	public abstract void releaseUserInfoList();

	public abstract void clearUserInfoList();

	public abstract IUserInfo[] getUserInfoList();

	public abstract void addUserInfo(IUserInfo userInfo) throws UserAllreadyExistException;

	public abstract void mergeUserInfo(IUserInfo userInfo);

	public abstract void updateUserInfo(IUserInfo userInfo);

	public abstract void deleteUser(String login);

	public abstract void store();

	public abstract IUserInfo createUserInfos();

	public abstract IUserInfo getUserInfos(String id);

	public abstract IUserInfo[] getUserInfoForRoles(String[] inRoles);

	public abstract String[] getAllRoles(GlobalContext globalContext, HttpSession session);

	public abstract void init(GlobalContext globalContext, HttpSession newSession);

	public abstract void reload(GlobalContext globalContext, HttpSession session);

	/**
	 * check if the user system use standard storage system of wcms
	 * @return true if standard system is used, false else a external system is used.
	 */
	public abstract boolean isStandardStorage();	
	
}