package org.javlo.helper;

import java.util.Collections;

import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;

public class SecurityHelper {

	public static boolean userAccessPage(ContentContext ctx, User user, MenuElement page) {
		if (AdminUserSecurity.getInstance().isAdmin(user)) {
			return true;
		} else if (user != null && !Collections.disjoint(page.getUserRoles(), user.getRoles())) {
			return true;
		} else {
			return false;
		}
	}

}
