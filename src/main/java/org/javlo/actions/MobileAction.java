package org.javlo.actions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModuleBean;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;

public class MobileAction implements IAction {

	public MobileAction() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getActionGroupName() {
		return "mobile";
	}

	public static String performModulesList(RequestService rs, HttpSession session, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, HttpServletRequest request) throws ModuleException {
		List<ModuleBean> modules = new LinkedList<ModuleBean>();
		ModulesContext moduleContext = ModulesContext.getInstance(session, ctx.getGlobalContext());
		for (Module module : moduleContext.getModules()) {
			if (module.isMobile()) {
				modules.add(new ModuleBean(ctx, module));				
			}
		}
		ctx.getAjaxData().put("modules", modules);
		ctx.getAjaxData().put("title", ctx.getGlobalContext().getGlobalTitle());
		return null;
	}
	
	public static String performInit(RequestService rs, HttpSession session, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, HttpServletRequest request) throws ModuleException {
		ctx.getAjaxData().put("title", ctx.getGlobalContext().getGlobalTitle());
		ctx.getAjaxData().put("token", ctx.getCurrentEditUser().getUserInfo().getToken());
		ContentContext ajaxAbsCtx = ctx.getContextForAbsoluteURL();
		Map<String,String> params = new HashMap<String, String>();
		params.put("j_token", ctx.getCurrentEditUser().getUserInfo().getToken());
		String ajaxURL = URLHelper.createAjaxURL(ajaxAbsCtx, params);
		ctx.getAjaxData().put("url", ajaxURL);
		return null;
	}

}
