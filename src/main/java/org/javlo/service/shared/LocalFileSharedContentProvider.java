package org.javlo.service.shared;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.files.GenericFile;
import org.javlo.context.ContentContext;
import org.javlo.filter.NotDirectoryFilter;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class LocalFileSharedContentProvider extends LocalImageSharedContentProvider {

	public static final String NAME = "local-file";

	public LocalFileSharedContentProvider() {
		setName(NAME);
	}

	protected FileFilter getFilter() {
		return new NotDirectoryFilter();
	}

	protected File getRootFolder(ContentContext ctx) {
		return new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), ctx.getGlobalContext().getStaticConfig().getFileFolder()));
	}

	protected ComponentBean getComponentBean(String name, String category, String specialValue, String lg) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("dir=" + category);
		out.println("file-name=" + name);
		if (specialValue != null) {
			out.println(specialValue);
		}
		out.close();
		String value = new String(outStream.toByteArray());
		ComponentBean imageBean = new ComponentBean(GenericFile.TYPE, value, lg);
		return imageBean;
	}

	protected String getPreviewURL(ContentContext ctx, ComponentBean compBean) throws Exception {
		GenericFile file = new GenericFile();
		file.init(compBean, ctx);
		if (StringHelper.isImage(file.getFile(ctx).getName()) || StringHelper.isPDF(file.getFile(ctx).getName())) {
			return URLHelper.createTransformURL(ctx, file.getFile(ctx), IMAGE_FILTER);
		} else {
			return URLHelper.getFileTypeURL(ctx, file.getFile(ctx));
		}
	}
	
	protected boolean acceptedDocument(ContentContext ctx, String fileName) {
		return ResourceHelper.isAcceptedDocument(ctx, fileName);
	}
	
	@Override
	public File getFolder(ContentContext ctx, String category) {
		return new File(URLHelper.mergePath(getRootFolder(ctx).getAbsolutePath(), category));
	}

}
