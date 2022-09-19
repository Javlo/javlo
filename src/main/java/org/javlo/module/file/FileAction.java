package org.javlo.module.file;

import java.awt.image.BufferedImage;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.bean.LinkToRenderer;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.InfoBean;
import org.javlo.filter.DirectoryFilter;
import org.javlo.helper.ExifHelper;
import org.javlo.helper.LangHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.PDFHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.image.ImageEngine;
import org.javlo.io.TransactionFile;
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
import org.javlo.ztatic.ResourceFactory;
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

	public static File getFolder(ContentContext ctx) throws FileNotFoundException, InstantiationException, IllegalAccessException, IOException, ModuleException {
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
		ctx.getRequest().setAttribute("canUpload", AdminUserSecurity.isCurrentUserCanUpload(ctx));
		ctx.getRequest().setAttribute("backImage", URLHelper.createStaticResourceURL(ctx, "/images/mimetypes/folder-empty.svg"));
		
		/*
		 * File importFolder = new
		 * File(URLHelper.mergePath(globalContext.getStaticFolder(),
		 * ctx.getGlobalContext().getStaticConfig().getImportResourceFolder(),
		 * DataAction.createImportFolder(ctx.getCurrentPage()))); if
		 * (importFolder.exists()) {
		 * ctx.getRequest().setAttribute("importFolder",
		 * URLHelper.mergePath(globalContext.getStaticConfig().getStaticFolder()
		 * , ctx.getGlobalContext().getStaticConfig().getImportResourceFolder(),
		 * DataAction.createImportFolder(ctx.getCurrentPage()))); }
		 */

		String editFileName = ctx.getRequest().getParameter("editFile");
		if (ctx.getRequest().getParameter("path") != null) {
			fileModuleContext.setPath(ctx.getRequest().getParameter("path"));
		}
		updateBreadCrumb(RequestService.getInstance(ctx.getRequest()), ctx, EditContext.getInstance(globalContext, ctx.getRequest().getSession()), modulesContext, modulesContext.getCurrentModule(), fileModuleContext, false, null);
		if (editFileName != null) {
			if (StringHelper.isImage(editFileName)) {
				updateBreadCrumb(RequestService.getInstance(ctx.getRequest()), ctx, EditContext.getInstance(globalContext, ctx.getRequest().getSession()), modulesContext, modulesContext.getCurrentModule(), fileModuleContext, false, editFileName);
				modulesContext.getCurrentModule().setToolsRenderer(null);
				File editFile = new File(URLHelper.mergePath(getFolder(ctx).getAbsolutePath(), editFileName));
				ctx.getRequest().setAttribute("editFile", editFileName);
				if (editFile.exists()) {
					ctx.getRequest().setAttribute("fileFound", true);
					StaticInfo staticInfo = StaticInfo.getInstance(ctx, editFile);
					ctx.getRequest().setAttribute("imageURL", staticInfo.getURL(ctx));
				} else {
					logger.warning("file not found : " + editFile);
					ctx.getRequest().setAttribute("fileFound", false);
				}				
				ctx.getRequest().setAttribute("jpeg", StringHelper.isJpeg(editFileName));
				modulesContext.getCurrentModule().setRenderer("/jsp/image_editor.jsp");
			} else {
				modulesContext.getCurrentModule().setToolsRenderer(null);
				File editFile = new File(URLHelper.mergePath(getFolder(ctx).getAbsolutePath(), editFileName));
				ctx.getRequest().setAttribute("editFile", editFileName);
				if (editFile.exists()) {
					ctx.getRequest().setAttribute("fileFound", true);
					ctx.getRequest().setAttribute("fileExt", StringHelper.getFileExtension(editFile.getName()));
					String content = ResourceHelper.loadStringFromFile(editFile);
					ctx.getRequest().setAttribute("content", content);
				} else {
					ctx.getRequest().setAttribute("fileFound", false);
					logger.warning("file not found : " + editFile);
				}
				modulesContext.getCurrentModule().setRenderer("/jsp/editor.jsp");
			}
		} else {
			if (modulesContext.getCurrentModule().getRenderer().endsWith("editor.jsp")) {
				getModuleContext(ctx.getRequest().getSession(), modulesContext.getCurrentModule()).setCurrentLink(null);
				getModuleContext(ctx.getRequest().getSession(), modulesContext.getCurrentModule()).setRenderer(null);
				modulesContext.getCurrentModule().setRenderer(null);
				modulesContext.getCurrentModule().restoreToolsRenderer();
				modulesContext.getCurrentModule().restoreAll();
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
					 * modulesContext.getCurrentModule().setToolsRenderer(null);
					 * } else {
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
		}
		ctx.getRequest().setAttribute("metaReadOnly", !ResourceHelper.canModifFolder(ctx, getFolder(ctx).getAbsolutePath()));
		return msg;
	}

	public String performBrowse(HttpServletRequest request, Module currentModule) {
		request.setAttribute("changeRoot", "true");
		return null;
	}
	
	public String performUpdateBreadCrumb(RequestService rs, ContentContext ctx, EditContext editContext, ModulesContext moduleContext, Module currentModule, FileModuleContext fileModuleContext) throws Exception {
		return updateBreadCrumb(rs, ctx, editContext, moduleContext, currentModule, fileModuleContext, false, null);
	}
	
	private String updateBreadCrumb(RequestService rs, ContentContext ctx, EditContext editContext, ModulesContext moduleContext, Module currentModule, FileModuleContext fileModuleContext, boolean readonly, String finalFile) throws Exception {

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
//				if (i<2 && rs.getParameter("select") != null) {
//					readonly=true;
//				}
				currentModule.pushBreadcrumb(new HtmlLink(staticURL, path, path, i == pathItems.length - 1, childrenLinks, readonly));
			}
		}
		
		if (finalFile != null) {
			currentModule.pushBreadcrumb(new HtmlLink(currentPath+'/'+finalFile, finalFile, finalFile, readonly));
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
		if (!canModifyFile(ctx, folder)) {
			return "securtiy error.";
		}
		boolean found = false;
		String latestFileName = "";
		if (folder.exists()) {
			for (File file : folder.listFiles((FileFilter) FileFileFilter.FILE)) {
				latestFileName = file.getAbsolutePath();
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
				return "focus technical error - file not found : "+latestFileName;
			}
			return null;
		} else {
			return "folder not found : " + folder;
		}
	}

	public String performUpdateMeta(RequestService rs, ServletContext application, ContentContext ctx, EditContext editContext, GlobalContext globalContext, FileModuleContext fileModuleContext, I18nAccess i18nAccess, MessageRepository messageRepository) throws Exception {
		File folder = getFolder(ctx);
		if (!ResourceHelper.canModifFolder(ctx, getFolder(ctx).getAbsolutePath())) {
			return "security error : you have not suffisant right to modify this file.";
		}
		if (!canModifyFile(ctx, folder)) {
			return "securtiy error.";
		}
		if (folder.exists()) {
			ResourceFactory resourceFactory = ResourceFactory.getInstance(ctx);
			for (File file : folder.listFiles()) {
				StaticInfo staticInfo = StaticInfo.getInstance(ctx, file);
				FileBean fileBean = new FileBean(ctx, staticInfo);
				String fileName = rs.getParameter("rename-" + fileBean.getId(), null);
				if (fileName != null && !fileName.equals(file.getName())) {
					File targetFile = new File(URLHelper.mergePath(staticInfo.getFile().getParentFile().getAbsolutePath(), fileName));
					ResourceHelper.renameResource(ctx, staticInfo.getFile(), targetFile);
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
				
				String[] taxonomy = rs.getParameterValues("taxonomy-" + fileBean.getId(), null);
				if (taxonomy != null) {
					staticInfo.setTaxonomy(ctx, new HashSet<String>(Arrays.asList(taxonomy)));
				}
				String authors = rs.getParameter("authors-" + fileBean.getId(), null);
				if (authors != null) {
					staticInfo.setAuthors(ctx, authors);
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

				String ref = rs.getParameter("ref-" + fileBean.getId(), null);
				String lg = rs.getParameter("lg-" + fileBean.getId(), null);
				if (ref != null) {
					StaticInfo.ReferenceBean refBean = new StaticInfo.ReferenceBean(ref, lg);
					if (resourceFactory.getStaticInfo(ctx, refBean) != null && !resourceFactory.getStaticInfo(ctx, refBean).getId(ctx).equals(staticInfo.getId(ctx))) {
						messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("file.message.error.reference-found") + ref, GenericMessage.ERROR));
					} else {
						if (StringHelper.isEmpty(lg) && !StringHelper.isEmpty(ref)) {
							messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("file.message.error.reference-nolang"), GenericMessage.ERROR));
						} else {
							if (!StringHelper.isEmpty(lg) && !globalContext.getContentLanguages().contains(lg)) {
								messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("file.message.error.undefined-lang"), GenericMessage.ALERT));
							}
							staticInfo.setReference(ctx, ref);
						}
					}
				}
				if (!StringHelper.isEmpty(lg)) {
					if (lg.length() != 2 || !StringHelper.isAlpha(lg)) {
						messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("file.message.error.bad-lg-format"), GenericMessage.ERROR));
					} else {
						staticInfo.setLanguage(ctx, lg);
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
				resourceFactory.update(ctx, staticInfo);
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

	public static String performClose(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		if (StringHelper.isTrue(rs.getParameter("close", null))) {
			ctx.setClosePopup(true);
		}
		return null;
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

		if (!canModifyFile(ctx, null)) {
			return "securtiy error.";
		}

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

	public static String performDelete(GlobalContext globalContext, RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String filePath = rs.getParameter("file", null);
		if (filePath == null) {
			return "bad request structure : need file parameter.";
		} else {
			File file = new File(URLHelper.mergePath(globalContext.getStaticFolder(), filePath));
			if (!canModifyFile(ctx, file)) {
				return "securtiy error.";
			}
			ResourceHelper.deleteResource(ctx, file);			
		}
		if (StringHelper.isTrue(rs.getParameter("close", null))) {
			ctx.setClosePopup(true);
		}
		return null;
	}
	
	public static String performJpeg(GlobalContext globalContext, RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String filePath = rs.getParameter("file", null);
		if (filePath == null) {
			return "bad request structure : need file parameter.";
		} else {
			File file = new File(URLHelper.mergePath(globalContext.getStaticFolder(), filePath));
			if (!file.exists()) {
				return "file not found : "+file;
			}
			if (!canModifyFile(ctx, file)) {
				return "securtiy error.";
			}
			BufferedImage image;
			if (!StringHelper.isPDF(file.getName())) {
				image = ImageIO.read(file);
			} else {
				image = PDFHelper.getPDFImage(file, 1);
			}
			File jpeg = new File(StringHelper.getFileNameWithoutExtension(file.getAbsolutePath())+".jpg");			
			jpeg = ResourceHelper.getFreeFileName(jpeg);
			ImageIO.write(image, "jpg", jpeg);
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
		ctx.getRequest().setAttribute("metaReadOnly", !ResourceHelper.canModifFolder(ctx, file.getParentFile().getAbsolutePath()));
		return null;
	}

	public static String performCreatefilestructure(RequestService rs, ContentContext ctx, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		File file = new File(URLHelper.mergePath(globalContext.getStaticFolder(), "file-structure", StringHelper.createFileName("structure-" + StringHelper.renderSortableTime(new Date()) + ".html")));
		if (!canModifyFile(ctx, file)) {
			return "securtiy error.";
		}
		file.getParentFile().mkdirs();
		file.createNewFile();
		ResourceHelper.writeStringToFile(file, ResourceHelper.fileStructureToHtml(new File(globalContext.getStaticConfig().getAllDataFolder())));
		return null;
	}

	public static String performModify(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws FileNotFoundException, InstantiationException, IllegalAccessException, IOException, ModuleException {
		File file = new File(URLHelper.mergePath(getFolder(ctx).getAbsolutePath(), rs.getParameter("file", "-- param file undefined --")));
		if (!canModifyFile(ctx, file)) {
			return "securtiy error.";
		}
		String content = rs.getParameter("content", null);
		if (content == null) {
			return "File content not found.";
		}
		if (!file.exists()) {
			return "File not found : " + file;
		} else {
			ResourceHelper.writeStringToFile(file, content);
		}
		return null;
	}

	private static boolean canModifyFile(ContentContext ctx, File file) {
		return AdminUserSecurity.getInstance().canRole(ctx.getCurrentEditUser(), AdminUserSecurity.CONTENT_ROLE);
	}

	public static String performEditimage(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		if (rs.getParameter("cancel", null) != null) {
			return null;
		}
		
		File file = new File(URLHelper.mergePath(getFolder(ctx).getAbsolutePath(), rs.getParameter("file", "-- param file undefined --")));
		if (!canModifyFile(ctx, file)) {
			return "securtiy error.";
		}
		if (!file.exists() || !file.isFile()) {
			return "file not found : " + file;
		}
		
		if (rs.getParameter("duplicate", null) != null) {
			File newFile = ResourceHelper.getFreeFileName(file);
			ResourceHelper.writeFileToFile(file, newFile);
			Map<String,String> params = new HashMap<String, String>();
			params.put("editFile", newFile.getName());
			if (rs.getParameter("previewEdit") != null) {
				params.put("previewEdit", rs.getParameter("previewEdit"));
			}
			if (rs.getParameter("__back") != null) {
				params.put("__back", rs.getParameter("__back"));
			}
			if (rs.getParameter("preview-edit-content") != null) {
				params.put("preview-edit-content", rs.getParameter("preview-edit-content"));
			}
			if (rs.getParameter("comp_id") != null) {
				params.put("comp_id", rs.getParameter("comp_id"));
			}
			NetHelper.sendRedirectTemporarily(ctx.getResponse(), URLHelper.createURL(ctx, params));			
		} else {
			StaticInfo staticInfo = StaticInfo.getInstance(ctx, file);
			ImageMetadata md = ExifHelper.readMetadata(file);
			BufferedImage image = ImageIO.read(file);
			boolean transform = false;
			if (StringHelper.isTrue(rs.getParameter("flip", null))) {
				image = ImageEngine.flip(image, false);
				if (staticInfo.getFocusZoneX(ctx) != StaticInfo.DEFAULT_FOCUS_X) {
					staticInfo.setFocusZoneX(ctx, 2 * StaticInfo.DEFAULT_FOCUS_X - staticInfo.getFocusZoneX(ctx));
				}
				transform = true;
			} else {
				int rotate = Integer.parseInt(rs.getParameter("rotate", null));
				if (rotate > 0) {
					transform = true;
					image = ImageEngine.rotate(image, rotate, null);
				}
			}
			int cropTop = Integer.parseInt(rs.getParameter("crop-top", null));
			int cropLeft = Integer.parseInt(rs.getParameter("crop-left", null));
			int cropWidth = Integer.parseInt(rs.getParameter("crop-width", null));
			int cropHeight = Integer.parseInt(rs.getParameter("crop-height", null));
			final int REFERENCE_SIZE = 10000;
			if (cropTop > 0 || cropLeft > 0 || cropWidth < REFERENCE_SIZE || cropHeight < REFERENCE_SIZE) {
				transform = true;
				int width = Math.round(cropWidth * image.getWidth() / REFERENCE_SIZE);
				int height = Math.round(cropHeight * image.getHeight() / REFERENCE_SIZE);
				int x = Math.round(cropLeft * image.getWidth() / REFERENCE_SIZE);
				int y = Math.round(cropTop * image.getHeight() / REFERENCE_SIZE);
				image = ImageEngine.cropImage(image, width, height, x, y);				
				staticInfo.resetImageSize(ctx);
			}
			if (transform) {
				logger.info("transform : " + file);
				FileCache.getInstance(ctx.getRequest().getSession().getServletContext()).deleteAllFile(ctx.getGlobalContext().getContextKey(), file.getName());
				TransactionFile transactionFile = new TransactionFile(file);
				staticInfo.resetCRC32();
				try {
					ImageIO.write(image, StringHelper.getFileExtension(file.getName().toLowerCase()), transactionFile.getTempFile());
					transactionFile.commit();
					ExifHelper.writeMetadata(md, file);
				} catch (Exception e) {
					transactionFile.rollback();
					throw e;
				}
			}
		}
		return null;
	}

}

	