package org.javlo.service.shared;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.image.GlobalImage;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.filter.ImageFileFilter;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;
import org.javlo.ztatic.StaticInfo;

public class LocalImageSharedContentProvider extends AbstractSharedContentProvider {
	
	private Collection<SharedContent> content = new LinkedList<SharedContent>();
	
	LocalImageSharedContentProvider() {
		setName("local-image");
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
				sharedContent = new SharedContent(""+imageFile.hashCode(), imageBean);
				sharedContent.addCategory(category);
				content.add(sharedContent);
				
				GlobalImage image = new GlobalImage();
				image.init(imageBean, ctx);
				String imageURL = image.getPreviewURL(ctx, "shared-preview");
				sharedContent.setTitle(imageFile.getName());
				StaticInfo staticInfo = StaticInfo.getInstance(ctx, imageFile);
				sharedContent.setDescription(staticInfo.getTitle(ctx));
				sharedContent.setImageUrl(imageURL);
				
				if (!getCategories(ctx).containsKey(category)) {
					getCategories(ctx).put(category, category);
				}
			} catch (Exception e) {				
				e.printStackTrace();
			}			
		}
		return content;
	}

	@Override
	public String getType() {	
		return TYPE_IMAGE;
	}

}
