package org.javlo.servlet;

import java.util.Iterator;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.InfoBean;
import org.javlo.helper.RequestHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.mailing.MailingModuleContext;
import org.javlo.service.RequestService;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

public class ContentOnlyServlet extends HttpServlet {

	public static final String TEMPLATE_PARAM_NAME = "template";
	
	private static final long serialVersionUID = 1L;

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ContentOnlyServlet.class.getName());

	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	public void destroy() {
		super.destroy();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			response.setContentType("text/html");
			ContentContext ctx = ContentContext.getContentContext(request, response);
			
			InfoBean.updateInfoBean(ctx);

			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

			if (globalContext.getPageIfExist(ctx, ctx.getPath(), false) == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}

			RequestHelper.traceMailingFeedBack(ctx);

			ctx.setRenderMode(ContentContext.PAGE_MODE); // todo: check how we can remove this line.

			ctx.setAbsoluteURL(true);
			MailingModuleContext mailingCtx = MailingModuleContext.getInstance(request);
			RequestService requestService = RequestService.getInstance(request);
			String templateID = requestService.getParameter(TEMPLATE_PARAM_NAME, null);
			
			Template template = null;

			if (templateID == null) {			
				templateID = mailingCtx.getCurrentTemplate();				
				if (templateID == null) {
					if (ctx.getCurrentTemplate() != null) {
						template = ctx.getCurrentTemplate();
						if (!template.isMailing()) {							
							for (Template mailingTemplate : TemplateFactory.getAllTemplatesFromContext(globalContext)){
								if (mailingTemplate.isMailing()) {
									templateID = mailingTemplate.getId();
								}
							}
						}
					} else {
						Iterator<Template> ite = ctx.getCurrentTemplates().iterator();
						Template t = ite.next();
						while (!t.isValid() && ite.hasNext() && !t.isMailing()) {
							t = ite.next();
						}
						if (t != null) {
							templateID = ctx.getCurrentTemplates().iterator().next().getId();
						}
					}
				}
			}

			if (templateID != null) {				
				template = TemplateFactory.getTemplates(request.getSession().getServletContext()).get(templateID);
				ctx.setCurrentTemplate(template);
				I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				i18nAccess.requestInit(ctx);
			}			
			String area = requestService.getParameter("only-area", null);
			if (area != null) {
				getServletContext().getRequestDispatcher("/jsp/view/content_view.jsp?area=" + area).include(request, response);
			} else {
				String jspPath = template.getRendererFullName(ctx);
				getServletContext().getRequestDispatcher(jspPath).include(request, response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}