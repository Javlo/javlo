package org.javlo.service.shared;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ServletHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;

public class SharedContentAction extends AbstractModuleAction {
	
	public static class SharedContentBean {
		
		private ISharedContentProvider contentProvider;
		private boolean active = false;
		
		public SharedContentBean (ISharedContentProvider inContentProvider) {
			contentProvider = inContentProvider;
		}

		public String getName() {
			return contentProvider.getName();
		}

		public URL getURL() {
			return contentProvider.getURL();
		}

		public Collection<SharedContent> getContent() {
			return contentProvider.getContent();
		}

		public Map<String, String> getCategories() {
			return contentProvider.getCategories();
		}

		public boolean isSearch() {
			return contentProvider.isSearch();
		}

		public boolean isEmpty() {
			return contentProvider.isEmpty();
		}

		public String getType() {
			return contentProvider.getType();
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}
		
		
		
	}

	@Override
	public String getActionGroupName() {
		return "shared-content";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String msg = super.prepare(ctx, modulesContext);
		
		SharedContentService service = SharedContentService.getInstance(ctx);
		List<String> activeProvider = service.getActiveProviderNames(ctx);
		
		Collection<ISharedContentProvider> contentProviders = SharedContentService.getInstance(ctx).getAllProvider(ctx);
		List<SharedContentBean> beans = new LinkedList<SharedContentAction.SharedContentBean>();
		for (ISharedContentProvider iSharedContentProvider : contentProviders) {
			ctx.setContentContextIfNeeded(iSharedContentProvider);
			SharedContentBean bean = new SharedContentBean(iSharedContentProvider);
			if (activeProvider.contains(bean.getName())) {				
				bean.setActive(true);
			}
			beans.add(bean);
		}
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		for (URL url : SharedContentProviderFactory.getInstance(ctx).getURLList(ctx.getGlobalContext())) {
			out.println(url);
		}
		out.close();
		ctx.getRequest().setAttribute("urls", new String(outStream.toByteArray()));

		ctx.getRequest().setAttribute("providers", beans);
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

	public static String performURLList(RequestService rs, GlobalContext globalContext, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		String urlList = rs.getParameter("url-list", null);
		if (urlList == null) {
			return "param 'url-list' not found.";
		}
		SharedContentProviderFactory.getInstance(ctx).setURLList(globalContext, urlList);
		return null;
	}

	public static String performSearch(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		String providerName = rs.getParameter("provider", "");
		ISharedContentProvider provider = SharedContentService.getInstance(ctx).getProvider(ctx, providerName);
		if (provider == null) {
			return "provider not found : " + providerName;
		} else {
			Collection<SharedContent> sharedContent = SharedContentService.getInstance(ctx).searchContent(provider, rs.getParameter("query", ""));		
			ctx.getRequest().setAttribute("sharedContent", sharedContent);
			if (ctx.isAjax()) {
				String result;
				try {
					result = ServletHelper.executeJSP(ctx, "/jsp/preview/shared_content_result.jsp");					
				} catch (Exception e) {
					result = e.getMessage();
					e.printStackTrace();
				}
				ctx.getAjaxInsideZone().put("shared-content-result", result);
			}
		}
		return null;
	}
	
	public static String performUpdateActive(RequestService rs, ContentContext ctx, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) {
		Collection<ISharedContentProvider> contentProviders = SharedContentService.getInstance(ctx).getAllProvider(ctx);		
		List<String> activeProvider = new LinkedList<String>();
		for (ISharedContentProvider contentProvider : contentProviders) {			
			if (rs.getParameter("active-"+contentProvider.getName(), null) != null) {
				System.out.println("***** SharedContentAction.performUpdateActive : contentProvider.getName() = "+contentProvider.getName()); //TODO: remove debug trace
				activeProvider.add(contentProvider.getName());
			}			
		}
		SharedContentService service = SharedContentService.getInstance(ctx);
		service.setActiveProviderNames(ctx, activeProvider);
		return null;
	}

}
