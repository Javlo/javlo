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

import javax.servlet.http.HttpSession;

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
import org.javlo.module.file.FileBean;
import org.javlo.module.file.FileModuleContext;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.user.User;
import org.javlo.ztatic.StaticInfo;

public class UploadGallery implements IInteractiveMacro, IAction {
	
	private static class UploadGalleryContext {
		
		private UploadGalleryContext(){};
		
		public static UploadGalleryContext getInstance(HttpSession session) {
			UploadGalleryContext galContext = (UploadGalleryContext)session.getAttribute(UploadGalleryContext.class.getName());
			if (galContext == null) {
				galContext = new UploadGalleryContext();
				session.setAttribute(UploadGalleryContext.class.getName(), galContext);
			}
			return galContext;
		}
		
		public Integer getSelectedYear() {
			return selectedYear;
		}

		public void setSelectedYear(Integer selectedYear) {
			this.selectedYear = selectedYear;
		}

		public Integer getSelectedMonth() {
			return selectedMonth;
		}

		public void setSelectedMonth(Integer selectedMonth) {
			this.selectedMonth = selectedMonth;
		}

		public Map<String, Set<File>> getUploadedFiles() {
			return uploadedFiles;
		}

		public void setUploadedFiles(Map<String, Set<File>> uploadedFiles) {
			this.uploadedFiles = uploadedFiles;
		}

		private Integer selectedYear;
		private Integer selectedMonth;
		private Map<String, Set<File>> uploadedFiles = new HashMap<String, Set<File>>();
		
	}

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

	

	@Override
	public String prepare(ContentContext ctx) {
		
		UploadGalleryContext uploadGalleryContext = UploadGalleryContext.getInstance(ctx.getRequest().getSession());

		if (uploadGalleryContext.getSelectedYear() != null && uploadGalleryContext.getSelectedMonth() != null) {
			Set<File> files = uploadGalleryContext.getUploadedFiles().get(getGalleryFolderName(uploadGalleryContext.getSelectedYear(),  uploadGalleryContext.getSelectedMonth()));
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

		ctx.getRequest().setAttribute("selectedMonth",  uploadGalleryContext.getSelectedMonth());
		ctx.getRequest().setAttribute("selectedYear",  uploadGalleryContext.getSelectedYear());

		return null;
	}

	public static String performUpload(RequestService rs, ContentContext ctx, GlobalContext gc, ContentService cs, User user, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		UploadGalleryContext uploadGalleryContext = UploadGalleryContext.getInstance(ctx.getRequest().getSession());
		
		int month = StringHelper.safeParseInt(rs.getParameter("month", null).trim(), 0);
		int year = StringHelper.safeParseInt(rs.getParameter("year", null).trim(), 0);
		FileItem[] files = rs.getFileItems("file");
		uploadGalleryContext.setSelectedMonth(month);
		uploadGalleryContext.setSelectedYear(year);

		boolean someExists = false;
		File root = getGalleryRoot(ctx);
		String galleryName = getGalleryFolderName(year, month);

		FileModuleContext.getInstance(ctx.getRequest()).setPath(URLHelper.mergePath(getRelativeGalleryRoot(ctx), galleryName));
		Set<File> galleryFiles = uploadGalleryContext.getUploadedFiles().get(galleryName);
		if (galleryFiles == null) {
			galleryFiles = new HashSet<File>();
			uploadGalleryContext.getUploadedFiles().put(galleryName, galleryFiles);
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
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("data.file-allready-exist", "file already exist."), GenericMessage.ALERT));
		}

		return null;
	}

	private static String getGalleryFolderName(int year, int month) {
		return StringHelper.renderNumber(year, 4) + '/' + StringHelper.renderNumber(month, 2);
	}

	private static File getGalleryRoot(ContentContext ctx) {
		return new File(URLHelper.mergePath(
				ctx.getGlobalContext().getDataFolder(),
				getRelativeGalleryRoot(ctx)));	
	}
	
	private static String getRelativeGalleryRoot(ContentContext ctx) {
		return URLHelper.mergePath(				
				ctx.getGlobalContext().getStaticConfig().getGalleryFolder(),
				"bydate");
	}

	@Override
	public boolean isPreview() {
		return true;
	}
}
