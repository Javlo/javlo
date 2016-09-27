/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.fileupload.FileItem;
import org.javlo.component.core.ComponentContext;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.IImageFilter;
import org.javlo.component.core.IReverseLinkComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.image.ImageEngine;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.content.Edit;
import org.javlo.module.core.Module;
import org.javlo.module.file.FileAction;
import org.javlo.navigation.MenuElement;
import org.javlo.rendering.Device;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;
import org.javlo.ztatic.StaticInfo;

/**
 * standard image component.
 * <h4>exposed variable :</h4>
 * <ul>
 * <li>inherited from {@link Image}</li>
 * <li>{@link String} image : url of image.</li>
 * </ul>
 * 
 * @author pvandermaesen
 */
public class GlobalImage extends Image implements IImageFilter {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(GlobalImage.class.getName());

	static final String HEADER_V1_0 = "image link storage V.1.0";

	static final String FILE_NAME_KEY_OVER = "file-name-over";

	public static final String IMAGE_FILTER = "image-filter";

	static final String DATE = "date";

	static final String LINK_KEY = "link";

	public static final String TYPE = "global-image";

	private static final String RAW_FILTER = "raw";

	private static final String LOCATION = "location";

	private static final String TITLE = "title";

	private static final String AUTO_LABEL = "auto";

	private GenericMessage msg;

	public GlobalImage() {
		try {
			init();
		} catch (ResourceNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected boolean canUpload(ContentContext ctx) {
		return !isFromShared(ctx);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String createFileURL(ContentContext ctx, String url) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return URLHelper.createResourceURL(ctx, getPage(), staticConfig.getImageFolder() + '/' + url);
	}

	@Override
	public String getCSSClassName(ContentContext ctx) {
		return getType();
	}

	@Override
	public String getSpecificClass(ContentContext ctx) {
		return getFilter(ctx);
	}

	protected boolean isImageFilter() {
		return true;
	}

	protected boolean isDecorationImage() {
		return false;
	}

	protected Map<String, String> getTranslatableResources(ContentContext ctx) throws Exception {
		return Collections.EMPTY_MAP;
	}

	public String getTranslatedID() {
		return properties.getProperty("translated", null);
	}

	public void setTranslatedID(String id) {
		properties.setProperty("translated", id);
	}

	@Override
	public boolean isAskWidth(ContentContext ctx) {
		return true;
	}

	@Override
	public String getWidth() {
		return properties.getProperty("width", null);
	}

	@Override
	public void setWidth(String width) {
		properties.setProperty("width", width);
	}

	protected String getDefaultFilter() {
		return "standard";
	}

	@Override
	public String getPreviewURL(ContentContext ctx, String filter) {
		try {
			String url = null;
			try {
				url = URLHelper.createTransformURL(ctx, ctx.getVirtualCurrentPage(),
						TemplateFactory.getTemplate(ctx, ctx.getVirtualCurrentPage()),
						getResourceURL(ctx, getFileName()), filter, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return url;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getImageHash(Device device) {
		String hash = "" + getFileName().hashCode();
		if (getWidth(device) < 0) {
			return hash;
		} else {
			return hash + '_' + getWidth(device);
		}
	}

	protected String getImageURL(ContentContext ctx) throws Exception {
		String decoImage = getDecorationImage();
		if (decoImage != null && decoImage.trim().length() > 0) {
			String imageLink = getResourceURL(ctx, getDecorationImage());
			String imageFilter = getConfig(ctx).getProperty("image.filter", getDefaultFilter());
			return URLHelper.addParam(URLHelper.createTransformURL(ctx, imageLink, imageFilter), "hash",
					getImageHash(ctx.getDevice()));
		} else {
			return null;
		}
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		// ctx.setCurrentTemplate(null); // reset template

		super.prepareView(ctx);
		String link = getLink();
		if (link.startsWith('/' + ctx.getGlobalContext().getStaticConfig().getStaticFolder())) {
			link = URLHelper.createResourceURL(ctx, link);
		}
		ctx.getRequest().setAttribute("link", link);
		String imageURL = getImageURL(ctx);
		if (imageURL != null) {
			ctx.getRequest().setAttribute("image", imageURL);
		} else {
			ctx.getRequest().setAttribute("image", null);
		}
		if (getFilter(ctx).equals(RAW_FILTER)) {
			ctx.getRequest().setAttribute("previewURL",
					URLHelper.createResourceURL(ctx, getResourceURL(ctx, getFileName())));
		} else {
			ctx.getRequest().setAttribute("previewURL", getPreviewURL(ctx, getFilter(ctx)));
		}
		ctx.getRequest().setAttribute("largeURL", getPreviewURL(ctx, getLargeFilter(ctx)));
		ctx.getRequest().setAttribute("media", this);
		ctx.getRequest().setAttribute("shortDate", StringHelper.renderShortDate(ctx, getDate()));
		if (isMeta() && !isLabel()) {
			ctx.getRequest().setAttribute("cleanLabel", cleanValue(ctx, getTitle()));
			ctx.getRequest().setAttribute("label", getTitle());
		}

		ctx.getRequest().setAttribute("location", getLocation(ctx));
		ctx.getRequest().setAttribute("filter", getFilter(ctx));
		int width = getWidth(ctx.getDevice());
		if (width >= 0) {
			ctx.getRequest().setAttribute("imageWidth", width);
		}

	}

	protected String getNewLinkParamName() {
		return "_new-link-" + getId();
	}

	protected boolean isLabel() {
		return !isMeta();
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		if (ctx.getRequest().getParameter("path") != null) {
			String newFolder = URLHelper.removeStaticFolderPrefix(ctx, ctx.getRequest().getParameter("path"));
			newFolder = newFolder
					.replaceFirst("/" + ctx.getGlobalContext().getStaticConfig().getImageFolderName() + '/', "");
			if (newFolder.trim().length() > 1 && !getDirSelected().equals(newFolder)) {
				setDirSelected(newFolder);
				setFileName("");
			}
		}

		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getSpecialInputTag());

		finalCode.append("<div class=\"js-change-submit image row form-group\"><div class=\"col-sm-5\">");

		finalCode.append(getPreviewCode(ctx));

		finalCode.append("</div><div class=\"col-sm-7\">");

		if (this instanceof IReverseLinkComponent) {
			finalCode.append("<div class=\"line\">");
			finalCode.append(XHTMLHelper.getCheckbox(getReverseLinkInputName(), isReverseLink()));
			finalCode.append(
					"<label for=\"" + getReverseLinkInputName() + "\">" + getReverseLinkeLabelTitle(ctx) + "</label>");
			finalCode.append("</div>");
		}

		if (isLabel() && !ctx.getGlobalContext().isMailingPlatform()) {
			finalCode.append("<div class=\"row form-group\"><div class=\"col-sm-3\">");
			finalCode.append("<label for=\"" + getLabelXHTMLInputName() + "\">" + getImageLabelTitle(ctx)
					+ " : </label></div><div class=\"col-sm-9\">");
			final String[][] params = { { "rows", "3" }, { "cols", "40" }, { "class", "form-control" } };
			finalCode.append(XHTMLHelper.getTextArea(getLabelXHTMLInputName(), getLabel(), params, "form-control"));
			finalCode.append("</div></div>");
		}

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		if (isMeta()) {
			finalCode.append("<fieldset>");
			finalCode.append("<legend>" + i18nAccess.getText("global.metadata") + "</legend>");
			finalCode.append("<div class=\"row form-group\"><div class=\"col-sm-3\">");
			finalCode.append("<label for=\"" + getInputNameDate() + "\">" + i18nAccess.getText("global.date")
					+ " : </label></div><div class=\"col-sm-9\">");
			finalCode.append("<input class=\"form-control\" type=\"text\" name=\"" + getInputNameDate() + "\" value=\""
					+ StringHelper.neverNull(StringHelper.renderTime(getDate())) + "\" />");
			finalCode.append("</div></div><div class=\"row form-group\"><div class=\"col-sm-3\">");
			finalCode.append("<label for=\"" + getInputNameLocation() + "\">" + i18nAccess.getText("global.location")
					+ " : </label></div>");
			finalCode.append("<div class=\"col-sm-9\"><input class=\"form-control\" type=\"text\" name=\""
					+ getInputNameLocation() + "\" value=\"" + getLocation() + "\" /></div>");
			finalCode.append("</div><div class=\"row form-group\"><div class=\"col-sm-3\">");
			finalCode.append("<label for=\"" + getInputNameTitle() + "\">" + i18nAccess.getText("global.title")
					+ " : </label></div>");
			finalCode.append("<div class=\"col-sm-9\"><input class=\"form-control\" type=\"text\" name=\""
					+ getInputNameTitle() + "\" value=\"" + getTitle() + "\" /></div>");
			finalCode.append("</div>");
			if (getTranslatableResources(ctx).size() > 0) {
				finalCode.append("<div class=\"row form-group\"><div class=\"col-sm-3\">");
				finalCode.append("<label for=\"" + getInputNameTranslation() + "\">"
						+ i18nAccess.getText("content.resource.translationof")
						+ " : </label></div><div class=\"col-sm-9\">");
				finalCode.append(XHTMLHelper.getDropDownFromMap(getInputNameTranslation(),
						getTranslatableResources(ctx), getTranslatedID(), "", true, "form-control"));
				finalCode.append("</div></div>");
			}
			finalCode.append("</fieldset>");
		}

		String folder = getDirSelected();
		Map<String, String> filesParams = new HashMap<String, String>();
		String path = URLHelper.mergePath(FileAction.getPathPrefix(ctx),
				StaticConfig.getInstance(ctx.getRequest().getSession()).getImageFolderName(), folder);
		filesParams.put("path", path);
		filesParams.put("webaction", "changeRenderer");
		filesParams.put("page", "meta");
		String backURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "content");
		if (ctx.isEditPreview()) {
			backURL = URLHelper.addParam(backURL, "comp_id", "cp_" + getId());
			backURL = URLHelper.addParam(backURL, "webaction", "editPreview");
		}
		backURL = URLHelper.addParam(backURL, "previewEdit", ctx.getRequest().getParameter("previewEdit"));
		filesParams.put(ElementaryURLHelper.BACK_PARAM_NAME, backURL + '&' + getNewLinkParamName() + "=/"
				+ ctx.getGlobalContext().getStaticConfig().getStaticFolder() + '/');

		String staticLinkURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams);
		filesParams.remove(ElementaryURLHelper.BACK_PARAM_NAME);
		filesParams.put(ElementaryURLHelper.BACK_PARAM_NAME, backURL);
		if (isLink()) {
			finalCode.append(
					"<div class=\"row form-group\"><div class=\"col-sm-3\"><label for=\"img_link_" + getId() + "\">");
			finalCode.append(getImageLinkTitle(ctx) + " : </label></div>");
			String linkToResources = "";
			if (!ctx.getGlobalContext().isMailingPlatform()) {
				linkToResources = "<div class=\"col-sm-2\"><a class=\"browse-link btn btn-default btn-xs\" href=\""
						+ URLHelper.addParam(staticLinkURL, "select", "back") + "\">"
						+ i18nAccess.getText("content.goto-static") + "</a></div>";
			}
			String link = getLink();
			if (ctx.getRequest().getParameter(getNewLinkParamName()) != null) {
				if (!ctx.getRequest().getParameter(getNewLinkParamName())
						.equals('/' + ctx.getGlobalContext().getStaticConfig().getStaticFolder() + '/')) {
					link = ctx.getRequest().getParameter(getNewLinkParamName());
				}
			}
			finalCode.append("<div class=\"col-sm-" + (linkToResources.length() == 0 ? 9 : 7)
					+ "\"><input class=\"form-control\" id=\"img_link_" + getId() + "\" name=\""
					+ getLinkXHTMLInputName() + "\" type=\"text\" value=\"" + link + "\"/></div>" + linkToResources
					+ "</div>");
		}

		finalCode.append(
				"<div class=\"row form-group\"><div class=\"col-sm-3\"><label for=\"new_dir_" + getId() + "\">");
		finalCode.append(getNewDirLabelTitle(ctx));
		finalCode.append(" : </label></div><div class=\"col-sm-9\"><input class=\"form-control\" id=\"new_dir_"
				+ getId() + "\" name=\"" + getNewDirInputName() + "\" type=\"text\"/></div></div>");
		finalCode.append(
				"<div class=\"row form-group\"><div class=\"col-sm-3\"><label for=\"" + getDirInputName() + "\">");
		finalCode.append(getDirLabelTitle(ctx));
		finalCode.append(" : </label></div>");

		if ((getDirList(ctx, getFileDirectory(ctx)) != null) && (getDirList(ctx, getFileDirectory(ctx)).length > 0)) {
			List<String> dirsCol = new LinkedList<String>();
			dirsCol.add("");
			String[] dirs = getDirList(ctx, getFileDirectory(ctx));
			for (String dir : dirs) {
				if (dir.length() > 0 && dir.startsWith("/")) {
					dir = dir.substring(1);
				}
				dirsCol.add(dir);
			}
			if (canUpload(ctx) && !ctx.getGlobalContext().isMailingPlatform()) {
				finalCode.append("<div class=\"col-sm-7\">");
			} else {
				finalCode.append("<div class=\"col-sm-9\">");
			}
			finalCode.append(XHTMLHelper.getInputOneSelect(getDirInputName(), dirsCol, folder, "form-control",
					getJSOnChange(ctx), true));
			finalCode.append("</div>");
		}

		if (canUpload(ctx) && !ctx.getGlobalContext().isMailingPlatform()) {
			String staticURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams);
			finalCode.append("<div class=\"col-sm-2\"><a class=\"" + EDIT_ACTION_CSS_CLASS
					+ " btn btn-default btn-xs\" href=\"" + staticURL + "\">");
			finalCode.append(i18nAccess.getText("content.goto-static"));
			finalCode.append("</a></div>");
		}
		finalCode.append("</div>");

		/* filter */
		Template currentTemplate = ctx.getCurrentTemplate();
		if (currentTemplate != null && isImageFilter()) {
			finalCode.append("<div class=\"row form-group\"><div class=\"col-sm-3\"><label for=\"filter-"
					+ getImageFilterInputName() + "\">");

			finalCode.append(i18nAccess.getText("content.global-image.image-filter"));
			finalCode.append(" : </label></div><div class=\"col-sm-7\">");

			List<String> filters = new ArrayList<String>();
			if (isAllowRAW(ctx)) {
				filters.add(RAW_FILTER);
			}
			filters.addAll(currentTemplate.getImageFilters());

			String[][] filtersArray = new String[filters.size()][2];
			int i = 0;
			for (String filter : filters) {
				filtersArray[i][0] = filter;
				filtersArray[i][1] = i18nAccess.getText("template.image.type." + filter, filter);
				i++;
			}
			finalCode.append(XHTMLHelper.getInputOneSelectWithClass(getImageFilterInputName(), filtersArray,
					getFilter(ctx), "no-submit"));
			finalCode.append("</div></div>");

		} else {
			logger.severe("template null in GlobalImage");
		}

		String[] fileList = getFileList(getFileDirectory(ctx), getFileFilter());
		if (fileList.length > 0 && isMutlimediaResource()) {
			finalCode.append("<div class=\"row form-group\"><div class=\"col-sm-3\"><label for=\""
					+ getSelectXHTMLInputName() + "\">" + getImageChangeTitle(ctx) + " : </label></div>");
			String[] fileListBlanck = new String[fileList.length + 1];
			fileListBlanck[0] = "";
			System.arraycopy(fileList, 0, fileListBlanck, 1, fileList.length);
			String fileName = getFileName();
			if (isFromShared(ctx)) {
				fileName = fileName.replaceFirst(ctx.getGlobalContext().getStaticConfig().getShareDataFolderKey() + '/',
						"");

			}
			finalCode.append("<div class=\"col-sm-9\">");
			finalCode.append(XHTMLHelper.getInputOneSelect(getSelectXHTMLInputName(), fileListBlanck, fileName,
					"form-control", getJSOnChange(ctx), true));
			finalCode.append("</div></div>");
		}

		if (canUpload(ctx)) {
			finalCode.append("<div class=\"row form-group\"><div class=\"col-sm-3\"><label for=\""
					+ getFileXHTMLInputName() + "\">" + getImageUploadTitle(ctx) + " : </label></div>");
			finalCode.append(
					"<div class=\"col-sm-7\"><input name=\"" + getFileXHTMLInputName() + "\" type=\"file\"/></div>");
			finalCode
					.append("<div class=\"col-sm-2\"><button name=\"upload\" type=\"submit\" class=\"btn btn-default btn-xs\" onclick=\"jQuery(this).parent().find('.ajax-loader').addClass('active');\">"
							+ getFileUploadActionTitle(ctx) + "</button>");
			finalCode.append("<span class=\"ajax-loader\"></span></div>");
			finalCode.append("</div>");
		}

		if (isDecorationImage()) {
			finalCode.append("<div class=\"row form-group deco-image\"><div class=\"col-sm-3\">");
			finalCode.append("<label for=\"" + getDecoImageFileXHTMLInputName() + "\">" + getImageDecorativeTitle(ctx)
					+ " : </label></div>");
			finalCode.append(
					"<div class=\"col-sm-9\"><input class=\"form-control\" id=\"" + getDecoImageFileXHTMLInputName()
							+ "\" name=\"" + getDecoImageFileXHTMLInputName() + "\" type=\"file\"/></div>");
			finalCode.append("</div>");

			fileList = getFileList(getFileDirectory(ctx), getDecorationFilter());
			if (fileList.length > 0) {
				finalCode.append("<div class=\"row form-group\"><div class=\"col-sm-3\">");
				finalCode.append("<label for=\"" + getDecoImageXHTMLInputName() + "\">");
				finalCode.append(getImageSelectTitle(ctx));
				finalCode.append("</label></div>");

				String[] fileListBlanck = new String[fileList.length + 1];
				fileListBlanck[0] = "";
				System.arraycopy(fileList, 0, fileListBlanck, 1, fileList.length);

				finalCode.append("<div class=\"col-sm-9\">");
				finalCode.append(XHTMLHelper.getInputOneSelect(getDecoImageXHTMLInputName(), fileListBlanck,
						getDecorationImage(), "form-control", getJSOnChange(ctx), true));
				finalCode.append("</div>");

				// actionURL=actionURL+"?"+RequestHelper.CLOSE_WINDOW_PARAMETER+"=true&"+RequestHelper.CLOSE_WINDOW_URL_PARAMETER+"="+actionURL;

				finalCode.append("</div>");
			}
		}

		if (isWithDescription()) {
			String descriptionTitle = i18nAccess.getText("component.link.description");
			finalCode.append("<div class=\"description row form-group\"><div class=\"col-sm-3\">");
			finalCode.append("<label style=\"margin-bottom: 3px;\" for=\"" + getDescriptionName() + "\">");
			finalCode.append(descriptionTitle);
			finalCode.append("</label></div><div class=\"col-sm-9\">");
			finalCode.append("<textarea id=\"" + getDescriptionName() + "\" name=\"" + getDescriptionName() + "\">");
			finalCode.append(getDescription());
			finalCode.append("</textarea></div></div>");
		}

		if (isEmbedCode()) {
			// String descriptionTitle =
			// i18nAccess.getText("component.link.description");
			finalCode.append("<div class=\"embed row form-group\"><div class=\"col-sm-3\">");
			finalCode.append("<label style=\"margin-bottom: 3px;\" for=\"" + getEmbedCodeName() + "\">");
			finalCode.append("embed code");
			finalCode.append("</label></div>");
			finalCode.append("<div class=\"col-sm-9\"><textarea class=\"form-control\" id=\"" + getEmbedCodeName()
					+ "\" name=\"" + getEmbedCodeName() + "\">");
			finalCode.append(getEmbedCode());
			finalCode.append("</textarea></div></div>");
		}

		finalCode.append(getMetaCode(ctx));

		finalCode.append("</div></div>");

		// validation
		if ((getFileName().trim().length() > 0) && (getLabel().trim().length() == 0)) {
			setMessage(
					new GenericMessage(i18nAccess.getText("component.message.image_no_label"), GenericMessage.ALERT));
		} else if (!isFileNameValid(ctx, getFileName())) {
			setMessage(new GenericMessage(i18nAccess.getText("component.error.file"), GenericMessage.ERROR));
		}

		return finalCode.toString();
	}

	protected boolean isMutlimediaResource() {
		return true;
	}

	protected boolean isLink() {
		return true;
	}

	protected boolean isEmbedCode() {
		return false;
	}

	public String getTitle() {
		return properties.getProperty(TITLE, "");
	}

	@Override
	public String getTitle(ContentContext ctx) {
		String title = getTitle();
		if (title.trim().length() == 0) {
			return super.getTitle(ctx);
		} else {
			return title;
		}
	}

	@Override
	public String getLocation(ContentContext ctx) {
		String location = getLocation();
		if (location.trim().length() == 0) {
			return super.getLocation(ctx);
		} else {
			return location;
		}
	}

	@Override
	public Date getDate(ContentContext ctx) {
		Date date = null;
		try {
			date = getDate();
		} catch (ParseException e) {
		}
		if (date == null) {
			return super.getDate(ctx);
		} else {
			return date;
		}
	}

	protected void setTitle(String title) {
		if (title != null) {
			properties.setProperty(TITLE, title);
		}
	}

	private String getInputNameTitle() {
		return "title_" + getId();
	}

	private String getInputNameTranslation() {
		return "translation_" + getId();
	}

	public String getLocation() {
		return properties.getProperty(LOCATION, "");
	}

	private void setLocation(String location) {
		properties.setProperty(LOCATION, location);
	}

	private String getInputNameLocation() {
		return "location_" + getId();
	}

	public Date getDate() throws ParseException {
		return StringHelper.parseDateOrTime(properties.getProperty(DATE, ""));
	}

	protected void setDate(String date) {
		properties.setProperty(DATE, date);
	}

	private String getInputNameDate() {
		return "date_" + getId();
	}

	protected boolean isMeta() {
		return false;
	}

	@Override
	public String getFileDirectory(ContentContext ctx) {
		String folder;
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (isFromShared(ctx)) {
			try {
				folder = URLHelper.mergePath(globalContext.getSharedDataFolder(ctx.getRequest().getSession()),
						staticConfig.getImageFolderName());
			} catch (Exception e) {
				e.printStackTrace();
				folder = null;
			}
		} else {
			folder = URLHelper.mergePath(globalContext.getDataFolder(), staticConfig.getImageFolder());
		}
		return folder;
	}

	public String getFileNameOver() {
		return properties.getProperty(FILE_NAME_KEY_OVER, "");
	}

	String getFileXHTMLInputNameOver() {
		return "image_name_over" + ID_SEPARATOR + getId();
	}

	protected String getFilter(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		return properties.getProperty(IMAGE_FILTER, globalContext.getDefaultImageFilter());
	}

	@Override
	public String getHexColor() {
		return GRAPHIC_COLOR;
	}

	String getImageFilterInputName() {
		return "_image_filter_" + getId();
	}

	@Override
	public String getImageImgName() {
		return "img_images_" + getId();
	}

	protected String getImageLinkTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-image.link");
	}

	public String getLink() {
		return properties.getProperty(LINK_KEY, "");
	}

	@Override
	public String getURL(ContentContext ctx) {
		if (StringHelper.isMailURL(getLink())) {
			return getLink();
		}
		if (getLink() != null && getLink().trim().length() > 0 && !getLink().equals("#")) {
			if (!StringHelper.isURL(getLink())) {
				ContentService content = ContentService.getInstance(ctx.getRequest());
				MenuElement targetPage;
				try {
					targetPage = content.getNavigation(ctx).searchChildFromName(getLink());
					if (targetPage != null) {
						return URLHelper.createURL(ctx, targetPage);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			String url = getLink();
			if (!StringHelper.isURL(url)) {
				url = URLHelper.createResourceURL(ctx, url);
			}
			return url;
		} else if (getFileName() != null && !getLink().equals("#")) {
			String fileLink = getResourceURL(ctx, getFileName());
			return URLHelper.createResourceURL(ctx, getPage(), fileLink).replace('\\', '/');
		}
		return null;
	}

	protected String getLargeFilter(ContentContext ctx) {
		return getConfig(ctx).getProperty("image.large-filter", "large");
	}

	protected String getLinkXHTMLInputName() {
		return "_link_name_" + getId();
	}

	@Override
	public GenericMessage getMessage() {
		return msg;
	}

	String getSelectXHTMLInputNameOver() {
		return "image_name_select_over" + ID_SEPARATOR + getId();
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		String filter = getFilter(ctx);
		if (HIDDEN.equals(filter)) {
			return "";
		}
		StringBuffer res = new StringBuffer();
		if ((getValue() != null) && (getValue().trim().length() > 0)) {
			String fileLink = getResourceURL(ctx, getFileName());

			String thumbURL;
			if (RAW_FILTER.equals(filter)) {
				thumbURL = URLHelper.createResourceURL(ctx, getPage(), fileLink).replace('\\', '/');
			} else {
				thumbURL = URLHelper.createTransformURL(ctx, getPage(), fileLink, filter).replace('\\', '/');
			}
			String viewURL = URLHelper.createTransformURL(ctx, getPage(), fileLink, "thumb-view").replace('\\', '/');
			res.append("<div class=\"" + filter + "\">");
			res.append("<div class=\"labeled-image\">");
			boolean openLink = false;
			if (getLink().trim().length() > 0) {
				if (!getLink().trim().equals(NO_LINK)) {

					String cssLinkClass = "";
					String linkType = StringHelper.getPathType(getLink(), "");
					if (linkType.length() > 0) {
						cssLinkClass = " class=\"" + linkType + "\"";
					}

					if (!getLink().contains("/")) { // considered as page name
						res.append("<a" + cssLinkClass + " rel=\"" + getConfig(ctx).getProperty("rel", "shadowbox")
								+ "\" href=\"" + URLHelper.createURLFromPageName(ctx, getLink().replace(".html", ""))
								+ "\">");
					} else {
						String target = "";
						GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
						if (globalContext.isOpenExternalLinkAsPopup(getLink())) {
							target = "target=\"_blank\" ";
						}
						res.append("<a" + cssLinkClass + ' ' + target + "href=\"" + XHTMLHelper.escapeXHTML(getLink())
								+ "\" title=\"" + StringHelper.removeTag(getStaticLabel(ctx)) + "\" rel=\""
								+ getConfig(ctx).getProperty("rel", "shadowbox") + "\">");
					}
					openLink = true;
				}
			} else {
				if (getConfig(ctx).isClickable()) {
					res.append("<a class=\"no-link\" href=\"" + viewURL
							+ getConfig(ctx).getProperty("comp.link.suffix", "") + "\" rel=\""
							+ getConfig(ctx).getProperty("rel", "shadowbox") + "\" title=\""
							+ StringHelper.removeTag(getStaticLabel(ctx)) + "\">");
					openLink = true;
				}
			}
			thumbURL = URLHelper.addParam(thumbURL, "hash", getStaticInfo(ctx).getVersionHash(ctx));
			res.append("<img src=\"");
			res.append(thumbURL);
			res.append("\" alt=\"");
			res.append(StringHelper.removeTag(getStaticLabel(ctx)));
			if (getStaticInfo(ctx).getDescription(ctx) != null
					&& getStaticInfo(ctx).getDescription(ctx).trim().length() > 0) {
				res.append("\" longdesc=\"");
				res.append(URLHelper.createTransformLongDescURL(ctx, fileLink));
			}
			res.append("\" /><span class=\"layer\">&nbsp;</span>");

			if (openLink) {
				res.append("</a>");
			}

			if (StringHelper.CR2BR(getLabel()).trim().length() > 0) {
				res.append("<div class=\"label\">" + StringHelper.CR2BR(getLabel()) + "</div>");
			}

			res.append(
					XHTMLHelper.renderSpecialLink(ctx, ctx.getRequestContentLanguage(), fileLink, getStaticInfo(ctx)));
			res.append("</div></div>");
		} else {
			res.append("&nbsp; <!--IMAGE NOT DEFINED--> ");
		}
		return res.toString();
	}

	@Override
	protected void init() throws ResourceNotFoundException {
		super.init();
		try {
			properties.load(stringToStream(getValue()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (isMeta()) {
			if (getLabel().trim().length() > 0 && getTitle().trim().length() == 0) {
				setTitle(getLabel());
			}
		}
	}

	/*
	 * @Override public void init(ComponentBean bean, ContentContext ctx) throws
	 * Exception { super.init(bean, ctx); boolean isImported = isImported(ctx);
	 * if (isImported) { ContentContext pageCtx =
	 * ctx.getContextOnPage(getPage()); if (pageCtx.getCurrentPage() != null) {
	 * String localImportFolder = getImportFolderPath(pageCtx); if
	 * (!localImportFolder.equals(getDirSelected())) { File imageSrc =
	 * getFile(ctx); if (!imageSrc.exists()) {
	 * logger.warning("file not found : " + imageSrc); } else { try {
	 * setDirSelected(localImportFolder); File imageTarget = getFile(ctx);
	 * ResourceHelper.writeFileToFile(imageSrc, imageTarget); storeProperties();
	 * setModify(); // SharedContentService.getInstance(ctx).clearCache(ctx);
	 * PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
	 * } catch (Throwable t) { logger.severe("error on copy image : "+imageSrc);
	 * t.printStackTrace(); } } } } } }
	 */

	/*
	 * @Override public void init(ComponentBean bean, ContentContext newContext)
	 * throws Exception { super.init(bean, newContext);
	 * 
	 * }
	 */

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String link = requestService.getParameter(getLinkXHTMLInputName(), null);
		String filter = requestService.getParameter(getImageFilterInputName(), null);
		String decoImage = requestService.getParameter(getDecoImageXHTMLInputName(), null);
		String date = requestService.getParameter(getInputNameDate(), null);
		String location = requestService.getParameter(getInputNameLocation(), null);
		String title = requestService.getParameter(getInputNameTitle(), null);
		String translationOf = requestService.getParameter(getInputNameTranslation(), null);
		String embedCode = requestService.getParameter(getEmbedCodeName(), null);
		String auto = requestService.getParameter(getTextAutoInputName(), null);

		if (requestService.getParameter(getFirstTextInputName(), null) != null) {
			setTextAuto(StringHelper.isTrue(auto));
		}

		setFirstText(requestService.getParameter(getFirstTextInputName(), ""));
		setSecondText(requestService.getParameter(getSecondTextInputName(), ""));

		String label = requestService.getParameter(getLabelXHTMLInputName(), null);
		String textLabel = requestService.getParameter(getLabelTextInputName(), null);
		if (label != null && !label.equals(getLabel())) {
			StaticInfo staticInfo = getStaticInfo(ctx);
			if (staticInfo != null && StringHelper.isEmpty(staticInfo.getTitle(ctx))) {
				staticInfo.setTitle(ctx, label);
			}
			setFirstText(null);
			setSecondText(null);
		} else if (textLabel != null && !textLabel.equals(getLabel()) && isTextAuto()) {
			setLabel(textLabel);
			requestService.setParameter(getLabelXHTMLInputName(), getLabel());
			setFirstText(null);
			setSecondText(null);
			setModify();
		}
		// if no label force label value for super classes.
		if (label == null) {
			requestService.setParameter(getLabelXHTMLInputName(), getLabel());
		}

		if (title != null) {
			if (!title.equals(getTitle())) {
				setTitle(title);
				storeProperties();
				setModify();
			}
		}
		if (translationOf != null) {
			if (getTranslatedID() == null || !getTranslatedID().equals(translationOf)) {
				setTranslatedID(translationOf);
				storeProperties();
				setModify();
			}
		}
		if (decoImage != null) {
			if (!decoImage.equals(getDecorationImage())) {
				setDecorationImage(decoImage);
				storeProperties();
				setModify();
			}
		}
		if (date != null) {
			try {
				Date currentDate = StringHelper.parseDateOrTime(date);
				if (!currentDate.equals(getDate())) {
					setDate(date);
					storeProperties();
					setModify();
				}
			} catch (Exception e) {
				logger.warning("unvalid date : " + date);
			}
		}
		if (location != null) {
			if (!location.equals(getLocation())) {
				setLocation(location);
				storeProperties();
				setModify();
			}
		}
		if (link != null) {
			if (!link.equals(getLink())) {

				link = link.trim();
				if (StringHelper.isURL(link)) {
					// Complete url
				} else if (StringHelper.isMailURL(link)) {
					// email
				} else if (link.equals("#")) {
					// Dummy url
				} else if (link.startsWith("/")) {
					// Absolute site URL
				} else if (!link.contains(".") && !link.contains("/")) {
					// Page name
				} else {
					// Bad link
					// MessageRepository.getInstance(ctx).setGlobalMessage(new
					// GenericMessage("bad link.", GenericMessage.ALERT));
					link = "http://" + link;
					setNeedRefresh(true);
				}

				if (!isLinkValid(link)) {
					MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(
							"link to video work only with youtube, dailymotion or europarltv.", GenericMessage.ALERT));
				}

				try {
					if (isMeta() && getTitle().trim().length() == 0 && URLHelper.isAbsoluteURL(link)) {
						if (ctx.getGlobalContext().getStaticConfig().isInternetAccess()) {							
							setTitle(NetHelper.getPageTitle(NetHelper.readPage(new URL(link))));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				setModify();
				setLink(link);
				storeProperties();
			}
		}
		if (filter != null) {
			if (!filter.equals(getFilter(ctx))) {
				setModify();
				setFilter(filter);
				storeProperties();
			}
		}
		if (embedCode != null) {
			if (!embedCode.equals(getEmbedCode())) {
				setModify();
				storeProperties();
				setEmbedCode(embedCode);
			}
		}

		String msg = super.performEdit(ctx);

		if (getWidth() != null && getWidth().trim().length() > 0) {
			if (StringHelper.isDigit(getWidth())) {
				msg = I18nAccess.getInstance(ctx).getText("content.image.width-noext",
						"Image 'width' need unity like px or %.");
			}
		}

		return msg;

	}

	protected boolean isLinkValid(String url) {
		return true;
	}

	public void setFilter(String filter) {
		properties.setProperty(IMAGE_FILTER, filter);
	}

	public void setLink(String link) {
		properties.setProperty(LINK_KEY, link);
	}

	public void setDecorationImage(String image) {
		properties.setProperty("deco_image", image);
	}

	public String getDecorationImage() {
		return properties.getProperty("deco_image");
	}

	@Override
	public void setMessage(GenericMessage inMsg) {
		msg = inMsg;
	}

	@Override
	protected void uploadFiles(ContentContext ctx, RequestService requestService) throws Exception {
		Collection<FileItem> itemsName = requestService.getAllFileItem();

		logger.info("upload " + itemsName.size() + " files.");

		for (FileItem item : itemsName) {
			setNeedRefresh(true);
			if (item.getFieldName().equals(getFileXHTMLInputName())) {
				String newFileName = null;
				newFileName = saveItem(ctx, item);
				if ((newFileName != null) && (newFileName.trim().length() > 0)) {
					properties.setProperty(FILE_NAME_KEY, newFileName);
					setModify();
				}
			} else if (item.getFieldName().equals(getFileXHTMLInputNameOver())) {
				String newFileName = null;
				newFileName = saveItem(ctx, item);
				if ((newFileName != null) && (newFileName.trim().length() > 0)) {
					properties.setProperty(FILE_NAME_KEY_OVER, newFileName);
					setModify();
				}
			} else if (item.getFieldName().equals(getDecoImageFileXHTMLInputName())) {
				String newFileName = null;
				newFileName = saveItem(ctx, item);
				if ((newFileName != null) && (newFileName.trim().length() > 0)) {
					setDecorationImage(newFileName);
					setModify();
				}
			}
		}
		if (isModify()) {
			storeProperties();
		}
	}

	@Override
	public String getImageLinkURL(ContentContext ctx) {
		return getLink();
	}

	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		super.initContent(ctx);
		setFilter(ctx.getCurrentTemplate().getDefaultImageFilter());
		setLink(getConfig(ctx).getProperty("content.link", ""));
		setModify();
		storeProperties();
		return true;
	}

	public int getHeight() {
		return Integer.parseInt(properties.getProperty("height", "-1"));
	}

	@Override
	public void setRenderer(ContentContext ctx, String renderer) {
		if (properties != null) {
			properties.remove(getWidthKey(ctx.getDevice()));
		}
		super.setRenderer(ctx, renderer);
	}

	private String getWidthKey(Device device) {
		if (device == null) {
			return "width-" + Device.DEFAULT;
		} else {
			return "width-" + device.getCode();
		}
	}

	public int getWidth(Device device) {
		return Integer.parseInt(properties.getProperty(getWidthKey(device), "-1"));
	}

	public String getFirstText() {
		return properties.getProperty("first-text", getLabel());
	}

	public void setFirstText(String text) {
		if (text == null) {
			properties.remove("first-text");
		} else {
			properties.setProperty("first-text", text);
		}
	}

	public String getSecondText() {
		return properties.getProperty("second-text", "");
	}

	public void setSecondText(String text) {
		if (text == null) {
			properties.remove("second-text");
		} else {
			properties.setProperty("second-text", text);
		}
	}

	public void setHeight(int height) {
		if (getHeight() != height) {
			properties.setProperty("height", "" + height);
			setModify();
		}
	}

	public void setWidth(ContentContext ctx, int width) {
		if (getWidth(ctx.getDevice()) != width) {
			properties.setProperty(getWidthKey(ctx.getDevice()), "" + width);
			setModify();
		}
	}

	@Override
	public String getActionGroupName() {
		return "global-image";
	}

	public static String performDataFeedBack(ContentContext ctx, EditContext editContext, GlobalContext globalContext,
			User currentUser, ContentService content, ComponentContext componentContext, RequestService rs,
			I18nAccess i18nAccess, MessageRepository messageRepository, Module currentModule,
			AdminUserFactory adminUserFactory) throws Exception {

		IContentVisualComponent comp = ComponentHelper.getComponentFromRequest(ctx, "compid");
		GlobalImage image = null;
		if (comp instanceof GlobalImage) {
			image = (GlobalImage) comp;
		}
		/*
		 * } else if (comp instanceof MirrorComponent) { image =
		 * (GlobalImage)((MirrorComponent) comp).getMirrorComponent(ctx); }
		 */
		if (image != null && image.getConfig(ctx).isDataFeedBack() && currentUser != null
				&& currentUser.validForRoles(AdminUserSecurity.CONTENT_ROLE)) {

			logger.info("exec data feed back (template:" + ctx.getCurrentTemplate().getName() + ").");
			String firstText = rs.getParameter("firsttext", null);
			String secondText = rs.getParameter("secondtext", null);
			String height = rs.getParameter("height", null);
			String width = rs.getParameter("width", null);
			if (image.isTextAuto()) {
				if (firstText != null && !firstText.equals(image.getFirstText())) {
					image.setModify();
					image.setFirstText(firstText);
				}
				if (secondText != null && !secondText.equals(image.getSecondText())) {
					image.setModify();
					image.setSecondText(secondText);
				}
			}
			if (height != null && height.trim().length() > 0) {
				int intHeight = Integer.parseInt(height);
				if (intHeight != image.getHeight()) {
					image.setModify();
					image.setHeight(intHeight);
				}
			}
			if (width != null && width.trim().length() > 0) {
				int inWidth = Integer.parseInt(width);
				if (inWidth != image.getWidth(ctx.getDevice())) {
					image.setModify();
					image.setWidth(ctx, inWidth);
				}
			}
			if (image.isModify()) {
				image.storeProperties();
				PersistenceService.getInstance(globalContext).setAskStore(true);
				Edit.performSave(ctx, editContext, globalContext, content, componentContext, rs, i18nAccess,
						messageRepository, currentModule, adminUserFactory);
			}
			ctx.getAjaxData().put("previewURL", image.getPreviewURL(ctx, image.getFilter(ctx)));
		} else {
			logger.fine("stop data feed back (template:" + ctx.getCurrentTemplate().getName() + ").");
		}
		return null;
	}

	public boolean isFloatText(ContentContext ctx) {
		if (getCurrentRenderer(ctx) == null) {
			return false;
		}
		return getCurrentRenderer(ctx).contains("image-");
	}

	@Override
	public String getSpecialTagTitle(ContentContext ctx) {
		if (ctx.getGlobalContext().isMailingPlatform()) {
			return "text";
		} else {
			return null;
		}
	}

	public boolean isTextAuto() {
		if (properties == null || properties.getProperty(AUTO_LABEL, null) == null) {
			return true; // default value
		}
		return StringHelper.isTrue(properties.getProperty(AUTO_LABEL, null));
	}

	public void setTextAuto(boolean auto) {
		properties.setProperty(AUTO_LABEL, "" + auto);
	}

	protected String getLabelTextInputName() {
		return getId() + ID_SEPARATOR + "label-text";
	}

	protected String getEditorComplexity(ContentContext ctx) {
		return getConfig(ctx).getProperty("editor-complexity", "light");
	}

	@Override
	public String getSpecialTagXHTML(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		Map<String, String> filesParams = new HashMap<String, String>();
		String path = FileAction.getPathPrefix(ctx);
		filesParams.put("path", path);
		filesParams.put("webaction", "changeRenderer");
		filesParams.put("page", "meta");
		filesParams.put("select", "_TYPE_");
		filesParams.put(ContentContext.PREVIEW_EDIT_PARAM, "true");
		String chooseImageURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams);

		out.println("<div class=\"text\">");

		String disabled = " enabled-zone";
		if (!isTextAuto()) {
			disabled = " disabled-zone";
		}

		out.println("<div class=\"line label-text" + disabled + "\"><label for=\"" + getLabelTextInputName()
				+ "\">label text : </label>");
		String id = "special-label-" + getId();
		String[][] paramsLabelText = new String[][] { { "rows", "3" }, { "cols", "100" }, { "class", "tinymce-light" },
				{ "id", id } };
		out.println(XHTMLHelper.getTextArea(getLabelTextInputName(), getLabel(), paramsLabelText));
		out.println("<script type=\"text/javascript\">jQuery(document).ready(loadWysiwyg('#" + id + "','"
				+ getEditorComplexity(ctx) + "','" + chooseImageURL + "'));</script>");
		out.println("</div>");

		if (isFloatText(ctx)) {
			out.println("<div class=\"line\">");
			out.println("<label for=\"" + getTextAutoInputName() + "\">Auto : </label>");
			String checked = "";
			if (isTextAuto()) {
				checked = " checked=\"checked\"";
			}
			out.println(
					"<input type=\"checkbox\" id=\"" + getTextAutoInputName() + "\" name=\"" + getTextAutoInputName()
							+ "\"" + checked + " onchange=\"switchClass('enabled-zone','disabled-zone');\" />");
			out.println("</div>");
			String url = URLHelper.createTransformURL(ctx, getPage(), getResourceURL(ctx, getFileName()), "list");

			disabled = " enabled-zone";
			if (isTextAuto()) {
				disabled = " disabled-zone";
			}

			out.println("<div class=\"group\">");
			out.println("<div class=\"text-image\"><img src=\"" + url + "\" /></div>");
			out.println("<div class=\"line first-text" + disabled + "\">");
			out.println("<label for=\"" + getFirstTextInputName() + "\">first text : </label>");
			id = "first-text-" + getId();
			String[][] paramsFirstText = new String[][] { { "rows", "3" }, { "cols", "100" },
					{ "class", "tinymce-light" }, { "id", id } };
			out.println(XHTMLHelper.getTextArea(getFirstTextInputName(), getFirstText(), paramsFirstText));
			out.println("<script type=\"text/javascript\">jQuery(document).ready(loadWysiwyg('#" + id + "','light','"
					+ chooseImageURL + "'));</script>");

			out.println("</div>");
			out.println("</div>");
			out.println("<div class=\"line second-text" + disabled + "\"><label for=\"" + getSecondTextInputName()
					+ "\">second text : </label>");
			id = "second-text-" + getId();
			String[][] paramsSecondText = new String[][] { { "rows", "3" }, { "cols", "100" },
					{ "class", "tinymce-light" }, { "id", id } };
			out.println(XHTMLHelper.getTextArea(getSecondTextInputName(), getSecondText(), paramsSecondText));
			out.println("<script type=\"text/javascript\">jQuery(document).ready(loadWysiwyg('#" + id + "','light','"
					+ chooseImageURL + "'));</script>");

			out.println("</div>");
		}
		out.println("</div>");

		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String getImageFilterKey(Device device) {
		if (getWidth(device) < 0) {
			return null;
		} else {
			return "" + getWidth(device);
		}
	}

	@Override
	public BufferedImage filterImage(Device device, BufferedImage image) {
		if (device.getCode().equalsIgnoreCase("pdf")) {
			return image;
		} else {
			reloadProperties();
			return ImageEngine.resizeWidth(image, getWidth(device), false);
		}
	}

	@Override
	public boolean isListable() {
		return true;
	}

	@Override
	public GenericMessage getTextMessage(ContentContext ctx) {
		if (isFloatText(ctx)) {
			try {
				I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				return new GenericMessage(
						i18nAccess.getText("content.message.pdf-warning", "This feature does not work in pdf."),
						GenericMessage.ALERT);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public boolean isLinkValid(ContentContext ctx) {
		String url = getURL(ctx);
		return StringHelper.isEmpty(url) && !StringHelper.isImage(url);
	}

	@Override
	protected boolean isFileNameValid(ContentContext ctx, String fileName) {
		if (StringHelper.isEmpty(fileName)) {
			return true;
		} else {
			String extension = ',' + ctx.getGlobalContext().getStaticConfig().getImageFormat() + ',';
			return extension.contains(',' + StringHelper.getFileExtension(fileName).toLowerCase() + ',');
		}
	}

	@Override
	public boolean isLocal(ContentContext ctx) {
		return isImported(ctx);
	}

}
