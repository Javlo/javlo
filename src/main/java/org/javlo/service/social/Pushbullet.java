package org.javlo.service.social;

public class Pushbullet extends AbstractSocialNetwork {

	@Override
	public String getName() {
		return "pushbullet";
	}

	@Override
	public String getAuthzEndpoint() {
		return "https://www.pushbullet.com/authorize";
	}

	@Override
	public String getTokenEndpoint() {
		return "https://www.pushbullet.com/authorize";
	}
}