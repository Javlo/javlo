package org.javlo.service.shared;

import java.util.Collection;
import java.util.Collections;

import org.javlo.actions.DataAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.template.Template;

public class ImportedImageSharedContentProvider extends LocalImageSharedContentProvider {

	public static final String NAME = "import-image";

	ImportedImageSharedContentProvider() {
		setName(NAME);
	}
	
	@Override
	public Collection<SharedContent> getContent(ContentContext ctx, Collection<String> categories) {
		if (getCategories(ctx).size() == 0) {
			return Collections.EMPTY_LIST;
		} else {
			return super.getContent(ctx, categories);
		}
	}

	@Override
	protected boolean isCategoryAccepted(ContentContext ctx, String category, MenuElement cp, Template template) {
		try {
			MenuElement page = ctx.getCurrentPage().getRootOfChildrenAssociation();
			if (page == null) {
				page = ctx.getCurrentPage();
			}
			String importFolder = DataAction.createImportFolder(ctx);						
			if (category.endsWith(importFolder)) {				
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

}
