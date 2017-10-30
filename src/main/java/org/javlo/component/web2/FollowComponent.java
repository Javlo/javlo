package org.javlo.component.web2;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;
import org.javlo.user.User;

public class FollowComponent extends AbstractVisualComponent implements IAction {

	public static final String TYPE = "follow";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		ctx.getRequest().setAttribute("followPage", isFollow(ctx));

	}

	private boolean isFollow(ContentContext ctx) {
		String currentUser = ctx.getCurrentUserId();
		if (currentUser != null) {
			return getPage().getFollowers(ctx).contains(currentUser);
		} else {
			return false;
		}
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (ctx.getCurrentEditUser() == null) {
			return "";
		} else {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

			String label = i18nAccess.getViewText("global.follow");
			if (isFollow(ctx)) {
				label = i18nAccess.getViewText("global.not-follow");
			}

			out.println("<form action=\"" + URLHelper.createURL(ctx) + "\" method=\"post\">");
			out.println("<div><input type=\"hidden\" name=\"webaction\" value=\"" + getActionGroupName() + ".follow\" />");
			out.println("<input type=\"hidden\" name=\"follow\" value=\"" + !isFollow(ctx) + "\" /></div>");
			out.println("<button type=\"submit\" class=\"btn-block btn btn-default "+(isFollow(ctx)?"btn-success":"btn-secondary")+" \">" + label + "</button>");
			out.println("</form>");
			out.close();
			return new String(outStream.toByteArray());
		}

	}

	public static String performFollow(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		User currentUser = ctx.getCurrentUser();
		if (currentUser != null) {
			if (StringHelper.isTrue(rs.getParameter("follow"))) {
				if (ctx.getCurrentPage().isReadAccess(ctx, currentUser)) {
					ctx.getCurrentPage().addFollowers(ctx, currentUser.getLogin());
				}
			} else {
				if (ctx.getCurrentPage().isReadAccess(ctx, currentUser)) {
					ctx.getCurrentPage().removeFollowers(ctx, currentUser.getLogin());
				}
			}
		}		
		return null;
	}

	@Override
	public String getActionGroupName() {
		return getType();
	}

}
