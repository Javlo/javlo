package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.admin.AdminAction;
import org.javlo.ztatic.FileCache;

public class ClearImageCache extends AbstractMacro {

	@Override
	public String getName() {
		return "clean-image-cache";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		MessageRepository messageRepository = MessageRepository.getInstance(ctx);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
		FileCache fileCache = FileCache.getInstance(ctx.getServletContext());
		return AdminAction.performClearimagecache(ctx.getRequest(), ctx.getGlobalContext(), ctx.getSession(), ctx.getCurrentUser(), ctx, messageRepository, i18nAccess, fileCache);
	}

	@Override
	public boolean isPreview() {
		return true;
	}
	
	@Override
	public String getIcon() {
		return "bi bi-stars";

}
