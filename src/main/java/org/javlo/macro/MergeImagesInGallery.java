package org.javlo.macro;

import java.io.File;
import java.util.Map;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.image.Image;
import org.javlo.component.multimedia.Multimedia;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.ztatic.StaticInfo;

public class MergeImagesInGallery extends AbstractMacro {

	@Override
	public String getName() {
		return "merge-images-in-gallery";
	}

	protected boolean transformImageToGallery(ContentContext ctx, Image img, Multimedia gal) throws Exception {
		StaticInfo resource = img.getStaticInfo(ctx);
		String dir = gal.getFilesDirectory(ctx);
		
		File newImageFile = new File(URLHelper.mergePath(dir, resource.getFile().getName()));
		boolean copied = ResourceHelper.copyFile(resource.getFile(), newImageFile, false);
		if (copied) {
			ResourceHelper.copyResourceData(ctx, resource.getFile(), newImageFile);
		}
		return copied;
	}

	protected boolean imageInGallery(ContentContext ctx, MenuElement currentPage) throws Exception {
		Multimedia gal = null;

		ContentElementList comps = currentPage.getAllContent(ctx);
		while (comps.hasNext(ctx)) {
			IContentVisualComponent comp = comps.next(ctx);
			if (comp instanceof Multimedia) {
				gal = (Multimedia) comp;
			}
		}
		if (gal == null) {
			ContentService content = ContentService.getInstance(ctx.getRequest());
			ComponentBean bean = new ComponentBean(Multimedia.TYPE, "", ctx.getRequestContentLanguage());
			String newId = content.createContent(ctx, currentPage, ComponentBean.DEFAULT_AREA, "0", bean, true);
			gal = (Multimedia) content.getComponent(ctx, newId);
			gal.setCurrentRootFolder(ctx, '/'+URLHelper.mergePath(ctx.getGlobalContext().getStaticConfig().getGalleryFolderName(), gal.getImportFolderPath(ctx)));
			gal.setStyle(ctx,  Multimedia.IMAGE);
		}

		int countImage = 0;
		comps = currentPage.getAllContent(ctx);
		while (comps.hasNext(ctx)) {
			IContentVisualComponent comp = comps.next(ctx);
			if (comp instanceof Image && !comp.isRepeat() && comp.getArea().equals(gal.getArea())) {
				transformImageToGallery(ctx, (Image) comp, gal);
				countImage++;
				comp.getPage().removeContent(ctx, comp.getId());
			}
			comp.getPage().releaseCache();
		}
		MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage("Gallery update width " + countImage + " images.", GenericMessage.INFO));

		return true;
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		ContentService.getInstance(ctx.getRequest());
		MenuElement currentPage = ctx.getCurrentPage();
		imageInGallery(ctx, currentPage);
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
};
