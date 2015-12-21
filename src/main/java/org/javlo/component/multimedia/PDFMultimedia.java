package org.javlo.component.multimedia;

import java.io.File;
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

	protected boolean isTag() {
		return false;
	}
	
	@Override
	public String getRenderer(ContentContext ctx) {	
		return super.getRenderer(ctx);
	}
	
	protected Collection<String> getSelection(ContentContext ctx) {
		String baseDir = getBaseStaticDir(ctx);
		File rootDir = new File(baseDir);
		Collection<File> files = ResourceHelper.getAllFiles(rootDir, null);
		Collection<String> folderSelection = new LinkedList<String>();
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
	
	protected List<MultimediaResource> getMultimediaResources(ContentContext ctx) throws Exception {
		List<MultimediaResource> outResource = new LinkedList<MultimediaResource>();
		String folder = getCurrentRootFolder();
		File pdfFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), folder));
		if (pages == -1) {
			pages = PDFHelper.getPDFPageSize(pdfFile);
		}		
		if (pages > 0) {			
			for (int p=1; p<pages; p++) {
				MultimediaResource resource = new MultimediaResource();
				resource.setCssClass("pdf-page");
				resource.setId("p"+p);
				resource.setName("page-"+p);
				resource.setTitle("page "+p);
				ContentContext forcePreviewCtx = ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE); // force no short url
				String previewURL = URLHelper.createTransformURL(forcePreviewCtx, getPage(),URLHelper.mergePath(ctx.getGlobalContext().getStaticConfig().getStaticFolder(), folder), "pdf-preview");
				String fullURL = URLHelper.createTransformURL(forcePreviewCtx, getPage(), URLHelper.mergePath(ctx.getGlobalContext().getStaticConfig().getStaticFolder(), folder), "full");
				previewURL = URLHelper.addParam(previewURL, "page", ""+p);
				resource.setPreviewURL(previewURL);
				fullURL = URLHelper.addParam(fullURL, "page", ""+p);
				resource.setURL(fullURL);
				outResource.add(resource);
			}
		} else {
			logger.warning("file not found : "+pdfFile);
		}
		return outResource;
	}
	
	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		pages = -1;
		return super.performEdit(ctx);
	}

}
