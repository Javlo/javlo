package org.javlo.servlet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.config.StaticConfig;
import org.javlo.context.GlobalContext;
import org.javlo.servlet.zip.ZipManagement;

public class ZIPServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void destroy() {
		super.destroy();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	@Override
	public void init() throws ServletException {
		super.init();
	}

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			response.setContentType("application/gzip");
			StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
			GlobalContext globalContext = GlobalContext.getInstance(request);

			Set<String> excludes = new HashSet<String>();
			String[] excludeParams = request.getParameterValues("exclude");
			if (excludeParams != null) {
				excludes.addAll(Arrays.asList(excludeParams));
			}
			// TODO: Deprecated?
			String filter = request.getParameter("filter");
			if (filter != null) {
				if (filter.equalsIgnoreCase("xml")) {
					excludes.add(staticConfig.getBackupFolder());
					excludes.add(staticConfig.getStaticFolder());
				}
			}
			if (!staticConfig.isDownloadIncludeTracking()) {
				excludes.add("persitence/tracking");
			}

			ZipOutputStream out = new ZipOutputStream(response.getOutputStream());
			out.setLevel(9);

			ZipManagement.zipDirectory(out, null, globalContext.getDataFolder(), request, excludes, null);

			out.finish();
			out.flush();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
