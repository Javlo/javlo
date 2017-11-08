package org.javlo.component.web2;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;

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

	private boolean isFollow(ContentContext ctx) throws Exception {
		String currentUser = ctx.getCurrentUserId();
		if (currentUser != null) {
			return ctx.getCurrentPage().getFollowers(ctx).contains(currentUser);
		} else {
			return false;
		}
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		if (ctx.getRenderMode() == ContentContext.PAGE_MODE) {
			return "";
		}

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
			out.println("<button type=\"submit\" class=\"btn-block btn btn-default " + (isFollow(ctx) ? "btn-success" : "btn-secondary") + " \">" + label + "</button>");
			out.println("</form>");

			List<String> followers = ctx.getCurrentPage().getFollowers(ctx);
			if (followers.size() > 0) {
				String folowersId = "followers-" + getId();
				out.println("<button class=\"btn btn-primary btn-followers\" type=\"button\" data-toggle=\"collapse\" data-target=\"#" + folowersId + "\" aria-expanded=\"false\" aria-controls=\"collapseExample\">");
				out.println(i18nAccess.getText("content.follow.list", "followers list"));
				out.println("</button>");

				out.println("<div class=\"collapse\" id=\"" + folowersId + "\"><div class=\"card card-body\">");

				UserFactory fact = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());

				out.println("<ul class=\"list-group list-group-flush\">");
				for (String follower : followers) {
					User user = fact.getUser(follower);
					if (user != null) {
						out.println("<li class=\"list-group-item\">");
						if (StringHelper.isMail(user.getUserInfo().getEmail())) {
							out.println("<a href=\"mailto:" + user.getUserInfo().getEmail() + "\">");
						}
						out.println(user.getLabel());
						if (StringHelper.isMail(user.getUserInfo().getEmail())) {
							out.println("</a>");
						}
						out.println("</li>");
					}
				}
			}

			out.println("</div></div>");
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
				} else {
					return "security error !";
				}
			} else {
				if (ctx.getCurrentPage().isReadAccess(ctx, currentUser)) {
					ctx.getCurrentPage().removeFollowers(ctx, currentUser.getLogin());
				} else {
					return "security error !";
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
