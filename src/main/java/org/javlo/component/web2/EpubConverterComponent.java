package org.javlo.component.web2;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.fileupload2.core.FileItem;
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
	
	private static Logger logger = Logger.getLogger(EpubConverterComponent.class.getName());

	private static final int MAX_FILES = 3;

	/** the only extensions the converter can handle : anything else is refused before the write. */
	private static final Collection<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "docx");

	public static String TYPE = "epub-converter-component";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
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
	
	/**
	 * resolve the file an upload will be written to, from the name sent by the
	 * uploader. The name is attacker controlled : it must never be able to place
	 * the file outside <code>epubFolder</code>, nor to create a file the converter
	 * cannot handle.
	 *
	 * @param epubFolder         the folder the upload must stay in
	 * @param submittedFileName  the raw name coming from the multipart request
	 * @return the file to write, always directly inside <code>epubFolder</code>
	 * @throws IOException if the name is refused, before anything is written
	 */
	public static File resolveUploadTarget(File epubFolder, String submittedFileName) throws IOException {
		if (StringHelper.isEmpty(submittedFileName)) {
			throw new IOException("no file name in the upload.");
		}
		if (submittedFileName.indexOf('/') >= 0 || submittedFileName.indexOf('\\') >= 0 || submittedFileName.indexOf(File.separatorChar) >= 0 || submittedFileName.indexOf('\0') >= 0) {
			throw new IOException("file name contains a path separator : " + submittedFileName);
		}
		String fileName = StringHelper.createFileName(submittedFileName);
		if (StringHelper.isEmpty(fileName)) {
			throw new IOException("empty file name after cleaning : " + submittedFileName);
		}
		if (!ALLOWED_EXTENSIONS.contains(StringHelper.getFileExtension(fileName).toLowerCase())) {
			throw new IOException("extension not convertible : " + submittedFileName);
		}
		File sourceFile = new File(epubFolder, fileName);
		if (!sourceFile.getCanonicalPath().startsWith(epubFolder.getCanonicalPath() + File.separator)) {
			throw new IOException("upload escapes the epub folder : " + submittedFileName);
		}
		return sourceFile;
	}

	public static String performConvert(ContentContext ctx, RequestService rs) throws Exception {
		for (FileItem fileItem : rs.getAllFileItem()) {
			File sourceFile;
			try {
				sourceFile = resolveUploadTarget(getEpubFolder(ctx), fileItem.getName());
			} catch (IOException e) {
				logger.warning("refused upload on " + TYPE + " : " + e.getMessage());
				return "bad file name.";
			}
			File targetFile = new File(StringHelper.replaceFileExtension(sourceFile.getAbsolutePath(), "epub"));
			ResourceHelper.writeStreamToFile(fileItem.getInputStream(), sourceFile);
			if (StringHelper.getFileExtension(sourceFile.getName()).equalsIgnoreCase("pdf")) {
				EpubConverter.convertPdfToEPub(sourceFile, targetFile);
			} else if (StringHelper.getFileExtension(sourceFile.getName()).equalsIgnoreCase("docx")) {
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
