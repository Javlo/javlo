package org.javlo.component.image;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
import org.javlo.ztatic.StaticInfo;

/**
 * simple image without filter.
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class Image extends AbstractFileComponent implements IImageTitle, IPreviewable, IStaticResource {

	public static final String STYLE_CENTER = "image-center";

	@Override
	public String[] getStyleList(ContentContext ctx) {
		String[] superList = super.getStyleList(ctx);
		if (superList != null && superList.length > 0) {
			return superList;
		} else {
			return new String[] { STYLE_CENTER, "image-left", "image-right", HIDDEN, MOBILE_TYPE };
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
		return getComponentCssClass(ctx) + ' ' + getType();
	}

	protected ImageConfig config = null;

	@Override
	public void init(ComponentBean bean, ContentContext ctx) throws Exception {
		super.init(bean, ctx);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		config = ImageConfig.getInstance(globalContext, ctx.getRequest().getSession(), ctx.getCurrentTemplate());
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
			res.append("<div " + getPrefixCssClass(ctx, getCSSClassName(ctx)) + getSpecialPreviewCssId(ctx) + "><img src=\"");
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
	}

	@Override
	protected String getCSSType() {
		return "image";
	}

	protected String[] getFileList(String directory) {
		return getFileList(directory, new ImageFileFilter());
	}

	@Override
	protected String getRelativeFileDirectory(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return staticConfig.getImageFolder();
	}

	@Override
	protected String getDisplayAllLabel(I18nAccess i18nAccess) {
		return i18nAccess.getText("content.image.load");
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
		if (isHiddenInMode(ctx, ctx.getRenderMode(), ctx.isMobile())) {
			return false;
		} else {
			return StringHelper.isImage(getFileName());			
		}		
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
		try {
			return new File(URLHelper.mergePath(getFileDirectory(ctx.getContextOnPage(getPage())), getDirSelected(), getFileName()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
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
	
	protected File getDefaultFile(ContentContext ctx) {		
		File defaultFile = new File(URLHelper.mergePath(getFileDirectory(ctx), "default.jpg"));
		if (defaultFile.exists()) {
			return defaultFile;
		} else {
			defaultFile = new File(URLHelper.mergePath(getFileDirectory(ctx), "default.png"));
			if (defaultFile.exists()) {
				return defaultFile;
			}
		}
		return null;
	}	

	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		super.initContent(ctx);
		if (isEditOnCreate(ctx)) {
			return false;
		}
		File defaultFile = getDefaultFile(ctx);
		if (defaultFile != null) {
			setDirSelected("");
			setFileName(defaultFile.getName());
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

	@Override
	public boolean isUploadOnDrop() {
		return false;
	}

	@Override
	public boolean isLocal(ContentContext ctx) {
		return false;
	}

	@Override
	protected String getMainFolder(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return staticConfig.getImageFolderName();
	}
	
	@Override
	public String getFontAwesome() {	
		return "picture-o";
	}
	
	@Override
	public boolean isMobileOnly(ContentContext ctx) {
		return getStyle().equals(MOBILE_TYPE);
	}
}
