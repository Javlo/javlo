package org.javlo.service.social;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractSocialNetwork implements ISocialNetwork {

	private final Map<String, String> data = new HashMap<String, String>();

	private static final String TOKEN = "token";
	private static final String URL = "url";
	private static final String LOGIN = "login";

	@Override
	public String getToken() {
		return data.get(TOKEN);
	}

	@Override
	public String getLogin() {
		return data.get(LOGIN);
	}

	@Override
	public String getURL() {
		return data.get("url");
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

}
