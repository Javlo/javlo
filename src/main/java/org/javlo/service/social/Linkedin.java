package org.javlo.service.social;

import org.apache.oltu.oauth2.common.OAuthProviderType;

public class Linkedin extends AbstractSocialNetwork {

	@Override
	public String getName() {
		return "linkedin";
	}

	@Override
	public String getAuthzEndpoint() {
		return OAuthProviderType.LINKEDIN.getAuthzEndpoint();
	}

	@Override
	public String getTokenEndpoint() {
		return OAuthProviderType.LINKEDIN.getTokenEndpoint();
	}
}