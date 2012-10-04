package org.javlo.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.XMLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;


public class SiteMapServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
		super.init();
	}

	public void destroy() {
		super.destroy();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			
			String host = request.getServerName();
			if (!GlobalContext.isExist(request, host) && request.getParameter("yes") == null) {
				getServletContext().getRequestDispatcher("/jsp/error/creation.jsp").include(request, response);
				return;
			}		

			response.setContentType("text/xml");
			ContentContext ctx = ContentContext.getContentContext(request, response);
			ContentService content = ContentService.getInstance(request);
			MenuElement root = content.getNavigation(ctx);
			String siteMap = XMLHelper.getSiteMap(ctx, root);
			response.getOutputStream().write(siteMap.getBytes());
			response.getOutputStream().flush();
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
