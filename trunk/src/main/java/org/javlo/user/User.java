/*
 * Created on 19-f�vr.-2004
 */
package org.javlo.user;

import java.security.Principal;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author pvandermaesen
 */
public class User implements Principal {

	String id=null;

	String login;
	String password;
	String[] roles = new String[0];
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
		roles = newRoles;
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
	public String[] getRoles() {
		return roles;
	}

	public boolean validForRoles ( String[] newRoles ) {
		boolean res = newRoles.length==0;
		Set<String> rolesSet = new TreeSet<String> ( Arrays.asList(roles) );
		for (int i = 0; (i<newRoles.length)&&(!res); i++) {
			res = rolesSet.contains(newRoles[i]);
		}
		return res;
	}

	public boolean validForRoles ( Set<String> rolesSet ) {
		boolean res = rolesSet.size()==0;
		for (int i = 0; (i<roles.length)&&(!res); i++) {
			res = rolesSet.contains(roles[i]);
		}
		return res;
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
