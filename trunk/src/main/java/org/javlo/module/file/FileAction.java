package org.javlo.module.file;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.filefilter.FileFileFilter;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.bean.LinkToRenderer;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.filter.DirectoryFilter;
import org.javlo.helper.LangHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.module.core.Module;
import org.javlo.module.core.Module.Box;
import org.javlo.module.core.Module.HtmlLink;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.ztatic.FileCache;
import org.javlo.ztatic.StaticInfo;

public class FileAction extends AbstractModuleAction {

	private static Logger logger = Logger.getLogger(FileAction.class.getName());

	public static class FileBean {
		ContentContext ctx;
		StaticInfo staticInfo;
		Map<String, String> tags;

		public FileBean(ContentContext ctx, File file) {
			this.ctx = ctx;
			try {
				this.staticInfo = StaticInfo.getInstance(ctx, file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public FileBean(ContentContext ctx, StaticInfo staticInfo) {
			this.ctx = ctx;
			this.staticInfo = staticInfo;
		}

		public String getURL() {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			return URLHelper.createResourceURL(ctx, '/' + globalContext.getStaticConfig().getStaticFolder() + staticInfo.getStaticURL());
		}

		public boolean isImage() {
			return StringHelper.isImage(getName());
		}

		public String getType() {
			return getManType().replace('/', '_');
		}

		public String getThumbURL() throws Exception {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			return URLHelper.createTransformURL(ctx, globalContext.getStaticConfig().getStaticFolder() + staticInfo.getStaticURL(), "list") + "?ts=" + staticInfo.getFile().lastModified();
		}

		public StaticInfo getStaticInfo() {
			return staticInfo;
		}

		public String getName() {
			return staticInfo.getFile().getName();
		}

		public String getDescription() {
			return staticInfo.getDescription(ctx);
		}

		public String getLocation() {
			return staticInfo.getLocation(ctx);
		}

		public String getDate() {
			return StringHelper.renderTime(staticInfo.getDate(ctx));
		}

		public String getManualDate() {
			return StringHelper.renderTime(staticInfo.getManualDate(ctx));
		}

		public String getTitle() {
			return staticInfo.getTitle(ctx);
		}

		public String getId() {
			return getName().replace('.', '_');
		}

		public int getFocusZoneX() {
			return staticInfo.getFocusZoneX(ctx);
		}

		public int getFocusZoneY() {
			return staticInfo.getFocusZoneY(ctx);
		}

		public String getSize() {
			return StringHelper.renderSize(staticInfo.getFile().length());
		}

		public String getManType() {
			return ResourceHelper.getFileExtensionToManType(StringHelper.getFileExtension(getName()));
		}

		public Map<String, String> getTags() {
			if (tags == null) {
				tags = new HashMap<String, String>();
				for (String tag : staticInfo.getTags(ctx)) {
					tags.put(tag, tag);
				}
			}
			return tags;
		}

		public boolean isShared() {
			return staticInfo.isShared(ctx);
		}

	}

	@Override
	public String getActionGroupName() {
		return "file";
	}

	@Override
	public AbstractModuleContext getModuleContext(HttpSession session, Module module) throws Exception {
		return FileModuleContext.getInstance(session, GlobalContext.getSessionInstance(session), module, FileModuleContext.class);
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String msg = super.prepare(ctx, modulesContext);
		FileModuleContext fileModuleContext = (FileModuleContext) LangHelper.smartInstance(ctx.getRequest(), ctx.getResponse(), FileModuleContext.class);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		ctx.getRequest().setAttribute("currentModule", modulesContext.getCurrentModule());
		ctx.getRequest().setAttribute("tags", globalContext.getTags());

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
				if (ctx.getRequest().getParameter("name") == null) {
					modulesContext.getCurrentModule().setToolsRenderer(null);
				} else {
					modulesContext.getCurrentModule().setToolsRenderer("/jsp/actions.jsp");
				}
			}
		}

		if (fileModuleContext.getCurrentLink().equals(FileModuleContext.PAGE_META)) {
			modulesContext.getCurrentModule().setToolsRenderer("/jsp/actions.jsp");
			modulesContext.getCurrentModule().clearAllBoxes();
			File folder = new File(URLHelper.mergePath(globalContext.getDataFolder(), fileModuleContext.getPath()));
			if (folder.exists()) {
				Collection<FileBean> allFileInfo = new LinkedList<FileBean>();
				for (File file : folder.listFiles((FileFilter) FileFileFilter.FILE)) {
					allFileInfo.add(new FileBean(ctx, StaticInfo.getInstance(ctx, file)));
				}
				ctx.getRequest().setAttribute("files", allFileInfo);
			} else {
				logger.warning("folder not found : " + folder);
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

		String[] pathItems = URLHelper.cleanPath(fileModuleContext.getPath(), true).split("/");
		String currentPath = "/";
		for (int i = 0; i < pathItems.length; i++) {
			String path = pathItems[i];
			if (path.trim().length() > 0) {
				currentPath = currentPath + path + '/';

				Map<String, String> filesParams = new HashMap<String, String>();
				filesParams.put("path", currentPath);
				if (rs.getParameter("changeRoot", null) != null) {
					filesParams.put("changeRoot", "true");
				}
				String staticURL;
				if (moduleContext.getFromModule() != null) {
					staticURL = URLHelper.createInterModuleURL(ctx, ctx.getPath(), FileModuleContext.MODULE_NAME, moduleContext.getFromModule().getName(), filesParams);
				} else {
					staticURL = URLHelper.createModuleURL(ctx, ctx.getPath(), FileModuleContext.MODULE_NAME, filesParams);
				}

				// search children
				GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
				File currentDir = new File(URLHelper.mergePath(globalContext.getDataFolder(), currentPath));
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
		String path = rs.getParameter("image_path", fileModuleContext.getPath());
		File folder = new File(URLHelper.mergePath(globalContext.getDataFolder(), path));
		if (folder.exists()) {
			for (File file : folder.listFiles((FileFilter) FileFileFilter.FILE)) {
				StaticInfo staticInfo = StaticInfo.getInstance(ctx, file);
				FileBean fileBean = new FileBean(ctx, staticInfo);

				String newFocusX = rs.getParameter("posx-" + fileBean.getId(), null);
				String newFocusY = rs.getParameter("posy-" + fileBean.getId(), null);

				if (newFocusX != null && newFocusY != null) {
					staticInfo.setFocusZoneX(ctx, (int) Math.round(Double.parseDouble(newFocusX)));
					staticInfo.setFocusZoneY(ctx, (int) Math.round(Double.parseDouble(newFocusY)));
					PersistenceService.getInstance(globalContext).store(ctx);
					messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("file.message.updatefocus", new String[][] { { "file", file.getName() } }), GenericMessage.INFO));

					FileCache fileCache = FileCache.getInstance(ctx.getRequest().getSession().getServletContext());
					fileCache.delete(file.getName());
				}
			}
			return null;
		} else {
			return "folder not found : " + folder;
		}
	}

	public String performUpdateMeta(RequestService rs, ContentContext ctx, GlobalContext globalContext, FileModuleContext fileModuleContext, I18nAccess i18nAccess, MessageRepository messageRepository) throws Exception {
		File folder = new File(URLHelper.mergePath(globalContext.getDataFolder(), fileModuleContext.getPath()));
		if (folder.exists()) {
			for (File file : folder.listFiles((FileFilter) FileFileFilter.FILE)) {
				StaticInfo staticInfo = StaticInfo.getInstance(ctx, file);
				FileBean fileBean = new FileBean(ctx, staticInfo);

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
				boolean shared = rs.getParameter("shared-" + fileBean.getId(), null) != null;
				staticInfo.setShared(ctx, shared);

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
			}
			PersistenceService.getInstance(globalContext).store(ctx);
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("file.message.updatemeta"), GenericMessage.INFO));
		} else {
			return "folder not found : " + folder;
		}
		return null;
	}
}
