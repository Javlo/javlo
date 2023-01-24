/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.IReverseLinkComponent;
import org.javlo.component.image.IImageTitle;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.ReverseLinkService;
import org.javlo.ztatic.StaticInfo;

/**
 * @author pvandermaesen
 */
public class GenericFile extends AbstractFileComponent implements IReverseLinkComponent, IImageTitle {

	public static final String TYPE = "file";

	private static final String HIDDEN = "hidden";

	@Override
	public String createFileURL(ContentContext ctx, String inURL) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		String url = ElementaryURLHelper.createStaticURL(ctx, staticConfig.getFileFolder() + '/' + inURL).replace('\\', '/');

		return url;
	}

	@Override
	protected String getDeleteTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-file.delete-file");
	}

	@Override
	public String getFileDirectory(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String folder = ElementaryURLHelper.mergePath(globalContext.getDataFolder(), getRelativeFileDirectory(ctx));
		return folder;
	}

	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

	@Override
	protected String getImageChangeTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-file.change");
	}

	@Override
	protected String getImageUploadTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-file.add");
	}

	@Override
	public String getLinkText(ContentContext ctx) {
		return getLabel();
	}

	@Override
	public String getLinkURL(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		if (staticConfig == null) {
			return "";
		}
		String url = ElementaryURLHelper.mergePath(getDirSelected(ctx), getFileName(ctx));
		url = URLHelper.createResourceURL(ctx, getPage(), staticConfig.getFileFolder() + '/' + url);
		return url;
	}
	
	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		String colPrefix = getColomnablePrefix(ctx);
		if (isDisplayHidden() && ctx.isAsViewMode()) {
			return "";
		}
		if (isWrapped(ctx)) {
			return colPrefix+super.getPrefixViewXHTMLCode(ctx) + "<div class=\"" + getType() + ' ' + StringHelper.neverNull(StringHelper.getFileExtension(getFileName(ctx))) + "\">";
		} else {
			return colPrefix;
		}
	}
	
	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		String colSuffix = getColomnableSuffix(ctx);
		if (isDisplayHidden() && ctx.isAsViewMode()) {
			return "";
		}
		if (isWrapped(ctx)) {
			return colSuffix+"</div>" + super.getSuffixViewXHTMLCode(ctx);
		} else {
			return colSuffix;
		}
		
	}

	@Override
	protected String getRelativeFileDirectory(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return staticConfig.getFileFolder();
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		if (super.getStyleList(ctx).length > 0) {
			return super.getStyleLabelList(ctx);
		} else {
			I18nAccess i18nAccess;
			try {
				i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				return new String[] { "standard", i18nAccess.getText("global.hidden") };
			} catch (Exception e) {
				e.printStackTrace();
			}
			return getStyleList(ctx);
		}
	}

	@Override
	public List<File> getFiles(ContentContext ctx) {
		List<File> files = new LinkedList<File>();
		File file = getFile(ctx);
		if (file != null) {
			files.add(file);
		}
		return files;
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		if (super.getStyleList(ctx).length > 0) {
			return super.getStyleList(ctx);
		} else {
			return new String[] { "standard", HIDDEN };
		}
	}

	/*
	 * @see org.javlo.itf.IContentVisualComponent#getType()
	 */
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getURL(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		String fileLink = URLHelper.mergePath(getDirSelected(ctx), getFileName(ctx));
		return URLHelper.createMediaURL(ctx, getPage(), staticConfig.getFileFolder() + '/' + fileLink).replace('\\', '/');
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		final String FILTER = "list";
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String fullName = ElementaryURLHelper.mergePath(getDirSelected(ctx), getFileName(ctx));
		fullName = ElementaryURLHelper.mergePath(globalContext.getStaticConfig().getFileFolder(), fullName);
		ctx.getRequest().setAttribute("imagePreview", URLHelper.createTransformURL(ctx, fullName, FILTER));
		fullName = ElementaryURLHelper.mergePath(globalContext.getDataFolder(), fullName);
		ctx.getRequest().setAttribute("ext", StringHelper.getFileExtension(getFileName(ctx)).toLowerCase());
		ctx.getRequest().setAttribute("size", StringHelper.getFileSize(fullName));
		ctx.getRequest().setAttribute("filter", FILTER);
		ctx.getRequest().setAttribute("isLabel", !StringHelper.isEmpty(getLabel()));
		if (getLabel().trim().length() == 0) {
			ctx.getRequest().setAttribute("label", getFileName(ctx));
		} else {
			ctx.getRequest().setAttribute("label", XHTMLHelper.textToXHTML(getLabel()));
		}
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		prepareView(ctx);

		if (getComponentCssClass(ctx).equals(HIDDEN)) {
			return "";
		}

		StringBuffer res = new StringBuffer();
		if ((getValue() != null) && (getValue().trim().length() > 0)) {
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());

			String url = ElementaryURLHelper.mergePath(getDirSelected(ctx), getFileName(ctx));
			url = URLHelper.createResourceURL(ctx, getPage(), staticConfig.getFileFolder() + '/' + url);

			String rel = "";
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			String openAsPopup = "";
			if (globalContext.isOpenFileAsPopup()) {
				openAsPopup = "target=\"_blank\" ";
			}

			res.append("<a " + getPrefixCssClass(ctx, getComponentCssClass(ctx)) + getSpecialPreviewCssId(ctx) + " " + rel + openAsPopup + "href=\"");
			if (ctx.getRenderMode() != ContentContext.PAGE_MODE) {
				res.append(StringHelper.toXMLAttribute(url));
			} else {
				ContentContext viewCtx = new ContentContext(ctx);
				viewCtx.setRenderMode(ContentContext.VIEW_MODE);
				res.append(URLHelper.addMailingFeedback(ctx, StringHelper.toXMLAttribute(url)));
			}
			res.append("\">");
			/*
			 * if (XHTMLHelper.getFileIcone(ctx, getFileName(ctx)).length() > 0) {
			 * res.append(XHTMLHelper.getFileIcone(ctx, getFileName(ctx)) +
			 * "&nbsp;"); }
			 */
			if (getLabel().trim().length() == 0) {
				res.append(getFileName(ctx));
			} else {
				res.append(textToXHTML(getLabel()));
			}
			String fullName = ElementaryURLHelper.mergePath(getDirSelected(ctx), getFileName(ctx));
			fullName = ElementaryURLHelper.mergePath(staticConfig.getFileFolder(), fullName);

			fullName = ElementaryURLHelper.mergePath(globalContext.getDataFolder(), fullName);
			res.append(" <span class=\"info\">(<span class=\"format\">" + StringHelper.getFileExtension(getFileName(ctx)) + "</span> <span class=\"size\">" + ctx.getRequest().getAttribute("size") + "</span>)</span></a>");
			if ((getDescription().trim().length() > 0) && (ctx.getRenderMode() != ContentContext.EDIT_MODE)) {
				res.append("<span class=\"description\">" + getDescription() + "</span>");
			}
		} else {
			res.append("&nbsp; <!--FILE NOT DEFINED--> ");
		}
		return res.toString();
	}

	@Override
	public boolean isInline() {
		return true;
	}

	@Override
	public boolean isListable() {
		return true;
	}

	@Override
	public boolean isOnlyThisPage() {
		return properties.getProperty(REVERSE_LINK_KEY, "none").equals(ReverseLinkService.ONLY_THIS_PAGE);
	}

	@Override
	public boolean isOnlyPreviousComponent() {
		return properties.getProperty(REVERSE_LINK_KEY, "none").equals(ReverseLinkService.ONLY_PREVIOUS_COMPONENT);
	}

	@Override
	public boolean isReverseLink() {
		return super.isReverseLink();
	}

	@Override
	public boolean isWithDescription() {
		return true;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		IContentVisualComponent previousComp = getPreviousComponent();
		if (previousComp == null || !previousComp.getType().equals(TYPE)) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public int getPopularity(ContentContext ctx) {
		StaticInfo staticInfo;
		try {
			staticInfo = StaticInfo.getInstance(ctx, getFile(ctx));
			if (staticInfo != null) {
				return staticInfo.getAccessFromSomeDays(ctx);
			}
		} catch (Exception e) {
		}
		return 0;
	}

	@Override
	public String getListGroup() {
		return "link";
	}

	@Override
	public boolean isUploadOnDrop() {
		return false;
	}

	@Override
	public String getFirstPrefix(ContentContext ctx) {
		if (!isList(ctx)) {
			return getConfig(ctx).getProperty("prefix.first", "");
		} else {
			String cssClass = "";
			if (getComponentCssClass(ctx) != null && getComponentCssClass(ctx).trim().length() > 0) {
				cssClass = ' ' + getComponentCssClass(ctx);
			}
			if (getListClass(ctx) != null) {
				cssClass = cssClass + ' ' + getListClass(ctx);
			}
			return "<" + getListTag(ctx) + " class=\"" + getType() + cssClass + ' ' + getCurrentRenderer(ctx) + "\">";
		}
	}
	
	@Override
	protected String getMainFolder(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return staticConfig.getFileFolderName();
	}

	@Override
	public String getImageDescription(ContentContext ctx) {
		return getLabel();
	}

	@Override
	public String getImageLinkURL(ContentContext ctx) {
		return null;
	}

	@Override
	public boolean isImageValid(ContentContext ctx) {
		return StringHelper.isPDF(getFileName(ctx));
	}

	@Override
	public int getPriority(ContentContext ctx) {
		return 1;
	}

	@Override
	public boolean isMobileOnly(ContentContext ctx) {
		return false;
	}
	
	@Override
	public String getIcon() {
		return "bi bi-file-binary";
	}

}
