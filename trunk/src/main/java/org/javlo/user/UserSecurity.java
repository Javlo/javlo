package org.javlo.user;

import java.util.Collections;

import org.javlo.context.ContentContext;
import org.javlo.ztatic.StaticInfo;

public class UserSecurity {

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

}
