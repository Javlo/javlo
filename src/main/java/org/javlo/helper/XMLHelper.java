/*
 * Created on 02-fevr.-2004
 */
package org.javlo.helper;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.image.IImageTitle;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.taxonomy.TaxonomyBean;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.service.resource.Resource;
import org.javlo.service.visitors.CookiesService;
import org.javlo.servlet.AccessServlet;
import org.javlo.servlet.IVersion;
import org.javlo.ztatic.StaticInfo;
import org.owasp.encoder.Encode;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author pvandermaesen
 */
public class XMLHelper {

	public static String getPageXML(ContentContext ctx, MenuElement page, String lang) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		out.println("<?xml version=\"1.0\" encoding=\"" + ContentContext.CHARACTER_ENCODING + "\"?>");
		out.println("<export key=\"" + globalContext.getContextKey() + "\" path=\"" + page.getPath() + "\" version=\"" + IVersion.VERSION + "\">");
		Collection<MenuElement> allCreatePage = new LinkedList<MenuElement>();
		insertXMLPage(out, allCreatePage, Arrays.asList(new MenuElement[] { page }), true, lang);
		ContentContext absoluteURLCtx = new ContentContext(ctx);
		absoluteURLCtx.setAbsoluteURL(true);
		out.println("<resources url=\"" + URLHelper.createResourceURL(absoluteURLCtx, page, "/") + "\">");
		Set<Resource> allResources = new HashSet<Resource>();
		for (MenuElement menuElement : allCreatePage) {
			Collection<Resource> resources = menuElement.getAllResources(ctx);
			for (Resource resource : resources) {
				if (!allResources.contains(resource)) {
					allResources.add(resource);
					File file = new File(URLHelper.mergePath(globalContext.getDataFolder(), resource.getUri()));
					StaticInfo staticInfo = StaticInfo.getInstance(ctx, file);
					if (ctx.getCurrentEditUser() != null || staticInfo.getReadRoles(absoluteURLCtx).size() == 0) {
						staticInfo.toXML(ctx, out);
					}
					// out.println("<resource id=\"" + StringHelper.neverNull(resource.getId()) +
					// "\" uri=\"" + resource.getUri() + "\"/>");
				}
			}
		}
		out.println("</resources>");
		// ContentService content = ContentService.getInstance(ctx.getRequest());
		// insertMap(out, content.getGlobalMap(ctx),
		// PersistenceService.GLOBAL_MAP_NAME);
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
	public static SiteMapBloc getSiteMapBloc(ContentContext ctx, Collection<MenuElement> pages, int i, Calendar latestDate, boolean withResources) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Collection<String> lgs = globalContext.getContentLanguages();
		long sitemapMaxsize = ctx.getGlobalContext().getStaticConfig().getSiteMapSizeLimit();
		Date lastmod = new Date(0);
		for (MenuElement root : pages) {
			long size = 0;
			for (MenuElement element : root.getAllChildrenList()) {
				for (String lg : lgs) {
					StringBuffer line = new StringBuffer();
					ContentContext lgCtx = new ContentContext(ctx);
					lgCtx.setLanguage(lg);
					lgCtx.setContentLanguage(lg);
					lgCtx.setRequestContentLanguage(lg);
					lgCtx.setFormat("html");
					lgCtx.setPath(element.getPath());
					lgCtx.setAbsoluteURL(true);
					if (element.getFinalSeoWeight() != MenuElement.SEO_HEIGHT_NULL && !element.notInSearch(lgCtx) && element.isRealContent(lgCtx) && (latestDate == null || element.getModificationDate(ctx).after(latestDate.getTime()) || element.getContentDateNeverNull(lgCtx).after(latestDate.getTime()))) {
						line.append("<url>");
						line.append("<loc>" + Encode.forXmlAttribute(URLHelper.createURL(lgCtx)) + "</loc>");
						SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
						line.append("<lastmod>" + dataFormat.format(element.getModificationDate(ctx)) + "</lastmod>");
						String changefreq = "weekly";
						if (element.getDepth() > 1 && element.getFinalSeoWeight() == MenuElement.SEO_HEIGHT_LOW) {
							changefreq = "monthly";
						}
						if (element.isReference(lgCtx) || element.getFinalSeoWeight() == MenuElement.SEO_HEIGHT_HIGHT) {
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
								line.append("<xhtml:link rel=\"alternate\" hreflang=\"" + locLg + "\" href=\"" + Encode.forXmlAttribute(URLHelper.createURL(locLgCtx)) + "\" />");
							}
						}
						if (withResources) {
							for (IImageTitle image : element.getImages(lgCtx)) {
								if (image.isImageValid(lgCtx)) {
									line.append("<image:image>");
									String imageURL = image.getResourceURL(lgCtx);
									if (!StringHelper.isURL(imageURL)) {
										imageURL = URLHelper.createResourceURL(lgCtx, imageURL);
									}

									line.append("<image:loc>" + Encode.forXmlAttribute(imageURL) + "</image:loc>");
									if (!StringHelper.isEmpty(image.getImageDescription(lgCtx))) {
										line.append("<image:title>" + Encode.forXmlContent(image.getImageDescription(lgCtx)) + "</image:title>");
									}
									line.append("</image:image>");
								}
							}
						}
						line.append("</url>");
						size = size + line.toString().getBytes().length;
						if (i >= 0 && (size >= (i - 1) * sitemapMaxsize && size < i * sitemapMaxsize)) {
							out.println(line);
							if (element.getModificationDate(ctx).getTime() > lastmod.getTime()) {
								lastmod = element.getModificationDate(ctx);
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
			long size = 0;
			for (MenuElement element : root.getAllChildrenList()) {
				for (String lg : lgs) {
					StringBuffer line = new StringBuffer();
					ContentContext lgCtx = new ContentContext(ctx);
					lgCtx.setLanguage(lg);
					lgCtx.setContentLanguage(lg);
					lgCtx.setRequestContentLanguage(lg);
					lgCtx.setFormat("html");
					lgCtx.setPath(element.getPath());
					lgCtx.setAbsoluteURL(true);
					if (!element.notInSearch(lgCtx) && element.isRealContent(lgCtx) && (latestDate == null || element.getModificationDate(ctx).after(latestDate.getTime()) || element.getContentDateNeverNull(lgCtx).after(latestDate.getTime()))) {
						line.append("<url>");
						line.append("<loc>" + URLHelper.createURL(lgCtx) + "</loc>");
						line.append("<news:news>");
						line.append("<news:publication>");
						line.append("<news:name>" + element.getGlobalTitle(lgCtx) + "</news:name>");
						line.append("<news:language>" + lg + "</news:language>");
						line.append("</news:publication>");
						SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
						line.append("<news:publication_date>" + dataFormat.format(element.getModificationDate(ctx)) + "</news:publication_date>");
						line.append("<news:title>" + Encode.forXmlContent(element.getTitle(lgCtx)) + "</news:title>");
						if (!StringHelper.isEmpty(element.getKeywords(lgCtx))) {
							line.append("<news:keywords>" + Encode.forXmlContent(element.getKeywords(lgCtx)) + "</news:keywords>");
						}
						if (!StringHelper.isEmpty(element.getCategory(lgCtx))) {
							line.append("<news:genres>" + Encode.forXmlContent(element.getCategory(lgCtx)) + "</news:genres>");
						}
						for (IImageTitle image : element.getImages(lgCtx)) {
							if (image.isImageValid(lgCtx)) {
								line.append("<image:image>");
								String imageURL = image.getResourceURL(lgCtx);
								if (!StringHelper.isURL(imageURL)) {
									imageURL = URLHelper.createResourceURL(lgCtx, imageURL);
								}
								line.append("<image:loc>" + imageURL + "</image:loc>");
								line.append("<image:title>" + Encode.forXmlContent(image.getImageDescription(lgCtx)) + "</image:title>");
								line.append(" </image:image>");
							}
						}
						line.append("</news:news>");
						line.append("</url>");
						size = size + line.toString().getBytes().length;
						if (size >= (i - 1) * sitemapMaxsize && size < i * sitemapMaxsize) {
							out.println(line);
							if (element.getModificationDate(ctx).getTime() > lastmod.getTime()) {
								lastmod = element.getModificationDate(ctx);
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
		insertXMLPage(out, Arrays.asList(new MenuElement[] { menu }), null);
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

	/**
	 * insert content as XML in a writer
	 * 
	 * @param out
	 *            the writer
	 * @param page
	 *            page exported
	 * @param lang
	 *            language exported (if null >> all languages)
	 * @throws Exception
	 */
	static void insertXMLContent(PrintWriter out, MenuElement page, String lang) throws Exception {
		ComponentBean[] beans = page.getAllLocalContentBean();
		for (int j = 0; j < beans.length; j++) {
			if (beans[j] != null) {

				String language = beans[j].getLanguage();
				if (lang == null || lang.equalsIgnoreCase(language)) {

					String id = beans[j].getId();
					String type = beans[j].getType();
					String value = beans[j].getValue();
					/*
					 * if (value.startsWith("test renomage")) { System.out.println(
					 * "***** value = "+value); }
					 */
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
					String hidden = "" + beans[j].isHidden();
					if (StringHelper.isTrue(hidden)) {
						out.print("\" hidden=\"");
						out.print(hidden);
					}
					if (beans[j].getColumnSize() >= 0) {
						out.print("\" colSize=\"");
						out.print(beans[j].getColumnSize());
					}
					if (beans[j].getColumnStyle() != null) {
						out.print("\" colStyle=\"");
						out.print(beans[j].getColumnStyle());
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

					if (beans[j].getDeleteDate() != null) {
						out.print("\" delDate=\"");
						out.print(StringHelper.renderTime(beans[j].getDeleteDate()));
					}

					if (beans[j].getCookiesDisplayStatus() != CookiesService.ALWAYS_STATUS) {
						out.print("\" displayCookiesStatus=\"");
						out.print(beans[j].getCookiesDisplayStatus());
					}

					if (style != null && style.length() > 0) {
						out.print("\" style=\"");
						out.print(StringHelper.toXMLAttribute(style));
					}
					if (beans[j].getCssTemplate() != null) {
						out.print("\" csstpl=\"");
						out.print(StringHelper.toXMLAttribute(beans[j].getCssTemplate()));
					}
					if (beans[j].getBackgroundColor() != null && beans[j].getBackgroundColor().trim().length() > 0) {
						out.print("\" bgcol=\"");
						out.print(beans[j].getBackgroundColor());
					}
					if (beans[j].getManualCssClass() != null && beans[j].getManualCssClass().trim().length() > 0) {
						out.print("\" css=\"");
						out.print(beans[j].getManualCssClass());
					}
					if (beans[j].getTextColor() != null && beans[j].getTextColor().trim().length() > 0) {
						out.print("\" txtcol=\"");
						out.print(beans[j].getTextColor());
					}
					if (beans[j].getTextPosition() != null && beans[j].getTextPosition().trim().length() > 0) {
						out.print("\" txtpos=\"");
						out.print(beans[j].getTextPosition());
					}
					if (beans[j].isRepeat()) {
						out.print("\" repeat=\"true");
					}
//					if (StringHelper.isTrue(beans[j].isForceCachable())) {
//						out.print("\" forceCachable=\"true");
//					}

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
	}

	static void insertXMLPage(PrintWriter out, Collection<MenuElement> pageList, Collection<MenuElement> pages, boolean recu, String lang) throws Exception {

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
			if (creator.trim().length() > 0) {
				out.print("\" creator=\"");
				out.print(creator);
			}
			if (modificationDate != null && modificationDate.trim().length() > 0) {
				out.print("\" modificationDate=\"");
				out.print(modificationDate);
			}
			if (latestEditor.trim().length() > 0) {
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
			if (lang!=null) {
				out.print("\" contentLanguage=\"");
				out.print(lang);
			}
			if (!page.isPageActive()) {
				out.print("\" active=\"");
				out.print(page.isActive());
			}
			if (page.isAdmin()) {
				out.print("\" admin=\"");
				out.print(page.isAdmin());
			}
			if (page.isModel()) {
				out.print("\" model=\"");
				out.print(page.isModel());
			}
			if (!page.getType().equals(MenuElement.PAGE_TYPE_DEFAULT)) {
				out.print("\" type=\"");
				out.print(page.getType());
			}
			if (page.isBreakRepeat()) {
				out.print("\" breakrepeat=\"");
				out.print(page.isBreakRepeat());
			}
			if (page.getTaxonomy() != null && page.getTaxonomy().size() > 0) {
				out.print("\" taxonomy=\"");
				out.print(StringHelper.collectionToString(page.getTaxonomy()));
			}
			if (page.getIpSecurityErrorPageName() != null) {
				out.print("\" ipsecpagename=\"");
				out.print(page.getIpSecurityErrorPageName());
			}
			if (!StringHelper.isEmpty(page.getSavedParent())) {
				out.print("\" savedParent=\"");
				out.print(StringHelper.toXMLAttribute(page.getSavedParent()));
			}
			if (page.isChildrenAssociation()) {
				out.print("\" childrenAssociation=\"");
				out.print(page.isChildrenAssociation());
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
			if (!valid) {
				out.print("\" valid=\"");
				out.print(valid);
			}
			if (page.isNeedValidation()) {
				out.print("\" ndval=\"");
				out.print(page.isNeedValidation());
			}
			if (page.isNoValidation()) {
				out.print("\" noval=\"");
				out.print(page.isNoValidation());
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
			// if (page.getFollowers().size() > 0) {
			// out.print("\" followers=\"");
			// out.print(StringHelper.toXMLAttribute(StringHelper.collectionToString(page.getFollowers(),
			// "#")));
			// }

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

			if (page.getLinkedURL().trim().length() == 0) { // not save the remote content
				insertXMLContent(out, page, lang);
				if (recu) {
					insertXMLPage(out, pageList, page.getChildMenuElements(), true, lang);
				}
			}

			out.println("</page>");
		}
	}

	static void insertXMLPage(PrintWriter out, Collection<MenuElement> pages, String lang) throws Exception {
		insertXMLPage(out, null, pages, true, lang);
	}

	static void insertTaxonomy(PrintWriter out, TaxonomyBean node) throws Exception {
		String deco = "";
		if (!StringHelper.isEmpty(node.getDecoration())) {
			deco = " deco=\"" + Encode.forXmlAttribute(node.getDecoration()) + "\"";
		}
		out.println("<taxo name=\"" + Encode.forXmlAttribute(node.getName()) + "\" id=\"" + Encode.forXmlAttribute(node.getId()) + "\"" + deco + ">");
		for (Map.Entry<String, String> label : node.getLabels().entrySet()) {
			out.println("<label lang=\"" + Encode.forXmlAttribute(label.getKey()) + "\">" + Encode.forXmlContent(label.getValue()) + "</label>");
		}
		for (TaxonomyBean child : node.getChildren()) {
			insertTaxonomy(out, child);
		}
		out.println("</taxo>");
	}

	/**
	 * return the content in a XML structure
	 * 
	 * @param ctx
	 *            the contentContext
	 * @return A string with a xml in.
	 */
	public static void storeXMLContent(Writer inOut, MenuElement menu, int renderMode, int version, Map<String, String> contentMap, TaxonomyBean taxonomyRoot) throws Exception {
		PrintWriter out = new PrintWriter(inOut, true);
		out.println("<?xml version=\"1.0\" encoding=\"" + ContentContext.CHARACTER_ENCODING + "\"?>");
		out.println("<content cmsversion=\"" + AccessServlet.VERSION + "\" version=\"" + version + "\" date=\"" + PersistenceService.renderDate(new Date()) + "\">");
		insertXMLPage(out, Arrays.asList(new MenuElement[] { menu }), null);
		if (!PersistenceService.STORE_DATA_PROPERTIES) {
			insertMap(out, contentMap, PersistenceService.GLOBAL_MAP_NAME);
		}
		if (taxonomyRoot != null) {
			insertTaxonomy(out, taxonomyRoot);
		}
		out.println("</content>");
		out.close();
	}

	public static void main(String[] args) {
		System.out.println("##### XMLHelper.main : StringHelper.toXMLAttribute(name) = " + StringHelper.toHTMLAttribute("p&p global")); // TODO: remove debug trace
	}

}
