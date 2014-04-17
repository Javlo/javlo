package org.javlo.macro;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileExistsException;
import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.file.FileAction.FileBean;
import org.javlo.module.file.FileModuleContext;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.user.User;
import org.javlo.ztatic.StaticInfo;

public class UploadGallery implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(UploadGallery.class.getName());

	@Override
	public String getName() {
		return "upload-gallery";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		return null;
	}

	@Override
	public boolean isAdmin() {
		return false;
	}

	@Override
	public String getActionGroupName() {
		return "macro-upload-gallery";
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/upload_gallery/home.jsp";
	}

	static Integer selectedYear;
	static Integer selectedMonth;
	static Map<String, Set<File>> uploadedFiles = new HashMap<String, Set<File>>();

	@Override
	public String prepare(ContentContext ctx) {

		if (selectedYear != null && selectedMonth != null) {
			Set<File> files = uploadedFiles.get(getGalleryFolderName(selectedYear, selectedMonth));
			if (files != null) {
				try {
					List<FileBean> fileList = new LinkedList<FileBean>();
					for (File file : files) {
						fileList.add(new FileBean(ctx, StaticInfo.getInstance(ctx, file)));
					}
					Collections.sort(fileList, new FileBean.FileBeanComparator(ctx, 1));
					ctx.getRequest().setAttribute("files", fileList);
				} catch (Exception e) {
					throw new RuntimeException("Problem...", e);
				}
			}
		}

		ctx.getRequest().setAttribute("selectedMonth", selectedMonth);
		ctx.getRequest().setAttribute("selectedYear", selectedYear);

		return null;
	}

	public static String performUpload(RequestService rs, ContentContext ctx, GlobalContext gc, ContentService cs, User user, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		int month = StringHelper.safeParseInt(rs.getParameter("month", null).trim(), 0);
		int year = StringHelper.safeParseInt(rs.getParameter("year", null).trim(), 0);
		FileItem[] files = rs.getFileItems("file");
		selectedMonth = month;
		selectedYear = year;

		boolean someExists = false;
		File root = getGalleryRoot(ctx);
		String galleryName = getGalleryFolderName(year, month);

		//TODO fix this hardcode
		FileModuleContext.getInstance(ctx.getRequest()).setPath("static/gallery/" + galleryName);

		Set<File> galleryFiles = uploadedFiles.get(galleryName);
		if (galleryFiles == null) {
			galleryFiles = new HashSet<File>();
			uploadedFiles.put(galleryName, galleryFiles);
		}

		File gallery = new File(root, galleryName);
		gallery.mkdirs();
		for (FileItem fileItem : files) {
			try {
				File outFile = ResourceHelper.writeFileItemToFolder(fileItem, gallery, false, false);
				galleryFiles.add(outFile);
			} catch (FileExistsException ex) {
				someExists = true;
			}
		}
		if (someExists) {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("data.file-allready-exist", "file allready exist."), GenericMessage.ALERT));
		}

		return null;
	}

	private static String getGalleryFolderName(int year, int month) {
		return StringHelper.renderNumber(year, 4) + "_" + StringHelper.renderNumber(month, 2);
	}

	private static File getGalleryRoot(ContentContext ctx) {
		return new File(URLHelper.mergePath(
				ctx.getGlobalContext().getDataFolder(),
				ctx.getGlobalContext().getStaticConfig().getGalleryFolder()));
	}

	@Override
	public boolean isPreview() {
		return true;
	}
}
