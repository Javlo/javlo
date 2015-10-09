/*
 * Created on 19-fevr.-2004
 */
package org.javlo.user;

import java.io.Serializable;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

/**
 * @author pvandermaesen
 */
public class User implements Principal, Serializable {

	String login;
	String password;
	private String context;
	Set<String> roles = new HashSet<String>();
	IUserInfo userInfo;
	private boolean editor = false;

	public User() { // user only for debug
		login = "debug1";
		password = "";
		userInfo = new UserInfo();
	};

	public User(IUserInfo newUserInfo) {
		setUserInfo(newUserInfo);
	}

	public User(String newLogin, String newPassword) {
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
	
	
	/**
	 * check if user can work with this roles.
	 * admin return always true.
	 * @param roles
	 * @return
	 */
	public boolean validForRoles(String... roles) {
		if (AdminUserSecurity.getInstance().isAdmin(this)) {
			return true;
		}
		Set<String> workingRoles = new HashSet<String>();
		for (String role : roles) {
			workingRoles.add(role);	
		}		
		workingRoles.retainAll(getRoles());
		return workingRoles.size() > 0;
	}

	public boolean validForRoles(Set<String> rolesSet) {
		if (AdminUserSecurity.getInstance().isAdmin(this) || rolesSet.size() == 0) {
			return true;
		}
		Set<String> workingRoles = new HashSet<String>();
		workingRoles.addAll(rolesSet);
		workingRoles.retainAll(getRoles());
		return workingRoles.size() > 0;
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
		login = info.getLogin();
		password = info.getPassword();
		roles = info.getRoles();
		userInfo = info;
	}

	@Override
	public String getName() {
		return getLogin();
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
	
	public boolean isRightPassword(String pwd, boolean encrypt) {
		if (encrypt) {
			pwd = StringHelper.encryptPassword(pwd);
		}
		return getPassword().equals(pwd);
	}

	public boolean isEditor() {
		return editor;
	}

	public void setEditor(boolean editor) {
		this.editor = editor;
	}
	
	public String getLabel() {
		if (userInfo.getFirstName() != null && userInfo.getFirstName().trim().length() > 0) {
			return userInfo.getFirstName()+' '+StringHelper.neverNull(getUserInfo().getLastName());
		} else {
			return login;
		}
	}

}
