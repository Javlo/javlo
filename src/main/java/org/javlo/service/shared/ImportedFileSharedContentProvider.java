package org.javlo.service.shared;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

import org.javlo.actions.DataAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.template.Template;

public class ImportedFileSharedContentProvider extends LocalFileSharedContentProvider {
	
	private static Logger logger = Logger.getLogger(ImportedFileSharedContentProvider.class.getName());

	public static final String NAME = "import-file";

	ImportedFileSharedContentProvider() {
		setName(NAME);
	}
	
	@Override
	public Collection<SharedContent> getContent(ContentContext ctx, Collection<String> categories) {		
		if (getCategories(ctx).size() == 0) {
			getContent(ctx);
			if (getCategories(ctx).size() == 0) {			
				return Collections.EMPTY_LIST;
			}
		}
		return super.getContent(ctx, categories);
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
	
	@Override
	public void upload(ContentContext ctx, String fileName, InputStream in, String category, boolean rename) throws IOException {
		File fileFolder = getRootFolder(ctx);
		try {
			fileFolder = new File(URLHelper.mergePath(fileFolder.getAbsolutePath(), URLHelper.mergePath(ctx.getGlobalContext().getStaticConfig().getImportFolder(),DataAction.createImportFolder(ctx))));			
		} catch (Exception e) {
			throw new IOException(e);
		}
		File newFile = new File(URLHelper.mergePath(fileFolder.getAbsolutePath(), fileName));
		if (rename) {
			newFile = ResourceHelper.getFreeFileName(newFile);
		}
		ResourceHelper.writeStreamToFile(in, newFile);
		logger.info("imported file : "+newFile);
	}
	
	@Override
	public boolean isSearch() {
		return false;
	}

}
