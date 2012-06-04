package org.javlo.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.RequestHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.syncro.FileStructureFactory;
import org.javlo.tracking.Track;
import org.javlo.tracking.Tracker;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.ztatic.StaticInfo;

/**
 * @author pvandermaesen
 * 
 * 
 */
public class ResourceServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String FILE_INFO = "file_structure.properties";

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ResourceServlet.class.getName());

	static int servletRun = 0;

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		processRequest(httpServletRequest, httpServletResponse);
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		processRequest(httpServletRequest, httpServletResponse);
	}

	/**
	 * get the text and the picture and build a button
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		
		servletRun++;

		OutputStream out = null;

		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());

		ContentContext ctx;
		try {
			ctx = ContentContext.getContentContext(request, response);
			RequestHelper.traceMailingFeedBack(ctx);
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new ServletException(e1.getMessage());
		}

		/* TRACKING */
		GlobalContext globalContext = GlobalContext.getInstance(request);
		IUserFactory fact = UserFactory.createUserFactory(globalContext, request.getSession());
		User user = fact.getCurrentUser(ctx.getRequest().getSession());
		String userName = null;
		if (user != null) {
			userName = user.getLogin();
		}
		try {
			Tracker tracker = Tracker.getTracker(globalContext, request.getSession());
			Track track = new Track(userName, "view picture", request.getRequestURI(), System.currentTimeMillis(), request.getHeader("referer"), request.getHeader("User-Agent"));
			track.setIP(request.getRemoteAddr());
			track.setSessionId(request.getSession().getId());
			tracker.addTrack(track);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		/* END TRACKING */

		// TODO: check if that work for caching
		/*
		 * Date toDay = new Date(); Calendar cal = Calendar.getInstance(); cal.setTime(toDay); cal.roll(Calendar.DAY_OF_YEAR, true); response.setHeader("Expires", cal.getTime().toString());
		 */

		InputStream fileStream = null;
		try {
			String pathInfo;
			String dataFolder = globalContext.getDataFolder();
			if (request.getServletPath().equals("/favicon.ico")) {
				pathInfo = URLHelper.mergePath(staticConfig.getStaticFolder(), request.getServletPath());
			} else {
				pathInfo = request.getPathInfo().substring(1);
				if (pathInfo.startsWith(staticConfig.getShareDataFolderKey())) {
					pathInfo = pathInfo.substring(staticConfig.getShareDataFolderKey().length() + 1);
					dataFolder = staticConfig.getShareDataFolder();
				}
				pathInfo = pathInfo.replace('\\', '/'); // for windows server
			}

			String ressourceURI = pathInfo;
			ressourceURI = ressourceURI.replace('\\', '/');

			logger.fine("load static ressource : " + ressourceURI);			

			response.setContentType(ResourceHelper.getFileExtensionToManType(StringHelper.getFileExtension(ressourceURI)));
			if (!pathInfo.equals(FILE_INFO)) {
				File file = new File(URLHelper.mergePath(dataFolder, ressourceURI));
				if (file.exists()) {
					response.setContentLength((int) file.length());
					StaticInfo.getInstance(ctx, file).addAccess(ctx);
				} else {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
			}
			out = response.getOutputStream();

			if (ressourceURI != null) {
				if (pathInfo.equals(FILE_INFO)) {
					String fileTreeProperties = FileStructureFactory.getInstance(new File(dataFolder)).fileTreeToProperties();
					out.write(fileTreeProperties.getBytes());
				} else {

					String finalName = URLHelper.mergePath(dataFolder, ressourceURI);

					fileStream = new FileInputStream(new File(finalName));

					if ((fileStream != null)) {
						ResourceHelper.writeStreamToStream(fileStream, out);
					}
				}

			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			ResourceHelper.closeResource(fileStream);
			try {
				if (out != null) {
					out.flush();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		servletRun--;
	}

}
