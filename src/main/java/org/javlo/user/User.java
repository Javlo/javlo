/*
 * Created on 19-f�vr.-2004
 */
package org.javlo.user;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author pvandermaesen
 */
public class User implements Principal {

	String id=null;

	String login;
	String password;
	Set<String> roles = new HashSet<String>();
	IUserInfo userInfo;

	public User(){ // user only for debug
		login="debug1";
		password="";
		userInfo = new UserInfos();
	};

	public User(IUserInfo newUserInfo) {
		setUserInfo(newUserInfo);
	}

	public User(String newLogin, String newPassword ) {
		login = newLogin;
		password = newPassword;
	}

	public User(String newLogin, String newPassword, String[] newRoles) {
		login = newLogin;
		password = newPassword;
		roles.addAll(Arrays.asList(newRoles));
	}

	/**
	 * @return
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return
	 */
	public Set<String> getRoles() {
		return roles;
	}

	public boolean validForRoles ( String[] newRoles ) {
		boolean res = newRoles.length==0;
		for (int i = 0; (i<newRoles.length)&&(!res); i++) {
			res = getRoles().contains(newRoles[i]);
		}
		return res;
	}

	public boolean validForRoles ( Set<String> rolesSet ) {
		Set<String> workingRoles = new HashSet<String>();
		workingRoles.addAll(rolesSet);
		return workingRoles.retainAll(getRoles());
	}

	/**
	 * @return
	 */
	public IUserInfo getUserInfo() {
		return userInfo;
	}

	/**
	 * @param info
	 */
	public void setUserInfo(IUserInfo info) {
		login=info.getLogin();
		password=info.getPassword();
		roles=info.getRoles();
		userInfo = info;
	}

	/**
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param string
	 */
	public void setId(String string) {
		id = string;
	}

	@Override
	public String getName() {
		return getLogin();
	}

	@Override
	public String toString() {
		return getName();
	}

}
