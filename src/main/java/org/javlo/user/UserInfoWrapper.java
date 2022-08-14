package org.javlo.user;

import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;

public class UserInfoWrapper {
	
	private IUserInfo userInfo;
	private String avatarUrl;
	
	public UserInfoWrapper(ContentContext ctx, IUserInfo userInfo) {
		this.setUserInfo(userInfo);
		this.avatarUrl = URLHelper.createAvatarUrl(ctx, userInfo);
	}

	public IUserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(IUserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public String getAvatar() {
		return avatarUrl;
	}

	public void setAvatar(String avatar) {
		this.avatarUrl = avatar;
	}

}
