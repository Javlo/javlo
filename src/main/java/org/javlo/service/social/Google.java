package org.javlo.service.social;

import java.util.Map;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest.AuthenticationRequestBuilder;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest.TokenRequestBuilder;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.javlo.context.ContentContext;
import org.javlo.utils.JSONMap;

public class Google extends AbstractSocialNetwork {

	private static final String USER_INFO_SCOPE = "https://www.googleapis.com/auth/userinfo.email";

	@Override
	public String getName() {
		return "google";
	}

	@Override
	public String getAuthzEndpoint() {
		return OAuthProviderType.GOOGLE.getAuthzEndpoint();
	}

	@Override
	public String getTokenEndpoint() {
		return OAuthProviderType.GOOGLE.getTokenEndpoint();
	}

	@Override
	protected void configureAuthenticationRequest(AuthenticationRequestBuilder builder, String clientId, ContentContext ctx) throws Exception {
		super.configureAuthenticationRequest(builder, clientId, ctx);
		builder.setScope(USER_INFO_SCOPE);
	}

	@Override
	protected void configureTokenRequest(TokenRequestBuilder builder, String clientId, String clientSecret, String code) {
		super.configureTokenRequest(builder, clientId, clientSecret, code);
		builder.setScope(USER_INFO_SCOPE);
	}

	@Override
	protected OAuthClientRequest buildTokenRequest(org.apache.oltu.oauth2.client.request.OAuthClientRequest.TokenRequestBuilder builder) throws OAuthSystemException {
		return builder.buildBodyMessage();
	}

	@Override
	protected SocialUser getSocialUser(String accessToken, OAuthClient oAuthClient) throws Exception {
		OAuthClientRequest oAuthRequest = new OAuthBearerClientRequest("https://www.googleapis.com/oauth2/v2/userinfo").setAccessToken(accessToken).buildHeaderMessage();
		OAuthResourceResponse resourceResponse = oAuthClient.resource(oAuthRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
		Map<String, Object> userInformation = JSONMap.parseMap(resourceResponse.getBody());
		SocialUser user = new SocialUser();
		user.setEmail((String) userInformation.get("email")).setFirstName((String) userInformation.get("given_name")).setLastName((String) userInformation.get("family_name")).setAvatarURL((String) userInformation.get("picture"));
		return user;
	}

}