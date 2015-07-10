package org.javlo.service.social;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.javlo.context.ContentContext;

public interface ISocialNetwork {

	public String getName();

	public String getLogin();

	public void setLogin(String login);

	public String getToken();

	public void setToken(String token);

	public String getURL();

	public void setURL(String url);

	public Map<String, String> getData();

	public void set(String key, String value);
	
	public String getClientSecret();
	
	public String getClientId();

	/**
	 * update value with a map.
	 */
	public void update(Map map);
	
	/**
	 * prepare social network for rendering
	 * @param ctx
	 * @throws OAuthSystemException 
	 * @throws Exception 
	 */
	public void prepare(ContentContext ctx) throws  Exception;
	
	public String getSigninURL(ContentContext ctx) throws Exception;
	
	public void setRedirectURL(String url);
	
	public void performRedirect(HttpServletRequest request, HttpServletResponse response);

}
