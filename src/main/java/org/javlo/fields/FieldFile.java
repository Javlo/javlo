package org.javlo.fields;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;
import org.javlo.bean.Link;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.filter.DirectoryFilter;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.Comparator.FileComparator;
import org.javlo.module.file.FileAction;
import org.javlo.service.RequestService;
import org.javlo.service.resource.Resource;
import org.javlo.ztatic.IStaticContainer;
import org.javlo.ztatic.StaticInfo;

public class FieldFile extends Field implements IStaticContainer {

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
		return this.properties.getProperty("field." + getName() + ".file.type", "file");
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

	protected List<String> getFolderListForSelection() {
		File dir = new File(getFileDirectory());
		List<String> list = new ArrayList<String>();
		list.add("");
		if (dir.exists()) {
			File[] files = dir.listFiles(new DirectoryFilter());
			if (!isCategoryRecursive()) {
				Comparator fileComparator = new FileComparator(FileComparator.NAME, true);
				Arrays.sort(files, fileComparator);
				for (File file : files) {
					list.add(file.getName());
				}
			} else {
				for (File file : ResourceHelper.getAllDirList(dir)) {
					String name = StringUtils.replaceOnce(file.getAbsolutePath(), dir.getAbsolutePath(), "");					
					list.add(name);
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

		out.println("<img" + cssClass + "  src=\"" + img + "\" alt=\"" + getCurrentFile() + " preview\"/>");
		if (title) {
			out.println("<div class=\"title\">" + getCurrentFile() + "</div>");
		}

		out.close();
		return writer.toString();
	}

	@Override
	public String getEditXHTMLCode(ContentContext ctx) throws Exception {
		
		String refCode = referenceEditCode(ctx);
		if (refCode != null) {
			return refCode;
		}
		
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		out.println("<div class=\"form-group field-"+getName()+"\">");
		out.println("<fieldset>");
		out.println("<legend>" + getLabel(new Locale(globalContext.getEditLanguage(ctx.getRequest().getSession()))) + "</legend>");
		out.println("<div class=\"commands\">");

		out.println("<div class=\"row form-group\"><div class=\"col-sm-3\">");
		out.println("<label for=\"" + getInputCreateFolderName() + "\">" + getCreateFolderLabel() + " : </label>");
		out.println("</div><div class=\"col-sm-8\"><input class=\"form-control\" type=\"text\" id=\"" + getInputCreateFolderName() + "\" name=\"" + getInputCreateFolderName() + "\" /></div>");
		out.println("<div class=\"col-sm-1\"><input type=\"submit\" class=\"ajax_update_click btn btn-default btn-xs pull-right\" name=\"create\" value=\">>\" />");
		out.println("</div></div>");
		
		String linkToResources = "";
		if (!ctx.getGlobalContext().isMailingPlatform()) {			
			Map<String, String> filesParams = new HashMap<String, String>();
			String path = URLHelper.mergePath(FileAction.getPathPrefix(ctx), StaticConfig.getInstance(ctx.getRequest().getSession()).getImageFolderName(), getCurrentFolder());
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
			
			linkToResources = "<div class=\"col-sm-3\"><a class=\"browse-link btn btn-default btn-xs\" href=\"" + URLHelper.addParam(staticLinkURL, "select", "back") + "\">" + i18nAccess.getText("content.goto-static") + "</a></div>";
		}

		out.println("<div class=\"row form-group\"><div class=\"col-sm-3\">");
		out.println("<label for=\"" + getInputFolderName() + "\">" + getFolderLabel() + " : </label></div><div class=\"col-sm-6\">");
		out.println(XHTMLHelper.getInputOneSelect(getInputFolderName(), getFolderListForSelection(), getCurrentFolder(), "form-control", "jQuery(this.form).trigger('submit');", true));
		out.println("</div>"+linkToResources+"</div>");
		
		out.println("<div class=\"row form-group\"><div class=\"col-sm-3\">");
		out.println("<label for=\"" + getInputFileName() + "\">" + getFileLabel() + " : </label></div><div class=\"col-sm-9\">");
		out.println(XHTMLHelper.getInputOneSelect(getInputFileName(), getFileList(), getCurrentFile(), "form-control", "jQuery(this.form).trigger('submit');", true));
		out.println("</div></div>");

		out.println("<div class=\"row form-group\"><div class=\"col-sm-3\">");
		out.println("<label for=\"" + getInputLabelFileName() + "\">" + getLabelLabel() + " : </label>");
		out.println("</div><div class=\"col-sm-9\"><input class=\"form-control\" type=\"text\" id=\"" + getInputLabelFileName() + "\" name=\"" + getInputLabelFileName() + "\" value=\"" + getCurrentLabel() + "\" />");
		out.println("</div></div>");

		out.println("<div class=\"row form-group\"><div class=\"col-sm-3\">");
		out.println("<label for=\"" + getInputAddFileName() + "\">" + getAddFileLabel() + " : </label>");
		out.println("</div><div class=\"col-sm-3\"><input type=\"file\" id=\"" + getInputAddFileName() + "\" name=\"" + getInputAddFileName() + "\" /></div>");
		out.println("</div>");
		

		if (isWithLink()) {
			out.println("<div class=\"row form-group\"><div class=\"col-sm-3\">");
			out.println("<label for=\"" + getInputLabelLinkName() + "\">" + getLinkLabel() + " : </label>");
			out.println("</div><div class=\"col-sm-9\"><input class=\"form-control\" type=\"text\" id=\"" + getInputLabelLinkName() + "\" name=\"" + getInputLabelLinkName() + "\" value=\"" + getCurrentLink() + "\" />");
			out.println("</div></div>");
		}

		out.println("</div>");
		out.println("<div class=\"preview\">");
		out.println(getPreviewCode(ctx, true));
		out.println("</div>");
		out.println("</fieldset></div>");

		out.close();
		return writer.toString();
	}
	
	/**
	 * render the field when he is used as reference value in a other language.
	 * @param ctx
	 * @return
	 * @throws Exception 
	 */
	protected String getReferenceFieldView(ContentContext ctx) throws Exception {
		return "<div class=\"slave-field line form-group\"><label>"+getLabel(new Locale(ctx.getContextRequestLanguage()))+"</label>"+getPreviewCode(ctx, false)+"</div>";
	}

	public String getCurrentLink() {
		return properties.getProperty("field." + getUnicName() + ".value.link", "");
	}

	protected String getInputLabelLinkName() {
		return getName() + "-link-" + getId();
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
		String fileURL = URLHelper.createResourceURL(ctx, URLHelper.mergePath(relativePath, getCurrentFile()));

		out.println("<div class=\"" + getType() + "\">");
		out.println("<a href=\"" + fileURL + "\">" + getViewLabel() + "</a>");
		out.println("</div>");

		out.close();
		return writer.toString();
	}

	@Override
	public String getType() {
		return "file";
	}

	public String getURL(ContentContext ctx) {
		String relativePath = URLHelper.mergePath(getFileTypeFolder(), getCurrentFolder());
		return URLHelper.createResourceURL(ctx, URLHelper.mergePath(relativePath, getCurrentFile()));
	}

	@Override
	public boolean process(HttpServletRequest request) {
		RequestService requestService = RequestService.getInstance(request);
		boolean modify = false;

		String newFolderName = requestService.getParameter(getInputCreateFolderName(), "");
		String folder = requestService.getParameter(getInputFolderName(), "");
		String fileName = requestService.getParameter(getInputFileName(), "");
		String newFileName = requestService.getParameter(getInputAddFileName(), "");
		String label = requestService.getParameter(getInputLabelFileName(), null);
		String link = requestService.getParameter(getInputLabelLinkName(), null);

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

		String currentFolder = getCurrentFolder();
		if (newFolderName.trim().length() > 0) {
			File newFolder = new File(URLHelper.mergePath(getFileDirectory(), newFolderName));
			newFolder.mkdirs();
			if (!getCurrentFolder().equals(newFolderName)) {
				setCurrentFolder(newFolderName);
				setCurrentFile("");
				setCurrentLabel("");
				newFileName = "";
				fileName = "";
				modify = true;
				setNeedRefresh(true);
			}
		} else if (!currentFolder.equals(folder)) {
			setCurrentFolder(folder);
			setCurrentFile("");
			setCurrentLabel("");
			newFileName = "";
			fileName = "";
			modify = true;
			setNeedRefresh(true);
		}

		if (newFileName.trim().length() > 0) {
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

		} else if (!fileName.equals(getCurrentFile())) {
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

	public void setCurrentFolder(String folder) {
		properties.setProperty("field." + getUnicName() + ".value.folder", folder);
	}

	public String getCurrentFile() {
		return properties.getProperty("field." + getUnicName() + ".value.file", null);
	}

	public void setCurrentFile(String file) {
		properties.setProperty("field." + getUnicName() + ".value.file", file);
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
		if (file.equals(oldName)) {
			String relativeNewFileDir = newName.getParentFile().getAbsolutePath().replace(getFileDirectory(), "");
			if (relativeNewFileDir.length() == newName.getParentFile().getAbsolutePath().length()) {
				return false;
			}
			setCurrentFile(newName.getName());
			setCurrentFolder(relativeNewFileDir);
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
			outList.add(new Link(fileURI, getLabel(new Locale(ctx.getRequestContentLanguage()))));
		}
		return outList;
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
	public String getDirSelected() {
		return null;
	}
	
	@Override
	public void setDirSelected(String dir) {
	}

}
