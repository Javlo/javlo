package org.javlo.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.RequestHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.RobotsTxt;
import org.javlo.service.syncro.FileStructureFactory;
import org.javlo.tracking.Track;
import org.javlo.tracking.Tracker;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.utils.CSVFactory;
import org.javlo.utils.JSONMap;
import org.javlo.utils.XLSTools;
import org.javlo.ztatic.StaticInfo;
import org.javlo.ztatic.StaticInfo.StaticInfoBean;

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
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest,
	 *      HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		processRequest(httpServletRequest, httpServletResponse);
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest,
	 *      HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		processRequest(httpServletRequest, httpServletResponse);
	}

	/**
	 * get the text and the picture and build a button
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		if (request.getServletPath().equals("/favicon.ico") || request.getServletPath().equals("/robots.txt")) {
			response.setHeader("Cache-Control", "max-age=600,must-revalidate");
			GlobalContext globalContext = GlobalContext.getSessionContext(request.getSession());
			if (globalContext != null) {
				String filePath = URLHelper.mergePath(globalContext.getStaticConfig().getStaticFolder(), request.getServletPath());

				String finalName = URLHelper.mergePath(globalContext.getDataFolder(), filePath);
				InputStream fileStream = null;
				try {
					File file = new File(finalName);
					if (file.exists()) {
						fileStream = new FileInputStream(new File(finalName));
						if ((fileStream != null)) {
							ResourceHelper.writeStreamToStream(fileStream, response.getOutputStream());
						}
					} else {
						if (request.getServletPath().equals("/robots.txt")) {
							RobotsTxt.renderRobotTxt(ContentContext.getContentContext(request, response), response.getOutputStream());
						} else {
							response.setStatus(404, "not found : " + filePath);
						}
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new ServletException(e);
				} finally {
					ResourceHelper.closeResource(fileStream);
				}
			}
			return;
		}

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

		if (ctx.getGlobalContext().isCollaborativeMode() && ctx.getCurrentEditUser() == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
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
		 * Date toDay = new Date(); Calendar cal = Calendar.getInstance();
		 * cal.setTime(toDay); cal.roll(Calendar.DAY_OF_YEAR, true);
		 * response.setHeader("Expires", cal.getTime().toString());
		 */

		InputStream fileStream = null;
		try {
			String pathInfo;
			String dataFolder = globalContext.getDataFolder();

			pathInfo = request.getPathInfo().substring(1);
			if (request.getPathInfo() != null && request.getPathInfo().length() > 1) {
				String newPath = globalContext.getTransformShortURL(pathInfo);
				if (newPath != null) {
					pathInfo = URLHelper.cleanPath(newPath, false);
				}
			}

			if (pathInfo.startsWith(staticConfig.getShareDataFolderKey())) {
				pathInfo = pathInfo.substring(staticConfig.getShareDataFolderKey().length() + 1);
				dataFolder = globalContext.getSharedDataFolder(request.getSession());
			} else if (pathInfo.startsWith(URLHelper.TEMPLATE_RESOURCE_PREFIX)) {
				pathInfo = pathInfo.substring(URLHelper.TEMPLATE_RESOURCE_PREFIX.length() + 1);
				dataFolder = staticConfig.getTemplateFolder();
			}
			pathInfo = pathInfo.replace('\\', '/'); // for windows server

			String resourceURI = pathInfo;
			resourceURI = resourceURI.replace('\\', '/');

			logger.fine("load static resource : " + resourceURI);

			String fileExt = StringHelper.getFileExtension(resourceURI);
			boolean json = fileExt.equalsIgnoreCase("json");
			if (json) {
				resourceURI = resourceURI.substring(0, resourceURI.lastIndexOf("."));
			} else {
				response.setContentType(ResourceHelper.getFileExtensionToMineType(fileExt));
			}
			if (!pathInfo.equals(FILE_INFO)) {
				File file = new File(URLHelper.mergePath(dataFolder, resourceURI));
				StaticInfo info = StaticInfo.getInstance(ctx, file);
				if (AdminUserFactory.createUserFactory(ctx.getGlobalContext(), request.getSession()).getCurrentUser(request.getSession()) == null) {
					if (!info.canRead(ctx, UserFactory.createUserFactory(ctx.getGlobalContext(), request.getSession()).getCurrentUser(request.getSession()), request.getParameter(ImageTransformServlet.RESOURCE_TOKEN_KEY))) {
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						return;
					}
				}
				if (!json) {
					if (file.exists()) {
						StaticInfo.getInstance(ctx, file).addAccess(ctx);
					} else {
						if (StringHelper.isExcelFile(file.getName())) {
							File csvFile = new File(ResourceHelper.changeExtention(file.getAbsolutePath(), "csv"));
							if (csvFile.exists()) {
								csvFile = new File(URLHelper.mergePath(dataFolder, ResourceHelper.changeExtention(resourceURI, "csv")));
								response.setContentType(ResourceHelper.getFileExtensionToMineType(StringHelper.getFileExtension(file.getName())));
								response.setHeader("Cache-Control", "no-cache");
								response.setHeader("Accept-Ranges", "bytes");
								CSVFactory csvFactory = new CSVFactory(csvFile);
								if (StringHelper.getFileExtension(file.getName()).equals("xls")) {
									XLSTools.writeXLS(XLSTools.getCellArray(csvFactory.getArray()), response.getOutputStream());
								} else {
									XLSTools.writeXLSX(XLSTools.getCellArray(csvFactory.getArray()), response.getOutputStream());
								}
								return;
							}
						}
						response.setStatus(HttpServletResponse.SC_NOT_FOUND);
						return;
					}
				}
			}

			if (resourceURI != null) {
				if (pathInfo.equals(FILE_INFO)) {
					String fileTreeProperties = FileStructureFactory.getInstance(new File(dataFolder)).fileTreeToProperties();
					out = response.getOutputStream();
					out.write(fileTreeProperties.getBytes());
				} else {
					if (resourceURI.startsWith(ResourceHelper.PRIVATE_DIR)) {
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						return;
					}
					String finalName = URLHelper.mergePath(dataFolder, resourceURI);
					File file = new File(finalName);
					if (!json) {
						response.setContentType(ResourceHelper.getFileExtensionToMineType(fileExt));
						response.setHeader("Cache-Control", "no-cache");
						response.setDateHeader(NetHelper.HEADER_LAST_MODIFIED, file.lastModified());
						long lastModifiedInBrowser = request.getDateHeader(NetHelper.HEADER_IF_MODIFIED_SINCE);
						if (file.isFile()) {
							if (file.lastModified() > 0 && file.lastModified() / 1000 <= lastModifiedInBrowser / 1000) {
								response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
							} else {
								response.setHeader("Accept-Ranges", "bytes");
								response.setContentLength((int) file.length());
								fileStream = new FileInputStream(file);
								out = response.getOutputStream();
								ResourceHelper.writeStreamToStream(fileStream, out);
							}
						}
					} else {
						Map<String, Object> outMap = new HashMap<String, Object>();
						response.setContentType("application/json");
						response.setHeader("Cache-Control", "no-cache");
						if (request.getAttribute("lg") != null) {
							ctx.setAllLanguage((String) request.getAttribute("lg"));
						}
						StaticInfoBean bean = new StaticInfoBean(ctx, StaticInfo.getInstance(ctx, file));
						outMap.putAll(BeanUtils.describe(bean));
						outMap.remove("class");
						outMap.remove("accessToken");
						outMap.remove("staticInfo");
						outMap.remove("URL");
						outMap.remove("folder");
						JSONMap.JSON.toJson(outMap, response.getWriter());
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			ResourceHelper.closeResource(fileStream);
		}
		servletRun--;
	}

}
