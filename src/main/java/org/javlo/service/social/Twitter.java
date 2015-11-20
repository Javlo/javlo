package org.javlo.service.social;

public class Twitter extends AbstractSocialNetwork {

	@Override
	public String getName() {
		return "twitter";
	}


	@Override
	public String getAuthzEndpoint() {
		return null;
	}

	@Override
	public String getTokenEndpoint() {
		return null;
	}

}
