package org.javlo.servlet;

import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;
import org.javlo.utils.StructuredProperties;

public class ContextServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {

		try {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			User user = ctx.getCurrentEditUser();
			if (user == null || !AdminUserSecurity.getInstance().haveRight(user, AdminUserSecurity.ADMIN_USER_ROLE)) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			} else {
				String context = StringHelper.getFileNameWithoutExtension(StringHelper.getFileNameFromPath(request.getRequestURI()));
				if (GlobalContext.isExist(request, context)) {
					GlobalContext globalContext = GlobalContext.getRealInstance(request.getSession(), context);
					StructuredProperties outProp = new StructuredProperties();
					for (Map.Entry<String, String> entry : globalContext.getConfig().entrySet()) {
						outProp.put(entry.getKey(), entry.getValue());
					}
					outProp.save(response.getOutputStream(), "context exported from javlo " + IVersion.VERSION + " - " + URLHelper.createStaticURL(ctx.getContextForAbsoluteURL(), "/"));
				} else {
					response.setStatus(HttpServletResponse.SC_FOUND);
				}
			}
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_FOUND);
			e.printStackTrace();
		}
	}
}
