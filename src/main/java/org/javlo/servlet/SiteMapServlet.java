package org.javlo.servlet;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XMLHelper;
import org.javlo.helper.XMLHelper.SiteMapBloc;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class SiteMapServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

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

			String host = request.getServerName();
			if (!GlobalContext.isExist(request, host) && request.getParameter("yes") == null) {
				getServletContext().getRequestDispatcher("/jsp/error/creation.jsp").include(request, response);
				return;
			}
			String siteMapFile = request.getRequestURI();

			int number = 0;
			if (siteMapFile.contains("-")) {
				number = Integer.parseInt(siteMapFile.substring(siteMapFile.lastIndexOf("-") + 1, siteMapFile.lastIndexOf(".")));
			}

			ContentContext ctx = ContentContext.getContentContext(request, response);
			ContentService content = ContentService.getInstance(request);
			
			if (number == 0)  {
				siteMapFile = request.getServletPath();
			}
			
			List<MenuElement> root;
			Calendar lastestDate = null;
			if (siteMapFile.startsWith("news-")) {
				root = MacroHelper.searchArticleRoot(ctx);
				lastestDate.roll(number, false);
			} else if (siteMapFile.startsWith("press-")) {
				root = MacroHelper.searchArticleRoot(ctx);
			} else {
				root = new LinkedList<MenuElement>();
				root.add(content.getNavigation(ctx));						
			}

			if (number > 0) {
				SiteMapBloc sitemap = XMLHelper.getSiteMapBloc(ctx, root, number, lastestDate);
				if (StringHelper.isEmpty(sitemap.getText())) {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				} else {
					PrintStream out = new PrintStream(response.getOutputStream());
					response.setContentType("text/xml");
					out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
					out.println("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">");
					out.println(sitemap.getText());
					out.println("</urlset>");
					out.flush();
				}
			} else {				
				PrintStream out = new PrintStream(response.getOutputStream());
				response.setContentType("text/xml");
				
				out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				out.println("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
				int i=1;
				SiteMapBloc sitemap = XMLHelper.getSiteMapBloc(ctx, root, i, lastestDate);
				SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
				while (!StringHelper.isEmpty(sitemap.getText())) {
					out.println("<sitemap>");
					out.println("<loc>"+URLHelper.createStaticURL(ctx.getContextForAbsoluteURL(), "/sitemap/sitemap-"+i+".xml")+"</loc>");
					out.println("<lastmod>"+dataFormat.format(sitemap.getLastmod())+"</lastmod>");
					out.println("</sitemap>");
					i++;
					sitemap = XMLHelper.getSiteMapBloc(ctx, root, i, lastestDate);
				}
				out.println("</sitemapindex>");
				
				out.flush();
			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
