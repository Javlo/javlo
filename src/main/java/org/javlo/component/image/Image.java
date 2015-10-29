package org.javlo.component.image;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javlo.actions.IAction;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IPreviewable;
import org.javlo.component.core.IStaticResource;
import org.javlo.component.files.AbstractFileComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.filter.ImageFileFilter;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.image.ImageConfig;
import org.javlo.message.MessageRepository;
import org.javlo.module.file.FileBean;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.ztatic.StaticInfo;

/**
 * simple image without filter.
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class Image extends AbstractFileComponent implements IImageTitle, IPreviewable, IStaticResource, IAction {

	public static final String STYLE_CENTER = "image-center";

	@Override
	public String[] getStyleList(ContentContext ctx) {
		String[] superList = super.getStyleList(ctx);
		if (superList != null && superList.length > 0) {
			return superList;
		} else {
			return new String[] { STYLE_CENTER, "image-left", "image-right", HIDDEN };
		}
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		String[] superList = super.getStyleLabelList(ctx);
		if (superList != null && superList.length > 0) {
			return superList;
		} else {
			String center = "center";
			String left = "left";
			String right = "right";
			String hidden = "hidden";
			try {
				I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
				center = i18n.getText("global.center");
				left = i18n.getText("global.left");
				right = i18n.getText("global.right");
				hidden = i18n.getText("global.hidden");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new String[] { center, left, right, hidden };
		}
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		String superTitle = super.getStyleTitle(ctx);
		if (superTitle != null) {
			return superTitle;
		} else {
			return "position";
		}
	}

	public String getCSSClassName(ContentContext ctx) {
		return getStyle(ctx) + ' ' + getType();
	}

	protected ImageConfig config = null;

	@Override
	public void init(ComponentBean bean, ContentContext ctx) throws Exception {
		super.init(bean, ctx);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		config = ImageConfig.getInstance(globalContext, ctx.getRequest().getSession(), ctx.getCurrentTemplate());
	}

	public String getImageImgName() {
		return "img_images_" + getId();
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer res = new StringBuffer();
		if ((getValue() != null) && (getFileName().trim().length() > 0)) {
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());

			String fileLink = URLHelper.mergePath(getDirSelected(), getFileName());

			String label = getLabel();
			if (label.trim().length() == 0) {
				label = getDescription();
			}

			String url = URLHelper.createResourceURL(ctx, getPage(), staticConfig.getImageFolder() + '/' + fileLink).replace('\\', '/');
			res.append("<div " + getSpecialPreviewCssClass(ctx, getCSSClassName(ctx)) + getSpecialPreviewCssId(ctx) + "><img src=\"");
			res.append(url);
			res.append("\" title=\"");
			res.append(label);
			res.append("\" alt=\"");
			res.append(label);
			res.append("\" /></div>");
		} else {
			res.append("&nbsp; <!--IMAGE NOT DEFINED--> ");
		}
		return res.toString();
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		StaticInfo staticInfo = getStaticInfo(ctx);
		if (staticInfo != null) {
			ctx.getRequest().setAttribute("file", new StaticInfo.StaticInfoBean(ctx, staticInfo));
		}
	}

	@Override
	protected String getCSSType() {
		return "image";
	}

	@Override
	protected String getPreviewCode(ContentContext ctx) throws Exception {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		out.println("<div id=\"" + getPreviewZoneId() + "\" class=\"selected-zone\">");
		out.println(getPreviewCode(ctx, getMaxPreviewImages()));
		out.println("</div>");

		out.close();
		return res.toString();
	}

	protected String getPreviewZoneId() {
		return "picture-zone-" + getId();
	}

	@Override
	public String getPreviewCode(ContentContext ctx, int maxDisplayedImage) throws Exception {
		return getPreviewCode(ctx, maxDisplayedImage, false);
	}
	
	protected String[] getFileList(String directory) {
		return getFileList(directory, new ImageFileFilter());
	}

	public String getPreviewCode(ContentContext ctx, int maxDisplayedImage, boolean imageList) throws Exception {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		String[] images = getFileList(getFileDirectory(ctx));
		String currentFileLink = URLHelper.mergePath(getDirSelected(), getFileName());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

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
			out.println("<img src=\"" + url + "\" />&nbsp;");
			if (!isFromShared(ctx)) {
				out.println("<div class=\"focus-point\">x</div>");
				out.println("<input class=\"posx\" type=\"hidden\" name=\"posx-" + file.getId() + "\" value=\"" + file.getFocusZoneX() + "\" />");
				out.println("<input class=\"posy\" type=\"hidden\" name=\"posy-" + file.getId() + "\" value=\"" + file.getFocusZoneY() + "\" />");
				out.println("<input class=\"path\" type=\"hidden\" name=\"image_path-" + file.getId() + "\" value=\"" + URLHelper.mergePath(getRelativeFileDirectory(ctx), getDirSelected()) + "\" />");
				out.println("</div></div>");
				out.println("<script type=\"text/javascript\">initFocusPoint();</script>");
			} else {
				out.println("</div></div>");
			}
		} else {
			imageList = true;
		}

		if (imageList) {
			out.println("<div class=\"name\">" + getFileName() + "</div>");
			out.println("</div><div class=\"image-list\">");
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
					out.print("<figcaption><a href=\"" + realURL + "\">" + image + "</a></figcaption></figure>");
					out.print("</div>");
					// }
				}
			}
			out.println("</div>");
		} else {
			params = new HashMap<String, String>();
			params.put("webaction", "image.loadImages");
			params.put("comp_id", getId());
			String ajaxURL = URLHelper.createAjaxURL(ctx, params);
			if (ctx.isEditPreview()) {
				ajaxURL = URLHelper.addParam(ajaxURL, ContentContext.PREVIEW_EDIT_PARAM, "true");
			}
			out.println("<div class=\"action\">");
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			out.println("<a class=\"action-button ajax\" href=\"" + ajaxURL + "\">" + i18nAccess.getText("content.image.load") + "</a>");
			out.println("</div>");
		}

		// TODO : create this javascrit method with a other mecanism
		/*
		 * out.println("<script language=\"javascript\">");
		 * out.println("autoScroll.delay(250);"); out.println("</script>");
		 */
		out.close();
		return res.toString();
	}

	protected int getMaxPreviewImages() {
		return Integer.MAX_VALUE;
	}

	@Override
	protected String getRelativeFileDirectory(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return staticConfig.getImageFolder();
	}

	@Override
	public String getFileDirectory(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String folder = URLHelper.mergePath(globalContext.getDataFolder(), getRelativeFileDirectory(ctx));
		return folder;
	}

	@Override
	public String createFileURL(ContentContext ctx, String url) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		String outURL = URLHelper.createStaticURL(ctx, getPage(), staticConfig.getImageFolder() + '/' + url).replace('\\', '/');
		return outURL;
	}

	/*
	 * @see org.javlo.itf.IContentVisualComponent#getType()
	 */
	@Override
	public String getType() {
		return "image";
	}

	@Override
	public String getHexColor() {
		return GRAPHIC_COLOR;
	}

	@Override
	public String getResourceURL(ContentContext ctx) {
		return getResourceURL(ctx, getFileName());
	}

	public String getResourceURL(ContentContext ctx, String fileLink) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		if (isFromShared(ctx)) {
			return URLHelper.mergePath(staticConfig.getShareDataFolderKey(), staticConfig.getImageFolderName(), getDirSelected(), fileLink.replaceFirst(staticConfig.getShareDataFolderKey(), ""));
		} else {
			return URLHelper.mergePath(staticConfig.getImageFolder(), URLHelper.mergePath(getDirSelected(), fileLink));
		}
	}

	@Override
	public String getTitle(ContentContext ctx) {
		String title = getLabel();
		if (title == null || title.trim().length() == 0) {
			try {
				title = StaticInfo.getInstance(ctx, new File(getFileDirectory(ctx))).getTitle(ctx);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return title;
	}

	@Override
	public String getHelpURI(ContentContext ctx) {
		return "/components/image.html";
	}

	public String getStaticLabel(ContentContext ctx) {
		if (getLabel() != null && getLabel().trim().length() > 0) {
			return getLabel();
		}
		StaticInfo info = getStaticInfo(ctx);
		if (info == null || info.getDescription(ctx) == null || info.getDescription(ctx).trim().length() == 0 && getLabel().trim().length() > 0) {
			return "";
		} else {
			return info.getFullDescription(ctx);
		}
	}

	@Override
	public String getImageDescription(ContentContext ctx) {
		return getStaticLabel(ctx);
	}

	@Override
	public boolean isImageValid(ContentContext ctx) {
		return getValue().trim().length() > 0;
	}

	@Override
	public String getImageLinkURL(ContentContext ctx) {
		return null;
	}

	@Override
	public String getLanguage(ContentContext ctx) {
		return getComponentBean().getLanguage();
	}

	@Override
	public Date getDate(ContentContext ctx) {
		StaticInfo staticInfo;
		try {
			staticInfo = StaticInfo.getInstance(ctx, getFile(ctx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return staticInfo.getDate(ctx);
	}

	@Override
	public String getLocation(ContentContext ctx) {
		StaticInfo staticInfo;
		try {
			staticInfo = StaticInfo.getInstance(ctx, getFile(ctx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return staticInfo.getLocation(ctx);
	}

	@Override
	public String getURL(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		String fileLink = URLHelper.mergePath(getDirSelected(), getFileName());
		String url = URLHelper.createResourceURL(ctx, getPage(), staticConfig.getImageFolder() + '/' + fileLink).replace('\\', '/');
		return url;
	}

	@Override
	public String getCssClass(ContentContext ctx) {
		return getType();
	}

	@Override
	public String getDescription(ContentContext ctx) {
		StaticInfo staticInfo;
		try {
			staticInfo = StaticInfo.getInstance(ctx, getFile(ctx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return staticInfo.getDescription(ctx);
	}

	@Override
	public File getFile(ContentContext ctx) {
		return new File(URLHelper.mergePath(getFileDirectory(ctx), getDirSelected(), getFileName()));
	}

	@Override
	public boolean isShared(ContentContext ctx) {
		StaticInfo staticInfo;
		try {
			staticInfo = StaticInfo.getInstance(ctx, getFile(ctx));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return staticInfo.isShared(ctx);
	}

	@Override
	public List<String> getTags(ContentContext ctx) {
		StaticInfo staticInfo;
		try {
			staticInfo = StaticInfo.getInstance(ctx, getFile(ctx));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return staticInfo.getTags(ctx);
	}

	@Override
	public String getPreviewURL(ContentContext ctx, String filter) {
		String fileLink = URLHelper.mergePath(getDirSelected(), getFileName());
		String url = null;
		try {
			url = URLHelper.createTransformURL(ctx, getPage(), getResourceURL(ctx, fileLink), filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return url;
	}

	@Override
	public int getPopularity(ContentContext ctx) {
		StaticInfo staticInfo = getStaticInfo(ctx);
		if (staticInfo != null) {
			try {
				return staticInfo.getAccessFromSomeDays(ctx);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	@Override
	public String getActionGroupName() {
		return "image";
	}

	public static String performLoadImages(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String compId = rs.getParameter("comp_id", null);
		if (compId != null) {
			Image comp = (Image) ContentService.getInstance(ctx.getRequest()).getComponent(ctx, compId);
			String previewCode = comp.getPreviewCode(ctx, comp.getMaxPreviewImages(), true);
			ctx.addAjaxInsideZone(comp.getPreviewZoneId(), previewCode);
			return null;
		}
		return "error on request structure.";

	}

	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		super.initContent(ctx);
		if (isEditOnCreate(ctx)) {
			return false;
		}

		String defaultFileName = "default.jpg";
		File defaultFile = new File(URLHelper.mergePath(getFileDirectory(ctx), defaultFileName));

		if (defaultFile.exists()) {
			setDirSelected("");
			setFileName("default.jpg");
			storeProperties();
			setStyle(ctx, STYLE_CENTER);
			setModify();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int getPriority(ContentContext ctx) {
		if (getConfig(ctx).getProperty("image.priority", null) == null) {
			return 5;
		} else {
			return Integer.parseInt(getConfig(ctx).getProperty("image.priority", null));
		}
	}

	protected boolean isAllowRAW(ContentContext ctx) {
		return StringHelper.isTrue(getConfig(ctx).getProperty("filter.allow-raw", null), true);
	}
}
