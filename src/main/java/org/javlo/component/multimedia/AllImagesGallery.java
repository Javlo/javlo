package org.javlo.component.multimedia;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.filefilter.ImageSharedFileFilter;
import org.javlo.i18n.I18nAccess;
import org.javlo.ztatic.StaticInfo;

public class AllImagesGallery extends MultimediaGallery {

	private static final String STYLE_3D_BIS = "3D Bis";
	public static final String TYPE = "all-images-gallery";

	protected void accessSorting(ContentContext ctx, List<File> files, int pertinentPageToBeSort) throws Exception {
		double minMaxPageRank = 0;
		TreeSet<File> maxElement = new TreeSet<File>(new StaticInfo.StaticFileSortByAccess(ctx, true));
		for (File file : files) {
			StaticInfo staticInfo = StaticInfo.getInstance(ctx, file);
			if (staticInfo.isShared(ctx)) {
				double pageRank = staticInfo.getAccessFromSomeDays(ctx);
				if (pageRank >= minMaxPageRank) {
					if (maxElement.size() > pertinentPageToBeSort) {
						maxElement.pollFirst();
						minMaxPageRank = StaticInfo.getInstance(ctx, maxElement.first()).getAccessFromSomeDays(ctx);
						/*
						 * System.out.println(""); System.out.println("**** list of element in maxElement list : "); for (File fileDebug : maxElement) { StaticInfo staticInfoDebug = StaticInfo.getInstance(ctx, fileDebug); System.out.println("-> "+fileDebug+" - "+staticInfoDebug.getAccessFromSomeDays(ctx)); } System.out.println("");
						 */
					}
					maxElement.add(file);
				}
			}
		}
		for (File file : maxElement) {
			files.remove(file);
		}
		for (File file : maxElement) {
			files.add(0, file);
		}

		/*logger.fine("access sorting final list :");
		for (int i = 0; i < pertinentPageToBeSort; i++) {
			if (files.size() >= i)  {
				StaticInfo staticInfo = StaticInfo.getInstance(ctx, files.get(i));
				logger.fine("   i : " + i + "  -  " + files.get(i) + "  -  " + staticInfo.getAccessFromSomeDays(ctx));
			}			
		}*/
	}

	@Override
	public Collection<File> getAllMultimediaFiles(ContentContext ctx) {

		List<File> outFiles = new LinkedList<File>();

		if (ORDER_BY_ACCESS.equals(getStyle(ctx))) {
			// outFiles.addAll(ResourceHelper.getAllFiles(new File(getFileImageDirectory(ctx, null)), new ImageFileFilter(), new StaticInfo.StaticFileSortByPopularity(ctx, false)));
			outFiles.addAll(ResourceHelper.getAllFiles(new File(getFileImageDirectory(ctx, null)), new ImageSharedFileFilter(ctx)));
			// outFiles.addAll(ResourceHelper.getAllFiles(new File(getFileGalleriesDirectory(ctx, null)), new ImageFileFilter(), new StaticInfo.StaticFileSortByPopularity(ctx, false)));
			outFiles.addAll(ResourceHelper.getAllFiles(new File(getFileGalleriesDirectory(ctx, null)), new ImageSharedFileFilter(ctx)));
		} else {
			outFiles.addAll(ResourceHelper.getAllFiles(new File(getFileImageDirectory(ctx, null)), new ImageSharedFileFilter(ctx), new StaticInfo.StaticFileSort(ctx, false)));
			outFiles.addAll(ResourceHelper.getAllFiles(new File(getFileGalleriesDirectory(ctx, null)), new ImageSharedFileFilter(ctx), new StaticInfo.StaticFileSort(ctx, false)));
		}
		
		List<File> filteredOutFiles = new LinkedList<File>();

		for (File file : outFiles) {
			try {
				if (acceptStaticInfo(ctx, StaticInfo.getInstance(ctx, file), 0)) {
					filteredOutFiles.add(file);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		outFiles = filteredOutFiles;

		if (getStyle(ctx).equals(ORDER_BY_ACCESS) && getMaxListSize() < 99) {
			try {
				accessSorting(ctx, outFiles, getMaxListSize());
				return outFiles;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (getStyle(ctx).equals(ORDER_BY_ACCESS)) {
			Collections.sort(outFiles, (new StaticInfo.StaticFileSortByAccess(ctx, false)));
		} else {
			Collections.sort(outFiles, (new StaticInfo.StaticFileSort(ctx, false)));
		}

		return outFiles;
	}

	public String getCSSClassName(ContentContext ctx) {
		return getStyle(ctx);
	}

	@Override
	public Collection<String> getExternalResources(ContentContext ctx) {
		if (getStyle(ctx).equals(STYLE_3D_BIS)) {
			List<String> resources = new LinkedList<String>();
			resources.add("/js/jquery/jquery_1_4_4.js");
			resources.add("/js/ContentFlow/contentflow.js");
			return resources;
		} else if (!getStyle(ctx).equals("3D")) {
			return super.getExternalResources(ctx);
		} else {
			List<String> resources = new LinkedList<String>();
			resources.add("/js/mootools.js");
			resources.add("/js/global.js");
			resources.add("/js/calendar/js/HtmlManager.js");
			resources.add("/js/calendar/js/calendarFunctions.js");
			resources.add("/js/calendar/js/calendarOptions.js");
			resources.add("/js/calendar/js/calendarTranslate_" + ctx.getLanguage() + ".js");
			resources.add("/js/calendar/css/style_calendar.css");
			resources.add("/js/calendar/css/style_calendarcolor.css");
			resources.add("/js/shadowbox/src/adapter/shadowbox-base.js");
			resources.add("/js/shadowbox/src/shadowbox.js");
			resources.add("/js/shadowboxOptions.js");
			resources.add("/js/onLoadFunctions.js");
			return resources;
		}
	}

	@Override
	protected String getGlobalCssClass() {
		return "thumbnails";
	}

	@Override
	protected String getHTMLRelation(ContentContext ctx) {
		if (!getStyle(ctx).equals("3D")) {
			return "shadowbox[thumb-" + getId() + "]";
		} else {
			return "shadowbox";
		}
	}

	@Override
	protected String getItemCssClass() {
		return "thumb-image";
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		String standard = "standard";
		String view3D = "3D";
		try {
			I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
			standard = i18n.getText("content.thumbnails.standard");
			view3D = i18n.getText("content.thumbnails.3D");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String[] { standard, view3D, STYLE_3D_BIS, ORDER_BY_ACCESS };
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "standard", "3D", STYLE_3D_BIS, ORDER_BY_ACCESS };
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		return "type";
	}

	@Override
	protected String getTransformFilter(File file) {
		return "thumbnails";
	}

	@Override
	public String getType() {
		return TYPE;
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (getStyle(ctx).equals("3D") && ctx.getDevice().isPointerDevice()) { // if no pointer device -> no mouse over -> not possible to put 3D thumbnails
			return render3DView(ctx);
		} else if (getStyle(ctx).equals(STYLE_3D_BIS)) {
			return render3DBisView(ctx);
		} else {
			return super.getViewXHTMLCode(ctx);
		}
	}

	@Override
	public int getWordCount(ContentContext ctx) {
		return 0;
	}

	/*
	 * @Override public Collection<File> getAllMultimediaFiles(ContentContext ctx) { Collection<File> outFiles;
	 * 
	 * 
	 * Logger.stepCount("mgal", "getAllMultimediaFiles - start");
	 * 
	 * 
	 * if (getStyle(ctx).equals(ORDER_BY_ACCESS)) { outFiles = new TreeSet<File>(new StaticInfo.StaticFileSortByAccess(ctx, false)); } else { outFiles = new TreeSet<File>(new StaticInfo.StaticFileSort(ctx, false)); }
	 * 
	 * if (ORDER_BY_ACCESS.equals(getStyle(ctx))) { outFiles.addAll(ResourceHelper.getAllFiles(new File(getFileImageDirectory(ctx, null)), new ImageFileFilter(), new StaticInfo.StaticFileSortByPopularity(ctx, false))); outFiles.addAll(ResourceHelper.getAllFiles(new File(getFileGalleriesDirectory(ctx, null)), new ImageFileFilter(), new StaticInfo.StaticFileSortByPopularity(ctx, false))); } else { outFiles.addAll(ResourceHelper.getAllFiles(new File(getFileImageDirectory(ctx, null)), new ImageFileFilter(), new StaticInfo.StaticFileSort(ctx, false))); outFiles.addAll(ResourceHelper.getAllFiles(new File(getFileGalleriesDirectory(ctx, null)), new ImageFileFilter(), new StaticInfo.StaticFileSort(ctx, false))); }
	 * 
	 * Logger.stepCount("mgal", "getAllMultimediaFiles - sort");
	 * 
	 * return outFiles; }
	 */

	@Override
	protected boolean isRenderInfo(ContentContext ctx) {
		if (getStyle(ctx).equals("3D")) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected boolean isRenderLanguage() {
		return false;
	}

	@Override
	public boolean isUnique() {
		return false;
	}

	protected String render3DBisView(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		Calendar startDate = GregorianCalendar.getInstance();
		startDate.setTime(getStartDate());
		Calendar endDate = GregorianCalendar.getInstance();
		endDate.setTime(getEndDate());

		int index = 0;

		out.println("<div class=\"ContentFlow\">");
		out.println("<div class=\"loadIndicator\"><div class=\"indicator\"></div></div>");
		out.println("<div class=\"flow\">");
		Collection<File> mulFiles = getAllMultimediaFiles(ctx);
		for (File file : mulFiles) {
			org.javlo.helper.Logger.stepCount("multimedia", " file : " + file);
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

			Calendar currentDate = GregorianCalendar.getInstance();

			if (info.getDate(ctx) != null) {
				currentDate.setTime(info.getDate(ctx));
			}

			if ((info.isShared(ctx) || !isDisplayOnlyShared()) && info.getDate(ctx) != null && currentDate.after(startDate) && currentDate.before(endDate) && index < getMaxListSize()) {

				File imageFile = new File(getImageFilePath(ctx, file.getAbsolutePath()));

				String multimediaURL = URLHelper.createRessourceURL(lgCtx, getMultimediaFileURL(ctx, currentLg, file));

				if (StringHelper.isImage(file.getName())) {
					URLHelper.createTransformURL(lgCtx, getImageFileURL(ctx, file), getPreviewFilter(file));
				}

				info.getTitle(lgCtx);

				String filter = "thumbnails-3D";

				if (index < 1 || !getStyle(ctx).equals(IMAGE_AFTER_EXEPT_FIRST)) {
					if (imageFile.exists()) {
						// out.println("<div><a href=\"" + previewURL + "\" rel=\"" + getHTMLRelation(ctx) + "\" title=\"" + StringHelper.removeTag(info.getFullDescription(ctx)) + "\">");
						String event = "";
						out.println("<img class=\"item\" src=\"" + URLHelper.createTransformURL(lgCtx, getImageFileURL(ctx, file), filter) + "\" alt=\"" + StringHelper.removeTag(info.getTitle(ctx)) + "\"" + event + " />");
						// out.println("</a>");
						out.println(XHTMLHelper.renderSpecialLink(ctx, currentLg, getMultimediaFileURL(ctx, currentLg, file), info));
						// out.println("</div>");
					} else {
						logger.info("image not found : " + imageFile);
					}
				}

				/*
				 * if (isRenderInfo(ctx)) { out.println("<div id=\"3D-description-" + index + "\" class=\"description\">"); out.println("<h4><a href=\"" + previewURL + "\" rel=\"" + getHTMLRelation(ctx) + "\" title=\"" + StringHelper.removeTag(info.getFullDescription(ctx)) + "\">" + title + "</a></h4>"); if (info.getLocation(ctx) != null && info.getLocation(ctx).trim().length() > 0) { out.println("<span class=\"place\">" + info.getLocation(ctx) + " - </span>"); } out.println("<span class=\"date\">" + StringHelper.renderShortDate(ctx, info.getDate(ctx)) + "</span>"); if (info.getDescription(ctx) != null && info.getDescription(ctx).trim().length() > 0) { out.println("<p>" + info.getDescription(ctx) + "</p>"); } out.println("</div>"); }
				 */
				index++;
			}
		}
		out.println("</div>");

		out.println("<div class=\"globalCaption\"></div>");
		out.println("<div class=\"scrollbar\"><div class=\"slider\"><div class=\"position\"></div></div></div>");

		out.println("</div>");
		out.close();
		return writer.toString();
	}

	protected String render3DView(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		Calendar startDate = GregorianCalendar.getInstance();
		startDate.setTime(getStartDate());
		Calendar endDate = GregorianCalendar.getInstance();
		endDate.setTime(getEndDate());

		int index = 0;

		out.println("<div id=\"3D-container\">");
		out.println("<div class=\"thumbnails-3D\">");
		out.println("<div class=\"images\">");
		Collection<File> mulFiles = getAllMultimediaFiles(ctx);
		for (File file : mulFiles) {
			org.javlo.helper.Logger.stepCount("multimedia", " file : " + file);
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

			Calendar currentDate = GregorianCalendar.getInstance();

			if (info.getDate(ctx) != null) {
				currentDate.setTime(info.getDate(ctx));
			}

			if ((info.isShared(ctx) || !isDisplayOnlyShared()) && info.getDate(ctx) != null && currentDate.after(startDate) && currentDate.before(endDate) && index < getMaxListSize()) {

				File imageFile = new File(getImageFilePath(ctx, file.getAbsolutePath()));

				String multimediaURL = URLHelper.createRessourceURL(lgCtx, getMultimediaFileURL(ctx, currentLg, file));

				String previewURL = multimediaURL;
				if (StringHelper.isImage(file.getName())) {
					previewURL = URLHelper.createTransformURL(lgCtx, getImageFileURL(ctx, file), getPreviewFilter(file));
				}

				String title = info.getTitle(lgCtx);
				/*
				 * if (title.trim().length() == 0) { title = file.getName(); }
				 */

				String filter = "thumbnails-3D";

				if (index < 1 || !getStyle(ctx).equals(IMAGE_AFTER_EXEPT_FIRST)) {
					if (imageFile.exists()) {
						out.println("<div class=\"board-thumb-image\"><a href=\"" + previewURL + "\" rel=\"" + getHTMLRelation(ctx) + "\" title=\"" + StringHelper.removeTag(info.getFullDescription(ctx)) + "\">");
						String event = "";
						out.println("<img src=\"" + URLHelper.createTransformURL(lgCtx, getImageFileURL(ctx, file), filter) + "\" alt=\"" + StringHelper.removeTag(info.getTitle(ctx)) + "\"" + event + " />");
						out.println("</a>");
						out.println(XHTMLHelper.renderSpecialLink(ctx, currentLg, getMultimediaFileURL(ctx, currentLg, file), info));
						out.println("</div>");
					} else {
						logger.info("image not found : " + imageFile);
					}
				}

				if (isRenderInfo(ctx)) {
					out.println("<div id=\"3D-description-" + index + "\" class=\"description\">");
					out.println("<h4><a href=\"" + previewURL + "\" rel=\"" + getHTMLRelation(ctx) + "\" title=\"" + StringHelper.removeTag(info.getFullDescription(ctx)) + "\">" + title + "</a></h4>");
					if (info.getLocation(ctx) != null && info.getLocation(ctx).trim().length() > 0) {
						out.println("<span class=\"place\">" + info.getLocation(ctx) + " - </span>");
					}
					out.println("<span class=\"date\">" + StringHelper.renderShortDate(ctx, info.getDate(ctx)) + "</span>");
					if (info.getDescription(ctx) != null && info.getDescription(ctx).trim().length() > 0) {
						out.println("<p>" + info.getDescription(ctx) + "</p>");
					}
					out.println("</div>");
				}
				index++;
			}
		}
		out.println("<div class=\"content_clear\"><span></span></div>");
		out.println("</div></div></div>");
		out.close();
		return writer.toString();
	}

}
