package org.javlo.service.social;

import java.net.URL;
import java.util.Map;

public interface ISocialNetwork {

	public String getName();

	public String getLogin();

	public void setLogin(String login);

	public String getToken();

	public void setToken(String token);

	public URL getURL();

	public void setURL(String url);

	public Map<String, String> getData();

	public void set(String key, String value);

}
