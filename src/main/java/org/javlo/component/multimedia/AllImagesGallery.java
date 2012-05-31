package org.javlo.component.multimedia;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.filefilter.ImageSharedFileFilter;
import org.javlo.i18n.I18nAccess;
import org.javlo.ztatic.StaticInfo;

public class AllImagesGallery extends MultimediaGallery {

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
		
	}

	@Override
	public Collection<File> getAllMultimediaFiles(ContentContext ctx) {

		List<File> outFiles = new LinkedList<File>();

		if (ORDER_BY_ACCESS.equals(getStyle(ctx))) { 
			outFiles.addAll(ResourceHelper.getAllFiles(new File(getFileImageDirectory(ctx, null)), new ImageSharedFileFilter(ctx)));
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
		return new String[] { standard, view3D, ORDER_BY_ACCESS };
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "standard", "3D", ORDER_BY_ACCESS };
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

	@Override
	public int getWordCount(ContentContext ctx) {
		return 0;
	}

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

}
