/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.IReverseLinkComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.ReverseLinkService;
import org.javlo.ztatic.StaticInfo;

/**
 * @author pvandermaesen
 */
public class SimpleVideo extends AbstractFileComponent implements IReverseLinkComponent {

	public static final String TYPE = "simple-video";

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
		String url = ElementaryURLHelper.mergePath(getDirSelected(), getFileName());
		url = URLHelper.createResourceURL(ctx, getPage(), staticConfig.getFileFolder() + '/' + url);
		return url;
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return super.getPrefixViewXHTMLCode(ctx) + "<div class=\"" + getType() + ' ' + StringHelper.neverNull(StringHelper.getFileExtension(getFileName())) + "\">";
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

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "</div>" + super.getSuffixViewXHTMLCode(ctx);
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
		String fileLink = URLHelper.mergePath(getDirSelected(), getFileName());
		return URLHelper.createResourceURL(ctx, getPage(), staticConfig.getFileFolder() + '/' + fileLink).replace('\\', '/');
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);		
		if ((getValue() != null) && (getValue().trim().length() > 0)) {
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
			String url = ElementaryURLHelper.mergePath(getDirSelected(), getFileName());
			url = URLHelper.createResourceURL(ctx, getPage(), staticConfig.getFileFolder() + '/' + url);
			ctx.getRequest().setAttribute("url", url);
			ctx.getRequest().setAttribute("infoHTML", XHTMLHelper.renderStaticInfo(ctx, getStaticInfo(ctx)));
		}
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		prepareView(ctx);
		if (getStyle(ctx).equals(HIDDEN)) {
			return "";
		}
		StringBuffer res = new StringBuffer();
		if ((getValue() != null) && (getValue().trim().length() > 0)) {
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
			String url = ElementaryURLHelper.mergePath(getDirSelected(), getFileName());
			url = URLHelper.createResourceURL(ctx, getPage(), staticConfig.getFileFolder() + '/' + url);
			StaticInfo info = getStaticInfo(ctx);
			res.append(XHTMLHelper.renderStaticInfo(ctx, info));
			res.append("<video controls><source src=\""+url+"\" type=\""+ResourceHelper.getFileExtensionToMineType(StringHelper.getFileExtension(url))+"\" />Your browser does not support the audio element.</video>");
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
		return false;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return false;
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
		return true;
	}

	@Override
	public String getFirstPrefix(ContentContext ctx) {
		if (!isList(ctx)) {
			return getConfig(ctx).getProperty("prefix.first", "");
		} else {
			String cssClass = "";
			if (getStyle(ctx) != null && getStyle(ctx).trim().length() > 0) {
				cssClass = ' ' + getStyle(ctx);
			}
			if (getListClass(ctx) != null) {
				cssClass = cssClass + ' ' + getListClass(ctx);
			}
			return "<" + getListTag(ctx) + " class=\"" + getType() + cssClass + ' ' + getCurrentRenderer(ctx) + "\">";
		}
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}
	
	@Override
	protected String getMainFolder(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return staticConfig.getFileFolderName();
	}

}
