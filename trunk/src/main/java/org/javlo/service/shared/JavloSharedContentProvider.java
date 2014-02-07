package org.javlo.service.shared;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.image.GlobalImage;
import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class JavloSharedContentProvider extends AbstractSharedContentProvider {

	public static final String NAME  = "javlo-local";

	public JavloSharedContentProvider() {
		setName(NAME);
	}

	@Override
	public Collection<SharedContent> getContent(ContentContext ctx) {
		List<SharedContent> outContent = new LinkedList<SharedContent>();
		ContentService content = ContentService.getInstance(ctx.getRequest());
		try {
			getCategories(ctx).clear();
			for (MenuElement page : content.getNavigation(ctx).getAllChildren()) {
				if (page.getSharedName() != null && page.getSharedName().length() > 0 && page.isRealContent(ctx)) {
					List<ComponentBean> beans = Arrays.asList(page.getContent());
					SharedContent sharedContent = new SharedContent(page.getSharedName(), beans);
					if (page.getParent() != null) {
						if (!getCategories(ctx).containsKey(page.getParent().getName())) {
							getCategories(ctx).put(page.getParent().getName(), page.getParent().getTitle(ctx));
						}
						sharedContent.addCategory(page.getParent().getName());
					}
					for (ComponentBean bean : beans) {
						if (bean.getType().equals(GlobalImage.TYPE) && (sharedContent.getImageURL() == null || sharedContent.getImageURL().trim().length() == 0)) {
							try {
								GlobalImage image = new GlobalImage();
								image.init(bean, ctx);
								String imageURL = image.getPreviewURL(ctx, "shared-preview");
								sharedContent.setImageUrl(imageURL);
								sharedContent.setLinkInfo(page.getId());								
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					outContent.add(sharedContent);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outContent;
	}
}
