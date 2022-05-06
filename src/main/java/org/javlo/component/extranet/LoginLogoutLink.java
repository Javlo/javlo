package org.javlo.component.extranet;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;

public class LoginLogoutLink extends AbstractVisualComponent {

	private static final String VISITOR = "visitor";

	private static final String[] styles = new String[] { "admin", VISITOR };

	public static final String TYPE = "login-logout";

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		return getStyleList(ctx);
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return styles;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		IUserFactory userFactory;
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String pwtoken = requestService.getParameter("pwtoken");
		
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		if (pwtoken == null) {
			if (getStyle().equals(VISITOR)) {
				userFactory = UserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
			} else {
				userFactory = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
			}
			User user = userFactory.getCurrentUser(ctx.getGlobalContext(), ctx.getRequest().getSession());
			if (user == null) {
				if (getStyle().equals(VISITOR)) {
					out.println("<form class=\"hidden-print\" id=\"loginform\" action=\"" + URLHelper.createURL(ctx) + "\" method=\"post\">");
				} else {
					out.println("<form class=\"hidden-print\" id=\"loginform\" action=\"" + URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE)) + "\" method=\"post\">");
				}
				if (!getStyle().equals(VISITOR)) {
					out.println("<input type=\"hidden\" value=\"adminlogin\" name=\"login-type\" />");
					out.println("<input type=\"hidden\" value=\"edit-login\" name=\"edit-login\" />");
					out.println("<input type=\"hidden\" name=\"" + ContentContext.PREVIEW_EDIT_PARAM + "\" value=\"true\" />");
				}
				out.println("<div class=\"form-group row\"><div class=\"col-sm-5\">");
				out.println("<input class=\"username form-control\" id=\"j_username\" type=\"text\" name=\"j_username\" placeholder=\"" + i18nAccess.getAllText("form.email", "e-mail") + "\" />");
				out.println("</div><div class=\"col-sm-5\">");
				out.println("<input class=\"password form-control\" id=\"j_password\" type=\"password\" name=\"j_password\"  placeholder=\"" + i18nAccess.getAllText("form.password", "login") + "\" />");
				out.println("</div><div class=\"col-sm-2\"><button type=\"submit\" class=\"submit btn btn-primary btn-xs\" >" + i18nAccess.getViewText("global.ok") + "</button></div>");
				out.println("</div><div class=\"checkbox\"><label>");
				out.println(" <div class=\"form-group\"><input type=\"checkbox\" name=\"autologin\" /> " + i18nAccess.getAllText("login.auto", "Remember me on this computer") + "</div>");
				out.println("</label>");
				String js = " onmouseenter=\"$('#j_password').addClass('disabled'); $('#j_username').addClass('focus');\"  onmouseleave=\"$('#j_password').removeClass('disabled'); $('#j_username').removeClass('focus');\"";
				out.println("&nbsp;<button type=\"submit\" name=\"webaction\" value=\"user.askChangePassword\" class=\"submit btn btn-secondary btn-xs\" " + js + ">" + i18nAccess.getViewText("login.change-password") + "</button>");
				out.println("</div>");
				out.println("</form>");
			} else {
				out.println("<a class=\"btn btn-default btn-logout btn-secondary hidden-print\" href=\"" + URLHelper.createURL(ctx) + "?edit-logout=logout\">logout (" + userFactory.getCurrentUser(ctx.getGlobalContext(), ctx.getRequest().getSession()).getLogin() + ")</a>");
			}
		} else {
			out.println("<p>"+i18nAccess.getViewText("login.change-password")+"</p>");
			out.println("<form class=\"hidden-print\" id=\"loginform\" action=\"" + URLHelper.createURL(ctx) + "\" method=\"post\">");
			out.println("<div><input type=\"hidden\" value=\"user.changePasswordWithToken\" name=\"webaction\" /><input type=\"hidden\" value=\"" + pwtoken + "\" name=\"token\" /></div>");
			out.println("<div class=\"form-group row\"><div class=\"col-sm-6\">");
			out.println("<input class=\"username form-control\" id=\"j_username\" type=\"password\" name=\"password\" placeholder=\"" + i18nAccess.getAllText("form.password", "password") + "\" />");
			out.println("</div><div class=\"col-sm-6\">");
			out.println("<input class=\"username form-control\" id=\"j_username\" type=\"password\" name=\"password2\" placeholder=\"" + i18nAccess.getAllText("form.password2", "password bis") + "\" />");
			out.println("</div></div>");
			out.println("<button type=\"submit\" class=\"submit btn btn-primary btn-xs\" >" + i18nAccess.getViewText("global.ok") + "</button></form>");
		}
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_ADMIN);
	}

}
