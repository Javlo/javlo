package org.javlo.service.shared;

import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;

public class SharedContentAction extends AbstractModuleAction {

	@Override
	public String getActionGroupName() {
		return "shared-content";
	}
	
	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String msg = super.prepare(ctx, modulesContext);
		Collection<ISharedContentProvider> contentProviders = SharedContentService.getInstance(ctx).getAllProvider(ctx);
		for (ISharedContentProvider iSharedContentProvider : contentProviders) {
			if (iSharedContentProvider instanceof JavloSharedContentProvider) {
				((JavloSharedContentProvider)iSharedContentProvider).setContentContext(ctx);
			}
		}
		ctx.getRequest().setAttribute("providers", contentProviders);
		return msg;
	}
	
	public static String performChoose(HttpSession session, RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		SharedContentContext sharedContentContext = SharedContentContext.getInstance(session);
		if (rs.getParameter("provider", "").length() > 0) {
			sharedContentContext.setProvider(rs.getParameter("provider", ""));
		}
		if (rs.getParameter("category", "").length() > 0) {
			sharedContentContext.getCategories().clear();
			sharedContentContext.getCategories().add(rs.getParameter("category", ""));
		}
		return null;
	}
	
	public static String performRefresh(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		String providerName = rs.getParameter("provider", "");
		ISharedContentProvider provider = SharedContentService.getInstance(ctx).getProvider(ctx, providerName);
		if (provider != null) {
			provider.refresh();
		}
		return null;
	}
	
	
}
