package org.javlo.component.multimedia;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.ztatic.StaticInfo;

public class LinkedResources extends MultimediaGallery {

	public static final String TYPE = "linked-resources";

	@Override
	public Collection<File> getAllMultimediaFiles(ContentContext ctx) {
		Collection<File> files = new LinkedList<File>();
		MenuElement currentPage;
		try {
			currentPage = getCurrentPage(ctx, false);
			Collection<StaticInfo> resources = currentPage.getResources(ctx);
			for (StaticInfo staticInfo : resources) {
				if (staticInfo.isShared(ctx)) {
					files.add(staticInfo.getFile());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return files;
	}

	@Override
	public String getHexColor() {
		return GRAPHIC_COLOR;
	}

	protected String getHTMLRelation() {
		return "shadowbox";
	}

	/*
	 * @Override protected String getViewXHTMLCode() throws Exception { StringWriter writer = new StringWriter(); PrintWriter out = new PrintWriter(writer);
	 * 
	 * MenuElement currentPage = getCurrentPage(false);
	 * 
	 * Collection<StaticInfo> resources = currentPage.getResources(ctx); out.println("<ul class=\""+getType()+"\">"); for (StaticInfo staticInfo : resources) { out.println("<li>"+staticInfo.getTitle()+"</li>"); } out.println("</ul>");
	 * 
	 * out.close(); return writer.toString(); }
	 */

	@Override
	protected String getTitle(ContentContext ctx) {
		I18nAccess i18nAccess;
		try {
			i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			return i18nAccess.getViewText("content.linked-resource");
		} catch (Exception e) {
			e.printStackTrace();
			return "i18n error : " + e.getMessage();
		}
	}

	@Override
	protected String getTransformFilter(File file) {
		if (StringHelper.isImage(file.getName())) {
			return "linked-image";
		} else if (StringHelper.isVideo(file.getName())) {
			return "linked-video";
		}
		return "thumbnails";
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public int getWordCount(ContentContext ctx) {
		return 0;
	}

	@Override
	protected boolean isDisplayOnlyShared() {
		return false;
	}

	@Override
	protected boolean isRenderLanguage() {
		return false;
	}

}
