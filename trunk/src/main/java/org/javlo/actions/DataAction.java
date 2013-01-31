package org.javlo.actions;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.service.NotificationService;
import org.javlo.service.RequestService;
import org.javlo.user.User;

public class DataAction implements IAction {

	@Override
	public String getActionGroupName() {
		return "data";
	}

	/**
	 * get the list of modification. option for request : markread=true, mark all notification returned as read. this method need user logger.
	 * 
	 * @return
	 */
	public static String performNotifications(RequestService rs, ContentContext ctx, NotificationService notif, User user) {
		if (user != null) {
			ctx.getAjaxData().put("notifications", notif.getNotifications(user.getLogin(), 999, StringHelper.isTrue(rs.getParameter("markread", null))));
		} else {
			return "no access";
		}
		return null;
	}

	public static String performToken(ContentContext ctx, User user) {
		if (user != null) {
			ctx.getAjaxData().put("token", user.getUserInfo().getToken());
		} else {
			return "no access";
		}
		return null;
	}
}
