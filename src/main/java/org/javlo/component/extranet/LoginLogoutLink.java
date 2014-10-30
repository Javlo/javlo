package org.javlo.component.extranet;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;

public class LoginLogoutLink extends AbstractVisualComponent {

	public static final String TYPE = "login-logout";

	public LoginLogoutLink() {
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		IUserFactory userFactory = UserFactory.createUserFactory(ctx.getRequest());
		User user = userFactory.getCurrentUser(ctx.getRequest().getSession());
		if (user == null) {
			out.println("<form id=\"loginform\" action=\""+URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE))+"\" method=\"post\">");
			out.println("<div class=\"line\">");
			out.println("<input type=\"hidden\" value=\"adminlogin\" name=\"login-type\" />");
			out.println("<input type=\"hidden\" value=\"edit-login\" name=\"edit-login\" />");
			out.println("<c:if test=\"${not empty param.previewEdit}\">");
			out.println("<input type=\"hidden\" name=\"previewEdit\" value=\"true\" />");
			out.println("</c:if>");
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			out.println("<input class=\"username\" id=\"j_username\" type=\"text\" name=\"j_username\" placeholder=\""+i18nAccess.getAllText("form.login", "login")+"\" />");
			out.println("<input class=\"password\" id=\"j_password\" type=\"password\" name=\"j_password\"  placeholder=\""+i18nAccess.getAllText("form.password", "login")+"\" />");
			out.println("<button name=\"submit\" class=\"submit\" >Login</button>");
			out.println("</div>");
			out.println("</form>");
		} else {
			out.println("<a href=\"" + URLHelper.createURL(ctx) + "?edit-logout=logout\">logout ("+userFactory.getCurrentUser(ctx.getRequest().getSession()).getLogin()+")</a>");
		}
		out.close();
		return new String(outStream.toByteArray());
	}

}
