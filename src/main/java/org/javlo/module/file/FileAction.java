package org.javlo.module.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.bean.LinkToRenderer;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.InfoBean;
import org.javlo.filter.DirectoryFilter;
import org.javlo.helper.LangHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.content.Edit;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.module.core.Module;
import org.javlo.module.core.Module.Box;
import org.javlo.module.core.Module.HtmlLink;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.syncro.SynchroHelper;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;
import org.javlo.ztatic.FileCache;
import org.javlo.ztatic.StaticInfo;

public class FileAction extends AbstractModuleAction {

	private static Logger logger = Logger.getLogger(FileAction.class.getName());

	@Override
	public String getActionGroupName() {
		return "file";
	}

	@Override
	public AbstractModuleContext getModuleContext(HttpSession session, Module module) throws Exception {
		return FileModuleContext.getInstance(session, GlobalContext.getSessionInstance(session), module, FileModuleContext.class);
	}

	public File getFolder(ContentContext ctx) throws FileNotFoundException, InstantiationException, IllegalAccessException, IOException, ModuleException {
		String sourceFolder = getContextROOTFolder(ctx);
		FileModuleContext fileModuleContext = FileModuleContext.getInstance(ctx.getRequest());
		File folder = new File(sourceFolder, fileModuleContext.getPath());
		return folder;
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {

		String msg = super.prepare(ctx, modulesContext);
		FileModuleContext fileModuleContext = (FileModuleContext) LangHelper.smartInstance(ctx.getRequest(), ctx.getResponse(), FileModuleContext.class);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		ctx.getRequest().setAttribute("currentModule", modulesContext.getCurrentModule());
		ctx.getRequest().setAttribute("tags", globalContext.getTags());
		ctx.getRequest().setAttribute("readRoles", globalContext.getUserRoles());
		ctx.getRequest().setAttribute("pathPrefix", getROOTPath(ctx));
		ctx.getRequest().setAttribute("sort", fileModuleContext.getSort());

		if (ctx.getRequest().getParameter("path") != null) {
			fileModuleContext.setPath(ctx.getRequest().getParameter("path"));
			performUpdateBreadCrumb(RequestService.getInstance(ctx.getRequest()), ctx, EditContext.getInstance(globalContext, ctx.getRequest().getSession()), modulesContext, modulesContext.getCurrentModule(), fileModuleContext);
		}

		if (modulesContext.getFromModule() == null && ctx.getRequest().getParameter("changeRoot") == null) {
			Box box = modulesContext.getCurrentModule().getBox("filemanager");
			if (box != null) {
				box.restoreTitle();
			}
			fileModuleContext.loadNavigation();
		} else {
			if (fileModuleContext.getTitle() != null) {
				modulesContext.getCurrentModule().restoreAll();
				fileModuleContext.getNavigation().clear();
				LinkToRenderer lnk = fileModuleContext.getHomeLink();
				fileModuleContext.getNavigation().add(lnk);
				fileModuleContext.setCurrentLink(lnk.getName());
				/*
				 * if (ctx.getRequest().getParameter("name") == null) {
				 * modulesContext.getCurrentModule().setToolsRenderer(null); }
				 * else {
				 */
				modulesContext.getCurrentModule().setToolsRenderer("/jsp/actions.jsp");
				/* } */
			}
		}

		if (fileModuleContext.getCurrentLink().equals(FileModuleContext.PAGE_META)) {
			if (ctx.getRequest().getAttribute("files") == null) {
				modulesContext.getCurrentModule().setToolsRenderer("/jsp/actions.jsp");
				modulesContext.getCurrentModule().clearAllBoxes();
				File folder = getFolder(ctx);
				if (folder.exists() && folder.listFiles(new DirectoryFilter()) != null) {
					List<FileBean> allFileInfo = new LinkedList<FileBean>();
					for (File file : folder.listFiles(new DirectoryFilter())) {
						allFileInfo.add(new FileBean(ctx, StaticInfo.getInstance(ctx, file)));
					}
					List<FileBean> fileList = new LinkedList<FileBean>();
					for (File file : folder.listFiles((FileFilter) FileFileFilter.FILE)) {
						fileList.add(new FileBean(ctx, StaticInfo.getInstance(ctx, file)));
					}
					Collections.sort(fileList, new FileBean.FileBeanComparator(ctx, fileModuleContext.getSort()));
					Collections.sort(allFileInfo, new FileBean.FileBeanComparator(ctx, fileModuleContext.getSort()));
					allFileInfo.addAll(fileList);
					ctx.getRequest().setAttribute("files", allFileInfo);
				} else {
					logger.warning("folder not found : " + folder);
				}
			} else {
				modulesContext.getCurrentModule().setToolsRenderer(null);
			}
		} else {
			if (modulesContext.getCurrentModule().getToolsRenderer() != null && modulesContext.getFromModule() == null) {
				modulesContext.getCurrentModule().restoreAll();
			}
		}

		return msg;
	}

	public String performBrowse(HttpServletRequest request, Module currentModule) {
		request.setAttribute("changeRoot", "true");
		return null;
	}

	public String performUpdateBreadCrumb(RequestService rs, ContentContext ctx, EditContext editContext, ModulesContext moduleContext, Module currentModule, FileModuleContext fileModuleContext) throws Exception {

		currentModule.clearBreadcrump();
		currentModule.setBreadcrumbTitle("");

		ctx.setRenderMode(ContentContext.EDIT_MODE); // ajax can preview mode by
														// default

		String[] tmpPathItems = URLHelper.cleanPath(fileModuleContext.getPath(), true).split("/");
		String[] pathItems = new String[tmpPathItems.length + 1];
		for (int i = 0; i < tmpPathItems.length; i++) {
			pathItems[i + 1] = tmpPathItems[i];
		}
		pathItems[0] = "/";

		String currentPath = "/";

		for (int i = 0; i < pathItems.length; i++) {
			String path = pathItems[i];
			if (path.trim().length() > 0) {
				currentPath = URLHelper.mergePath(currentPath, path, "/");

				Map<String, String> filesParams = new HashMap<String, String>();
				filesParams.put("path", currentPath);
				if (rs.getParameter("changeRoot", null) != null) {
					filesParams.put("changeRoot", "true");
				}
				if (rs.getParameter("name", null) != null) {
					filesParams.put("name", rs.getParameter("name", null));
				}
				String staticURL;
				if (moduleContext.getFromModule() != null) {
					staticURL = URLHelper.createInterModuleURL(ctx, ctx.getPath(), FileModuleContext.MODULE_NAME, moduleContext.getFromModule().getName(), filesParams);
				} else {
					staticURL = URLHelper.createModuleURL(ctx, ctx.getPath(), FileModuleContext.MODULE_NAME, filesParams);
				}

				// search children
				File currentDir = new File(URLHelper.mergePath(getContextROOTFolder(ctx), currentPath));
				File[] children = currentDir.listFiles(new DirectoryFilter());
				List<HtmlLink> childrenLinks = new LinkedList<Module.HtmlLink>();
				if (children != null) {
					for (File file : children) {
						String childPath = URLHelper.mergePath(currentPath, file.getName());
						String childURL;
						filesParams = new HashMap<String, String>();
						filesParams.put("path", childPath);
						if (rs.getParameter("changeRoot", null) != null) {
							filesParams.put("changeRoot", "true");
						}
						if (rs.getParameter("name", null) != null) {
							filesParams.put("name", rs.getParameter("name", null));
						}
						if (moduleContext.getFromModule() != null) {
							childURL = URLHelper.createInterModuleURL(ctx, ctx.getPath(), FileModuleContext.MODULE_NAME, moduleContext.getFromModule().getName(), filesParams);
						} else {
							childURL = URLHelper.createModuleURL(ctx, ctx.getPath(), FileModuleContext.MODULE_NAME, filesParams);
						}
						childrenLinks.add(new HtmlLink(childURL, file.getName(), file.getName()));
					}
				}
				Collections.sort(childrenLinks, new HtmlLink.SortOnLegend());
				currentModule.pushBreadcrumb(new HtmlLink(staticURL, path, path, i == pathItems.length - 1, childrenLinks));
			}
		}

		String componentRenderer = editContext.getBreadcrumbsTemplate();
		ctx.getRequest().setAttribute("currentModule", currentModule);
		String breadcrumbsHTML = ServletHelper.executeJSP(ctx, componentRenderer);

		ctx.getAjaxInsideZone().put("breadcrumbs", breadcrumbsHTML);
		return null;
	}

	public String performUpdateFocus(RequestService rs, ContentContext ctx, GlobalContext globalContext, FileModuleContext fileModuleContext, I18nAccess i18nAccess, MessageRepository messageRepository) throws Exception {
		File folder = getFolder(ctx);
		if (rs.getParameter("image_path", null) != null) {
			folder = new File(globalContext.getDataFolder(), rs.getParameter("image_path", null));
		}
		boolean found = false;
		if (folder.exists()) {
			for (File file : folder.listFiles((FileFilter) FileFileFilter.FILE)) {
				StaticInfo staticInfo = StaticInfo.getInstance(ctx, file);
				FileBean fileBean = new FileBean(ctx, staticInfo);

				String newFocusX = rs.getParameter("posx-" + fileBean.getId(), null);
				String newFocusY = rs.getParameter("posy-" + fileBean.getId(), null);

				if (newFocusX != null && newFocusY != null) {
					found = true;
					staticInfo.setFocusZoneX(ctx, (int) Math.round(Double.parseDouble(newFocusX)));
					staticInfo.setFocusZoneY(ctx, (int) Math.round(Double.parseDouble(newFocusY)));
					PersistenceService.getInstance(globalContext).setAskStore(true);
					// messageRepository.setGlobalMessageAndNotification(ctx,
					// new
					// GenericMessage(i18nAccess.getText("file.message.updatefocus",
					// new String[][] { { "file", file.getName() } }),
					// GenericMessage.INFO));

					FileCache fileCache = FileCache.getInstance(ctx.getRequest().getSession().getServletContext());
					fileCache.delete(ctx, file.getName());
				}
			}
			if (!found) {
				return "focus technical error : file not found.";
			}
			return null;
		} else {
			return "folder not found : " + folder;
		}
	}

	public String performUpdateMeta(RequestService rs, ServletContext application, ContentContext ctx, EditContext editContext, GlobalContext globalContext, FileModuleContext fileModuleContext, I18nAccess i18nAccess, MessageRepository messageRepository) throws Exception {
		File folder = getFolder(ctx);
		if (folder.exists()) {
			for (File file : folder.listFiles()) {
				StaticInfo staticInfo = StaticInfo.getInstance(ctx, file);
				FileBean fileBean = new FileBean(ctx, staticInfo);				
				String fileName = rs.getParameter("rename-"+fileBean.getId(), null);				
				if (fileName != null && !fileName.equals(file.getName())) {
					File targetFile = new File(URLHelper.mergePath(staticInfo.getFile().getParentFile().getAbsolutePath(), fileName));
					ResourceHelper.renameResource(ctx,staticInfo.getFile(),targetFile);
					staticInfo.getFile().renameTo(targetFile);
					staticInfo.renameFile(ctx, targetFile);					
					PersistenceService.getInstance(globalContext).setAskStore(true);
					if (staticInfo.getFile().isDirectory()) {
						FileCache.getInstance(application).clear(globalContext.getContextKey());
					} else {
						FileCache.getInstance(application).deleteAllFile(globalContext.getContextKey(), staticInfo.getFile().getName());
					}
				}				
				String title = rs.getParameter("title-" + fileBean.getId(), null);
				if (title != null) {
					staticInfo.setTitle(ctx, title);
				}
				String description = rs.getParameter("description-" + fileBean.getId(), null);
				if (description != null) {
					staticInfo.setDescription(ctx, description);
				}
				String location = rs.getParameter("location-" + fileBean.getId(), null);
				if (location != null) {
					staticInfo.setLocation(ctx, location);
				}
				String copyright = rs.getParameter("copyright-" + fileBean.getId(), null);
				if (copyright != null) {
					staticInfo.setCopyright(ctx, copyright);
				}
				if (!globalContext.isMailingPlatform()) {
					boolean shared = rs.getParameter("shared-" + fileBean.getId(), null) != null;
					if (title != null) {
						staticInfo.setShared(ctx, shared);
					}
				}
				String date = rs.getParameter("date-" + fileBean.getId(), null);
				if (date != null) {
					if (date.trim().length() == 0) {
						staticInfo.setDate(ctx, null);
					} else {
						try {
							staticInfo.setDate(ctx, StringHelper.parseTime(date));
						} catch (Exception e) {
							try {
								staticInfo.setDate(ctx, StringHelper.parseDate(date));
							} catch (Exception e1) {
								messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("file.message.error.date-format"), GenericMessage.ERROR));
							}
						}
					}
				}

				/* tags */
				Collection<String> tags = globalContext.getTags();
				for (String tag : tags) {
					if (rs.getParameter("tag_" + tag + '_' + fileBean.getId(), null) != null) {
						staticInfo.addTag(ctx, tag);
					} else {
						staticInfo.removeTag(ctx, tag);
					}
				}

				/* roles */
				Collection<String> roles = globalContext.getUserRoles();
				for (String role : roles) {
					if (rs.getParameter("readrole_" + role + '_' + fileBean.getId(), null) != null) {
						staticInfo.addReadRole(ctx, role);
					} else {
						staticInfo.removeReadRole(ctx, role);
					}
				}

				if (StringHelper.isTrue(rs.getParameter("close", null))) {
					ctx.setClosePopup(true);
				}
			}
			PersistenceService.getInstance(globalContext).setAskStore(true);
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("file.message.updatemeta"), GenericMessage.INFO));
		} else {
			return "folder not found : " + folder;
		}
		return null;
	}

	public static String getROOTPath(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (AdminUserSecurity.getInstance().isGod(ctx.getCurrentEditUser())) {
			return URLHelper.mergePath("/", globalContext.getFolder());
		} else {
			return "/";
		}

	}

	/**
	 * get the prefix of url resource.
	 * 
	 * @param ctx
	 * @return
	 */
	public static String getURLPathPrefix(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		User user = AdminUserFactory.createAdminUserFactory(globalContext, ctx.getRequest().getSession()).getCurrentUser(ctx.getRequest().getSession());
		if (!AdminUserSecurity.getInstance().isGod(user)) {
			return URLHelper.mergePath("/", globalContext.getStaticConfig().getStaticFolder(), "/");
		} else {
			return "/";
		}

	}

	/**
	 * get the prefix of path to browse resource.
	 * 
	 * @param ctx
	 * @return
	 */
	public static String getPathPrefix(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		User user = AdminUserFactory.createAdminUserFactory(globalContext, ctx.getRequest().getSession()).getCurrentUser(ctx.getRequest().getSession());
		if (AdminUserSecurity.getInstance().isGod(user)) {
			return URLHelper.mergePath("/", globalContext.getStaticConfig().getStaticFolder(), "/");
		} else {
			return "/";
		}

	}

	public static String getContextROOTFolder(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (AdminUserSecurity.getInstance().isGod(ctx.getCurrentEditUser())) {
			return globalContext.getDataFolder();
		} else {
			return URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getStaticFolder());
		}
	}

	public static String performUpload(ContentContext ctx, RequestService rs) throws FileNotFoundException, InstantiationException, IllegalAccessException, IOException, ModuleException {
		String sourceFolder = getContextROOTFolder(ctx);

		FileModuleContext fileModuleContext = FileModuleContext.getInstance(ctx.getRequest());
		String folderName = StringHelper.createFileName(rs.getParameter("folder", "").trim());
		File folder = new File(sourceFolder, fileModuleContext.getPath());
		if (folderName.length() > 0) {
			folder = new File(sourceFolder, URLHelper.mergePath(fileModuleContext.getPath(), folderName));
			if (!folder.exists()) {
				folder.mkdir();
			}
		}

		for (FileItem file : rs.getAllFileItem()) {
			if (file.getName().trim().length() > 0) {
				File newFile = new File(URLHelper.mergePath(folder.getAbsolutePath(), StringHelper.createFileName(file.getName())));
				newFile = ResourceHelper.getFreeFileName(newFile);
				InputStream in = file.getInputStream();
				try {
					ResourceHelper.writeStreamToFile(in, newFile);
				} finally {
					ResourceHelper.closeResource(in);
				}
				fileModuleContext.setSort(4);
				ctx.setNeedRefresh(true);
			}
		}

		String urlStr = rs.getParameter("url", "");
		if (urlStr.trim().length() > 0) {
			URL url = new URL(urlStr);
			InputStream in = url.openConnection().getInputStream();
			try {
				File newFile = new File(URLHelper.mergePath(folder.getAbsolutePath(), StringHelper.createFileName(StringHelper.getFileNameFromPath(urlStr))));
				newFile = ResourceHelper.getFreeFileName(newFile);
				ResourceHelper.writeStreamToFile(in, newFile);
			} finally {
				ResourceHelper.closeResource(in);
			}
		}

		return null;
	}

	public static String performDelete(GlobalContext globalContext, RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		String filePath = rs.getParameter("file", null);
		if (filePath == null) {
			return "bad request structure : need file parameter.";
		} else {
			File file = new File(URLHelper.mergePath(globalContext.getStaticFolder(), filePath));
			if (file.isFile()) {
				file.delete();
				if (StringHelper.isImage(file.getName())) {
					FileCache.getInstance(ctx.getRequest().getSession().getServletContext()).deleteAllFile(globalContext.getContextKey(), file.getName());
				}
			} else if (file.isDirectory()) {
				for (File child : file.listFiles()) {
					if (StringHelper.isImage(child.getName())) {
						FileCache.getInstance(ctx.getRequest().getSession().getServletContext()).deleteAllFile(globalContext.getContextKey(), child.getName());
					}
				}
				FileUtils.deleteDirectory(file);
			}
		}
		if (StringHelper.isTrue(rs.getParameter("close", null))) {
			ctx.setClosePopup(true);
		}
		return null;
	}

	public static String performOrder(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String order = rs.getParameter("order", null);
		if (order != null) {
			int sortNum = Integer.parseInt(order);
			if (sortNum == 1 || sortNum == 2 || sortNum == 3 || sortNum == 4) {
				FileModuleContext fileModuleContext = (FileModuleContext) LangHelper.smartInstance(ctx.getRequest(), ctx.getResponse(), FileModuleContext.class);
				fileModuleContext.setSort(sortNum);
			}
		} else {
			return "bad request structure : need 'sort' param.";
		}
		return null;
	}

	public static String performSynchro(ContentContext ctx) throws Exception {
		SynchroHelper.performSynchro(ctx);
		return null;
	}

	public static final String performChangeLanguage(RequestService requestService, ContentContext ctx, GlobalContext globalContext, I18nAccess i18nAccess, MessageRepository messageRepository) throws IOException {
		return Edit.performChangeLanguage(requestService, ctx, globalContext, i18nAccess, messageRepository);
	}

	public static final String performPreviewedit(HttpServletRequest request, ContentContext ctx, RequestService rs, EditContext editCtx) throws Exception {
		String path = URLHelper.decodePathForAttribute(request.getParameter("file"));
		File file = new File(path);
		if (!file.exists()) {
			return "file not found : " + file;
		}
		FileModuleContext.getInstance(ctx.getRequest());
		String sourceFolder = URLHelper.cleanPath(getContextROOTFolder(ctx), false);
		String parentPath = URLHelper.cleanPath(file.getParentFile().getCanonicalPath().replaceFirst(sourceFolder, ""), false);
		FileModuleContext.getInstance(ctx.getRequest()).setPath(parentPath.replace(sourceFolder, ""));
		FileBean fileBean = new FileBean(ctx, StaticInfo.getInstance(ctx, file));
		request.setAttribute("files", Arrays.asList(new FileBean[] { fileBean }));
		InfoBean.getCurrentInfoBean(ctx).setFakeCurrentURL(request.getParameter("currentURL"));
		ctx.getRequest().setAttribute("specialEditRenderer", "/modules/file/jsp/meta.jsp?one=true");
		return null;
	}

	public static String performCreatefilestructure(RequestService rs, ContentContext ctx, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		File file = new File(URLHelper.mergePath(globalContext.getStaticFolder(), "file-structure", StringHelper.createFileName("structure-" + StringHelper.renderSortableTime(new Date()) + ".html")));
		file.getParentFile().mkdirs();
		System.out.println("***** FileAction.performCreatefilestructure : file = " + file); // TODO:
																							// remove
																							// debug
																							// trace
		file.createNewFile();
		ResourceHelper.writeStringToFile(file, ResourceHelper.fileStructureToHtml(new File(globalContext.getStaticConfig().getAllDataFolder())));
		return null;
	}

}
