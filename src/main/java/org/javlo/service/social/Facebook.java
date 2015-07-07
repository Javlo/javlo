package org.javlo.service.social;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
import org.javlo.helper.NetHelper;
import org.javlo.service.social.AbstractSocialNetwork.SocialUser;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserInfo;
import org.javlo.utils.JSONMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Facebook extends AbstractSocialNetwork {
	
	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Facebook.class.getName());
	
	public static final Gson JSON = new GsonBuilder().setDateFormat("yyyy-MM-dd_HH-mm-ss-SSS ").create();

	@Override
	public String getName() {
		return "facebook";
	}
	
	@Override
	public String getSigninURL(ContentContext ctx) throws Exception {
		if (getClientId().length() == 0 || getClientSecret().length() == 0) {
			return null;
		}
		
		OAuthClientRequest request = OAuthClientRequest
				   .authorizationLocation("https://graph.facebook.com/oauth/authorize")
				   .setClientId(getClientId())
				   .setRedirectURI(getRedirectURL())
				   .buildQueryMessage();		
		
		return request.getLocationUri();
	}

	public String getClientId() {
		return data.get("client_id");
	}

	public String getClientSecret() {
		return data.get("client_secret");
	}

	public void setClientId(String id) {
		data.put("client_id", id);
	}

	public void setClientSecret(String secret) {
		data.put("client_secret", secret);
	}

	@Override
	public void update(Map map) {
		super.update(map);
		setClientSecret("" + map.get("client_secret"));
		setClientId("" + map.get("client_id"));
	}
	
	public IUserInfo getInitialUserInfo(String accessToken) throws Exception {
		URL url;
		url = new URL("https://api-read.facebook.com/restserver.php?access_token="+accessToken+"&api_key="+getClientId()+"&format=json-strings&method=fql.query&pretty=0&query=SELECT%20name%2C%20first_name%2C%20last_name%2C%20uid%2C%20email%20FROM%20user%20WHERE%20uid%3D693608149&sdk=joey");
		
		logger.info("read info from DB : "+url);
		
		String jsonStr = NetHelper.readPage(url);
		
		TypeToken<ArrayList<Map<String,String>>> list = new TypeToken<ArrayList<Map<String,String>>>(){};
		List<Map<String,String>> info = (List<Map<String,String>>)JSONMap.JSON.fromJson(jsonStr,list.getType());

		IUserInfo ui = new UserInfo();
		ui.setLogin(""+info.get(0).get("email"));
		ui.setEmail(""+info.get(0).get("email"));
		ui.setFirstName(""+info.get(0).get("first_name"));
		ui.setLastName(""+info.get(0).get("last_name"));
		return ui;
	}
	
	@Override
	public OAuthProviderType getProviderType() {	
		return OAuthProviderType.FACEBOOK;
	}
	
	protected String getAccessToken(String code, OAuthClient oAuthClient) throws OAuthSystemException, OAuthProblemException {
		OAuthClientRequest request = OAuthClientRequest
				   .authorizationLocation("https://graph.facebook.com/oauth/authorize")
				   .setClientId("your-facebook-application-client-id")
				   .setRedirectURI(getRedirectURL())
				   .buildQueryMessage();

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
			OAuthClientRequest oAuthRequest = new OAuthBearerClientRequest("https://graph.facebook.com/pvandermaesen").setAccessToken(accessToken).buildHeaderMessage();
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

