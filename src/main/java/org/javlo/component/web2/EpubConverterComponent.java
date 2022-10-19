package org.javlo.component.web2;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.io.EpubConverter;
import org.javlo.service.RequestService;
import org.javlo.ztatic.DoubleFile;

public class EpubConverterComponent extends AbstractVisualComponent implements IAction {
	
	private static final int MAX_FILES = 3;
	
	public static String TYPE = "epub-converter-component";

	@Override
	public String getType() {
		return TYPE;
	}
	
	public static File getEpubFolder(ContentContext ctx) {
		File dir = new File(URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(),TYPE));
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
	
	@Override
	public String getCurrentRenderer(ContentContext ctx) {
		if (getComponentBean().getRenderer() == null && getRenderes(ctx).size() > 0) {
			String defaultRenderer = getConfig(ctx).getDefaultRenderer();
			if (defaultRenderer != null) {
				return defaultRenderer;
			} else {
				return getRenderes(ctx).keySet().iterator().next();
			}
		} else {
			return getComponentBean().getRenderer();
		}
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		ctx.getRequest().setAttribute("actionUrl", URLHelper.createActionURL(ctx, TYPE+".convert", "file.epub"));
		List<DoubleFile> latestConvertion = new LinkedList<>();
		for (File file : getEpubFolder(ctx).listFiles()) {
			if (StringHelper.getFileExtension(file.getName()).equalsIgnoreCase("epub")) {
				File sourceFile = new File(StringHelper.replaceFileExtension(file.getAbsolutePath(), "pdf"));
				if (sourceFile.exists()) {
					latestConvertion.add(new DoubleFile(ctx, sourceFile, file));
				}
				sourceFile = new File(StringHelper.replaceFileExtension(file.getAbsolutePath(), "docx"));
				if (sourceFile.exists()) {
					latestConvertion.add(new DoubleFile(ctx, sourceFile, file));
				}
			}
		}
		Collections.sort(latestConvertion, new Comparator<DoubleFile>() {
			@Override
			public int compare(DoubleFile o1, DoubleFile o2) {
				return (int)(o2.lastModified()-o1.lastModified());
			}
		});
		if (latestConvertion.size()>MAX_FILES) {
			latestConvertion.get(latestConvertion.size()-1).delete();
			latestConvertion.remove(latestConvertion.size()-1);
		}
		ctx.getRequest().setAttribute("latestConvertion", latestConvertion);
	}
	
	public static String performConvert(ContentContext ctx, RequestService rs) throws Exception {
		for (FileItem fileItem : rs.getAllFileItem()) {
			File sourceFile = new File(getEpubFolder(ctx).getAbsolutePath(), fileItem.getName());
			File targetFile = new File(StringHelper.replaceFileExtension(sourceFile.getAbsolutePath(), "epub"));
			ResourceHelper.writeStreamToFile(fileItem.getInputStream(), sourceFile);
			if (StringHelper.getFileExtension(fileItem.getName()).equalsIgnoreCase("pdf")) {
				EpubConverter.convertPdfToEPub(sourceFile, targetFile);
			} else if (StringHelper.getFileExtension(fileItem.getName()).equalsIgnoreCase("docx")) {
				EpubConverter.convertDocxToEPub(sourceFile, targetFile);
			}
		}
		return null;
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "no renderer";
	}

	@Override
	public String getActionGroupName() {
		return getType();
	}
	
	public static void main(String[] args) {
		System.out.println(StringHelper.replaceFileExtension("test.png", ".epub"));
	}

}
