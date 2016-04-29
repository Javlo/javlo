package org.javlo.service.social;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.client.HttpClient;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest.AuthenticationRequestBuilder;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest.TokenRequestBuilder;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.user.IUserFactory;
import org.javlo.user.TransientUserInfo;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.user.UserInfo;
import org.javlo.user.exception.UserAllreadyExistException;

public abstract class AbstractSocialNetwork implements ISocialNetwork {

	protected static class SocialUser {
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

	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AbstractSocialNetwork.class.getName());

	protected final Map<String, String> data = new HashMap<String, String>();

	private static final String TOKEN = "token";
	private static final String CLIENT_ID = "clientId";
	private static final String CLIENT_SECRET = "clientSecret";
	private static final String URL = "url";
	private static final String LOGIN = "login";

	private String redirectURL = null;

	@Override
	public void prepare(ContentContext ctx) throws Exception {
		ctx.getRequest().setAttribute(getName() + "_signinURL", getSigninURL(ctx));
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
		Map<String, String> params = new HashMap<String, String>();
		params.put("name", getName());
		params.put("page", ctx.getCurrentPage().getId());
		return StringHelper.mapToString(params);
	}

	//public abstract OAuthProviderType getProviderType();
	
	public abstract String getAuthzEndpoint(); 
	
	public abstract String getTokenEndpoint();

	@Override
	public String getSigninURL(ContentContext ctx) throws Exception {
		String clientId = getClientId();
		if (clientId == null || clientId.isEmpty()) {
			return null;
		}
		String clientSecret = getClientSecret();
		if (clientSecret == null || clientSecret.isEmpty()) {
			return null;
		}
		AuthenticationRequestBuilder builder = createAuthenticationRequest();
		configureAuthenticationRequest(builder, clientId, ctx);
		OAuthClientRequest request = buildAuthenticationRequest(builder);
		return request.getLocationUri();
	}

	protected AuthenticationRequestBuilder createAuthenticationRequest() {
		return OAuthClientRequest.authorizationLocation(getAuthzEndpoint());
	}

	protected void configureAuthenticationRequest(AuthenticationRequestBuilder builder, String clientId, ContentContext ctx) throws Exception {
		builder
				.setClientId(clientId)
				.setResponseType(OAuth.OAUTH_CODE)
				.setState(getState(ctx))
				.setRedirectURI(getRedirectURL());
	}

	protected OAuthClientRequest buildAuthenticationRequest(AuthenticationRequestBuilder builder) throws OAuthSystemException {
		return builder.buildQueryMessage();
	}

	public String getRedirectURL() {
		return redirectURL;
	}

	@Override
	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}

	@Override
	public void performRedirect(HttpServletRequest request, HttpServletResponse response) {
		HttpClient httpClient = null;
		try {
			httpClient = new URLConnectionClient();
			OAuthClient oAuthClient = new OAuthClient(httpClient);
			OAuthAuthzResponse oar = OAuthAuthzResponse.oauthCodeAuthzResponse(request);
			String code = oar.getCode();
			String accessToken = getAccessToken(code, oAuthClient);
			TransientUserInfo.getInstance(request.getSession()).setToken(accessToken);
			SocialUser user = getSocialUser(accessToken, oAuthClient);
			if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
				logger.warning("OAuth login failed with provider: " + getName());
				return;
			}
			ContentContext ctx = ContentContext.getContentContext(request, response);
			login(ctx, user);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (httpClient != null) {
				httpClient.shutdown();
			}
		}
	}

	private String getAccessToken(String code, OAuthClient oAuthClient) throws OAuthSystemException, OAuthProblemException {
		String clientId = getClientId();
		if (clientId == null || clientId.isEmpty()) {
			return null;
		}
		String clientSecret = getClientSecret();
		if (clientSecret == null || clientSecret.isEmpty()) {
			return null;
		}
		TokenRequestBuilder builder = createTokenRequest();
		configureTokenRequest(builder, clientId, clientSecret, code);
		OAuthClientRequest request = buildTokenRequest(builder);

		OAuthAccessTokenResponse response = executeTokenRequest(oAuthClient, request);

		String accessToken = response.getAccessToken();
		Long expiresIn = response.getExpiresIn();

		//TODO remove sysouts
		System.out.println("accessToken = " + accessToken);
		System.out.println("expiresIn = " + expiresIn);

		return accessToken;
	}

	protected OAuthAccessTokenResponse executeTokenRequest(OAuthClient oAuthClient, OAuthClientRequest request) throws OAuthSystemException, OAuthProblemException {
		return oAuthClient.accessToken(request, OAuthJSONAccessTokenResponse.class);
	}

	protected TokenRequestBuilder createTokenRequest() {
		return OAuthClientRequest.tokenLocation(getTokenEndpoint());
	}

	protected void configureTokenRequest(TokenRequestBuilder builder, String clientId, String clientSecret, String code) {
		builder
				.setGrantType(GrantType.AUTHORIZATION_CODE)
				.setClientId(clientId)
				.setClientSecret(clientSecret)
				.setRedirectURI(getRedirectURL())
				.setCode(code);
	}

	protected OAuthClientRequest buildTokenRequest(TokenRequestBuilder builder) throws OAuthSystemException {
		return builder.buildQueryMessage();
	}

	protected SocialUser getSocialUser(String accessToken, OAuthClient oAuthClient) throws Exception {
		return null;
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
			fillUserInfo(userInfo, socialUser);
			userFactory.addUserInfo(userInfo);
			userFactory.store();
		}
		userFactory.autoLogin(ctx.getRequest(), socialUser.getEmail());
	}

	protected void fillUserInfo(UserInfo userInfo, SocialUser socialUser) {
	}
	
	public String getLoginURL() {
		String url = getAuthzEndpoint();
		url = URLHelper.addParam(url, "client_id", getClientId());
		url = URLHelper.addParam(url, "redirect_uri", getRedirectURL());
		url = URLHelper.addParam(url, "response_type", "code");
		return url;
	}
	
}
