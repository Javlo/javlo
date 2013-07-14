package org.javlo.actions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileExistsException;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.files.GenericFile;
import org.javlo.component.image.GlobalImage;
import org.javlo.component.image.Image;
import org.javlo.component.multimedia.Multimedia;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ContentHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.importation.TanukiImportTools;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.ImportJCRPageMacro;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.ticket.TicketAction;
import org.javlo.service.ContentService;
import org.javlo.service.IMService;
import org.javlo.service.PersistenceService;
import org.javlo.service.IMService.IMItem;
import org.javlo.service.NotificationService;
import org.javlo.service.NotificationService.Notification;
import org.javlo.service.NotificationService.NotificationContainer;
import org.javlo.service.RequestService;
import org.javlo.template.Template;
import org.javlo.user.User;
import org.javlo.ztatic.StaticInfo;

public class DataAction implements IAction {

	private static Logger logger = Logger.getLogger(DataAction.class.getName());

	@Override
	public String getActionGroupName() {
		return "data";
	}

	/**
	 * get the list of modification. option for request : markread=true, mark all notification returned as read. this method need user logger.
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
			if (lastDate != null) {
				Calendar startCal = Calendar.getInstance();
				startCal.setTime(lastDate);
				List<NotificationContainer> notifs = notif.getNotifications(user.getLogin(), 999, StringHelper.isTrue(rs.getParameter("markread", null)));
				Calendar cal = Calendar.getInstance();
				for (NotificationContainer notificationContainer : notifs) {
					cal.setTime(notificationContainer.getNotification().getCreationDate());
					if (cal.after(startCal)) {
						finalNotifs.add(notificationContainer);
					}
				}
			} else {
				finalNotifs.addAll(notif.getNotifications(user.getLogin(), 999, StringHelper.isTrue(rs.getParameter("markread", null))));
			}
			IMService imService = IMService.getInstance(session);
			String currentSite = globalContext.getContextKey();
			String currentUser = ctx.getCurrentUserId();
			List<IMItem> messages = new LinkedList<IMItem>();
			imService.fillMessageList(currentSite, currentUser, lastDate, messages);
			if (!messages.isEmpty()) {
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

	public static String performUpdateArea(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		String areaKey = null;
		String area = rs.getParameter("area", null);
		if (area != null) {
			for (Map.Entry<String, String> areaId : ctx.getCurrentTemplate().getAreasMap().entrySet()) {
				if (areaId.getValue().equals(area)) {
					areaKey = areaId.getKey();
				}
			}
		}

		if (areaKey == null) {
			return "area not found : " + area;
		}

		String mode = rs.getParameter("render-mode", null);

		if (mode != null) {
			ctx.setRenderMode(Integer.parseInt(mode));
		}
		ctx.getAjaxInsideZone().put(area, ServletHelper.executeJSP(ctx, "/jsp/view/content_view.jsp?area=" + areaKey));
		return null;
	}
	
	public static String performTickets(ContentContext ctx) throws ConfigurationException, IOException {
		ctx.getAjaxData().put("tickets", TicketAction.getMyTicket(ctx));
		return null;
	}

	public static String performUpload(RequestService rs, ContentContext ctx, GlobalContext gc, ContentService cs, User user, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		if (user == null) {
			return "Please, login before upload files.";
		}
		Template tpl = ctx.getCurrentTemplate();
		// Calendar cal = Calendar.getInstance();
		// String importFolder = "" + (cal.get(Calendar.MONTH) + 1) + '_' + cal.get(Calendar.YEAR);
		String importFolder = StringHelper.createFileName(ctx.getCurrentPage().getTitle(ctx.getContextForDefaultLanguage()));
		importFolder = StringHelper.trimOn(importFolder, "-");
		importFolder = importFolder.replace('-', '_');
		int countImages = 0;
		FileItem imageItem = null;
		try {
			for (FileItem item : rs.getAllFileItem()) {
				
				logger.info("try to import ("+ctx.getCurrentUserId()+") : "+item.getName());
				
				if (StringHelper.isImage(item.getName())) {
					countImages++;
					if (imageItem == null) {
						imageItem = item;
					}
				} else if (StringHelper.getFileExtension(item.getName()).equalsIgnoreCase("odt")) {
					InputStream in = item.getInputStream();
					Collection<ComponentBean> beans = ContentHelper.createContentFromODT(gc, in, item.getName(), ctx.getRequestContentLanguage());
					in.close();
					cs.createContent(ctx, beans, "0", true);
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
					String resourceRelativeFolder = URLHelper.mergePath(gc.getStaticConfig().getStaticFolder(), tpl.getImportResourceFolder(), importFolder);
					File targetFolder = new File(URLHelper.mergePath(gc.getDataFolder(), resourceRelativeFolder));
					if (!targetFolder.exists()) {
						targetFolder.mkdirs();
					}
					File newFile = ResourceHelper.writeFileItemToFolder(item, targetFolder, false, true);
					if (newFile != null && newFile.exists()) {
						ByteArrayOutputStream outStream = new ByteArrayOutputStream();
						PrintStream out = new PrintStream(outStream);
						String dir = resourceRelativeFolder.replaceFirst(gc.getStaticConfig().getFileFolder(), "");
						out.println("dir=" + dir);
						out.println("file-name=" + newFile.getName());
						out.close();
						ComponentBean bean = new ComponentBean(GenericFile.TYPE, new String(outStream.toByteArray()), ctx.getRequestContentLanguage());
						cs.createContentAtEnd(ctx, bean, true);
						ctx.setNeedRefresh(true);
						StaticInfo staticInfo = StaticInfo.getInstance(ctx, newFile);
						staticInfo.setShared(ctx, false); // by default a simple image is'nt share
					} else {
						return "error upload file : " + item.getName();
					}
				}
			}
			File targetFolder = null;
			if (countImages == 1) {
				String imageRelativeFolder = URLHelper.mergePath(gc.getStaticConfig().getStaticFolder(), tpl.getImportImageFolder(), importFolder);
				targetFolder = new File(URLHelper.mergePath(gc.getDataFolder(), imageRelativeFolder));
				if (!targetFolder.exists()) {
					targetFolder.mkdirs();
				}
				File newFile = ResourceHelper.writeFileItemToFolder(imageItem, targetFolder, false, true);
				if (newFile != null && newFile.exists()) {
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					PrintStream out = new PrintStream(outStream);
					String dir = imageRelativeFolder.replaceFirst(gc.getStaticConfig().getImageFolder(), "");
					out.println("dir=" + dir);
					out.println("file-name=" + imageItem.getName());
					out.println(GlobalImage.IMAGE_FILTER + "=full");
					out.close();
					ComponentBean image = new ComponentBean(GlobalImage.TYPE, new String(outStream.toByteArray()), ctx.getRequestContentLanguage());
					image.setStyle(Image.STYLE_CENTER);
					// cs.createContent(ctx, image, "0", true);
					cs.createContentAtEnd(ctx, image, true);
					ctx.setNeedRefresh(true);
				} else {
					return "error on file : " + imageItem.getName();
				}
			} else if (countImages > 1) { // gallery

				Collection<IContentVisualComponent> mediaComps = ctx.getCurrentPage().getContentByType(ctx, Multimedia.TYPE);
				boolean galleryFound = false;

				String galleryRelativeFolder = null;
				if (mediaComps.size() > 0) {
					for (IContentVisualComponent comp : mediaComps) {
						Multimedia multimedia = (Multimedia) comp;
						if (multimedia.getCurrentRootFolder().length() > tpl.getImportGalleryFolder().length()) {
							galleryRelativeFolder = URLHelper.mergePath(gc.getStaticConfig().getStaticFolder(), multimedia.getCurrentRootFolder());
							galleryFound = true;
							ctx.setNeedRefresh(true);
						}
					}

				}
				if (galleryRelativeFolder == null) {
					String galFolder = importFolder;
					if (galFolder.trim().length() == 0) {
						galFolder = "images";
					}
					galleryRelativeFolder = URLHelper.mergePath(gc.getStaticConfig().getStaticFolder(), tpl.getImportGalleryFolder(), importFolder);
				}
				targetFolder = new File(URLHelper.mergePath(gc.getDataFolder(), galleryRelativeFolder));

				if (!targetFolder.exists()) {
					targetFolder.mkdirs();
				}
				for (FileItem item : rs.getAllFileItem()) {
					if (StringHelper.isImage(item.getName())) {
						if (ResourceHelper.writeFileItemToFolder(item, targetFolder, false, true) == null) {
							logger.warning("Could'nt upload : " + item.getName());
						}
					}
				}

				if (!galleryFound) {
					ComponentBean multimedia = new ComponentBean(Multimedia.TYPE, "--12,128-" + galleryRelativeFolder.replaceFirst(gc.getStaticConfig().getStaticFolder(), "") + "---", ctx.getRequestContentLanguage());
					multimedia.setStyle(Multimedia.IMAGE);

					Collection<IContentVisualComponent> titles = ctx.getCurrentPage().getContentByType(ctx, Title.TYPE);
					String previousId = "0";
					if (titles.size() > 0) {
						previousId = titles.iterator().next().getId();
					}
					cs.createContent(ctx, multimedia, previousId, true);

					ctx.setNeedRefresh(true);
				}
				
				if (ctx.isNeedRefresh()) { // if there are modification >>> store new contnet.
					PersistenceService.getInstance(ctx.getGlobalContext()).store(ctx);
				}

			}
		} catch (FileExistsException e) {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("data.file-allready-exist", "file allready exist."), GenericMessage.ERROR));
		}
		return null;
	}

	public static String performSessionId(ContentContext ctx, HttpSession session, User user) {
		ctx.getAjaxData().put("sessionId", session.getId());
		return null;
	}
}
