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

	private ContentContext ctx;
	
	public static final String NAME  = "javlo - local";

	public JavloSharedContentProvider(ContentContext ctx) {
		setName(NAME);
		this.ctx = ctx;
	}

	public void setContentContext(ContentContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public Collection<SharedContent> getContent() {
		List<SharedContent> outContent = new LinkedList<SharedContent>();
		ContentService content = ContentService.getInstance(ctx.getRequest());
		try {
			for (MenuElement page : content.getNavigation(ctx).getAllChildren()) {
				if (page.getSharedName() != null && page.getSharedName().length() > 0) {
					List<ComponentBean> beans = Arrays.asList(page.getContent());
					SharedContent sharedContent = new SharedContent(page.getSharedName(), beans);
					for (ComponentBean bean : beans) {
						if (bean.getType().equals(GlobalImage.TYPE)) {
							try {
								GlobalImage image = new GlobalImage();
								image.init(bean, ctx);
								String imageURL = image.getPreviewURL(ctx, "shared-preview");
								sharedContent.setImageUrl(imageURL);								
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
