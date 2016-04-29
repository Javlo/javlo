package org.javlo.service.shared;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.image.GlobalImage;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.filter.ImageFileFilter;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.ztatic.StaticInfo;

public class GlobalImageSharedContentProvider extends LocalImageSharedContentProvider {

	private Collection<SharedContent> content = new LinkedList<SharedContent>();

	public static final String NAME = "global-image";

	GlobalImageSharedContentProvider() {
		setName(NAME);
	}

	protected File getRootFolder(ContentContext ctx) {
		StaticConfig sc = ctx.getGlobalContext().getStaticConfig();
		File dir;
		try {
			dir = new File(URLHelper.mergePath(ctx.getGlobalContext().getSharedDataFolder(ctx.getRequest().getSession()), sc.getShareImageFolder()));
			return dir;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Collection<SharedContent> getContent(ContentContext ctx) {
		setCategories(new HashMap<String, String>());
		content.clear();
		File imageFolder = getRootFolder(ctx);
		StaticConfig staticConfig = ctx.getGlobalContext().getStaticConfig();
		for (File imageFile : ResourceHelper.getAllFiles(imageFolder, new ImageFileFilter())) {
			String category = StringHelper.cleanPath(imageFile.getParentFile().getAbsolutePath().replace(imageFolder.getAbsolutePath(), ""));
			category = category.replace('\\', '/');
			if (category.startsWith("/")) {
				category = category.substring(1);
			}
			category = category.trim();
			if (!categories.containsKey(category)) {
				categories.put(category, category);
			}

			try {
				
				if (isCategoryAccepted(ctx, category, ctx.getCurrentPage(), ctx.getCurrentTemplate())) {
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					PrintStream out = new PrintStream(outStream);
					out.println("dir=" + category);
					out.println("file-name=" + URLHelper.mergePath(staticConfig.getShareDataFolderKey(), imageFile.getName()));
					ContentContext masterContext = new ContentContext(ctx);
					masterContext.setForceGlobalContext(ctx.getGlobalContext().getMasterContext(ctx.getRequest().getSession()));
					StaticInfo staticInfo = StaticInfo.getInstance(masterContext, imageFile);
					String desc = staticInfo.getTitle(masterContext) + ' ' + staticInfo.getDescription(masterContext) + ' ' + StringHelper.collectionToString(staticInfo.getTags(masterContext));
					out.println("meta=" + desc);
					out.println(GlobalImage.IMAGE_FILTER + "=full");
					out.close();
					String value = new String(outStream.toByteArray());
					ComponentBean imageBean = new ComponentBean(GlobalImage.TYPE, value, ctx.getRequestContentLanguage());
					imageBean.setArea(ctx.getArea());
					SharedContent sharedContent;
					sharedContent = new SharedContent("" + imageFile.hashCode(), imageBean);
					sharedContent.addCategory(category);
					sharedContent.setSortOn(staticInfo.getCreationDate(ctx).getTime());
					content.add(sharedContent);
					GlobalImage image = new GlobalImage();
					image.init(imageBean, ctx);
					String imageURL = image.getPreviewURL(ctx.getContextWithArea(ComponentBean.DEFAULT_AREA), "shared-preview");
					sharedContent.setTitle(imageFile.getName());
					sharedContent.setDescription(staticInfo.getTitle(ctx));
					sharedContent.setImageUrl(imageURL);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return content;
	}
}