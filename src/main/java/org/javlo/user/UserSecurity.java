package org.javlo.user;

import java.io.IOException;
import java.util.Collections;

import jakarta.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.ztatic.StaticInfo;

public class UserSecurity {
	
	public static String SHADOW_USER = "shadowUser";

	private UserSecurity() {
	}

	public static boolean isCurrentUserCanRead(ContentContext ctx, StaticInfo info) {
		if (info.getReadRoles(ctx) != null && info.getReadRoles(ctx).size() > 0) {
			if (ctx.getCurrentEditUser() == null) {
				if (ctx.getCurrentUser() == null) {
					return false;
				} else {
					if (Collections.disjoint(info.getReadRoles(ctx), ctx.getCurrentUser().getRoles())) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public static void storeShadowUser(HttpSession session) throws Exception {
		session.setAttribute(SHADOW_USER, session.getAttribute(UserFactory.createUserFactory(GlobalContext.getSessionInstance(session), session).getSessionKey()));
	}
	
	public static User getShadowUser(HttpSession session) {
		return (User)session.getAttribute(SHADOW_USER);
	}
	
	public static void clearShadowUser(HttpSession session) {
		session.removeAttribute(SHADOW_USER);
	}

}
