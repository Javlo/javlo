package org.javlo.service.shared;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.image.GlobalImage;
import org.javlo.context.ContentContext;
import org.javlo.context.INeedContentContext;
import org.javlo.navigation.MenuElement;

/**
 * get all shared page from parent of the parent (mailing composition)
 * 
 * @author pvandermaesen
 * 
 */
public class CloserJavloSharedContentProvider extends AbstractSharedContentProvider {

	public static final String NAME = "closer-javlo-local";

	public CloserJavloSharedContentProvider() {
		setName(NAME);		
	}

	private static String getSharedName(MenuElement page, int i) {
		/*
		 * if (page.getSharedName() != null && page.getSharedName().length() >
		 * 0) { return page.getSharedName(); } else
		 */if (page.getParent() != null && page.getParent().getSharedName() != null && page.getParent().getSharedName().length() > 0) {
			return page.getParent().getSharedName() + '-' + i;
		} else {
			return null;
		}
	}

	@Override
	public Collection<SharedContent> getContent(ContentContext ctx) {

		MenuElement currentPage;
		try {
			currentPage = ctx.getCurrentPage();
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}

		if (currentPage.getParent() == null) {
			return Collections.EMPTY_LIST;
		}
		if (!currentPage.isChildrenAssociation() && currentPage.getParent().getParent() == null) {
			return Collections.EMPTY_LIST;
		}

		MenuElement rootPage = currentPage.getParent();
		if (!currentPage.isChildrenAssociation()) {
			rootPage = currentPage.getParent().getParent();
		}

		List<SharedContent> outContent = new LinkedList<SharedContent>();
		try {
			getCategories(ctx).clear();
			int i = 0;
			for (MenuElement page : rootPage.getAllChildren()) {
				i++;
				if (getSharedName(page, i) != null && page.isRealContent(ctx)) {
					List<ComponentBean> beans = Arrays.asList(page.getContent());
					SharedContent sharedContent = new SharedContent(getSharedName(page, i), beans);
					for (ComponentBean bean : beans) {
						sharedContent.setLinkInfo(page.getId());
						if (bean.getType().equals(GlobalImage.TYPE)) {
							try {
								GlobalImage image = new GlobalImage();
								image.init(bean, ctx);
								String imageURL = image.getPreviewURL(ctx, "shared-preview");
								sharedContent.setImageUrl(imageURL);								
								if (page.getParent() != null) {
									if (!getCategories(ctx).containsKey(page.getParent().getName())) {
										getCategories(ctx).put(page.getParent().getName(), page.getParent().getTitle(ctx));
									}
									sharedContent.addCategory(page.getParent().getName());
								}
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

	/**
	 * never empty because dynamic
	 */
	@Override
	public boolean isEmpty(ContentContext ctx) {
		return false;
	}

}
