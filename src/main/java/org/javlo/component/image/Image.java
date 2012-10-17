package org.javlo.component.image;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IPreviewable;
import org.javlo.component.core.IStaticResource;
import org.javlo.component.files.AbstractFileComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.image.ImageConfig;
import org.javlo.module.file.FileAction;
import org.javlo.ztatic.StaticInfo;

/**
 * simple image without filter.
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class Image extends AbstractFileComponent implements IImageTitle, IPreviewable, IStaticResource {

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "image-left", "image-right", "image-center", HIDDEN };
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		String left = "left";
		String right = "right";
		String center = "center";
		String hidden = "hidden";
		try {
			I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
			left = i18n.getText("global.left");
			right = i18n.getText("global.right");
			center = i18n.getText("global.center");
			hidden = i18n.getText("global.hidden");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String[] { left, right, center, hidden };
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		return "position";
	}

	public String getCSSClassName(ContentContext ctx) {
		return getStyle(ctx) + ' ' + getType();
	}

	protected ImageConfig config = null;

	@Override
	public void init(ComponentBean bean, ContentContext ctx) throws Exception {
		super.init(bean, ctx);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		// Warning : with javlo 1.4 this line has use a other ImageConfig from component.config package, check if regression
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
	protected String getCSSType() {
		return "image";
	}

	@Override
	protected String getPreviewCode(ContentContext ctx) throws Exception {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		out.println("<div id=\"" + getPreviewZoneId() + "\" class=\"list-container\" style=\"height: 220px; overflow: scroll; text-align: center;\">");
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
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		String[] images = getFileList(getFileDirectory(ctx));
		String currentFileLink = URLHelper.mergePath(getDirSelected(), getFileName());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		out.println("<div class=\"image-selected\">");

		FileAction.FileBean file = new FileAction.FileBean(ctx, getFile(ctx));
		out.println("<div class=\"focus-zone\">");
		out.println("<div id=\"" + getPreviewZoneId() + "\" class=\"list-container\">");
		out.println("<img src=\"" + URLHelper.createTransformURL(ctx, getPage(), getResourceURL(ctx, getFileName()), "list") + "\" />&nbsp;");
		out.println("<div class=\"focus-point\">x</div>");
		out.println("<input class=\"posx\" type=\"hidden\" name=\"posx-" + file.getId() + "\" value=\"" + file.getFocusZoneX() + "\" />");
		out.println("<input class=\"posy\" type=\"hidden\" name=\"posy-" + file.getId() + "\" value=\"" + file.getFocusZoneY() + "\" />");
		out.println("<input class=\"path\" type=\"hidden\" name=\"image_path-" + file.getId() + "\" value=\"" + URLHelper.mergePath(getRelativeFileDirectory(ctx), getDirSelected()) + "\" />");
		out.println("</div></div>");
		out.println("<script type=\"text/javascript\">initFocusPoint();</script>");

		out.println("<div class=\"name\">" + getFileName() + "</div>");
		out.println("</div><div class=\"image-list\">");
		for (int i = 0; i < images.length; i++) {
			if ((images[i] != null) && (images[i].trim().length() > 0)) {
				StaticInfo staticInfo = StaticInfo.getInstance(ctx, getFileURL(ctx, images[i]));
				String fileLink = URLHelper.mergePath(getDirSelected(), images[i]);
				String selected = "class=\"preview-image\"";
				boolean isSelectedImage = false;
				if (fileLink.equals(currentFileLink)) {
					selected = " class=\"preview-image selected\"";
					isSelectedImage = true;
				}
				String realURL = URLHelper.createResourceURL(ctx, getPage(), '/' + getResourceURL(ctx, images[i])) + "?CRC32=" + staticInfo.getCRC32();
				String previewURL = URLHelper.createTransformURL(ctx, getPage(), getResourceURL(ctx, images[i]), "preview") + "?CRC32=" + staticInfo.getCRC32();
				String url = URLHelper.createTransformURL(ctx, getPage(), getResourceURL(ctx, images[i]), getConfig(ctx).getProperty("thumbnails-filter", "thumbnails")) + "?CRC32=" + staticInfo.getCRC32();
				String id = "image_name_select__" + getId();
				if (i < maxDisplayedImage || isSelectedImage) {
					out.print("<div " + selected + ">");
					String onMouseOver = "";
					if (globalContext.isImagePreview()) {
						onMouseOver = " onMouseOver=\"previewImage('" + previewURL + "')\" onMouseOut=\"previewClear()\"";
					}
					out.print("<a class=\"image\" href=\"#\" onclick=\"jQuery('#" + id + "').val('" + images[i] + "');jQuery('#" + id + "').trigger('change');" + getJSOnChange(ctx) + "\"><img name=\"" + getImageImgName() + "\"" + onMouseOver + " src=\"");
					out.print(url);
					out.print("\" alt=\"\">&nbsp;</a>");
					out.print("<div class=\"name\"><a href=\"" + realURL + "\">" + images[i] + "</a></div>");
					out.print("</div>");
				}
			}
		}
		out.println("</div>");

		// TODO : create this javascrit method with a other mecanism
		/*
		 * out.println("<script language=\"javascript\">"); out.println("autoScroll.delay(250);"); out.println("</script>");
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
		return URLHelper.mergePath(staticConfig.getImageFolder(), URLHelper.mergePath(getDirSelected(), fileLink));
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
}
