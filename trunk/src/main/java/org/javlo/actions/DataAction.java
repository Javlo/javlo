package org.javlo.actions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.logging.Logger;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileExistsException;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.files.GenericFile;
import org.javlo.component.image.GlobalImage;
import org.javlo.component.image.Image;
import org.javlo.component.multimedia.Multimedia;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ContentHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.ContentService;
import org.javlo.service.NotificationService;
import org.javlo.service.RequestService;
import org.javlo.template.Template;
import org.javlo.user.User;

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
	 */
	public static String performNotifications(RequestService rs, ContentContext ctx, NotificationService notif, User user) {
		if (user != null) {
			ctx.getAjaxData().put("notifications", notif.getNotifications(user.getLogin(), 999, StringHelper.isTrue(rs.getParameter("markread", null))));
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

	public static String performUpload(RequestService rs, ContentContext ctx, GlobalContext gc, ContentService cs, User user, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		if (user == null) {
			return "Please, login before upload files.";
		}
		Template tpl = ctx.getCurrentTemplate();
		Calendar cal = Calendar.getInstance();
		String importFolder = "" + (cal.get(Calendar.MONTH) + 1) + '_' + cal.get(Calendar.YEAR);
		int countImages = 0;
		FileItem imageItem = null;
		try {
			for (FileItem item : rs.getAllFileItem()) {
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
				} else {
					String resourceRelativeFolder = URLHelper.mergePath(gc.getStaticConfig().getStaticFolder(), tpl.getImportResourceFolder(), importFolder);
					File targetFolder = new File(URLHelper.mergePath(gc.getDataFolder(), resourceRelativeFolder));
					if (!targetFolder.exists()) {
						targetFolder.mkdirs();
					}
					if (ResourceHelper.writeFileItemToFolder(item, targetFolder) > 0) {
						ByteArrayOutputStream outStream = new ByteArrayOutputStream();
						PrintStream out = new PrintStream(outStream);
						String dir = resourceRelativeFolder.replaceFirst(gc.getStaticConfig().getFileFolder(), "");
						out.println("dir=" + dir);
						out.println("file-name=" + item.getName());
						out.close();
						ComponentBean bean = new ComponentBean(GenericFile.TYPE, new String(outStream.toByteArray()), ctx.getRequestContentLanguage());
						cs.createContent(ctx, bean, "0", true);
						ctx.setNeedRefresh(true);
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
				if (ResourceHelper.writeFileItemToFolder(imageItem, targetFolder) > 0) {
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					PrintStream out = new PrintStream(outStream);
					String dir = imageRelativeFolder.replaceFirst(gc.getStaticConfig().getImageFolder(), "");
					out.println("dir=" + dir);
					out.println("file-name=" + imageItem.getName());
					out.println(GlobalImage.IMAGE_FILTER + "=full");
					out.close();
					ComponentBean image = new ComponentBean(GlobalImage.TYPE, new String(outStream.toByteArray()), ctx.getRequestContentLanguage());
					image.setStyle(Image.STYLE_CENTER);
					cs.createContent(ctx, image, "0", true);
					ctx.setNeedRefresh(true);
				} else {
					return "error on file : " + imageItem.getName();
				}
			} else if (countImages > 1) { // gallery
				String galFolder = StringHelper.trimSpaceAndUnderscore(StringHelper.removeNumber(StringHelper.getFileNameWithoutExtension(imageItem.getName())));
				if (galFolder.trim().length() == 0) {
					galFolder = "images";
				}
				String galleryRelativeFolder = URLHelper.mergePath(gc.getStaticConfig().getStaticFolder(), tpl.getImportGalleryFolder(), importFolder, galFolder);
				targetFolder = new File(URLHelper.mergePath(gc.getDataFolder(), galleryRelativeFolder));
				int i = 1;
				while (targetFolder.exists()) {
					targetFolder = new File(URLHelper.mergePath(gc.getDataFolder(), galleryRelativeFolder, "_" + i));
					i++;
				}
				targetFolder.mkdirs();
				for (FileItem item : rs.getAllFileItem()) {
					if (StringHelper.isImage(item.getName())) {
						if (ResourceHelper.writeFileItemToFolder(item, targetFolder) < 0) {
							logger.warning("Could'nt upload : " + item.getName());
						}
					}
				}
				ComponentBean multimedia = new ComponentBean(Multimedia.TYPE, "--12,128-" + galleryRelativeFolder.replaceFirst(gc.getStaticConfig().getStaticFolder(), "") + "---", ctx.getRequestContentLanguage());
				multimedia.setStyle(Multimedia.IMAGE);
				cs.createContent(ctx, multimedia, "0", true);
				ctx.setNeedRefresh(true);

			}
		} catch (FileExistsException e) {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("data.file-allready-exist", "file allready exist."), GenericMessage.ERROR));
		}
		return null;
	}
}
