package org.javlo.service.shared;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.logging.Logger;

import org.javlo.actions.DataAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.template.Template;

public class ImportedImageSharedContentProvider extends LocalImageSharedContentProvider {
	
	private static Logger logger = Logger.getLogger(ImportedImageSharedContentProvider.class.getName());

	public static final String NAME = "import-image";

	ImportedImageSharedContentProvider() {
		setName(NAME);
	}
	
	@Override
	public Collection<SharedContent> getContent(ContentContext ctx, Collection<String> categories) {		
		return getContent(ctx);	
	}

	@Override
	protected boolean isCategoryAccepted(ContentContext ctx, String category, MenuElement cp, Template template) {
		try {
			if (ctx.getCurrentPage() == null) {
				return false;
			}
			MenuElement page = ctx.getCurrentPage().getRootOfChildrenAssociation();
			if (page == null) {
				page = ctx.getCurrentPage();
			}
			String importFolder = DataAction.createImportFolder(ctx.getCurrentPage());
			String importPrefix = ctx.getGlobalContext().getStaticConfig().getImportFolder();
			if (importPrefix.startsWith("/")) {
				importPrefix = importPrefix.substring(1);
			}
			if (category.endsWith(importFolder) && (category.startsWith(importPrefix) || category.startsWith(ctx.getGlobalContext().getStaticConfig().getImportFolder()))) {	
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public void upload(ContentContext ctx, String fileName, InputStream in, String category, boolean rename) throws IOException {
		if (!ResourceHelper.isAcceptedImage(ctx, fileName)) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("global.message.file-format-error"), GenericMessage.ERROR));
			return;
		}
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		File imageFolder = new File(URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getImageFolder()));
		try {
			imageFolder = new File(URLHelper.mergePath(imageFolder.getAbsolutePath(), AbstractVisualComponent.getImportFolderPath(ctx, ctx.getCurrentPage())));			
		} catch (Exception e) {
			throw new IOException(e);
		}
		File newFile = new File(URLHelper.mergePath(imageFolder.getAbsolutePath(), fileName));
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
	
	@Override
	public boolean isEmpty(ContentContext ctx) {
		return false;
	}
	
	@Override
	public boolean isUploadable(ContentContext ctx) {	
		return true;
	}
}
