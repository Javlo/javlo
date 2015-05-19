package org.javlo.service.social;

import org.apache.oltu.oauth2.common.OAuthProviderType;


public class Twitter extends AbstractSocialNetwork {

	@Override
	public String getName() {
		return "twitter";
	}

	@Override
	public OAuthProviderType getProviderType() {
		return  null;
	}

}
