/**
 * Created on Aug 13, 2003
 */
package org.javlo.actions;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.PaginationContext;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.rendering.Device;
import org.javlo.template.Template;

/**
 * @author pvandermaesen list of actions for search in cms.
 */
public class ViewActions implements IAction {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ViewActions.class.getName());

	public static String performLanguage(HttpServletRequest request, HttpServletResponse response) {
		String msg = null;

		try {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			GlobalContext globalContext = GlobalContext.getInstance(request);
			String lang = request.getParameter("lg");
			if (lang != null) {
				if (globalContext.getLanguages().contains(lang)) {
					ctx.setAllLanguage(lang);
					ctx.setCookieLanguage(lang);
					I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
					i18nAccess.changeViewLanguage(ctx);
					String url = URLHelper.createURL(ctx);
					NetHelper.sendRedirectTemporarily(response, url);
				}
			}

		} catch (Exception e) {
			msg = e.getMessage();
		}

		return msg;
	}

	public static String performReloadconfig(HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.info("reload config");
		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession().getServletContext());
		staticConfig.reload();
		GlobalContext globalContext = GlobalContext.getInstance(request);
		globalContext.reload();
		return null;
	}

	public static String performUnabledalternativetemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		ctx.getCurrentTemplate().enabledAlternativeTemplate(ctx);

		GlobalContext globalContext = GlobalContext.getInstance(request);
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
		i18nAccess.requestInit(ctx); // reload template i18n

		return null;
	}

	public static String performDisabledalternativetemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);

		GlobalContext globalContext = GlobalContext.getInstance(request);

		Template template = ctx.getCurrentTemplate();
		template.disabledAlternativeTemplate(ctx);

		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
		i18nAccess.requestInit(ctx); // reload template i18n

		return null;
	}

	public static String performFrontCache(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String key = request.getParameter("key");
		String value = request.getParameter("value");
		String renderer = request.getParameter("renderer");
		if (key != null && value != null) {
			value = value.replace("<", "&lt;").replace(">", "&gt;");
			globalContext.putItemInFrontCache(ctx, key, value, renderer);
		}
		return null;
	}

	public static String performForcedefaultdevice(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Device device = Device.getDevice(request);
		device.forceDefault();
		logger.info("force default device : " + device.getCode());
		return null;
	}

	public static String performPagination(HttpServletRequest request, HttpServletResponse response) throws Exception {
		PaginationContext paginationContext = PaginationContext.getInstance(request, request.getParameter("key"));
		if (paginationContext != null) {
			logger.fine("new page selected : " + paginationContext.getPage());
		}
		return null;
	}

	@Override
	public String getActionGroupName() {
		return "view";
	}

}
