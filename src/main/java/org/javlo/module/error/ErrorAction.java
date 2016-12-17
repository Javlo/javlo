package org.javlo.module.error;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;

public class ErrorAction extends AbstractModuleAction {

	@Override
	public String getActionGroupName() {
		return "error";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String msg = super.prepare(ctx, modulesContext);
		
		
		if (modulesContext.getCurrentModule().getRenderer().contains("404")) {
			ctx.getRequest().setAttribute("page", "404");
			Properties list404 = ctx.getGlobalContext().get404UrlMap();
			Map<String, String> final404 = new LinkedHashMap<String, String>();
			for (Iterator iterator = list404.keySet().iterator(); iterator.hasNext();) {
				String url = (String) iterator.next();
				if (!StringHelper.isEmpty(list404.get(url))) {
					final404.put(url, "" + list404.get(url));
				}
			}
			ctx.getRequest().setAttribute("list404", final404);
		} else if (modulesContext.getCurrentModule().getRenderer().contains("forward")) {
			ctx.getRequest().setAttribute("page", "forward");
			Map<String, String> finalForward = new LinkedHashMap<String, String>();
			ContentService content = ContentService.getInstance(ctx.getRequest());
			MenuElement root = content.getNavigation(ctx);
			Properties prop = ctx.getGlobalContext().getRedirectUrlMap();
			for (Iterator iterator = ctx.getGlobalContext().getRedirectUrlMap().keySet().iterator(); iterator.hasNext();) {
				String url = (String) iterator.next();				
				MenuElement page = root.searchChildFromId(prop.getProperty(url));
				finalForward.put(url, page != null?page.getPath():"Page not found !");				
			}
			ctx.getRequest().setAttribute("listForward", finalForward);
		}
		return msg;
	}

	public static String performDelete(RequestService rs, ContentContext ctx, MessageRepository messageRepository, User user) {
		if (!AdminUserSecurity.getInstance().isAdmin(user)) {
			return "no right";
		}
		String url = rs.getParameter("url", null);
		if (url == null) {
			return "no url found.";
		} else {
			Properties list404 = ctx.getGlobalContext().get404UrlMap();
			if (list404.get(url) == null) {
				return "url not found : " + url;
			} else {				
				ctx.getGlobalContext().hide404Url(ctx, url);
				messageRepository.setGlobalMessage(new GenericMessage("404 url removed from list.", GenericMessage.INFO));
			}
		}
		return null;
	}
	
	public static String performDeleteForward(RequestService rs, ContentContext ctx, MessageRepository messageRepository, User user) {
		if (!AdminUserSecurity.getInstance().isAdmin(user)) {
			return "no right";
		}
		String url = rs.getParameter("url", null);
		if (url == null) {
			return "no url found.";
		} else {
			ctx.getGlobalContext().delRedirectUrl(ctx, url);
		}
		return null;
	}
	
	public static String performPage404(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, Module module) throws IOException {
		module.restoreAll();
		return null;
	}
	
	public static String performPageForward(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, Module module) throws IOException {
		module.setRenderer("jsp/forward.jsp");
		return null;
	}
	
	public static String performReset404(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, User user) {
		if (!AdminUserSecurity.getInstance().isAdmin(user)) {
			return "no right";
		}
		ctx.getGlobalContext().reset404File();
		return null;
	}
	
	public static String performForward(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, User user) throws Exception {
		if (!AdminUserSecurity.getInstance().isAdmin(user)) {
			return "no right";
		}
		String pageRef = rs.getParameter("page", null);
		String url = rs.getParameter("url", null);
		if (pageRef == null || url == null) {
			return "no page found.";
		}
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement page = content.getNavigation(ctx).searchChildFromName(pageRef);
		if (page == null)  {
			page = content.getNavigation(ctx).searchChildFromId(pageRef);
		}
		if (page == null)  {
			page = content.getNavigation(ctx).searchChild(ctx, pageRef);
		}
		if (page == null) {
			return "page not found : "+pageRef;
		} else {
			ctx.getGlobalContext().storeUrl(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE), url, page.getId());
			ctx.getGlobalContext().delete404Url(ctx, url);
		}
		return null;
	}
}
