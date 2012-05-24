package org.javlo.helper.filefilter;

import java.io.File;
import java.io.FileFilter;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.ztatic.StaticInfo;

public class ImageSharedFileFilter implements FileFilter {

	ContentContext ctx;

	public ImageSharedFileFilter (ContentContext inCtx) {
		ctx = inCtx;
	}


	@Override
	public boolean accept(File file) {
		if (StringHelper.isImage(file.getName())) {
			try {
				StaticInfo staticInfo = StaticInfo.getInstance(ctx, file);
				return staticInfo.isShared(ctx);
			} catch (Exception e) {
			}
		} else {
			return false;
		}
		return false;
	}

}
