package org.javlo.service.social;

import java.util.Map;

public class Facebook extends AbstractSocialNetwork {

	@Override
	public String getName() {
		return "facebook";
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

}