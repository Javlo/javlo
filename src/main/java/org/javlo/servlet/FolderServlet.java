package org.javlo.servlet;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.servlet.zip.ZipManagement;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;


public class FolderServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	public static final String TEMPLATE_PATH = "/template";
	public static final String MAILING_TEMPLATE_PATH = "/mailing-template";

	/** marker a folder can contain to forbid its zip export */
	public static final String NO_ZIP_FILE = ".nozip";
	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(FolderServlet.class.getName());
	
	private final List<String> DMZ_FOLDER = Arrays.asList(new String[] {"/template", "/mailing-template"});

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

	/**
	 * the zip export streams the whole template folder : the jsp renderers, the
	 * config.properties and everything else that folder holds. It is the server
	 * side source of the site, so it is reserved to a logged edit user holding
	 * the content or the design role, the same roles as the template module.
	 */
	private static boolean canDownloadFolder(ContentContext ctx) {
		User editUser = ctx.getCurrentEditUser();
		if (editUser == null) {
			return false;
		}
		AdminUserSecurity userSecurity = AdminUserSecurity.getInstance();
		return userSecurity.canRole(editUser, AdminUserSecurity.CONTENT_ROLE) || userSecurity.canRole(editUser, AdminUserSecurity.DESIGN_ROLE);
	}

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			/* no page is involved here, only the logged user matters */
			ContentContext ctx = ContentContext.getContentContextNoPageManagement(request, response);
			if (!canDownloadFolder(ctx)) {
				logger.warning("unauthorized access for zip : " + request.getRequestURI());
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			String uri = request.getPathInfo();

			String ext = StringHelper.getFileExtension(uri);
			if (ext.trim().length() > 0) {
				uri = uri.substring(0, uri.length() - (ext.length()+1) );
			}

			StaticConfig staticConfig = StaticConfig.getInstance(getServletContext());
			boolean folderDMZAccess = false;
			File folder;

			if (uri.startsWith(TEMPLATE_PATH)) {
				folder = new File(staticConfig.getTemplateFolder() + uri.substring(TEMPLATE_PATH.length()));
			} else if (uri.startsWith(MAILING_TEMPLATE_PATH)) {
				folder = new File(staticConfig.getMailingTemplateFolder() + uri.substring(MAILING_TEMPLATE_PATH.length()));
			} else if (staticConfig.isDataFolderRelative()) {
				folder = new File(ResourceHelper.getRealPath(getServletContext(),uri));
			} else {
				folder = new File(uri);
			}
			for (String folderDMZ : DMZ_FOLDER) {
				if (uri.startsWith(folderDMZ)) {
					folderDMZAccess = true;
				}
			}
			if (!folderDMZAccess) {
				logger.warning("unautorized access for zip : "+uri);
			}
			if (!folderDMZAccess || !folder.exists() || !folder.isDirectory()) {
				response.sendError(404);
			} else {
				/*
				 * the marker has to be looked up in the folder on disk : it used
				 * to be resolved against the request path, so it pointed at the
				 * root of the filesystem and the opt-out never triggered.
				 */
				File securityFile = new File(folder, NO_ZIP_FILE);
				if (!securityFile.exists()) {
					response.setContentType("application/gzip");
					ZipManagement.zipDirectory(response.getOutputStream(), folder.getAbsolutePath(), request);
				} else {
					response.sendError(404);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}