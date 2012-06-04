/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.image;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.fileupload.FileItem;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IReverseLinkComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ArrayHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.service.RequestService;


/**
 * @author pvandermaesen
 */
public class ImageLink extends FilterImage {

	private static final String EXTERNAL_LINK_PATTERN = ".*://.*";

	static final String HEADER_V1_0 = "image link storage V.1.0";

	static final String FILE_NAME_KEY_OVER = "file-name-over";

	static final String LINK_KEY = "link";

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "image-left", "image-right", "image-center" };
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
	public String getStyleTitle(ContentContext ctx) {
		return "position";
	}

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {
		super.init(bean, newContext);
		properties.load(stringToStream(getValue()));
	}

	@Override
	public String getCSSClassName(ContentContext ctx) {
		return getType();
	}

	@Override
	public String getImageImgName() {
		return "img_images_" + getId();
	}

	String getSelectXHTMLInputNameOver() {
		return "image_name_select_over" + ID_SEPARATOR + getId();
	}

	String getFileXHTMLInputNameOver() {
		return "image_name_over" + ID_SEPARATOR + getId();
	}

	String getLinkXHTMLInputName() {
		return getId() + ID_SEPARATOR + "link_name";
	}

	String getImageLinkTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-image.link");
	}

	public String getFileNameOver() {
		return properties.getProperty(FILE_NAME_KEY_OVER, "");
	}

	public String getLink() {
		return properties.getProperty(LINK_KEY, "");
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer res = new StringBuffer();
		if ((getValue() != null) && (getValue().trim().length() > 0)) {
			String fileLink = URLHelper.mergePath(getDirSelected(), getFileName());
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
			String thumbURL = URLHelper.createTransformURL(ctx, getPage(), staticConfig.getImageFolder() + '/' + fileLink, getFilter(ctx)).replace('\\', '/');
			res.append("<div "+getSpecialPreviewCssClass(ctx, getCSSClassName(ctx) + " "+getStyle(ctx)+" "+getType())+getSpecialPreviewCssId(ctx)+">");
			res.append("<div class=\"labeled-image\">");
			if (getLink().trim().length() > 0) {
				res.append("<a href=\""+getLink()+"\">");
			}
			res.append("<img src=\"");
			res.append(thumbURL);
			res.append("\" title=\"");
			res.append(StringHelper.removeTag(getLabel()));
			res.append("\" alt=\"");
			res.append(StringHelper.removeTag(getLabel()));
			res.append("\" />");
			if (getLink().trim().length() > 0) {
				res.append("</a>");
			}
			res.append("<div>" + StringHelper.CR2BR(getLabel()) + "</div>");
			res.append("</div></div>");
		} else {
			res.append("&nbsp; <!--IMAGE NOT DEFINED--> ");
		}
		return res.toString();
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getSpecialInputTag());

		finalCode.append("<table class=\"edit normal-layout image\"><tr><td style=\"vertical-align: middle;text-align: center;\">");

		finalCode.append(getPreviewCode(ctx));

		finalCode.append("</td><td>");
		
		if (this instanceof IReverseLinkComponent) {
			finalCode.append("<div class=\"line\">");
			finalCode.append(XHTMLHelper.getCheckbox(getReverseLinkInputName(), isReverseLink()));
			finalCode.append("<label for=\""+getReverseLinkInputName()+"\">"+getReverseLinkeLabelTitle(ctx)+"</label>");
			finalCode.append("</div>");
		}
		
		finalCode.append(getImageLabelTitle(ctx));		
		String[][] params = { { "rows", "1" } };
		finalCode.append(XHTMLHelper.getTextArea(getLabelXHTMLInputName(), getLabel(), params));
		finalCode.append("<br />");
		
		finalCode.append("<div style=\"margin-top: 5px; margin-bottom: 5px;\"><label style=\"float: left; width: 160px; height: 16px;\" for=\"img_link_" + getId() + "\">");
		finalCode.append(getImageLinkTitle(ctx));
		finalCode.append(" : </label><input id=\"img_link_" + getId() + "\" name=\"" + getLinkXHTMLInputName() + "\" type=\"text\" value=\""+getLink()+"\"/></div>");

		finalCode.append("<div style=\"margin-top: 5px; margin-bottom: 5px;\"><label style=\"float: left; width: 160px; height: 16px;\" for=\"new_dir_" + getId() + "\">");
		finalCode.append(getNewDirLabelTitle(ctx));
		finalCode.append(" : </label><input id=\"new_dir_" + getId() + "\" name=\"" + getNewDirInputName() + "\" type=\"text\"/></div>");

		if ((getDirList(getFileDirectory(ctx)) != null)&&(getDirList(getFileDirectory(ctx)).length > 0)) {
			finalCode.append("<div style=\"margin-top: 5px; margin-bottom: 5px;\"><label style=\"float: left; width: 160px; height: 16px;\" for=\"" + getDirInputName() + "\">");
			finalCode.append(getDirLabelTitle(ctx));
			finalCode.append(" : </label>");
			finalCode.append(XHTMLHelper.getInputOneSelect(getDirInputName(), ArrayHelper.addFirstElem(getDirList(getFileDirectory(ctx)), ""), getDirSelected(), getJSOnChange(ctx), true));
			finalCode.append("</div>");
		}
		
		finalCode.append(getImageUploadTitle(ctx));
		finalCode.append("<br /><input name=\"" + getFileXHTMLInputName() + "\" type=\"file\"/><br/><br/>");

		String[] fileList = getFileList(getFileDirectory(ctx));
		if (fileList.length > 0) {
			
			finalCode.append(getImageChangeTitle(ctx));

			finalCode.append("<br />");

			String[] fileListBlanck = new String[fileList.length + 1];
			fileListBlanck[0] = "";
			System.arraycopy(fileList, 0, fileListBlanck, 1, fileList.length);

			finalCode.append(XHTMLHelper.getInputOneSelect(getSelectXHTMLInputName(), fileListBlanck, getFileName(), getJSOnChange(ctx), true));

			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			
			finalCode.append("<a href=\"#\" onclick=\"document.forms['content_update'].dir.value='");
			finalCode.append(URLHelper.mergePath(getRelativeFileDirectory(ctx), getDirSelected()));
			finalCode.append("';document.forms['content_update'].submit(); return false;\">&nbsp;");
			finalCode.append(i18nAccess.getText("content.goto-static"));
			finalCode.append("</a>");
		}
		
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

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

	@Override
	public String getFileDirectory(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String folder = URLHelper.mergePath(globalContext.getDataFolder(), staticConfig.getImageFolder());
		return folder;
	}

	@Override
	public String createFileURL(ContentContext ctx, String url) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return URLHelper.createResourceURL(ctx, getPage(), staticConfig.getImageFolder() + '/' + url);
	}

	/*
	 * @see org.javlo.itf.IContentVisualComponent#getType()
	 */
	@Override
	public String getType() {
		return "image-link";
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
			}
		}
	}

	@Override
	public void refresh(ContentContext ctx) throws Exception {
		
		super.refresh(ctx);
		
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String link = requestService.getParameter(getLinkXHTMLInputName(), null);
		
		if (link != null) {

			if (!link.equals(getLink())) {
				setModify();
			}

			properties.setProperty(LINK_KEY, link);
			
			storeProperties();
		}
	}

	@Override
	public String getHexColor() {
		return GRAPHIC_COLOR;
	}
	
	private GenericMessage msg;
	
	@Override
	public GenericMessage getMessage() {
		return msg;
	}
	
	@Override
	public void setMessage(GenericMessage inMsg) {
		msg = inMsg;
	}

	@Override
	protected String getFilter(ContentContext ctx) {
		return "link";
	}

}
