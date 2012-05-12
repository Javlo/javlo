/*
 * Created on Aug 13, 2003
 */
package org.javlo.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.javlo.admin.AdminContext;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentContext;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentComponentsList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.IPreviewable;
import org.javlo.component.files.AbstractFileComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.ContentManager;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.StatContext;
import org.javlo.filter.NotDirectoryFilter;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.DebugHelper;
import org.javlo.helper.JavaHelper;
import org.javlo.helper.Logger;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.NavigationHelper;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.IMacro;
import org.javlo.macro.MacroFactory;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.IURLFactory;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ClipBoard;
import org.javlo.service.ContentService;
import org.javlo.service.NavigationService;
import org.javlo.service.PersistenceService;
import org.javlo.service.PublishListener;
import org.javlo.service.RequestService;
import org.javlo.service.ReverseLinkService;
import org.javlo.service.exception.ServiceException;
import org.javlo.service.resource.Resource;
import org.javlo.service.syncro.SynchroThread;
import org.javlo.servlet.zip.ZipManagement;
import org.javlo.template.Template;
import org.javlo.template.TemplateSearchContext;
import org.javlo.thread.AbstractThread;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserEditFilter;
import org.javlo.user.UserFactory;
import org.javlo.user.UserInfos;
import org.javlo.user.exception.UserAllreadyExistException;
import org.javlo.utils.CSVFactory;
import org.javlo.ztatic.FileCache;
import org.javlo.ztatic.IStaticContainer;
import org.javlo.ztatic.StaticContext;
import org.javlo.ztatic.StaticInfo;

import be.noctis.common.xml.NodeXML;
import be.noctis.common.xml.XMLFactory;

/**
 * @author pvandermaesen list of actions for cms.
 */
public class EditActions {

	/** STATIC MANAGEMENT */

	private static class LocaleStaticInfo {
		private String title;
		private String description;
		private String location;
		private String pageId;
		private Date date;
		private boolean shared;

		public Date getDate() {
			return date;
		}

		public String getDescription() {
			return description;
		}

		public String getLocation() {
			return location;
		}

		public String getPageId() {
			return pageId;
		}

		public String getTitle() {
			return title;
		}

		public boolean isShared() {
			return shared;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public void setLocation(String location) {
			this.location = location;
		}

		public void setPageId(String pageId) {
			this.pageId = pageId;
		}

		public void setShared(boolean shared) {
			this.shared = shared;
		}

		public void setTitle(String title) {
			this.title = title;
		}
	}

	/**
	 * create a static logger.
	 */
	protected static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(EditActions.class.getName());

	private static void autoPublish(HttpServletRequest request, HttpServletResponse response) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		if (globalContext.isEasy()) {
			performPublish(request, response);
		}
	}

	private static boolean canModifyCurrentPage(ContentContext ctx) throws Exception {
		ContentService.createContent(ctx.getRequest());
		MenuElement currentPage = ctx.getCurrentPage();
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest()); IUserFactory adminUserFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());

		if (currentPage.isBlocked()) {
			if (!currentPage.getBlocker().equals(adminUserFactory.getCurrentUser(ctx.getRequest().getSession()).getName())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * check is user have all right for modify the current page.
	 * 
	 * @param ctx
	 *            the contentcontext
	 * @return true if user have all right for modify the current page
	 * @throws Exception
	 */
	private static boolean checkPageSecurity(ContentContext ctx) throws Exception {
		AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance(ctx.getRequest().getSession().getServletContext());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		IUserFactory adminUserFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		ContentService.createContent(ctx.getRequest());
		MenuElement currentPage = ctx.getCurrentPage();
		if (currentPage.getEditorRoles().size() > 0) {
			if (!adminUserSecurity.haveRight(adminUserFactory.getCurrentUser(ctx.getRequest().getSession()), AdminUserSecurity.FULL_CONTROL_ROLE)) {
				if (!adminUserFactory.getCurrentUser(ctx.getRequest().getSession()).validForRoles(currentPage.getEditorRoles())) {
					MessageRepository messageRepository = MessageRepository.getInstance(ctx);
					I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.security.noright-onpage"), GenericMessage.ERROR));
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * copy file to a directory
	 * 
	 * @param sourceFile
	 * @param destDir
	 * @return the new file
	 * @throws IOException
	 */
	public static File copyFile(File sourceFile, File destDir) throws IOException {
		File newFile = new File("" + destDir + '/' + sourceFile.getName());
		FileUtils.copyFile(sourceFile, newFile);
		return newFile;
	}

	private static File getCurrentDir(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		StaticContext stcCtx = StaticContext.getInstance(request.getSession(), ctx.getRenderMode());
		RequestService requestService = RequestService.getInstance(request);
		String currentDirParam = requestService.getParameter("dir", "" + stcCtx.getCurrentPath());
		File currentDir = new File(URLHelper.mergePath(getStaticDir(request, response), currentDirParam));
		return currentDir;
	}

	/** STATIC MANAGEMENT */

	private static List<File> getFileList(HttpServletRequest request, HttpServletResponse response) throws Exception {

		int c = Integer.parseInt(request.getParameter("length"));

		List<File> outFiles = new LinkedList<File>();
		for (int i = 0; i < c; i++) {
			String key = "image-" + i;
			String fileName = request.getParameter(key);
			if (fileName != null) {
				File file = new File(getStaticDir(request, response), fileName);
				outFiles.add(file);
			}
		}
		return outFiles;
	}

	private static Map<String, LocaleStaticInfo> getStaticDescriptionList(ContentContext ctx) throws Exception {

		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		int c = Integer.parseInt(requestService.getParameter("length", null));
		Map<String, LocaleStaticInfo> allDescription = new HashMap<String, LocaleStaticInfo>();
		for (int i = 0; i < c; i++) {
			LocaleStaticInfo info = new LocaleStaticInfo();

			info.setTitle(requestService.getParameter("title-" + i, null));
			info.setDescription(requestService.getParameter("description-" + i, null));
			info.setLocation(requestService.getParameter("location-" + i, null));
			info.setShared(requestService.getParameter("shared-" + i, null) != null);
			String dateStr = requestService.getParameter("date-" + i, null);
			String pagePath = requestService.getParameter("linked-page-" + i, null);

			if (pagePath != null) {
				ContentService content = ContentService.createContent(ctx.getRequest());
				MenuElement page = content.getNavigation(ctx).searchChild(ctx, pagePath);
				if (page != null) {
					info.setPageId(page.getId());
				} else {
					info.setPageId("");
				}
			}
			try {
				if (dateStr != null && dateStr.trim().length() > 0) {
					Date date = StringHelper.parseDateOrTime(dateStr);
					info.setDate(date);
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}

			String staticURL = requestService.getParameter("static-url-" + i, null);
			if ((staticURL != null)) {
				allDescription.put(staticURL, info);
			}
		}
		return allDescription;
	}

	private static String getStaticDir(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
		if (ctx.getRenderMode() == ContentContext.ADMIN_MODE) {
			return staticConfig.getShareDataFolder();
		} else {
			return globalContext.getDataFolder();
		}
	}

	private static Map<String, String> getStaticPositionList(HttpServletRequest request) {

		int c = Integer.parseInt(request.getParameter("length"));
		Map<String, String> allPosition = new HashMap<String, String>();
		for (int i = 0; i < c; i++) {
			String position = request.getParameter("position-" + i);
			String staticURL = request.getParameter("static-url-" + i);
			if ((position != null) && (staticURL != null)) {
				allPosition.put(staticURL, position);
			}
		}
		return allPosition;
	}

	private static void modifPage(ContentContext ctx) throws Exception {
		ContentService.createContent(ctx.getRequest());
		MenuElement currentPage = ctx.getCurrentPage();
		currentPage.setModificationDate(new Date());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		currentPage.setLatestEditor(editCtx.getUserPrincipal().getName());
		currentPage.setValid(false);
		currentPage.releaseCache();
	}

	private static boolean nameExist(String name, ContentContext ctx) throws Exception {
		ContentService content = ContentService.createContent(ctx.getRequest());
		MenuElement page = content.getNavigation(ctx);
		return (page.searchChildFromName(name) != null);
	}

	/**
	 * this method add a page in the menu, by default this page is unvisible
	 * 
	 * @param request
	 *            current request
	 * @param response
	 *            current response
	 * @return message to the user
	 */
	public static String performAddnav(HttpServletRequest request, HttpServletResponse response) {

		String message = null;

		try {

			ContentContext ctx = ContentContext.getContentContext(request, response);
			GlobalContext globalContext = GlobalContext.getInstance(request);
			EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());

			String path = ctx.getPath();
			String nodeName = request.getParameter("node");

			I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
			message = validNodeName(nodeName, i18nAccess);
			// if (ContentManager.getPathDepth(path) >= editCtx.getMenuDepth())
			// {
			// String[][] balise = { { "depth", "" + editCtx.getMenuDepth() } };
			// message = i18nAccess.getText("action.menu.depth", balise);
			// }

			if (nameExist(nodeName, ctx)) {
				message = i18nAccess.getText("action.validation.name-allready-exist", new String[][] { { "name", nodeName } });
			}

			if (message == null) {				
				MenuElement elem = MenuElement.getInstance(globalContext);
				elem.setName(nodeName);
				elem.setCreator(editCtx.getUserPrincipal().getName());
				elem.setVisible(globalContext.isNewPageVisible());
				ContentService.createContent(request);
				ctx.getCurrentPage().addChildMenuElementAutoPriority(elem);
				path = path + "/" + nodeName;
				String msg = i18nAccess.getText("action.add.new-page", new String[][] { { "path", path } });
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
			}
			PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
			persistenceService.store(ctx);
			autoPublish(request, response);

			NavigationService navigationService = NavigationService.getInstance(globalContext, ctx.getRequest().getSession());
			navigationService.clearPage(ctx);

		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		return message;
	}

	/**
	 * this method add a page in the menu, by default this page is unvisible
	 * 
	 * @param request
	 *            current request
	 * @param response
	 *            current response
	 * @return message to the user
	 */
	public static String performAddnavfirst(HttpServletRequest request, HttpServletResponse response) {

		String message = null;

		try {

			ContentContext ctx = ContentContext.getContentContext(request, response);
			GlobalContext globalContext = GlobalContext.getInstance(request);
			EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());

			String path = ctx.getPath();
			String nodeName = request.getParameter("node");

			I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
			message = validNodeName(nodeName, i18nAccess);
			// if (ContentManager.getPathDepth(path) >= editCtx.getMenuDepth())
			// {
			// String[][] balise = { { "depth", "" + editCtx.getMenuDepth() } };
			// message = i18nAccess.getText("action.menu.depth", balise);
			// }

			if (nameExist(nodeName, ctx)) {
				message = i18nAccess.getText("action.validation.name-allready-exist", new String[][] { { "name", nodeName } });
			}

			if (message == null) {				
				MenuElement elem = MenuElement.getInstance(globalContext);
				elem.setName(nodeName);
				elem.setCreator(editCtx.getUserPrincipal().getName());
				elem.setVisible(globalContext.isNewPageVisible());
				ContentService.createContent(request);
				ctx.getCurrentPage().addChildMenuElementOnTop(elem);
				path = path + "/" + nodeName;
				String msg = i18nAccess.getText("action.add.new-page", new String[][] { { "path", path } });
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
			}
			PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
			persistenceService.store(ctx);
			autoPublish(request, response);

			NavigationService navigationService = NavigationService.getInstance(globalContext, ctx.getRequest().getSession());
			navigationService.clearPage(ctx);

		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		return message;
	}

	public static String performAdduser(HttpServletRequest request, HttpServletResponse response) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());		
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		boolean admin = editCtx.getCurrentView() == EditContext.ADMIN_USER_VIEW;
		if (ContentManager.getParameterValue(request, "login", "").trim().length() == 0) {
			return i18nAccess.getText("user.login-not-empty");
		}
		if (ContentManager.getParameterValue(request, "password", "").trim().length() == 0) {
			return i18nAccess.getText("user.password-not-empty");
		}
		IUserFactory userFactory;
		if (admin) {
			userFactory = AdminUserFactory.createUserFactory(globalContext, request.getSession());
		} else {
			userFactory = UserFactory.createUserFactory(globalContext, request.getSession());
		}
		ContentContext ctx = ContentContext.getContentContext(request, response);
		IUserInfo userInfo = userFactory.createUserInfos();
		userInfo.setLogin(request.getParameter("login"));
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		if (staticConfig.isPasswordEncryt()) {
			userInfo.setPassword(StringHelper.encryptPassword(request.getParameter("password")));
		} else {
			userInfo.setPassword(request.getParameter("password"));
		}
		try {
			userFactory.addUserInfo(userInfo);
		} catch (UserAllreadyExistException e) {
			return i18nAccess.getText("user.allready");
		}
		String msg = i18nAccess.getText("user.create");
		userFactory.store();
		MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
		return null;
	}

	public static String performAdminlogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return null;
	}

	public static String performAdminroles(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String msg = null;

		// String userRolesRaw = ContentManager.getParameterValue(request,
		// "user_roles", "");

		// String[] userRoles = userRolesRaw.split("\\" +
		// ContentManager.MULTI_PARAM_SEP);

		RequestService requestService = RequestService.getInstance(request);
		String[] adminRoles = requestService.getParameterValues("admin_roles", new String[0]);

		ContentService.createContent(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		if (!checkPageSecurity(ctx)) {
			return null;
		}

		MenuElement currentElement = ctx.getCurrentPage();
		synchronized (currentElement) {
			currentElement.clearEditorGroups();
			for (String role : adminRoles) {
				currentElement.addEditorRoles(role);
			}
		}
		GlobalContext globalContext = GlobalContext.getInstance(request);
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);

		return msg;
	}

	public static String performAdminuserfilter(HttpServletRequest request, HttpServletResponse response) {
		return performUserfilter(request, true);
	}

	public static String performBlockpage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentService.createContent(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		if (!checkPageSecurity(ctx)) {
			return null;
		}
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		MenuElement currentPage = ctx.getCurrentPage();
		AdminUserSecurity userSec = AdminUserSecurity.getInstance(request.getSession().getServletContext());
		IUserFactory adminUserFactory = AdminUserFactory.createUserFactory(globalContext, request.getSession());
		if (!currentPage.isBlocked()) {
			currentPage.setBlocked(true);
			currentPage.setBlocker(editCtx.getUserPrincipal().getName());
			PersistenceService.getInstance(globalContext).store(ctx);
		} else {
			if (currentPage.getBlocker().equals(adminUserFactory.getCurrentUser(request.getSession()).getName()) || userSec.haveRight(adminUserFactory.getCurrentUser(request.getSession()), "admin")) {
				currentPage.setBlocked(false);
				PersistenceService.getInstance(globalContext).store(ctx);
			}
		}

		return null;
	}

	public static String performCancelcopy(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String msg = null;

		ContentContext.getContentContext(request, response);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		editCtx.setPathForCopy(null);

		ClipBoard clipBoard = ClipBoard.getClibBoard(request);
		clipBoard.clear();

		return msg;
	}

	public static final String performChangeinterface(HttpServletRequest request, HttpServletResponse response) {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editContext = EditContext.getInstance(globalContext, request.getSession());
		editContext.setLightInterface(!editContext.isLightInterface());
		return null;
	}

	public static final String performChangemacrodate(HttpServletRequest request, HttpServletResponse response) {
		RequestService requestService = RequestService.getInstance(request);
		String dateStr = requestService.getParameter(MacroHelper.MACRO_DATE_KEY, null);
		if (dateStr != null) {
			try {
				Date date = StringHelper.parseDateOrTime(dateStr);
				MacroHelper.setCurrentMacroDate(request.getSession(), date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String performChangename(HttpServletRequest request, HttpServletResponse response) throws Exception {

		ContentContext ctx = ContentContext.getContentContext(request, response);
		if (!checkPageSecurity(ctx)) {
			return null;
		}
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		if (!canModifyCurrentPage(ctx)) {
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}

		String message = null;

		try {
			String nodeName = request.getParameter("name");
			message = validNodeName(nodeName, i18nAccess);

			if (nameExist(nodeName, ctx)) {
				message = i18nAccess.getText("action.validation.name-allready-exist", new String[][] { { "name", nodeName } });
			}

			if (message == null) {
				ContentService.createContent(request);
				MenuElement elem = ctx.getCurrentPage();
				// if (elem.getParent() != null) { // could not change the root
				// WHY NOT ?
				// element
				elem.setName(nodeName);
				GlobalContext globalContext = GlobalContext.getInstance(request);
				PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
				persistenceService.store(ctx);
				String msg = i18nAccess.getText("action.update.name", new String[][] { { "name", nodeName } });
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
				autoPublish(request, response);
				elem.releaseCache();
				if (elem.getParent() != null) {
					ctx.setPath(elem.getParent().getPath());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		return message;
	}

	public static String performChangetype(HttpServletRequest request, HttpServletResponse response) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		String newType = request.getParameter("type");
		String message = null;
		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
			if (newType != null) {
				editCtx.setActiveType(newType);
				newType = i18nAccess.getText("content." + newType, newType);
				ContentContext ctx = ContentContext.getContentContext(request, response);
				String msg = i18nAccess.getText("content.new-type", new String[][] { { "type", newType } });
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
			} else {
				message = "Fatal error : type not found";
			}
		} catch (FileNotFoundException e) {
			message = e.getMessage();
			e.printStackTrace();
		} catch (IOException e) {
			message = e.getMessage();
			e.printStackTrace();
		}
		return message;
	}

	public static String performChangeuserroles(HttpServletRequest request, HttpServletResponse response) {

		RequestService requestService = RequestService.getInstance(request);

		String msg = null;
		String login = requestService.getParameter("login", null);
		if (login != null) {
			GlobalContext globalContext = GlobalContext.getInstance(request);
			EditContext editContext = EditContext.getInstance(globalContext, request.getSession());
			boolean admin = (editContext.getCurrentView() == EditContext.ADMIN_USER_VIEW);
			IUserFactory userFact;
			if (admin) {
				userFact = AdminUserFactory.createUserFactory(globalContext, request.getSession());
			} else {
				userFact = UserFactory.createUserFactory(globalContext, request.getSession());
			}
			IUserInfo userInfos = userFact.getUserInfos(login);
			if (userInfos != null) {
				String[] roles = requestService.getParameterValues("roles", new String[0]);
				userInfos.setRoles(roles);
				userFact.updateUserInfo(userInfos);
			} else {
				logger.warning("user info not found for login : " + login);
			}
		}
		return msg;
	}

	public static String performChangeview(HttpServletRequest request, HttpServletResponse response) {
		String msg = null;
		String view = ContentManager.getParameterValue(request, "view", null);
		if (view != null) {
			GlobalContext globalContext = GlobalContext.getInstance(request);
			EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
			editCtx.setMailing(false);
			editCtx.setMainRenderer(null);
			editCtx.setCommandRenderer(null);
			editCtx.setViewCommand(true);
			editCtx.setViewComponent(true);
			editCtx.setViewMode(true);

			try {
				int newView = Integer.parseInt(view);

				switch (newView) {
				case EditContext.DATA_VIEW:
					editCtx.setMainRenderer("/jsp/edit/data/data.jsp");
					break;

				default:
					break;
				}

				editCtx.setCurrentView(newView);
			} catch (Exception e) {
				Logger.log(e);
			}
		}
		return msg;
	}

	public static String performChoosepaternity(HttpServletRequest request, HttpServletResponse response) {
		String message = null;

		try {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			if (!checkPageSecurity(ctx)) {
				return null;
			}
			GlobalContext globalContext = GlobalContext.getInstance(request);
			EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());			

			if (globalContext.isLightMenu()) {
				editCtx.setMainRenderer("/jsp/edit/paternity/choose_fraternity.jsp");
			} else {
				editCtx.setMainRenderer("/jsp/edit/paternity/choose_paternity.jsp");
			}
			editCtx.setCommandRenderer(null);
			editCtx.setViewCommand(true);
			editCtx.setViewComponent(false);
			editCtx.setViewMode(true);

		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		return message;
	}

	public static String performChoosetemplate(HttpServletRequest request, HttpServletResponse response) {
		String message = null;

		try {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			if (!checkPageSecurity(ctx)) {
				return null;
			}
			GlobalContext globalContext = GlobalContext.getInstance(request);
			EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
			editCtx.setMainRenderer("/jsp/edit/user_template/choose_template.jsp");
			editCtx.setCommandRenderer("/jsp/edit/user_template/template_search.jsp");
			editCtx.setViewCommand(false);
			editCtx.setViewComponent(false);
			editCtx.setViewMode(true);

		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		return message;
	}

	public static String performChoosevirtualpaternity(HttpServletRequest request, HttpServletResponse response) {
		String message = null;

		try {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			if (!checkPageSecurity(ctx)) {
				return null;
			}
			GlobalContext globalContext = GlobalContext.getInstance(request);
			EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
			editCtx.setMainRenderer("/jsp/edit/paternity/choose_virtual_paternity.jsp");
			editCtx.setCommandRenderer(null);
			editCtx.setViewCommand(true);
			editCtx.setViewComponent(false);
			editCtx.setViewMode(true);
		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		return message;
	}

	public static String performCopy(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String message = null;

		String id = request.getParameter("comp_id");
		if (id != null) {
			ContentService.createContent(request);
			ContentContext ctx = ContentContext.getContentContext(request, response);
			MenuElement currentPage = ctx.getCurrentPage();
			ContentElementList contents = currentPage.getContent(ctx);
			while (contents.hasNext(ctx)) {
				IContentVisualComponent elem = contents.next(ctx);
				if (elem.getId().equals(id)) {
					ClipBoard clibBoard = ClipBoard.getClibBoard(request);
					clibBoard.copy(elem.getBean(ctx));
				}
			}
		}

		return message;
	}

	public static String performCopypagestructure(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String msg = null;

		ContentContext ctx = ContentContext.getContentContext(request, response);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());

		editCtx.setPathForCopy(ctx);

		return msg;
	}

	public static String performCopystaticfile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		List<File> fileList = getFileList(request, response);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		StaticContext stcCtx = StaticContext.getInstance(request.getSession(), ctx.getRenderMode());
		stcCtx.setCutFiles(Collections.EMPTY_LIST);
		stcCtx.setCopyFiles(fileList);
		return null;
	}

	public static String performCreaterole(HttpServletRequest request, HttpServletResponse response) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
		RequestService requestService = RequestService.getInstance(request);
		EditContext editContext = EditContext.getInstance(globalContext, request.getSession());
		boolean admin = editContext.getCurrentView() == EditContext.ADMIN_USER_VIEW;

		String name = requestService.getParameter("role-name", "");
		if ((name.trim().length() == 0) || (!PatternHelper.ALPHANNUM_NOSPACE_PATTERN.matcher(name).matches())) {
			String msg = i18nAccess.getText("action.role.setrole");
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
		} else {			
			if (admin) {
				Set<String> roles = new HashSet<String>(globalContext.getAdminUserRoles());
				roles.add(name);
				globalContext.setAdminUserRoles(roles);
			} else {
				Set<String> roles = new HashSet<String>(globalContext.getUserRoles());
				roles.add(name);
				globalContext.setUserRoles(roles);
			}
			String msg = i18nAccess.getText("action.role.added", new String[][] { { "role", name } });
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
		}
		return null;
	}

	public static String performCutstaticfile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		List fileList = getFileList(request, response);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		StaticContext stcCtx = StaticContext.getInstance(request.getSession(), ctx.getRenderMode());
		stcCtx.setCopyFiles(Collections.EMPTY_LIST);
		stcCtx.setCutFiles(fileList);
		return null;
	}

	public static String performData(HttpServletRequest request, HttpServletResponse response) {
		String msg = null;

		return msg;
	}

	public static String performDeleterole(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
		RequestService requestService = RequestService.getInstance(request);
		EditContext editContext = EditContext.getInstance(globalContext, request.getSession());
		boolean admin = editContext.getCurrentView() == EditContext.ADMIN_USER_VIEW;

		String role = requestService.getParameter("edit-roles-list", null);
		if (role == null) {
			String msg = i18nAccess.getText("action.role.chooserole");
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
		} else {			
			if (!admin) {
				Set<String> roles = new HashSet<String>(globalContext.getUserRoles());
				roles.remove(role);
				globalContext.setUserRoles(roles);
			} else {
				Set<String> roles = new HashSet<String>(globalContext.getAdminUserRoles());
				roles.remove(role);
				globalContext.setAdminUserRoles(roles);
			}
			String msg = i18nAccess.getText("action.role.deleted", new String[][] { { "role", role } });
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
		}
		return null;
	}

	public static String performDeletestaticfile(HttpServletRequest request, HttpServletResponse response) throws Exception {

		int c = Integer.parseInt(request.getParameter("length"));

		for (int i = 0; i < c; i++) {
			String key = "image-" + i;
			String fileName = request.getParameter(key);
			if (fileName != null) {
				ContentContext ctx = ContentContext.getContentContext(request, response);

				StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
				String staticPath;
				if (ctx.getRenderMode() == ContentContext.ADMIN_MODE) {
					staticPath = staticConfig.getShareDataFolder();
				} else {
					GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
					staticPath = globalContext.getDataFolder();
				}

				File file = new File(URLHelper.mergePath(staticPath, fileName));
				StaticInfo fileInfo = StaticInfo.getInstance(ctx, file);
				if (file.delete()) {
					FileCache fileCache = FileCache.getInstance(request.getSession().getServletContext());
					String fromDateFolderURL = URLHelper.mergePath(staticConfig.getStaticFolder(), fileInfo.getStaticURL());
					fileCache.delete(fromDateFolderURL);
					logger.info("file : " + file + " deleted.");
					StaticInfo.getInstance(ctx, file).delete(ctx);
					GlobalContext globalContext = GlobalContext.getInstance(request);
					PersistenceService.getInstance(globalContext).store(ctx); // save static info
				} else {
					logger.severe("file : " + file + " can not be deleted.");
				}
			}
		}
		return null;
	}

	public static String performDeleteuser(HttpServletRequest request, HttpServletResponse response) {
		String msg = null;
		String login = ContentManager.getParameterValue(request, "login", null);
		if (login != null) {
			GlobalContext globalContext = GlobalContext.getInstance(request);
			EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
			boolean admin = editCtx.getCurrentView() == EditContext.ADMIN_USER_VIEW;
			IUserFactory fact;			
			if (admin) {
				fact = AdminUserFactory.createUserFactory(globalContext, request.getSession());
			} else {
				fact = UserFactory.createUserFactory(globalContext, request.getSession());
			}
			fact.deleteUser(login);
		}
		return msg;
	}

	public static final String performEditmode(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		ctx.setRenderMode(ContentContext.EDIT_MODE);
		return null;
	}

	public static final String performEditoneiframe(HttpServletRequest request, HttpServletResponse response) {
		// special action executed in the servlet
		return null;
	}

	public static String performFilecorrection(HttpServletRequest request, HttpServletResponse response) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.correctAllFiles();
		ContentService.createContent(request).releaseAll(ContentContext.getContentContext(request, response), globalContext);
		return "";
	}

	/**
	 * find static element in content.
	 * 
	 * @param request
	 *            the current request
	 * @param response
	 *            the current response
	 * @return error message
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String performFindstatic(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			ContentService content = ContentService.createContent(request);
			ContentContext ctx = ContentContext.getContentContext(request, response);
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			MenuElement page = content.getNavigation(ctx);

			StaticContext stcCtx = StaticContext.getInstance(request.getSession());
			File currentDir = new File(URLHelper.mergePath(globalContext.getDataFolder(), "" + stcCtx.getCurrentPath()));
			File[] files = currentDir.listFiles(new NotDirectoryFilter());

			int ref = 0;

			for (File file : files) {
				ResourceHelper.extractNotStaticDir(StaticConfig.getInstance(request.getSession()), globalContext, file.getAbsolutePath());

				String ressourceURIWithoutStatic = ResourceHelper.extractRessourceDir(StaticConfig.getInstance(request.getSession()), globalContext, file.getAbsolutePath());

				StaticInfo info = StaticInfo.getInstance(ctx, file);

				Set<String> lgs = globalContext.getContentLanguages();

				ContentContext lgCtx = new ContentContext(ctx);
				Collection<MenuElement> containers = new LinkedList<MenuElement>();
				for (String lg : lgs) {
					lgCtx.setRequestContentLanguage(lg);
					containers.addAll(searchStaticURIInContent(lgCtx, ressourceURIWithoutStatic, page));
					ref = ref + containers.size();
				}

				info.setContainers(containers);
			}

			I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			new Locale(ctx.getRequestContentLanguage());
			String msg = i18nAccess.getText("action.static.static-not-found");
			if (ref > 0) {
				msg = i18nAccess.getText("action.static.static-found", new String[][] { { "ref", "" + ref } });
			}
			messageRepository.setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));

		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}

		return null;
	}

	public static String performHttps(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String message = null;

		try {

			ContentContext ctx = ContentContext.getContentContext(request, response);
			if (!checkPageSecurity(ctx)) {
				return null;
			}

			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

			if (!canModifyCurrentPage(ctx)) {
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
				return null;
			}

			String path = ctx.getPath();
			String https = request.getParameter("https");

			if (!https.matches("yes|no")) {
				message = i18nAccess.getText("action.visible.validation");
			} else {
				MenuElement elem = ctx.getCurrentPage();
				elem.setHttps(StringHelper.isTrue(https));
				GlobalContext globalContext = GlobalContext.getInstance(request);
				PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
				persistenceService.store(ctx);
				if (https.equals("yes")) {
					String msg = i18nAccess.getText("action.https.confirm-yes", new String[][] { { "path", path } });
					MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
				} else {
					String msg = i18nAccess.getText("action.https.confirm-no", new String[][] { { "path", path } });
					MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		autoPublish(request, response);
		return message;
	}

	public static String performImportpage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		if (!checkPageSecurity(ctx)) {
			return null;
		}
		ContentService.createContent(ctx.getRequest());
		MenuElement currentPage = ctx.getCurrentPage();

		String importURL = requestService.getParameter("import-url", null);

		if (importURL != null) {
			String XMLURL = StringHelper.changeFileExtension(importURL, "xml");
			InputStream in = null;
			try {
				URL url = new URL(XMLURL);
				in = url.openStream();
				NodeXML node = XMLFactory.getFirstNode(in);
				NodeXML pageNode = node.getChild("page");
				GlobalContext globalContext = GlobalContext.getInstance(request);
				PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
				NavigationHelper.importPage(ctx, persistenceService, pageNode, currentPage, ctx.getLanguage(), false);
				NodeXML resourcesNode = node.getChild("resources");
				if (resourcesNode != null) {
					String baseURL = resourcesNode.getAttributeValue("url");
					Collection<Resource> resources = new LinkedList<Resource>();
					NodeXML resourceNode = resourcesNode.getChild("resource");
					if (resourceNode == null) {
						logger.warning("resource node not found in : " + url);
					}
					while (resourceNode != null) {
						if (baseURL != null) {
							Resource resource = new Resource();
							resource.setId(resourceNode.getAttributeValue("id"));
							resource.setUri(resourceNode.getAttributeValue("uri"));
							resources.add(resource);							
							ResourceHelper.downloadResource(globalContext.getDataFolder(), baseURL, resources);
						}
						resourceNode = resourceNode.getNext("resource");
					}
				} else {
					logger.warning("resources node not found in : " + url);
					MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage("resources node not found in : " + url, GenericMessage.ERROR));
				}
			} catch (Exception e) {
				e.printStackTrace();
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(e.getMessage(), GenericMessage.ERROR));
			} finally {
				ResourceHelper.closeResource(in);
			}

		}
		return null;
	}

	/**
	 * insert a new content
	 * 
	 * @param request
	 *            current request
	 * @param response
	 *            current response
	 * @return a message i18n, in the language of the context
	 */
	public static String performInsert(HttpServletRequest request, HttpServletResponse response) {

		String message = null;

		try {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			if (!checkPageSecurity(ctx)) {
				return null;
			}
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			GlobalContext globalContext = GlobalContext.getInstance(request);
			EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());

			if (!canModifyCurrentPage(ctx)) {
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
				return null;
			}

			RequestService requestService = RequestService.getInstance(request);

			String parentId = requestService.getParameter("number", null);
			String repeatStr = requestService.getParameter("repeat", null);
			String initContent = requestService.getParameter("content", null);
			String beforeValue = requestService.getParameter("before_value", "");
			String afterValue = requestService.getParameter("after_value", "");
			String compID = requestService.getParameter("compId", null);
			String compType = requestService.getParameter("comp-type", null);
			if (compType != null) {
				editCtx.setActiveType(compType);
			}

			if (requestService.getParameter("last", null) != null) {
				ContentService.createContent(ctx.getRequest());
				MenuElement currentPage = ctx.getCurrentPage();
				ContentElementList elementList = currentPage.getContent(ctx);
				while (elementList.hasNext(ctx)) {
					parentId = elementList.next(ctx).getId();
				}
			}

			// System.out.println("***** TRACE [EditActions.performInsert] : parentId = "+parentId);
			// /* --TRACE-- 27 janv. 2010 11:24:40 *///TODO: remove trace
			// System.out.println("***** TRACE [EditActions.performInsert] : beforeValue = "+beforeValue);
			// /* --TRACE-- 27 janv. 2010 11:26:51 *///TODO: remove trace
			// System.out.println("***** TRACE [EditActions.performInsert] : afterValue = "+afterValue);
			// /* --TRACE-- 27 janv. 2010 11:26:55 *///TODO: remove trace

			int repeat = 1;
			try {
				repeat = Integer.parseInt(repeatStr);
			} catch (RuntimeException e) {
				// if error when parse repeat str default value is 1
			}
			String type = editCtx.getActiveType();

			ContentService content = ContentService.createContent(request);

			ComponentContext compCtx = ComponentContext.getInstance(request);

			if (compID != null) { // extract procedure
				IContentVisualComponent oldComp = content.getComponent(ctx, compID);

				// find parentID for the current element
				IContentComponentsList contentList = ctx.getCurrentPage().getContent(ctx);
				IContentVisualComponent parent = null;
				parentId = "0";
				while (contentList.hasNext(ctx)) {
					IContentVisualComponent nextComp = contentList.next(ctx);
					if (nextComp.getId().equals(compID)) {
						if (parent != null) {
							parentId = parent.getId();
						}
					}
					parent = nextComp;
				}

				if (oldComp == null) {
					throw new Exception("component : " + compID + " not found.");
				} else if (beforeValue.trim().length() > 0) {
					String newId = content.createContent(ctx, parentId, oldComp.getType(), beforeValue);
					parentId = newId;
					compCtx.addNewComponent(content.getComponent(ctx, newId));
				}
			}

			for (int i = 0; i < repeat; i++) {

				message = null; // TODO: generate a better message when insert
				// multiple component.

				ComponentBean bean = new ComponentBean();
				bean.setType(type);
				IContentVisualComponent comp = ComponentFactory.CreateComponent(ctx, bean, null, null, null);
				if (comp.isUnique()) {
					IContentComponentsList contentList = ctx.getCurrentPage().getContent(ctx);
					while (contentList.hasNext(ctx)) {
						IContentVisualComponent component = contentList.next(ctx);
						if (component.getType().equals(type)) {
							// String i18nType = i18nAccess.getText("content." +
							// type);							
							String i18nType = StringHelper.getFirstNotNull(comp.getComponentLabel(ctx,globalContext.getEditLanguage()), i18nAccess.getText("content." + type));
							message = i18nAccess.getText("action.add.error.unique", new String[][] { { "type", i18nType } });
							MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(message, GenericMessage.ERROR));
						}
					}
				}

				if (message == null) {

					modifPage(ctx);

					logger.info("create new component : " + type);

					String newId = content.createContent(ctx, parentId, type, initContent);

					compCtx.addNewComponent(content.getComponent(ctx, newId));

					if (comp.isContainer()) {
						newId = content.createContent(ctx, newId, type, null);
						compCtx.addNewComponent(content.getComponent(ctx, newId));
					}

					if (compID != null) { // extract procedure
						IContentVisualComponent oldComp = content.getComponent(ctx, compID);
						if (oldComp == null) {
							throw new Exception("component : " + compID + " not found.");
						} else {
							compCtx.addNewComponent(oldComp);
							oldComp.setValue(afterValue);
						}
					}

					// String i18nType = i18nAccess.getText("content." + type);					
					String i18nType = StringHelper.getFirstNotNull(comp.getComponentLabel(ctx,globalContext.getEditLanguage()), i18nAccess.getText("content." + type));
					String msg = i18nAccess.getText("action.add.type", new String[][] { { "type", i18nType } });
					MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));

					parentId = newId;
					request.setAttribute("new_component_id", newId);
					autoPublish(request, response);
				}
			}
			// NO persitence when juste insert element
			// GlobalContext globalContext = GlobalContext.getInstance(request); PersistenceService.getInstance(globalContext).store(ctx);

		} catch (Exception e) {
			message = e.getMessage();
			e.printStackTrace();
		}

		return message;
	}

	/**
	 * check the message if a component is insered
	 * 
	 * @param request
	 *            current request
	 * @param response
	 *            current response
	 * @return a message i18n, in the language of the context
	 */
	public static String performInsertmsg(HttpServletRequest request, HttpServletResponse response) {

		String message = null;

		try {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			GlobalContext globalContext = GlobalContext.getInstance(request);
			EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());

			if (!canModifyCurrentPage(ctx)) {
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
				return null;
			}

			RequestService requestService = RequestService.getInstance(request);

			String parentId = requestService.getParameter("number", null);
			String repeatStr = requestService.getParameter("repeat", null);

			String beforeValue = requestService.getParameter("before_value", null); // if

			String compID = requestService.getParameter("compId", null); // if

			int repeat = 1;
			try {
				repeat = Integer.parseInt(repeatStr);
			} catch (RuntimeException e) {
				// if error when parse repeat str default value is 1
			}
			String type = editCtx.getActiveType();

			ContentService content = ContentService.createContent(request);

			ComponentContext compCtx = ComponentContext.getInstance(request);

			if (compID != null) { // extract procedure
				IContentVisualComponent oldComp = content.getComponent(ctx, compID);

				// find parentID for the current element
				IContentComponentsList contentList = ctx.getCurrentPage().getContent(ctx);
				IContentVisualComponent parent = null;
				parentId = "0";
				while (contentList.hasNext(ctx)) {
					IContentVisualComponent nextComp = contentList.next(ctx);
					if (nextComp.getId().equals(compID)) {
						if (parent != null) {
							parentId = parent.getId();
						}
					}
					parent = nextComp;
				}

				if (oldComp == null) {
					throw new Exception("component : " + compID + " not found.");
				} else if (beforeValue.trim().length() > 0) {
					String newId = content.createContent(ctx, parentId, oldComp.getType(), beforeValue);
					parentId = newId;
					compCtx.addNewComponent(content.getComponent(ctx, newId));
				}
			}

			for (int i = 0; i < repeat; i++) {

				message = null;

				ComponentBean bean = new ComponentBean();
				bean.setType(type);
				IContentVisualComponent comp = ComponentFactory.CreateComponent(ctx, bean, null, null, null);
				if (comp.isUnique()) {
					IContentComponentsList contentList = ctx.getCurrentPage().getContent(ctx);
					while (contentList.hasNext(ctx)) {
						IContentVisualComponent component = contentList.next(ctx);
						if (component.getType().equals(type)) {
							// String i18nType = i18nAccess.getText("content." +
							// type);							
							String i18nType = StringHelper.getFirstNotNull(comp.getComponentLabel(ctx,globalContext.getEditLanguage()), i18nAccess.getText("content." + type));
							message = i18nAccess.getText("action.add.error.unique", new String[][] { { "type", i18nType } });
							MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(message, GenericMessage.ERROR));
						}
					}
				}
			}

		} catch (Exception e) {
			message = e.getMessage();
			e.printStackTrace();
		}

		return null;
	}

	public static String performInsertpage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		if (requestService.getParameter("add-first", null) == null) {
			return performAddnav(request, response);
		} else {
			return performAddnavfirst(request, response);
		}
	}

	public static String performLinkpage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		if (!checkPageSecurity(ctx)) {
			return null;
		}
		RequestService requestService = RequestService.getInstance(request);
		String importURL = requestService.getParameter("import-url", null);
		MenuElement currentPage = ctx.getCurrentPage();
		currentPage.setLinkedURL(importURL);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);
		currentPage.updateLinkedData(ctx);
		return null;
	}

	public static String performMacro(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		requestService.getParameter("macro", "");
		Collection<IMacro> macros = MacroFactory.getInstance(StaticConfig.getInstance(request.getSession().getServletContext())).getMacros();
		for (IMacro macro : macros) {
			if (requestService.getParameter("macro-" + macro.getName(), null) != null) {
				ContentContext ctx = ContentContext.getContentContext(request, response);
				macro.perform(ctx, Collections.EMPTY_MAP);
			}
		}
		return null;
	}

	public static final String performMailingstatfilter(HttpServletRequest request, HttpServletResponse response) {
		RequestService requestService = RequestService.getInstance(request);
		String filter = requestService.getParameter("filter", null);
		String clear = requestService.getParameter("clear", null);		
		if (clear != null) {
			StatContext ctx = StatContext.getInstance(request);
			ctx.setMailingFilter("");
		} else {
			if (filter != null) {
				StatContext ctx = StatContext.getInstance(request);
				if (filter.trim().length() == 0) {
					ctx.setMailingFilter("");
				} else {
					ctx.setMailingFilter(filter);
				}
			}
		}
		return null;
	}

	public static String performManualdate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentService.createContent(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		MenuElement currentPage = ctx.getCurrentPage();
		if (currentPage.getManualModificationDate() != null) {
			currentPage.setManualModificationDate(null);
		} else {
			currentPage.setManualModificationDate(currentPage.getRealModificationDate());
		}
		return null;
	}

	public static String performManualdatemodification(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentService.createContent(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		MenuElement currentPage = ctx.getCurrentPage();

		RequestService requestService = RequestService.getInstance(request);
		String newDate = requestService.getParameter("new-date", "");

		try {
			Date date = StringHelper.parseDate(newDate);
			currentPage.setManualModificationDate(date);
		} catch (ParseException e) {
			GlobalContext globalContext = GlobalContext.getInstance(request);
			I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
			String msg = i18nAccess.getText("component.message.warning.date-format");
			return msg;
		}
		return null;
	}

	public static String performMergeusers(HttpServletRequest request, HttpServletResponse response) {

		GlobalContext globalContext = GlobalContext.getInstance(request);
		
		EditContext editContext = EditContext.getInstance(globalContext, request.getSession());

		RequestService requestService = RequestService.getInstance(request);

		Collection<FileItem> fileItems = requestService.getAllFileItem();

		String msg = null;
		request.getAttributeNames();
		for (FileItem item : fileItems) {
			if (item.getFieldName().trim().length() > 1) {
				try {
					boolean admin = (editContext.getCurrentView() == EditContext.ADMIN_USER_VIEW);
					IUserFactory userFact;					
					if (admin) {
						userFact = AdminUserFactory.createUserFactory(globalContext, request.getSession());
					} else {
						userFact = UserFactory.createUserFactory(globalContext, request.getSession());
					}
					Charset charset = Charset.forName(ContentContext.CHARACTER_ENCODING);
					if (StringHelper.getFileExtension(item.getName()).equals("txt")) { // hack
						// for
						// excel
						// unicode
						// export
						charset = Charset.forName("utf-16");
					}

					CSVFactory csvFact = new CSVFactory(item.getInputStream(), null, charset);
					String[][] usersArrays = csvFact.getArray();
					Collection<IUserInfo> userInfoList = new LinkedList<IUserInfo>();
					for (int i = 1; i < usersArrays.length; i++) {
						IUserInfo userInfo = userFact.createUserInfos();
						String[] labels = usersArrays[0];
						try {
							BeanHelper.copy(JavaHelper.createMap(labels, usersArrays[i]), userInfo);
							userInfoList.add(userInfo);
						} catch (Exception e) {
							Logger.log(Logger.WARNING, "user: " + userInfo.getLogin() + " can not be insert.");
						}
					}

					if (userInfoList.size() > 0) {
						// userFact.clearUserInfoList();
						for (Object element2 : userInfoList) {
							UserInfos element = (UserInfos) element2;
							userFact.mergeUserInfo(element);
						}
						userFact.store();
					}

				} catch (Exception e) {
					Logger.log(e);
				}

			}
		}
		return msg;
	}

	public static String performMkdir(HttpServletRequest request, HttpServletResponse response) throws Exception {
		File currentDir = getCurrentDir(request, response);
		String newDir = request.getParameter("new-dir");
		if (newDir != null) {
			if (new File("" + currentDir + '/' + newDir).mkdir()) {
				Logger.log(Logger.ERROR, "error can not create directory : " + newDir);
			}
		}
		return null;
	}

	public static final String performModifytemplate(HttpServletRequest request, HttpServletResponse response) throws ConfigurationException, IOException {
		RequestService requestService = RequestService.getInstance(request);

		String templateId = requestService.getParameter("template", null);

		if (templateId != null) {
			AdminContext adminContext = AdminContext.getInstance(request.getSession());
			adminContext.setFileToEdit(null);

			GlobalContext globalContext = GlobalContext.getInstance(request);
			EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
			editCtx.setMainRenderer("/jsp/admin/template/modify_template.jsp");
			editCtx.setCommandRenderer("/jsp/admin/template/template_upload.jsp");
			editCtx.setViewCommand(false);
			editCtx.setViewComponent(false);
			editCtx.setViewMode(true);
		}

		return null;

	}

	public static String performMovedown(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		if (!checkPageSecurity(ctx)) {
			return null;
		}
		MenuElement elem = ctx.getCurrentPage();
		if (elem.getParent() != null) {
			MenuElement parent = elem.getParent();
			MenuElement[] children = parent.getChildMenuElements();
			NavigationHelper.changeStepPriority(children, 10);
			for (int i = 0; i < children.length - 1; i++) {
				if (children[i].equals(elem)) {
					elem.setPriority(children[i + 1].getPriority() + 5);
				}
			}
			GlobalContext globalContext = GlobalContext.getInstance(request);
			PersistenceService.getInstance(globalContext).store(ctx);
		}
		return null;
	}

	public static String performMovepreview(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		MenuElement elem = ctx.getCurrentPage();
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		MenuElement pageToMove = elem.searchChildFromId(requestService.getParameter("select-page", null));
		if (pageToMove != null) {
			MenuElement targetPage = null;
			if (elem != null) {
				MenuElement[] children = elem.getChildMenuElements();
				NavigationHelper.changeStepPriority(children, 10);
				if (requestService.getParameter("page_0", null) != null) {
					pageToMove.setPriority(5);
				} else {
					for (MenuElement element : children) {
						if (requestService.getParameter("page_" + element.getId(), null) != null) {
							targetPage = element;
							pageToMove.setPriority(targetPage.getPriority() + 5);
						}
					}
				}
				if (targetPage != null) {
					String msg = i18nAccess.getText("action.move-page.moved", new String[][] { { "page", pageToMove.getTitle(ctx) }, { "target", targetPage.getTitle(ctx) } });
					MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
				} else if (requestService.getParameter("page_0", null) != null) {
					MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(i18nAccess.getText("action.move-page.move-on-top"), GenericMessage.INFO));
				} else {
					logger.warning("target page not found.");
					MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(i18nAccess.getText("global.page-not-found"), GenericMessage.ERROR));
				}
				GlobalContext globalContext = GlobalContext.getInstance(request);
				PersistenceService.getInstance(globalContext).store(ctx);
			} else {
				logger.warning("page not found : " + requestService.getParameter("select-page", null));
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(i18nAccess.getText("global.page-not-found"), GenericMessage.ERROR));
			}
		}
		return null;
	}

	public static String performMovetochild(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String pageId = request.getParameter("pageId");
		if (pageId != null) {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			if (!checkPageSecurity(ctx)) {
				return null;
			}
			MenuElement elem = ContentService.createContent(request).getNavigation(ctx).searchChildFromId(pageId);
			if ((elem != null) && (elem.getParent() != null)) {
				String path = elem.getPath();
				MenuElement newParent = null;
				MenuElement[] elems = elem.getParent().getChildMenuElements();
				for (int i = 0; i < elems.length - 1; i++) {
					if (elems[i].equals(elem)) {
						newParent = elems[i + 1];
					}
				}
				elem.moveToParent(newParent);
				GlobalContext globalContext = GlobalContext.getInstance(request);
				PersistenceService.getInstance(globalContext).store(ctx);
				I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
				String[][] balises = { { "path", path }, { "new-path", elem.getPath() } };
				String msg = i18nAccess.getText("action.link.move", balises);
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
			}
		} else {
			logger.warning("action movetochild could not execute because pageId not found in request.");
		}
		return null;
	}

	public static String performMovetoparent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String pageId = request.getParameter("pageId");
		if (pageId != null) {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			if (!checkPageSecurity(ctx)) {
				return null;
			}
			MenuElement elem = ContentService.createContent(request).getNavigation(ctx).searchChildFromId(pageId);
			if ((elem != null) && (elem.getParent() != null)) {
				if (elem.getParent().getParent() != null) {
					String path = elem.getPath();
					elem.moveToParent(elem.getParent().getParent());
					GlobalContext globalContext = GlobalContext.getInstance(request);
					PersistenceService.getInstance(globalContext).store(ctx);
					I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
					String[][] balises = { { "path", path }, { "new-path", elem.getPath() } };
					String msg = i18nAccess.getText("action.link.move", balises);
					MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
					// TODO : check why this message never set on display
				}
			}
		} else {
			logger.warning("action movetoparent could not execute because pageId not found in request.");
		}
		return null;
	}

	public static String performMoveup(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		if (!checkPageSecurity(ctx)) {
			return null;
		}
		MenuElement elem = ctx.getCurrentPage();
		if (elem.getParent() != null) {
			MenuElement parent = elem.getParent();
			MenuElement[] children = parent.getChildMenuElements();
			NavigationHelper.changeStepPriority(children, 10);
			for (int i = 1; i < children.length; i++) {
				if (children[i].equals(elem)) {
					elem.setPriority(children[i - 1].getPriority() - 5);
				}
			}
			GlobalContext globalContext = GlobalContext.getInstance(request);
			PersistenceService.getInstance(globalContext).store(ctx);
		}
		return null;
	}

	public static String performPaste(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String message = null;

		ContentContext ctx = ContentContext.getContentContext(request, response);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		if (!checkPageSecurity(ctx)) {
			return null;
		}

		if (!canModifyCurrentPage(ctx)) {
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}

		ClipBoard clipBoard = ClipBoard.getClibBoard(request);
		if (!clipBoard.isEmpty(ctx)) {
			String parentId = ContentManager.getParameterValue(request, "number", null);
			ComponentBean bean = (ComponentBean) clipBoard.getCopied();
			ContentService content = ContentService.createContent(request);
			bean.setArea(ctx.getArea());
			content.createContent(ctx, bean, parentId);

			modifPage(ctx);

			GlobalContext globalContext = GlobalContext.getInstance(request);
			PersistenceService.getInstance(globalContext).store(ctx);
			autoPublish(request, response);
		}
		return message;
	}

	public static String performPastepage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String msg = null;

		ContentService content = ContentService.createContent(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);

		if (!checkPageSecurity(ctx)) {
			return null;
		}

		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		ContentContext newCtx = editCtx.getContextForCopy();
		newCtx.setRequest(request);
		newCtx.setResponse(response);

		ContentElementList elems = newCtx.getCurrentPage().getContent(newCtx);

		String parentId = ContentManager.getParameterValue(request, "number", null);
		IContentVisualComponent parent = content.getComponent(ctx, parentId);

		while (elems.hasNext(ctx)) {
			ComponentBean bean = elems.next(ctx).getBean(ctx);
			bean.setArea(ctx.getArea());
			bean.setLanguage(ctx.getContentLanguage());

			parentId = content.createContent(ctx, bean, parentId);
		}

		if (parent != null) {
			IContentVisualComponent nextParent = parent.next();
			IContentVisualComponent newParent = content.getComponent(ctx, parentId);
			newParent.setNextComponent(nextParent); // reconnect the list
			if (nextParent != null) {
				nextParent.setPreviousComponent(newParent);
			}
		}

		modifPage(ctx);		
		PersistenceService.getInstance(globalContext).store(ctx);
		autoPublish(request, response);
		return msg;
	}

	public static String performPastepagestructure(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String msg = null;

		ContentService content = ContentService.createContent(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		if (!checkPageSecurity(ctx)) {
			return null;
		}

		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		ContentContext newCtx = editCtx.getContextForCopy();
		newCtx.setRequest(request);
		newCtx.setResponse(response);

		// if ( !(Content.contentExistForContext(newCtx,request) ) ) {
		// newCtx.setPath(null);
		// } else {
		ContentElementList elems = newCtx.getCurrentPage().getContent(newCtx);

		String parentId = "0";

		while (elems.hasNext(ctx)) {
			ComponentBean bean = elems.next(ctx).getBean(ctx);
			// parentId = dao.createNewContent(parentId, bean.getType(), ctx,
			// bean.getValue());
			parentId = content.createContent(ctx, bean, parentId);
		}
		// }

		modifPage(ctx);
		autoPublish(request, response);
		return msg;

	}

	public static String performPastestaticfile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		StaticContext stcCtx = StaticContext.getInstance(request.getSession(), ctx.getRenderMode());

		File currentPath = getCurrentDir(request, response);
		Iterator copyFiles = stcCtx.getCopyFiles().iterator();
		Iterator cutFiles = stcCtx.getCutFiles().iterator();

		while (copyFiles.hasNext()) {
			File file = (File) copyFiles.next();
			copyFile(file, currentPath);
			// RessourceHelper.renameResource(ctx, file, newFile);
		}
		while (cutFiles.hasNext()) {
			File file = (File) cutFiles.next();
			File newFile = copyFile(file, currentPath);
			file.delete();
			ResourceHelper.renameResource(ctx, file, newFile);
		}

		GlobalContext globalContext = GlobalContext.getInstance(request); PersistenceService.getInstance(globalContext).store(ctx); // save static info

		stcCtx.getCutFiles().clear();

		return null;
	}

	public static final String performPreviewedit(HttpServletRequest request, HttpServletResponse response) {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		editCtx.setEditPreview(!editCtx.isEditPreview());
		return null;
	}

	public static String performPublish(HttpServletRequest request, HttpServletResponse response) throws Exception {

		DebugHelper.writeInfo(System.out);

		ContentService content = ContentService.createContent(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		synchronized (content.getNavigation(ctx).getLock()) {

			String message = null;

			GlobalContext globalContext = GlobalContext.getInstance(request);

			PersistenceService persistenceService = PersistenceService.getInstance(globalContext);

			if (!globalContext.isPortail()) {
				// content.setViewNav(content.getNavigation(ctx));
				// persistenceService.store(ctx, ContentContext.VIEW_MODE);
				persistenceService.publishPreviewFile(ctx);
			} else {
				ContentContext viewCtx = new ContentContext(ctx);
				viewCtx.setRenderMode(ContentContext.VIEW_MODE);

				MenuElement viewNav = content.getNavigation(viewCtx);
				NavigationHelper.publishNavigation(ctx, content.getNavigation(ctx), viewNav);
				persistenceService.store(viewCtx, ContentContext.VIEW_MODE);
			}

			globalContext.setPublishDate(new Date());

			content.releaseViewNav(ctx, globalContext);
			// structure than previewNav

			// TrackingProperties prop = TrackingProperties.getInstance(request.getSession().getServletContext());
			// prop.setPublisher(editCtx.getUserPrincipal().getName());
			// prop.setPublishDate(new Date());

			I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
			String msg = i18nAccess.getText("content.published");
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));

			/*
			 * try { AbstractThread threadTest = AbstractThread.createInstance(StaticConfig.getInstance(request.getSession().getServletContext()).getThreadFolder(), ThreadTest.class); threadTest.store(); } catch (IOException e) { e.printStackTrace(); }
			 */

			performSynchro(request, response);

			
			NavigationService navigationService = NavigationService.getInstance(globalContext, request.getSession());
			navigationService.clearAllPage();

			// clean component list when publish
			ComponentFactory.cleanComponentList(request.getSession().getServletContext(), globalContext);

			/*** check url ***/
			ContentContext lgCtx = new ContentContext(ctx);
			Collection<String> lgs = globalContext.getContentLanguages();
			Collection<String> urls = new HashSet<String>();
			String dblURL = null;
			IURLFactory urlFactory = globalContext.getURLFactory(lgCtx);
			if (urlFactory != null) {
				for (String lg : lgs) {
					lgCtx.setRequestContentLanguage(lg);
					MenuElement[] children = ContentService.createContent(ctx.getRequest()).getNavigation(lgCtx).getAllChilds();
					for (MenuElement menuElement : children) {
						String url = lgCtx.getRequestContentLanguage() + urlFactory.createURL(lgCtx, menuElement);
						if (urls.contains(url)) {
							dblURL = url;
						} else {
							urls.add(url);
						}
					}
				}
			}

			if (dblURL != null) {
				msg = i18nAccess.getText("action.publish.error.same-url", new String[][] { { "url", dblURL } });
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ALERT));
			}

			content.clearComponentCache();

			// trick for PortletManager to clear view data, but should be generalized in some PublishManager
			Collection<PublishListener> listeners = (Collection<PublishListener>) request.getSession().getServletContext().getAttribute(PublishListener.class.getName());
			if (listeners != null) {
				for (PublishListener listener : listeners) {
					listener.onPublish(ctx);
				}
			}

			return message;
		}
	}

	public static String performRedo(HttpServletRequest request, HttpServletResponse response) {
		try {
			GlobalContext globalContext = GlobalContext.getInstance(request);
			PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
			persistenceService.redo();
			ContentService content = ContentService.createContent(request);
			content.releasePreviewNav();
		} catch (ServiceException e) {
			return e.getMessage();
		}
		return null;
	}

	public static String performRemove(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String message = null;
		ContentContext ctx = ContentContext.getContentContext(request, response);

		if (!checkPageSecurity(ctx)) {
			return null;
		}

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		if (!canModifyCurrentPage(ctx)) {
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}

		String id = request.getParameter("number");
		if (id != null) {

			id = id.replace(',', '#'); /* hack for resolve base64 probs */

			ClipBoard clipBoard = ClipBoard.getClibBoard(request);
			if (id.equals(clipBoard.getCopied())) {
				clipBoard.clear();
			}
			ContentService.createContent(request);
			MenuElement elem = ctx.getCurrentPage();
			String type = elem.removeContent(ctx, id);
			GlobalContext globalContext = GlobalContext.getInstance(request);
			PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
			persistenceService.store(ctx);

			if (type != null) {
				String typeName = type;
				// String typeName = i18nAccess.getText("content." + type);
				String msg = i18nAccess.getText("action.component.removed", new String[][] { { "type", typeName } });
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
			}

			modifPage(ctx);
			autoPublish(request, response);

		}
		return message;
	}

	public static String performRemovenav(HttpServletRequest request, HttpServletResponse response) throws Exception {

		ContentContext ctx = ContentContext.getContentContext(request, response);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		if (!canModifyCurrentPage(ctx)) {
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}

		String message = null;

		String path = request.getParameter("del_path");
		String id = request.getParameter("del_id");

		ContentService content = ContentService.createContent(request);
		MenuElement menuElement;
		if (path != null) {
			menuElement = content.getNavigation(ctx).searchChild(ctx, path);
		} else {
			menuElement = content.getNavigation(ctx).searchChildFromId(id);
			path = menuElement.getPath();
		}

		String newPath = menuElement.getParent().getPath();
		
		GlobalContext globalContext = GlobalContext.getInstance(request);

		if (message == null) {
			if (menuElement == null) {
				message = i18nAccess.getText("action.remove.can-not-delete");
			} else {
				synchronized (menuElement) {
					menuElement.clearVirtualParent();
				}

				NavigationService service = NavigationService.getInstance(globalContext, request.getSession());
				service.removeNavigation(ctx, menuElement);
				String msg = i18nAccess.getText("action.remove.deleted", new String[][] { { "path", path } });
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
				autoPublish(request, response);
			}
		}

		ctx.setPath(newPath);

		NavigationService navigationService = NavigationService.getInstance(globalContext, ctx.getRequest().getSession());
		navigationService.clearPage(ctx);

		return message;
	}

	public static String performRepeat(HttpServletRequest request, HttpServletResponse response) throws Exception {

		ContentContext ctx = ContentContext.getContentContext(request, response);
		if (!checkPageSecurity(ctx)) {
			return null;
		}
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		if (!canModifyCurrentPage(ctx)) {
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}

		String message = null;

		try {

			String id = request.getParameter("number");
			if (id != null) {
				ContentService content = ContentService.createContent(request);
				IContentVisualComponent comp = content.getComponent(ctx, id);
				comp.setRepeat(true);
				GlobalContext globalContext = GlobalContext.getInstance(request);
				PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
				persistenceService.store(ctx);
				// dao.updateContentRepeat(request, id, true);
			} else {
				Logger.log(Logger.WARNING, "id not found in request in Repeat action.");
			}
			String msg = i18nAccess.getText("action.repeat.confirm");
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
			autoPublish(request, response);
		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}

		return message;
	}

	public static String performReversedlink(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentService content = ContentService.createContent(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		if (!checkPageSecurity(ctx)) {
			return null;
		}
		MenuElement currentPage = ctx.getCurrentPage();
		RequestService requestService = RequestService.getInstance(request);
		String reversedLink = requestService.getParameter("reversed_link", null);
		if (reversedLink != null) {

			/* check if reverse link is ok */
			String[] lines = StringHelper.readLines(reversedLink);
			GlobalContext globalContext = GlobalContext.getInstance(request);
			ReverseLinkService reverseLinkService = ReverseLinkService.getInstance(globalContext);
			Map<String, MenuElement> reverseLink = reverseLinkService.getReversedLinkCache(content.getNavigation(ctx));
			Set<String> texts = reverseLink.keySet();
			I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
			for (String line : lines) {
				for (String text : texts) {
					if ((text.trim().length() > 0) && (line.trim().length() > 0)) {
						if (text.trim().equals(line.trim())) {
							MenuElement targetPage = reverseLink.get(text);
							if (targetPage != currentPage) {
								String[][] tags = new String[][] { { "name", line }, { "path", targetPage.getPath() } };
								String msg = i18nAccess.getText("action.reversed-link.allready-exist", tags);
								MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
								return null;
							}
						}
					}
				}
			}

			/*
			 * for (int i = 0; i < lines.length; i++) { for (int j = 0; j < lines.length; j++) { if (i != j) { if ((lines[j].trim().length() > 0) && (lines[i].trim().length() > 0)) { if (lines[i].contains(lines[j])) { String[][] tags = new String[][] { { "name1", lines[i] }, { "name2", lines[j] } }; String msg = i18nAccess.getText("action.reversed-link.allready-exist-local", tags); MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR)); return null; } } } } }
			 */

			currentPage.setReversedLink(reversedLink);
			String msg = i18nAccess.getText("action.reversed-link.added");
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
			PersistenceService.getInstance(globalContext).store(ctx);

			reverseLinkService.clearCache();
		}
		return null;
	}

	public static String performRmdir(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		StaticContext stcCtx = StaticContext.getInstance(request.getSession(), ctx.getRenderMode());
		File currentDir = getCurrentDir(request, response);
		if (!currentDir.delete()) {
			Logger.log(Logger.ERROR, "error can not delete current directory ; " + currentDir);
		} else {
			stcCtx.setCurrentPath("" + stcCtx.getCurrentPath().getParentFile());
		}

		return null;
	}

	public static String performRotateleft(HttpServletRequest request, HttpServletResponse response) {
		// String image = ContentManager.getParameterValue(request, "image",
		// null);
		// if (image != null) {
		// String realFile =
		// request.getSession().getServletContext().getRealPath(image);
		// RenderedOp img = JAI.create("fileload", realFile);
		// img = ImageHelper.rotate(img, 270);
		// JAI.create("filestore", img, realFile,
		// ImageHelper.getImageFormat(image), null);
		//
		// FileCache fc =
		// FileCache.getInstance(request.getSession().getServletContext());
		// fc.delete(image); // clear image from cache.
		// return "";
		// }
		// return "bad parameters";
		return "";
	}

	public static String performRotateright(HttpServletRequest request, HttpServletResponse response) {
		// String image = ContentManager.getParameterValue(request, "image",
		// null);
		// if (image != null) {
		// String realFile =
		// request.getSession().getServletContext().getRealPath(image);
		// RenderedOp img = JAI.create("fileload", realFile);
		// img = ImageHelper.rotate(img, 90);
		// JAI.create("filestore", img, realFile,
		// ImageHelper.getImageFormat(image), null);
		//
		// FileCache fc =
		// FileCache.getInstance(request.getSession().getServletContext());
		// fc.delete(image); // clear image from cache.
		// return "";
		// }
		// return "bad parameters";
		return "";
	}

	public static String performSavemetastaticfile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		Map<String, LocaleStaticInfo> allDescription = getStaticDescriptionList(ctx);

		Collection<String> keys = allDescription.keySet();
		for (String staticURL : keys) {
			StaticInfo staticInfo = StaticInfo.getInstance(ctx, staticURL);
			staticInfo.setDescription(ctx, allDescription.get(staticURL).getDescription());
			staticInfo.setLocation(ctx, allDescription.get(staticURL).getLocation());
			staticInfo.setDate(ctx, allDescription.get(staticURL).getDate());
			staticInfo.setLinkedPageId(ctx, allDescription.get(staticURL).getPageId());
			staticInfo.setShared(ctx, allDescription.get(staticURL).isShared());
			if (allDescription.get(staticURL).getDate() == null) {
				staticInfo.setEmptyDate(ctx);
			}
			staticInfo.setTitle(ctx, allDescription.get(staticURL).getTitle());
		}

		Map<String, String> allPosition = getStaticPositionList(request);

		keys = allDescription.keySet();
		FileCache fileCache = FileCache.getInstance(request.getSession().getServletContext());
		for (String staticURL : keys) {
			StaticInfo staticInfo = StaticInfo.getInstance(ctx, staticURL);
			if (allPosition != null && staticURL != null && allPosition.get(staticURL) != null) {
				String[] posArray = allPosition.get(staticURL).split(",");
				int newZoneX = Integer.parseInt(posArray[0]);
				int newZoneY = Integer.parseInt(posArray[1]);
				if ((newZoneX != staticInfo.getFocusZoneX(ctx)) || (newZoneY != staticInfo.getFocusZoneY(ctx))) {
					fileCache.delete(staticURL);
					staticInfo.setFocusZoneX(ctx, newZoneX);
					staticInfo.setFocusZoneY(ctx, newZoneY);
				}
			}
		}

		GlobalContext globalContext = GlobalContext.getInstance(request);
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
		MessageRepository messageRepository = MessageRepository.getInstance(ctx);
		Locale locale = new Locale(ctx.getContentLanguage());
		String msg = i18nAccess.getText("action.static.description-updated", new String[][] { { "lang", locale.getDisplayLanguage(new Locale(GlobalContext.getInstance(request).getEditLanguage())) } });
		messageRepository.setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));

		PersistenceService.getInstance(globalContext).store(ctx);
		return null;
	}

	public static String performSearchtemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);

		TemplateSearchContext tempCtx = TemplateSearchContext.getInstance(request.getSession());
		if (requestService.getParameter("mytemplate", null) != null) {
			GlobalContext globalContext = GlobalContext.getInstance(request);
			IUserFactory userFactory = UserFactory.createUserFactory(globalContext, request.getSession());
			if (userFactory.getCurrentUser(request.getSession()) != null) {
				tempCtx.setOwner(userFactory.getCurrentUser(request.getSession()).getLogin());
			}
		} else {
			tempCtx.setOwner(null);
		}
		tempCtx.setAuthors(requestService.getParameter("authors", ""));
		tempCtx.setSource(requestService.getParameter("source", ""));
		tempCtx.setDominantColor(requestService.getParameter("dominant_color", ""));
		try {
			String dateStr = requestService.getParameter("date", "");
			if (dateStr.trim().length() == 0) {
				tempCtx.setDate(null);
			} else {
				Date date = StringHelper.parseDate(dateStr);
				tempCtx.setDate(date);
			}
		} catch (ParseException e) {
			tempCtx.setDate(null);
		}
		try {
			tempCtx.setDepth(Integer.parseInt(requestService.getParameter("depth", "")));
		} catch (NumberFormatException e) {
		}
		return null;
	}

	public static String performSelectarea(HttpServletRequest request, HttpServletResponse response) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		RequestService requestService = RequestService.getInstance(request);
		String newArea = requestService.getParameter("area-select", null);
		if (newArea != null) {
			editCtx.setCurrentArea(newArea);
		}
		return null;
	}

	public static final String performShowallpreview(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String compId = request.getParameter("comp-id");
		if (compId != null) {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			PrintStream out = new PrintStream(response.getOutputStream());
			GlobalContext globalContext = GlobalContext.getInstance(request);
			ContentService content = ContentService.getInstance(globalContext);
			IContentVisualComponent comp = content.getComponent(ctx, compId);
			if (comp != null && comp instanceof IPreviewable) {
				out.print(((IPreviewable) comp).getPreviewCode(ctx, Integer.MAX_VALUE));
			}
			out.close();
		}
		return "";
	}

	public static String performSpecialaction(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String action = request.getParameter("special_action");
		logger.info("execute special action : " + action);

		if (action.equals("mailing")) {
			GlobalContext globalContext = GlobalContext.getInstance(request);
			EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
			editCtx.setCurrentView(EditContext.MAILING_VIEW);
			MailingActions.performCancel(request, response); // init
			// emailling
			// status.
			editCtx.setMailing(true);
		}

		return "";
	}

	public static String performStatselect(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		GlobalContext globalContext = GlobalContext.getInstance(request);

		String message = null;

		StatContext statCtx = StatContext.getInstance(request);
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
		ContentContext ctx = ContentContext.getContentContext(request, response);

		String mode = requestService.getParameter("selection", null);
		if (mode != null) {
			if (!statCtx.getCurrentStat().equals(mode)) {
				statCtx.setCurrentStat(mode);
				String msg = i18nAccess.getText("content.stat.new-select", new String[][] { { "stat", i18nAccess.getText("content.stat.mode." + mode) } });
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
			}
		}
		String currentMailing = requestService.getParameter("selection-mailing", null);
		if (mode != null) {
			if ((statCtx.getCurrentMailing() == null) || !statCtx.getCurrentMailing().equals(currentMailing)) {
				statCtx.setCurrentMailing(currentMailing);
				String msg = i18nAccess.getText("content.stat.new-select-mailing");
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
			}
		}
		String strFrom = requestService.getParameter("from", null);
		Date from = statCtx.getFrom();
		if (strFrom != null) {
			try {
				from = StringHelper.parseDate(strFrom);
				statCtx.setFrom(from);
			} catch (ParseException e) {
				message = i18nAccess.getText("content.stat.error.date");
			}
		}
		String strTo = requestService.getParameter("to", null);
		if (strTo != null) {
			try {
				Date to = StringHelper.parseDate(strTo);
				Calendar cal = GregorianCalendar.getInstance();
				/* set the time at the end of the day */
				cal.setTime(to);
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				statCtx.setTo(cal.getTime());

				if (to.before(from)) {
					message = i18nAccess.getText("content.stat.error.badorder_date");
				}
			} catch (ParseException e) {
				message = i18nAccess.getText("content.stat.error.date");
			}
		}

		if (requestService.getParameter("only_current_page", null) != null) {
			String path = ctx.getCurrentPage().getPath();
			statCtx.setParentPath(path);
		} else {
			statCtx.setParentPath(null);
		}

		return message;
	}

	public static String performSynchro(HttpServletRequest request, HttpServletResponse response) throws Exception {
		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession().getServletContext());
		GlobalContext globalContext = GlobalContext.getInstance(request);

		if (globalContext.getDMZServerIntra() != null) {
			SynchroThread synchro = (SynchroThread) AbstractThread.createInstance(staticConfig.getThreadFolder(), SynchroThread.class);
			synchro.initSynchronisationThread(staticConfig, globalContext, request.getSession().getServletContext());
			synchro.store();
		}
		return null;
	}

	public static String performTemplate(HttpServletRequest request, HttpServletResponse response) {
		String message = null;

		try {
			ContentContext ctx = ContentContext.getContentContext(request, response);

			RequestService requestService = RequestService.getInstance(request);

			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

			if (!canModifyCurrentPage(ctx)) {
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
				return null;
			}

			ContentService.createContent(request);
			MenuElement elem = ctx.getCurrentPage();
			String layout = requestService.getParameter("template", null);

			if (layout != null) {
				Template template = Template.getApplicationInstance(request.getSession().getServletContext(), ctx, layout, false);

				if (template.exist()) {
					elem.setTemplateName(template.getId());
				} else {
					elem.setTemplateName(null);
				}

				GlobalContext globalContext = GlobalContext.getInstance(request);
				EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
				editCtx.setMainRenderer(null);
				editCtx.setCommandRenderer(null);
				editCtx.setViewCommand(true);
				editCtx.setViewComponent(true);
				editCtx.setViewMode(true);

				PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
				persistenceService.store(ctx);
				autoPublish(request, response);
			}

		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		return message;
	}

	public static String performUndo(HttpServletRequest request, HttpServletResponse response) {
		try {
			GlobalContext globalContext = GlobalContext.getInstance(request);
			PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
			persistenceService.undo();
			ContentService content = ContentService.createContent(request);
			content.releasePreviewNav();
		} catch (ServiceException e) {
			return e.getMessage();
		}
		return null;
	}

	public static String performUnrepeat(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String message = null;

		ContentContext ctx = ContentContext.getContentContext(request, response);
		if (!checkPageSecurity(ctx)) {
			return null;
		}
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		if (!canModifyCurrentPage(ctx)) {
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}

		try {

			String id = request.getParameter("number");
			if (id != null) {
				ContentService content = ContentService.createContent(request);
				IContentVisualComponent comp = content.getComponent(ctx, id);
				comp.setRepeat(false);
				GlobalContext globalContext = GlobalContext.getInstance(request);
				PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
				persistenceService.store(ctx);
			} else {
				Logger.log(Logger.WARNING, "id not found in request in Unrepeat action.");
			}

			String msg = i18nAccess.getText("action.repeat.unconfirm");
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
			autoPublish(request, response);
		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		return message;
	}

	public static String performUpdate(HttpServletRequest request, HttpServletResponse response) {

		String message = "";

		try {

			ContentContext ctx = ContentContext.getContentContext(request, response);
			GlobalContext globalContext = GlobalContext.getInstance(request);
			I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());

			if (!canModifyCurrentPage(ctx)) {
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
				return null;
			}

			if (!checkPageSecurity(ctx)) {
				return null;
			}

			ContentService content = ContentService.createContent(ctx.getRequest());
			MenuElement currentPage = ctx.getCurrentPage();
			AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance(ctx.getRequest().getSession().getServletContext());
			IUserFactory adminUserFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());

			RequestService requestService = RequestService.getInstance(request);

			String delFile = requestService.getParameter("delfile", "");
			String delType = requestService.getParameter("deltype", "");
			String delId = requestService.getParameter("delid", "");
			String dir = requestService.getParameter("dir", "");

			if (dir.trim().length() > 0) {
				EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
				editCtx.setCurrentView(EditContext.STATIC_VIEW);
			}

			if (delFile.length() > 0) {
				if (!adminUserSecurity.haveRight(adminUserFactory.getCurrentUser(ctx.getRequest().getSession()), AdminUserSecurity.REMOVE_STATIC_ROLE)) {
					message = i18nAccess.getText("action.security.noright", new String[][] { { "action", AdminUserSecurity.REMOVE_STATIC_ROLE } });
				} else {
					ComponentBean bean = new ComponentBean();
					bean.setId(delId);
					bean.setType(delType);
					bean.setValue(delFile);
					bean.setRepeat(true);
					AbstractFileComponent fileComp = (AbstractFileComponent) ComponentFactory.CreateComponent(ctx, bean, null, null, null);
					delFile = fileComp.getFileDirectory(ctx) + '/' + delFile;
					String fileName = request.getSession().getServletContext().getRealPath(delFile);
					File f = new File(fileName);
					if (f.exists()) {
						f.delete();
					} else {
						message = "" + f + " not found.";
					}
				}
			} else {
				message = null;

				IContentComponentsList contentList = currentPage.getAllContent(ctx);

				boolean modif = false;

				boolean needRefresh = false;

				while (contentList.hasNext(ctx)) {
					IContentVisualComponent elem = contentList.next(ctx);
					if (StringHelper.isTrue(requestService.getParameter("id-" + elem.getId(), "false"))) {
						elem.performConfig(ctx);
						elem.refresh(ctx);						
					}
					needRefresh = needRefresh || elem.isNeedRefresh();

					if (elem.isModify()) {
						modif = true;
					}
					if (message == null) {
						message = elem.getErrorMessage();
					}
				}
				ctx.setNeedRefresh(needRefresh);
				if (modif) {
					modifPage(ctx);
					if (adminUserFactory.getCurrentUser(ctx.getRequest().getSession()) != null) {
						content.setAttribute(ctx, "user.update", adminUserFactory.getCurrentUser(ctx.getRequest().getSession()).getLogin());
					}
					PersistenceService.getInstance(globalContext).store(ctx);
				}
				if (message == null) {
					MessageRepository messageRepository = MessageRepository.getInstance(ctx);
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.updated"), GenericMessage.INFO));
					autoPublish(request, response);
				}

				NavigationService navigationService = NavigationService.getInstance(globalContext, ctx.getRequest().getSession());
				navigationService.clearPage(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		return message;
	}

	public static String performUpdatefraternity(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentService content = ContentService.createContent(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		if (!checkPageSecurity(ctx)) {
			return null;
		}
		MenuElement currentPage = ctx.getCurrentPage();
		RequestService requestService = RequestService.getInstance(request);
		if (requestService.getParameter("cancel", null) == null) {

			String newBrotherCode = null;

			if (requestService.getParameter("N_0", null) != null) {
				newBrotherCode = "N_0";
			}

			MenuElement[] allpages = content.getNavigation(ctx).getAllChilds();
			for (MenuElement page : allpages) {
				if (requestService.getParameter("P_" + page.getId(), null) != null) {
					newBrotherCode = "P_" + page.getId();
				} else if ((requestService.getParameter("N_" + page.getId(), null) != null)) {
					newBrotherCode = "N_" + page.getId();
				}
			}

			if (newBrotherCode != null) {
				boolean firstBrother = true;
				if (newBrotherCode.startsWith("N_")) {
					firstBrother = false;
				}
				newBrotherCode = newBrotherCode.substring(2);
				MenuElement newBrother = content.getNavigation(ctx).searchChildFromId(newBrotherCode);
				synchronized (currentPage) {
					if ((currentPage != null)) {
						if (newBrother != null) {
							String path = currentPage.getPath();

							MenuElement parent = newBrother.getParent();
							if (firstBrother) {
								parent = newBrother;
								newBrother = null;
							}
							currentPage.moveToParent(parent);

							if (newBrother != null) {
								MenuElement[] children = parent.getChildMenuElements();
								NavigationHelper.changeStepPriority(children, 10);
								currentPage.setPriority(newBrother.getPriority() + 5);
							}

							ctx.setPath(currentPage.getPath());

							GlobalContext globalContext = GlobalContext.getInstance(request); PersistenceService.getInstance(globalContext).store(ctx);
							I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
							if (newBrother != null) {
								String[][] balises = { { "path", path }, { "new-path", newBrother.getPath() } };
								String msg = i18nAccess.getText("action.link.move", balises);
								MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
							}
							// TODO : check why this message never set on
							// display
						} else {
							logger.warning("brother not found. [parentid=" + newBrother + "]");
						}
					} else {
						logger.warning("action Updatepaternity could not execute because current page not found in request.");
					}
				}
			} else {
				logger.warning("new brother id not found");
			}
			GlobalContext globalContext = GlobalContext.getInstance(request); PersistenceService.getInstance(globalContext).store(ctx);
		}

		if (requestService.getParameter("ok", null) != null) {
			GlobalContext globalContext = GlobalContext.getInstance(request);
			EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
			editCtx.setMainRenderer(null);
			editCtx.setCommandRenderer(null);
			editCtx.setViewCommand(true);
			editCtx.setViewComponent(true);
			editCtx.setViewMode(true);
		}

		return null;
	}

	public static String performUpdateone(HttpServletRequest request, HttpServletResponse response) {

		String message = "";

		try {

			ContentContext ctx = ContentContext.getContentContext(request, response);

			if (!checkPageSecurity(ctx)) {
				return null;
			}

			GlobalContext globalContext = GlobalContext.getInstance(request);
			I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());

			if (!canModifyCurrentPage(ctx)) {
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
				return null;
			}

			String delFile = ContentManager.getParameterValue(request, "delfile", "");
			String delType = ContentManager.getParameterValue(request, "deltype", "");
			String delId = ContentManager.getParameterValue(request, "delid", "");
			if (delFile.length() > 0) {
				ComponentBean bean = new ComponentBean();
				bean.setId(delId);
				bean.setType(delType);
				bean.setValue(delFile);
				bean.setRepeat(true);
				AbstractFileComponent fileComp = (AbstractFileComponent) ComponentFactory.CreateComponent(ctx, bean, null, null, null);
				delFile = fileComp.getFileDirectory(ctx) + '/' + delFile;
				String fileName = request.getSession().getServletContext().getRealPath(delFile);
				File f = new File(fileName);
				if (f.exists()) {
					f.delete();
				} else {
					message = "" + f + " not found.";
				}
			} else {
				message = null;

				ContentService content = ContentService.createContent(ctx.getRequest());

				boolean modif = false;

				String compId = ContentManager.getParameterValue(request, "component_id", null);

				IContentVisualComponent elem = content.getComponent(ctx, compId);
				elem.refresh(ctx);

				/** tool bar * */
				String newStyle = ContentManager.getParameterValue(request, "style_select_" + elem.getId(), null);
				if (newStyle != null) {
					if (!newStyle.equals(elem.getStyle(ctx))) {
						elem.setStyle(ctx, newStyle);
						modif = true;
					}
				}

				if (elem.isModify()) {
					modif = true;
					modifPage(ctx);
				}
				if (message == null) {
					message = elem.getErrorMessage();
				}

				if (modif) {
					PersistenceService.getInstance(globalContext).store(ctx);
				}
				if (message == null) {
					// message = i18nAccess.getText("action.updated");
					MessageRepository messageRepository = MessageRepository.getInstance(ctx);
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.updated"), GenericMessage.INFO));
					autoPublish(request, response);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		return message;
	}

	public static String performUpdatepaternity(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentService content = ContentService.createContent(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		if (!checkPageSecurity(ctx)) {
			return null;
		}
		MenuElement currentPage = ctx.getCurrentPage();
		RequestService requestService = RequestService.getInstance(request);
		if (requestService.getParameter("cancel", null) == null) {
			String newParentId = requestService.getParameter("parent", null);
			if (newParentId != null) {
				MenuElement newParrent = content.getNavigation(ctx).searchChildFromId(newParentId);
				synchronized (currentPage) {
					if ((currentPage != null)) {
						if (newParrent != null) {
							String path = currentPage.getPath();
							currentPage.moveToParent(newParrent);
							GlobalContext globalContext = GlobalContext.getInstance(request); PersistenceService.getInstance(globalContext).store(ctx);
							I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
							String[][] balises = { { "path", path }, { "new-path", newParrent.getPath() } };
							String msg = i18nAccess.getText("action.link.move", balises);
							MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
							// TODO : check why this message never set on
							// display
						} else {
							logger.warning("parent not found. [parentid=" + newParentId + "]");
						}
					} else {
						logger.warning("action Updatepaternity could not execute because current page not found in request.");
					}
				}
			} else {
				logger.warning("new parent id not found");
			}
			GlobalContext globalContext = GlobalContext.getInstance(request); PersistenceService.getInstance(globalContext).store(ctx);
		}

		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		editCtx.setMainRenderer(null);
		editCtx.setCommandRenderer(null);
		editCtx.setViewCommand(true);
		editCtx.setViewComponent(true);
		editCtx.setViewMode(true);

		return null;
	}

	public static String performUpdatevirtualpaternity(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentService.createContent(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		if (!checkPageSecurity(ctx)) {
			return null;
		}
		MenuElement currentPage = ctx.getCurrentPage();
		RequestService requestService = RequestService.getInstance(request);
		if (requestService.getParameter("cancel", null) == null) {
			String[] parentId = requestService.getParameterValues("parent", new String[0]);
			synchronized (currentPage) {
				currentPage.clearVirtualParent();
				for (String element : parentId) {
					currentPage.addVirtualParent(element);
				}
			}
			GlobalContext globalContext = GlobalContext.getInstance(request); PersistenceService.getInstance(globalContext).store(ctx);
		}

		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		editCtx.setMainRenderer(null);
		editCtx.setCommandRenderer(null);
		editCtx.setViewCommand(true);
		editCtx.setViewComponent(true);
		editCtx.setViewMode(true);

		return null;
	}

	public static String performUploadfile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		File currentDir = getCurrentDir(request, response);

		RequestService requestService = RequestService.getInstance(request);
		Collection<FileItem> fileItems = requestService.getAllFileItem();

		for (FileItem item : fileItems) {
			try {
				if (StringHelper.getFileExtension(item.getName()).equalsIgnoreCase("zip")) {
					ZipInputStream zipIn = new ZipInputStream(item.getInputStream());
					ZipEntry entry = zipIn.getNextEntry();
					while (entry != null) {
						String fileName = ResourceHelper.getFile(StringHelper.getFileNameFromPath(item.getName()));
						if (fileName.equalsIgnoreCase("Thumbs.db")) {
							// i don't understand why i found this file on
							// windows ???
						} else {
							String path = entry.getName().replace('\\', '/');
							String filtredName = StringHelper.createFileName(StringHelper.getFileNameFromPath(path));
							File newFile = new File(URLHelper.mergePath(currentDir.getAbsolutePath(), filtredName));
							if (!newFile.exists()) {
								newFile.getParentFile().mkdirs();
								FileOutputStream out = new FileOutputStream(newFile);
								InputStream in = zipIn;

								ResourceHelper.writeStreamToStream(in, out);

								/*
								 * int available = in.available(); byte[] buffer = new byte[available]; int read = in.read(buffer); while (read > 0) { out.write(buffer); available = in.available(); buffer = new byte[available]; read = in.read(buffer); }
								 */
								out.close();
							}
						}

						entry = zipIn.getNextEntry();
					}
					zipIn.close();
				} else {
					String filtredName = item.getName().replace('\\', '/');
					filtredName = StringHelper.getFileNameFromPath(filtredName);
					filtredName = StringHelper.createFileName(filtredName);
					File newFile = new File(URLHelper.mergePath(currentDir.getAbsolutePath(), filtredName));
					if (!newFile.exists()) {
						newFile.createNewFile();
						FileOutputStream out = new FileOutputStream(newFile);
						InputStream in = item.getInputStream();

						try {
							ResourceHelper.writeStreamToStream(in, out);
						} finally {
							ResourceHelper.closeResource(in);
							ResourceHelper.closeResource(out);
						}
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static String performUploadfilefromurl(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		String fileURL = requestService.getParameter("file-url", null);
		if (fileURL == null) {
			logger.warning("no file url defined.");
			return null;
		}
		String fileName = ResourceHelper.getFile(StringHelper.getFileNameFromPath(fileURL));
		File currentDir = getCurrentDir(request, response);
		File newFile = new File(URLHelper.mergePath(currentDir.getAbsolutePath(), StringHelper.createFileName(fileName)));
		InputStream in = null;
		try {
			URL url = new URL(fileURL);
			in = url.openStream();
			ResourceHelper.writeStreamToFile(in, newFile);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			ContentContext ctx = ContentContext.getContentContext(request, response);
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage(e.getMessage(), GenericMessage.ERROR));
		} finally {
			ResourceHelper.closeResource(in);
		}
		return null;
	}

	public static String performUploadsite(HttpServletRequest request, HttpServletResponse response) {
		String msg = null;

		RequestService requestService = RequestService.getInstance(request);
		Collection<FileItem> fileItems = requestService.getAllFileItem();

		for (FileItem item : fileItems) {
			try {
				if (StringHelper.getFileExtension(item.getName()).equalsIgnoreCase("zip")) {
					InputStream in = item.getInputStream();
					try {
						ZipManagement.uploadZipFile(request, response, in);
					} finally {
						ResourceHelper.closeResource(in);
					}
					ContentService content = ContentService.createContent(request);
					content.releasePreviewNav();
				}
				// } else if
				// (StringHelper.getFileExtension(item.getName()).equalsIgnoreCase("xhtml"))
				// {
				// ContentContext ctx =
				// ContentContext.getContentContext(request, response);
				// UploadXHTML uploadXHTML = new UploadXHTML(ctx);
				// uploadXHTML.uploadXHTML(item.getInputStream(), request,
				// response);
				// TODO: correction for PersistenceContent Use
				// }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return msg;
	}

	public static String performUploadusers(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		GlobalContext globalContext = GlobalContext.getInstance(request);

		EditContext editContext = EditContext.getInstance(globalContext, request.getSession());

		RequestService requestService = RequestService.getInstance(request);

		ContentContext ctx = ContentContext.getContentContext(request, response);

		Collection<FileItem> fileItems = requestService.getAllFileItem();

		String msg = null;
		request.getAttributeNames();
		for (FileItem item : fileItems) {
			if (item.getFieldName().trim().length() > 1) {
				try {
					boolean admin = (editContext.getCurrentView() == EditContext.ADMIN_USER_VIEW);
					IUserFactory userFact;
					if (admin) {
						userFact = AdminUserFactory.createUserFactory(globalContext, request.getSession());
					} else {
						userFact = UserFactory.createUserFactory(globalContext, request.getSession());
					}

					Charset charset = Charset.forName(ContentContext.CHARACTER_ENCODING);
					if (StringHelper.getFileExtension(item.getName()).equals("txt")) { // hack
						// for
						// excel
						// unicode
						// export
						charset = Charset.forName("utf-16");
					}

					InputStream in = item.getInputStream();
					CSVFactory csvFact;
					try {
						csvFact = new CSVFactory(in, null, charset);
					} finally {
						ResourceHelper.closeResource(in);
					}
					String[][] usersArrays = csvFact.getArray();
					if (usersArrays.length < 2) {
						I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
						msg = i18nAccess.getText("global.message.file-format-error");
						MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
						return null;
					} else {
						if (usersArrays[1].length < 5) {
							I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
							msg = i18nAccess.getText("global.message.file-format-error");
							MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
							return null;
						}
					}

					Collection<IUserInfo> userInfoList = new LinkedList<IUserInfo>();
					for (int i = 1; i < usersArrays.length; i++) {
						IUserInfo userInfo = userFact.createUserInfos();
						String[] labels = usersArrays[0];
						try {
							// USER-CHANGE
							BeanHelper.copy(JavaHelper.createMap(labels, usersArrays[i]), userInfo);
							// userInfo.setAllValues(JavaHelper.createMap(labels,
							// usersArrays[i]));
							userInfoList.add(userInfo);
						} catch (Exception e) {
							Logger.log(Logger.WARNING, "user: " + userInfo.getLogin() + " can not be insert.");
						}
					}

					if (userInfoList.size() > 0) {
						userFact.clearUserInfoList();
						for (Object element2 : userInfoList) {
							UserInfos element = (UserInfos) element2;
							try {
								userFact.addUserInfo(element);
							} catch (UserAllreadyExistException e) {
								Logger.log(Logger.WARNING, "user: " + element.getLogin() + " can not be insert.");
							}
						}
						userFact.store();
					}

				} catch (Exception e) {
					Logger.log(e);
				}

			}
		}
		return msg;
	}

	private static String performUserfilter(HttpServletRequest request, boolean admin) {
		String msg = null;
		RequestService requestService = RequestService.getInstance(request);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		UserEditFilter userFilter;
		if (admin) {
			userFilter = editCtx.getAdminUserEditFilter();
		} else {
			userFilter = editCtx.getUserEditFilter();
		}
		String loginContain = requestService.getParameter("login_contain", "");
		userFilter.addFieldContain("login", loginContain);
		String emailContain = requestService.getParameter("email_contain", "");
		userFilter.addFieldContain("email", emailContain);
		String firstnameContain = requestService.getParameter("firstName_contain", "");
		userFilter.addFieldContain("firstName", firstnameContain);
		String lastnameContain = requestService.getParameter("lastName_contain", "");
		userFilter.addFieldContain("lastName", lastnameContain);
		String rolesContain = requestService.getParameter("roles_contain", "");
		userFilter.addFieldContain("roles", rolesContain);

		return msg;
	}

	public static String performUserfilter(HttpServletRequest request, HttpServletResponse response) {
		return performUserfilter(request, false);
	}

	public static String performUserroles(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String msg = null;

		// String userRolesRaw = ContentManager.getParameterValue(request,
		// "user_roles", "");

		// String[] userRoles = userRolesRaw.split("\\" +
		// ContentManager.MULTI_PARAM_SEP);

		RequestService requestService = RequestService.getInstance(request);
		String[] userRoles = requestService.getParameterValues("user_roles", new String[0]);

		ContentService.createContent(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		if (!checkPageSecurity(ctx)) {
			return null;
		}

		MenuElement currentElement = ctx.getCurrentPage();

		currentElement.setUserRoles(userRoles);

		GlobalContext globalContext = GlobalContext.getInstance(request);
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);

		return msg;
	}

	public static String performValidallpages(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentService content = ContentService.createContent(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		AdminUserSecurity userSec = AdminUserSecurity.getInstance(request.getSession().getServletContext());
		GlobalContext globalContext = GlobalContext.getInstance(request);
		IUserFactory adminUserFactory = AdminUserFactory.createUserFactory(globalContext, request.getSession());

		if (!userSec.haveRight(adminUserFactory.getCurrentUser(request.getSession()), "admin")) {
			return null;
		}

		MenuElement rootPage = content.getNavigation(ctx);
		int validedPages = rootPage.validAllChildren();

		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
		String msg = i18nAccess.getText("portail.message.all-valided", new String[][] { { "count", "" + validedPages } });
		MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));

		PersistenceService.getInstance(globalContext).store(ctx);
		return null;
	}

	public static String performValidationpage(HttpServletRequest request, HttpServletResponse response) throws Exception {

		ContentService.createContent(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		if (!checkPageSecurity(ctx)) {
			return null;
		}
		GlobalContext globalContext = GlobalContext.getInstance(request);
		EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		MenuElement currentPage = ctx.getCurrentPage();
		AdminUserSecurity userSec = AdminUserSecurity.getInstance(request.getSession().getServletContext());
		IUserFactory adminUserFactory = AdminUserFactory.createUserFactory(globalContext, request.getSession());

		if (!userSec.haveRight(adminUserFactory.getCurrentUser(request.getSession()), "admin")) {
			return null;
		}

		if (!currentPage.isValid()) {
			currentPage.setValid(true);
			currentPage.setValidater(editCtx.getUserPrincipal().getName());
		} else {
			currentPage.setValid(false);
		}
		PersistenceService.getInstance(globalContext).store(ctx);
		return null;
	}

	public static String performVisible(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String message = null;

		try {

			ContentContext ctx = ContentContext.getContentContext(request, response);
			if (!checkPageSecurity(ctx)) {
				return null;
			}

			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

			if (!canModifyCurrentPage(ctx)) {
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
				return null;
			}

			String path = ctx.getPath();
			String visible = request.getParameter("visible");

			if (!visible.matches("yes|no")) {
				message = i18nAccess.getText("action.visible.validation");
			} else {
				MenuElement elem = ctx.getCurrentPage();
				elem.setVisible(StringHelper.isTrue(visible));
				GlobalContext globalContext = GlobalContext.getInstance(request);
				PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
				persistenceService.store(ctx);
				if (visible.equals("yes")) {
					String msg = i18nAccess.getText("action.visible.confirm-yes", new String[][] { { "path", path } });
					MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
				} else {
					String msg = i18nAccess.getText("action.visible.confirm-no", new String[][] { { "path", path } });
					MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		autoPublish(request, response);
		return message;
	}

	private static Collection<MenuElement> searchStaticURIInContent(ContentContext ctx, String uri, MenuElement page) throws Exception {

		Collection<MenuElement> pageList = new LinkedList<MenuElement>();

		boolean outFound = false;
		ContentElementList contentList = page.getContent(ctx);

		while (contentList.hasNext(ctx)) {
			IContentVisualComponent comp = contentList.next(ctx);
			if (comp instanceof IStaticContainer) {
				IStaticContainer staticContainer = (IStaticContainer) comp;
				if (staticContainer.contains(ctx, uri)) {
					pageList.add(page);
				}
			}
			/*
			 * if (comp.getValue().indexOf(token) > -1) { return true; }
			 */
		}
		MenuElement[] children = page.getChildMenuElements();
		for (int i = 0; ((i < children.length) && (!outFound)); i++) {
			pageList.addAll(searchStaticURIInContent(ctx, uri, children[i]));
		}
		return pageList;
	}

	public static String validNodeName(String name, I18nAccess i18nAccess) {
		String res = null;
		if (name.length() == 0) {
			res = i18nAccess.getText("action.validation.name-not-empty");
		} else if (!name.matches("([a-z]|[A-Z]|[0-9]|_|-)*")) {
			res = i18nAccess.getText("action.validation.name-syntax");
		}
		return res;
	}

}
