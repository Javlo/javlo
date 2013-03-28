package org.javlo.user;

import java.security.Principal;

public class UserPrincipal extends User {

	private Principal principal = null;

	public UserPrincipal (Principal inPrincipal) {
		principal = inPrincipal;
	}

	@Override
	public String getLogin() {
		return principal.getName();
	}

	@Override
	public String getName() {
		return principal.getName();
	}

	public Principal getPrincipal() {
		return principal;
	}
}
