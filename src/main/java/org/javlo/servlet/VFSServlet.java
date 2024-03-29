package org.javlo.servlet;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.VFSHelper;

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

	private synchronized void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		InputStream in = null;
		OutputStream out = null;
		FileSystemManager fsManager = null;
		FileObject file = null;
		try {
			String pathInfo = request.getPathInfo().substring(1);

			String[] pathInfoTab = pathInfo.split(".zip");
			if (pathInfoTab.length == 2) {
				String zipFileName = pathInfoTab[0] + ".zip";
				pathInfo = pathInfoTab[1];

				ContentContext ctx = ContentContext.getContentContext(request, response);
				GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
				StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext());
				String dataFolder = globalContext.getDataFolder();
				File zipFile = new File(URLHelper.mergePath(dataFolder, zipFileName));
				long lastModified = zipFile.lastModified();
				long lastModifiedInBrowser = request.getDateHeader(NetHelper.HEADER_IF_MODIFIED_SINCE);
				if (lastModified > 0 && lastModified / 1000 <= lastModifiedInBrowser / 1000) {
					response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					return;
				}				
				if (!zipFile.exists()) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "file not found : " + zipFileName);
				} else {
					if (lastModified > 0) {
						response.setDateHeader(NetHelper.HEADER_LAST_MODIFIED, lastModified);
					}
					if (pathInfo.startsWith(staticConfig.getShareDataFolderKey())) {
						pathInfo = pathInfo.substring(staticConfig.getShareDataFolderKey().length() + 1);
						dataFolder = globalContext.getSharedDataFolder(request.getSession());
					}
					String resourceURI = pathInfo;
					resourceURI = resourceURI.replace('\\', '/');
					logger.info("read : " + resourceURI + " in : " + zipFileName);
					fsManager = VFS.getManager();
					file = fsManager.resolveFile(StringHelper.getFileExtension(zipFileName) + ":" + zipFile.getAbsolutePath());
					file = file.resolveFile('/' + resourceURI);
					in = file.getContent().getInputStream();
					out = response.getOutputStream();

					response.setContentType(ResourceHelper.getFileExtensionToMineType(StringHelper.getFileExtension(resourceURI)));
					ResourceHelper.writeStreamToStream(in, out);

				}
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "no or more than 1 '.zip' found in path : " + pathInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ResourceHelper.closeResource(in);
			ResourceHelper.closeResource(out);
			VFSHelper.closeFileSystem(file);
			//VFSHelper.closeManager(fsManager);
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
