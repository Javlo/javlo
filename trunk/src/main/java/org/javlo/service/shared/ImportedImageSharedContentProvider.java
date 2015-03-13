package org.javlo.service.shared;

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
	protected boolean isCategoryAccepted(ContentContext ctx, String category, MenuElement cp, Template template) {
		try {
			String importFolder = StringHelper.createFileName(ctx.getCurrentPage().getTitle(ctx.getContextForDefaultLanguage()));
			importFolder = importFolder.replace('-', '_');			
			if (category.endsWith(importFolder)) {				
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

}
