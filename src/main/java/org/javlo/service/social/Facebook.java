package org.javlo.service.social;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest.AuthenticationRequestBuilder;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest.TokenRequestBuilder;
import org.apache.oltu.oauth2.client.response.GitHubTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.javlo.context.ContentContext;
import org.javlo.helper.NetHelper;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserInfo;
import org.javlo.utils.JSONMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Facebook extends AbstractSocialNetwork {

	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Facebook.class.getName());

	public static final Gson JSON = new GsonBuilder().setDateFormat("yyyy-MM-dd_HH-mm-ss-SSS ").create();

	private static final String DEFAULT_SCOPE = "email";

	@Override
	public String getName() {
		return "facebook";
	}

	@Override
	public OAuthProviderType getProviderType() {
		return OAuthProviderType.FACEBOOK;
	}

	@Deprecated
	public IUserInfo getInitialUserInfo(String accessToken) throws Exception {
		URL url;
		url = new URL("https://api-read.facebook.com/restserver.php?access_token=" + accessToken + "&api_key=" + getClientId() + "&format=json-strings&method=fql.query&pretty=0&query=SELECT%20name%2C%20first_name%2C%20last_name%2C%20uid%2C%20email%20FROM%20user%20WHERE%20uid%3D693608149&sdk=joey");

		logger.info("read info from DB : " + url);

		String jsonStr = NetHelper.readPage(url);

		TypeToken<ArrayList<Map<String, String>>> list = new TypeToken<ArrayList<Map<String, String>>>() {
		};
		List<Map<String, String>> info = (List<Map<String, String>>) JSONMap.JSON.fromJson(jsonStr, list.getType());

		IUserInfo ui = new UserInfo();
		ui.setLogin("" + info.get(0).get("email"));
		ui.setEmail("" + info.get(0).get("email"));
		ui.setFirstName("" + info.get(0).get("first_name"));
		ui.setLastName("" + info.get(0).get("last_name"));
		return ui;
	}

	@Override
	protected void configureAuthenticationRequest(AuthenticationRequestBuilder builder, String clientId, ContentContext ctx) throws Exception {
		super.configureAuthenticationRequest(builder, clientId, ctx);
		builder.setScope(DEFAULT_SCOPE);
	}

	@Override
	protected void configureTokenRequest(TokenRequestBuilder builder, String clientId, String clientSecret, String code) {
		super.configureTokenRequest(builder, clientId, clientSecret, code);
		builder.setScope(DEFAULT_SCOPE);
	}

	@Override
	protected OAuthAccessTokenResponse executeTokenRequest(OAuthClient oAuthClient, OAuthClientRequest request) throws OAuthSystemException, OAuthProblemException {
		//Facebook is not fully compatible with OAuth 2.0 draft 10, access token response is
		//application/x-www-form-urlencoded, not json encoded so we use dedicated response class for that
		//Custom response classes are an easy way to deal with oauth providers that introduce modifications to
		//OAuth 2.0 specification
		return oAuthClient.accessToken(request, GitHubTokenResponse.class);
	}

	@Override
	protected SocialUser getSocialUser(String accessToken, OAuthClient oAuthClient) throws Exception {
		OAuthClientRequest request = new OAuthBearerClientRequest("https://graph.facebook.com/me")
				.setAccessToken(accessToken)
				.buildQueryMessage();

		OAuthResourceResponse resourceResponse = oAuthClient.resource(request, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
		String body = resourceResponse.getBody();
		//TODO remove sysout
		System.out.println("Facebook.getSocialUser() - Response body:" + body);
		Map<String, Object> userInformation = JSONMap.parseMap(body);
		return new SocialUser()
				.setEmail((String) userInformation.get("email"))
				.setFirstName((String) userInformation.get("first_name"))
				.setLastName((String) userInformation.get("last_name"))
		//.setAvatarURL((String) userInformation.get("picture"))
		;
	}

}
