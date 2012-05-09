package org.javlo.servlet;

import java.util.Iterator;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.RequestHelper;
import org.javlo.mailing.MailingContext;
import org.javlo.navigation.PageConfiguration;
import org.javlo.service.RequestService;
import org.javlo.template.Template;


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

			RequestHelper.traceMailingFeedBack(ctx);

            ctx.setRenderMode(ContentContext.PAGE_MODE); // todo: check how we can remove this line.

            ctx.setAbsoluteURL(true);
            MailingContext mailingCtx = MailingContext.getInstance(request.getSession());
            GlobalContext globalContext = GlobalContext.getInstance(request);
            PageConfiguration pageConfiguration = PageConfiguration.getInstance(globalContext);
            RequestService requestService = RequestService.getInstance(request);
            String templateID = requestService.getParameter(TEMPLATE_PARAM_NAME, null);

            if (templateID == null) {
            	templateID = mailingCtx.getCurrentTemplate();
            	if (templateID == null) {
            		Iterator<Template> ite = pageConfiguration.getMailingTemplates().iterator();
            		Template t = ite.next();
            		while (!t.isValid()&&ite.hasNext()) {
            			t = ite.next();
            		}
            		if (t != null) {
            			templateID = t.getId();
            		}
            	}
            }

            Template template = Template.getApplicationInstance(request.getSession().getServletContext(), ctx, templateID, true);
            getServletContext().getRequestDispatcher(template.getRendererFullName(ctx)).include(request, response);

       } catch (Exception e) {
            e.printStackTrace();
        }
    }
}