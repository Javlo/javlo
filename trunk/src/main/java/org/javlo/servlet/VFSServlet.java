package org.javlo.servlet;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class VFSServlet extends HttpServlet {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(VFSServlet.class.getName());

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
		InputStream in = null;
		OutputStream out = null;
		try {
			String PARAM_NAME = "file";
			String pathInfo = request.getPathInfo().substring(1);

			String[] pathInfoTab = pathInfo.split(".zip");
			if (pathInfoTab.length == 2) {
				String zipFileName = pathInfoTab[0] + ".zip";
				pathInfo = pathInfoTab[1];
				if (zipFileName == null) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "no file defined param : " + PARAM_NAME);
				} else {
					ContentContext ctx = ContentContext.getContentContext(request, response);
					GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
					StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext());
					String dataFolder = globalContext.getDataFolder();
					File zipFile = new File(URLHelper.mergePath(dataFolder, zipFileName));
					if (!zipFile.exists()) {
						response.sendError(HttpServletResponse.SC_NOT_FOUND, "file not found : " + zipFileName);
					} else {
						if (pathInfo.startsWith(staticConfig.getShareDataFolderKey())) {
							pathInfo = pathInfo.substring(staticConfig.getShareDataFolderKey().length() + 1);
							dataFolder = staticConfig.getShareDataFolder();
						}
						String resourceURI = pathInfo;
						resourceURI = resourceURI.replace('\\', '/');
						logger.info("read : " + resourceURI + " in : " + zipFileName);
						FileSystemManager fsManager = VFS.getManager();
						FileObject file = fsManager.resolveFile(StringHelper.getFileExtension(zipFileName) + ":" + zipFile.getAbsolutePath());
						file = file.resolveFile('/' + resourceURI);
						in = file.getContent().getInputStream();
						out = response.getOutputStream();

						response.setContentType(ResourceHelper.getFileExtensionToManType(StringHelper.getFileExtension(resourceURI)));
						ResourceHelper.writeStreamToStream(in, out);
					}
				}
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "no or more than 1 '.zip' found in path : " + pathInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ResourceHelper.closeResource(in);
			ResourceHelper.closeResource(out);
		}
	}

	public static void main(String[] args) {
		try {
			FileSystemManager fsManager = VFS.getManager();
			FileObject file = fsManager.resolveFile("zip:/tmp/sexy.zip");
			FileObject[] files = file.getChildren();
			for (int i = 0; i < files.length; i++) {
				System.out.println("" + i + " - " + files[i].getName());
			}
		} catch (FileSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
