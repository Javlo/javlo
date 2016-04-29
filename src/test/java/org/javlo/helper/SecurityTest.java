package org.javlo.helper;

import java.util.HashSet;
import java.util.Set;

import org.javlo.user.AdminUserInfo;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;

import junit.framework.TestCase;

public class SecurityTest extends TestCase {
	
	public void testHaveRole() throws Exception {		
		AdminUserSecurity userSecurity = AdminUserSecurity.getInstance();
		AdminUserInfo userInfo = new AdminUserInfo();		
		userInfo.setLogin("test");
		Set<String> roles = new HashSet<String>();
		roles.add(AdminUserSecurity.CONTENT_ROLE);
		userInfo.setRoles(roles);
		User user = new User();
		user.setUserInfo(userInfo);
		assertTrue(userSecurity.haveRole(user, AdminUserSecurity.CONTENT_ROLE));				
	}

}
