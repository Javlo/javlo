package org.javlo.component.multimedia;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.javlo.component.meta.TimeRangeComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.filefilter.HTMLFileFilter;
import org.javlo.helper.filefilter.SoundFileFilter;
import org.javlo.helper.filefilter.VideoOrURLFileFilter;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserSecurity;
import org.javlo.ztatic.StaticInfo;

public class MultimediaGallery extends TimeRangeComponent {

	public static final String TYPE = "multimedia-gallery";

	protected static final String STATIC_VIDEO_FOLDER = "videos";
	protected static final String STATIC_SOUND_FOLDER = "sounds";
	protected static final String STATIC_IMAGE_FOLDER = "images";
	protected static final String STATIC_EMBED_FOLDER = "embed";

	protected static final String IMAGE_FIRST = "image first";
	protected static final String IMAGE_AFTER_EXEPT_FIRST = "only first item with image first";
	protected static final String ORDER_BY_ACCESS = "order by access";

	protected boolean acceptStaticInfo(ContentContext ctx, StaticInfo info, int index) {
		Calendar currentDate = GregorianCalendar.getInstance();
		if (info.getDate(ctx) != null) {
			currentDate.setTime(info.getDate(ctx));
		}
		Calendar startDate = GregorianCalendar.getInstance();
		startDate.setTime(getStartDate());
		Calendar endDate = GregorianCalendar.getInstance();
		endDate.setTime(getEndDate());
		return (info.isShared(ctx) || !isDisplayOnlyShared()) && info.getDate(ctx) != null && currentDate.after(startDate) && currentDate.before(endDate) && index < getMaxListSize();
	}

	public Collection<File> getAllMultimediaFiles(ContentContext ctx) {
		List<File> files = new LinkedList<File>();
		Collection<String> filesName = new HashSet<String>();
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Collection<String> langs = globalContext.getContentLanguages();

		/** Videos **/
		for (String lg : langs) {
			File videoDir = new File(getFileVideoDirectory(ctx, lg));			
			if (videoDir.exists()) {
				File[] filesLg = videoDir.listFiles(new VideoOrURLFileFilter());
				for (File file : filesLg) {
					if (!filesName.contains(file.getName())) {
						filesName.add(file.getName());
						files.add(file);
					}
				}
			}

		}

		/** Sounds **/
		for (String lg : langs) {
			File videoDir = new File(getFileSoundDirectory(ctx, lg));

			if (videoDir.exists()) {
				File[] filesLg = videoDir.listFiles(new SoundFileFilter());
				for (File file : filesLg) {
					if (!filesName.contains(file.getName())) {
						filesName.add(file.getName());
						files.add(file);
					}
				}
			}

		}

		/** Embed **/
		for (String lg : langs) {
			File videoDir = new File(getFileEmbedDirectory(ctx, lg));

			if (videoDir.exists()) {
				File[] filesLg = videoDir.listFiles(new HTMLFileFilter());
				for (File file : filesLg) {
					if (!filesName.contains(file.getName())) {
						filesName.add(file.getName());
						files.add(file);
					}
				}
			}

		}

		if (getStyle(ctx).equals(ORDER_BY_ACCESS)) {
			Collections.sort(files, new StaticInfo.StaticFileSortByPopularity(ctx, false));
		} else {
			Collections.sort(files, new StaticInfo.StaticFileSort(ctx, false));
		}

		return files;
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		Date date = getStartDate();
		if (date == null) {
			date = new Date();
		}

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		out.println(getSpecialInputTag());
		out.println("<input id=\"contentdate\" style=\"width: 120px;\" type=\"text\" id=\"" + getInputStartDateName() + "\" name=\"" + getInputStartDateName() + "\" value=\"" + StringHelper.renderDateWithDefaultValue(getStartDate(), "") + "\"/> - ");
		out.println("<input style=\"width: 120px;\" type=\"text\" id=\"" + getInputEndDateName() + "\" name=\"" + getInputEndDateName() + "\" value=\"" + StringHelper.renderDateWithDefaultValue(getEndDate(), "") + "\"/>&nbsp;&nbsp;");
		out.println(i18nAccess.getText("content.multimedia-gallery.list-size") + " : <input style=\"width: 120px;\" type=\"text\" id=\"" + getInputMaxListSizeName() + "\" name=\"" + getInputMaxListSizeName() + "\" value=\"" + getMaxListSize() + "\"/>");

		out.close();
		return writer.toString();
	}

	public String getFileEmbedDirectory(ContentContext ctx, String lg) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String folder = URLHelper.mergePath(globalContext.getDataFolder(), getRelativeFileDirectory(ctx));
		folder = URLHelper.mergePath(folder, STATIC_EMBED_FOLDER + '/' + lg);
		return folder;
	}

	public String getFileGalleriesDirectory(ContentContext ctx, String lg) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext());
		String folder = URLHelper.mergePath(globalContext.getDataFolder(), staticConfig.getGalleryFolder());
		return folder;
	}

	public String getFileImageDecorationDirectory(ContentContext ctx, String multimediaFolder) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String folder = URLHelper.mergePath(globalContext.getDataFolder(), getRelativeFileDirectory(ctx));
		folder = URLHelper.mergePath(folder, URLHelper.mergePath(multimediaFolder, STATIC_IMAGE_FOLDER));
		return folder;
	}

	public String getFileImageDirectory(ContentContext ctx, String lg) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext());
		String folder = URLHelper.mergePath(globalContext.getDataFolder(), staticConfig.getImageFolder());
		return folder;
	}

	public String getFileSoundDirectory(ContentContext ctx, String lg) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String folder = URLHelper.mergePath(globalContext.getDataFolder(), getRelativeFileDirectory(ctx));
		folder = URLHelper.mergePath(folder, STATIC_SOUND_FOLDER + '/' + lg);
		return folder;
	}

	public String getFileVideoDirectory(ContentContext ctx, String lg) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String folder = URLHelper.mergePath(globalContext.getDataFolder(), getRelativeFileDirectory(ctx));
		folder = URLHelper.mergePath(folder, STATIC_VIDEO_FOLDER + '/' + lg);
		return folder;
	}

	protected String getGlobalCssClass() {
		return "multimedia";
	}

	@Override
	public String getHexColor() {
		return GRAPHIC_COLOR;
	}

	protected String getHTMLRelation(ContentContext ctx) {
		return "shadowbox";
	}

	protected String getImageFilePath(ContentContext ctx, String fileLink) {
		if (StringHelper.isImage(fileLink)) {
			return fileLink;
		}
		String imageFileName = FilenameUtils.getBaseName(fileLink) + ".jpg";
		String folder = STATIC_EMBED_FOLDER;
		if (StringHelper.isVideo(fileLink) || StringHelper.isURLFile(fileLink)) {
			folder = STATIC_VIDEO_FOLDER;
		} else if (StringHelper.isSound(fileLink)) {
			folder = STATIC_SOUND_FOLDER;
		}
		return URLHelper.mergePath(getFileImageDecorationDirectory(ctx, folder), imageFileName);
	}

	protected String getImageFileURL(ContentContext ctx, File file) {
		String folder = STATIC_EMBED_FOLDER;
		if (StringHelper.isVideo(file.getName()) || StringHelper.isURLFile(file.getName())) {
			folder = STATIC_VIDEO_FOLDER;
		} else if (StringHelper.isSound(file.getName())) {
			folder = STATIC_SOUND_FOLDER;
		} else if (StringHelper.isImage(file.getName())) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			return file.getAbsolutePath().replace(globalContext.getDataFolder(), "");
		}
		String imageFileName = FilenameUtils.getBaseName(file.getName()) + ".jpg";
		return URLHelper.mergePath(getRelativeFileDirectory(ctx), URLHelper.mergePath(URLHelper.mergePath(folder, STATIC_IMAGE_FOLDER), imageFileName));
	}

	public String getInputMaxListSizeName() {
		return "__" + getId() + ID_SEPARATOR + "list-size";
	}

	protected String getItemCssClass() {
		return "page-link";
	}

	public int getMaxListSize() {
		int maxListSize = 3;
		try {
			String maxListSizeStr = getValue().split(VALUE_SEPARATOR)[2];
			maxListSize = Integer.parseInt(maxListSizeStr);
		} catch (Throwable t) {
			logger.warning(t.getMessage());
		}
		return maxListSize;
	}

	protected String getMultimediaFilePath(ContentContext ctx, String lg, File file) {
		if (StringHelper.isVideo(file.getName()) || StringHelper.isURLFile(file.getName())) {
			return URLHelper.mergePath(getFileVideoDirectory(ctx, lg), file.getName());
		} else if (StringHelper.isSound(file.getName())) {
			return URLHelper.mergePath(getFileSoundDirectory(ctx, lg), file.getName());
		} else if (StringHelper.isImage(file.getName())) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			String relativeFile = file.getAbsolutePath().replace(globalContext.getDataFolder(), "");
			return relativeFile;
		} else {
			return URLHelper.mergePath(getFileEmbedDirectory(ctx, lg), file.getName());
		}
	}

	protected String getMultimediaFileURL(ContentContext ctx, File file) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String relativeFileName = file.getAbsolutePath().replace(globalContext.getDataFolder(), "");
		return relativeFileName;
	}

	protected String getMultimediaFileURL(ContentContext ctx, String lg, File file) {
		if (StringHelper.isVideo(file.getName()) || StringHelper.isURLFile(file.getName())) {
			return URLHelper.mergePath(getRelativeFileDirectory(ctx), URLHelper.mergePath(STATIC_VIDEO_FOLDER + '/' + lg, file.getName()));
		} else if (StringHelper.isSound(file.getName())) {
			return URLHelper.mergePath(getRelativeFileDirectory(ctx), URLHelper.mergePath(STATIC_SOUND_FOLDER + '/' + lg, file.getName()));
		} else if (StringHelper.isImage(file.getName())) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			String relativeFileName = file.getAbsolutePath().replace(globalContext.getDataFolder(), "");
			return relativeFileName;
		} else {
			return URLHelper.mergePath(getRelativeFileDirectory(ctx), URLHelper.mergePath(STATIC_EMBED_FOLDER + '/' + lg, file.getName()));
		}
	}

	protected String getPreviewFilter(File file) {
		return "thumb-view";
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
		return new String[] { IMAGE_FIRST, IMAGE_AFTER_EXEPT_FIRST, ORDER_BY_ACCESS };
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

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		// out.println(super.getViewXHTMLCode());

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Collection<String> langs = globalContext.getContentLanguages();

		int index = 0;

		Collection<File> mulFiles = getAllMultimediaFiles(ctx);
		
		if (mulFiles.size() == 0) {
			logger.warning("no multimedia files found in : "+getPage().getPath()+" ["+getType()+"]");
			return "";
		}

		out.println("<div class=\"" + getGlobalCssClass() + "\">");
		if (getTitle(ctx) != null) {
			out.println("<h2>" + getTitle(ctx) + "</h2>");
		}

		for (File file : mulFiles) {

			String cssClass = "embed";
			if (StringHelper.isVideo(file.getName())) {
				cssClass = "video";
			} else if (StringHelper.isSound(file.getName())) {
				cssClass = "sound";
			} else if (StringHelper.isImage(file.getName())) {
				cssClass = "image";
			}

			String currentLg = ctx.getRequestContentLanguage();
			File multimediaFile = new File(getMultimediaFilePath(ctx, currentLg, file));
			if (!(multimediaFile.exists())) {
				currentLg = globalContext.getDefaultLanguages().iterator().next(); // TODO:
				// manage
				// multimedia
				// gallery
				// for
				// all
				// languages
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

				out.println("<div class=\"" + getItemCssClass() + " index-" + index + " " + cssClass + "\">");

				File imageFile = new File(getImageFilePath(ctx, file.getAbsolutePath()));

				String multimediaURL = URLHelper.createRessourceURL(lgCtx, getPage(), getMultimediaFileURL(ctx, currentLg, file));

				String previewURL = multimediaURL;
				if (StringHelper.isImage(file.getName())) {
					if (isCountAccess(ctx)) {
						previewURL = URLHelper.createTransformURL(lgCtx, getPage(), getImageFileURL(ctx, file), getPreviewFilter(file));
					} else {
						previewURL = URLHelper.createTransformURLWithoutCountAccess(lgCtx, getImageFileURL(ctx, file), getPreviewFilter(file));
					}
				}

				String title = info.getTitle(lgCtx);
				/*
				 * if (title.trim().length() == 0) { title = file.getName(); }
				 */

				if (index < 1 || !getStyle(ctx).equals(IMAGE_AFTER_EXEPT_FIRST)) {
					if (imageFile.exists()) {
						String debugInfo = "";
						if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
							EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
							AdminUserSecurity security = AdminUserSecurity.getInstance();
							if (security.haveRight(editCtx.getEditUser(), AdminUserSecurity.CONTENT_ROLE) && ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
								debugInfo = " access:" + info.getAccessFromSomeDays(ctx);
							}
						}
						out.println("<a href=\"" + previewURL + "\" rel=\"" + getHTMLRelation(ctx) + "\" title=\"" + StringHelper.removeTag(info.getFullDescription(lgCtx)) + debugInfo + "\">");
						out.println("<img src=\"" + URLHelper.createTransformURL(lgCtx, getPage(), getImageFileURL(ctx, file), getTransformFilter(file)) + "\" alt=\"" + StringHelper.removeTag(info.getTitle(ctx)) + debugInfo + "\" />");
						out.println("</a>");
						out.println(XHTMLHelper.renderSpecialLink(ctx, currentLg, getMultimediaFileURL(ctx, currentLg, file), info));
						/*
						 * out.println("<a class=\"hd\" href=\"" + multimediaURL + "\" title=\"" + info.getFullDescription(lgCtx) + "\">"); out.println("HD"); out.println("</a>");
						 */
					} else {
						logger.info("image not found : " + imageFile);
					}
				}

				if (isRenderInfo(ctx)) {
					out.println("<h4><a href=\"" + previewURL + "\" title=\"" + StringHelper.removeTag(info.getFullDescription(lgCtx)) + "\">" + title + "</a></h4>");
					if (info.getLocation(ctx) != null && info.getLocation(ctx).trim().length() > 0) {
						out.println("<span class=\"place\">" + info.getLocation(ctx) + " - </span>");
					}
					out.println("<span class=\"date\">" + StringHelper.renderShortDate(ctx, info.getDate(ctx)) + "</span>");
					if (info.getDescription(ctx) != null && info.getDescription(ctx).trim().length() > 0) {
						out.println("<p>" + info.getDescription(ctx) + "</p>");
					}
				}

				if (!(index < 1 || !getStyle(ctx).equals(IMAGE_AFTER_EXEPT_FIRST))) {
					if (imageFile.exists()) {
						out.println("<a href=\"" + previewURL + "\" rel=\"" + getHTMLRelation(ctx) + "\" title=\"" + StringHelper.removeTag(info.getFullDescription(ctx)) + "\"><img src=\"" + URLHelper.createTransformURL(lgCtx, getImageFileURL(ctx, file), getTransformFilter(file)) + "\" alt=\"" + StringHelper.removeTag(info.getTitle(ctx)) + "\" /></a>");
					} else {
						logger.info("image not found : " + imageFile);
					}
				}

				if (isRenderLanguage()) {
					I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
					out.println("<a class=\"read-more\" href=\"" + multimediaURL + "\" title=\"" + StringHelper.removeTag(info.getFullDescription(ctx)) + "\">" + i18nAccess.getViewText("global.read-more") + "</a>");
					out.println("<div class=\"content-languages\"><ul>");
					for (String lg : langs) {
						if ((new File(getMultimediaFilePath(ctx, lg, file))).exists()) {
							multimediaURL = URLHelper.createRessourceURL(ctx, getMultimediaFileURL(ctx, lg, file));
							lgCtx = new ContentContext(ctx);
							lgCtx.setRequestContentLanguage(lg);
							info = StaticInfo.getInstance(lgCtx, getMultimediaFileURL(ctx, lg, file));
							if (info.isPertinent(lgCtx)) {
								out.println("<li class=\"" + lg + "\">");
								out.println("<a href=\"" + multimediaURL + "\" rel=\"" + getHTMLRelation(ctx) + "\" title=\"" + StringHelper.removeTag(info.getFullDescription(lgCtx)) + "\"><span>" + lg + "</span></a>");
								out.println("</li>");
							}
						} else {
							lgCtx.setRequestContentLanguage(lg);
							info = StaticInfo.getInstance(lgCtx, file);
							if (info.isPertinent(lgCtx)) {
								multimediaURL = URLHelper.createRessourceURL(ctx, getMultimediaFileURL(ctx, file));
								lgCtx = new ContentContext(ctx);
								lgCtx.setRequestContentLanguage(lg);
								info = StaticInfo.getInstance(lgCtx, getMultimediaFileURL(ctx, file));
								if (info.isPertinent(lgCtx)) {
									out.println("<li class=\"" + lg + "\">");
									out.println("<a href=\"" + multimediaURL + "\" rel=\"" + getHTMLRelation(ctx) + "\" title=\"" + StringHelper.removeTag(info.getFullDescription(lgCtx)) + "\"><span>" + lg + "</span></a>");
									out.println("</li>");
								}
							}
						}
					}
					
				}
				out.println("<div class=\"end-sequence\"><span></span></div>");
				out.println("</div>");

				index++;
			}
		}

		out.println("<div class=\"content_clear\"><span>&nbsp;</span></div></ul></div>");
		out.println("</div>");
		out.close();

		return writer.toString();
	}

	@Override
	public int getWordCount(ContentContext ctx) {
		return 0;
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return !isRepeat();
	}

	protected boolean isCountAccess(ContentContext ctx) {
		if (getStyle(ctx).equals(ORDER_BY_ACCESS)) {
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

	protected boolean isRenderInfo(ContentContext ctx) {
		return true;
	}

	protected boolean isRenderLanguage() {
		return true;
	}

	@Override
	public boolean needJavaScript(ContentContext ctx) {
		return false;
	}

	@Override
	public void refresh(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String newStartDate = requestService.getParameter(getInputStartDateName(), null);
		String newEndDate = requestService.getParameter(getInputEndDateName(), null);
		String newListSizeDate = requestService.getParameter(getInputMaxListSizeName(), null);
		if (newStartDate != null && newEndDate != null && newListSizeDate != null) {

			Date startDate = new Date();
			try {
				startDate = StringHelper.parseDateOrTime(newStartDate);
			} catch (ParseException p) {
				p.printStackTrace();
				setNeedRefresh(true);
			}

			Date endDate = new Date();
			try {
				endDate = StringHelper.parseDateOrTime(newEndDate);
			} catch (ParseException p) {
				p.printStackTrace();
				setNeedRefresh(true);
			}

			String dateStr = StringHelper.renderTime(startDate) + VALUE_SEPARATOR + StringHelper.renderTime(endDate) + VALUE_SEPARATOR + newListSizeDate;
			if (!dateStr.equals(getValue())) {
				setValue(dateStr);
				setModify();
			}
		}
	}

	@Override
	public boolean isUnique() {
		return false;
	}

}
