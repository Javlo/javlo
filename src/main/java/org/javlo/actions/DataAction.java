package org.javlo.actions;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.IUploadResource;
import org.javlo.component.files.ArrayFileComponent;
import org.javlo.component.files.FileFinder;
import org.javlo.component.files.GenericFile;
import org.javlo.component.files.Sound;
import org.javlo.component.files.VFSFile;
import org.javlo.component.image.GlobalImage;
import org.javlo.component.image.Image;
import org.javlo.component.multimedia.Multimedia;
import org.javlo.component.multimedia.PDFMultimedia;
import org.javlo.component.title.Title;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.ContentHelper;
import org.javlo.helper.JavaScriptBlob;
import org.javlo.helper.LangHelper;
import org.javlo.helper.NavigationHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.importation.ImportConfigBean;
import org.javlo.helper.importation.TanukiImportTools;
import org.javlo.i18n.I18nAccess;
import org.javlo.image.ImageEngine;
import org.javlo.image.ImageHelper;
import org.javlo.macro.interactive.ImportJCRPageMacro;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.content.Edit;
import org.javlo.module.ticket.TicketAction;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.PageAssociationBean;
import org.javlo.service.ContentService;
import org.javlo.service.CountService;
import org.javlo.service.IMService;
import org.javlo.service.IMService.IMItem;
import org.javlo.service.NotificationService;
import org.javlo.service.NotificationService.Notification;
import org.javlo.service.NotificationService.NotificationContainer;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.location.LocationService;
import org.javlo.service.shared.CloserJavloSharedContentProvider;
import org.javlo.service.shared.ISharedContentProvider;
import org.javlo.service.shared.ImportedFileSharedContentProvider;
import org.javlo.service.shared.ImportedImageSharedContentProvider;
import org.javlo.service.shared.SharedContentContext;
import org.javlo.service.shared.SharedContentService;
import org.javlo.servlet.IVersion;
import org.javlo.template.Template;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;
import org.javlo.utils.MemoryBean;
import org.javlo.ztatic.StaticInfo;

public class DataAction implements IAction {

	private static Logger logger = Logger.getLogger(DataAction.class.getName());

	public static final String SYNCHRO_CODE_PARAM = "synchrocode";

	@Override
	public String getActionGroupName() {
		return "data";
	}

	public static String performNotificationsAsRead(RequestService rs, ContentContext ctx, GlobalContext globalContext, NotificationService notif, User user, HttpSession session) throws ParseException {
		if (user != null) {
			notif.markAllAsRead(user.getLogin());
		}
		return null;
	}

	/**
	 * get the list of modification. option for request : markread=true, mark all
	 * notification returned as read. this method need user logger.
	 * 
	 * @return
	 * @throws ParseException
	 */
	public static String performNotifications(RequestService rs, ContentContext ctx, GlobalContext globalContext, NotificationService notif, User user, HttpSession session) throws ParseException {
		if (user != null) {
			Date lastDate = null;
			if (rs.getParameter("lastdate", null) != null) {
				lastDate = StringHelper.parseFileTime(rs.getParameter("lastdate", null));
			}
			List<NotificationContainer> finalNotifs = new LinkedList<NotificationService.NotificationContainer>();
			AdminUserSecurity userSecurity = AdminUserSecurity.getInstance();
			if (lastDate != null) {
				Calendar startCal = Calendar.getInstance();
				startCal.setTime(lastDate);

				List<NotificationContainer> notifs = notif.getNotifications(user.getLogin(), userSecurity.isAdmin(ctx.getCurrentEditUser()), 999, StringHelper.isTrue(rs.getParameter("markread", null)));
				Calendar cal = Calendar.getInstance();
				for (NotificationContainer notificationContainer : notifs) {
					cal.setTime(notificationContainer.getNotification().getCreationDate());
					if (cal.after(startCal)) {
						finalNotifs.add(notificationContainer);
					}
				}
			} else {
				finalNotifs.addAll(notif.getNotifications(user.getLogin(), userSecurity.isAdmin(ctx.getCurrentEditUser()), 999, StringHelper.isTrue(rs.getParameter("markread", null))));
			}
			IMService imService = IMService.getInstance(session);
			String currentSite = globalContext.getContextKey();
			String currentUser = ctx.getCurrentUserId();
			List<IMItem> messages = new LinkedList<IMItem>();
			imService.fillMessageList(currentSite, currentUser, lastDate, messages);
			if (!messages.isEmpty()) {
				ContentContext absoluteCtx = ctx.getContextForAbsoluteURL();
				absoluteCtx.setRenderMode(ContentContext.EDIT_MODE);
				for (IMItem item : messages) {
					String msg = item.getFromUser();
					if (!item.getReceiverUser().equals(IMService.ALL_USERS)) {
						msg += " > " + item.getReceiverUser();
					}
					msg += ": " + item.getMessage();
					Notification notification = new Notification();
					notification.setCreationDate(item.getSentDate());
					notification.setMessage(msg);
					notification.setReceiver(item.getReceiverUser());
					notification.setUserId(item.getFromUser());
					notification.setType(GenericMessage.INFO);
					notification.setUrl(URLHelper.createModuleURL(absoluteCtx, "", "communication", LangHelper.objStr(LangHelper.entry("webaction", "changeRenderer"), LangHelper.entry("page", currentSite))));
					finalNotifs.add(new NotificationContainer(notification, false, currentUser));
				}
			}
			ctx.getAjaxData().put("notifications", finalNotifs);
		} else {
			return "no access";
		}
		return null;
	}

	public static String performToken(ContentContext ctx, User user) {
		if (user != null) {
			ctx.getAjaxData().put("token", user.getUserInfo().getToken());
		} else {
			return "no access";
		}
		return null;
	}

	public static String performDate(ContentContext ctx, User user) {
		ctx.getAjaxData().put("date", StringHelper.renderFileTime(new Date()));
		return null;
	}

	public static String performServerInfo(HttpServletRequest request, ContentContext ctx, RequestService requestService, GlobalContext globalContext, StaticConfig staticConfig, HttpServletResponse response) {

		String clientSynchroCode = requestService.getParameter(SYNCHRO_CODE_PARAM, null);

		Map<String, Object> serverInfo = new LinkedHashMap<String, Object>();
		serverInfo.put("version", IVersion.VERSION);

		long now = System.currentTimeMillis();
		// //TODO remove trace
		// System.out.println("======================= Process data.serverInfo
		// webaction: clientSynchroCode=" + clientSynchroCode
		// + ", now=" + now
		// + ", staticConfig.getSynchroCode()=" + staticConfig.getSynchroCode()
		// + ", staticConfig.getSynchroTokenValidityMinutes()=" +
		// staticConfig.getSynchroTokenValidityMinutes());
		if (clientSynchroCode == null) {
			logger.warning("no synchro code sent to webaction data.serverInfo");
			serverInfo.put("message", "No synchro code!");
		} else if (!StringHelper.timedTokenValidate(clientSynchroCode, staticConfig.getSynchroCode(), staticConfig.getSynchroTokenValidityMinutes(), now)) {
			logger.warning("bad synchro code sent to webaction data.serverInfo");
			serverInfo.put("message", "Synchro code not valid!");
		} else {
			BeanHelper.extractPropertiesAsString(serverInfo, request, "remotePort", "remoteHost", "remoteAddr", "localAddr", "localName", "localPort", "serverName", "serverPort", "characterEncoding"
			// , "contextPath"
			);
			serverInfo.put("contextKey", ctx.getGlobalContext().getContextKey());
			serverInfo.put("systemUser", System.getProperty("user.name"));

			serverInfo.put("lastPublishDate", globalContext.getPublishDateLabel());
			serverInfo.put("lastPublisher", globalContext.getLatestPublisher());

			CountService countService = CountService.getInstance(ctx.getRequest().getSession().getServletContext());
			serverInfo.put("countServiceCount", "" + countService.getCount());
			serverInfo.put("countServiceAverage", "" + countService.getAverage());

			serverInfo.put("systemUser", System.getProperty("user.name"));

			serverInfo.put("serverCharge", CountService.getInstance(request.getSession().getServletContext()).getCount());
			serverInfo.put("siteCharge", globalContext.getCount());

			List<String> connectedUsers = new LinkedList<String>();
			List<Principal> list = globalContext.getAllPrincipals();
			for (Principal user : list) {
				connectedUsers.add(user.getName());
			}
			serverInfo.put("connectedUsers", connectedUsers);

			Map<String, Object> headersOut = new LinkedHashMap<String, Object>();
			@SuppressWarnings("unchecked")
			Enumeration<String> headers = request.getHeaderNames();
			while (headers.hasMoreElements()) {
				String headerName = headers.nextElement();
				@SuppressWarnings("unchecked")
				Enumeration<String> values = request.getHeaders(headerName);
				List<String> valuesOut = new LinkedList<String>();
				while (values.hasMoreElements()) {
					String headerValue = values.nextElement();
					valuesOut.add(headerValue);
				}
				headersOut.put(headerName, valuesOut);
			}
			ctx.getAjaxData().put("requestHeaders", headersOut);
		}
		ctx.getAjaxData().put("serverInfo", serverInfo);
		return null;
	}

	public static String performOneTimeToken(ContentContext ctx, User user) {
		if (user != null) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			String token = globalContext.createOneTimeToken(user.getUserInfo().getToken());
			ctx.getAjaxData().put("token", token);
		} else {
			return "no access";
		}
		return null;
	}

	public static void updateAreaAjax(ContentContext ctx, String area) throws Exception {
		if (area == null) {
			return;
		}
		Collection<String> areas = new LinkedList<String>();
		Map<String, String> areaMap = ctx.getCurrentTemplate().getAreasMap();
		for (Map.Entry<String, String> areaId : areaMap.entrySet()) {
			if (area == null || areaId.getValue().equals(area)) {
				areas.add(areaId.getKey());
			}
		}
		for (String areaKey : areas) {
			ctx.getAjaxInsideZone().put(areaMap.get(areaKey), ServletHelper.executeJSP(ctx, "/jsp/view/content_view.jsp?area=" + areaKey));
		}
	}

	public static String performUpdateArea(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		String area = rs.getParameter("area", null);
		Collection<String> areas = new LinkedList<String>();

		Map<String, String> areaMap = ctx.getCurrentTemplate().getAreasMap();
		for (Map.Entry<String, String> areaId : areaMap.entrySet()) {
			if (area == null || areaId.getValue().equals(area)) {
				areas.add(areaId.getKey());
			}
		}

		String mode = rs.getParameter("render-mode", null);

		if (mode != null) {
			ctx.setRenderMode(Integer.parseInt(mode));
		}

		ctx.getRequest().removeAttribute("specific-comp");

		for (String areaKey : areas) {
			ctx.getAjaxInsideZone().put(areaMap.get(areaKey), ServletHelper.executeJSP(ctx, "/jsp/view/content_view.jsp?area=" + areaKey));
		}

		return null;
	}

	public static String performUpdateAllArea(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		Collection<String> areas = new LinkedList<String>();
		Map<String, String> areaMap = ctx.getCurrentTemplate().getAreasMap();
		for (Map.Entry<String, String> areaId : areaMap.entrySet()) {
			areas.add(areaId.getKey());
		}

		String mode = rs.getParameter("render-mode", null);

		if (mode != null) {
			ctx.setRenderMode(Integer.parseInt(mode));
		}

		ctx.getRequest().removeAttribute("specific-comp");

		for (String areaKey : areas) {
			ctx.getAjaxInsideZone().put(areaMap.get(areaKey), ServletHelper.executeJSP(ctx, "/jsp/view/content_view.jsp?area=" + areaKey));
		}

		return null;
	}

	public static String performTickets(ContentContext ctx) throws Exception {
		ctx.getAjaxData().put("tickets", TicketAction.getMyTicket(ctx));
		return null;
	}

	public static String performUpload(RequestService rs, ContentContext ctx, GlobalContext gc, ContentService cs, User user, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		boolean rename = StringHelper.isTrue(rs.getParameter("rename", null), true);
		if (!rename) {
			ctx.setNeedRefresh(true);
		}
		String msg = uploadContent(rs, ctx, gc, cs, user, messageRepository, i18nAccess, new ImportConfigBean(ctx), rename);
		updateAreaAjax(ctx, rs.getParameter("area"));
		return msg;
	}

	public static String performUploadShared(RequestService rs, ContentContext ctx, GlobalContext gc, ContentService cs, User user, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		SharedContentService sharedContentService = SharedContentService.getInstance(ctx);
		SharedContentContext sharedContentContext = SharedContentContext.getInstance(ctx);
		ISharedContentProvider provider = sharedContentService.getProvider(ctx, sharedContentContext.getProvider());
		boolean rename = StringHelper.isTrue(rs.getParameter("rename", null), true);
		if (!AdminUserSecurity.isCurrentUserCanUpload(ctx) && !sharedContentContext.getProvider().equals(ImportedImageSharedContentProvider.NAME)) {
			return "you have no right to upload file here.";
		}
		if (!rename) {
			ctx.setNeedRefresh(true);
		}
		if (provider != null) {
			for (FileItem item : rs.getAllFileItem()) {
				InputStream in = item.getInputStream();
				provider.upload(ctx, StringHelper.createFileName(item.getName()), in, sharedContentContext.getCategory(), rename);
				ResourceHelper.closeResource(in);
			}
		}
		Edit.updatePreviewCommands(ctx, null);
		return null;
	}

	/**
	 * upload image and return the local file.
	 * 
	 * @param ctx
	 * @param importFolder
	 * @param imageItem
	 * @param config
	 * @return the local file
	 * @throws Exception
	 */
	protected static File createImage(ContentContext ctx, String importFolder, FileItem imageItem, ImportConfigBean config, boolean content, String previousId, boolean rename) throws Exception {
		GlobalContext gc = ctx.getGlobalContext();
		String imageRelativeFolder = URLHelper.mergePath(gc.getStaticConfig().getStaticFolder(), ctx.getGlobalContext().getStaticConfig().getImportImageFolder(), importFolder);
		File targetFolder = new File(URLHelper.mergePath(gc.getDataFolder(), imageRelativeFolder));
		if (!targetFolder.exists()) {
			targetFolder.mkdirs();
		}
		File newFile = ResourceHelper.writeFileItemToFolder(imageItem, targetFolder, true, rename);
		if (newFile != null && newFile.exists()) {
			ContentService cs = ContentService.getInstance(gc);
			String dir = imageRelativeFolder.replaceFirst(gc.getStaticConfig().getImageFolder(), "");
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			out.println("dir=" + dir);
			out.println("file-name=" + StringHelper.getFileNameFromPath(newFile.getName()));
			if (newFile.getName() != null && newFile.getName().toLowerCase().endsWith(".gif")) {
				out.println(GlobalImage.IMAGE_FILTER + "=" + GlobalImage.RAW_FILTER);
			} else {
				if (ctx.getArea().contains("header") && ctx.getCurrentTemplate().getImageFilters().contains("banner")) {
					out.println(GlobalImage.IMAGE_FILTER + "=" + "banner");
				} else {
					out.println(GlobalImage.IMAGE_FILTER + "=" + ctx.getCurrentTemplate().getDefaultImageFilter());
				}
			}
			out.close();
			if (config.isCreateContentOnImportImage() || content) {
				ComponentBean image = new ComponentBean(GlobalImage.TYPE, new String(outStream.toByteArray()), ctx.getRequestContentLanguage());
				image.setStyle(Image.STYLE_CENTER);
				if (!content) {
					Collection<IContentVisualComponent> titles = ctx.getCurrentPage().getContentByType(ctx, Title.TYPE);
					if (titles.size() > 0) {
						previousId = titles.iterator().next().getId();
					}
					image.setArea(config.getArea());
					if (config.isBeforeContent()) {
						cs.createContent(ctx.getContextWithArea(config.getArea()), image, previousId, true);
					} else {
						cs.createContentAtEnd(ctx.getContextWithArea(config.getArea()), image, true);
					}
				} else {
					image.setArea(ctx.getArea());
					cs.createContent(ctx, image, previousId, true);
				}
			}
		}
		return newFile;
	}

	/**
	 * create image and return the local folder.
	 * 
	 * @param ctx
	 * @param targetFolder
	 * @param importFolder
	 * @param imageItem
	 * @param config
	 * @return
	 * @throws Exception
	 */
	protected static File createOrUpdateGallery(ContentContext ctx, File targetFolder, String importFolder, Collection<FileItem> imageItem, ImportConfigBean config, boolean content, String previousId) throws Exception {
		boolean galleryFound = false;
		GlobalContext globalContext = ctx.getGlobalContext();
		ContentService cs = ContentService.getInstance(globalContext);
		String galleryRelativeFolder = null;

		boolean pdf = false;
		String fileName = null;
		if (imageItem.size() > 0 && StringHelper.isPDF(imageItem.iterator().next().getName())) {
			pdf = true;
			fileName = StringHelper.createFileName(imageItem.iterator().next().getName());
		}

		String baseGalleryFolder = ctx.getGlobalContext().getStaticConfig().getImportGalleryFolder();
		if (!config.isCreateContentOnImportImage() && !content) {
			baseGalleryFolder = ctx.getGlobalContext().getStaticConfig().getImportImageFolder();
		}

		Multimedia compGalleryFound = null;
		Collection<IContentVisualComponent> mediaComps;
		if (pdf) {
			mediaComps = ctx.getCurrentPage().getContentByType(ctx, PDFMultimedia.TYPE);
		} else {
			mediaComps = ctx.getCurrentPage().getContentByType(ctx, Multimedia.TYPE);
		}
		if (mediaComps.size() > 0) {
			for (IContentVisualComponent comp : mediaComps) {
				Multimedia multimedia = (Multimedia) comp;
				if (multimedia.getCurrentRootFolder().length() > baseGalleryFolder.length() || content) {
					galleryRelativeFolder = URLHelper.mergePath(globalContext.getStaticConfig().getStaticFolder(), multimedia.getCurrentRootFolder());
					galleryFound = true;
					ctx.setNeedRefresh(true);
					if (content) {
						if (multimedia.getId().equals(previousId)) {
							compGalleryFound = multimedia;
							if (pdf) {
								((PDFMultimedia) compGalleryFound).setCurrentRootFolder(ctx, URLHelper.createStaticFolderURI(ctx, targetFolder) + "/" + fileName);
							}
							break;
						}
					} else {
						compGalleryFound = multimedia;
						if (pdf) {
							((PDFMultimedia) compGalleryFound).setCurrentRootFolder(ctx, URLHelper.createStaticFolderURI(ctx, targetFolder) + "/" + fileName);
						}
						break;
					}
				}
			}
		}

		if (galleryRelativeFolder == null) {
			String galFolder = importFolder;
			if (galFolder.trim().length() == 0) {
				galFolder = "images";
			}
			galleryRelativeFolder = URLHelper.mergePath(globalContext.getStaticConfig().getStaticFolder(), baseGalleryFolder, importFolder);
		}
		targetFolder = new File(URLHelper.mergePath(globalContext.getDataFolder(), galleryRelativeFolder));
		if (compGalleryFound != null) {
			targetFolder = new File(URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getStaticFolder(), compGalleryFound.getCurrentRootFolder()));
		}

		if (!targetFolder.exists()) {
			targetFolder.mkdirs();
		}
		for (FileItem item : imageItem) {
			if (StringHelper.isImage(item.getName())) {
				if (ResourceHelper.writeFileItemToFolder(item, targetFolder, false, true) == null) {
					logger.warning("Could'nt upload : " + item.getName());
				}
			}
		}

		if (!galleryFound && (config.isCreateContentOnImportImage() || content)) {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			out.println(Multimedia.PAGE_SIZE + "=" + 128);
			out.println(Multimedia.MAX_LIST_SIZE + "=" + 99);
			out.println(Multimedia.ROOT_FOLDER + "=" + galleryRelativeFolder.replaceFirst(globalContext.getStaticConfig().getStaticFolder(), ""));
			out.close();
			ComponentBean multimedia = new ComponentBean(Multimedia.TYPE, new String(outStream.toByteArray()), ctx.getRequestContentLanguage());
			// ComponentBean multimedia = new ComponentBean(Multimedia.TYPE, "--12,128-" +
			// galleryRelativeFolder.replaceFirst(globalContext.getStaticConfig().getStaticFolder(),
			// "") + "---", ctx.getRequestContentLanguage());
			multimedia.setStyle(Multimedia.IMAGE);

			Collection<IContentVisualComponent> titles = ctx.getCurrentPage().getContentByType(ctx, Title.TYPE);
			if (!content && titles.size() > 0) {
				previousId = titles.iterator().next().getId();
			}
			if (!content) {
				multimedia.setArea(config.getArea());
				if (config.isBeforeContent()) {
					cs.createContent(ctx.getContextWithArea(config.getArea()), multimedia, previousId, true);
				} else {
					cs.createContentAtEnd(ctx.getContextWithArea(config.getArea()), multimedia, true);
				}
			} else {
				multimedia.setArea(ctx.getArea());
				cs.createContent(ctx, multimedia, previousId, true);
			}

			ctx.setNeedRefresh(true);
		}

		if (ctx.isNeedRefresh()) { // if there are modification >>>
									// store new contnet.
			PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
		}

		return targetFolder;
	}

	public static final String createImportFolder(String pageName) throws Exception {
		String importFolder = StringHelper.createFileName(pageName);
		importFolder = StringHelper.trimOn(importFolder.trim(), "_");
		importFolder = importFolder.replace('-', '_');
		return importFolder;
	}

	public static final String createImportFolder(MenuElement inPage) throws Exception {
		MenuElement page = inPage;
		if (inPage.isChildrenAssociation()) {
			page = inPage.getRootOfChildrenAssociation();
		}
		if (page == null) {
			page = inPage;
		}
		return createImportFolder(page.getName());
	}

	public static String uploadContent(RequestService rs, ContentContext ctx, GlobalContext gc, ContentService cs, User user, MessageRepository messageRepository, I18nAccess i18nAccess, ImportConfigBean config, boolean rename) throws Exception {
		if (user == null) {
			return "Please, login before upload files.";
		}

		if (!Edit.checkPageSecurity(ctx)) {
			return "no suffisant right.";
		}
		String importFolder = createImportFolder(ctx.getCurrentPage());

		int countImages = 0;
		FileItem imageItem = null;
		String msg = null;
		try {
			String previousId = rs.getParameter("previous", "0");

			IContentVisualComponent comp = cs.getCachedComponent(ctx, previousId);
			if (comp != null && comp instanceof IUploadResource && ((IUploadResource) comp).isUploadOnDrop()) {
				msg = ((IUploadResource) comp).performUpload(ctx);
				ctx.setNeedRefresh(true);
			} else {
				boolean content = StringHelper.isTrue(rs.getParameter("content", null));
				ctx = ctx.getContextWithArea(rs.getParameter("area", ctx.getArea()));
				String newCompId = null;
				File targetFolder = null;

				ISharedContentProvider provider = SharedContentService.getInstance(ctx).getProvider(ctx, rs.getParameter("provider"));
				if (provider != null && provider.isUploadable(ctx)) {
					SharedContentContext sharedContentContext = SharedContentContext.getInstance(ctx);
					targetFolder = provider.getFolder(ctx, sharedContentContext.getCategory());
				} 

				if (targetFolder == null) {
					String resourceRelativeFolder = URLHelper.mergePath(gc.getStaticConfig().getStaticFolder(), ctx.getGlobalContext().getStaticConfig().getImportImageFolder(), importFolder);
					targetFolder = new File(URLHelper.mergePath(gc.getDataFolder(), resourceRelativeFolder));
					provider = null;
				}

				for (FileItem item : rs.getAllFileItem()) {
					logger.info("try to import (" + ctx.getCurrentUserId() + ") : " + item.getName());

					if (item.getName().equals("blob")) {
						File targetImage = new File(URLHelper.mergePath(targetFolder.getAbsolutePath(), "clipboard.png"));
						targetFolder.getParentFile().mkdirs();
						targetImage = ResourceHelper.getFreeFileName(targetImage);
						JavaScriptBlob blob = new JavaScriptBlob(new String(item.get()));
						if (blob.getContentType().equalsIgnoreCase("image/png")) {
							logger.info("create clipboard file : "+targetImage);
							ResourceHelper.writeBytesToFile(targetImage, blob.getData());
							SharedContentContext sharedContentContext = SharedContentContext.getInstance(ctx);
							sharedContentContext.setProvider(ImportedImageSharedContentProvider.NAME);
							SharedContentService.getInstance(ctx).clearCache(ctx);
							// ISharedContentProvider provider =
							// SharedContentService.getInstance(ctx).getProvider(ctx,
							// sharedContentContext.getProvider());
							// provider.refresh(ctx);
							// provider.getContent(ctx); // refresh categories
							// list
						} else {
							msg = "bad blog format : " + blob.getContentType();
							logger.warning(msg);
							return msg;
						}
					}

					if (!ResourceHelper.isDocument(ctx, item.getName())) {
						logger.warning("try to import bad file format : " + item.getName());
						messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("global.message.file-format-error") + " : " + StringHelper.getFileExtension(item.getName()), GenericMessage.ERROR));
						return "bad file format : " + item.getName();
					}

					if (StringHelper.isImage(item.getName())) {
						countImages++;
						if (imageItem == null) {
							imageItem = item;
						}
					} else if (StringHelper.getFileExtension(item.getName()).equalsIgnoreCase("odt") && config.isSharedImportDocument()) {
						InputStream in = item.getInputStream();
						Collection<ComponentBean> beans = ContentHelper.createContentFromODT(gc, in, item.getName(), ctx.getRequestContentLanguage());
						in.close();
						cs.createContent(ctx, beans, previousId, true);
						ctx.setNeedRefresh(true);
					} else if (StringHelper.getFileExtension(item.getName()).equalsIgnoreCase("docx") && config.isSharedImportDocument()) {
						InputStream in = item.getInputStream();
						MenuElement page = ctx.getCurrentPage();
						Collection<ComponentBean> beans = ContentHelper.createContentFromDocx(ctx, in, page, item.getName(), ctx.getRequestContentLanguage());
						in.close();
						ContentContext contentCtx = new ContentContext(ctx);
						if (ctx.getCurrentPage().isChildrenOfAssociation() && !content) {
							PageAssociationBean pageAssociation = new PageAssociationBean(ctx, ctx.getCurrentPage().getRootOfChildrenAssociation());
							page = NavigationHelper.createChildPageAutoName(pageAssociation.getArticleRoot().getPage(), ctx);
							SharedContentContext sharedContentContext = SharedContentContext.getInstance(ctx);
							sharedContentContext.setProvider(CloserJavloSharedContentProvider.NAME);
							SharedContentService.getInstance(ctx).clearCache(ctx);
							contentCtx.setArea(ComponentBean.DEFAULT_AREA);
						}
						cs.createContent(contentCtx, page, beans, previousId, true);
						ctx.setNeedRefresh(true);
					} else if (StringHelper.getFileExtension(item.getName()).equalsIgnoreCase("zip") && item.getName().startsWith("export_")) { // JCR
						InputStream in = item.getInputStream();
						ImportJCRPageMacro.importFile(ctx, in, item.getName(), ctx.getCurrentPage());
						ResourceHelper.closeResource(in);
						ctx.setNeedRefresh(true);
					} else if (item.getName().contains("-tanuki-")) {
						InputStream in = item.getInputStream();
						TanukiImportTools.createContentFromTanuki(ctx, in, item.getName(), ctx.getRequestContentLanguage());
						in.close();
						ctx.setNeedRefresh(true);
					} else {
						boolean isArray = StringHelper.getFileExtension(item.getName()).equalsIgnoreCase("xls") || StringHelper.getFileExtension(item.getName()).equalsIgnoreCase("xlsx") || StringHelper.getFileExtension(item.getName()).equalsIgnoreCase("ods") || StringHelper.getFileExtension(item.getName()).equalsIgnoreCase("csv");
						String resourceRelativeFolder = URLHelper.mergePath(gc.getStaticConfig().getStaticFolder(), ctx.getGlobalContext().getStaticConfig().getImportResourceFolder(), importFolder);
						targetFolder = new File(URLHelper.mergePath(gc.getDataFolder(), resourceRelativeFolder));

						if (!targetFolder.exists()) {
							targetFolder.mkdirs();
						}
						File newFile = ResourceHelper.writeFileItemToFolder(item, targetFolder, true, rename);
						if (newFile != null && newFile.exists()) {

							SharedContentContext sharedContentContext = SharedContentContext.getInstance(ctx);
							SharedContentService sharedContentService = SharedContentService.getInstance(ctx);

							if (provider != null && provider.getFolder(ctx, sharedContentContext.getCategory()) != null) {
								File targetFile = new File(URLHelper.mergePath(provider.getFolder(ctx, sharedContentContext.getCategory()).getAbsolutePath(), newFile.getName()));
								if (targetFile.exists()) {
									targetFile.delete();
								}
								FileUtils.moveFile(newFile, targetFile);
							} else {
								boolean isStaticHTML = false;
								String dir = resourceRelativeFolder.replaceFirst(gc.getStaticConfig().getFileFolder(), "");
								if (StringHelper.getFileExtension(newFile.getName()).equalsIgnoreCase("zip") || StringHelper.isHTML(newFile.getName())) {
									isStaticHTML = StringHelper.isHTML(newFile.getName());
									if (!isStaticHTML) {
										ZipFile zip = null;
										try {
											zip = new ZipFile(newFile);
											Enumeration<? extends ZipEntry> entries = zip.entries();
											while (entries.hasMoreElements()) {
												if (entries.nextElement().getName().equals("index.html")) {
													isStaticHTML = true;
												}
											}
										} finally {
											ResourceHelper.closeResource(zip);
										}
									}
									if (isStaticHTML) {
										resourceRelativeFolder = URLHelper.mergePath(gc.getStaticConfig().getStaticFolder(), ctx.getGlobalContext().getStaticConfig().getImportVFSFolder(), importFolder);
										targetFolder = new File(URLHelper.mergePath(gc.getDataFolder(), resourceRelativeFolder));
										File vfsFile = new File(URLHelper.mergePath(targetFolder.getAbsolutePath(), newFile.getName()));
										if (vfsFile.exists()) {
											vfsFile.delete();
										}
										FileUtils.moveFile(newFile, vfsFile);
									}
								}
								ByteArrayOutputStream outStream = new ByteArrayOutputStream();
								PrintStream out = new PrintStream(outStream);
								out.println("dir=" + dir);
								out.println("file-name=" + StringHelper.getFileNameFromPath(newFile.getName()));
								out.close();
								String beanType = GenericFile.TYPE;
								if (isArray && ctx.getGlobalContext().hasComponent(ArrayFileComponent.class.getCanonicalName())) {
									beanType = ArrayFileComponent.TYPE;
								}
								if (ResourceHelper.isSound(ctx, newFile.getName()) && ctx.getGlobalContext().hasComponent(Sound.class.getCanonicalName())) {
									beanType = Sound.TYPE;
								}
								if (isStaticHTML) {
									beanType = VFSFile.TYPE;
								}
								StaticInfo staticInfo = StaticInfo.getInstance(ctx, newFile);
								ComponentBean bean = new ComponentBean(beanType, new String(outStream.toByteArray()), ctx.getRequestContentLanguage());
								if (sharedContentService.getActiveProviderNames(ctx).contains(ImportedFileSharedContentProvider.NAME)) {
									sharedContentContext.setProvider(ImportedFileSharedContentProvider.NAME);
									sharedContentService.clearCache(ctx);
								} else if (!content) {
									cs.createContentAtEnd(ctx, bean, true);
								}
								if (content) {
									bean.setArea(ctx.getArea());
									newCompId = cs.createContent(ctx, bean, previousId, true);
								}
								staticInfo.setShared(ctx, false);

								if (ctx.getCurrentPage().getUserRoles().size() > 0) {
									staticInfo.addReadRole(ctx, ctx.getCurrentPage().getUserRoles());
								}
							}
							ctx.setNeedRefresh(true);
						} else {
							return "error upload file : " + item.getName();
						}
					}
				}

				File folderSelection = null;

				ContentService contentService = ContentService.getInstance(ctx.getRequest());
				IContentVisualComponent previousComp = contentService.getComponent(ctx, previousId);
				if (previousComp != null && previousComp.getType().equals(FileFinder.TYPE)) {

				} else if (previousComp != null && (previousComp.getType().equals(Multimedia.TYPE) || previousComp.getType().equals(PDFMultimedia.TYPE))) {
					countImages = 2;
				}
				
				SharedContentContext sharedContentContext = SharedContentContext.getInstance(ctx);
				if (provider != null) {
					targetFolder = provider.getFolder(ctx, sharedContentContext.getCategory());
					importFolder = "";
				}
				
				if (countImages == 1) {
					if (provider != null) {
						targetFolder = provider.getFolder(ctx, sharedContentContext.getCategory());
						importFolder = "";
					} else {
						sharedContentContext.setProvider(ImportedImageSharedContentProvider.NAME);
					}
					
					if (!config.isImagesAsGallery() || provider != null) {
						folderSelection = createImage(ctx, importFolder, imageItem, config, content, previousId, rename);
						if (folderSelection != null) {
							if (provider != null) {
								File targetFile = new File(URLHelper.mergePath(targetFolder.getAbsolutePath(), folderSelection.getName()));
								targetFile = ResourceHelper.getFreeFileName(targetFile);
								FileUtils.moveFile(folderSelection, targetFile);
							}
							folderSelection = folderSelection.getParentFile();
						}
						
					} else {
						folderSelection = createOrUpdateGallery(ctx, targetFolder, importFolder, Arrays.asList(new FileItem[] { imageItem }), config, content, previousId);
						if (folderSelection != null && newCompId != null) {
							ctx.getCurrentPage().removeContent(ctx, newCompId);
						}
					}
					
					if (provider == null) {
						sharedContentContext.setProvider(ImportedImageSharedContentProvider.NAME);
						provider = SharedContentService.getInstance(ctx).getProvider(ctx, sharedContentContext.getProvider());
					}
					
					ctx.setNeedRefresh(true);
					
					SharedContentService.getInstance(ctx).clearCache(ctx);
					
					provider.refresh(ctx);
					provider.getContent(ctx); // refresh categories list
				} else if (countImages > 1) { // gallery
					
					System.out.println(">>>>>>>>> DataAction.uploadContent : gallery"); //TODO: remove debug trace
					
					if (provider != null) {
						targetFolder = provider.getFolder(ctx, sharedContentContext.getCategory());
						importFolder = "";
					}
					
					if (provider == null && !config.isImagesAsImages() && ctx.getGlobalContext().getComponents().contains(Multimedia.class.getName())) {
						folderSelection = createOrUpdateGallery(ctx, targetFolder, importFolder, rs.getAllFileItem(), config, content, previousId);
						if (folderSelection != null && newCompId != null) {
							ctx.getCurrentPage().removeContent(ctx, newCompId);
						}
					} else {
						for (FileItem file : rs.getAllFileItem()) {
							folderSelection = createImage(ctx, importFolder, file, config, content, previousId, rename);
							if (provider != null) {
								File targetFile = new File(URLHelper.mergePath(targetFolder.getAbsolutePath(), folderSelection.getName()));
								targetFile = ResourceHelper.getFreeFileName(targetFile);
								System.out.println(">>>>>>>>> DataAction.uploadContent : targetFile = "+targetFile); //TODO: remove debug trace
								FileUtils.moveFile(folderSelection, targetFile);
							}
							if (folderSelection != null) {
								folderSelection = folderSelection.getParentFile();
							}
						}
					}
					
					if (provider == null) {
						sharedContentContext.setProvider(ImportedImageSharedContentProvider.NAME);
						provider = SharedContentService.getInstance(ctx).getProvider(ctx, sharedContentContext.getProvider());
					}
					
					SharedContentService.getInstance(ctx).clearCache(ctx);
					provider.refresh(ctx);
					provider.getContent(ctx); // refresh categories list
				}
				if (!config.isCreateContentOnImportImage() && !content && countImages > 0) {
					if (provider == null) {
						sharedContentContext.setProvider(ImportedImageSharedContentProvider.NAME);
					}
					SharedContentService.getInstance(ctx).clearCache(ctx);
					if (provider == null) {
						provider = SharedContentService.getInstance(ctx).getProvider(ctx, sharedContentContext.getProvider());
					}
					provider.refresh(ctx);
					provider.getContent(ctx); // refresh categories list
					String prefix = URLHelper.mergePath(gc.getDataFolder(), gc.getStaticConfig().getImageFolder()).replace('\\', '/');
					if (folderSelection != null) {
						String currentPath = folderSelection.getAbsolutePath().replace('\\', '/');
						String cat = StringUtils.replace(currentPath, prefix, "");
						if (cat.length() > 1) { // remove '/'
							cat = cat.substring(1);
						}
						if (provider.getName().equals(ImportedImageSharedContentProvider.NAME)) {
							sharedContentContext.setCategories(new LinkedList<String>(Arrays.asList(new String[] { cat })));
						}
					}
					ctx.setNeedRefresh(true);
				}
			}
		} catch (FileExistsException e) {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("data.file-allready-exist", "file already exists."), GenericMessage.ERROR));
		}
		return msg;
	}

	public static String performSessionId(ContentContext ctx, HttpSession session, User user) {
		ctx.getAjaxData().put("sessionId", session.getId());
		return null;
	}

	public static String performTab(RequestService rs, HttpSession session) {
		session.setAttribute("tab", rs.getParameter("tab", null));
		return null;
	}

	public static String performMemory(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws NumberFormatException, IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Map memory = BeanUtils.describe(new MemoryBean());
		memory.remove("class");
		ctx.getAjaxData().putAll(memory);
		return null;
	}

	public static String performLocation(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws NumberFormatException, IOException {
		if (ctx.getCurrentEditUser() == null) {
			return null;
		}
		Double lat = StringHelper.safeParseDouble(rs.getParameter("lat", null), null);
		Double lg = StringHelper.safeParseDouble(rs.getParameter("long", null), null);
		if (lat != null && lg != null) {
			ctx.getAjaxData().put("location", LocationService.getLocation(lg, lat, rs.getParameter("lg", "en")).getFullLocality());
			return "";
		} else {
			return null;
		}
	}

	public static String performCreateFileName(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		String fileName = rs.getParameter("filename", null);
		if (fileName == null) {
			return "filename param not found.";
		} else {
			ctx.getAjaxData().put("fileName", StringHelper.createFileName(fileName));
		}
		return null;
	}

	public static String performFileExist(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String fileName = rs.getParameter("filename", null);
		boolean exist = false;
		if (ResourceHelper.isAcceptedImage(ctx, fileName)) {
			String imageRelativeFolder = URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), ctx.getGlobalContext().getStaticConfig().getImportImageFolder(), createImportFolder(ctx.getCurrentPage()));
			File image = new File(URLHelper.mergePath(imageRelativeFolder, StringHelper.createFileName(fileName)));
			exist = image.exists();
		} else {
			String imageRelativeFolder = URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), ctx.getGlobalContext().getStaticConfig().getImportResourceFolder(), createImportFolder(ctx.getCurrentPage()));
			File file = new File(URLHelper.mergePath(imageRelativeFolder, StringHelper.createFileName(fileName)));
			exist = file.exists();
		}
		ctx.getAjaxData().put("exist", exist);
		return null;
	}
	
	public static String performFileExistShared(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		SharedContentService sharedContentService = SharedContentService.getInstance(ctx);
		SharedContentContext sharedContentContext = SharedContentContext.getInstance(ctx);
		ISharedContentProvider provider = sharedContentService.getProvider(ctx, sharedContentContext.getProvider());
		if (!AdminUserSecurity.isCurrentUserCanUpload(ctx) && !sharedContentContext.getProvider().equals(ImportedImageSharedContentProvider.NAME)) {
			return "you have no right to upload file here.";
		}
		boolean fileExist = false;
		String filename = rs.getParameter("filename");
		if (provider != null && filename != null) {
			fileExist = provider.exist(ctx, filename, sharedContentContext.getCategory());
		}
		ctx.getAjaxData().put("exist", fileExist);
		return null;
	}

	public static String performUploadscreenshot(RequestService rs, ContentContext ctx, GlobalContext globalContext, User user, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		if (ctx.getCurrentEditUser() != null) {
			File imageFile;
			String pageName = rs.getParameter("page");
			if (StringHelper.isEmpty(pageName)) {
				imageFile = globalContext.getScreenshotFile(ctx);
			} else {
				imageFile = globalContext.getPageScreenshotFile(pageName);
			}
			imageFile.getParentFile().mkdirs();
			imageFile.createNewFile();
			for (FileItem item : rs.getAllFileItem()) {
				JavaScriptBlob blob = new JavaScriptBlob(new String(item.get()));
				ResourceHelper.writeBytesToFile(imageFile, blob.getData());
			}
			InputStream in = new FileInputStream(imageFile);
			BufferedImage img;
			try {
				img = ImageIO.read(in);
			} finally {
				ResourceHelper.closeResource(in);
			}
			img = ImageEngine.trim(img, Color.WHITE, 1);
			ImageHelper.saveImage(img, imageFile);
			StaticInfo staticInfo = StaticInfo.getInstance(ctx, imageFile);
			staticInfo.setFocusZoneY(ctx, 10);
			staticInfo.setFocusZoneX(ctx, 499);
			PersistenceService.getInstance(globalContext).setAskStore(true);
		}
		return null;
	}

	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return true;
	}
}
