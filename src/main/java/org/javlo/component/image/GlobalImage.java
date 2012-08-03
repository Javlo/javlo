/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.image;

import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.service.RequestService;
import org.javlo.template.Template;

/**
 * Standard image component.
 * @author pvandermaesen
 */
public class GlobalImage extends FilterImage {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(GlobalImage.class.getName());

	static final String HEADER_V1_0 = "image link storage V.1.0";

	static final String FILE_NAME_KEY_OVER = "file-name-over";

	static final String IMAGE_FILTER = "image-filter";
	
	static final String DATE = "date";

	static final String LINK_KEY = "link";

	public static final String TYPE = "global-image";

	private static final String RAW_FILTER = "raw";
	private static final String HIDDEN_FILTER = "hidden";

	private static final int MAX_PICTURE = 12;

	private static final String LOCATION = "location";
	
	private static final String TITLE = "title";

	private GenericMessage msg;

	@Override
	protected boolean canUpload() {
		return true;
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
	
	protected Map<String,String> getTranslatableResources(ContentContext ctx) throws Exception {
		return Collections.EMPTY_MAP;
	}
	
	public String getTranslatedID() {
		return properties.getProperty("translated", null);
	}
	
	public void setTranslatedID(String id) {
		properties.setProperty("translated", id);
	}
	
	protected boolean isDisplayAllBouton() {
		return false;
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

		finalCode.append(getImageLabelTitle(ctx));
		String[][] params = { { "rows", "1" } };
		finalCode.append(XHTMLHelper.getTextArea(getLabelXHTMLInputName(), getLabel(), params));
		finalCode.append("<br />");
		
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		
		if (isMeta()) {
			finalCode.append("<fieldset>");
			finalCode.append("<legend>"+i18nAccess.getText("global.metadata")+"</legend>");
			finalCode.append("<div class=\"line\">");
			finalCode.append("<label for=\""+getInputNameDate()+"\">"+i18nAccess.getText("global.date")+" : </label>");
			finalCode.append("<input type=\"text\" name=\""+getInputNameDate()+"\" value=\""+StringHelper.neverNull(StringHelper.renderTime(getDate()))+"\" />");
			finalCode.append("</div><div class=\"line\">");
			finalCode.append("<label for=\""+getInputNameLocation()+"\">"+i18nAccess.getText("global.location")+" : </label>");
			finalCode.append("<input type=\"text\" name=\""+getInputNameLocation()+"\" value=\""+getLocation()+"\" />");
			finalCode.append("</div><div class=\"line\">");
			finalCode.append("<label for=\""+getInputNameTitle()+"\">"+i18nAccess.getText("global.title")+" : </label>");
			finalCode.append("<input type=\"text\" name=\""+getInputNameTitle()+"\" value=\""+getTitle()+"\" />");
			finalCode.append("</div>");
			if (getTranslatableResources(ctx).size() > 0) {
				finalCode.append("<div class=\"line\">");
				finalCode.append("<label for=\""+getInputNameTranslation()+"\">"+i18nAccess.getText("content.resource.translationof")+" : </label>");
				finalCode.append(XHTMLHelper.getDropDownFromMap(getInputNameTranslation(), getTranslatableResources(ctx), getTranslatedID(), "" , true));
				finalCode.append("</div>");
			}
			finalCode.append("</div>");
			finalCode.append("</fieldset>");
		}

		finalCode.append("<div style=\"margin-top: 5px; margin-bottom: 5px;\"><label style=\"float: left; width: 160px; height: 16px;\" for=\"img_link_" + getId() + "\">");
		finalCode.append(getImageLinkTitle(ctx));
		finalCode.append(" : </label><input id=\"img_link_" + getId() + "\" name=\"" + getLinkXHTMLInputName() + "\" type=\"text\" value=\"" + getLink() + "\"/></div>");

		finalCode.append("<div style=\"margin-top: 5px; margin-bottom: 5px;\"><label style=\"float: left; width: 160px; height: 16px;\" for=\"new_dir_" + getId() + "\">");
		finalCode.append(getNewDirLabelTitle(ctx));
		finalCode.append(" : </label><input id=\"new_dir_" + getId() + "\" name=\"" + getNewDirInputName() + "\" type=\"text\"/></div>");
		
		if ((getDirList(getFileDirectory(ctx)) != null) && (getDirList(getFileDirectory(ctx)).length > 0)) {
			finalCode.append("<div style=\"margin-top: 5px; margin-bottom: 5px;\"><label style=\"float: left; width: 160px; height: 16px;\" for=\"" + getDirInputName() + "\">");
			finalCode.append(getDirLabelTitle(ctx));
			finalCode.append(" : </label>");
			Collection<String> dirsCol = new LinkedList<String>();
			dirsCol.add("");
			String[] dirs = getDirList(getFileDirectory(ctx));
			for (String dir : dirs) {
				dirsCol.add(dir);
			}
			finalCode.append(XHTMLHelper.getInputOneSelect(getDirInputName(), dirsCol, getDirSelected(), getJSOnChange(ctx), true));
			
			Map<String, String> filesParams = new HashMap<String, String>();
			filesParams.put("path", URLHelper.mergePath("/", getRelativeFileDirectory(ctx), getDirSelected()));
			filesParams.put("webaction", "changeRenderer");
			filesParams.put("page", "meta");
			String staticURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams); 
			
			finalCode.append("<a class=\"" + EDIT_ACTION_CSS_CLASS + "\" href=\""+staticURL+"\">&nbsp;");
			finalCode.append(i18nAccess.getText("content.goto-static"));
			finalCode.append("</a>");
			finalCode.append("</div>");
		}

		/* filter */
		Template currentTemplate = ctx.getCurrentTemplate();
		if (currentTemplate != null && isImageFilter()) {
			finalCode.append("<div style=\"margin-top: 5px; margin-bottom: 5px;\"><label style=\"float: left; width: 160px; height: 16px;\" for=\"filter-" + getImageFilterInputName() + "\">");
			
			finalCode.append(i18nAccess.getText("content.global-image.image-filter"));
			finalCode.append(" : </label>");

			List<String> filters = new ArrayList<String>();
			filters.add(RAW_FILTER);
			filters.add(HIDDEN_FILTER);
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
		if (fileList.length > 0) {
			
			finalCode.append("<div class=\"line\"><label for=\"" + getSelectXHTMLInputName() +"\">"+getImageChangeTitle(ctx)+" : </label>");


			String[] fileListBlanck = new String[fileList.length + 1];
			fileListBlanck[0] = "";
			System.arraycopy(fileList, 0, fileListBlanck, 1, fileList.length);

			finalCode.append(XHTMLHelper.getInputOneSelect(getSelectXHTMLInputName(), fileListBlanck, getFileName(), getJSOnChange(ctx), true));
			finalCode.append("</div>");

			if (fileList.length > MAX_PICTURE && isDisplayAllBouton() ) {
				finalCode.append("<div class=\"line\">");
				Map<String, String> ajaxParams = new HashMap<String, String>();
				ajaxParams.put("webaction", "showallpreview");
				ajaxParams.put("comp-id", getId());
				String ajaxURL = URLHelper.createAjaxURL(ctx, ajaxParams);
				finalCode.append("<input type=\"button\" value=\"" + i18nAccess.getText("content.image.show-all") + "\" onclick=\"showAllPictures('" + getPreviewZoneId() + "' , '" + ajaxURL + "');\" />");
				finalCode.append("</div>");
			}
		}
		
		if (canUpload()) {
			finalCode.append("<div class=\"line\"><label for=\"" + getFileXHTMLInputName() +"\">"+getImageUploadTitle(ctx)+" : </label>");
			finalCode.append("<input name=\"" + getFileXHTMLInputName() + "\" type=\"file\"/></div>");			
		}
		
		if (isDecorationImage()) {
			finalCode.append("<div class=\"line deco-image\">");
			finalCode.append("<label for=\""+getDecoImageFileXHTMLInputName()+"\">"+getImageDecorativeTitle(ctx)+" :</label>");
			finalCode.append("<input  id=\"" + getDecoImageFileXHTMLInputName() + "\" name=\"" + getDecoImageFileXHTMLInputName() + "\" type=\"file\"/>");
			finalCode.append("</div><div class=\"line\">");
			
			fileList = getFileList(getFileDirectory(ctx), getDecorationFilter());
			if (fileList.length > 0) {
				finalCode.append("<label for=\"" + getDecoImageXHTMLInputName() + "\">");
				finalCode.append(getImageDecorativeTitle(ctx));
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

		finalCode.append("</td></tr></table>");

		// validation
		if ((getFileName().trim().length() > 0) && (getLabel().trim().length() == 0)) {
			setMessage(new GenericMessage(i18nAccess.getText("component.message.image_no_label"), GenericMessage.ALERT));
		} else if (!isFileNameValid(getFileName())) {
			setMessage(new GenericMessage(i18nAccess.getText("component.error.file"), GenericMessage.ERROR));
		}

		return finalCode.toString();
	}

	public String getTitle() {
		return properties.getProperty(TITLE,"");
	}
	
	private void setTitle(String title) {
		properties.setProperty(TITLE,title);
	}

	private String getInputNameTitle() {
		return "title_"+getId();
	}
	
	private String getInputNameTranslation() {
		return "translation_"+getId();
	}

	public String getLocation() {
		return properties.getProperty(LOCATION,"");
	}
	
	private void setLocation(String location) {
		properties.setProperty(LOCATION,location);
	}

	private String getInputNameLocation() {
		return "location_"+getId();
	}

	public Date getDate() throws ParseException {
		return StringHelper.parseDateOrTime(properties.getProperty(DATE,""));
	}
	
	protected void setDate(String date) {
		properties.setProperty(DATE, date);
	}

	private String getInputNameDate() {
		return "date_"+getId();
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

	@Override
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
	
	public String getURL(ContentContext ctx) {
		if (getLink() != null ||  getLink().trim().length() > 0) {
			return getLink();
		} else if (getFileName() != null) {
			String fileLink = getImageURL(ctx, getFileName());
			return URLHelper.createResourceURL(ctx, getPage(), fileLink).replace('\\', '/');
		}
		return null;
	}
	
	public String getPreviewURL(ContentContext ctx) throws Exception {
		if (getLink() != null) {
			return getLink();
		} else if (getFileName() != null) {
			String fileLink = getImageURL(ctx, getFileName());
			return URLHelper.createTransformURL(ctx, getPage(), fileLink, "thumb-view").replace('\\', '/');
		}
		return null;
	}

	protected String getLinkXHTMLInputName() {
		return "_link_name_" + getId();
	}

	@Override
	protected int getMaxPreviewImages() {
		return MAX_PICTURE;
	}

	@Override
	public GenericMessage getMessage() {
		return msg;
	}

	String getSelectXHTMLInputNameOver() {
		return "image_name_select_over" + ID_SEPARATOR + getId();
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		String left = "left";
		String right = "right";
		String center = "center";
		try {
			I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
			left = i18n.getText("global.left");
			right = i18n.getText("global.right");
			center = i18n.getText("global.center");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String[] { left, right, center };
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "image-left", "image-right", "image-center" };
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		return "position";
	}

	/*
	 * @see org.javlo.itf.IContentVisualComponent#getType()
	 */
	@Override
	public String getType() {
		return TYPE;
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		String filter = getFilter(ctx);
		if (HIDDEN_FILTER.equals(filter)) {
			return "";
		}
		StringBuffer res = new StringBuffer();
		if ((getValue() != null) && (getValue().trim().length() > 0)) {
			String fileLink = getImageURL(ctx, getFileName());

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
						cssLinkClass = " class=\""+linkType+"\"";
					}

					if (!getLink().contains("/")) { // considered as page name
						res.append("<a" + cssLinkClass + " rel=\""+getConfig(ctx).getProperty("rel", "shadowbox")+"\" href=\"" + URLHelper.createURLFromPageName(ctx, getLink().replace(".html", "")) + "\">");
					} else {
						String target = "";
						GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
						if (globalContext.isOpenExernalLinkAsPopup(getLink())) {
							target = "target=\"_blank\" ";
						}
						res.append("<a" + cssLinkClass + ' ' + target + "href=\"" + XHTMLHelper.escapeXHTML(getLink()) + "\" title=\""+StringHelper.removeTag(getStaticLabel(ctx))+"\" rel=\""+getConfig(ctx).getProperty("rel", "shadowbox")+"\">");
					}
					openLink = true;
				}
			} else {
				if (getConfig(ctx).isClickable()) {
					res.append("<a class=\"no-link\" href=\"" + viewURL + getConfig(ctx).getProperty("comp.link.suffix", "") + "\" rel=\""+getConfig(ctx).getProperty("rel", "shadowbox")+"\" title=\"" + StringHelper.removeTag(getStaticLabel(ctx)) + "\">");
					openLink = true;
				}
			}
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

			if ( StringHelper.CR2BR(getLabel()).trim().length() > 0) {
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
	public boolean needJavaScript(ContentContext ctx) {
		return false;
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
			if (!date.equals(getDate())) {
				try {
					StringHelper.parseDateOrTime(date);
					setDate(date);
					storeProperties();
					setModify();
				} catch (Exception e) {
					logger.warning("unvalid date : "+date);
				}
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
		
		super.performEdit(ctx);

	}

	private void setFilter(String filter) {
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
		for (FileItem item : itemsName) {
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
