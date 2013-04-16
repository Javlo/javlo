/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.image;

import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IReverseLinkComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.file.FileAction;
import org.javlo.service.RequestService;
import org.javlo.template.Template;

/**
 * standard image component. <h4>exposed variable :</h4>
 * <ul>
 * <li>inherited from {@link Image}</li>
 * <li>{@link String} image : url of image.</li>
 * </ul>
 * 
 * @author pvandermaesen
 */
public class GlobalImage extends Image {

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

	private GenericMessage msg;

	@Override
	protected boolean canUpload(ContentContext ctx) {
		return true;
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

	protected String getDefaultFilter() {
		return "standard";
	}

	@Override
	public String getPreviewURL(ContentContext ctx, String filter) {
		try {
			String url = URLHelper.createTransformURL(ctx, getPage(), getResourceURL(ctx, getFileName()), filter);
			url = URLHelper.addParam(url, "hash", getStaticInfo(ctx).getVersionHash());
			return url;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected String getImageURL(ContentContext ctx) throws Exception {
		String decoImage = getDecorationImage();
		if (decoImage != null && decoImage.trim().length() > 0) {
			String imageLink = getResourceURL(ctx, getDecorationImage());
			String imageFilter = getConfig(ctx).getProperty("image.filter", getDefaultFilter());
			return URLHelper.addParam(URLHelper.createTransformURL(ctx, imageLink, imageFilter), "hash", getStaticInfo(ctx).getVersionHash());
		} else {
			return null;
		}
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		String imageURL = getImageURL(ctx);
		if (imageURL != null) {
			ctx.getRequest().setAttribute("image", imageURL);
		}
		if (getFilter(ctx).equals(RAW_FILTER)) {
			ctx.getRequest().setAttribute("previewURL", getURL(ctx));
		} else {
			ctx.getRequest().setAttribute("previewURL", getPreviewURL(ctx, getFilter(ctx)));
		}
		ctx.getRequest().setAttribute("media", this);
		ctx.getRequest().setAttribute("shortDate", StringHelper.renderShortDate(ctx, getDate()));
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getSpecialInputTag());

		finalCode.append("<table class=\"js-change-submit edit normal-layout image\"><tr><td style=\"vertical-align: middle;text-align: center;\">");

		finalCode.append(getPreviewCode(ctx));

		finalCode.append("</td><td>");

		if (this instanceof IReverseLinkComponent) {
			finalCode.append("<div class=\"line\">");
			finalCode.append(XHTMLHelper.getCheckbox(getReverseLinkInputName(), isReverseLink()));
			finalCode.append("<label for=\"" + getReverseLinkInputName() + "\">" + getReverseLinkeLabelTitle(ctx) + "</label>");
			finalCode.append("</div>");
		}

		if (!isMeta()) {
			finalCode.append("<label for=\"" + getLabelXHTMLInputName() + "\">" + getImageLabelTitle(ctx) + " : </label>");
			String[][] params = { { "rows", "1" } };
			finalCode.append(XHTMLHelper.getTextArea(getLabelXHTMLInputName(), getLabel(), params));
			finalCode.append("<br />");
		}

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		if (isMeta()) {
			finalCode.append("<fieldset>");
			finalCode.append("<legend>" + i18nAccess.getText("global.metadata") + "</legend>");
			finalCode.append("<div class=\"line\">");
			finalCode.append("<label for=\"" + getInputNameDate() + "\">" + i18nAccess.getText("global.date") + " : </label>");
			finalCode.append("<input type=\"text\" name=\"" + getInputNameDate() + "\" value=\"" + StringHelper.neverNull(StringHelper.renderTime(getDate())) + "\" />");
			finalCode.append("</div><div class=\"line\">");
			finalCode.append("<label for=\"" + getInputNameLocation() + "\">" + i18nAccess.getText("global.location") + " : </label>");
			finalCode.append("<input type=\"text\" name=\"" + getInputNameLocation() + "\" value=\"" + getLocation() + "\" />");
			finalCode.append("</div><div class=\"line\">");
			finalCode.append("<label for=\"" + getInputNameTitle() + "\">" + i18nAccess.getText("global.title") + " : </label>");
			finalCode.append("<input type=\"text\" name=\"" + getInputNameTitle() + "\" value=\"" + getTitle() + "\" />");
			finalCode.append("</div>");
			if (getTranslatableResources(ctx).size() > 0) {
				finalCode.append("<div class=\"line\">");
				finalCode.append("<label for=\"" + getInputNameTranslation() + "\">" + i18nAccess.getText("content.resource.translationof") + " : </label>");
				finalCode.append(XHTMLHelper.getDropDownFromMap(getInputNameTranslation(), getTranslatableResources(ctx), getTranslatedID(), "", true));
				finalCode.append("</div>");
			}
			finalCode.append("</div>");
			finalCode.append("</fieldset>");
		}

		if (isLink()) {
			finalCode.append("<div class=\"line\"><label for=\"img_link_" + getId() + "\">");
			finalCode.append(getImageLinkTitle(ctx));
			finalCode.append(" : </label><input id=\"img_link_" + getId() + "\" name=\"" + getLinkXHTMLInputName() + "\" type=\"text\" value=\"" + getLink() + "\"/></div>");
		}

		finalCode.append("<div class=\"line\"><label for=\"new_dir_" + getId() + "\">");
		finalCode.append(getNewDirLabelTitle(ctx));
		finalCode.append(" : </label><input id=\"new_dir_" + getId() + "\" name=\"" + getNewDirInputName() + "\" type=\"text\"/></div>");

		if ((getDirList(getFileDirectory(ctx)) != null) && (getDirList(getFileDirectory(ctx)).length > 0)) {
			finalCode.append("<div class=\"line\"><label for=\"" + getDirInputName() + "\">");
			finalCode.append(getDirLabelTitle(ctx));
			finalCode.append(" : </label>");
			Collection<String> dirsCol = new LinkedList<String>();
			dirsCol.add("");
			String[] dirs = getDirList(getFileDirectory(ctx));
			for (String dir : dirs) {
				if (dir.length() > 0 && dir.startsWith("/")) {
					dir = dir.substring(1);
				}
				dirsCol.add(dir);
			}
			finalCode.append(XHTMLHelper.getInputOneSelect(getDirInputName(), dirsCol, getDirSelected(), getJSOnChange(ctx), true));

			Map<String, String> filesParams = new HashMap<String, String>();

			String path = URLHelper.mergePath(FileAction.getPathPrefix(ctx), StaticConfig.getInstance(ctx.getRequest().getSession()).getImageFolderName(), getDirSelected());
			filesParams.put("path", path);
			filesParams.put("webaction", "changeRenderer");
			filesParams.put("page", "meta");
			String staticURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams);

			finalCode.append("<a class=\"" + EDIT_ACTION_CSS_CLASS + "\" href=\"" + staticURL + "\">&nbsp;");
			finalCode.append(i18nAccess.getText("content.goto-static"));
			finalCode.append("</a>");
			finalCode.append("</div>");
		}

		/* filter */
		Template currentTemplate = ctx.getCurrentTemplate();
		if (currentTemplate != null && isImageFilter()) {
			finalCode.append("<div class=\"line\"><label for=\"filter-" + getImageFilterInputName() + "\">");

			finalCode.append(i18nAccess.getText("content.global-image.image-filter"));
			finalCode.append(" : </label>");

			List<String> filters = new ArrayList<String>();
			filters.add(RAW_FILTER);
			filters.addAll(currentTemplate.getImageFilters());

			String[][] filtersArray = new String[filters.size()][2];
			int i = 0;
			for (String filter : filters) {
				filtersArray[i][0] = filter;
				filtersArray[i][1] = i18nAccess.getText("template.image.type." + filter, filter);
				i++;
			}
			finalCode.append(XHTMLHelper.getInputOneSelect(getImageFilterInputName(), filtersArray, getFilter(ctx)));
			finalCode.append("</div>");

		} else {
			logger.severe("template null in GlobalImage");
		}

		String[] fileList = getFileList(getFileDirectory(ctx), getFileFilter());
		if (fileList.length > 0 && isMutlimediaResource()) {

			finalCode.append("<div class=\"line\"><label for=\"" + getSelectXHTMLInputName() + "\">" + getImageChangeTitle(ctx) + " : </label>");

			String[] fileListBlanck = new String[fileList.length + 1];
			fileListBlanck[0] = "";
			System.arraycopy(fileList, 0, fileListBlanck, 1, fileList.length);

			finalCode.append(XHTMLHelper.getInputOneSelect(getSelectXHTMLInputName(), fileListBlanck, getFileName(), getJSOnChange(ctx), true));
			finalCode.append("</div>");
		}

		if (canUpload(ctx)) {
			finalCode.append("<div class=\"line\"><label for=\"" + getFileXHTMLInputName() + "\">" + getImageUploadTitle(ctx) + " : </label>");
			finalCode.append("<input name=\"" + getFileXHTMLInputName() + "\" type=\"file\"/></div>");
		}

		if (isDecorationImage()) {
			finalCode.append("<div class=\"line deco-image\">");
			finalCode.append("<label for=\"" + getDecoImageFileXHTMLInputName() + "\">" + getImageDecorativeTitle(ctx) + " :</label>");
			finalCode.append("<input  id=\"" + getDecoImageFileXHTMLInputName() + "\" name=\"" + getDecoImageFileXHTMLInputName() + "\" type=\"file\"/>");
			finalCode.append("</div>");

			fileList = getFileList(getFileDirectory(ctx), getDecorationFilter());
			if (fileList.length > 0) {
				finalCode.append("<div class=\"line\">");
				finalCode.append("<label for=\"" + getDecoImageXHTMLInputName() + "\">");
				finalCode.append(getImageSelectTitle(ctx));
				finalCode.append("</label>");

				String[] fileListBlanck = new String[fileList.length + 1];
				fileListBlanck[0] = "";
				System.arraycopy(fileList, 0, fileListBlanck, 1, fileList.length);

				finalCode.append(XHTMLHelper.getInputOneSelect(getDecoImageXHTMLInputName(), fileListBlanck, getDecorationImage(), getJSOnChange(ctx), true));

				// actionURL=actionURL+"?"+RequestHelper.CLOSE_WINDOW_PARAMETER+"=true&"+RequestHelper.CLOSE_WINDOW_URL_PARAMETER+"="+actionURL;

				finalCode.append("</div>");
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

		if (isEmbedCode()) {
			// String descriptionTitle = i18nAccess.getText("component.link.description");
			finalCode.append("<div class=\"embed\">");
			finalCode.append("<label style=\"margin-bottom: 3px;\" for=\"" + getEmbedCodeName() + "\">");
			finalCode.append("embed code");
			finalCode.append("</label>");
			finalCode.append("<textarea id=\"" + getEmbedCodeName() + "\" name=\"" + getEmbedCodeName() + "\">");
			finalCode.append(getEmbedCode());
			finalCode.append("</textarea></div>");

		}

		finalCode.append("</td></tr></table>");

		// validation
		if ((getFileName().trim().length() > 0) && (getLabel().trim().length() == 0)) {
			setMessage(new GenericMessage(i18nAccess.getText("component.message.image_no_label"), GenericMessage.ALERT));
		} else if (!isFileNameValid(getFileName())) {
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

	private void setTitle(String title) {
		properties.setProperty(TITLE, title);
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
		folder = URLHelper.mergePath(globalContext.getDataFolder(), staticConfig.getImageFolder());
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
		if (getLink() != null && getLink().trim().length() > 0) {
			return getLink();
		} else if (getFileName() != null) {
			String fileLink = getResourceURL(ctx, getFileName());
			return URLHelper.createResourceURL(ctx, getPage(), fileLink).replace('\\', '/');
		}
		return null;
	}

	public String getPreviewURL(ContentContext ctx) throws Exception {
		if (getLink() != null) {
			return getLink();
		} else if (getFileName() != null) {
			String fileLink = getResourceURL(ctx, getFileName());
			String url = URLHelper.createTransformURL(ctx, getPage(), fileLink, "thumb-view").replace('\\', '/');
			url = URLHelper.addParam(url, "hash", getStaticInfo(ctx).getVersionHash());
			return url;
		}
		return null;
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
						res.append("<a" + cssLinkClass + " rel=\"" + getConfig(ctx).getProperty("rel", "shadowbox") + "\" href=\"" + URLHelper.createURLFromPageName(ctx, getLink().replace(".html", "")) + "\">");
					} else {
						String target = "";
						GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
						if (globalContext.isOpenExernalLinkAsPopup(getLink())) {
							target = "target=\"_blank\" ";
						}
						res.append("<a" + cssLinkClass + ' ' + target + "href=\"" + XHTMLHelper.escapeXHTML(getLink()) + "\" title=\"" + StringHelper.removeTag(getStaticLabel(ctx)) + "\" rel=\"" + getConfig(ctx).getProperty("rel", "shadowbox") + "\">");
					}
					openLink = true;
				}
			} else {
				if (getConfig(ctx).isClickable()) {
					res.append("<a class=\"no-link\" href=\"" + viewURL + getConfig(ctx).getProperty("comp.link.suffix", "") + "\" rel=\"" + getConfig(ctx).getProperty("rel", "shadowbox") + "\" title=\"" + StringHelper.removeTag(getStaticLabel(ctx)) + "\">");
					openLink = true;
				}
			}
			thumbURL = URLHelper.addParam(thumbURL, "hash", getStaticInfo(ctx).getVersionHash());
			res.append("<img src=\"");
			res.append(thumbURL);
			res.append("\" alt=\"");
			res.append(StringHelper.removeTag(getStaticLabel(ctx)));
			if (getStaticInfo(ctx).getDescription(ctx) != null && getStaticInfo(ctx).getDescription(ctx).trim().length() > 0) {
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

			res.append(XHTMLHelper.renderSpecialLink(ctx, ctx.getRequestContentLanguage(), fileLink, getStaticInfo(ctx)));
			res.append("</div></div>");
		} else {
			res.append("&nbsp; <!--IMAGE NOT DEFINED--> ");
		}
		return res.toString();
	}

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {
		super.init(bean, newContext);
		properties.load(stringToStream(getValue()));
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String link = requestService.getParameter(getLinkXHTMLInputName(), null);
		String filter = requestService.getParameter(getImageFilterInputName(), null);
		String decoImage = requestService.getParameter(getDecoImageXHTMLInputName(), null);
		String date = requestService.getParameter(getInputNameDate(), null);
		String location = requestService.getParameter(getInputNameLocation(), null);
		String title = requestService.getParameter(getInputNameTitle(), null);
		String translationOf = requestService.getParameter(getInputNameTranslation(), null);
		String embedCode = requestService.getParameter(getEmbedCodeName(), null);

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

				if (!StringHelper.isURL(link)) {
					MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage("bad link.", GenericMessage.ALERT));
				} else {
					if (!isLinkValid(link)) {
						MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage("link to video work only with youtube, dailymotioin or europartv.", GenericMessage.ALERT));
					}
				}

				try {
					if (getTitle().trim().length() == 0 && URLHelper.isAbsoluteURL(link)) {
						setTitle(NetHelper.getPageTitle(NetHelper.readPage(new URL(link))));
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

		super.performEdit(ctx);

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
}
