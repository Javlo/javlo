package org.javlo.user;

public class PublicUserInfo {

	private IUserInfo userInfo;
	
	public PublicUserInfo(IUserInfo userInfo) {
		this.userInfo = userInfo;
	}
	
	public String getLogin() {
		return userInfo.getLogin();
	}
	
	public String getFirstName() {
		return userInfo.getFirstName();
	}
	
	public String getLastName() {
		return userInfo.getFirstName();
	}
	
	public String getAvatarURL() {
		return userInfo.getAvatarURL();
	}

}
