package org.javlo.servlet;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.links.RSSRegistration;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.tracking.Track;
import org.javlo.tracking.Tracker;

public class XMLServlet extends HttpServlet {

	private static Logger logger = Logger.getLogger(XMLServlet.class.getName());

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

	protected String executeRSSCurrentRenderer(ContentContext ctx, Template template, String imageURL, String largeImageURL, String imageDescription, String text) throws Exception {
		String renderer = template.getRSSRendererFullName(ctx);
		if (renderer != null) {
			logger.fine("execute RSS renderer : '" + renderer);

			ctx.getRequest().setAttribute("imageURL", imageURL);
			ctx.getRequest().setAttribute("largeImageURL", largeImageURL);
			ctx.getRequest().setAttribute("imageDescription", imageDescription);
			ctx.getRequest().setAttribute("text", text);

			return ServletHelper.executeJSP(ctx, renderer);
		} else {
			String imageHTML = "<a href=\"" + largeImageURL + "\"><img src=\"" + imageURL + "\" alt=\"" + imageDescription + "\" /></a>";
			return "<![CDATA[ " + imageHTML + text + "]]>";
		}
	}

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {

		try {
			Writer out = response.getWriter();

			String uri = request.getPathInfo();

			String[] uriSplited = uri.split("/");
			if (uriSplited.length < 4) {
				response.setContentType("application/xhtml+xml");
				out.write("<?xml version=\"1.0\" encoding=\"" + ContentContext.CHARACTER_ENCODING + "\"?>");
				out.write("<xml><error>malformed URL use : xml/[lang]/[format]/[group]</error></xml>");
			} else {
				String lang = uriSplited[1];
				String templateName = null;
				String format = uriSplited[2];
				String channel = URLHelper.extractName(uriSplited[3]);
				if (uriSplited.length > 4) {
					lang = uriSplited[1];
					templateName = uriSplited[2];
					format = uriSplited[3];
					channel = URLHelper.extractName(uriSplited[4]);
				}

				Template template = TemplateFactory.getTemplates(getServletContext()).get(templateName);

				if (format.toLowerCase().equals("rss")) {

					response.setContentType("application/xhtml+xml");

					GlobalContext globalContext = GlobalContext.getInstance(request);
					ContentContext ctx = ContentContext.getNewContentContext(request, response);
					ctx.setAbsoluteURL(true);
					ctx.setLanguage(lang);
					ContentService content = ContentService.createContent(request);

					/* tracking */
					Tracker tracker = Tracker.getTracker(globalContext, request.getSession());
					Track track = new Track(null, null, request.getRequestURI(), System.currentTimeMillis(), request.getHeader("Referer"), request.getHeader("User-Agent"));
					track.setIP(request.getRemoteAddr());
					track.setSessionId(request.getSession().getId());
					tracker.addTrack(track);

					List<MenuElement> rssPages = content.getNavigation(ctx).getAllChildsWithComponentType(ctx, RSSRegistration.TYPE);
					List<MenuElement> pages = new ArrayList<MenuElement>(rssPages);

					Date latestDate = new Date(0);
					Iterator<MenuElement> iter = pages.iterator();
					while (iter.hasNext()) {
						MenuElement page = iter.next();
						if (!page.isRealContent(ctx)) {
							iter.remove();
							break;
						} else {
							ContentElementList compList = page.getAllContent(ctx);
							while (compList.hasNext(ctx)) {
								IContentVisualComponent comp = compList.next(ctx);
								if (comp instanceof RSSRegistration) {
									RSSRegistration rss = (RSSRegistration) comp;
									if (rss.isHideInvisible() && !page.isVisible()) {
										iter.remove();
										break;
									}
								}
							}
							if (page.getModificationDate().getTime() > latestDate.getTime()) {
								latestDate = page.getModificationDate();
							}
						}
					}

					out.write("<?xml version=\"1.0\" encoding=\"" + ContentContext.CHARACTER_ENCODING + "\"?>");

					String rss = null;

					if (template != null && template.getRssCSS() != null) {
						rss = URLHelper.createStaticTemplateURL(ctx, template, template.getRssCSS());
					}

					if (rss != null) {
						out.write("<?xml-stylesheet type=\"text/css\" href=\"" + rss + "\"?>");
					}

					out.write("<rss version=\"2.0\">");
					out.write("<channel>");
					out.write("<title>" + globalContext.getGlobalTitle() + " - " + channel + "</title>");
					out.write("<link>" + URLHelper.createAbsoluteViewURL(ctx, "/") + "</link>");
					out.write("<description>" + globalContext.getGlobalTitle() + "</description>");
					out.write("<language>" + lang + "</language>");
					out.write("<pubDate>" + StringHelper.renderDateAsRFC822String(latestDate) + "</pubDate>");
					out.write("<generator>" + ContentContext.PRODUCT_NAME + "</generator>");

					for (MenuElement page : pages) {
						if (page.isRealContent(ctx)) {
							ContentElementList contentList = page.getAllContent(ctx);
							List<String> pageChannel = new LinkedList<String>();
							boolean allChannel = false;
							boolean componentRSSFound = false;
							while (contentList.hasNext(ctx)) {
								IContentVisualComponent comp = contentList.next(ctx);
								if (comp.getType().equals(RSSRegistration.TYPE)) {
									componentRSSFound = true;
									if (comp.getValue(ctx).trim().length() == 0) {
										allChannel = true;
									} else {
										pageChannel.add(comp.getValue(ctx));
									}
								}
							}

							if (componentRSSFound) {
								if (allChannel || (pageChannel.contains(channel)) || (channel.equals("all"))) {
									out.write("<item>");
									out.write("<title><![CDATA[ " + page.getTitle(ctx) + "]]> </title>");

									String imageURL = null;
									String largeImageURL = null;
									String imageDescription = null;
									if (page.getImage(ctx) != null) {
										imageURL = URLHelper.createTransformURL(ctx, page, template, page.getImage(ctx).getImageURL(ctx), "rss");
										largeImageURL = URLHelper.createTransformURL(ctx, page, template, page.getImage(ctx).getImageURL(ctx), "thumb-view");
										imageDescription = page.getImage(ctx).getImageDescription(ctx);
									}
									out.write("<description>" + executeRSSCurrentRenderer(ctx, template, imageURL, largeImageURL, imageDescription, page.getDescription(ctx)) + "</description>");
									if (template.isPDFRenderer()) {
										String pdfLink = URLHelper.createURL(ctx.getContextWithOtherFormat("pdf"), page.getPath());
										out.write("<enclosure url=\"" + pdfLink + "\" type=\"application/pdf\" />");
									}
									out.write("<authors>" + page.getCreator() + "</authors>");
									out.write("<pubDate>" + StringHelper.renderDateAsRFC822String(page.getModificationDate()) + "</pubDate>");
									out.write("<link><![CDATA[ " + URLHelper.createURL(ctx.getContextWithOtherFormat("html"), page.getPath()) + "]]> </link>");
									out.write("</item>");
								}
							}
						}
					}

					out.write("</channel>");
					out.write("</rss>");
				} else {
					response.setContentType("application/xhtml+xml");
					out.write("<?xml version=\"1.0\" encoding=\"" + ContentContext.CHARACTER_ENCODING + "\"?>");
					out.write("<xml><error>format not found : " + format + "</xml>");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}