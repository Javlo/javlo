package org.javlo.servlet;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.config.StaticConfig;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.servlet.zip.ZipManagement;


public class FolderServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	public static final String TEMPLATE_PATH = "/template";
	public static final String MAILING_TEMPLATE_PATH = "/mailing-template";
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

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
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
				File securityFile = new File(URLHelper.mergePath(uri, ".nozip"));				
				if (!securityFile.exists()) {
					response.setContentType("application/gzip");
					ZipOutputStream out = new ZipOutputStream(response.getOutputStream());
					ZipManagement.zipDirectory(out, "", folder.getAbsolutePath(), request);
					out.finish();
					out.flush();
					out.close();
				} else {
					response.sendError(404);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}