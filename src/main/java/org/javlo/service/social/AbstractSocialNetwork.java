package org.javlo.service.social;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.user.UserInfo;
import org.javlo.user.exception.UserAllreadyExistException;

public abstract class AbstractSocialNetwork implements ISocialNetwork {
	
	protected final class SocialUser {
		private String email;
		private String firstName;
		private String lastName;
		private String avatarURL;
		public String getEmail() {
			return email;
		}
		public SocialUser setEmail(String email) {
			this.email = email;
			return this;
		}
		public String getFirstName() {
			return firstName;
		}
		public SocialUser setFirstName(String firstName) {
			this.firstName = firstName;
			return this;
		}
		public String getLastName() {
			return lastName;
		}
		public SocialUser setLastName(String lastName) {
			this.lastName = lastName;
			return this;
		}
		public String getAvatarURL() {
			return avatarURL;
		}
		public SocialUser setAvatarURL(String avatarURL) {
			this.avatarURL = avatarURL;
			return this;
		}
		
	}

	protected final Map<String, String> data = new HashMap<String, String>();

	private static final String TOKEN = "token";
	private static final String CLIENT_ID = "clientnId";
	private static final String CLIENT_SECRET = "clientSecret";
	private static final String URL = "url";
	private static final String LOGIN = "login";
	
	private String redirectURL = null;
	
	@Override
	public void prepare(ContentContext ctx) throws Exception {
		ctx.getRequest().setAttribute(getName()+"_signinURL", getSigninURL(ctx));
	}

	@Override
	public String getToken() {
		return StringHelper.neverNull(data.get(TOKEN));
	}

	@Override
	public String getLogin() {
		return StringHelper.neverNull(data.get(LOGIN));
	}

	@Override
	public String getURL() {
		return StringHelper.neverNull(data.get(URL));
	}

	@Override
	public Map<String, String> getData() {
		return data;
	}

	@Override
	public void setToken(String token) {
		data.put(TOKEN, token);
	}

	@Override
	public void setURL(String url) {
		data.put(URL, url);
	}

	@Override
	public void setLogin(String login) {
		data.put(LOGIN, login);
	}

	@Override
	public void set(String key, String value) {
		data.put(key, value);
	}

	@Override
	public void update(Map map) {
		setURL(StringHelper.neverNull(map.get("url")));
		setToken(StringHelper.neverNull(map.get("token")));
		setLogin(StringHelper.neverNull(map.get("login")));
		setClientId(StringHelper.neverNull(map.get("clientid")));
		setClientSecret(StringHelper.neverNull(map.get("clientsecret")));
	}
	
	@Override
	public String getClientId() {
		return StringHelper.neverNull(data.get(CLIENT_ID));
	}
	
	public void setClientId(String clientId) {
		data.put(CLIENT_ID, clientId);
	}
	
	@Override
	public String getClientSecret() {
		return StringHelper.neverNull(data.get(CLIENT_SECRET));
	}
	
	public void setClientSecret(String clientSecret) {
		data.put(CLIENT_SECRET, clientSecret);
	}
	
	protected String getState(ContentContext ctx) throws Exception {
		Map<String,String> params = new HashMap<String, String>();
		params.put("name", getName());
		params.put("page", ctx.getCurrentPage().getId());
		return StringHelper.mapToString(params);
	}
	
	@Override
	public String getSigninURL(ContentContext ctx) throws OAuthSystemException, Exception {	
		if (getClientId().length() == 0 || getClientSecret().length() == 0) {
			return null;
		}
		Map<String,String> params = new HashMap<String, String>();
		params.put("socialNetwork", getName());
		params.put("page", URLHelper.createURL(ctx));
		OAuthClientRequest request = OAuthClientRequest
				   .authorizationProvider(getProviderType())
				   .setClientId(getClientId())
				   .setResponseType(OAuth.OAUTH_CODE)
				   .setState(StringHelper.mapToString(params))				   
				   .setRedirectURI(getRedirectURL())
				   .buildQueryMessage();
		
		return request.getLocationUri();
	}
	
	public String getRedirectURL() {
		return redirectURL;
	}

	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}
	
	@Override
	public void performRedirect(HttpServletRequest request, HttpServletResponse response) {
	}
	
	protected void login(ContentContext ctx, SocialUser socialUser) throws UserAllreadyExistException, IOException {
		IUserFactory userFactory = UserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
		User user = userFactory.getUser(socialUser.getEmail());
		if (user == null) {
			UserInfo userInfo = new UserInfo();
			userInfo.setLogin(socialUser.getEmail());
			userInfo.setEmail(socialUser.getEmail());
			userInfo.setFirstName(socialUser.getFirstName());
			userInfo.setLastName(socialUser.getLastName());
			userInfo.setAvatarURL(socialUser.getAvatarURL());
			userInfo.setAccountType("oauth");
			userFactory.addUserInfo(userInfo);
			userFactory.store();			
		}	
		userFactory.autoLogin(ctx.getRequest(),socialUser.getEmail());
	}

}
