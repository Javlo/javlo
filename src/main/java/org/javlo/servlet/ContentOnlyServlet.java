package org.javlo.servlet;

import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.InfoBean;
import org.javlo.filter.CatchAllFilter;
import org.javlo.helper.NetHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.RequestHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
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
		ContentContext ctx  = null; 
		try {
			ctx = ContentContext.getContentContext(request, response);
			response.setContentType("text/html");			
			
			InfoBean.updateInfoBean(ctx);

			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

			String path = ctx.getPath();
			if (path == null || path.trim().length() == 0) {
				path = "/";
			}
			if (globalContext.getPageIfExist(ctx, path, false) == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			
			if (StringHelper.isTrue(request.getParameter("mailing"))) {
				String query = "";
				if (request.getQueryString() != null) {
					query = request.getQueryString().replace("mailing=", "_removed=");
					query = request.getQueryString().replace(CatchAllFilter.CHECK_CONTEXT_PARAM+'=', "_removed=");
				}				
				String newURL = URLHelper.createURL(ctx.getContextForAbsoluteURL().getContextWithOtherRenderMode(ContentContext.PAGE_MODE));
				newURL = URLHelper.addParams(newURL, query);
				URL url = new URL(newURL);
				String content = NetHelper.readPageForMailing(url);				
				if (content == null) {
					logger.warning("could not read : "+url);
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					return;
				} else {
					if (StringHelper.isTrue(request.getParameter("download"))) {
						response.setContentType("application/octet-stream; charset=" + ContentContext.CHARACTER_ENCODING); 
					} else {
						response.setContentType("text/html; charset=" + ContentContext.CHARACTER_ENCODING);
					}
					ResourceHelper.writeStringToStream(content, response.getOutputStream(), ContentContext.CHARACTER_ENCODING);
					return;
				}
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
			
			if (ctx.getGlobalContext().isCollaborativeMode()) {
				Set<String> pageRoles = ctx.getCurrentPage().getEditorRolesAndParent();
				if ((pageRoles.size() > 0 || ctx.getCurrentEditUser() == null)) { 
					if (ctx.getCurrentEditUser() == null || !ctx.getCurrentEditUser().validForRoles(pageRoles)) {
						ctx.setSpecialContentRenderer("/jsp/view/no_access.jsp");
					}
				}
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
		} finally {			
			if (ctx != null && ctx.isClearSession()) {
				HttpSession session = ctx.getRequest().getSession();
				session.invalidate();
			}
		}
	}
}