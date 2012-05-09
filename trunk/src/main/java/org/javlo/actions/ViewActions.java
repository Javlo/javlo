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
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.PageConfiguration;
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
					ctx.setLanguage(lang);
					ctx.setContentLanguage(lang);
					ctx.setCookieLanguage(lang);
					I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
					i18nAccess.changeViewLanguage(ctx);
					//request.getRequestDispatcher(URLHelper.createURL(ctx)).forward(request, response);
					response.sendRedirect(URLHelper.createURL(ctx));
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
		ctx.setCurrentTemplate(ctx.getCurrentTemplate().getFinalTemplate(ctx));

		GlobalContext globalContext = GlobalContext.getInstance(request);
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
		i18nAccess.requestInit(ctx); // reload template i18n

		return null;
	}

	public static String performDisabledalternativetemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		
		GlobalContext globalContext = GlobalContext.getInstance(request);
		PageConfiguration pageConfig = PageConfiguration.getInstance(globalContext);		
		MenuElement currentPage = ctx.getCurrentPage();
		Template template = pageConfig.getCurrentTemplate(ctx, currentPage);

		template.disabledAlternativeTemplate(ctx);
		ctx.setCurrentTemplate(template.getFinalTemplate(ctx));

		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
		i18nAccess.requestInit(ctx); // reload template i18n

		return null;
	}

	public static String performForcedefaultdevice(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Device device = Device.getDevice(request);
		device.forceDefault();
		logger.info("force default device : " + device.getCode());
		return null;
	}

	@Override
	public String getActionGroupName() {
		return "view";
	}

}
