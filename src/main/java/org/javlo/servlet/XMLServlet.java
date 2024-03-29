package org.javlo.servlet;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.links.RSSRegistration;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.ContentDateComparator;
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
		String renderer = null;
		if (template != null) {
			renderer = template.getRSSRendererFullName(ctx);
		}
		if (renderer != null) {
			logger.fine("execute RSS renderer : '" + renderer);

			ctx.getRequest().setAttribute("imageURL", imageURL);
			ctx.getRequest().setAttribute("largeImageURL", largeImageURL);
			ctx.getRequest().setAttribute("imageDescription", StringHelper.toXMLAttribute(imageDescription));
			ctx.getRequest().setAttribute("text", StringHelper.escapeHTML(StringHelper.toXMLAttribute(text)));

			return ServletHelper.executeJSP(ctx, renderer);
		} else {
			String imageHTML = "";
			if (imageURL != null) {
				imageHTML = "<a href=\"" + largeImageURL + "\"><img src=\"" + imageURL + "\" alt=\"" + imageDescription + "\" /></a>";
			}
			return "<![CDATA[ " + imageHTML + text + "]]>";
		}
	}

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {

		try {
			Writer out = response.getWriter();

			String uri = request.getPathInfo();

			String[] uriSplited = uri.split("/");
			if (uriSplited.length < 4) {
				response.setContentType("text/xml");
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

					response.setContentType("text/xml");

					GlobalContext globalContext = GlobalContext.getInstance(request);
					ContentContext ctx = ContentContext.getFreeContentContext(request, response);
					if (template != null && !template.isTemplateInWebapp(ctx)) {
						StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
						template.importTemplateInWebapp(staticConfig, ctx);
					}

					ctx.setAbsoluteURL(true);
					ctx.setLanguage(lang);
					ContentService content = ContentService.getInstance(request);

//					/* tracking */
//					Tracker tracker = Tracker.getTracker(globalContext, request.getSession());
//					Track track = new Track(null, null, request.getRequestURI(), System.currentTimeMillis(), request.getHeader("Referer"), request.getHeader("User-Agent"));
//					track.setIP(request.getRemoteAddr());
//					track.setSessionId(request.getSession().getId());
//					tracker.addTrack(track);

					List<MenuElement> rssPages = content.getNavigation(ctx).getAllChildrenWithComponentType(ctx, RSSRegistration.TYPE);
					List<MenuElement> pages = new ArrayList<MenuElement>(rssPages);

					boolean autoSwitchLanguage = globalContext.isAutoSwitchToDefaultLanguage();
					if (request.getParameter("auto-switch-language") != null) {
						autoSwitchLanguage = StringHelper.isTrue(request.getParameter("auto-switch-language"));
					}

					Date latestDate = new Date(0);
					Iterator<MenuElement> iter = pages.iterator();
					while (iter.hasNext()) {
						MenuElement page = iter.next();
						ContentContext lgCtx = ctx;
						if (autoSwitchLanguage && !page.isRealContent(ctx)) {
							lgCtx = ctx.getContextWithContent(page);
							if (lgCtx == null) {
								lgCtx = ctx;
							}
						}
						if (!page.isRealContent(lgCtx)) {
							iter.remove();
							break;
						} else {
							ContentElementList compList = page.getAllContent(lgCtx);
							while (compList.hasNext(lgCtx)) {
								IContentVisualComponent comp = compList.next(lgCtx);
								if (comp instanceof RSSRegistration) {
									RSSRegistration rss = (RSSRegistration) comp;
									if (rss.isHideInvisible() && !page.isVisible()) {
										iter.remove();
										break;
									}
								}
							}
							if (page.getModificationDate(ctx).getTime() > latestDate.getTime()) {
								latestDate = page.getModificationDate(ctx);
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
					if (template != null && template.getRSSImageURL() != null) {
						out.write("<image>");
						out.write("<title>" + globalContext.getGlobalTitle() + " - " + channel + "</title>");
						out.write("<url>" + template.getRSSImageURL() + "</url>");
						out.write("</image>");
					}
					
					Collections.sort(pages, new ContentDateComparator(ctx, false));

					for (MenuElement page : pages) {
						ContentContext lgCtx = ctx;
						if (autoSwitchLanguage && !page.isRealContent(ctx)) {
							lgCtx = ctx.getContextWithContent(page);
							if (lgCtx == null) {
								lgCtx = ctx;
							}
						}
						if (page.isRealContent(lgCtx)) {
							ContentElementList contentList = page.getAllContent(lgCtx);
							List<String> pageChannel = new LinkedList<String>();
							boolean allChannel = false;
							boolean componentRSSFound = false;
							while (contentList.hasNext(lgCtx)) {
								IContentVisualComponent comp = contentList.next(lgCtx);
								if (comp.getType().equals(RSSRegistration.TYPE)) {
									componentRSSFound = true;
									if (comp.getValue(lgCtx).trim().length() == 0) {
										allChannel = true;
									} else {
										pageChannel.add(((RSSRegistration) comp).getChannel());
									}
								}
							}

							if (componentRSSFound) {
								if (allChannel || (pageChannel.contains(channel)) || (channel.equals("all"))) {
									out.write("<item>");
									out.write("<title><![CDATA[ " + page.getTitle(lgCtx) + "]]> </title>");

									String imageURL = null;
									String largeImageURL = null;
									String imageDescription = null;
									if (page.getImage(lgCtx) != null) {
										imageURL = URLHelper.createTransformURL(lgCtx, page, template, page.getImage(lgCtx).getResourceURL(lgCtx), "rss");
										largeImageURL = URLHelper.createTransformURL(lgCtx, page, template, page.getImage(lgCtx).getResourceURL(lgCtx), "thumb-view");
										imageDescription = page.getImage(lgCtx).getImageDescription(lgCtx);
									}
									out.write("<description>" + executeRSSCurrentRenderer(lgCtx, template, imageURL, largeImageURL, imageDescription, page.getDescriptionAsText(lgCtx)) + "</description>");
									if (template != null && template.isPDFRenderer()) {
										String pdfLink = URLHelper.createURL(lgCtx.getContextWithOtherFormat("pdf"), page.getPath());
										out.write("<enclosure url=\"" + pdfLink + "\" type=\"application/pdf\" />");
									}
									out.write("<authors>" + page.getCreator() + "</authors>");
									out.write("<pubDate>" + StringHelper.renderDateAsRFC822String(page.getContentDateNeverNull(lgCtx)) + "</pubDate>");
									out.write("<link><![CDATA[ " + URLHelper.createURL(lgCtx.getContextWithOtherFormat("html"), page.getPath()) + "]]> </link>");
									out.write("</item>");
								}
							}
						}
					}

					out.write("</channel>");
					out.write("</rss>");
				} else {
					response.setContentType("text/xml");
					out.write("<?xml version=\"1.0\" encoding=\"" + ContentContext.CHARACTER_ENCODING + "\"?>");
					out.write("<xml><error>format not found : " + format + "</xml>");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}