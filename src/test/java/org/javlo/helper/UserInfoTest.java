package org.javlo.helper;

import org.javlo.user.UserInfo;

import junit.framework.TestCase;

public class UserInfoTest extends TestCase {
	
  public void testSetValue() throws Exception {
	  	UserInfo userInfo = new UserInfo();
	  	String login = "javlo";
	  	userInfo.setValue("login", login);
		assertEquals(userInfo.getLogin(), login);
		String role = "mailing";
		userInfo.setValue("rolesRaw", role);
		assertTrue(userInfo.getRoles().contains(role));
  }
	
}
