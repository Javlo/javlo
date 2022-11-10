package org.javlo.component.multimedia;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.helper.PDFHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class PDFMultimedia extends Multimedia {

	public static final String TYPE = "pdf-mutlimedia";

	private int pages = -1;

	@Override
	public String getType() {
		return TYPE;
	}

	protected boolean isDateRange() {
		return false;
	}

	protected boolean isOrder() {
		return false;
	}

	protected boolean isManualOrder() {
		return false;
	}

	protected boolean isTag() {
		return false;
	}

	@Override
	public String getRenderer(ContentContext ctx) {
		return super.getRenderer(ctx);
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		File pdfFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), getCurrentRootFolder()));
		ctx.getRequest().setAttribute("pdfUrl", URLHelper.createResourceURL(ctx, pdfFile));
	}

	protected String getCurrentRootFolderForBrowse() {
		File currentFolder = new File(getCurrentRootFolder());
		if (currentFolder == null || currentFolder.getParentFile() == null) {
			return null;
		}
		return currentFolder.getParentFile().getPath();
	}
	
	public void setCurrentRootFolder(ContentContext ctx, String folder) {
		setFieldValue(ROOT_FOLDER, folder);
	}

	protected List<String> getSelection(ContentContext ctx) {
		String baseDir = getBaseStaticDir(ctx);
		File rootDir = new File(baseDir);
		Collection<File> files = ResourceHelper.getAllFiles(rootDir, null);
		List<String> folderSelection = new LinkedList<String>();
		folderSelection.add("/");
		for (File file : files) {
			if (StringHelper.getFileExtension(file.getName()).equalsIgnoreCase("pdf")) {
				folderSelection.add(file.getAbsolutePath().replace('\\', '/').replaceFirst(baseDir, ""));
			}
		}
		return folderSelection;
	}

	protected String getTransformFilter(File file) {
		return "standard";
	}

	@Override
	protected boolean isSelectBrowse() {
		return true;
	}

	protected String getEditPreview(ContentContext ctx) throws Exception {
		File pdfFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), getCurrentRootFolder()));
		if (!pdfFile.exists()) {
			return "";
		} else {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			out.println("<div class=\"preview\">");

			ctx.getRequest().setAttribute("pdfUrl", URLHelper.createResourceURL(ctx, pdfFile));
			out.println("<img src=\"" + URLHelper.createTransformURL(ctx, pdfFile, "preview") + "\" />");
			out.println("</div>");
			out.close();
			return new String(outStream.toByteArray());
		}
	}

	@Override
	public String getResourceURL(ContentContext ctx) {
		try {
			return getFirstResource(ctx).getURL();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	protected MultimediaResource getFirstResource(ContentContext ctx) throws Exception {
		List<MultimediaResource> resources = getMultimediaResources(ctx);
		if (resources.size() == 0) {
			return null;
		} else {
			MultimediaResource resource = null;
			int i = 0;
			while (i < resources.size() && (resource == null)) {
				resource = resources.get(i);
				i++;
			}
			if (resource == null) {
				return null;
			}
			String relPath = URLHelper.mergePath(ctx.getGlobalContext().getStaticConfig().getStaticFolder(), getCurrentRootFolder());
			resource.setURL(relPath);
			return resource;
		}
	}

	protected List<MultimediaResource> getMultimediaResources(ContentContext ctx) throws Exception {
		List<MultimediaResource> outResource = new LinkedList<MultimediaResource>();
		String folder = getCurrentRootFolder();
		File pdfFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), folder));
		if (pages == -1) {
			pages = PDFHelper.getPDFPageSize(pdfFile);
		}
		if (pages > 0) {
			for (int p = 1; p < pages; p++) {
				MultimediaResource resource = new MultimediaResource();
				resource.setCssClass("pdf-page");
				resource.setId("p" + p);
				resource.setName("page-" + p);
				resource.setTitle("page " + p);
				ContentContext forcePreviewCtx = ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE); // force no short url
				forcePreviewCtx.setAbsoluteURL(false);
				String previewURL = URLHelper.createTransformURL(forcePreviewCtx, getPage(), URLHelper.mergePath(ctx.getGlobalContext().getStaticConfig().getStaticFolder(), folder), "pdf-preview");
				String fullURL = URLHelper.createTransformURL(forcePreviewCtx, getPage(), URLHelper.mergePath(ctx.getGlobalContext().getStaticConfig().getStaticFolder(), folder), "full");
				previewURL = URLHelper.addParam(previewURL, "page", "" + p);
				resource.setPreviewURL(previewURL);
				fullURL = URLHelper.addParam(fullURL, "page", "" + p);
				resource.setURL(fullURL);
				outResource.add(resource);
			}
		} else {
			logger.warning("file not found : " + pdfFile);
		}
		return outResource;
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		pages = -1;
		return super.performEdit(ctx);
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}

}
