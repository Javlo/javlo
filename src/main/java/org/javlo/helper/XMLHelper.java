/*
 * Created on 02-fevr.-2004
 */
package org.javlo.helper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.image.IImageTitle;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.service.resource.Resource;
import org.javlo.servlet.AccessServlet;

/**
 * @author pvandermaesen
 */
public class XMLHelper {

	public static final class SiteMapBloc {
		private String text;
		private Date lastmod;

		public SiteMapBloc(String text, Date lastmod) {
			super();
			this.text = text;
			this.lastmod = lastmod;
		}

		public String getText() {
			return text;
		}

		public Date getLastmod() {
			return lastmod;
		}

	}

	public static String getPageXML(ContentContext ctx, MenuElement page) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		out.println("<?xml version=\"1.0\" encoding=\"" + ContentContext.CHARACTER_ENCODING + "\"?>");
		out.println("<export key=\"" + globalContext.getContextKey() + "\" path=\"" + page.getPath() + "\">");
		Collection<MenuElement> allCreatePage = new LinkedList<MenuElement>();
		insertXMLPage(out, allCreatePage, Arrays.asList(new MenuElement[] { page }), ctx.getContentLanguage(), true);
		ContentContext absoluteURLCtx = new ContentContext(ctx);
		absoluteURLCtx.setAbsoluteURL(true);
		out.println("<resources url=\"" + URLHelper.createResourceURL(absoluteURLCtx, page, "/") + "\">");
		Set<Resource> allResources = new HashSet<Resource>();
		for (MenuElement menuElement : allCreatePage) {
			Collection<Resource> resources = menuElement.getAllResources(ctx);
			for (Resource resource : resources) {
				if (!allResources.contains(resource)) {
					allResources.add(resource);
					out.println("<resource id=\"" + StringHelper.neverNull(resource.getId()) + "\" uri=\"" + resource.getUri() + "\"/>");
				}
			}
		}
		out.println("</resources>");
		ContentService content = ContentService.getInstance(ctx.getRequest());
		insertMap(out, content.getGlobalMap(ctx), PersistenceService.GLOBAL_MAP_NAME);
		out.println("</export>");

		out.close();
		return writer.toString();
	}

	/**
	 * create sitemap protocol 0.9 for structured ranking in google
	 * 
	 * @param root
	 *            the root element of the navigation
	 * @return a valid xml sitemap
	 * @throws Exception
	 */
	public static SiteMapBloc getSiteMapBloc(ContentContext ctx, Collection<MenuElement> pages, int i, Calendar latestDate) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Collection<String> lgs = globalContext.getContentLanguages();
		long sitemapMaxsize = ctx.getGlobalContext().getStaticConfig().getSiteMapSizeLimit();
		Date lastmod = new Date(0);
		for (MenuElement root : pages) {
			MenuElement[] children = root.getAllChildren();
			long size = 0;
			for (MenuElement element : children) {
				for (String lg : lgs) {
					StringBuffer line = new StringBuffer();
					ContentContext lgCtx = new ContentContext(ctx);
					lgCtx.setLanguage(lg);
					lgCtx.setContentLanguage(lg);
					lgCtx.setRequestContentLanguage(lg);
					lgCtx.setFormat("html");
					lgCtx.setPath(element.getPath());
					lgCtx.setAbsoluteURL(true);
					if (!element.notInSearch(lgCtx) && element.isRealContent(lgCtx) && (latestDate == null || element.getModificationDate().after(latestDate.getTime()) || element.getContentDateNeverNull(lgCtx).after(latestDate.getTime()))) {
						line.append("<url>");
						line.append("<loc>" + URLHelper.createURL(lgCtx) + "</loc>");
						SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
						line.append("<lastmod>" + dataFormat.format(element.getModificationDate()) + "</lastmod>");
						String changefreq = "weekly";
						if (element.getDepth() > 1 && element.getSeoWeight() == MenuElement.SEO_HEIGHT_LOW) {
							changefreq = "monthly";
						}
						if (element.isReference(lgCtx) || element.getSeoWeight() == MenuElement.SEO_HEIGHT_HIGHT) {
							changefreq = "daily";
						}
						line.append("<changefreq>" + changefreq + "</changefreq>");
						line.append("<priority>" + element.getSiteMapPriority(lgCtx) + "</priority>");

						for (String locLg : lgs) {
							ContentContext locLgCtx = new ContentContext(lgCtx);
							locLgCtx.setLanguage(locLg);
							locLgCtx.setContentLanguage(locLg);
							locLgCtx.setRequestContentLanguage(locLg);
							locLgCtx.setFormat("html");
							locLgCtx.setAbsoluteURL(true);
							if (element.isRealContent(locLgCtx)) {
								line.append("<xhtml:link rel=\"alternate\" hreflang=\"" + locLg + "\" href=\"" + URLHelper.createURL(locLgCtx) + "\" />");
							}
						}
						for (IImageTitle image : element.getImages(lgCtx)) {
							if (image.isImageValid(lgCtx)) {
								line.append("<image:image>");
								String imageURL = image.getResourceURL(lgCtx);
								if (!StringHelper.isURL(imageURL)) {
									imageURL = URLHelper.createResourceURL(lgCtx, imageURL);
								}
								
								line.append("<image:loc>" + imageURL + "</image:loc>");
								if (!StringHelper.isEmpty(image.getImageDescription(lgCtx))) {
									line.append("<image:title>" + image.getImageDescription(lgCtx) + "</image:title>");
								}
								line.append("</image:image>");
							}
						}
						line.append("</url>");
						size = size + line.toString().getBytes().length;
						if (size >= (i - 1) * sitemapMaxsize && size < i * sitemapMaxsize) {
							out.println(line);
							if (element.getModificationDate().getTime() > lastmod.getTime()) {
								lastmod = element.getModificationDate();
							}
						}
					}
				}
			}
		}
		out.close();
		return new SiteMapBloc(writer.toString(), lastmod);
	}

	/**
	 * create sitemap protocol 0.9 for structured ranking in google
	 * 
	 * @param root
	 *            the root element of the navigation
	 * @return a valid xml sitemap
	 * @throws Exception
	 */
	public static SiteMapBloc getSiteMapNewsBloc(ContentContext ctx, Collection<MenuElement> pages, int i, Calendar latestDate) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Collection<String> lgs = globalContext.getContentLanguages();
		long sitemapMaxsize = ctx.getGlobalContext().getStaticConfig().getSiteMapSizeLimit();
		Date lastmod = new Date(0);
		for (MenuElement root : pages) {
			MenuElement[] children = root.getAllChildren();
			long size = 0;
			for (MenuElement element : children) {
				for (String lg : lgs) {
					StringBuffer line = new StringBuffer();
					ContentContext lgCtx = new ContentContext(ctx);
					lgCtx.setLanguage(lg);
					lgCtx.setContentLanguage(lg);
					lgCtx.setRequestContentLanguage(lg);
					lgCtx.setFormat("html");
					lgCtx.setPath(element.getPath());
					lgCtx.setAbsoluteURL(true);
					if (!element.notInSearch(lgCtx) && element.isRealContent(lgCtx) && (latestDate == null || element.getModificationDate().after(latestDate.getTime()) || element.getContentDateNeverNull(lgCtx).after(latestDate.getTime()))) {
						line.append("<url>");
						line.append("<loc>" + URLHelper.createURL(lgCtx) + "</loc>");
						line.append("<news:news>");
						line.append("<news:publication>");
						line.append("<news:name>" + element.getGlobalTitle(lgCtx) + "</news:name>");
						line.append("<news:language>" + lg + "</news:language>");
						line.append("</news:publication>");
						SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
						line.append("<news:publication_date>" + dataFormat.format(element.getModificationDate()) + "</news:publication_date>");
						line.append("<news:title>" + element.getTitle(lgCtx) + "</news:title>");
						if (!StringHelper.isEmpty(element.getKeywords(lgCtx))) {
							line.append("<news:keywords>" + element.getKeywords(lgCtx) + "</news:keywords>");
						}
						if (!StringHelper.isEmpty(element.getCategory(lgCtx))) {
							line.append("<news:genres>" + element.getCategory(lgCtx) + "</news:genres>");
						}
						for (IImageTitle image : element.getImages(lgCtx)) {
							if (image.isImageValid(lgCtx)) {
								line.append("<image:image>");
								String imageURL = image.getResourceURL(lgCtx);
								if (!StringHelper.isURL(imageURL)) {
									imageURL = URLHelper.createResourceURL(lgCtx, imageURL);
								}
								line.append("<image:loc>" + imageURL + "</image:loc>");
								line.append("<image:title>" + image.getImageDescription(lgCtx) + "</image:title>");
								line.append(" </image:image>");
							}
						}
						line.append("</news:news>");
						line.append("</url>");
						size = size + line.toString().getBytes().length;
						if (size >= (i - 1) * sitemapMaxsize && size < i * sitemapMaxsize) {
							out.println(line);
							if (element.getModificationDate().getTime() > lastmod.getTime()) {
								lastmod = element.getModificationDate();
							}
						}
					}
				}
			}
		}
		out.close();
		return new SiteMapBloc(writer.toString(), lastmod);
	}

	public static String getXMLContent(ContentContext ctx, int version) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String defaultLg = globalContext.getDefaultLanguages().iterator().next();
		if (!globalContext.getLanguages().contains(defaultLg)) {
			defaultLg = null;
		}
		MenuElement menu = content.getNavigation(ctx);
		return getXMLContent(menu, ctx.getRenderMode(), version, defaultLg);
	}

	/**
	 * return the content in a XML structure
	 * 
	 * @param ctx
	 *            the contentContext
	 * @return A string with a xml in.
	 */
	public static String getXMLContent(MenuElement menu, int renderMode, int version, String defaultLg) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		out.println("<?xml version=\"1.0\" encoding=\"" + ContentContext.CHARACTER_ENCODING + "\"?>");
		if (renderMode == ContentContext.VIEW_MODE) {
			out.println("<content version=\"" + version + "\">");
		} else {
			out.println("<content>");
		}
		insertXMLPage(out, Arrays.asList(new MenuElement[] { menu }), defaultLg);
		out.println("</content>");
		out.close();

		return writer.toString();
	}

	private static void insertMap(PrintWriter out, Map<String, String> contentMap, String name) {
		if (contentMap != null && contentMap.size() > 0) {
			out.println("<properties name=\"" + name + "\">");
			for (String key : contentMap.keySet()) {
				if (contentMap.get(key) != null && contentMap.get(key).length() > 0) {
					out.print("<property key=\"" + StringHelper.toXMLAttribute(key) + "\">");
					out.print("<![CDATA[");
					out.print(contentMap.get(key));
					out.print("]]>");
					out.println("</property>");
				}
			}
			out.println("</properties>");
		}
	}

	static void insertXMLContent(PrintWriter out, MenuElement page, String defaultLg) throws Exception {
		ComponentBean[] beans = page.getAllLocalContentBean();
		for (int j = 0; j < beans.length; j++) {
			if (beans[j] != null) {
				String id = beans[j].getId();
				String type = beans[j].getType();
				String language = beans[j].getLanguage();
				String value = beans[j].getValue();
				/*
				 * if (value.startsWith("test renomage")) { System.out.println(
				 * "***** value = "+value); }
				 */
				String repeat = "" + beans[j].isRepeat();
				boolean nolink = beans[j].isNolink();
				String style = beans[j].getStyle();
				String inlist = "" + beans[j].isList();

				out.print("<component id=\"");
				out.print(id);
				out.print("\" type=\"");
				out.print(type);
				if (StringHelper.isTrue(inlist)) {
					out.print("\" inlist=\"");
					out.print(inlist);
				}
				if (!ComponentBean.DEFAULT_AREA.equals(beans[j].getArea())) {
					out.print("\" area=\"");
					out.print(beans[j].getArea());
				}

				out.print("\" language=\"");
				out.print(language);

				out.print("\" authors=\"");
				out.print(StringHelper.neverNull(beans[j].getAuthors()));

				out.print("\" creationDate=\"");
				out.print(StringHelper.renderTime(beans[j].getCreationDate()));

				out.print("\" modificationDate=\"");
				out.print(StringHelper.renderTime(beans[j].getModificationDate()));

				if (style != null && style.length() > 0) {
					out.print("\" style=\"");
					out.print(StringHelper.toXMLAttribute(style));
				}
				if (beans[j].getBackgroundColor() != null && beans[j].getBackgroundColor().trim().length() > 0) {
					out.print("\" bgcol=\"");
					out.print(beans[j].getBackgroundColor());
				}
				if (beans[j].getTextColor() != null && beans[j].getTextColor().trim().length() > 0) {
					out.print("\" txtcol=\"");
					out.print(beans[j].getTextColor());
				}
				if (StringHelper.isTrue(repeat)) {
					out.print("\" repeat=\"");
					out.print(repeat);
				}
				if (nolink) {
					out.print("\" nolink=\"");
					out.print(nolink);
				}
				if (beans[j].getRenderer() != null) {
					out.print("\" renderer=\"");
					out.print(beans[j].getRenderer());
				}
				if (beans[j].getLayout() != null) {
					out.print("\" layout=\"");
					out.print(beans[j].getLayout().getLayout());
				}
				if (beans[j].getHiddenModes() != null && !beans[j].getHiddenModes().isEmpty()) {
					out.print("\" hiddenModes=\"");
					out.print(StringHelper.collectionToString(beans[j].getHiddenModes(), ","));
				}
				beans[j].setModify(false);
				out.print("\" >");
				out.print("<![CDATA[");
				out.print(value);
				out.print("]]>");
				out.println("</component>");
			}
		}
	}

	static void insertXMLPage(PrintWriter out, Collection<MenuElement> pageList, Collection<MenuElement> pages, String defaultLg, boolean recu) throws Exception {

		for (MenuElement page : pages) {
			if (pageList != null) {
				pageList.add(page);
			}
			String id = page.getId();
			String name = page.getHumanName();
			int priority = page.getPriority();
			Collection<String> roles = page.getUserRoles();
			String template = "";
			if (page.getTemplateId() != null) {
				template = page.getTemplateId();
			}
			StringBuffer rolesRaw = new StringBuffer();
			String creationDate = PersistenceService.renderDate(page.getCreationDate());
			String modificationDate = PersistenceService.renderDate(page.getModificationDate());
			String validationDate = "";
			if (page.getValidationDate() != null) {
				validationDate = PersistenceService.renderDate(page.getValidationDate());
			}
			String creator = page.getCreator();
			if (creator == null) {
				creator = "";
			}
			String latestEditor = page.getLatestEditor();
			if (latestEditor == null) {
				latestEditor = "";
			}
			boolean valid = page.isValid();
			String validater = page.getValidater();
			String reversedLink = page.getReversedLink();

			String sep = "";
			for (String role : roles) {
				rolesRaw.append(sep);
				rolesRaw.append(role);
				sep = ";";
			}
			boolean visible = page.isVisible();
			out.print("<page id=\"");
			out.print(id);
			out.print("\" name=\"");
			out.print(StringHelper.toXMLAttribute(name));
			out.print("\" creationDate=\"");
			out.print(creationDate);
			if (creator != null && creator.trim().length() > 0) {
				out.print("\" creator=\"");
				out.print(creator);
			}
			if (modificationDate != null && modificationDate.trim().length() > 0) {
				out.print("\" modificationDate=\"");
				out.print(modificationDate);
			}
			if (latestEditor != null && latestEditor.trim().length() > 0) {
				out.print("\" latestEditor=\"");
				out.print(latestEditor);
			}
			out.print("\" priority=\"");
			out.print(priority);
			if (template != null && template.length() > 0) {
				out.print("\" layout=\"");
				out.print(template);
			}
			if (visible) {
				out.print("\" visible=\"");
				out.print(visible);
			}
			if (!page.isActive()) {
				out.print("\" active=\"");
				out.print(page.isActive());
			}
			if (!page.getType().equals(MenuElement.PAGE_TYPE_DEFAULT)) {
				out.print("\" type=\"");
				out.print(page.getType());
			}
			if (page.isBreakRepeat()) {
				out.print("\" breakrepeat=\"");
				out.print(page.isBreakRepeat());
			}
			if (!StringHelper.isEmpty(page.getSavedParent())) {
				out.print("\" savedParent=\"");
				out.print(StringHelper.toXMLAttribute(page.getSavedParent()));
			}
			if (page.isChildrenAssociation()) {
				out.print("\" childrenAssociation=\"");
				out.print(page.isChildrenAssociation());
			}
			if (page.isChangeNotification()) {
				out.print("\" changeNotification=\"");
				out.print(page.isChangeNotification());
			}
			if (page.getSharedName() != null && page.getSharedName().trim().length() > 0) {
				out.print("\" sharedName=\"");
				out.print(page.getSharedName());
			}
			if (page.getSeoWeight() != MenuElement.SEO_HEIGHT_INHERITED) {
				out.print("\" seoWeight=\"");
				out.print(page.getSeoWeight());
			}
			if (page.isHttps())
				if (validationDate != null && validationDate.trim().length() > 0) {
					out.print("\" https=\"");
					out.print(page.isHttps());
				}
			if (validater != null && validater.trim().length() > 0) {
				out.print("\" validater=\"");
				out.print(validater);
			}
			if (valid) {
				out.print("\" valid=\"");
				out.print(valid);
			}
			if (reversedLink.trim().length() > 0) {
				out.print("\" reversed-link=\"");
				out.print(StringHelper.toXMLAttribute(StringHelper.arrayToString(StringHelper.readLines(reversedLink), "#")));
			}
			if (page.getLinkedURL().trim().length() > 0) {
				out.print("\" linked-url=\"");
				out.print(StringHelper.toXMLAttribute(page.getLinkedURL()));
			}
			if (page.getStartPublishDate() != null) {
				out.print("\" start-publish=\"");
				out.print(StringHelper.renderSortableTime(page.getStartPublishDate()));
			}
			if (page.getEndPublishDate() != null) {
				out.print("\" end-publish=\"");
				out.print(StringHelper.renderSortableTime(page.getEndPublishDate()));
			}
			if (page.getEditorRoles().size() > 0) {
				out.print("\" editor-roles=\"");
				out.print(StringHelper.toXMLAttribute(StringHelper.collectionToString(page.getEditorRoles(), "#")));
			}

			/* vparent */
			List<String> parentId = new LinkedList<String>();
			for (MenuElement elem : page.getVirtualParent()) {
				parentId.add(elem.getId());
			}
			if (page.getVirtualParent().size() > 0) {
				out.print("\" vparent=\"");
				out.print(StringHelper.toXMLAttribute(StringHelper.collectionToString(parentId)));
			}
			if (page.isShortURL()) {
				out.print("\" shorturl=\"");
				out.print(StringHelper.toXMLAttribute(page.getShortURL()));
			}

			if (rolesRaw.toString().trim().length() > 0) {
				out.print("\" userRoles=\"");
				out.print(StringHelper.toXMLAttribute(rolesRaw.toString()));
			}
			out.println("\">");

			if (page.getLinkedURL().trim().length() == 0) { // not save the
															// remote content
				insertXMLContent(out, page, defaultLg);
				if (recu) {
					insertXMLPage(out, pageList, page.getChildMenuElements(), defaultLg, true);
				}
			}

			out.println("</page>");
		}
	}

	static void insertXMLPage(PrintWriter out, Collection<MenuElement> pages, String defaultLg) throws Exception {
		insertXMLPage(out, null, pages, defaultLg, true);
	}

	/**
	 * return the content in a XML structure
	 * 
	 * @param ctx
	 *            the contentContext
	 * @return A string with a xml in.
	 */
	public static void storeXMLContent(Writer inOut, MenuElement menu, int renderMode, int version, String defaultLg, Map<String, String> contentMap) throws Exception {
		PrintWriter out = new PrintWriter(inOut, true);

		out.println("<?xml version=\"1.0\" encoding=\"" + ContentContext.CHARACTER_ENCODING + "\"?>");
		out.println("<content cmsversion=\"" + AccessServlet.VERSION + "\" version=\"" + version + "\" date=\"" + PersistenceService.renderDate(new Date()) + "\">");
		insertXMLPage(out, Arrays.asList(new MenuElement[] { menu }), defaultLg);
		insertMap(out, contentMap, PersistenceService.GLOBAL_MAP_NAME);
		out.println("</content>");
		out.close();
	}

}
