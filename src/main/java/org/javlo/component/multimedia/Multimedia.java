package org.javlo.component.multimedia;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.IVideo;
import org.javlo.component.image.IImageTitle;
import org.javlo.component.meta.TimeRangeComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.PaginationContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.filefilter.HTMLFileFilter;
import org.javlo.helper.filefilter.ImageFileFilter;
import org.javlo.helper.filefilter.SoundFileFilter;
import org.javlo.helper.filefilter.VideoOrURLFileFilter;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.file.FileAction;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.ztatic.StaticInfo;

/**
 * standard image component. <h4>exposed variable :</h4>
 * <ul>
 * <li>inherited from {@link AbstractVisualComponent}</li>
 * <li>{@link String} title : the title.</li>
 * <li>{@link PaginationContext} pagination : pagination context.</li>
 * <li>{@link MultimediaResource} resources : list of resources to be displayed.</li>
 * </ul>
 * 
 * @author pvandermaesen
 */
public class Multimedia extends TimeRangeComponent implements IImageTitle {

	public static final String TYPE = "multimedia";
	protected static final String STATIC_VIDEO_FOLDER = "videos";
	protected static final String STATIC_SOUND_FOLDER = "sounds";
	protected static final String STATIC_IMAGE_FOLDER = "images";

	protected static final String STATIC_EMBED_FOLDER = "embed";
	protected static final String IMAGE_FIRST = "image first";
	protected static final String IMAGE_AFTER_EXEPT_FIRST = "only first item with image first";

	protected static final String ORDER_BY_ACCESS = "order by access";
	protected static final String REVERSE_ORDER = "reverse order";
	protected static final String NAME_ORDER = "name order";	

	public static final String ALL = "all";
	public static final String IMAGE = "image";
	public static final String SOUND = "sound";
	public static final String VIDEO = "video";
	public static final String EMBED = "embed";

	protected boolean acceptStaticInfo(ContentContext ctx, StaticInfo info) {

		Collection<String> tags = getTags();
		if (tags.size() > 0) {
			tags = new HashSet<String>(tags);
			tags.retainAll(info.getTags(ctx));
			if (tags.size() == 0) {
				return false;
			}
		}

		Calendar currentDate = GregorianCalendar.getInstance();
		if (info.getDate(ctx) != null) {
			currentDate.setTime(info.getDate(ctx));
		}
		Calendar startDate = GregorianCalendar.getInstance();
		if (getStartDate() == null) {
			startDate = null;
		} else {
			startDate.setTime(getStartDate());
		}
		Calendar endDate = GregorianCalendar.getInstance();
		if (getEndDate() == null) {
			endDate = null;
		} else {
			endDate.setTime(getEndDate());
		}

		boolean afterAccept = true;
		if (startDate != null && !currentDate.after(startDate)) {
			afterAccept = false;
		}

		boolean beforeAccept = true;
		if (endDate != null && !currentDate.before(endDate)) {
			beforeAccept = false;
		}

		if (info.getDate(ctx) == null && (startDate != null || endDate != null)) {
			afterAccept = false;
			beforeAccept = false; // not necessary, just more "clean" :-)
		}

		return (info.isShared(ctx) || !isDisplayOnlyShared()) && afterAccept && beforeAccept;
	}

	protected boolean acceptResource(ContentContext ctx, MultimediaResource info) {

		Collection<String> tags = getTags();
		if (tags.size() > 0) {
			tags = new HashSet<String>(tags);
			tags.retainAll(info.getTags());
			if (tags.size() == 0) {
				return false;
			}
		}

		Calendar currentDate = GregorianCalendar.getInstance();
		if (info.getDate() != null) {
			currentDate.setTime(info.getDate());
		} else {
			return true;
		}

		Calendar startDate = null;
		if (getStartDate() == null) {
			startDate = null;
		} else {
			startDate = GregorianCalendar.getInstance();
			startDate.setTime(getStartDate());
		}
		Calendar endDate = null;
		if (getEndDate() == null) {
			endDate = null;
		} else {
			endDate = GregorianCalendar.getInstance();
			endDate.setTime(getEndDate());
		}

		boolean afterAccept = true;
		if (startDate != null && !currentDate.after(startDate)) {
			afterAccept = false;
		}

		boolean beforeAccept = true;
		if (endDate != null && !currentDate.before(endDate)) {
			beforeAccept = false;
		}

		if (info.getDate() == null && (startDate != null || endDate != null)) {
			afterAccept = false;
			beforeAccept = false; // not necessary, just more "clean" :-)
		}

		return afterAccept && beforeAccept;
	}

	protected boolean displayEmbed(ContentContext ctx) {
		return getStyle().equals(ALL) || getStyle().equals(EMBED);
	}

	protected boolean displayImage(ContentContext ctx) {
		return getStyle().equals(ALL) || getStyle().equals(IMAGE);
	}

	protected boolean displaySound(ContentContext ctx) {
		return getStyle().equals(ALL) || getStyle().equals(SOUND);
	}

	protected boolean displayVideo(ContentContext ctx) {
		return getStyle().equals(ALL) || getStyle().equals(VIDEO);
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

	protected String getImageFilter(ContentContext ctx) {
		return getConfig(ctx).getProperty("image-filter", "preview");
	}

	protected ContentContext getValidVideoCtx(ContentContext ctx, IVideo video) {
		if (video.isRealContent(ctx)) {
			return ctx;
		}
		ContentContext lgCtx = new ContentContext(ctx);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Collection<String> languages = globalContext.getContentLanguages();
		for (String lg : languages) {
			lgCtx.setAllLanguage(lg);
			if (video.isRealContent(lgCtx)) {
				return lgCtx;
			}
		}
		return ctx;
	}

	private boolean contentVideoOnlyShared(ContentContext ctx) {
		return StringHelper.isTrue(getConfig(ctx).getProperty("only-shared", null));
	}

	protected MultimediaResource createResource(ContentContext ctx, IVideo video) {
		MultimediaResource resource = new MultimediaResource();
		ContentContext lgCtx = getValidVideoCtx(ctx, video);
		resource.setURL(video.getURL(lgCtx));
		resource.setDescription(video.getImageDescription(lgCtx));
		resource.setPreviewURL(video.getPreviewURL(ctx, getImageFilter(lgCtx)));
		resource.setDate(video.getDate(lgCtx));
		resource.renderDate(lgCtx);
		resource.setLocation(video.getLocation(lgCtx));
		resource.setCssClass(video.getCssClass(lgCtx));
		resource.setTitle(video.getTitle(lgCtx));
		resource.setTags(video.getTags(lgCtx));
		resource.setIndex(video.getPopularity(lgCtx));
		resource.setLanguage(lgCtx.getRequestContentLanguage());
		resource.setId(video.getId());
		return resource;
	}

	protected List<MultimediaResource> getContentVideo(ContentContext ctx) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		List<MultimediaResource> outResources = new LinkedList<MultimediaResource>();
		ContentContext freeCtx = ctx.getContextWithArea(null);
		freeCtx.setFree(true);
		MenuElement page = content.getNavigation(freeCtx);
		ContentContext lgCtx = freeCtx.getContextWithContent(page);
		if (lgCtx == null) {
			lgCtx = freeCtx;
		}
		ContentElementList comps = page.getAllContent(lgCtx);
		while (comps.hasNext(lgCtx)) {
			IContentVisualComponent comp = comps.next(lgCtx);
			if (comp instanceof IVideo) {
				IVideo video = (IVideo) comp;
				if (video.isShared(lgCtx) || !contentVideoOnlyShared(lgCtx)) {
					MultimediaResource resource = createResource(ctx, video);
					outResources.add(resource);
				}
			}
		}
		MenuElement[] children = page.getAllChildren();
		for (MenuElement child : children) {
			lgCtx = freeCtx.getContextWithContent(child);
			if (lgCtx == null) {
				lgCtx = freeCtx;
			}
			comps = child.getAllContent(lgCtx);
			while (comps.hasNext(lgCtx)) {
				IContentVisualComponent comp = comps.next(lgCtx);
				if (comp instanceof IVideo) {
					IVideo video = (IVideo) comp;
					if (video.isShared(lgCtx) || !contentVideoOnlyShared(lgCtx)) {
						MultimediaResource resource = createResource(ctx, video);
						outResources.add(resource);
					}
				}
			}
		}

		return outResources;
	}

	public Collection<File> getAllMultimediaFiles(ContentContext ctx) {
		List<File> files = new LinkedList<File>();
		Collection<String> filesName = new HashSet<String>();

		/** Images **/
		File imageDir = null;
		if (displayImage(ctx)) {
			File newImageDir = new File(getFilesDirectory(ctx));
			if (imageDir == null || !newImageDir.equals(imageDir)) {
				imageDir = newImageDir;
				if (imageDir.exists()) {
					Collection<File> filesLg = ResourceHelper.getAllFiles(imageDir, new ImageFileFilter());
					for (File file : filesLg) {
						if (!filesName.contains(file.getName())) {
							filesName.add(file.getName());
							files.add(file);
						}
					}
				}
			}
		}

		/** Videos **/
		if (displayVideo(ctx)) {
			File videoDir = new File(getFilesDirectory(ctx));
			if (videoDir.exists()) {
				Collection<File> filesLg = ResourceHelper.getAllFiles(videoDir, new VideoOrURLFileFilter());
				for (File file : filesLg) {
					if (!filesName.contains(file.getName())) {
						filesName.add(file.getName());
						files.add(file);
					}
				}
			}
		}

		/** Sounds **/
		if (displaySound(ctx)) {
			File soundDir = new File(getFilesDirectory(ctx));
			if (soundDir.exists()) {
				Collection<File> filesLg = ResourceHelper.getAllFiles(soundDir, new SoundFileFilter());
				for (File file : filesLg) {
					if (!filesName.contains(file.getName())) {
						filesName.add(file.getName());
						files.add(file);
					}
				}
			}
		}

		/** Embed **/
		if (displayEmbed(ctx)) {
			File embedDir = new File(getFilesDirectory(ctx));
			if (embedDir.exists()) {
				Collection<File> filesLg = ResourceHelper.getAllFiles(embedDir, new HTMLFileFilter());
				for (File file : filesLg) {
					if (!filesName.contains(file.getName())) {
						filesName.add(file.getName());
						files.add(file);
					}
				}
			}
		}

		/*
		 * if (isOrderByAccess(ctx)) { Collections.sort(files, new StaticInfo.StaticFileSortByPopularity(ctx, false)); } else { Collections.sort(files, new StaticInfo.StaticFileSort(ctx, false)); }
		 */

		return files;
	}

	public String getBaseStaticDir(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String folder = URLHelper.mergePath(globalContext.getDataFolder(), getRelativeFileDirectory(ctx));
		folder = folder.replace('\\', '/');
		return folder;
	}

	public String getCurrentRootFolder() {
		String[] values = getValue().split(VALUE_SEPARATOR);
		if (values.length >= 4) {
			return values[3];
		} else {
			return "/";
		}
	}

	@Override
	protected String getDisplayAsInputName() {
		return "display-as-" + getId();
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println(getSpecialInputTag());

		String baseDir = getBaseStaticDir(ctx);
		File rootDir = new File(baseDir);
		Collection<File> files = ResourceHelper.getAllFiles(rootDir, null);
		Collection<String> folderSelection = new LinkedList<String>();
		folderSelection.add("/");
		for (File file : files) {
			if (file.isDirectory() && file.list().length > 0) {
				folderSelection.add(file.getAbsolutePath().replace('\\', '/').replaceFirst(baseDir, ""));
			}
		}

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getInputTitle() + "\">" + i18nAccess.getText("global.title") + "</label>");
		out.println(" : <input style=\"width: 120px;\" type=\"text\" id=\"" + getInputTitle() + "\" name=\"" + getInputTitle() + "\" value=\"" + getTitle() + "\"/>");
		out.println("</div>");

		out.println("<div class=\"line\">");
		if (isFolder()) {
			RequestService requestService = RequestService.getInstance(ctx.getRequest());
			String folder = getCurrentRootFolder();
			String newFolder = URLHelper.removeStaticFolderPrefix(ctx,requestService.getParameter("path", ""));
			if (newFolder.trim().length() > 1) {
				folder = newFolder;
			}
			out.println(XHTMLHelper.getInputOneSelect(getInputBaseFolderName(), folderSelection, folder));
		}

		String backURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "content");
		backURL = URLHelper.addParam(backURL, "comp_id", "cp_" + getId());
		backURL = URLHelper.addParam(backURL, "webaction", "editPreview");
		
		Map<String, String> filesParams = new HashMap<String, String>();
		filesParams.put("path", URLHelper.mergePath(FileAction.getPathPrefix(ctx), getCurrentRootFolder()));
		filesParams.put("webaction", "changeRenderer");
		filesParams.put("page", "meta");
		filesParams.put(ElementaryURLHelper.BACK_PARAM_NAME, backURL);
		
		String staticURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams);
		out.println("&nbsp;<a class=\"" + EDIT_ACTION_CSS_CLASS + "\" href=\"" + staticURL + "\">");
		out.println(i18nAccess.getText("content.goto-static"));
		out.println("</a>");
		out.println("</div>");

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getInputStartDateName() + "\">" + i18nAccess.getText("content.multimedia-gallery.date-range") + "</label>");
		out.println(" : <input id=\"contentdate\" style=\"width: 120px;\" type=\"text\" id=\"" + getInputStartDateName() + "\" name=\"" + getInputStartDateName() + "\" value=\"" + StringHelper.renderDateWithDefaultValue(getStartDate(), "") + "\"/> - ");
		out.println("<input style=\"width: 120px;\" type=\"text\" id=\"" + getInputEndDateName() + "\" name=\"" + getInputEndDateName() + "\" value=\"" + StringHelper.renderDateWithDefaultValue(getEndDate(), "") + "\"/>");
		out.println("</div>");

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getInputMaxListSizeName() + "\">" + i18nAccess.getText("content.multimedia-gallery.list-size") + "</label>");
		out.println(" : <input style=\"width: 120px;\" type=\"text\" id=\"" + getInputMaxListSizeName() + "\" name=\"" + getInputMaxListSizeName() + "\" value=\"" + getMaxListSize() + "\"/>");
		out.println("</div>");

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getInputPageSizeName() + "\">" + i18nAccess.getText("content.multimedia-gallery.page-size") + "</label>");
		out.println(" : <input style=\"width: 120px;\" type=\"text\" id=\"" + getInputPageSizeName() + "\" name=\"" + getInputPageSizeName() + "\" value=\"" + getPageSize() + "\"/>");
		out.println("</div>");
				
		out.println("<div class=\"line\">");
		out.print("<input type=\"checkbox\" name=\"" + getInputNameReverseOrder() + "\" id=\"" + getInputNameReverseOrder() + "\" ");
		if (isReverseOrder(ctx)) {
			out.print("checked=\"checked\" ");
		}
		out.print("/>");
		out.println("<label for=\"" + getInputNameReverseOrder() + "\"> reverse order.</label>");
		out.println("</div>");
		
		out.println("<div class=\"line\">");
		out.print("<input type=\"checkbox\" name=\"" + getInputNameNameOrder() + "\" id=\"" + getInputNameNameOrder() + "\" ");
		if (isNameOrder(ctx)) {
			out.print("checked=\"checked\" ");
		}
		out.print("/>");
		out.println("<label for=\"" + getInputNameNameOrder() + "\"> order by name.</label>");
		out.println("</div>");
		
		
		out.println("<div class=\"line\">");
		out.print("<input type=\"checkbox\" name=\"" + getInputNameOrderByAccess() + "\" id=\"" + getInputNameOrderByAccess() + "\" ");
		if (isOrderByAccess(ctx)) {
			out.print("checked=\"checked\" ");
		}
		out.print("/>");
		out.println("<label for=\"" + getInputNameOrderByAccess() + "\"> order by access.</label>");
		out.println("</div>");

		/* tags */
		Collection<String> tags = globalContext.getTags();
		Collection<String> selectedTags = getTags();
		if (tags.size() > 0) {
			out.println("<fieldset class=\"tags\">");
			out.println("<legend>" + i18nAccess.getText("global.tags") + "</legend>");
			for (String tag : tags) {
				String checked = "";
				if (selectedTags.contains(tag)) {
					checked = " checked=\"checked\"";
				}
				out.println("<span class=\"line-inline\"><input type=\"checkbox\" id=\"" + getInputTag(tag) + "\" name=\"" + getInputTag(tag) + "\"" + checked + " /><label for=\"" + getInputTag(tag) + "\">" + tag + "</label></span>");
			}
			out.println("</fieldset>");
		}

		out.close();
		return writer.toString();
	}

	@Override
	public Collection<String> getExternalResources(ContentContext ctx) {
		Collection<String> resources = new LinkedList<String>();
		/*
		 * resources.add("/js/mootools.js"); resources.add("/js/global.js"); resources.add("/js/shadowbox/src/adapter/shadowbox-base.js"); resources.add("/js/shadowbox/src/shadowbox.js"); resources.add("/js/shadowboxOptions.js"); resources.add("/js/onLoadFunctions.js");
		 */
		return resources;
	}

	public String getFilesDirectory(ContentContext ctx) {
		String fileDir = URLHelper.mergePath(getBaseStaticDir(ctx), getCurrentRootFolder());
		return fileDir;
	}

	protected String getGlobalCssClass() {
		return "multimedia";
	}

	@Override
	public String getHexColor() {
		return GRAPHIC_COLOR;
	}

	protected String getHTMLRelation(ContentContext ctx) {
		return "shadowbox" + getId();
	}

	protected String getImageFilePath(ContentContext ctx, String fileLink) {
		if (StringHelper.isImage(fileLink)) {
			return fileLink;
		} else {
			return FilenameUtils.getBaseName(fileLink) + ".jpg";
		}
	}

	public String getInputBaseFolderName() {
		return "__" + getId() + ID_SEPARATOR + "base-folder";
	}

	public String getInputMaxListSizeName() {
		return "__" + getId() + ID_SEPARATOR + "list-size";
	}

	public String getInputPageSizeName() {
		return "__" + getId() + ID_SEPARATOR + "page-size";
	}

	public String getInputTitle() {
		return "__" + getId() + ID_SEPARATOR + "title";
	}

	protected String getInputNameOrderByAccess() {
		return "order_by_access_" + getId();
	}
	
	protected String getInputNameReverseOrder() {
		return "reverse_order_" + getId();
	}
	
	protected String getInputNameNameOrder() {
		return getInputName("name_order");
	}

	protected String getItemCssClass() {
		return "page-link";
	}

	public int getMaxListSize() {
		int maxListSize = 16;
		try {
			String maxListSizeStr = getValue().split(VALUE_SEPARATOR)[2];
			if (maxListSizeStr.contains(",")) {
				maxListSize = Integer.parseInt(maxListSizeStr.split(",")[1]);
			} else {
				maxListSize = Integer.parseInt(maxListSizeStr);
			}
		} catch (Throwable t) {
			logger.warning(t.getMessage());
		}
		return maxListSize;
	}

	public int getPageSize() {
		int pageSize = 0;
		try {
			String pageSizeStr = getValue().split(VALUE_SEPARATOR)[2];
			if (pageSizeStr.contains(",")) {
				pageSize = Integer.parseInt(pageSizeStr.split(",")[0]);
			}
		} catch (Throwable t) {
			logger.warning(t.getMessage());
		}
		return pageSize;

	}

	public List<String> getTags() {
		String[] data = getValue().split(VALUE_SEPARATOR);
		if (data.length > 5) {
			String rawTags = data[5];
			return StringHelper.stringToCollection(rawTags);
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	public String getTitle() {
		String[] data = getValue().split(VALUE_SEPARATOR);
		if (data.length > 6) {
			return data[6];
		} else {
			return "";
		}
	}

	protected String getMultimediaFilePath(ContentContext ctx, String lg, File file) {
		return URLHelper.mergePath(getFilesDirectory(ctx), file.getName());
	}

	protected String getMultimediaFileURL(ContentContext ctx, File file) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String relativeFileName = file.getAbsolutePath().replace(globalContext.getDataFolder(), "");
		return relativeFileName;
	}

	protected String getMultimediaFileURL(ContentContext ctx, String lg, File file) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String relativeFileName = file.getAbsolutePath().replace(globalContext.getDataFolder(), "");
		return relativeFileName;
	}

	protected String getPreviewFilter(File file) {
		return "preview";
	}

	protected String getRelativeFileDirectory(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return staticConfig.getStaticFolder();
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		return getStyleList(ctx);
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { ALL, IMAGE, VIDEO, SOUND };
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		try {
			I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
			return i18n.getText("content.date.style-title");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	protected String getTitle(ContentContext ctx) {
		return null;
	}

	protected String getTransformFilter(File file) {
		return "video";
	}

	@Override
	public String getType() {
		return TYPE;
	}

	protected MultimediaResource getFirstResource(ContentContext ctx) throws Exception {
		Collection<File> mulFiles = getAllMultimediaFiles(ctx);
		if (mulFiles.size() == 0) {
			return null;
		} else {
			MultimediaResource resource = new MultimediaResource();

			ContentContext lgCtx = ctx.getContextWithContent(getPage());

			File file = mulFiles.iterator().next();

			StaticInfo info = StaticInfo.getInstance(lgCtx, file);

			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

			String fileName = ResourceHelper.removeDataFolderDir(globalContext, file.getAbsolutePath());

			resource.setTitle(info.getTitle(lgCtx));
			resource.setRelation(getHTMLRelation(ctx));
			resource.setLocation(info.getLocation(ctx));
			resource.setDescription(StringHelper.removeTag(info.getFullDescription(lgCtx)));
			resource.setDate(info.getDate(ctx));
			resource.setShortDate(StringHelper.renderDate(resource.getDate(), globalContext.getShortDateFormat()));
			resource.setMediumDate(StringHelper.renderDate(resource.getDate(), globalContext.getMediumDateFormat()));
			resource.setFullDate(StringHelper.renderDate(resource.getDate(), globalContext.getFullDateFormat()));

			resource.setIndex(info.getAccessFromSomeDays(lgCtx));

			resource.setURL(fileName);
			resource.setPreviewURL(fileName);

			return resource;
		}
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		Collection<File> mulFiles = getAllMultimediaFiles(ctx);

		List<MultimediaResource> allResource = new LinkedList<MultimediaResource>();
		Map<String, MultimediaResource> allURL = new HashMap<String, MultimediaResource>();
		
		boolean countAccess = isCountAccess(ctx);
		
		for (File file : mulFiles) {

			String cssClass = "embed";
			if (StringHelper.isVideo(file.getName())) {
				cssClass = "video";
			} else if (StringHelper.isSound(file.getName())) {
				cssClass = "sound";
			} else if (StringHelper.isImage(file.getName())) {
				cssClass = "image";
			}

			MultimediaResource resource = new MultimediaResource();
			resource.setCssClass(cssClass);

			String currentLg = ctx.getRequestContentLanguage();
			File multimediaFile = new File(getMultimediaFilePath(ctx, currentLg, file));
			if (!(multimediaFile.exists())) {
				currentLg = globalContext.getDefaultLanguages().iterator().next();
			} else {
				file = multimediaFile;
			}
			ContentContext lgCtx = new ContentContext(ctx);
			Iterator<String> defaultLg = globalContext.getDefaultLanguages().iterator();
			lgCtx.setRequestContentLanguage(lgCtx.getRequestContentLanguage()); // if the page is defined only in a lang the information must still be display in current lg
			StaticInfo info = StaticInfo.getInstance(lgCtx, file);

			while (!info.isPertinent(lgCtx) && defaultLg.hasNext()) {
				String lg = defaultLg.next();
				lgCtx.setAllLanguage(lg);
			}
			if (!info.isPertinent(lgCtx)) {
				lgCtx = ctx;
			}

			if (acceptStaticInfo(ctx, info)) {

				String multimediaURL = URLHelper.createResourceURL(lgCtx, getPage(), getMultimediaFileURL(ctx, currentLg, file));

				String previewURL = multimediaURL;
				String fileName = ResourceHelper.removeDataFolderDir(globalContext, file.getAbsolutePath());
				if (StringHelper.isImage(file.getName())) {
					if (countAccess) {
						previewURL = URLHelper.createTransformURL(lgCtx, getPage(), getImageFilePath(ctx, fileName), getPreviewFilter(file));
					} else {
						previewURL = URLHelper.createTransformURLWithoutCountAccess(lgCtx, getImageFilePath(ctx, fileName), getPreviewFilter(file));
					}
				} else if (StringHelper.isVideo(file.getName())) {
					String imageName = StringHelper.getFileNameWithoutExtension(fileName) + ".jpg";
					if (countAccess) {
						previewURL = URLHelper.createTransformURL(lgCtx, getPage(), getImageFilePath(ctx, imageName), getPreviewFilter(file));
					} else {
						previewURL = URLHelper.createTransformURLWithoutCountAccess(lgCtx, getImageFilePath(ctx, imageName), getPreviewFilter(file));
					}
				} else {
					previewURL = URLHelper.createTransformURL(lgCtx, getPage(), getImageFilePath(ctx, fileName), getTransformFilter(file));
				}

				resource.setTitle(info.getTitle(lgCtx));
				resource.setRelation(getHTMLRelation(lgCtx));
				resource.setLocation(info.getLocation(lgCtx));
				resource.setDescription(info.getDescription(lgCtx));
				resource.setFullDescription(StringHelper.removeTag(info.getFullDescription(lgCtx)));
				resource.setDate(info.getDate(lgCtx));
				resource.setShortDate(StringHelper.renderDate(resource.getDate(), globalContext.getShortDateFormat()));
				resource.setMediumDate(StringHelper.renderDate(resource.getDate(), globalContext.getMediumDateFormat()));
				resource.setFullDate(StringHelper.renderDate(resource.getDate(), globalContext.getFullDateFormat()));
				resource.setURL(multimediaURL);
				resource.setTags(info.getTags(lgCtx));
				resource.setLanguage(lgCtx.getRequestContentLanguage());
				resource.setIndex(info.getAccessFromSomeDays(lgCtx));

				allURL.put(resource.getURL(), resource);

				resource.setPreviewURL(previewURL);

				if (isRenderInfo(ctx)) {					
					allResource.add(resource);
				}
			}
		}

		int countContentResource = 0;

		if (displayVideo(ctx)) {
			List<MultimediaResource> contentVideos = getContentVideo(ctx);
			for (MultimediaResource resource : contentVideos) {
				if (acceptResource(ctx, resource)) {
					if (allURL.get(resource.getURL()) != null) { // equals and hash is overidded on MultimediaResource -> two "equals" resource can be different (sample : a static resource and a component resource linked with same file).
						allResource.remove(allURL.get(resource.getURL()));
					} 
					allResource.add(resource);
					countContentResource++;
				}
			}
		}
		logger.fine("load content resource : " + countContentResource);

		

		if (isOrderByAccess(ctx)) {
			Collections.sort(allResource, new MultimediaResource.SortByIndex(true));
		} else {
			if (isNameOrder(ctx)) {
				Collections.sort(allResource, new MultimediaResource.SortByName(isReverseOrder(ctx)));
			} else {
				Collections.sort(allResource, new MultimediaResource.SortByDate(isReverseOrder(ctx)));
			}
		}

		int max = Math.min(getMaxListSize(), allResource.size());
		PaginationContext pagination = PaginationContext.getInstance(ctx.getRequest(), getId(), max, getPageSize());
		ctx.getRequest().setAttribute("title", getTitle());
		ctx.getRequest().setAttribute("pagination", pagination);
		ctx.getRequest().setAttribute("resources", allResource.subList(0, max));
	}

	@Override
	public int getWordCount(ContentContext ctx) {
		return 0;
	}

	@Override
	protected void init() throws ResourceNotFoundException {
		initDate = false;
		super.init();
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		String contentCache = getConfig(ctx).getProperty("cache.content", null);
		if (contentCache != null) {
			return StringHelper.isTrue(contentCache);
		}
		return !isOrderByAccess(ctx);
	}

	@Override
	public boolean isContentTimeCachable(ContentContext ctx) {
		return isOrderByAccess(ctx);
	}

	@Override
	public boolean isContentCachableByQuery(ContentContext ctx) {
		return !isRepeat();
	}

	protected boolean isCountAccess(ContentContext ctx) {
		if (isOrderByAccess(ctx)) {
			return false;
		}
		return true;
	}
	
	protected boolean isDisplayOnlyShared() {
		return true;
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return false;
	}

	boolean isFolder() {
		return true;
	}

	public boolean isOrderByAccess(ContentContext ctx) {
		return getValue(ctx).contains(ORDER_BY_ACCESS);
	}
	
	public boolean isReverseOrder(ContentContext ctx) {
		return getValue(ctx).endsWith(REVERSE_ORDER);
	}
	
	public boolean 	isNameOrder(ContentContext ctx) {
		return getValue(ctx).contains(NAME_ORDER);
	}

	protected boolean isRenderInfo(ContentContext ctx) {
		return true;
	}

	protected boolean isRenderLanguage() {
		return true;
	}

	@Override
	public boolean isUnique() {
		return false;
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String folder = requestService.getParameter(getInputBaseFolderName(), null);
		String newStartDate = requestService.getParameter(getInputStartDateName(), null);
		String newEndDate = requestService.getParameter(getInputEndDateName(), null);
		String newListSizeDate = requestService.getParameter(getInputMaxListSizeName(), null);
		String newPageSize = requestService.getParameter(getInputPageSizeName(), null);
		String newDisplayType = requestService.getParameter(getDisplayAsInputName(), null);
		String title = requestService.getParameter(getInputTitle(), null);

		if (newStartDate != null && newEndDate != null && newListSizeDate != null) {

			boolean isOrderByAcess = requestService.getParameter(getInputNameOrderByAccess(), null) != null;
			boolean isReverseOrder = requestService.getParameter(getInputNameReverseOrder(), null) != null;
			boolean isNameOrder = requestService.getParameter(getInputNameNameOrder(), null) != null;

			Date startDate = StringHelper.parseDateOrTime(newStartDate);
			Date endDate = StringHelper.parseDateOrTime(newEndDate);
			if (newDisplayType == null) {
				newDisplayType = "";
			}

			/* tags */
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			List<String> selectedTags = new LinkedList<String>();
			for (String tag : globalContext.getTags()) {
				if (requestService.getParameter(getInputTag(tag), null) != null) {
					selectedTags.add(tag);
				}
			}

			String multimediaInfo = StringHelper.neverNull(StringHelper.renderTime(startDate)) + VALUE_SEPARATOR + StringHelper.neverNull(StringHelper.renderTime(endDate)) + VALUE_SEPARATOR + newPageSize + ',' + newListSizeDate + VALUE_SEPARATOR + folder + VALUE_SEPARATOR + newDisplayType + VALUE_SEPARATOR + StringHelper.collectionToString(selectedTags) + VALUE_SEPARATOR + title;
			if (isNameOrder) {
				multimediaInfo = multimediaInfo + VALUE_SEPARATOR + NAME_ORDER;
			}
			if (isOrderByAcess && !isNameOrder) {
				multimediaInfo = multimediaInfo + VALUE_SEPARATOR + ORDER_BY_ACCESS;
			}
			if (isReverseOrder) {
				multimediaInfo = multimediaInfo + VALUE_SEPARATOR + REVERSE_ORDER;
			}			
			if (!multimediaInfo.equals(getValue())) {
				setValue(multimediaInfo);
				setModify();
			}
		}
	}

	@Override
	public String getImageDescription(ContentContext ctx) {
		try {
			return getFirstResource(ctx).getDescription();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public String getResourceURL(ContentContext ctx) {
		try {
			return getFirstResource(ctx).getPreviewURL();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public String getImageLinkURL(ContentContext ctx) {
		try {
			return getFirstResource(ctx).getURL();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public boolean isImageValid(ContentContext ctx) {
		try {
			return getFirstResource(ctx) != null;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	protected Object getLock(ContentContext ctx) {
		return ctx.getGlobalContext().getLockLoadContent();
	}
	
	@Override
	public int getPriority(ContentContext ctx) {
		if (getConfig(ctx).getProperty("image.priority", null) == null) {
			return 4;
		} else {
			return Integer.parseInt(getConfig(ctx).getProperty("image.priority", null));
		}
	}

}
