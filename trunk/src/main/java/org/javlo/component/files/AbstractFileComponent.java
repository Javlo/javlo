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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.fileupload.FileItem;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.IReverseLinkComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.filter.DirectoryFilter;
import org.javlo.filter.ZIPFilter;
import org.javlo.helper.ArrayHelper;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.Comparator.FileComparator;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.RequestService;
import org.javlo.service.ReverseLinkService;
import org.javlo.service.resource.Resource;
import org.javlo.ztatic.IStaticContainer;
import org.javlo.ztatic.StaticInfo;

/**
 * @author pvandermaesen
 */
public abstract class AbstractFileComponent extends AbstractVisualComponent implements IStaticContainer {

	static final String HEADER_V1_0 = "file storage V.1.1";

	public static final String LABEL_KEY = "label";

	public static final String DIR_KEY = "dir";

	public static final String FILE_NAME_KEY = "file-name";

	public static final String DESCRIPTION_KEY = "description";

	protected static final String REVERSE_LINK_KEY = "reverse-lnk";

	protected static final String ENCODING_KEY = "encoding";

	protected static final String DEFAULT_ENCODING = "default";

	protected Properties properties = new Properties();

	public AbstractFileComponent() {
		properties.setProperty(LABEL_KEY, "");
		properties.setProperty(FILE_NAME_KEY, "");
		properties.setProperty(FILE_NAME_KEY, "");
		properties.setProperty(DESCRIPTION_KEY, "");
		properties.setProperty(ENCODING_KEY, DEFAULT_ENCODING);
		properties.setProperty(REVERSE_LINK_KEY, ReverseLinkService.NONE);
	}

	protected boolean canUpload() {
		return true;
	}

	@Override
	public boolean contains(ContentContext ctx, String inURI) {
		String uri = ElementaryURLHelper.mergePath(getDirSelected(), getFileName());
		uri = ElementaryURLHelper.mergePath(getFileDirectory(ctx), uri);

		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		uri = ResourceHelper.extractRessourceDir(staticConfig, globalContext, uri);

		/* clean path */
		inURI = inURI.replace('\\', '/').replaceAll("//", "/");
		uri = uri.replace('\\', '/').replaceAll("//", "/");

		return uri.equals(inURI);
	}

	public abstract String createFileURL(ContentContext ctx, String url);

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

	protected String getCSSType() {
		return "file";
	}

	protected String getDeleteTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-image.delete-file");
	}

	public String getDescription() {
		return properties.getProperty(DESCRIPTION_KEY);
	}

	public String getDescriptionName() {
		return getId() + ID_SEPARATOR + "description";
	}

	protected String getDirInputName() {
		return getId() + ID_SEPARATOR + "dir_name";
	}

	protected String getDirLabelTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-image.dir");
	}

	protected String[] getDirList(String directory) {
		File dir = new File(directory);
		return dir.list(new DirectoryFilter());
	}

	public String getDirSelected() {
		return properties.getProperty(DIR_KEY, "");
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getSpecialInputTag());

		finalCode.append("<table class=\"edit normal-layout " + getCSSType() + "\"><tr><td style=\"vertical-align: middle;text-align: center;\">");

		finalCode.append(getPreviewCode(ctx));

		finalCode.append("</td><td class=\"file-command\">");

		if (this instanceof IReverseLinkComponent) {
			/*
			 * finalCode.append("<div class=\"line\">"); finalCode.append(XHTMLHelper.getCheckbox(getReverseLinkInputName(), isReverseLink())); finalCode.append("<label for=\"" + getReverseLinkInputName() + "\">" + getReverseLinkeLabelTitle(ctx) + "</label>"); finalCode.append("</div>");
			 */

			finalCode.append("<div class=\"line\">");
			String reverseLink = properties.getProperty(REVERSE_LINK_KEY, ReverseLinkService.NONE);

			// 1.3 to 1.4 conversion from legacy value "true" to corresponding "all"
			if (StringHelper.isTrue(reverseLink)) {
				reverseLink = ReverseLinkService.ALL;
			}
			finalCode.append("<label for=\"" + getReverseLinkInputName() + "\">" + getReverseLinkeLabelTitle(ctx) + " : </label>");
			finalCode.append(XHTMLHelper.getReverlinkSelectType(ctx, getReverseLinkInputName(), reverseLink));
			finalCode.append("</div>");
		}

		finalCode.append(getImageLabelTitle(ctx));
		finalCode.append("<br />");
		String[][] params = { { "rows", "1" } };
		finalCode.append(XHTMLHelper.getTextArea(getLabelXHTMLInputName(), getLabel(), params));

		if (canUpload()) {
			finalCode.append("<div class=\"command\" style=\"margin-top: 5px; margin-bottom: 5px;\"><label style=\"float: left; width: 160px; height: 16px;\" for=\"new_dir_" + getId() + "\">");
			finalCode.append(getNewDirLabelTitle(ctx));
			finalCode.append(" : </label><input id=\"new_dir_" + getId() + "\" name=\"" + getNewDirInputName() + "\" type=\"text\"/></div>");
		}

		if ((getDirList(getFileDirectory(ctx)) != null) && (getDirList(getFileDirectory(ctx)).length > 0)) {
			finalCode.append("<div style=\"margin-top: 5px; margin-bottom: 5px;\"><label style=\"float: left; width: 160px; height: 16px;\" for=\"" + getDirInputName() + "\">");
			finalCode.append(getDirLabelTitle(ctx));
			finalCode.append(" : </label>");
			finalCode.append(XHTMLHelper.getInputOneSelect(getDirInputName(), ArrayHelper.addFirstElem(getDirList(getFileDirectory(ctx)), ""), getDirSelected(), getJSOnChange(ctx), true));
			finalCode.append("</div>");
		}

		if (needEncoding()) {
			finalCode.append("<div class=\"line\">");
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			finalCode.append("<label style=\"float: left; width: 160px; line-height: 16px;\" for=\"" + getEncodingXHTMLInputName() + "\">" + i18nAccess.getText("content.file.encoding") + " : </label>");
			String[] encodings = new String[globalContext.getEncodings().size() + 1];
			encodings[0] = "default";
			int i = 1;
			for (String encoding : globalContext.getEncodings()) {
				encodings[i] = encoding;
				i++;
			}
			finalCode.append(XHTMLHelper.getInputOneSelect(getEncodingXHTMLInputName(), encodings, getEncoding(), null, false));
			finalCode.append("</div>");
		}

		if (canUpload()) {
			finalCode.append(getImageUploadTitle(ctx));
			finalCode.append("<br /><input name=\"" + getFileXHTMLInputName() + "\" type=\"file\"/><br /><br />");
		}

		String[] fileList = getFileList(getFileDirectory(ctx), getFileFilter());
		if (fileList.length > 0) {

			finalCode.append(getImageChangeTitle(ctx));

			finalCode.append("<br />");

			String[] fileListBlanck = new String[fileList.length + 1];
			fileListBlanck[0] = "";
			System.arraycopy(fileList, 0, fileListBlanck, 1, fileList.length);

			finalCode.append(XHTMLHelper.getInputOneSelect(getSelectXHTMLInputName(), fileListBlanck, getFileName(), getJSOnChange(ctx), true));

			/*
			 * finalCode.append("<a href=\"javascript:document.forms['content_update'].deltype.value='" ); finalCode.append(getType()); finalCode.append("';document.forms['content_update'].delfile.value='" ); finalCode.append(getFileName()); finalCode.append("';document.forms['content_update'].delid.value='" ); finalCode.append(getId());finalCode.append( "';document.forms['content_update'].submit();\">&nbsp;"); finalCode.append(getDeleteTitle()); finalCode.append("</a>");
			 */

			if (ctx.getRenderMode() == ContentContext.EDIT_MODE) {
				if (isLinkToStatic()) {
					finalCode.append("&nbsp;<a class=\"" + IContentVisualComponent.EDIT_ACTION_CSS_CLASS + "\" href=\"#\" onclick=\"document.forms['content_update'].dir.value='");
					finalCode.append(ElementaryURLHelper.mergePath(getRelativeFileDirectory(ctx), getDirSelected()));
					finalCode.append("';document.forms['content_update'].submit(); return false;\">");
					finalCode.append(i18nAccess.getText("content.goto-static"));
					finalCode.append("</a>");
				}
			}
		}

		if (isWithDescription()) {
			String descriptionTitle = i18nAccess.getText("component.link.description");
			finalCode.append("<div class=\"description\">");
			finalCode.append("<label style=\"margin-bottom: 3px;\" for=\"" + getDescriptionName() + "\">");
			finalCode.append(descriptionTitle);
			finalCode.append("</label>");
			finalCode.append("<textarea id=\"" + getDescriptionName() + "\" name=\"" + getDescriptionName() + "\">");
			finalCode.append(getDescription());
			finalCode.append("</textarea></div>");
		}

		finalCode.append("</td></tr></table>");

		// validation
		if (!isFileNameValid(getFileName())) {
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

	public abstract String getFileDirectory(ContentContext ctx);

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
			Arrays.sort(files, fileComparator);

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
		return ElementaryURLHelper.mergePath(getRelativeFileDirectory(ctx), ElementaryURLHelper.mergePath(getDirSelected(), fileLink));
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
	
	protected String getImageDecorativeTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-image.add");
	}


	public String getJSOnChange(ContentContext ctx) {
		String ajaxURL = URLHelper.createAjaxURL(ctx);
		String formRef = "document.forms['content_update']";
		return "updateComponent(" + formRef + ",'" + ajaxURL + "'); reloadComponent('" + getId() + "','" + ajaxURL + "');";
	}

	public String getLabel() {
		return properties.getProperty(LABEL_KEY);
	}

	protected String getLabelXHTMLInputName() {
		return getId() + ID_SEPARATOR + "label_name";
	}

	protected String getNewDirInputName() {
		return getId() + ID_SEPARATOR + "new_dir_name";
	}

	protected String getNewDirLabelTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-image.new-dir");
	}

	protected String getPreviewCode(ContentContext ctx) throws Exception {
		return "?PreviewCode not defined?";
	}

	protected abstract String getRelativeFileDirectory(ContentContext ctx);

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
			setDirSelected("");
			setFileName("");
			properties.setProperty(LABEL_KEY, "");
			properties.setProperty(DESCRIPTION_KEY, "");
			properties.setProperty(REVERSE_LINK_KEY, ReverseLinkService.NONE);
		} else {
			properties.load(stringToStream(getValue()));
		}
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return getFileName().trim().length() == 0;
	}

	@Override
	public boolean isExtractable() {
		return false;
	}

	protected boolean isFileNameValid(String fileName) {
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

	@Override
	public void refresh(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		String label = requestService.getParameter(getLabelXHTMLInputName(), null);
		String fileName = requestService.getParameter(getFileXHTMLInputName(), "");
		String newDir = requestService.getParameter(getNewDirInputName(), "");
		String selectedDir = requestService.getParameter(getDirInputName(), "");
		String description = requestService.getParameter(getDescriptionName(), "");
		String reverseLink = requestService.getParameter(getReverseLinkInputName(), ReverseLinkService.NONE);

		if (newDir.trim().length() > 0) {
			String repositoryDir = getFileDirectory(ctx);

			if (PatternHelper.ALPHANNUM_NOSPACE_PATTERN.matcher(newDir).matches()) {
				File file = new File(repositoryDir + '/' + newDir);
				if (file.mkdirs()) {
					MessageRepository messageRepository = MessageRepository.getInstance(ctx);
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("content.file.info.create-dir", new String[][] { { "group", newDir } }), GenericMessage.INFO));
					selectedDir = newDir;
					setNeedRefresh(true);
				}
			} else {
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("content.file.error.bad-rep-name"), GenericMessage.ERROR));
			}
		}

		if (fileName != null) {

			if (fileName.trim().length() == 0) {
				fileName = requestService.getParameter(getSelectXHTMLInputName(), "");
				fileName = StringHelper.getFileNameFromPath(fileName);
			}

			if (label != null) {
				if ((!label.equals(getLabel())) || (!fileName.equals(getFileName()))) {
					setModify();
				}
				if (!reverseLink.equals(properties.getProperty(REVERSE_LINK_KEY))) {
					properties.setProperty(REVERSE_LINK_KEY, reverseLink);
					setModify();

					GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
					ReverseLinkService reverlinkService = ReverseLinkService.getInstance(globalContext);
					reverlinkService.clearCache();
				}

				if (!getDirSelected().equals(selectedDir)) {
					setModify();
					fileName = "";
					MessageRepository messageRepository = MessageRepository.getInstance(ctx);
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("content.file.info.select-dir", new String[][] { { "group", selectedDir } }), GenericMessage.INFO));
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
				properties.setProperty(LABEL_KEY, label);
				properties.setProperty(DESCRIPTION_KEY, description);
			}

			if (canUpload()) {
				if (isFileNameValid(fileName)) {
					try {
						uploadFiles(ctx, requestService);
					} catch (IOException e) {
						MessageRepository messageRepository = MessageRepository.getInstance(ctx);
						messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("content.file.exist"), GenericMessage.ERROR));
					}
				}
			}

			storeProperties();
		}
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
		String currentFile = ElementaryURLHelper.mergePath(getFileDirectory(ctx), getDirSelected());
		currentFile = ElementaryURLHelper.mergePath(currentFile, getFileName());
		File file = new File(currentFile);
		if (file.equals(oldName)) {
			String relativeNewFileDir = newName.getParentFile().getAbsolutePath().replace(getFileDirectory(ctx), "");
			if (relativeNewFileDir.length() == newName.getParentFile().getAbsolutePath().length()) {
				return false;
			}
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
				throw new IOException("file allready exist");
			}

			File dir = f.getParentFile();
			if (!dir.exists()) {
				dir.mkdirs();
			}
			FileOutputStream out = new FileOutputStream(f);
			ResourceHelper.writeStreamToStream(in, out);
			/*
			 * int read = in.read(); while (read >= 0) { out.write(read); read = in.read(); }
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
		for (FileItem item : items) {
			if (item.getFieldName().equals(getFileXHTMLInputName())) {
				File file = new File(item.getName());
				ZIPFilter filter = new ZIPFilter();
				String newFileName = null;
				if (filter.accept(file, item.getName()) && expandZip()) {
					newFileName = item.getName();
					expandZip(ctx, new ZipInputStream(item.getInputStream())); //TODO: who close this stream ?
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

}
