package org.javlo.servlet;

import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.UserInterfaceContext;
import org.javlo.data.InfoBean;
import org.javlo.helper.ServletHelper;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.tracking.Tracker;

public class ModuleServlet extends HttpServlet {

	private static Logger logger = Logger.getLogger(ModuleServlet.class.getName());

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {

			ContentContext ctx = ContentContext.getContentContext(request, response);

			Tracker.trace(request, response);

			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

			/*** update module status before action ***/
			ModulesContext moduleContext = ModulesContext.getInstance(request.getSession(), globalContext);
			moduleContext.initContext(request, response);

			InfoBean.updateInfoBean(ctx);

			String action = ServletHelper.execAction(ctx);
			if (action != null) {
				logger.info("exec action : " + action);
			}

			ServletHelper.prepareModule(ctx);
			String moduleName = request.getParameter("module");
			String path = request.getPathInfo();
			if (path != null || moduleName != null) {
				if (moduleName == null) {
					path = path.trim();
					if (path.startsWith("/")) {
						path = path.substring(1);
					}
					moduleName = path;
				}
				if (path != null && path.contains("/")) {
					moduleName = path.substring(0, path.indexOf("/"));
					ctx.setPath(path.substring(path.indexOf("/")));
				} else {
					ctx.setPath("/");
				}
				if (moduleName != null && moduleName.trim().length() > 0) {
					ModulesContext modulesContext = ModulesContext.getInstance(request.getSession(), globalContext);
					modulesContext.setCurrentModule(moduleName);
					if (modulesContext.isCurrentModule()) {
						Module module = modulesContext.getCurrentModule();
						if (module.getViewRenderer() != null) {
							RequestDispatcher rd = request.getRequestDispatcher(module.getViewRenderer());
							rd.include(request, response);
							UserInterfaceContext uic = UserInterfaceContext.getInstance(request.getSession(), globalContext);
							uic.setCurrentModule(module.getName());
							return;
						} else {
							throw new ServletException("no view renderer for module : " + module.getName());
						}
					}
				}
			}

			throw new ServletException("module not found : " + moduleName);

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
