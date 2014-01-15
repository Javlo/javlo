package org.javlo.component.multimedia;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.PaginationContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.filefilter.ImageFileFilter;
import org.javlo.service.RequestService;
import org.javlo.ztatic.StaticInfo;

public class Gallery extends AbstractVisualComponent {

	public static final String TYPE = "gallery";

	protected String getRootStaticDir(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String folder = URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getStaticFolder(), getGalleryFolder(ctx));
		folder = folder.replace('\\', '/');
		return folder;
	}

	private String getGalleryFolder(ContentContext ctx) {
		return getConfig(ctx).getProperty("folder", "gallery");
	}

	protected Collection<File> getAllMultimediaFiles(ContentContext ctx) {
		List<File> files = new LinkedList<File>();
		Collection<String> filesName = new HashSet<String>();
		File resourceFolder = new File(getRootStaticDir(ctx));		
		if (resourceFolder.exists()) {
			Collection<File> filesLg = ResourceHelper.getAllFiles(resourceFolder, new ImageFileFilter());
			for (File file : filesLg) {
				if (!filesName.contains(file.getName())) {
					filesName.add(file.getName());
					files.add(file);
				}
			}
		} else {
			logger.warning("ressource folder not found : "+resourceFolder);
		}
		return files;
	}

	protected String getMultimediaFileURL(ContentContext ctx, String lg, File file) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String relativeFileName = file.getAbsolutePath().replace(globalContext.getDataFolder(), "");
		return relativeFileName;
	}
	
	protected String getImageFilePath(ContentContext ctx, String fileLink) {
		if (StringHelper.isImage(fileLink)) {
			return fileLink;
		} else {
			return FilenameUtils.getBaseName(fileLink) + ".jpg";
		}
	}

	protected List<MultimediaResource> getResources(ContentContext ctx) throws Exception {
		GlobalContext globalContext = ctx.getGlobalContext();
		MultimediaResourceFilter filter = getFilter(ctx);
		List<MultimediaResource> resources = new LinkedList<MultimediaResource>();
		
		boolean countAccess = isCountAccess(ctx);
		String previewFilter = getPreviewFilter(ctx);
		
		for (File file : getAllMultimediaFiles(ctx)) {
			MultimediaResource resource = new MultimediaResource();
			StaticInfo info = StaticInfo.getInstance(ctx, file);
			resource.setTitle(info.getTitle(ctx));
			resource.setLocation(info.getLocation(ctx));
			resource.setDescription(info.getDescription(ctx));
			resource.setFullDescription(StringHelper.removeTag(info.getFullDescription(ctx)));
			resource.setDate(info.getDate(ctx));
			resource.setShortDate(StringHelper.renderDate(resource.getDate(), globalContext.getShortDateFormat()));
			resource.setMediumDate(StringHelper.renderDate(resource.getDate(), globalContext.getMediumDateFormat()));
			resource.setFullDate(StringHelper.renderDate(resource.getDate(), globalContext.getFullDateFormat()));
			resource.setURL(URLHelper.createResourceURL(ctx, getPage(), getMultimediaFileURL(ctx, ctx.getLanguage(), file)));
			resource.setTags(info.getTags(ctx));
			resource.setLanguage(ctx.getRequestContentLanguage());
			resource.setIndex(info.getAccessFromSomeDays(ctx));
			resource.setRelation(getRelation(ctx));
			
			String previewURL = resource.getURL();
			String fileName = ResourceHelper.removeDataFolderDir(globalContext, file.getAbsolutePath());
			if (StringHelper.isImage(file.getName())) {
				if (countAccess) {
					previewURL = URLHelper.createTransformURL(ctx, getPage(), getImageFilePath(ctx, fileName), previewFilter);
				} else {
					previewURL = URLHelper.createTransformURLWithoutCountAccess(ctx, getImageFilePath(ctx, fileName), previewFilter);
				}
			} else if (StringHelper.isVideo(file.getName())) {
				String imageName = StringHelper.getFileNameWithoutExtension(fileName) + ".jpg";
				if (countAccess) {
					previewURL = URLHelper.createTransformURL(ctx, getPage(), getImageFilePath(ctx, imageName), previewFilter);
				} else {
					previewURL = URLHelper.createTransformURLWithoutCountAccess(ctx, getImageFilePath(ctx, imageName), previewFilter);
				}
			}
			resource.setPreviewURL(previewURL);
			
			if (filter.accept(resource)) {
				resources.add(resource);
			}
		}
		return resources;
	}

	private boolean isCountAccess(ContentContext ctx) {
		return StringHelper.isTrue(getConfig(ctx).getProperty("count-access",null));
	}
	
	private String getPreviewFilter(ContentContext ctx) {
		return getConfig(ctx).getProperty("filter.preview","preview");
	}
	
	private String getRelation(ContentContext ctx) {
		return getConfig(ctx).getProperty("html.relation","gal");
	}


	@Override
	public String getType() {
		return TYPE;
	}

	protected MultimediaResourceFilter getFilter(ContentContext ctx) {
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		MultimediaResourceFilter filter = new MultimediaResourceFilter();
		filter.setQuery(rs.getParameter("query", null));
		return filter;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		
		List<MultimediaResource> resouces = new LinkedList<MultimediaResource>();
		PaginationContext pagination = PaginationContext.getInstance(ctx.getRequest(), getId(), resouces.size(), getPageSize(ctx));
		ctx.getRequest().setAttribute("pagination", pagination);
		ctx.getRequest().setAttribute("resources", getResources(ctx));
	}

	private int getPageSize(ContentContext ctx) {
		return Integer.parseInt(getConfig(ctx).getProperty("filter.preview","32"));
	}

}
