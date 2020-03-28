package org.javlo.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.servlet.zip.ZipManagement;
import org.javlo.user.AdminUserSecurity;

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

			ContentContext ctx = ContentContext.getContentContext(request, response);
			if (ctx.getCurrentEditUser() == null) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
			
			boolean right = AdminUserSecurity.getInstance().canRole(ctx.getCurrentEditUser() , AdminUserSecurity.CONTENT_ROLE);
			right = right && !AdminUserSecurity.getInstance().haveRole(ctx.getCurrentEditUser() , AdminUserSecurity.LIGHT_INTERFACE_ROLE);
			right = right && !AdminUserSecurity.getInstance().haveRole(ctx.getCurrentEditUser() , AdminUserSecurity.CONTRIBUTOR_ROLE);
			
			if (!right) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

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
				excludes.add("persistence/tracking");
			}

			ZipManagement.zipDirectory(response.getOutputStream(), globalContext.getDataFolder(), request, excludes, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		Set<String> excludes = new HashSet<String>();
		excludes.add("persitence/tracking");
		ZipManagement.zipDirectory(new ByteArrayOutputStream(), "C:/Users/user/data/javlo/data-ctx/data-4contes", null, excludes, null);
	}
}
