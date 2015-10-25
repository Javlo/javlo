package org.javlo.service.shared;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.image.GlobalImage;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.filter.ImageFileFilter;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.template.Template;
import org.javlo.ztatic.StaticInfo;

public class LocalImageSharedContentProvider extends AbstractSharedContentProvider {
	
	private static Logger logger = Logger.getLogger(LocalImageSharedContentProvider.class.getName());
	
	public static final String NAME = "local-image";
	
	private Collection<SharedContent> content = new LinkedList<SharedContent>();
	
	LocalImageSharedContentProvider() {
		setName(NAME);
	}

	@Override
	public Collection<SharedContent> getContent(ContentContext ctx) {		
		setCategories(new HashMap<String, String>());
		content.clear();
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		File imageFolder = new File(URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getImageFolder()));
		for (File imageFile : ResourceHelper.getAllFiles(imageFolder, new ImageFileFilter() ) ) {			
			String category = imageFile.getParentFile().getAbsolutePath().replace(imageFolder.getAbsolutePath(), "");
			category = category.replace('\\', '/');
			if (category.startsWith("/")) {
				category = category.substring(1);
			}
			if (!categories.containsKey(category)) {
				categories.put(category, category);
			}
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			out.println("dir=" + category);
			out.println("file-name=" + imageFile.getName());
			out.println(GlobalImage.IMAGE_FILTER + "=full");
			out.close();
			String value = new String(outStream.toByteArray());
			ComponentBean imageBean = new ComponentBean(GlobalImage.TYPE, value, ctx.getRequestContentLanguage());
			imageBean.setArea(ctx.getArea());
			SharedContent sharedContent;
			try {
				StaticInfo staticInfo = StaticInfo.getInstance(ctx, imageFile);
				sharedContent = new SharedContent(""+imageFile.hashCode(), imageBean);
				sharedContent.addCategory(category);
				sharedContent.setSortOn(staticInfo.getCreationDate(ctx).getTime());
				content.add(sharedContent);				
				GlobalImage image = new GlobalImage();
				image.init(imageBean, ctx);
				String imageURL = image.getPreviewURL(ctx, "shared-preview");
				sharedContent.setTitle(imageFile.getName());				
				sharedContent.setDescription(staticInfo.getTitle(ctx));
				sharedContent.setImageUrl(imageURL);				
				sharedContent.setEditAsModal(true);
				String url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE));
				url = URLHelper.addParam(url, "webaction", "file.previewEdit");
				url = URLHelper.addParam(url, "module", "file");
				url = URLHelper.addParam(url, "nobreadcrumbs", "true");				
				url = URLHelper.addParam(url, "file", URLHelper.encodePathForAttribute(imageFile.getPath()));
				url = URLHelper.addParam(url, "previewEdit", "true");		
				sharedContent.setEditURL(url);

			} catch (Exception e) {				
				e.printStackTrace();
			}			
		}
		return content;
	}
	
	protected boolean isCategoryAccepted(ContentContext ctx, String category, MenuElement cp, Template template) {
		if ( !category.startsWith(template.getImportFolder()) && !category.startsWith(template.getImportImageFolder()) && !category.startsWith(template.getImportGalleryFolder()) && !category.startsWith(template.getImportResourceFolder())) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public Map<String, String> getCategories(ContentContext ctx) {
		Map<String, String> outCategories = new HashMap<String, String>();
		MenuElement cp;
		try {
			cp = ctx.getCurrentPage();			
			Template template = ctx.getCurrentTemplate();
			for (String category : categories.keySet()) {
				String catKey = category;
				category = '/'+category;		
				if (isCategoryAccepted(ctx, category, cp, template)) {
					outCategories.put(catKey, catKey);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return outCategories;
	}

	@Override
	public String getType() {	
		return TYPE_IMAGE;
	}
	
	@Override
	public boolean isUploadable() {
		return true;
	}
	
	@Override
	public void upload(ContentContext ctx, String fileName, InputStream in, String category) throws IOException {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		File imageFolder = new File(URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getImageFolder()));
		imageFolder = new File(URLHelper.mergePath(imageFolder.getAbsolutePath(), category));
		File newFile = new File(URLHelper.mergePath(imageFolder.getAbsolutePath(), fileName));
		newFile = ResourceHelper.getFreeFileName(newFile);		
		ResourceHelper.writeStreamToFile(in, newFile);
		logger.info("imported file : "+newFile);
	}
}
