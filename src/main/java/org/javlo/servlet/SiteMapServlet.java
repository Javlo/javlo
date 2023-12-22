package org.javlo.servlet;

import org.apache.commons.lang3.StringEscapeUtils;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.*;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.utils.HtmlPart;
import org.owasp.encoder.Encode;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class SiteMapServlet extends HttpServlet {
	
	private static Logger logger = Logger.getLogger(SiteMapServlet.class.getName());

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
	
	protected static String escapeJson (String text) {
		return StringEscapeUtils.escapeJson(text);
	}

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {

			GlobalContext globalContext = GlobalContext.getInstance(request);
			if (globalContext.isPreviewMode() && globalContext.getPublishDate() != null) {
				long lastModified = globalContext.getPublishDate().getTime();
				response.setDateHeader(NetHelper.HEADER_LAST_MODIFIED, lastModified);
				response.setHeader("Cache-Control", "max-age=60,must-revalidate");
				long lastModifiedInBrowser = request.getDateHeader(NetHelper.HEADER_IF_MODIFIED_SINCE);
				if (lastModified > 0 && lastModified / 1000 <= lastModifiedInBrowser / 1000) {
					response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					return;
				}
			}
			String siteMapFile = request.getRequestURI();
			if (siteMapFile.contains(request.getServletPath())) {
				siteMapFile = siteMapFile.substring(siteMapFile.indexOf(request.getServletPath()));
			}
			int number = 0;
			String pageName = null;
			ContentContext ctx = ContentContext.getContentContext(request, response);
			if (request.getServletPath().equals("/sitemap") && siteMapFile.contains("-")) {
				String[] splitedMapFile = siteMapFile.split("-");
				if (splitedMapFile.length == 2) {
					String lastItem = splitedMapFile[1].substring(0, splitedMapFile[1].lastIndexOf("."));
					if (StringHelper.isDigit(lastItem)) {
						number = Integer.parseInt(lastItem);
					} else {
						pageName = lastItem;
					}
				} else {
					String lastItem = splitedMapFile[2].substring(0, splitedMapFile[2].lastIndexOf("."));
					number = Integer.parseInt(lastItem);
					pageName = splitedMapFile[1]; 
				}
			}
			
			ctx.setFree(true);
			ContentService content = ContentService.getInstance(request);

			if (number == 0) {
				siteMapFile = request.getServletPath();
			}
			
			response.setContentType("text/xml");
			
			List<MenuElement> root;
			Calendar lastestDate = null;
			boolean sitemapResource = globalContext.getSpecialConfig().isSitemapResources();
			if (pageName != null) {				
				MenuElement mainPage = content.getNavigation(ctx).searchChildFromName(pageName);				
				if (mainPage == null) {
					root = Collections.EMPTY_LIST;					
					logger.severe("page not found > '"+pageName+"'");
				} else {
					root = new LinkedList<MenuElement>();
					root.add(mainPage);
				}
			} else if (ctx.getRequest().getServletPath().equalsIgnoreCase("/news-sitemap.xml")) {
				root = MacroHelper.searchArticleRoot(ctx);
				lastestDate = Calendar.getInstance();
				for (int j=0; j<globalContext.getStaticConfig().getSiteMapNewsLimit(); j++) {
					lastestDate.roll(Calendar.DAY_OF_YEAR, false);					
				}				
				SiteMapBloc sitemap = XMLHelper.getSiteMapNewsBloc(ctx, root, 1, lastestDate);
				Writer writer = new OutputStreamWriter(response.getOutputStream(), "UTF8");
				BufferedWriter out = new BufferedWriter(writer);					
				out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				out.newLine();
				out.write("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" xmlns:news=\"http://www.google.com/schemas/sitemap-news/0.9\" xmlns:image=\"http://www.google.com/schemas/sitemap-image/1.1\" >");
				out.write(sitemap.getText());
				out.write("</urlset>");
				out.newLine();
				out.flush();
				return;
			}  else if (ctx.getRequest().getServletPath().equalsIgnoreCase("/images-sitemap.xml") && sitemapResource) {				
				Writer writer = new OutputStreamWriter(response.getOutputStream(), "UTF8");		
				BufferedWriter out = new BufferedWriter(writer);		
				out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				out.newLine();
				out.write("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"  xmlns:xhtml=\"http://www.w3.org/1999/xhtml\" xmlns:image=\"http://www.google.com/schemas/sitemap-image/1.1\">");
				out.newLine();
				out.write("</urlset>");
				out.newLine();
				out.flush();
				return;
			} else if (ctx.getRequest().getServletPath().equalsIgnoreCase("/sitemap.json")) {
				response.setContentType("application/json");
				lastestDate = Calendar.getInstance();
				for (int j=0; j<globalContext.getStaticConfig().getSiteMapNewsLimit(); j++) {
					lastestDate.roll(Calendar.DAY_OF_YEAR, false);					
				}				
				Writer writer = new OutputStreamWriter(response.getOutputStream(), "UTF8");
				BufferedWriter out = new BufferedWriter(writer);
				
				out.write('{');
				out.write("\"name\": \""+Encode.forJavaScript(ctx.getGlobalContext().getGlobalTitle())+"\",");
				out.write("\"lastmod\": \""+StringHelper.renderSortableTime(ctx.getGlobalContext().getPublishDate())+"\",");
				out.write("\"url\":\""+Encode.forHtmlAttribute(URLHelper.createStaticURL(ctx.getContextForAbsoluteURL(), "/"))+"\",");
				out.write("\"pages\": [");
				String sep="";
				ContentContext urlCtx = new ContentContext(ctx);
				urlCtx.setFormat("html");
				for (MenuElement page : content.getNavigation(ctx).getAllChildrenList()) {					
					if (page.isActive() && page.getFinalSeoWeight() > MenuElement.SEO_HEIGHT_NULL) {
						out.write(sep);
						out.write("{");						
						out.write("\"page\":\""+escapeJson(page.getName())+"\",");
						out.write("\"title\":\""+escapeJson(page.getTitle(ctx))+"\",");						
						HtmlPart description = page.getDescription(ctx);
						if (description != null && !StringHelper.isEmpty(description.getText())) {
							out.write("\"description\":\""+escapeJson(description.getText())+"\",");
						}
						out.write("\"date\":\""+StringHelper.renderDate(page.getContentDateNeverNull(ctx))+"\",");
						out.write("\"url\":\""+escapeJson(URLHelper.createURL(urlCtx, page))+"\",");
						String contentToSeach = (page.getKeywords(ctx)+" "+StringHelper.collectionToString(page.getSubTitles(ctx, 2), " - ")).trim();
						if (contentToSeach.length()>0) {
							out.write("\"content\":\""+escapeJson(contentToSeach)+"\",");
						}
						out.write("\"weight\":"+page.getFinalSeoWeight());
						out.write("}");
						sep=",";
					}
				}
				out.write("]}");
				out.flush();
				return;
			} else {
				root = new LinkedList<MenuElement>();
				root.add(content.getNavigation(ctx));
			}			
			if (number > 0) {
				SiteMapBloc sitemap = XMLHelper.getSiteMapBloc(ctx, root, number, lastestDate, sitemapResource);
				if (StringHelper.isEmpty(sitemap.getText())) {
					logger.warning("");
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				} else {
					Writer writer = new OutputStreamWriter(response.getOutputStream(), "UTF8");		
					BufferedWriter out = new BufferedWriter(writer);							
					out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
					out.newLine();
					out.write("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"  xmlns:xhtml=\"http://www.w3.org/1999/xhtml\" xmlns:image=\"http://www.google.com/schemas/sitemap-image/1.1\">");
					out.newLine();
					out.write(sitemap.getText());
					out.write("</urlset>");
					out.newLine();
					out.flush();
				}
			} else {		
				Writer writer = new OutputStreamWriter(response.getOutputStream(), "UTF8");
				BufferedWriter out = new BufferedWriter(writer);
				out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				out.newLine();
				out.write("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
				out.newLine();
				int i = 1;
				SiteMapBloc sitemap = XMLHelper.getSiteMapBloc(ctx, root, i, lastestDate, sitemapResource);
				SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
				while (!StringHelper.isEmpty(sitemap.getText())) {
					out.write("<sitemap>");
					out.newLine();
					if (pageName == null) {						
						out.write("<loc>" + URLHelper.createStaticURL(ctx.getContextForAbsoluteURL(), "/sitemap/sitemap-" + i + ".xml") + "</loc>");
					} else {
						out.write("<loc>" + URLHelper.createStaticURL(ctx.getContextForAbsoluteURL(), "/sitemap/sitemap-" + pageName + '-' + i + ".xml") + "</loc>");
					}
					out.write("<lastmod>" + dataFormat.format(sitemap.getLastmod()) + "</lastmod>");
					out.newLine();
					out.write("</sitemap>");
					out.newLine();
					i++;
					sitemap = XMLHelper.getSiteMapBloc(ctx, root, i, lastestDate, sitemapResource);
				}
				out.newLine();
				out.write("</sitemapindex>");

				out.flush();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
