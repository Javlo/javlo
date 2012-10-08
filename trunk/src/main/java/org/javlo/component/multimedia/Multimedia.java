package org.javlo.component.multimedia;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
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
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.IVideo;
import org.javlo.component.image.IImageTitle;
import org.javlo.component.meta.TimeRangeComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.exception.RessourceNotFoundException;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.filefilter.HTMLFileFilter;
import org.javlo.helper.filefilter.ImageFileFilter;
import org.javlo.helper.filefilter.SoundFileFilter;
import org.javlo.helper.filefilter.VideoOrURLFileFilter;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.ztatic.StaticInfo;

public class Multimedia extends TimeRangeComponent implements IImageTitle {

	public static final String TYPE = "multimedia";
	protected static final String STATIC_VIDEO_FOLDER = "videos";
	protected static final String STATIC_SOUND_FOLDER = "sounds";
	protected static final String STATIC_IMAGE_FOLDER = "images";

	protected static final String STATIC_EMBED_FOLDER = "embed";
	protected static final String IMAGE_FIRST = "image first";
	protected static final String IMAGE_AFTER_EXEPT_FIRST = "only first item with image first";

	protected static final String ORDER_BY_ACCESS = "order by access";

	private static final String ALL = "all";
	private static final String IMAGE = "image";
	private static final String SOUND = "sound";
	private static final String VIDEO = "video";
	private static final String EMBED = "embed";

	protected boolean acceptStaticInfo(ContentContext ctx, StaticInfo info, int index) {

		System.out.println("***** Multimedia.acceptStaticInfo : info file : " + info.getFile()); // TODO: remove debug trace

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

		return (info.isShared(ctx) || !isDisplayOnlyShared()) && afterAccept && beforeAccept && index < getMaxListSize();
	}

	protected boolean acceptResource(ContentContext ctx, MultimediaResource info, int index) {

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

		if (info.getDate() == null && (startDate != null || endDate != null)) {
			afterAccept = false;
			beforeAccept = false; // not necessary, just more "clean" :-)
		}

		return afterAccept && beforeAccept && index < getMaxListSize();
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
		return getConfig(ctx).getProperty("image-filter", "thumbnails");
	}

	protected List<MultimediaResource> getContentVideo(ContentContext ctx) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		List<MultimediaResource> outResources = new LinkedList<MultimediaResource>();
		List<IContentVisualComponent> allComps = content.getAllContent(ctx);
		for (IContentVisualComponent comp : allComps) {
			if (comp instanceof IVideo) {
				IVideo video = (IVideo) comp;
				if (video.isShared(ctx)) {
					MultimediaResource resource = new MultimediaResource();
					resource.setURL(video.getURL(ctx));
					resource.setDescription(video.getImageDescription(ctx));
					resource.setPreviewURL(video.getPreviewURL(ctx, getImageFilter(ctx)));
					resource.setDate(video.getDate(ctx));
					resource.renderDate(ctx);
					resource.setLocation(video.getLocation(ctx));
					resource.setCssClass(video.getCssClass(ctx));
					resource.setTitle(video.getTitle(ctx));
					resource.setTags(video.getTags(ctx));
					outResources.add(resource);
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

		if (isOrderByAccess(ctx)) {
			Collections.sort(files, new StaticInfo.StaticFileSortByPopularity(ctx, false));
		} else {
			Collections.sort(files, new StaticInfo.StaticFileSort(ctx, false));
		}

		return files;
	}

	public String getBaseStaticDir(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String folder = URLHelper.mergePath(globalContext.getDataFolder(), getRelativeFileDirectory(ctx));
		folder = folder.replace('\\', '/');
		return folder;
	}

	protected String getCurrentRootFolder() {
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

	private String getDisplayType() {
		String[] values = getValue().split(VALUE_SEPARATOR);
		String out = null;
		if (values.length >= 5) {
			out = values[4];
			if (out.isEmpty()) {
				out = null;
			}
		}
		return out;
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
		if (isFolder()) {
			out.println(XHTMLHelper.getInputOneSelect(getInputBaseFolderName(), folderSelection, getCurrentRootFolder()));
		}
		out.println("<input id=\"contentdate\" style=\"width: 120px;\" type=\"text\" id=\"" + getInputStartDateName() + "\" name=\"" + getInputStartDateName() + "\" value=\"" + StringHelper.renderDateWithDefaultValue(getStartDate(), "") + "\"/> - ");
		out.println("<input style=\"width: 120px;\" type=\"text\" id=\"" + getInputEndDateName() + "\" name=\"" + getInputEndDateName() + "\" value=\"" + StringHelper.renderDateWithDefaultValue(getEndDate(), "") + "\"/>");
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getInputMaxListSizeName() + "\">" + i18nAccess.getText("content.multimedia-gallery.list-size") + "</label>");
		out.println(" : <input style=\"width: 120px;\" type=\"text\" id=\"" + getInputMaxListSizeName() + "\" name=\"" + getInputMaxListSizeName() + "\" value=\"" + getMaxListSize() + "\"/>");
		out.println("</div>");

		Map<String, String> renderers = getConfig(ctx).getRenderes();
		if (renderers.size() > 1) {
			out.println("<fieldset class=\"display\">");
			out.println("<legend>" + i18nAccess.getText("content.page-teaser.display-type") + "</legend><div class=\"line\">");

			out.println("<div class=\"line\">");
			for (Map.Entry<String, String> entry : renderers.entrySet()) {
				out.println(XHTMLHelper.getRadio(getDisplayAsInputName(), entry.getKey(), getDisplayType()));
				out.println("<label for=\"" + entry.getKey() + "\">" + entry.getKey() + "</label></div><div class=\"line\">");
			}
			out.println("</fieldset>");

			out.println("<div class=\"line\">");
			out.print("<input type=\"checkbox\" name=\"" + getInputNameOrderByAccess() + "\" id=\"" + getInputNameOrderByAccess() + "\" ");
			if (isOrderByAccess(ctx)) {
				out.print("checked=\"checked\" ");
			}
			out.print("/>");
			out.println("<label for=\"" + getInputNameOrderByAccess() + "\"> order by access.</label>");
			out.println("</div>");

		}

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
		;
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

	protected String getInputNameOrderByAccess() {
		return "order_by_access_" + getId();
	}

	protected String getItemCssClass() {
		return "page-link";
	}

	public int getMaxListSize() {
		int maxListSize = 16;
		try {
			String maxListSizeStr = getValue().split(VALUE_SEPARATOR)[2];
			maxListSize = Integer.parseInt(maxListSizeStr);
		} catch (Throwable t) {
			logger.warning(t.getMessage());
		}
		return maxListSize;
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
	public String getRenderer(ContentContext ctx) {
		String displayType = getDisplayType();
		Map<String, String> renderers = getConfig(ctx).getRenderes();
		String renderer = null;
		if (renderers.size() == 1) {
			renderer = renderers.values().iterator().next();
		} else if (renderers.size() > 0 && displayType != null) {
			renderer = renderers.get(displayType);
		}
		if (renderer == null) {
			renderer = "multimedia.jsp";
		}
		return renderer;
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

	protected MultimediaResource getFirstRessource(ContentContext ctx) throws Exception {
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

			resource.setURL(fileName);
			resource.setPreviewURL(fileName);

			return resource;
		}
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		int index = 0;

		Collection<File> mulFiles = getAllMultimediaFiles(ctx);
		if (mulFiles.size() == 0) {
			logger.warning("no multimedia resources found.");
			ctx.getRequest().setAttribute("resources", Collections.EMPTY_LIST);
			return;
		}

		List<MultimediaResource> allResource = new ArrayList<MultimediaResource>();
		Map<String, MultimediaResource> allURL = new HashMap<String, MultimediaResource>();

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
				lgCtx.setRequestContentLanguage(defaultLg.next());
			}
			if (!info.isPertinent(lgCtx)) {
				lgCtx = ctx;
			}

			if (acceptStaticInfo(ctx, info, index)) {

				String multimediaURL = URLHelper.createResourceURL(lgCtx, getPage(), getMultimediaFileURL(ctx, currentLg, file));

				String previewURL = multimediaURL;
				String fileName = ResourceHelper.removeDataFolderDir(globalContext, file.getAbsolutePath());
				if (StringHelper.isImage(file.getName())) {
					if (isCountAccess(ctx)) {
						previewURL = URLHelper.createTransformURL(lgCtx, getPage(), getImageFilePath(ctx, fileName), getPreviewFilter(file));
					} else {
						previewURL = URLHelper.createTransformURLWithoutCountAccess(lgCtx, getImageFilePath(ctx, fileName), getPreviewFilter(file));
					}
				} else if (StringHelper.isVideo(file.getName())) {
					String imageName = StringHelper.getFileNameWithoutExtension(fileName) + ".jpg";
					if (isCountAccess(ctx)) {
						previewURL = URLHelper.createTransformURL(lgCtx, getPage(), getImageFilePath(ctx, imageName), getPreviewFilter(file));
					} else {
						previewURL = URLHelper.createTransformURLWithoutCountAccess(lgCtx, getImageFilePath(ctx, imageName), getPreviewFilter(file));
					}
				} else {
					previewURL = URLHelper.createTransformURL(lgCtx, getPage(), getImageFilePath(ctx, fileName), getTransformFilter(file));
				}

				resource.setTitle(info.getTitle(lgCtx));
				resource.setRelation(getHTMLRelation(ctx));
				resource.setLocation(info.getLocation(ctx));
				resource.setDescription(StringHelper.removeTag(info.getFullDescription(lgCtx)));
				resource.setDate(info.getDate(ctx));
				resource.setShortDate(StringHelper.renderDate(resource.getDate(), globalContext.getShortDateFormat()));
				resource.setMediumDate(StringHelper.renderDate(resource.getDate(), globalContext.getMediumDateFormat()));
				resource.setFullDate(StringHelper.renderDate(resource.getDate(), globalContext.getFullDateFormat()));
				resource.setURL(multimediaURL);
				resource.setTags(info.getTags(lgCtx));

				allURL.put(resource.getURL(), resource);

				resource.setPreviewURL(previewURL);

				if (isRenderInfo(ctx)) {
					index++;
					resource.setIndex(index);
					allResource.add(resource);
				}
			}
		}

		List<MultimediaResource> contentVideos = getContentVideo(ctx);
		for (MultimediaResource resource : contentVideos) {
			if (acceptResource(ctx, resource, index)) {
				if (allURL.get(resource.getURL()) != null) { // equals and hash is overidded on MultimediaResource -> two "equals" resource can be different (sample : a static resource and a component resource linked with same file).
					allResource.remove(allURL.get(resource.getURL()));
				} else {
					index++;
				}
				allResource.add(resource);
			}
		}

		ctx.getRequest().setAttribute("resources", allResource);
	}

	@Override
	public int getWordCount(ContentContext ctx) {
		return 0;
	}

	@Override
	protected void init() throws RessourceNotFoundException {
		initDate = false;
		super.init();
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return !isRepeat();
	}

	protected boolean isCountAccess(ContentContext ctx) {
		if (getValue(ctx).endsWith(ORDER_BY_ACCESS)) {
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
		return getValue(ctx).endsWith(ORDER_BY_ACCESS);
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
	public boolean needJavaScript(ContentContext ctx) {
		return false;
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String folder = requestService.getParameter(getInputBaseFolderName(), null);
		String newStartDate = requestService.getParameter(getInputStartDateName(), null);
		String newEndDate = requestService.getParameter(getInputEndDateName(), null);
		String newListSizeDate = requestService.getParameter(getInputMaxListSizeName(), null);
		String newDisplayType = requestService.getParameter(getDisplayAsInputName(), null);

		if (newStartDate != null && newEndDate != null && newListSizeDate != null) {

			boolean isOrderByAcess = requestService.getParameter(getInputNameOrderByAccess(), null) != null;

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

			String multimediaInfo = StringHelper.neverNull(StringHelper.renderTime(startDate)) + VALUE_SEPARATOR + StringHelper.neverNull(StringHelper.renderTime(endDate)) + VALUE_SEPARATOR + newListSizeDate + VALUE_SEPARATOR + folder + VALUE_SEPARATOR + newDisplayType + VALUE_SEPARATOR + StringHelper.collectionToString(selectedTags);
			if (isOrderByAcess) {
				multimediaInfo = multimediaInfo + VALUE_SEPARATOR + ORDER_BY_ACCESS;
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
			return getFirstRessource(ctx).getDescription();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public String getResourceURL(ContentContext ctx) {
		try {
			return getFirstRessource(ctx).getPreviewURL();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public String getImageLinkURL(ContentContext ctx) {
		try {
			return getFirstRessource(ctx).getURL();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public boolean isImageValid(ContentContext ctx) {
		try {
			return getFirstRessource(ctx) != null;
		} catch (Exception e) {
			return false;
		}
	}

}
