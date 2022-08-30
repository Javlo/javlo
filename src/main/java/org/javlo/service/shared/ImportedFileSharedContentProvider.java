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
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
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
			String importFolder = DataAction.createImportFolder( page);
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
		if (!ResourceHelper.isAcceptedDocument(ctx, fileName)) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("global.message.file-format-error"), GenericMessage.ERROR));
			return;
		}
		File fileFolder = getRootFolder(ctx);
		try {
			fileFolder = new File(URLHelper.mergePath(fileFolder.getAbsolutePath(), URLHelper.mergePath(ctx.getGlobalContext().getStaticConfig().getImportFolder(),DataAction.createImportFolder(ctx.getCurrentPage()))));			
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
