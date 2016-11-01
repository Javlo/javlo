/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.javlo.actions.IAction;
import org.javlo.bean.Link;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.ILink;
import org.javlo.component.core.IReverseLinkComponent;
import org.javlo.component.core.IUploadResource;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.filter.ZIPFilter;
import org.javlo.helper.ArrayHelper;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.LocalLogger;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.Comparator.FileComparator;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.file.FileAction;
import org.javlo.module.file.FileBean;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.service.ReverseLinkService;
import org.javlo.service.resource.LocalResource;
import org.javlo.service.resource.Resource;
import org.javlo.service.resource.ResourceStatus;
import org.javlo.servlet.AccessServlet;
import org.javlo.user.AdminUserSecurity;
import org.javlo.ztatic.IStaticContainer;
import org.javlo.ztatic.StaticInfo;
import org.owasp.encoder.Encode;

/**
 * Abstract component for access to a file (file,image...)
 * <h4>exposed variable :</h4>
 * <ul>
 * <li>inherited from {@link AbstractVisualComponent}</li>
 * <li>{@link String} url : url to resource.</li>
 * <li>{@link String} description : description of the resource.</li>
 * <li>{@link String} label : label defined by contributor.</li>
 * <li>{@link boolean} blank : true if link must be open as popup.</li>
 * <li>{@link StaticInfo} resource : static info of resource.</li>
 * </ul>
 * 
 * @author pvandermaesen
 */
public class AbstractFileComponent extends AbstractVisualComponent implements IStaticContainer, ILink, IUploadResource, IAction {

	static final String HEADER_V1_0 = "file storage V.1.1";

	public static final String LABEL_KEY = "label";

	public static final String DIR_KEY = "dir";

	public static final String FILE_NAME_KEY = "file-name";

	public static final String DESCRIPTION_KEY = "description";

	public static final String EMBED_CODE_KEY = "embed-code";

	protected static final String REVERSE_LINK_KEY = "reverse-lnk";

	protected static final String ENCODING_KEY = "encoding";

	protected static final String DEFAULT_ENCODING = "default";

	public static final String TYPE = "abstractfile";

	protected Properties properties = new Properties();

	public AbstractFileComponent() {
		super();
		properties.setProperty(LABEL_KEY, "");
		properties.setProperty(FILE_NAME_KEY, "");
		properties.setProperty(DESCRIPTION_KEY, "");
		properties.setProperty(ENCODING_KEY, DEFAULT_ENCODING);
		properties.setProperty(REVERSE_LINK_KEY, ReverseLinkService.NONE);
	}

	protected boolean canUpload(ContentContext ctx) {
		return AdminUserSecurity.isCurrentUserCanUpload(ctx);
	}

	@Override
	public boolean contains(ContentContext ctx, String inURI) {
		String uri = ElementaryURLHelper.mergePath(getDirSelected(), getFileName());
		uri = ElementaryURLHelper.mergePath(getFileDirectory(ctx), uri);

		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		GlobalContext globalContext = ctx.getGlobalContext();
		uri = ResourceHelper.extractResourceDir(staticConfig, globalContext, uri);

		/* clean path */
		inURI = inURI.replace('\\', '/').replaceAll("//", "/");
		uri = uri.replace('\\', '/').replaceAll("//", "/");

		return uri.equals(inURI);
	}

	public String createFileURL(ContentContext ctx, String url) {
		throw new NotImplementedException("createFileURL");
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AbstractFileComponent)) {
			return false;
		}
		AbstractFileComponent comp = (AbstractFileComponent) obj;

		boolean eq = getComponentBean().getStyle().equals(comp.getComponentBean().getStyle());
		eq = eq && getComponentBean().isList() == comp.getComponentBean().isList();
		eq = eq && getComponentBean().isRepeat() == comp.getComponentBean().isRepeat();
		eq = eq && getComponentBean().getLanguage().equals(comp.getComponentBean().getLanguage());
		eq = properties.equals(comp.properties);

		return eq;
	}

	protected boolean expandZip() {
		return false;
	}

	@Override
	public String getURL(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		String fileLink = URLHelper.mergePath(getDirSelected(), getFileName());
		return URLHelper.createResourceURL(ctx, getPage(), staticConfig.getImageFolder() + '/' + fileLink).replace('\\', '/');
	}

	@Override
	public boolean isLinkValid(ContentContext ctx) {
		return !StringHelper.isEmpty(getURL(ctx));
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		String url = getURL(ctx);
		if (url != null && url.startsWith('/' + ctx.getGlobalContext().getStaticConfig().getStaticFolder())) {
			url = URLHelper.createResourceURL(ctx, url);
		}
		ctx.getRequest().setAttribute("url", url);
		ctx.getRequest().setAttribute("linkToImage", StringHelper.isImage(url));
		ctx.getRequest().setAttribute("blank", ctx.getGlobalContext().isOpenExternalLinkAsPopup(url));
		ctx.getRequest().setAttribute("descritpion", getDescription());
		ctx.getRequest().setAttribute("cleanDescription", Encode.forHtmlAttribute(getDescription()));
		StaticInfo staticInfo = getStaticInfo(ctx);
		String cleanLabel = null;
		if (staticInfo != null) {
			cleanLabel = StringHelper.toHTMLAttribute(StringHelper.removeTag(staticInfo.getTitle(ctx)));
			if (!StringHelper.isEmpty(staticInfo.getCopyright(ctx))) {
				ctx.getRequest().setAttribute("copyright", staticInfo.getCopyright(ctx));
			}
			ctx.getRequest().setAttribute("resourceLabel", staticInfo.getTitle(ctx));
			ctx.getRequest().setAttribute("resourceCleanLabel", cleanLabel);
		}
		if (getLabel() != null && getLabel().length() > 0) {
			ctx.getRequest().setAttribute("label", getLabel());
			ctx.getRequest().setAttribute("cleanLabel", StringHelper.toXMLAttribute(StringHelper.removeTag(getLabel())));
			ctx.getRequest().setAttribute("htmlLabel", XHTMLHelper.textToXHTML(XHTMLHelper.autoLink(getLabel())));
		} else if (staticInfo != null) {
			ctx.getRequest().setAttribute("label", staticInfo.getTitle(ctx));
			ctx.getRequest().setAttribute("cleanLabel", cleanLabel);
			ctx.getRequest().setAttribute("htmlLabel", XHTMLHelper.textToXHTML(XHTMLHelper.autoLink(getLabel())));
			ctx.getRequest().setAttribute("resource", staticInfo);
		}

		ctx.getRequest().setAttribute("resource", staticInfo);

	}

	/**
	 * @param stream
	 */
	private void expandZip(ContentContext ctx, ZipInputStream stream) throws Exception {
		ZipEntry entry = stream.getNextEntry();
		while (entry != null) {
			saveFile(ctx, entry.getName(), stream);
			entry = stream.getNextEntry();
		}
	}

	@Override
	public Collection<Resource> getAllResources(ContentContext ctx) {
		Collection<Resource> outList = new LinkedList<Resource>();
		if (getFileName() != null && getFileName().trim().length() > 0) {
			String fileURI = getFileURL(ctx, getFileName());
			Resource resource = new Resource();
			resource.setUri(fileURI);
			outList.add(resource);
		}
		return outList;
	}

	@Override
	public Collection<Link> getAllResourcesLinks(ContentContext ctx) {
		Collection<Link> outList = new LinkedList<Link>();
		if (getFileName() != null && getFileName().trim().length() > 0) {
			String desc = getDescription();
			if (desc == null || desc.trim().length() == 0) {
				desc = getFileName();
			}
			String url = URLHelper.createResourceURL(ctx, getFileURL(ctx, getFileName()));
			Link link = new Link(url, desc);
			outList.add(link);
		}
		return outList;
	}

	protected String getCSSType() {
		return "file";
	}

	protected String getDeleteTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-image.delete-file");
	}

	public String getDescription() {
		return properties.getProperty(DESCRIPTION_KEY, "");
	}

	public String getEmbedCode() {
		return properties.getProperty(EMBED_CODE_KEY, "");
	}

	public void setEmbedCode(String embedCode) {
		properties.setProperty(EMBED_CODE_KEY, embedCode);
	}

	public String getDescriptionName() {
		return getId() + ID_SEPARATOR + "description";
	}

	public String getEmbedCodeName() {
		return getId() + ID_SEPARATOR + "embed-code";
	}

	protected String getDirInputName() {
		return "dir_name" + ID_SEPARATOR + getId();
	}

	protected String getDirLabelTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-image.dir");
	}

	protected String[] getDirList(ContentContext ctx, String inFolder) throws Exception {
		File folder = new File(inFolder);
		Collection<File> sourceChildren = ResourceHelper.getAllDirList(folder);
		Collection<String> children = new LinkedList<String>();
		String importFolder = ctx.getGlobalContext().getStaticConfig().getImportFolder();
		if (importFolder.length() > 1 && importFolder.startsWith("/")) {
			importFolder = importFolder.substring(1);
		}
		String currentImportFolder = getImportFolderPath(ctx);
		for (File dir : sourceChildren) {
			String child = StringUtils.replace(dir.getAbsolutePath(), folder.getAbsolutePath(), "").replace('\\', '/');
			if (child.length() > 1 && child.startsWith("/")) {
				child = child.substring(1);
			}
			if (!child.startsWith(importFolder) || child.startsWith(currentImportFolder)) {
				children.add(child);
			}
		}
		String[] folders = new String[children.size()];
		int i = 0;
		for (String dir : children) {
			folders[i] = dir;
			i++;
		}
		return folders;
	}

	public String getDirSelected() {
		String dir = properties.getProperty(DIR_KEY, "");
		if (dir.length() > 1 && dir.startsWith("/")) {
			dir = dir.substring(1);
			setDirSelected(dir);
		}
		return dir;
	}

	@Override
	protected String getEditorComplexity(ContentContext ctx) {
		return properties.getProperty("editor-complexity", "light");
	}

	protected String getMetaCode(ContentContext ctx) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		StaticInfo staticInfo = getStaticInfo(ctx);
		if (isDisplayMeta(ctx) && staticInfo != null && staticInfo.getFile().exists() && staticInfo.getFile().isFile()) {
			String url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE));
			if (!ctx.isEditPreview()) {
				String formAction = URLHelper.addParam(url, "module", "content");
				url = URLHelper.addParam(url, "formAction", formAction);
			} else {
				String formAction = URLHelper.addParam(url, "module", "content");
				formAction = URLHelper.addParam(formAction, "previewEdit", "true");
				formAction = URLHelper.addParam(formAction, "webaction", "edit.editPreview");
				formAction = URLHelper.addParam(formAction, "comp_id", getId());
				url = URLHelper.addParam(url, "formAction", formAction);
			}
			url = URLHelper.addParam(url, "nobreadcrumbs", "true");
			url = URLHelper.addParam(url, "webaction", "file.previewEdit");
			url = URLHelper.addParam(url, "module", "file");
			url = URLHelper.addParam(url, "file", URLHelper.encodePathForAttribute(staticInfo.getFile().getPath()));

			I18nAccess i18nAccess;
			try {
				i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				String button = "";
				if (canUpload(ctx)) {
					button = "<a href=\"" + url + "\" class=\"btn btn-default btn-xs pull-right\">" + i18nAccess.getText("global.change") + "</a>";
				}
				out.println("<div class=\"panel panel-default\"><div class=\"panel-heading\">Meta-data" + button + "</h3></div><div class=\"panel-body\">");
				out.println("<div class=\"row form-group\"><div class=\"col-sm-3\">" + i18nAccess.getText("field.title") + "</div>");
				out.println("<div class=\"col-sm-9\">" + staticInfo.getTitle(ctx) + "</div>");
				out.println("</div>");
				out.println("<div class=\"row form-group\"><div class=\"col-sm-3\">" + i18nAccess.getText("field.description") + "</div>");
				out.println("<div class=\"col-sm-9\">" + staticInfo.getDescription(ctx) + "</div>");
				out.println("</div>");
				out.println("<div class=\"row form-group\"><div class=\"col-sm-3\">" + i18nAccess.getText("field.location") + "</div>");
				out.println("<div class=\"col-sm-9\">" + staticInfo.getLocation(ctx) + "</div>");
				out.println("</div>");
				out.println("<div class=\"row form-group\"><div class=\"col-sm-3\">" + i18nAccess.getText("field.copyright") + "</div>");
				out.println("<div class=\"col-sm-9\">" + staticInfo.getCopyright(ctx) + "</div>");
				out.println("</div>");
				out.println("<div class=\"row form-group\"><div class=\"col-sm-3\">" + i18nAccess.getText("field.date") + "</div>");
				out.println("<div class=\"col-sm-9\">" + StringHelper.renderDate(staticInfo.getDate(ctx)) + "</div>");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			out.println("</div></div></div>");
		}
		out.close();
		return new String(outStream.toByteArray());
	}

	protected boolean isDisplayMeta(ContentContext ctx) {
		return ctx.getGlobalContext().getStaticConfig().isImageMetaEdition();
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getSpecialInputTag());

		finalCode.append("<div class=\"row\"><div class=\"col-md-4 file-preview\">");

		finalCode.append(getPreviewCode(ctx));

		finalCode.append("</div><div class=\"col-md-8\">");

		if (this instanceof IReverseLinkComponent && isReversedLink(ctx)) {
			/*
			 * finalCode.append("<div class=\"line\">");
			 * finalCode.append(XHTMLHelper
			 * .getCheckbox(getReverseLinkInputName(), isReverseLink()));
			 * finalCode.append("<label for=\"" + getReverseLinkInputName() +
			 * "\">" + getReverseLinkeLabelTitle(ctx) + "</label>");
			 * finalCode.append("</div>");
			 */

			finalCode.append("<div class=\"form-group\">");
			String reverseLink = properties.getProperty(REVERSE_LINK_KEY, ReverseLinkService.NONE);

			// 1.3 to 1.4 conversion from legacy value "true" to corresponding
			// "all"
			if (StringHelper.isTrue(reverseLink)) {
				reverseLink = ReverseLinkService.ALL;
			}
			finalCode.append("<label for=\"" + getReverseLinkInputName() + "\">" + getReverseLinkeLabelTitle(ctx) + " : </label>");
			finalCode.append(XHTMLHelper.getReverlinkSelectType(ctx, getReverseLinkInputName(), reverseLink));
			finalCode.append("</div>");
		}
		finalCode.append("<div class=\"form-group\">");
		finalCode.append("<label for=\"" + getLabelXHTMLInputName() + "\">" + getImageLabelTitle(ctx) + " : </label>");
		String[][] params = { { "rows", "1" }, { "class", "form-control" } };
		finalCode.append(XHTMLHelper.getTextArea(getLabelXHTMLInputName(), getLabel(), params));
		finalCode.append("</div>");

		if ((getDirList(ctx, getFileDirectory(ctx)) != null) && (getDirList(ctx, getFileDirectory(ctx)).length > 0)) {
			finalCode.append("<div class=\"form-group\"><label for=\"" + getDirInputName() + "\">");
			finalCode.append(getDirLabelTitle(ctx));
			finalCode.append(" : </label>");
			finalCode.append(XHTMLHelper.getInputOneSelect(getDirInputName(), ArrayHelper.addFirstElem(getDirList(ctx, getFileDirectory(ctx)), ""), getDirSelected(), "form-control", getJSOnChange(ctx), true));
			finalCode.append("</div>");
		}

		if (needEncoding()) {
			finalCode.append("<div class=\"form-group\">");
			GlobalContext globalContext = ctx.getGlobalContext();
			finalCode.append("<label for=\"" + getEncodingXHTMLInputName() + "\">" + i18nAccess.getText("content.file.encoding") + " : </label>");
			String[] encodings = new String[globalContext.getEncodings().size() + 1];
			encodings[0] = "default";
			int i = 1;
			for (String encoding : globalContext.getEncodings()) {
				encodings[i] = encoding;
				i++;
			}
			finalCode.append(XHTMLHelper.getInputOneSelect(getEncodingXHTMLInputName(), encodings, getEncoding(), "form-control", null, false));
			finalCode.append("</div>");
		}

		boolean canUploadInImport = false;

		canUploadInImport = getDirSelected().equals(getImportFolderPath(ctx));
		if (canUpload(ctx)) {
			finalCode.append("<div class=\"row\"><div class=\"col-md-6\">");
			finalCode.append("<div class=\"form-group\"><label for=\"new_dir_" + getId() + "\">");
			finalCode.append(getNewDirLabelTitle(ctx));
			finalCode.append(" : </label><input class=\"form-control\" id=\"new_dir_" + getId() + "\" name=\"" + getNewDirInputName() + "\" type=\"text\"/></div>");
			finalCode.append("</div>");
		}
		if (canUpload(ctx) || canUploadInImport) {
			if (!canUpload(ctx)) {
				finalCode.append("<div class=\"row\">");
			}
			finalCode.append("<div class=\"col-md-6\">");
			finalCode.append("<div class=\"form-group\">");
			finalCode.append("<label for=\"" + getFileXHTMLInputName() + "\">" + getImageUploadTitle(ctx) + "</label>");
			finalCode.append("<input class=\"form-control\" name=\"" + getFileXHTMLInputName() + "\" id=\"" + getFileXHTMLInputName() + "\" type=\"file\"/></div>");
			finalCode.append("</div></div>");
		}

		String[] fileList = getFileList(getFileDirectory(ctx), getFileFilter());
		if (fileList.length > 0) {
			finalCode.append("<div class=\"form-group\">");
			finalCode.append("<label for=\"" + getSelectXHTMLInputName() + "\">");
			finalCode.append(getImageChangeTitle(ctx));
			finalCode.append("</label>");

			String[] fileListBlanck = new String[fileList.length + 1];
			fileListBlanck[0] = "";
			System.arraycopy(fileList, 0, fileListBlanck, 1, fileList.length);

			finalCode.append(XHTMLHelper.getInputOneSelect(getSelectXHTMLInputName(), fileListBlanck, getFileName(), "form-control", getJSOnChange(ctx), true));

			if (ctx.getRenderMode() == ContentContext.EDIT_MODE && !ctx.isEditPreview() && canUpload(ctx)) {
				if (isLinkToStatic()) {
					Map<String, String> filesParams = new HashMap<String, String>();
					filesParams.put("path", URLHelper.mergePath("/", getRelativeFileDirectory(ctx), getDirSelected()));
					String staticURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams);

					finalCode.append("<a class=\"" + IContentVisualComponent.EDIT_ACTION_CSS_CLASS + "\" href=\"" + staticURL + "\" >");
					finalCode.append(i18nAccess.getText("content.goto-static"));
					finalCode.append("</a>");
				}
			}
			finalCode.append("</div>");
		}

		if (isWithDescription()) {
			String descriptionTitle = i18nAccess.getText("component.link.description");
			finalCode.append("<div class=\"description form-group\">");
			finalCode.append("<label for=\"" + getEmbedCode() + "\">");
			finalCode.append(descriptionTitle);
			finalCode.append("</label>");
			finalCode.append("<textarea class=\"form-control tinymce-light wysiwyg\" id=\"" + getDescriptionName() + "\" name=\"" + getDescriptionName() + "\">");
			finalCode.append(getDescription());
			finalCode.append("</textarea></div>");
			Map<String, String> filesParams = new HashMap<String, String>();
			String path = FileAction.getPathPrefix(ctx);
			filesParams.put("path", path);
			filesParams.put("webaction", "changeRenderer");
			filesParams.put("page", "meta");
			filesParams.put("select", "_TYPE_");
			filesParams.put(ContentContext.PREVIEW_EDIT_PARAM, "true");
			String chooseImageURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams);
			finalCode.append("<script type=\"text/javascript\">jQuery(document).ready(loadWysiwyg('#" + getDescriptionName() + "','" + getEditorComplexity(ctx) + "','" + chooseImageURL + "'));</script>");
		}

		finalCode.append(getMetaCode(ctx));

		finalCode.append("</div></div>");

		// validation
		if (!isFileNameValid(ctx, getFileName())) {
			setMessage(new GenericMessage(i18nAccess.getText("component.error.file"), GenericMessage.ERROR));
		}

		return finalCode.toString();
	}

	public String getEncoding() {
		return properties.getProperty(ENCODING_KEY);
	}

	protected String getEncodingXHTMLInputName() {
		return "encoding" + ID_SEPARATOR + getId();
	}

	public String getFileDirectory(ContentContext ctx) {
		throw new NotImplementedException("getFileDirectory");
	}

	protected String[] getFileList(String directory) {
		return getFileList(directory, null);
	}

	protected FilenameFilter getFileFilter() {
		return null;
	}

	protected FilenameFilter getDecorationFilter() {
		return null;
	}

	protected String[] getFileList(String directory, FilenameFilter filter) {
		File dir = new File(ElementaryURLHelper.mergePath(directory, getDirSelected()));
		String[] res = new String[0];
		if (dir.exists()) {
			File[] files = dir.listFiles(filter);

			Comparator fileComparator = new FileComparator(FileComparator.LASTMODIFIED, true);
			try {
				Arrays.sort(files, fileComparator);
			} catch (Throwable t) { // TODO: try to remove this.
				t.printStackTrace();
			}

			ArrayList list = new ArrayList();
			for (File file : files) {
				if (file.isFile()) {
					list.add(file.getName());
				}
				res = new String[list.size()];
				list.toArray(res);
			}
		}
		Arrays.sort(res);
		return res;
	}

	public String getFileName() {
		return properties.getProperty(FILE_NAME_KEY, "");
	}

	protected String getFileURL(ContentContext ctx, String fileLink) {
		return URLHelper.mergePath("/", getRelativeFileDirectory(ctx), ElementaryURLHelper.mergePath(getDirSelected(), fileLink));
	}

	protected String getFileXHTMLInputName() {
		return "selection" + ID_SEPARATOR + getId();
	}

	protected String getDecoImageXHTMLInputName() {
		return "image_deco_" + ID_SEPARATOR + getId();
	}

	protected String getDecoImageFileXHTMLInputName() {
		return "image_deco_file_" + ID_SEPARATOR + getId();
	}

	protected String getImageChangeTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-image.change");
	}

	protected String getImageLabelTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-image.label");
	}

	protected String getImageUploadTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-image.add");
	}

	protected String getFileUploadActionTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("content.upload");
	}

	protected String getImageDecorativeTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-image.add");
	}

	protected String getImageSelectTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("global.select");
	}

	public String getJSOnChange(ContentContext ctx) {
		String js = "";
		if (ctx.isEditPreview()) {
			js = "jQuery(this.form).append('<input type=&quot;hidden&quot; name=&quot;" + AccessServlet.PERSISTENCE_PARAM + "&quot; value=&quot;false&quot; />');";
		}
		return js + "jQuery(this.form).trigger('submit')";
	}

	public String getLabel() {
		return properties.getProperty(LABEL_KEY, "");
	}

	protected String getLabelXHTMLInputName() {
		return getId() + ID_SEPARATOR + "label_name";
	}

	protected String getTextAutoInputName() {
		return getId() + ID_SEPARATOR + "manuel";
	}

	protected String getFirstTextInputName() {
		return getId() + ID_SEPARATOR + "first_text";
	}

	protected String getSecondTextInputName() {
		return getId() + ID_SEPARATOR + "second_text";
	}

	protected String getNewDirInputName() {
		return getId() + ID_SEPARATOR + "new_dir_name";
	}

	protected String getNewDirLabelTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-image.new-dir");
	}

	public String getImageImgName() {
		return "img_images_" + getId();
	}

	public String getResourceURL(ContentContext ctx) {
		return getResourceURL(ctx, getFileName());
	}

	protected String getMainFolder(ContentContext ctx) {
		throw new NotImplementedException("getMainFolder");
	}

	public String getResourceURL(ContentContext ctx, String fileLink) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		if (isFromShared(ctx)) {
			return URLHelper.mergePath(staticConfig.getShareDataFolderKey(), getMainFolder(ctx), getDirSelected(), fileLink.replaceFirst(staticConfig.getShareDataFolderKey(), ""));
		} else {
			return URLHelper.mergePath(staticConfig.getStaticFolder(), getMainFolder(ctx), URLHelper.mergePath(getDirSelected(), fileLink));
		}
	}

	protected int getMaxPreviewImages() {
		return Integer.MAX_VALUE;
	}

	protected String getPreviewZoneId() {
		return "picture-zone-" + getId();
	}

	protected String getPreviewCode(ContentContext ctx) throws Exception {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		out.println("<div id=\"" + getPreviewZoneId() + "\" class=\"selected-zone\">");
		out.println(getPreviewCode(ctx, getMaxPreviewImages()));
		out.println("</div>");

		out.close();
		return res.toString();
	}

	public File getFile(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String fullName = ElementaryURLHelper.mergePath(getDirSelected(), getFileName());
		fullName = ElementaryURLHelper.mergePath(staticConfig.getFileFolder(), fullName);
		fullName = ElementaryURLHelper.mergePath(globalContext.getDataFolder(), fullName);
		return new File(fullName);
	}

	public String getPreviewCode(ContentContext ctx, int maxDisplayedImage) throws Exception {
		return getPreviewCode(ctx, maxDisplayedImage, false);
	}

	public String getPreviewCode(ContentContext ctx, int maxDisplayedImage, boolean imageList) throws Exception {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		String[] images = getFileList(getFileDirectory(ctx));
		String currentFileLink = URLHelper.mergePath(getDirSelected(), getFileName());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		out.println("<div class=\"preview-image-wrapper " + (imageList ? "list" : "no-list") + "\">");

		FileBean file = new FileBean(ctx, getFile(ctx));
		Map<String, String> params = new HashMap<String, String>();
		params.put("webaction", "edit.save");
		params.put("components", getId());
		params.put("id-" + getId(), "true");
		params.put(getFileXHTMLInputName(), "file.png"); // fake file name
		params.put(getDirInputName(), getDirSelected()); // fake file name
		String uploadURL = URLHelper.createURL(ctx, params);
		out.println("<div class=\"image-selected\" data-fieldname=\"" + getFileXHTMLInputName() + "\" data-url=\"" + uploadURL + "\">");

		out.println("<div class=\"focus-zone\">");

		out.println("<div id=\"" + getPreviewZoneId() + "\" class=\"list-container\">");

		String url;
		if (getFileName().trim().length() > 0) {
			url = URLHelper.createTransformURL(ctx, getPage(), getResourceURL(ctx, getFileName()), "list");
			url = URLHelper.addParam(url, "hash", getStaticInfo(ctx).getVersionHash(ctx));
			if (isFromShared(ctx)) {
				out.println("<img src=\"" + url + "\" />&nbsp;");
			} else if (!isFromShared(ctx)) {
				out.println("<div class=\"focus-image-wrapper\"><img src=\"" + url + "\" />");
				out.println("<div class=\"focus-point\">x</div>");
				out.println("<input class=\"posx\" type=\"hidden\" name=\"posx-" + file.getId() + "\" value=\"" + file.getFocusZoneX() + "\" />");
				out.println("<input class=\"posy\" type=\"hidden\" name=\"posy-" + file.getId() + "\" value=\"" + file.getFocusZoneY() + "\" />");
				out.println("<input class=\"path\" type=\"hidden\" name=\"image_path-" + file.getId() + "\" value=\"" + URLHelper.mergePath(getRelativeFileDirectory(ctx), getDirSelected()) + "\" /></div>&nbsp;");
			}
		} else {
			imageList = true;
		}
		out.println("</div></div>");
		if (!isFromShared(ctx)) {
			out.println("<script type=\"text/javascript\">initFocusPoint();</script>");
		}
		if (imageList) {
			out.println("<div class=\"name\">" + getFileName() + "</div>");
			out.println("<div class=\"image-list\">");
			for (String image : images) {
				if ((image != null) && (image.trim().length() > 0)) {
					StaticInfo staticInfo = StaticInfo.getInstance(ctx, getFileURL(ctx, image));
					String fileLink = URLHelper.mergePath(getDirSelected(), image);
					String selected = "class=\"preview-image\"";
					if (fileLink.equals(currentFileLink)) {
						selected = " class=\"preview-image selected\"";
					}
					String realURL = URLHelper.createResourceURL(ctx, getPage(), '/' + getResourceURL(ctx, image));
					realURL = URLHelper.addParam(realURL, "CRC32", "" + staticInfo.getCRC32());
					String previewURL = URLHelper.createTransformURL(ctx, getPage(), getResourceURL(ctx, image), "preview");
					previewURL = URLHelper.addParam(previewURL, "CRC32", "" + staticInfo.getCRC32());
					url = URLHelper.createTransformURL(ctx, getPage(), getResourceURL(ctx, image), "list");
					url = URLHelper.addParam(url, "hash", staticInfo.getVersionHash(ctx));
					String id = "image_name_select__" + getId();
					// if (i < maxDisplayedImage || isSelectedImage) {
					out.print("<div " + selected + ">");
					String onMouseOver = "";
					if (globalContext.isImagePreview()) {
						onMouseOver = " onMouseOver=\"previewImage('" + previewURL + "')\" onMouseOut=\"previewClear()\"";
					}
					out.print("<figure><a class=\"image\" href=\"#\" onclick=\"jQuery('#" + id + "').val('" + image + "');jQuery('#" + id + "').trigger('change');" + getJSOnChange(ctx) + "\">");
					out.print("<img name=\"" + getImageImgName() + "\"" + onMouseOver + " src=\"");
					out.print(url);
					out.print("\" alt=\"\">&nbsp;</a>");
					out.print("<figcaption><a target=\"_blank\" title=\"" + image + "\" href=\"" + realURL + "\">" + image + "</a></figcaption></figure>");
					out.print("</div>");
					// }
				}
			}
			out.println("</div>");
		} else {
			params = new HashMap<String, String>();
			params.put("webaction", "abstractfile.loadImages");
			params.put("comp_id", getId());
			String ajaxURL = URLHelper.createAjaxURL(ctx, params);
			if (ctx.isEditPreview()) {
				ajaxURL = URLHelper.addParam(ajaxURL, ContentContext.PREVIEW_EDIT_PARAM, "true");
			}
			out.println("<div class=\"action\">");
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			out.println("<a class=\"action-button ajax\" href=\"" + ajaxURL + "\">" + getDisplayAllLabel(i18nAccess) + "</a>");
			out.println("</div>");
		}
		out.println("</div>");
		out.println("</div>");
		// TODO : create this javascrit method with a other mecanism
		/*
		 * out.println("<script language=\"javascript\">");
		 * out.println("autoScroll.delay(250);"); out.println("</script>");
		 */
		out.close();
		return res.toString();
	}

	protected String getDisplayAllLabel(I18nAccess i18nAccess) {
		return i18nAccess.getText("content.files.load", "Display all files");
	}

	protected String getRelativeFileDirectory(ContentContext ctx) {
		throw new NotImplementedException("getRelativeFileDirectory");
	}

	protected String getReverseLinkeLabelTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("component.link.reverse");
	}

	protected String getReverseLinkInputName() {
		return getId() + ID_SEPARATOR + "reverlink_name";
	}

	protected String getSelectXHTMLInputName() {
		return "image_name_select" + ID_SEPARATOR + getId();
	}

	public StaticInfo getStaticInfo(ContentContext ctx) {
		StaticInfo staticInfo;
		try {
			staticInfo = StaticInfo.getInstance(ctx, getFileURL(ctx, getFileName()));
			return staticInfo;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getWordCount(ContentContext ctx) {
		String value = getLabel();
		if (value != null) {
			return value.split(" ").length;
		}
		return 0;
	}

	@Override
	public void init(ComponentBean bean, ContentContext ctx) throws Exception {
		super.init(bean, ctx);
		/* check if the content of db is correct version */
		if (getValue().trim().length() == 0) {
			if (!AdminUserSecurity.isCurrentUserCanUpload(ctx)) {
				setDirSelected(getImportFolderPath(ctx));
			} else {
				setDirSelected("");
			}
			setFileName("");
			properties.setProperty(LABEL_KEY, "");
			properties.setProperty(DESCRIPTION_KEY, "");
			properties.setProperty(REVERSE_LINK_KEY, ReverseLinkService.NONE);
		} else {
			properties.load(stringToStream(getValue()));
		}

		if (isImported(ctx) && getPage() != null) {
			String importFolder = getImportFolderPath(ctx);
			if (!getDirSelected().equals(importFolder)) {
				File oldFile = getFile(ctx);
				setDirSelected(importFolder);
				File newFile = getFile(ctx);
				try {
					if (oldFile.exists()) {
						ResourceHelper.writeFileToFile(oldFile, newFile);
						ResourceHelper.copyResourceData(ctx, oldFile, newFile);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				ResourceHelper.cleanImportResource(ctx, oldFile);
			}
		}

	}

	protected boolean isImported(ContentContext ctx) {
		return getDirSelected().startsWith(URLHelper.removeFirstSlash(ctx.getGlobalContext().getStaticConfig().getImportFolder()));
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return getFileName().trim().length() == 0;
	}

	protected boolean isFileNameValid(ContentContext ctx, String fileName) {
		return true;
	}

	@Override
	public boolean isInsertable() {
		return false;
	}

	protected boolean isLinkToStatic() {
		return true;
	}

	public boolean isOnlyFirstOccurrence() {
		return ReverseLinkService.ONLY_FIRST.equals(properties.getProperty(REVERSE_LINK_KEY));
	}

	public boolean isReverseLink() {
		String reverseLinkValue = properties.getProperty(REVERSE_LINK_KEY, ReverseLinkService.NONE);
		return ReverseLinkService.LINK_TYPES.contains(reverseLinkValue);
	}

	public boolean isWithDescription() {
		return false;
	}

	protected boolean needEncoding() {
		return false;
	}

	protected boolean isFromShared(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return getFileName().startsWith(staticConfig.getShareDataFolderKey());
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {

		boolean fromShared = isFromShared(ctx);

		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		String label = requestService.getParameter(getLabelXHTMLInputName(), "");
		String fileName = requestService.getParameter(getFileXHTMLInputName(), "");
		String newDir = requestService.getParameter(getNewDirInputName(), "");
		String selectedDir = requestService.getParameter(getDirInputName(), "");
		String description = requestService.getParameter(getDescriptionName(), "");
		String reverseLink = requestService.getParameter(getReverseLinkInputName(), ReverseLinkService.NONE);

		String initialFileName = fileName;

		if (newDir.trim().length() > 0) {
			String repositoryDir = getFileDirectory(ctx);

			if (PatternHelper.ALPHANNUM_NOSPACE_PATTERN.matcher(newDir).matches()) {
				File file = new File(repositoryDir + '/' + newDir);
				if (file.mkdirs()) {
					MessageRepository messageRepository = MessageRepository.getInstance(ctx);
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("content.file.info.create-dir", new String[][] { { "group", newDir } }), GenericMessage.INFO));
					selectedDir = newDir;
					setModify();
					setNeedRefresh(true);
				}
			} else {
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("content.file.error.bad-rep-name"), GenericMessage.ERROR));
			}
		}

		if (fileName.trim().length() == 0) {
			fileName = requestService.getParameter(getSelectXHTMLInputName(), "");
			fileName = StringHelper.getFileNameFromPath(fileName);
		}

		if (fromShared && fileName != null) {
			fileName = URLHelper.mergePath(ctx.getGlobalContext().getStaticConfig().getShareDataFolderKey(), fileName);
		}

		if ((!label.equals(getLabel())) || (!fileName.equals(getFileName()))) {
			setModify();
		}
		if (!reverseLink.equals(properties.getProperty(REVERSE_LINK_KEY))) {
			properties.setProperty(REVERSE_LINK_KEY, reverseLink);
			setModify();

			GlobalContext globalContext = ctx.getGlobalContext();
			ReverseLinkService reverlinkService = ReverseLinkService.getInstance(globalContext);
			reverlinkService.clearCache();
		}

		if (!getDirSelected().equals(selectedDir)) {
			fileName = "";
			if (fromShared) {
				fileName = ctx.getGlobalContext().getStaticConfig().getShareDataFolderKey();
			}
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("content.file.info.select-dir", new String[][] { { "group", selectedDir } }), GenericMessage.INFO));
			setModify();
			setNeedRefresh(true);
		}

		if (needEncoding()) {
			String encoding = requestService.getParameter(getEncodingXHTMLInputName(), null);
			if (encoding != null) {
				properties.setProperty(ENCODING_KEY, encoding);
				setModify();
			}
		}

		setDirSelected(selectedDir);
		setFileName(fileName);
		setLabel(label);
		properties.setProperty(DESCRIPTION_KEY, description);

		// if (canUpload(ctx)) {

		if (isFileNameValid(ctx, initialFileName)) {
			try {
				uploadFiles(ctx, requestService);
			} catch (IOException e) {
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("content.file.exist"), GenericMessage.ERROR));
			}
		} else {
			setFileName("");
			return i18nAccess.getText("component.file.badformat", "bad file format.");
		}
		// }

		if (isModify()) {
			setNeedRefresh(true);
		}

		storeProperties();

		return null;
	}

	protected void setLabel(String label) {
		properties.setProperty(LABEL_KEY, label);
	}

	protected void reloadProperties() {
		synchronized (properties) {
			properties.clear();
			try {
				properties.load(stringToStream(getValue()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean renameResource(ContentContext ctx, File oldName, File newName) {

		if (oldName.equals(newName)) {
			return false;
		}
		String currentFile = URLHelper.mergePath(getFileDirectory(ctx), getDirSelected(), getFileName());

		File file = new File(currentFile);

		if (file.getAbsolutePath().replace('\\', '/').equals(oldName.getAbsolutePath().replace('\\', '/'))) {
			String relativeNewFileDir = newName.getParentFile().getAbsolutePath().replace('\\', '/').replace(getFileDirectory(ctx).replace('\\', '/'), "");
			setFileName(newName.getName());
			setDirSelected(relativeNewFileDir);
			setModify();
			storeProperties();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * save a inputStream on disk
	 * 
	 * @param item
	 *            the item of multipart form
	 * @param request
	 * @return file name
	 */
	String saveFile(ContentContext ctx, String name, InputStream in) throws Exception {
		String fileName = null;

		fileName = ResourceHelper.getWindowsFileName(name);
		String imageName = fileName;
		if ((fileName != null) && (fileName.length() > 0)) {
			String dirFile = ElementaryURLHelper.mergePath(getFileDirectory(ctx), getDirSelected());

			imageName = StringHelper.createFileName(fileName);

			String realName = dirFile + '/' + imageName;

			File f = new File(realName);
			if (f.exists()) {
				// create temp file
				File tempFile = new File(StringHelper.getFileNameWithoutExtension(f.getAbsolutePath()) + "__TEMP" + '.' + StringHelper.getFileExtension(f.getName()));
				ResourceHelper.writeStreamToFile(in, tempFile);
				ResourceStatus resouceStatus = ResourceStatus.getInstance(ctx.getRequest().getSession());
				resouceStatus.addSource(new LocalResource(ctx, tempFile));
				resouceStatus.addTarget(new LocalResource(ctx, f));
				// throw new IOException("file already exist");
				ctx.setNeedRefresh(true);
				return fileName;
			}

			File dir = f.getParentFile();
			if (!dir.exists()) {
				dir.mkdirs();
			}
			FileOutputStream out = new FileOutputStream(f);
			ResourceHelper.writeStreamToStream(in, out);
			/*
			 * int read = in.read(); while (read >= 0) { out.write(read); read =
			 * in.read(); }
			 */
			out.close();

			StaticInfo staticInfo = StaticInfo.getInstance(ctx, f);
			MenuElement currentPage = ctx.getCurrentPage();

			staticInfo.setLinkedPageId(ctx, currentPage.getId());
			staticInfo.setShared(ctx, false);
		}
		return fileName;
	}

	protected String saveItem(ContentContext ctx, FileItem item) throws Exception {
		if (!item.isFormField()) {
			String fileName = StringHelper.getFileNameFromPath(item.getName().replace('\\', '/'));
			return saveFile(ctx, StringHelper.createFileName(fileName), item.getInputStream());
		} else {
			return null;
		}
	}

	public void setDirSelected(String dir) {
		properties.setProperty(DIR_KEY, dir);
		storeProperties();
	}

	public void setFileName(String name) {
		properties.setProperty(FILE_NAME_KEY, name);
	}

	@Override
	public void setValue(String inContent) {
		super.setValue(inContent);
		reloadProperties();
	}

	public void storeProperties() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String res = "";
		try {
			properties.store(out, HEADER_V1_0);
			out.flush();
			res = new String(out.toByteArray());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setValue(res);
	}

	protected void uploadFiles(ContentContext ctx, RequestService service) throws Exception {
		Collection<FileItem> items = service.getAllFileItem();
		logger.info("upload " + items.size() + " files.");
		for (FileItem item : items) {
			if (item.getFieldName().equals(getFileXHTMLInputName())) {
				File file = new File(item.getName());
				ZIPFilter filter = new ZIPFilter();
				String newFileName = null;
				if (filter.accept(file, item.getName()) && expandZip()) {
					newFileName = item.getName();
					expandZip(ctx, new ZipInputStream(item.getInputStream()));
				} else {
					newFileName = saveItem(ctx, item);
				}

				if ((newFileName != null) && (newFileName.trim().length() > 0)) {
					properties.setProperty(FILE_NAME_KEY, newFileName);
					setModify();
				}
			}
		}
	}

	@Override
	public String performUpload(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String fileName = requestService.getParameter(getFileXHTMLInputName(), "");
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		if (isFileNameValid(ctx, fileName)) {
			try {
				uploadFiles(ctx, requestService);
				return null;
			} catch (IOException e) {
				return i18nAccess.getText("content.file.exist");
			}
		} else {
			return i18nAccess.getText("component.file.badformat", "bad file format.");
		}

	}

	@Override
	public boolean isMirroredByDefault(ContentContext ctx) {
		return true;
	}

	@Override
	public String getContentAsText(ContentContext ctx) {
		return getLabel();
	}

	public static String performLoadImages(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String compId = rs.getParameter("comp_id", null);
		if (compId != null) {
			AbstractFileComponent comp = (AbstractFileComponent) ContentService.getInstance(ctx.getRequest()).getComponent(ctx, compId);
			String previewCode = comp.getPreviewCode(ctx, comp.getMaxPreviewImages(), true);
			ctx.addAjaxInsideZone(comp.getPreviewZoneId(), previewCode);
			return null;
		}
		return "error on request structure.";

	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getActionGroupName() {
		return TYPE;
	}

	@Override
	public boolean isUploadOnDrop() {
		return false;
	}

	@Override
	public int getPopularity(ContentContext ctx) {
		return 0;
	}

	@Override
	public List<File> getFiles(ContentContext ctx) {
		return null;
	}

}
