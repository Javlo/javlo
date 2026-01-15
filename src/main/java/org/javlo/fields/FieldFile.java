package org.javlo.fields;


import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.lang3.StringUtils;
import org.javlo.actions.DataAction;
import org.javlo.bean.Link;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.filter.DirectoryFilter;
import org.javlo.helper.Comparator.FileComparator;
import org.javlo.helper.*;
import org.javlo.module.file.FileAction;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.google.translation.ITranslator;
import org.javlo.service.resource.Resource;
import org.javlo.ztatic.IStaticContainer;
import org.javlo.ztatic.StaticInfo;
import org.javlo.ztatic.StaticInfoBean;

import java.io.*;
import java.util.*;

public class FieldFile extends Field implements IStaticContainer {
	
public class StaticFileBean extends FieldBean {
		
		private String filter = getFilter();

		public StaticFileBean(ContentContext ctx) {
			super(ctx);
		}
		
		public String getPreviewUrl() throws Exception {

			FieldFile refField = (FieldFile)FieldFile.this.getReference(ctx);

			if ( refField.getCurrentFile() == null || refField.getCurrentFile().trim().length() == 0) {
				return null;
			}
			String relativePath = URLHelper.mergePath(refField.getFileTypeFolder(), refField.getCurrentFolder());
			String fileURL = URLHelper.mergePath(relativePath, refField.getCurrentFile());
			try {
				return URLHelper.createTransformURL(ctx, '/' + fileURL, getImageFilter());
			} catch (Exception e) {			
				e.printStackTrace();
				return null;
			}
		}
		
		@Deprecated
		public String getPreviewURL() throws Exception {
			return getPreviewUrl();
		}
		
		public String getResourceUrl() throws Exception {

			FieldFile refField = (FieldFile)FieldFile.this.getReference(ctx);

			if ( refField.getCurrentFile() == null || refField.getCurrentFile().trim().length() == 0) {
				return null;
			}
			String relativePath = URLHelper.mergePath(refField.getFileTypeFolder(), refField.getCurrentFolder());
			String fileURL = URLHelper.mergePath(relativePath, refField.getCurrentFile());
			try {
				return URLHelper.createResourceURL(ctx, '/' + fileURL);
			} catch (Exception e) {			
				e.printStackTrace();
				return null;
			}
		}
		
		@Deprecated
		public String getResourceURL() throws Exception {
			return getResourceUrl();
		}
		
		public String getLink() {
			return FieldFile.this.getCurrentLink();
		}
		
		public String getAlt() {
			return FieldFile.this.getCurrentLabel();
		}

		public String getImageFilter() {
			return filter;
		}

		public void setImageFilter(String filter) {
			this.filter = filter;
		}
		
		public String getViewXHTMLCode() throws Exception {
			FieldFile refField = (FieldFile)FieldFile.this.getReference(ctx);
			return refField.getViewXHTMLCode(ctx);
		}
		
		public StaticInfo getStaticInfo() throws Exception {
			FieldFile refField = (FieldFile)FieldFile.this.getReference(ctx);
			String relativePath = URLHelper.mergePath(refField.getFileTypeFolder(), refField.getCurrentFolder());
			String fileURL = URLHelper.mergePath(relativePath, refField.getCurrentFile());
			File file = new File(URLHelper.mergePath(URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), fileURL)));
			return StaticInfo.getInstance(ctx, file);
		}
		
		public StaticInfoBean getStaticInfoBean() throws Exception {
			String relativePath = URLHelper.mergePath(FieldFile.this.getFileTypeFolder(), FieldFile.this.getCurrentFolder());
			String fileURL = URLHelper.mergePath(relativePath, FieldFile.this.getCurrentFile());
			File file = new File(URLHelper.mergePath(URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), fileURL)));
			return new StaticInfoBean (ctx, StaticInfo.getInstance(ctx, file));
		}
		
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		copyFilesInImportFolder(ctx);
	}

	protected FieldBean newFieldBean(ContentContext ctx) {
		return new StaticFileBean(ctx);
	}

	protected String getFilter() {
		return properties.getProperty("field." + getUnicName() + ".image.filter", "standard");
	}

	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FieldFile.class.getName());

	protected String getInputFolderName() {
		return getName() + "-folder-" + getId();
	}

	protected String getInputCreateFolderName() {
		return getName() + "-create-folder-" + getId();
	}

	protected String getFolderLabel() {
		return getI18nAccess().getText("global.group");
	}

	protected String getInputFileName() {
		return getName() + "-file-" + getId();
	}

	protected String getInputFileNameSelect() {
		return getName() + "-select-file-" + getId();
	}

	protected String getInputAddFileName() {
		return getName() + "-add-file-" + getId();
	}

	protected String getInputLabelFileName() {
		return getName() + "-label-file-" + getId();
	}

	protected String getFileLabel() {
		return getI18nAccess().getText("global.file");
	}

	protected String getLabelLabel() {
		return getI18nAccess().getText("global.label");
	}

	protected String getAddFileLabel() {
		return getI18nAccess().getText("action.add-file.add");
	}

	protected String getCreateFolderLabel() {
		return getI18nAccess().getText("action.add-image.new-dir");
	}

	protected String getFileType() {
		return this.properties.getProperty("field." + getName() + ".file.type", null);
	}

	protected boolean isCategoryRecursive() {
		return StringHelper.isTrue(this.properties.getProperty("field." + getName() + ".recursive", null), true);
	}

	protected String getFileTypeFolder() {
		return getStaticConfig().getFileFolder();
	}

	protected String getFileDirectory() {
		return URLHelper.mergePath(getGlobalContext().getDataFolder(), getFileTypeFolder());
	}
	
	public static String getImportFolderPath(ContentContext ctx) {
		try {
			MenuElement page = ctx.getCurrentPage();
			String importFolder = ctx.getGlobalContext().getStaticConfig().getImportFolder();
			if (importFolder.length() > 1 && importFolder.startsWith("/")) {
				importFolder = importFolder.substring(1);
			}
			return importFolder + '/' + DataAction.createImportFolder(page);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected void copyFilesInImportFolder(ContentContext ctx) throws Exception {
		if (!ctx.isAsViewMode()) {
			File oldFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), getRelativeFileDirectory(ctx), getCurrentFolder(), getCurrentFile()));
			String oldPath = StringHelper.cleanPath(oldFile.getAbsolutePath());
			if (oldPath.contains(ctx.getGlobalContext().getStaticConfig().getImportFolder())) {
				String importFolder = getImportFolderPath(ctx);
				File newFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(),
						getRelativeFileDirectory(ctx),
						importFolder,
						getCurrentFile()));
				try {
					if (!oldFile.getAbsolutePath().equals(newFile.getAbsolutePath()) && (oldFile.exists() || newFile.exists())) {
						if (!newFile.getParentFile().exists()) {
							newFile.getParentFile().mkdirs();
						}
						if (!newFile.exists()) {
							ResourceHelper.writeFileToFile(oldFile, newFile);
							ResourceHelper.copyResourceData(ctx, oldFile, newFile);
						}
						setCurrentFolder(ctx, importFolder);
						getReferenceComponent(ctx).setModify();
						PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
						logger.info("copy imported file : "+oldFile+" -> "+newFile);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				ResourceHelper.cleanImportResource(ctx, oldFile);
			}
		}
	}
	
	@Override
	public boolean  initContent(ContentContext ctx) throws Exception {
		setCurrentFolder(ctx, getImportFolderPath(ctx));
		return super.initContent(ctx);
	}

	protected void clearImportFolder(ContentContext ctx) {
		if (ctx.isAsModifyMode()) {
			String curFolder = getCurrentFolder();
			String importFolder = ctx.getGlobalContext().getStaticConfig().getImportFolder();
			if (importFolder.startsWith("/")) {
				importFolder = importFolder.substring(1);
			}
			if (curFolder.contains(importFolder)) {
				properties.setProperty("field." + getUnicName() + ".value.folder", getImportFolderPath(ctx));
			}
		}
	}

	@Override
	public void setProperties(ContentContext ctx, Properties p) {
		super.setProperties(ctx, p);
	}

	protected List<String> getFolderListForSelection(ContentContext ctx) {
		String importFolder = ctx.getGlobalContext().getStaticConfig().getImportFolder();
		if (importFolder.length() > 1 && importFolder.startsWith("/")) {
			importFolder = importFolder.substring(1);
		}
		String currentImportFolder = getImportFolderPath(ctx);
		
		File dir = new File(getFileDirectory());
		List<String> list = new ArrayList<String>();
		list.add("");
		if (dir.exists()) {
			File[] files = dir.listFiles((FilenameFilter)new DirectoryFilter());
			if (!isCategoryRecursive()) {
				Comparator fileComparator = new FileComparator(FileComparator.NAME, true);
				Arrays.sort(files, fileComparator);
				for (File file : files) {
					list.add(file.getName());
				}
			} else {
				for (File file : ResourceHelper.getAllDirList(dir)) {
					String name = StringUtils.replaceOnce(file.getAbsolutePath(), dir.getAbsolutePath(), "");
					name = StringHelper.cleanPath(name);
					if (name.startsWith("/")) {
						name = name.substring(1);
					}
					if (!name.startsWith(importFolder) || name.startsWith(currentImportFolder)) {
						list.add(name);
					}
				}
				if (!list.contains(currentImportFolder)) {
					list.add(currentImportFolder);
				}
			}
		}
		return ResourceHelper.cleanFolderList(list);
	}

	protected List<String> getFileList() {
		File dir = new File(URLHelper.mergePath(getFileDirectory(), getCurrentFolder()));
		List<String> list = new ArrayList<String>();
		list.add("");
		if (dir.exists()) {
			File[] files = dir.listFiles();

			Comparator fileComparator = new FileComparator(FileComparator.NAME, true);
			Arrays.sort(files, fileComparator);

			for (int i = 0; i < files.length; i++) {
				if (!files[i].isDirectory()) {
					list.add(files[i].getName());
				}
			}
		} else {
			logger.warning("folder not found : " + dir);
		}
		return list;
	}

	protected String getPreviewCode(ContentContext ctx, boolean title) throws Exception {
		if ((getValue() != null && getValue().trim().length() == 0) || getCurrentFile() == null || getCurrentFile().trim().length() == 0) {
			return "";
		}
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		String relativePath = URLHelper.mergePath(getFileTypeFolder(), getCurrentFolder());
		String fileURL = URLHelper.mergePath(relativePath, getCurrentFile());
		String img;
		String cssClass = "";
		if (StringHelper.isImage(getCurrentFile())) {
			img = URLHelper.createTransformURL(ctx, '/' + fileURL, "icon");
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			if (globalContext.isImagePreview()) {
				cssClass = " class=\"ajax_image_preview\"";
			}
		} else {
			img = XHTMLHelper.getFileBigIcone(ctx, fileURL);
		}
		out.println("<img" + cssClass + "  src=\"" + img + "\" alt=\"" + getCurrentFile() + " preview\" />");
		if (title) {
			out.println("<div class=\"title\">" + getCurrentFile() + "</div>");
		}
		out.close();
		return writer.toString();
	}

	@Override
	public String getEditXHTMLCode(ContentContext ctx, boolean search) throws Exception {

		copyFilesInImportFolder(ctx);

		String refCode = referenceEditCode(ctx);
		if (refCode != null) {
			return refCode;
		}

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		out.println("<div class=\"form-group field-" + getName() + "\">");
		out.println("<fieldset>");
		out.println("<legend>" + getLabel(ctx, ctx.getLocale()) + "</legend>");
		out.println("<div class=\"commands\"><div class=\"row\"><div class=\"col-sm-9\">");

		/** select feed back **/
		RequestService rs = RequestService.getInstance(ctx.getRequest());

		String path = ctx.getRequest().getParameter("path");
		if (path != null) {
			path = path.substring(path.indexOf("/",1));
			String fileName = StringHelper.getFileNameFromPath(path);
			path = path.replace(fileName, "");
			if (path.startsWith("/")) {
				path = path.substring(1);
			}
			if (path.endsWith("/")) {
				path = path.substring(0,path.length()-1);
			}
			setCurrentFolder(ctx, path);
			setCurrentFile(fileName);
		} else if (!StringHelper.isEmpty(rs.getParameter(getInputFileNameSelect())) && rs.getParameter("backreturn") == null) {

			// vérifier, il me semble qu'on n'entre jamais ici, car le lien contient toujours "path"

			String file = StringHelper.getFileNameFromPath(rs.getParameter(getInputFileNameSelect()));
			setCurrentFile(file);
			String folder = new File(rs.getParameter(getInputFileNameSelect())).getParentFile().getPath();
			folder = StringHelper.cleanPath(folder);
			folder = folder.replace(ctx.getGlobalContext().getStaticConfig().getImageFolder(), "");
			while (folder.length() > 0 && folder.startsWith("/")) {
				folder = folder.substring(1);
			}
			setCurrentFolder(ctx, folder);
			out.println("<input type=\"hidden\" name=\"" + getForceModifFieldName() + "\" value=\"true\" />");
		}

		if (!isLight()) {
			out.println("<div class=\"row form-group\"><div class=\"" + LABEL_CSS + "\">");
			out.println("<label for=\"" + getInputCreateFolderName() + "\">" + getCreateFolderLabel() + " : </label>");
			out.println("</div><div class=\"" + SMALL_VALUE_SIZE + "\"><input class=\"form-control\" type=\"text\" id=\"" + getInputCreateFolderName() + "\" name=\"" + getInputCreateFolderName() + "\" /></div>");
			out.println("<div class=\"" + SMALL_PART_SIZE + "\"><input type=\"submit\" class=\"ajax_update_click btn btn-default btn-xs\" name=\"create\" value=\">>\" />");
			out.println("</div></div>");

			Map<String, String> filesParams = new HashMap<String, String>();
			path = URLHelper.mergePath(FileAction.getPathPrefix(ctx), StaticConfig.getInstance(ctx.getRequest().getSession()).getImageFolderName(), getCurrentFolder());
			filesParams.put("path", path);
			filesParams.put("webaction", "changeRenderer");
			filesParams.put("page", "meta");
			String backURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "content");
			if (ctx.isEditPreview()) {
				backURL = URLHelper.addParam(backURL, "comp_id", "cp_" + getId());
				backURL = URLHelper.addParam(backURL, "webaction", "editPreview");
			}
			backURL = URLHelper.addParam(backURL, "previewEdit", ctx.getRequest().getParameter("previewEdit"));
			filesParams.put(ElementaryURLHelper.BACK_PARAM_NAME, backURL + "=/" + ctx.getGlobalContext().getStaticConfig().getStaticFolder() + '/');
			String staticLinkURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams);

			String linkToResources = "";

			out.println("<div class=\"row form-group folder\"><div class=\"" + LABEL_CSS + "\">");
			out.println("<label for=\"" + getInputFolderName() + "\">" + getFolderLabel() +" ["+getCurrentFolder()+"] : </label></div><div class=\"" + SMALL_VALUE_SIZE + "\">");
			out.println(XHTMLHelper.getInputOneSelect(getInputFolderName(), getFolderListForSelection(ctx), getCurrentFolder(), "form-control", "jQuery(this.form).trigger('submit');", true));
			out.println("</div></div>");

			if (!ctx.getGlobalContext().isMailingPlatform()) {
				backURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "content");
				if (ctx.isEditPreview()) {
					backURL = URLHelper.addParam(backURL, "comp_id", "cp_" + getId());
					backURL = URLHelper.addParam(backURL, "webaction", "editPreview");
				}
				backURL = URLHelper.addParam(backURL, "previewEdit", ctx.getRequest().getParameter("previewEdit"));
				filesParams.put(ElementaryURLHelper.BACK_PARAM_NAME, backURL + '&' + getInputFileNameSelect() + "=/" + ctx.getGlobalContext().getStaticConfig().getStaticFolder() + '/');
				staticLinkURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams);
				staticLinkURL = URLHelper.addParam(staticLinkURL, "select", "back");
				linkToResources = "<div class=\"" + SMALL_PART_SIZE + "\"><a class=\"browse-link btn btn-default btn-xs\" href=\"" + staticLinkURL + "\">" + i18nAccess.getText("global.select") + "</a></div>";
			}

			out.println("<div class=\"row form-group\"><div class=\"" + LABEL_CSS + "\">");
			out.println("<label for=\"" + getInputFileName() + "\">" + getFileLabel() + " : </label></div><div class=\"" + SMALL_VALUE_SIZE + "\">");
			out.println(XHTMLHelper.getInputOneSelect(getInputFileName(), getFileList(), getCurrentFile(), "form-control", "jQuery(this.form).trigger('submit');", true));
			out.println("</div>" + linkToResources + "</div>");
		}

		out.println("<div class=\"row form-group\"><div class=\"" + LABEL_CSS + "\">");
		out.println("<label for=\"" + getInputAddFileName() + "\">" + getAddFileLabel() + " " + i18nAccess.getEditLg() + " : </label>");
		out.println("</div><div class=\"" + VALUE_SIZE + "\"><input type=\"file\" id=\"" + getInputAddFileName() + "\" name=\"" + getInputAddFileName() + "\" /></div>");
		out.println("</div>");

		out.println("<div class=\"row form-group\"><div class=\"" + LABEL_CSS + "\">");
		out.println("<label for=\"" + getInputLabelFileName() + "\">" + getLabelLabel() + " : </label>");
		out.println("</div><div class=\"" + VALUE_SIZE + "\"><input class=\"form-control\" type=\"text\" id=\"" + getInputLabelFileName() + "\" name=\"" + getInputLabelFileName() + "\" value=\"" + getCurrentLabel() + "\" />");
		out.println("</div></div>");

		if (isWithLink()) {
			out.println("<div class=\"row form-group\"><div class=\"" + LABEL_CSS + "\">");
			out.println("<label for=\"" + getInputLabelLinkName() + "\">" + getLinkLabel() + " : </label>");
			out.println("</div><div class=\"" + VALUE_SIZE + "\"><input class=\"form-control\" type=\"text\" id=\"" + getInputLabelLinkName() + "\" name=\"" + getInputLabelLinkName() + "\" value=\"" + getCurrentLink() + "\" />");
			out.println("</div></div>");
		}

		out.println("</div><div class=\"col-sm-3\"><div class=\"preview\">");
		out.println(getPreviewCode(ctx, true));
		if (!StringHelper.isEmpty(getCurrentFile())) {
			out.println("<button type=\"submit\" name=\"" + getDeleteLinkName() + "\" class=\"btn btn-default btn-xs\" value=\"1\">" + i18nAccess.getText("global.delete") + "</button>");
		}
		out.println("</div></div></div></div>");
		out.println("</fieldset></div>");

		out.close();
		return writer.toString();
	}

	@Override
	public String getSearchEditXHTMLCode(ContentContext ctx) throws Exception {
		String refCode = referenceEditCode(ctx);
		if (refCode != null) {
			return refCode;
		}
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println("<div class=\"form-check\"><div class=\"checkbox\">");
		out.println(getEditLabelCode());
		out.println("<label class=\"form-check-label\">");
		String checkedHTML = "";
		if (StringHelper.isTrue(getValue())) {
			checkedHTML = " checked=\"checked\"";
		}

		String label = getSearchLabel(ctx, new Locale(ctx.getContextRequestLanguage()));

		out.print("<input id=\"" + getInputName() + "\" name=\"" + getInputName() + "\" type=\"checkbox\" value=\"true\"" + checkedHTML + " class=\"form-check-input\" />");
		out.println(label);
		if (getMessage() != null && getMessage().trim().length() > 0) {
			out.println("	<div class=\"message " + getMessageTypeCSSClass() + "\">" + getMessage() + "</div>");
		}
		out.println("</label></div></div>");
		out.close();
		return writer.toString();
	}

	/**
	 * render the field when he is used as reference value in a other language.
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	protected String getReferenceFieldView(ContentContext ctx) throws Exception {
		return "<div class=\"slave-field line form-group\"><label>" + getLabel(ctx, ctx.getLocale()) + "</label>" + getPreviewCode(ctx, false) + "</div>";
	}

	public String getCurrentLink() {
		return properties.getProperty("field." + getUnicName() + ".value.link", "");
	}

	protected String getInputLabelLinkName() {
		return getName() + "-link-" + getId();
	}

	protected String getDeleteLinkName() {
		return getName() + "-delete-" + getId();
	}

	protected String getLinkLabel() {
		return getI18nAccess().getText("global.link");
	}

	protected boolean isWithLink() {
		return false;
	}

	@Override
	public boolean isPertinent(ContentContext ctx) {
		return getCurrentFile() != null && getCurrentFile().length() > 0;
	}

	@Override
	public boolean search(ContentContext ctx, String query) {
		boolean needFile = StringHelper.isTrue(query);
		if (needFile) {
			return !StringHelper.isEmpty(getCurrentFile());
		} else {
			return true;
		}
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		String refCode = referenceViewCode(ctx);
		if (refCode != null) {
			return refCode;
		}

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		if (!isViewDisplayed()) {
			return "";
		}

		if ((getValue() != null && getValue().trim().length() == 0) || getCurrentFile() == null || getCurrentFile().trim().length() == 0) {
			return "";
		}

		String relativePath = URLHelper.mergePath(getFileTypeFolder(), getCurrentFolder());
		String fileURL = URLHelper.createFileURL(ctx, URLHelper.mergePath(relativePath, getCurrentFile()));

		String target = "";
		if (ctx.getGlobalContext().isOpenFileAsPopup()) {
			target = "target=\"_blank\"";
		}
		out.println("<a class=\"" + getCssClass() + ' ' + getType() + "\"" + target + " href=\"" + fileURL + "\">" + getViewLabel() + "</a>");

		out.close();
		return writer.toString();
	}

	@Override
	public String getType() {
		return "file";
	}

	public String getURL(ContentContext ctx) {
		String relativePath = URLHelper.mergePath(getFileTypeFolder(), getCurrentFolder());
		return URLHelper.createFileURL(ctx, URLHelper.mergePath(relativePath, getCurrentFile()));
	}

	@Override
	public boolean process(ContentContext ctx) {

		clearImportFolder(ctx);

		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		boolean modify = StringHelper.isTrue(requestService.getParameter(getForceModifFieldName()));

		String newFolderName = requestService.getParameter(getInputCreateFolderName(), "");
		String folder = requestService.getParameter(getInputFolderName(), "");
		String fileName = requestService.getParameter(getInputFileName(), "");
		String newFileName = requestService.getParameter(getInputAddFileName(), "");
		String label = requestService.getParameter(getInputLabelFileName(), null);
		String link = requestService.getParameter(getInputLabelLinkName(), null);
		boolean delete = StringHelper.isTrue(requestService.getParameter(getDeleteLinkName(), null));

		if (label != null) {
			if (!label.equals(getCurrentLabel())) {
				modify = true;
				setCurrentLabel(label);
			}
		}

		if (link != null) {
			if (!link.equals(getCurrentLink())) {
				modify = true;
				setCurrentLink(link);
			}
		}

		if (isLight()) {
			try {
				setCurrentFolder(ctx, AbstractVisualComponent.getImportFolderPath(ctx, comp.getPage()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			String currentFolder = getCurrentFolder();
			if (newFolderName.trim().length() > 0) {
				File newFolder = new File(URLHelper.mergePath(getFileDirectory(), newFolderName));
				newFolder.mkdirs();
				if (!getCurrentFolder().equals(newFolderName)) {
					setCurrentFolder(ctx, newFolderName);
					setCurrentFile("");
					setCurrentLabel("");
					newFileName = "";
					fileName = "";
					modify = true;
					setNeedRefresh(true);
				}
			} else if (!currentFolder.equals(folder)) {
				setCurrentFolder(ctx, folder);
				setCurrentFile("");
				setCurrentLabel("");
				newFileName = "";
				fileName = "";
				modify = true;
				setNeedRefresh(true);
			}
		}
		boolean upload = true;
		if (!StringHelper.isEmpty(getFileType())) {
			List<String> types = StringHelper.stringToCollection(getFileType(), ",");
			if (!types.contains(StringHelper.getFileExtension(newFileName).toLowerCase())) {
				setMessage(i18nAccess.getText("global.bad-file-type") + getFileType());
				setMessageType(Field.MESSAGE_ERROR);
				upload = false;
			}
		}
		if (delete) {
			String dir = URLHelper.mergePath(getFileDirectory(), getCurrentFolder());
			File file = new File(URLHelper.mergePath(dir, getCurrentFile()));
			if (file.exists()) {
				file.delete();
			}
			setCurrentFile(null);
		} else if (upload && newFileName.trim().length() > 0) {
			newFileName = StringHelper.createFileName(newFileName);
			Collection<FileItem> fileItems = requestService.getAllFileItem();
			try {
				for (FileItem fileItem : fileItems) {
					if (fileItem.getFieldName().equals(getInputAddFileName())) {
						String dir = URLHelper.mergePath(getFileDirectory(), getCurrentFolder());
						File file = new File(URLHelper.mergePath(dir, newFileName));
						if (!file.exists()) {
							file.getParentFile().mkdirs();
							file.createNewFile();
							InputStream in = fileItem.getInputStream();
							try {
								ResourceHelper.writeStreamToFile(in, file);
							} finally {
								ResourceHelper.closeResource(in);
							}
						}
						modify = true;
					}
				}
				setCurrentFile(newFileName);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (!isLight() && !fileName.equals(getCurrentFile())) {
			setCurrentFile(fileName);
			modify = true;
			setNeedRefresh(true);
		}

		return modify;
	}

	/* values */

	private void setCurrentLink(String link) {
		properties.setProperty("field." + getUnicName() + ".value.link", link);
	}

	public String getCurrentFolder() {
		return properties.getProperty("field." + getUnicName() + ".value.folder", "");
	}

	public void  setCurrentFolder(ContentContext ctx, String folder) {
		if (folder == null) {
			return;
		}
		if (folder.startsWith("/")) {
			folder = folder.substring(1);
		}
		properties.setProperty("field." + getUnicName() + ".value.folder", folder);
	}

	public String getCurrentFile() {
		return properties.getProperty("field." + getUnicName() + ".value.file", null);
	}

	public void setCurrentFile(String file) {
		if (file == null) {
			properties.remove("field." + getUnicName() + ".value.file");
		} else {
			properties.setProperty("field." + getUnicName() + ".value.file", file);
		}
	}

	protected String getCurrentLabel() {
		return properties.getProperty("field." + getUnicName() + ".value.label", "");
	}

	protected String getViewLabel() {
		String viewLabel = getCurrentLabel();
		if ((viewLabel == null) || (viewLabel.trim().length() == 0)) {
			viewLabel = getCurrentFile();
		}
		return viewLabel;
	}

	protected void setCurrentLabel(String label) {
		properties.setProperty("field." + getUnicName() + ".value.label", label);
	}

	protected String getFileURL(ContentContext ctx, String fileLink) {
		return URLHelper.mergePath(getFileTypeFolder(), getCurrentFolder(), fileLink);
	}

	@Override
	public boolean contains(ContentContext ctx, String inURI) {
		String uri = URLHelper.mergePath(getCurrentFolder(), getCurrentFile());
		uri = ElementaryURLHelper.mergePath(getFileDirectory(), uri);

		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		uri = ResourceHelper.extractResourceDir(staticConfig, globalContext, uri);

		/* clean path */
		inURI = inURI.replace('\\', '/').replaceAll("//", "/");
		uri = uri.replace('\\', '/').replaceAll("//", "/");

		return uri.equals(inURI);
	}

	@Override
	public Collection<Resource> getAllResources(ContentContext ctx) {
		Collection<Resource> outList = new LinkedList<Resource>();
		if (getCurrentFile() != null && getCurrentFile().trim().length() > 0) {
			String fileURI = getFileURL(ctx, getCurrentFile());
			Resource resource = new Resource();
			resource.setUri(fileURI);
			outList.add(resource);
		}
		return outList;
	}

	@Override
	public boolean renameResource(ContentContext ctx, File oldName, File newName) {
		if (oldName.equals(newName)) {
			return false;
		}
		String currentFile = ElementaryURLHelper.mergePath(getFileDirectory(), getCurrentFolder());
		currentFile = ElementaryURLHelper.mergePath(currentFile, getCurrentFile());
		File file = new File(currentFile);
		try {
			file = file.getCanonicalFile();
			oldName = oldName.getCanonicalFile();
			newName = newName.getCanonicalFile();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if (file.equals(oldName)) {
			String relativeNewFileDir = ResourceHelper.removePath(newName.getParentFile().getAbsolutePath(), getFileDirectory());
			setCurrentFile(newName.getName());
			setCurrentFolder(ctx, relativeNewFileDir);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Collection<Link> getAllResourcesLinks(ContentContext ctx) {
		Collection<Link> outList = new LinkedList<Link>();
		if (getCurrentFile() != null && getCurrentFile().trim().length() > 0) {
			String fileURI = getFileURL(ctx, getCurrentFile());
			outList.add(new Link(fileURI, getLabel(ctx, ctx.getLocale())));
		}
		return outList;
	}

	protected String getRelativeFileDirectory(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return staticConfig.getFileFolder();
	}

	protected StaticInfo getStaticInfo(ContentContext ctx) throws Exception {
		File file = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), getRelativeFileDirectory(ctx), getCurrentFolder(), getCurrentFile()));
		if (!file.exists()) {
			logger.warning("file not found : " + file + " (path=" + ctx.getPath() + " - site:" + ctx.getGlobalContext().getContextKey() + ")");
			return null;
		} else {
			return StaticInfo.getInstance(ctx, file);
		}
	}

	@Override
	public int getPopularity(ContentContext ctx) {
		StaticInfo staticInfo;
		try {
			staticInfo = StaticInfo.getInstance(ctx, getCurrentFile());
			if (staticInfo != null) {
				return staticInfo.getAccessFromSomeDays(ctx);
			}
		} catch (Exception e) {
		}
		return 0;
	}

	@Override
	public List<File> getFiles(ContentContext ctx) {
		// TODO Need implementation if necesary
		return Collections.EMPTY_LIST;
	}

	@Override
	public String getDirSelected(ContentContext ctx) {
		return null;
	}

	@Override
	public void setDirSelected(String dir) {
	}

	protected boolean isLight() {
		return StringHelper.isTrue(getPropertyValue("light", null));
	}

	protected String getRessourceURL(ContentContext ctx) {
		if (getCurrentFile() == null || getCurrentFile().trim().length() == 0) {
			return null;
		}
		String relativePath = URLHelper.mergePath(getFileTypeFolder(), getCurrentFolder());
		String fileURL = URLHelper.mergePath(relativePath, getCurrentFile());
		try {
			return URLHelper.createFileURL(ctx, '/' + fileURL);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected boolean isValueTranslatable() {
		return true;
	}

	public boolean transflateFrom(ContentContext ctx, ITranslator translator, String lang) {
		if (!isValueTranslatable()) {
			return false;
		} else {
			boolean translated = true;
			String newValue = translator.translate(ctx, getLabel(), lang, ctx.getRequestContentLanguage());
			if (newValue == null) {
				translated = false;
				newValue = ITranslator.ERROR_PREFIX + getValue();
			}
			setLabel(newValue);
			return translated;
		}
	}

}
