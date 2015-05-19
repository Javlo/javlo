package org.javlo.service.social;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.client.HttpClient;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.utils.JSONMap;


public class Google extends AbstractSocialNetwork {

	@Override
	public String getName() {
		return "google";
	}
	
	public OAuthProviderType getProviderType() { 
		return OAuthProviderType.GOOGLE;
	}
	
	@Override
	public String getSigninURL(ContentContext ctx) throws Exception {
		if (getClientId().length() == 0 || getClientSecret().length() == 0) {
			return null;
		}
		OAuthClientRequest request = OAuthClientRequest
				   .authorizationProvider(getProviderType())
				   .setClientId(getClientId())
				   .setResponseType(OAuth.OAUTH_CODE)
				   .setState(getState(ctx))
				   .setScope("https://www.googleapis.com/auth/userinfo.email")
				   .setRedirectURI(getRedirectURL())
				   .buildQueryMessage();
		
		return request.getLocationUri();
	}
	
	protected String getAccessToken(String code, OAuthClient oAuthClient) throws OAuthSystemException, OAuthProblemException {
		OAuthClientRequest request = OAuthClientRequest.tokenProvider(OAuthProviderType.GOOGLE)
				.setGrantType(GrantType.AUTHORIZATION_CODE)
				.setClientId(getClientId())
				.setClientSecret(getClientSecret())
				.setRedirectURI(getRedirectURL())
				.setScope("https://www.googleapis.com/auth/userinfo.email")
				.setCode(code)
				.buildBodyMessage();

		OAuthJSONAccessTokenResponse response = oAuthClient.accessToken(request, OAuthJSONAccessTokenResponse.class);

		String accessToken = response.getAccessToken();
		Long expiresIn = response.getExpiresIn();

		System.out.println("accessToken = " + accessToken);
		System.out.println("expiresIn = " + expiresIn);

		return accessToken;
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
			OAuthClientRequest oAuthRequest = new OAuthBearerClientRequest("https://www.googleapis.com/oauth2/v2/userinfo").setAccessToken(accessToken).buildHeaderMessage();
			OAuthResourceResponse resourceResponse = oAuthClient.resource(oAuthRequest,OAuth.HttpMethod.GET, OAuthResourceResponse.class);
			Map<String, Object> userInformation = JSONMap.parseMap(resourceResponse.getBody());
			SocialUser user = new SocialUser();
			user.setEmail((String)userInformation.get("email")).setFirstName((String)userInformation.get("given_name")).setLastName((String)userInformation.get("family_name")).setAvatarURL((String)userInformation.get("picture"));
			ContentContext ctx = ContentContext.getContentContext(request, response);
			login(ctx, user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}