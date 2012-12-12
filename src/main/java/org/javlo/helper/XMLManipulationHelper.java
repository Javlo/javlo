package org.javlo.helper;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.io.FileUtils;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.template.TemplatePlugin;

public class XMLManipulationHelper {

	public static class BadXMLException extends Exception {

		private int localisation = -1;

		private String tag = "";

		private String text = "";

		public BadXMLException(String msg) {
			super(msg);
		}

		public BadXMLException(String msg, int i, String tag, String text) {
			super(msg);
			localisation = i;
			this.tag = tag;
			this.text = text;
		}

		public int getLocalisation() {
			return localisation;
		}

		public String getTag() {
			return tag;
		}

		public String getText() {
			return text;
		}

	}

	public static class TagComparatorOnEndTag implements Comparator<TagDescription> {

		@Override
		public int compare(TagDescription tag1, TagDescription tag2) {
			return tag2.getCloseStart() - tag1.getCloseStart();
		}

	}

	public static class TagComparatorOnStartTag implements Comparator<TagDescription> {

		@Override
		public int compare(TagDescription tag1, TagDescription tag2) {
			return tag2.getOpenEnd() - tag1.getOpenEnd();
		}

	}

	/*
	 * private static String getGoogleAnalyticsCode() throws IOException {
	 * 
	 * StringWriter outString = new StringWriter(); BufferedWriter out = new BufferedWriter(outString);out.append( "<%if ((globalContext.getGoogleAnalyticsUACCT().trim().length() > 3)&&(ctx.getRenderMode() == ContentContext.VIEW_MODE)) {%>" );out.append( " src=\"http://www.google-analytics.com/urchin.js\" type=\"text/javascript\">" ); out.newLine(); out.append("</script>"); out.newLine(); out.append("<script type=\"text/javascript\">"); out.newLine(); out.append("_uacct = \"<%=globalContext.getGoogleAnalyticsUACCT()%>\";"); out.newLine(); out.append("urchinTracker();"); out.newLine(); out.append("</script>"); out.newLine(); out.append("<%}%>"); out.newLine(); out.close();
	 * 
	 * return outString.toString(); }
	 */

	public static class TagDescription {

		private String name;

		private int openStart = -1;

		private int openEnd = -1;

		private int closeStart = -1;

		private int closeEnd = -1;

		private final Map<String, String> attributes = new HashMap<String, String>();

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			TagDescription otherTag = (TagDescription) obj;
			return getOpenStart() == otherTag.getOpenStart();
		}

		public Map<String, String> getAttributes() {
			return attributes;
		}

		public int getCloseEnd() {
			return closeEnd;
		}

		public int getCloseStart() {
			return closeStart;
		}

		public String getInside(String content) {
			if (getOpenEnd() + 1 < getCloseStart()) {
				return content.substring(getOpenEnd() + 1, getCloseStart());
			} else {
				return "";
			}
		}

		public String getName() {
			return name;
		}

		public int getOpenEnd() {
			return openEnd;
		}

		public int getOpenStart() {
			return openStart;
		}

		public boolean isAutoClose() {
			return !(getCloseStart() > getOpenEnd());
		}

		/**
		 * return the tag rendered in HTML
		 * 
		 * @param inside
		 *            the content (null for autoclose tag)
		 * @return a HTML code with the tag
		 */
		public String render(String inside) {
			StringBuffer outTag = new StringBuffer();
			outTag.append("<" + getName());
			Collection<Map.Entry<String, String>> entries = getAttributes().entrySet();
			for (Map.Entry<String, String> entry : entries) {
				outTag.append(" ");
				outTag.append(entry.getKey());
				outTag.append("=");
				outTag.append("\"");
				outTag.append(entry.getValue());
				outTag.append("\"");
			}
			if (inside == null) {
				outTag.append(" />");
			} else {
				outTag.append(">");
				outTag.append(inside);
				outTag.append("</" + getName() + ">");
			}
			return outTag.toString();
		}

		public void setCloseEnd(int closeEnd) {
			this.closeEnd = closeEnd;
		}

		public void setCloseStart(int closeStart) {
			this.closeStart = closeStart;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setOpenEnd(int openEnd) {
			this.openEnd = openEnd;
		}

		public void setOpenStart(int openStart) {
			this.openStart = openStart;
		}

		@Override
		public String toString() {
			StringBuffer out = new StringBuffer();
			out.append("<" + getName());
			for (String key : attributes.keySet()) {
				out.append(" ");
				out.append(key);
				out.append("=\"");
				out.append(attributes.get(key));
				out.append("\"");
			}
			if (getOpenEnd() == getCloseEnd()) { // auto close tag
				out.append(" />");
			} else {
				out.append(">");
			}
			return out.toString();
		}

	}

	public static final String AREA_PREFIX = "area.";

	public static final String AREA_VIEW_PREFIX = "area-view-if-empty.";

	public static final String HEADER_ZONE = "<!-- INSERT HEADER HERE -->";

	public static String changeLink(String html, String linkPrefix) throws BadXMLException {
		TagDescription[] tags = searchAllTag(html, false);
		StringRemplacementHelper remplacement = new StringRemplacementHelper();
		for (TagDescription tag : tags) {
			if (tag.getAttributes().get("src") != null) {
				if (!tag.getAttributes().get("src").contains(":")) {
					tag.getAttributes().put("src", URLHelper.mergePath(linkPrefix, tag.getAttributes().get("src")));
				}
				remplacement.addReplacement(tag.getOpenStart(), tag.getOpenEnd() + 1, tag.toString());
			}
			if (tag.getAttributes().get("href") != null && !tag.getAttributes().get("href").startsWith("#")) {
				if (!tag.getAttributes().get("href").contains(":")) {
					tag.getAttributes().put("href", URLHelper.mergePath(linkPrefix, tag.getAttributes().get("href")));
				}
				remplacement.addReplacement(tag.getOpenStart(), tag.getOpenEnd() + 1, tag.toString());
			}
		}
		return remplacement.start(html);
	}

	/**
	 * convert a generic html file to a jsp template file for wcms
	 * 
	 * @param htmlFile
	 *            the source file
	 * @param jspFile
	 *            if jsp null no file is create -> just for test the HTML structure
	 * @param options
	 * @param areas
	 * @param resources
	 * @param messages
	 *            contains the message for the final user
	 * @partam ids, fill the list with html is found on convertion
	 * @param isMail
	 * @return
	 * @throws IOException
	 * @throws BadXMLException
	 */
	private static int convertHTMLtoJSP(GlobalContext globalContext, I18nAccess i18nAccess, File htmlFile, File jspFile, Map<String, String> options, List<String> areas, List<String> resources, List<TemplatePlugin> templatePlugins, List<GenericMessage> messages, List<String> ids, boolean isMail) throws IOException {

		String templateVersion = StringHelper.getRandomId();

		if (resources == null) {
			resources = new LinkedList<String>();
		}
		if (messages == null) {
			messages = new LinkedList<GenericMessage>();
		}

		int depth = 0;
		try {

			if (jspFile != null) {
				if (!jspFile.exists()) {
					jspFile.createNewFile();
				}
			}
			// StringBuffer contentBuffered = new StringBuffer();
			//
			// InputStream in = new FileInputStream(htmlFile);
			// try {
			// int read = in.read();
			// while (read >= 0) {
			// contentBuffered.append((char) read);
			// read = in.read();
			// }
			// } finally {
			// ResourceHelper.closeResource(in);
			// }
			// String content = contentBuffered.toString();

			String content = FileUtils.readFileToString(htmlFile, ContentContext.CHARACTER_ENCODING);
			TagDescription[] tags = new TagDescription[0];

			tags = searchAllTag(content, true);
			// displayResult(tags, content, System.out);

			StringRemplacementHelper remplacement = new StringRemplacementHelper();
			for (int i = 0; i < tags.length; i++) {
				Map<String, String> attributes = tags[i].getAttributes();
				String idValue = attributes.get("id");
				if (ids != null && idValue != null) {
					ids.add(idValue);
				}

				/* remove description and keywords from template */
				if (!isMail) {
					if (tags[i].getName().equalsIgnoreCase("meta")) {
						if ((tags[i].getAttributes().get("name") != null) && (tags[i].getAttributes().get("name").equalsIgnoreCase("description"))) {
							remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getCloseEnd() + 1, "");
						} else if ((tags[i].getAttributes().get("name") != null) && (tags[i].getAttributes().get("name").equalsIgnoreCase("keywords"))) {
							remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getCloseEnd() + 1, "");
						}
					}
				}

				/* area */
				// if (!isMail) {
				for (String area : areas) {
					String areaValue = getValue(options, AREA_PREFIX + area, area);
					boolean displayIfEmpty = StringHelper.isTrue(getValue(options, AREA_VIEW_PREFIX + area, "true"));
					String prefix = "";
					String sufix = "";
					if (!displayIfEmpty) {
						prefix = "<%if (!currentPage.isEmpty(ctx, \"" + area + "\")) {%>";
						sufix = "<%}%>";
					}
					if ((idValue != null) && (idValue.trim().equals(areaValue))) {
						remplacement.addReplacement(tags[i].getOpenStart() - 1, tags[i].getOpenStart() - 1, prefix);
						remplacement.addReplacement(tags[i].getOpenEnd() + 1, tags[i].getCloseStart(), "<jsp:include page=\"/jsp/view/content_view.jsp?area=" + area + "\" />");
						remplacement.addReplacement(tags[i].getCloseEnd() + 1, tags[i].getCloseEnd() + 1, sufix);
					}
				}
				// }

				/* languages */
				if (!isMail) {
					if ((idValue != null) && (idValue.trim().equals(getValue(options, "tagid.language", "language")))) {
						boolean list = StringHelper.isTrue(getValue(options, "tagid.language.list", "true"));
						boolean languageJS = StringHelper.isTrue(getValue(options, "tagid.language.auto-change", "true"));
						if (list) {
							remplacement.addReplacement(tags[i].getOpenEnd() + 1, tags[i].getCloseStart(), "<%=XHTMLHelper.renderLanguage(ctx)%>");
						} else {
							String selectID = getValue(options, "tagid.language.select-id", "select_language");
							String selectInputID = getValue(options, "tagid.language.select-input-id", "select_language_submit");
							remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getOpenStart(), "<%if (globalContext.getVisibleLanguages().size() > 1) {%>");
							boolean renderForm = !tags[i].getName().equalsIgnoreCase("form");
							remplacement.addReplacement(tags[i].getOpenEnd() + 1, tags[i].getCloseStart(), "<%=XHTMLHelper.renderSelectLanguage(ctx, " + languageJS + ", \"" + selectID + "\",  \"" + selectInputID + "\", " + renderForm + ")%>");
							remplacement.addReplacement(tags[i].getCloseEnd() + 1, tags[i].getCloseEnd() + 1, "<%}%>");

						}
					}
				}

				/* content language */
				if (!isMail) {
					if ((idValue != null) && (idValue.trim().equals(getValue(options, "tagid.content-language", "content-language")))) {
						boolean list = StringHelper.isTrue(getValue(options, "tagid.content-language.list", "true"));
						boolean languageJS = StringHelper.isTrue(getValue(options, "tagid.content-language.auto-change", "true"));
						if (list) {
							remplacement.addReplacement(tags[i].getOpenEnd() + 1, tags[i].getCloseStart(), "<%=XHTMLHelper.renderContentLanguage(ctx)%>");
						} else {
							remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getOpenStart(), "<%if (globalContext.getContentLanguage().size() > 1) {%>");
							remplacement.addReplacement(tags[i].getOpenEnd() + 1, tags[i].getCloseStart(), "<%=XHTMLHelper.renderSelectLanguage(ctx, " + languageJS + ")%>");
							remplacement.addReplacement(tags[i].getCloseEnd() + 1, tags[i].getCloseEnd() + 1, "<%}%>");

						}
					}
				}

				/* breadcrumb */// TODO: not language but breadcrumb ???
				// if (!isMail) {
				if ((idValue != null) && (idValue.trim().equals(getValue(options, "tagid.breadcrumb", "breadcrumb")))) {
					remplacement.addReplacement(tags[i].getOpenEnd() + 1, tags[i].getCloseStart(), "<%=XHTMLNavigationHelper.getBreadcrumbList(ctx)%>");
				}
				// }

				// if (!isMail) {
				Collection<String> keys = options.keySet();
				for (String key : keys) {
					if ((key != null) && key.startsWith("tagid.menu-")) {
						int from = 0;
						int to = 999;
						for (int s = 0; s < 99; s++) {
							for (int e = s; e < 99; e++) {
								if (key.startsWith("tagid.menu-" + s + '_' + e)) {
									from = s;
									to = e;
								}
							}
						}
						boolean extended = false;
						if (key.contains("-extended-")) {
							extended = true;
						}

						String pageName = key.substring(key.lastIndexOf('-') + 1);
						if ((idValue != null) && (idValue.trim().equals(getValue(options, key, "")))) {
							StringBuffer stringBuffer = new StringBuffer();
							stringBuffer.append("<%MenuElement headerNode" + pageName + " = content.getNavigation(ctx).searchChildFromName(\"" + pageName + "\");");
							stringBuffer.append("%><%=XHTMLNavigationHelper.renderMenu(ctx, headerNode" + pageName + ", " + from + ", " + to + ", " + extended + ")%>");
							remplacement.addReplacement(tags[i].getOpenEnd() + 1, tags[i].getCloseStart(), stringBuffer.toString());
						}
					}
					// }
				}

				if (!isMail) {
					for (int s = 0; s < 99; s++) {
						for (int e = s; e < 99; e++) {
							if ((idValue != null) && (idValue.trim().equals(getValue(options, "tagid.menu_" + s + "_" + e, "menu_" + s + "_" + e)))) {
								remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getOpenStart(), "<%if ( XHTMLNavigationHelper.menuExist(ctx," + (s + 1) + ") ) {%>");
								remplacement.addReplacement(tags[i].getOpenEnd() + 1, tags[i].getCloseStart(), "<%=XHTMLNavigationHelper.renderMenu(ctx, " + s + ", " + e + ", globalContext.isExtendMenu())%>");
								remplacement.addReplacement(tags[i].getCloseEnd() + 1, tags[i].getCloseEnd() + 1, "<%}%>");
								if (e > depth) { // depth of the template
									depth = e;
								}
							}
						}
					}
				}

				/* insert before all */
				if (!isMail) {
					if (tags[i].getName().equalsIgnoreCase("body")) {
						String contentZone = getValue(options, AREA_PREFIX + "content", null);
						if (contentZone != null) {
							remplacement.addReplacement(tags[i].getOpenEnd() + 1, tags[i].getOpenEnd() + 1, getPreviewCode() + getEscapeMenu(contentZone) + getResetTemplate() + getAfterBodyCode());
						}
					}
				}

				/* link - StyleSheet */
				if (tags[i].getName().equalsIgnoreCase("link")) {
					String hrefValue = attributes.get("href");

					if ((hrefValue != null) && (!StringHelper.isURL(hrefValue))) {
						String newLinkGeneratorIf = "<%if (!XHTMLHelper.allReadyInsered(ctx, \"" + hrefValue + "\")) {%>";
						resources.add(hrefValue);
						attributes.put("href", "<%=URLHelper.createStaticTemplateURL(ctx,\"/" + hrefValue + "\", \"" + templateVersion + "\")%>");
						remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getOpenEnd() + 1, newLinkGeneratorIf + tags[i].toString() + "<%}%>");
					}
				}

				/* form action */
				if (tags[i].getName().equalsIgnoreCase("form")) {
					String actionValue = attributes.get("action");
					if ((actionValue != null) && (!StringHelper.isURL(actionValue))) {
						attributes.put("action", "<%=URLHelper.createURL(ctx,\"" + actionValue + "\")%>");
						remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getOpenEnd() + 1, tags[i].toString());
					}
				}

				/* search form */
				if (tags[i].getName().equalsIgnoreCase("form")) {
					if ((tags[i].getAttributes().get("id") != null) && (tags[i].getAttributes().get("id").equals(options.get("tagid.form.search")))) {
						String searchInput = "<input type=\"hidden\" name=\"webaction\" value=\"search.search\" />";

						List<TagDescription> children = searchChildren(tags, tags[i]);
						for (TagDescription tagDescription : children) {
							if (tagDescription.getName().equalsIgnoreCase("input")) {
								if ((tagDescription.getAttributes().get("type") != null) && (tagDescription.getAttributes().get("type").equalsIgnoreCase("text"))) {
									tagDescription.getAttributes().put("name", "keywords");
									tagDescription.getAttributes().put("accesskey", "4");
									// tagDescription.getAttributes().put("value", "<%=i18nAccess.getViewText(\"search.title\")%>");
									// tagDescription.getAttributes().put("onfocus", "if (this.value == '<%=i18nAccess.getViewText(\"search.title\")%>'){this.value='';}");
									remplacement.addReplacement(tagDescription.getOpenStart(), tagDescription.getCloseEnd() + 1, tagDescription.render(null));
									remplacement.addReplacement(tagDescription.getOpenEnd() + 1, tagDescription.getOpenEnd() + 1, searchInput);
								}
							}
						}
					}
				}

				/* link - link */
				if (tags[i].getName().equalsIgnoreCase("a")) {
					String hrefValue = attributes.get("href");
					if (hrefValue != null) {
						if (!hrefValue.startsWith("#") && !hrefValue.startsWith("?") && !hrefValue.startsWith("${")) {
							if (hrefValue.toLowerCase().startsWith("rss")) {
								String channel = "";
								if (hrefValue.contains(":")) {
									channel = hrefValue.split(":")[1];
								}
								hrefValue = "<%=URLHelper.createRSSURL(ctx, \"" + channel + "\")%>";
								attributes.put("href", hrefValue);
								remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getOpenEnd() + 1, tags[i].toString());
							} else if ((hrefValue != null) && (!StringHelper.isURL(hrefValue)) && (!StringHelper.isMailURL(hrefValue))) {
								attributes.put("href", "<%=URLHelper.createURLCheckLg(ctx,\"" + hrefValue + "\")%>");
								remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getOpenEnd() + 1, tags[i].toString());
							}
						}
					}
				}

				/* insert after body */
				if (!isMail) {
					if (tags[i].getName().equalsIgnoreCase("body")) {
						remplacement.addReplacement(tags[i].getCloseStart(), tags[i].getCloseStart(), getGoogleAnalyticsCode());
					}
				}

				/* head - StyleSheet */
				if (!isMail) {
					if (tags[i].getName().equalsIgnoreCase("head")) {

						if (content.indexOf(HEADER_ZONE) > 0) {
							remplacement.addReplacement(content.indexOf(HEADER_ZONE), content.indexOf(HEADER_ZONE) + HEADER_ZONE.length(), getHTMLPrefixHead());
						} else {
							remplacement.addReplacement(tags[i].getOpenEnd() + 1, tags[i].getOpenEnd() + 1, getHTMLPrefixHead());
						}

						/** template plugin **/
						ByteArrayOutputStream outStream = new ByteArrayOutputStream();
						PrintStream out = new PrintStream(outStream);
						out.println("");
						out.println("<!-- template plugins -->");
						for (TemplatePlugin plugin : templatePlugins) {
							if (plugin != null) {
								String headHTML = plugin.getHTMLHead(globalContext);
								TagDescription[] pluginTags = searchAllTag(headHTML, false);
								for (TagDescription tag : pluginTags) {
									String resource = null;
									if (tag.getAttributes().get("src") != null) {
										resource = new File(tag.getAttributes().get("src")).getName(); // for js take only the name of js file.
										tag.getAttributes().put("src", "<%=URLHelper.createStaticTemplatePluginURL(ctx, \"" + tag.getAttributes().get("src") + "\", \"" + plugin.getFolder() + "\")%>");
									}
									if (tag.getAttributes().get("href") != null) {
										resource = tag.getAttributes().get("href");
										tag.getAttributes().put("href", "<%=URLHelper.createStaticTemplatePluginURL(ctx, \"" + tag.getAttributes().get("href") + "\", \"" + plugin.getFolder() + "\")%>");
									}
									String inside = tag.getInside(headHTML);
									if (tag.getName().equalsIgnoreCase("link")) { // auto close link tag
										inside = null;
									}
									String outHead = tag.render(inside);
									if (resource != null) {
										outHead = "<%if (!XHTMLHelper.allReadyInsered(ctx,\"" + resource + "\")) { %>" + outHead + "<%} else {%><!-- resource allready insered: " + resource + " --><%}%>";
									}
									String homeRendercode = "<%=URLHelper.createStaticTemplatePluginURL(ctx, \"/\", \"" + plugin.getFolder() + "\")%>";
									outHead = outHead.replace(TemplatePlugin.HOME_KEY, homeRendercode);
									out.println(outHead);
								}
							}
						}
						out.println("<!-- end template plugins -->");
						out.close();

						remplacement.addReplacement(tags[i].getCloseStart() - 1, tags[i].getCloseStart(), getHTMLSufixHead() + new String(outStream.toByteArray()));
					}
				}

				/* title */
				if (!isMail) {
					if (tags[i].getName().equalsIgnoreCase("title")) {
						if (tags[i].getCloseStart() - tags[i].getOpenEnd() > 1) { // if
							// content
							// in
							// title
							// tag
							if (!tags[i].getInside(content).toLowerCase().contains("title")) {
								remplacement.addReplacement(tags[i].getCloseStart(), tags[i].getCloseStart(), " - <%=currentTitle%>");
							}
						} else {
							remplacement.addReplacement(tags[i].getCloseStart(), tags[i].getCloseStart(), "<%=currentTitle%>");
						}
					}
				}

				/* resource */
				String srcValue = attributes.get("src");
				if ((srcValue != null)) {
					resources.add(srcValue);
					if (tags[i].getName().equalsIgnoreCase("script")) {
						String newLinkGeneratorIf = "<%if (!XHTMLHelper.allReadyInsered(ctx, \"" + srcValue + "\")) {%>";
						if (!StringHelper.isURL(srcValue)) {
							attributes.put("src", "<%=URLHelper.createStaticTemplateURL(ctx,\"/" + srcValue + "\")%>");
						}
						remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getOpenEnd() + 1, newLinkGeneratorIf + tags[i].toString() + "<%}%>");
					} else {
						attributes.put("src", "<%=URLHelper.createStaticTemplateURL(ctx,\"/" + srcValue + "\")%>");
						remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getOpenEnd() + 1, tags[i].toString());
					}
				}

				/* flash resource access */
				if (tags[i].getName().equalsIgnoreCase("param")) {
					if (tags[i].getAttributes().get("name") != null && tags[i].getAttributes().get("value") != null) {
						if (tags[i].getAttributes().get("name").equalsIgnoreCase("movie")) {
							tags[i].getAttributes().put("value", "<%=URLHelper.createStaticTemplateURL(ctx,\"/" + tags[i].getAttributes().get("value") + "\")%>");
							remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getOpenEnd() + 1, tags[i].toString());
						}
					}
				}

				/* flash resource access */
				if (tags[i].getName().equalsIgnoreCase("object")) {
					if (tags[i].getAttributes().get("data") != null) {
						tags[i].getAttributes().put("data", "<%=URLHelper.createStaticTemplateURL(ctx,\"/" + tags[i].getAttributes().get("data") + "\")%>");
						remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getOpenEnd() + 1, tags[i].toString());
					}
				}
			}

			// src url
			int urlIndex = content.indexOf("url(");
			while (urlIndex >= 0) {
				int closeIndex = content.substring(urlIndex).indexOf(')') + urlIndex;
				String url = content.substring(urlIndex + 4, closeIndex).trim();
				if (url.startsWith("'")) {
					url = url.substring(1, url.length() - 1);
				}
				resources.add(url);
				String newURL = "'<%=URLHelper.createStaticTemplateURL(ctx,\"" + url + "\")%>'";
				remplacement.addReplacement(urlIndex + 4, closeIndex, newURL);
				urlIndex = content.indexOf("url(", closeIndex);
			}
			String newContent = getJSPHeader() + remplacement.start(content);

			// replace meta data
			// if (!isMail) {
			newContent = newContent.replaceAll("##global-title##", "<%=globalTitle%>");
			newContent = newContent.replaceAll("##page-title##", "<%=currentTitle%>");
			newContent = newContent.replaceAll("##page-name##", "<%=pageName%>");
			newContent = newContent.replaceAll("##language##", "<%=ctx.getLanguage()%>");
			newContent = newContent.replaceAll("##country##", "<%=globalContext.getCountry()%>");
			newContent = newContent.replaceAll("##date-full##", "<%=StringHelper.renderUserFriendlyDate(ctx, new Date())%>");
			newContent = newContent.replaceAll("##date-short##", "<%=StringHelper.renderDate(ctx, new Date())%>");
			// }

			// TODO: change the charset with other technique
			newContent = newContent.replaceAll("charset=utf-8", "charset=" + ContentContext.CHARACTER_ENCODING);
			newContent = newContent.replaceAll("charset=UTF-8", "charset=" + ContentContext.CHARACTER_ENCODING);
			newContent = newContent.replaceAll("charset=iso-8859-15", "charset=" + ContentContext.CHARACTER_ENCODING);
			newContent = newContent.replaceAll("charset=ISO-8859-15", "charset=" + ContentContext.CHARACTER_ENCODING);
			newContent = newContent.replaceAll("charset=iso-8859-1", "charset=" + ContentContext.CHARACTER_ENCODING);
			newContent = newContent.replaceAll("charset=ISO-8859-1", "charset=" + ContentContext.CHARACTER_ENCODING);
			newContent = newContent.replaceAll("charset=iso-8859-2", "charset=" + ContentContext.CHARACTER_ENCODING);
			newContent = newContent.replaceAll("charset=ISO-8859-2", "charset=" + ContentContext.CHARACTER_ENCODING);

			newContent = newContent.replaceAll("##mailing.web-view##", "<a href=\"<%=URLHelper.createAbsoluteViewURL(ctx, ctx.getPath())%>\"><%=i18nAccess.getViewText(\"mailing.not-visible\")%></a>");

			if (jspFile != null) {
				ResourceHelper.writeStringToFile(jspFile, newContent, ContentContext.CHARACTER_ENCODING);
			}
		} catch (BadXMLException e) {
			if (i18nAccess != null) {
				String[][] params = new String[][] { { "line", "" + e.getLocalisation() }, { "tag", e.getTag() } };
				String msg = i18nAccess.getText("template.error.xml", params);
				GenericMessage outMsg = new GenericMessage(msg, GenericMessage.ERROR);
				messages.add(outMsg);
				ResourceHelper.writeStringToFile(jspFile, outMsg.getMessage());
			} else {
				ResourceHelper.writeStringToFile(jspFile, e.getMessage());
			}
			e.printStackTrace();
		}

		return depth + 1;

	}

	public static int convertHTMLtoMail(File htmlFile, File jspFile) throws IOException, BadXMLException {
		return convertHTMLtoJSP(null, null, htmlFile, jspFile, Collections.EMPTY_MAP, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, null, null, true);
	}

	public static int convertHTMLtoTemplate(GlobalContext globalContext, File htmlFile, File jspFile, Map<String, String> tagsID, List<String> areas, List<String> resources, List<TemplatePlugin> templatePlugins, List<String> ids, boolean isMailing) throws IOException, BadXMLException {
		return convertHTMLtoJSP(globalContext, null, htmlFile, jspFile, tagsID, areas, resources, templatePlugins, null, ids, isMailing);
	}

	public static int convertHTMLtoTemplate(GlobalContext globalContext, I18nAccess i18nAccess, File htmlFile, File jspFile, Map<String, String> tagsID, List<String> areas, List<String> resources, List<TemplatePlugin> templatePlugins, List<GenericMessage> messages) throws IOException, BadXMLException {
		return convertHTMLtoJSP(globalContext, i18nAccess, htmlFile, jspFile, tagsID, areas, resources, templatePlugins, messages, null, false);
	}

	private static String getAfterBodyCode() throws IOException {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println("<%if (currentPage.getImage(ctx) != null && globalContext.isFirstImage()) {");
		out.print("%><img id=\"_first-image\" src=\"<%=URLHelper.createTransformURL(ctx, currentPage.getImage(ctx).getResourceURL(ctx), \"standard\")%>\" alt=\"<%=currentPage.getImage(ctx).getImageDescription(ctx)%>\" />");
		out.println("<%}%>");
		out.close();
		return writer.toString();
	}

	/**
	 * return all parents name as lowercase.
	 * 
	 * @param allTags
	 * @param tag
	 * @return
	 */
	public static Set<String> getAllParentName(TagDescription[] allTags, TagDescription tag) {
		Set<String> outParents = new LinkedHashSet<String>();

		TagDescription parent = searchParent(allTags, tag);
		while (parent != null) {
			outParents.add(parent.getName().toLowerCase());
			parent = searchParent(allTags, parent);
		}

		return outParents;
	}

	private static String getEscapeMenu(String contentId) throws IOException {
		StringWriter outString = new StringWriter();
		BufferedWriter out = new BufferedWriter(outString);
		out.append("<div style=\"position: absolute; top: -100px;\" id=\"jv_escape_menu\">");
		out.newLine();
		out.append("<ul>");
		out.append("<li><a href=\"#" + contentId + "\"><%=i18nAccess.getViewText(\"wai.to_content\")%></a></li>");
		out.append("</ul>");
		out.newLine();
		out.append("</div>");
		out.newLine();
		out.close();
		return outString.toString();
	}

	private static String getGoogleAnalyticsCode() throws IOException {

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		/*
		 * out.println("<script type=\"text/javascript\">"); out.println("var gaJsHost = ((\"https:\" == document.location.protocol) ? \"https://ssl.\" : \"http://www.\");"); out.println("document.write(unescape(\"%3Cscript src='\" + gaJsHost + \"google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E\"));"); out.println("</script>"); out.println("<script type=\"text/javascript\">"); out.println("try {"); out.println("var pageTracker = _gat._getTracker(\"<%=globalContext.getGoogleAnalyticsUACCT()%>\");"); out.println("pageTracker._trackPageview();"); out.println("} catch(err) {}</script>");
		 */

		out.println("<script type=\"text/javascript\">");
		out.println("var _gaq = _gaq || [];");
		out.println("_gaq.push(['_setAccount', '<%=globalContext.getGoogleAnalyticsUACCT()%>']);");
		out.println("_gaq.push(['_trackPageview']);");
		out.println("   (function() {");
		out.println("      var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;");
		out.println("      ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';");
		out.println("      var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);");
		out.println("   })();");
		out.println("</script>");

		out.close();
		return writer.toString();
	}

	public static String getHTMLBody(String html) throws BadXMLException {
		TagDescription[] tags = searchAllTag(html, false);
		for (TagDescription tag : tags) {
			if (tag.getName().equalsIgnoreCase("body")) {
				return html.substring(tag.getOpenEnd() + 1, tag.getCloseStart());
			}
		}
		return html;
	}

	public static String getHTMLCleanedHead(String html) throws BadXMLException {
		TagDescription[] tags = searchAllTag(html, false);
		StringRemplacementHelper remplacement = new StringRemplacementHelper();
		for (TagDescription tag : tags) {
			if (tag.getName().equalsIgnoreCase("title") || tag.getName().equalsIgnoreCase("meta")) {
				remplacement.addReplacement(tag.getOpenStart(), tag.getCloseEnd() + 1, "");
			}
		}
		html = remplacement.start(html);
		tags = searchAllTag(html, false);
		for (TagDescription tag : tags) {
			if (tag.getName().equalsIgnoreCase("head")) {
				return html.substring(tag.getOpenEnd() + 1, tag.getCloseStart());
			}
		}
		return "";
	}

	private static String getHTMLPrefixHead() throws IOException {

		StringWriter outString = new StringWriter();
		BufferedWriter out = new BufferedWriter(outString);

		out.append("<script type=\"text/javascript\">");
		out.newLine();
		out.append("<!--");
		out.newLine();
		out.append("var sLanguage = '<%=ctx.getRequestContentLanguage()%>';");
		out.newLine();
		out.append("var server = '<%=URLHelper.createStaticURL(ctx, \"/\")%>';");
		out.newLine();
		out.append("-->");
		out.newLine();
		out.append("</script>");

		out.append("<%if (currentPage.getKeywords(ctx).length()>0){%>");
		out.newLine();
		out.append("<meta name=\"keywords\" content=\"<%=currentPage.getKeywords(ctx)%>\" />");
		out.newLine();
		out.append("<%}");
		out.append("%><%if (currentPage.getMetaDescription(ctx).length()>0){%><meta name=\"description\" content=\"<%=currentPage.getMetaDescription(ctx)%>\" />");
		out.newLine();
		out.append("<%}");
		out.newLine();
		out.append("%><%=XHTMLNavigationHelper.getRSSHeader(ctx, currentPage)%>");
		out.newLine();

		out.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"<%=URLHelper.createStaticURL(ctx,\"/jsp/view/components_css.jsp\")%>\" />");
		out.newLine();
		out.append("<link rel=\"shortcut icon\" type=\"image/ico\" href=\"<%=URLHelper.createStaticURL(ctx,\"/favicon.ico\")%>\" />");
		out.newLine();
		out.append("<%if (ctx.isInteractiveMode()) {%>");
		out.newLine();
		out.append("<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, \"/js/lib/jquery-1.7.2.min.js\")%>");
		out.newLine();
		out.append("<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, \"/js/lib/jquery-ui-1.8.20.custom.min.js\")%>");
		out.newLine();
		out.append("<%EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());%>");
		out.newLine();
		out.append("<%if (editCtx.isEditPreview()) {%>");
		out.newLine();	
		out.append("<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, \"/js/lib/jquery.colorbox-min.js\")%>");
		out.newLine();
		out.append("<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, \"/css/lib/colorbox/colorbox.css\")%>");
		out.newLine();
		out.append("<%  }%>");
		out.newLine();
		out.append("<%}%>");

		out.append("<%for (String uri : currentPage.getExternalResources(ctx)) {%>");
		out.newLine();
		out.append("<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, uri)%>");
		out.append("<%}%>");

		out.close();

		return outString.toString();
	}

	private static String getHTMLSufixHead() throws IOException {

		StringWriter outString = new StringWriter();
		BufferedWriter out = new BufferedWriter(outString);

		out.append("<%if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE || ctx.getRenderMode() == ContentContext.TIME_MODE) {");
		out.newLine();
		out.append("EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());%>");
		out.newLine();
		out.append("<%=(ctx.isInteractiveMode() ? \"<link rel=\\\"stylesheet\\\" type=\\\"text/css\\\" href=\\\"\"+URLHelper.createStaticURL(ctx,\"/css/preview/edit_preview.css\")+\"\\\"></link>\" : \"\")  %>");
		out.newLine();
		out.append("<%=(ctx.isInteractiveMode() ? \"<link rel=\\\"stylesheet\\\" type=\\\"text/css\\\" href=\\\"\"+editCtx.getEditTemplateFolder()+\"/css/edit_preview.css\"+\"\\\"></link>\" : \"\")  %>");
		out.newLine();
		out.append("<%=(ctx.isInteractiveMode() ? \"<script type=\\\"text/javascript\\\" src=\\\"\"+URLHelper.createStaticURL(ctx,\"/js/preview/edit_preview.js\")+\"\\\"></script>\" : \"\")  %>");
		out.newLine();
		out.append("<%=(ctx.isInteractiveMode() ? \"<script type=\\\"text/javascript\\\" src=\\\"\"+URLHelper.createStaticURL(ctx,\"/js/edit/ajax.js\")+\"\\\"></script>\" : \"\")  %>");
		out.newLine();
		out.append("<%=(ctx.isInteractiveMode() ? \"<script type=\\\"text/javascript\\\" src=\\\"\"+URLHelper.createStaticURL(ctx,\"/js/edit/core.js\")+\"\\\"></script>\" : \"\")  %>");
		out.newLine();

		out.append("<%if ((ctx.isInteractiveMode())&&(security.haveRight((User)editCtx.getUserPrincipal(), \"update\"))) {%><script type=\"text/javascript\">");
		out.newLine();
		out.append("var ajaxURL = \"<%=URLHelper.createAjaxURL(ctx)%>\";");
		out.newLine();
		out.append("var currentURL = \"<%=URLHelper.createURL(ctx)%>\";");
		out.newLine();
		out.append("var editPreviewURL = \"<%=URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE))%>?module=content&webaction=editPreview&previewEdit=true\";");
		out.newLine();
		out.append("</script><%}%>");

		out.append("<%}%>");
		out.newLine();
		out.append("<%if (currentPage.getHeaderContent(ctx) != null) {%><%=currentPage.getHeaderContent(ctx)%><%}%>");

		out.close();

		return outString.toString();
	}

	private static String getJSPHeader() throws IOException {

		StringWriter outString = new StringWriter();
		BufferedWriter out = new BufferedWriter(outString);

		out.append("<%@page contentType=\"text/html\" pageEncoding=\"" + ContentContext.CHARACTER_ENCODING + "\"");
		out.newLine();
		out.append("	import=\"java.util.Date,");
		out.newLine();
		out.append("		org.javlo.helper.URLHelper,");
		out.newLine();
		out.append("		org.javlo.context.ContentContext,");
		out.newLine();
		out.append("		org.javlo.service.ContentService,");
		out.newLine();
		out.append("		org.javlo.data.InfoBean,");
		out.newLine();
		out.append("		org.javlo.i18n.I18nAccess,");
		out.newLine();
		out.append("		org.javlo.helper.StringHelper,");
		out.newLine();
		out.append("		org.javlo.user.User,");
		out.newLine();
		out.append("		org.javlo.context.EditContext,");
		out.newLine();
		out.append("		org.javlo.helper.XHTMLHelper,");
		out.newLine();
		out.append("		org.javlo.message.MessageRepository,");
		out.newLine();
		out.append("		org.javlo.user.AdminUserSecurity,");
		out.newLine();
		out.append("		org.javlo.navigation.MenuElement,");
		out.newLine();
		out.append("		org.javlo.helper.XHTMLNavigationHelper,");
		out.newLine();
		out.append("		org.javlo.context.GlobalContext\"");
		out.newLine();
		out.append("%>");
		out.newLine();
		out.append("<%@taglib prefix=\"c\" uri=\"http://java.sun.com/jsp/jstl/core\"%>");
		out.newLine();
		out.append("<%");
		out.newLine();
		out.append("ContentContext ctx = ContentContext.getContentContext(request, response);");
		out.newLine();
		out.append("ContentService content = ContentService.getInstance(request);");
		out.newLine();
		out.append("GlobalContext globalContext = GlobalContext.getInstance(request); ");
		out.newLine();
		out.append("MenuElement currentPage = ctx.getCurrentPage();");
		out.newLine();
		out.append("InfoBean infoBean = InfoBean.getCurrentInfoBean(request);");
		out.newLine();
		out.append("String currentTitle = currentPage.getPageTitle(ctx);");
		out.newLine();
		out.append("String pageName = currentPage.getName();");
		out.newLine();
		out.newLine();
		out.append("String globalTitle = currentPage.getGlobalTitle(ctx);");
		out.append("if (globalTitle == null) {");
		out.append("	globalTitle = globalContext.getGlobalTitle();");
		out.append("}");
		out.newLine();
		out.append("I18nAccess i18nAccess = I18nAccess.getInstance(request);");
		out.newLine();
		out.append("AdminUserSecurity security = AdminUserSecurity.getInstance();");
		out.newLine();
		out.append("%>");
		out.close();

		return outString.toString();
	}

	private static String getPreviewCode() {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println("<%if (ctx.isInteractiveMode() && ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {%>");
		out.println("<jsp:include page=\"/jsp/preview/command.jsp\" />");
		// out.println("<%if (editCtx.isEditPreview()) {");
		out.println("<%MessageRepository messageRepository = MessageRepository.getInstance(ctx);");
		out.println("   if (messageRepository.getGlobalMessage().getMessage().trim().length() > 0) {%>");
		out.println("	    <div id=\"pc_message\" class=\"standard\">");
		out.println("		<%=XHTMLHelper.getIconesCode(ctx, \"close.gif\", \"close\", \"hidePreviewMessage();\")%>");
		out.println("       <div class=\"<%=messageRepository.getGlobalMessage().getTypeLabel()%>\"><%=messageRepository.getGlobalMessage().getMessage()%></div>");
		out.println("</div><%}%>");
		// out.println("<%}%>");
		out.println("<%}%>");

		out.println("<%if (ctx.isInteractiveMode() && ctx.getRenderMode() == ContentContext.TIME_MODE) {%>");
		out.println("<jsp:include page=\"/jsp/time-traveler/command.jsp\" />");
		out.println("<%MessageRepository messageRepository = MessageRepository.getInstance(ctx);");
		out.println("   if (messageRepository.getGlobalMessage().getMessage().trim().length() > 0) {%>");
		out.println("	    <div id=\"pc_message\" class=\"standard\">");
		out.println("		<%=XHTMLHelper.getIconesCode(ctx, \"close.gif\", \"close\", \"hidePreviewMessage();\")%>");
		out.println("       <div class=\"<%=messageRepository.getGlobalMessage().getTypeLabel()%>\"><%=messageRepository.getGlobalMessage().getMessage()%></div>");
		out.println("</div><%}%>");
		out.println("<%}%>");

		out.close();
		return writer.toString();
	}

	private static String getResetTemplate() throws IOException {
		StringWriter outString = new StringWriter();
		BufferedWriter out = new BufferedWriter(outString);
		/*
		 * out.append("<%if (request.getParameter(PageConfiguration.FORCE_TEMPLATE_PARAM_NAME) != null) {%>"); out.append("<div id=\"_reset_template\">"); out.append("<a href=\"<%=URLHelper.createURLNoForceTemplate(ctx)%>\"><%=i18nAccess.getViewText(\"template.clean-force-template\")%> : <%=request.getParameter(PageConfiguration.FORCE_TEMPLATE_PARAM_NAME)%></a>"); out.append("<div class=\"_reset_template-close\">"); out.append("<%=XHTMLHelper.getIconesCode(ctx, \"close.gif\", \"start\")%>"); out.append("<a href=\"#\" onclick=\"document.getElementById('_reset_template').style.visibility='hidden';; return false;\">"); out.append("</a></div>"); out.append("</div>"); out.append("<%}%>"); out.newLine();
		 */
		out.close();
		return outString.toString();
	}

	private static String getValue(Map<String, String> map, String key, String defaultValue) {
		String out = map.get(key);
		if (out == null) {
			return defaultValue;
		} else {
			return out;
		}
	}

	public static void main(String[] args) {
		/*
		 * try { List<String> areas = new LinkedList<String>(); areas.add("mainCol"); convertHTMLtoTemplate(new File("c:/trans/index.html"), new File("c:/trans/index.jsp"), new HashMap(), areas, new LinkedList<String>()); System.out.println("done."); } catch (Exception e) { e.printStackTrace(); }
		 */
		try {

			String html = "<html><head></head><body><div><ul><li>list</li></ul></div></body></html>";
			TagDescription[] description = searchAllTag(html, false);
			for (TagDescription element : description) {
				System.out.println("tag : " + element.getName());
				System.out.println("** " + element.getName() + " parent = " + searchParent(description, element));
			}
			System.out.println("HEAD:" + getHTMLCleanedHead(html));
			System.out.println("BODY:" + getHTMLBody(html));
		} catch (BadXMLException e) {
			e.printStackTrace();
		}
	}

	public static TagDescription[] searchAllTag(String xml, boolean validation) throws BadXMLException {

		boolean inTag = false;
		Stack<TagDescription> stack = new Stack<TagDescription>();

		Collection<TagDescription> outTags = new ArrayList<TagDescription>();

		StringBuffer key = new StringBuffer();
		StringBuffer value = new StringBuffer();
		StringBuffer tagNameBuf = new StringBuffer();
		int openTag = 0;
		int closeTag = 0;
		int line = 1;
		for (int i = 0; i < xml.length(); i++) {

			if (xml.charAt(i) == '\n') {
				line++;
			}

			if ((xml.charAt(i) == '<') && (xml.charAt(i + 1) != '!')) {
				TagDescription tag = new TagDescription();
				openTag = i;
				if (!inTag) {
					i++;
					tagNameBuf.setLength(0);
					while ((xml.charAt(i) != '>') && (xml.charAt(i) != ' ') && (i < xml.length())) {
						tagNameBuf.append(xml.charAt(i));
						i++;
					}

					/* search attributes */
					Map<String, String> attributes = tag.getAttributes();
					boolean inValue = false;
					boolean inCharSequence = false;
					while ((xml.charAt(i) != '>') && (xml.charAt(i) != '<') && (i < xml.length())) {
						if ((xml.charAt(i) == '"')) {
							inCharSequence = !inCharSequence;
						}
						if ((xml.charAt(i) == ' ') && (!inCharSequence)) {
							if (inValue) {
								if ((key.length() > 0)) {
									String attValue = value.toString();
									if (attValue.startsWith("\"") || attValue.startsWith("'")) {
										attValue = attValue.substring(1, attValue.length() - 1);
										if (attValue.endsWith("\"") || attValue.endsWith("'")) {
											if (attValue.length() > 1) {
												attValue = attValue.substring(0, attValue.length() - 1);
											} else {
												attValue = "";
											}
										}
									}
									attributes.put(key.toString(), attValue);
								}
								key.setLength(0);
								value.setLength(0);
								inValue = false;
							}
						} else if ((xml.charAt(i) == '=') && (!inCharSequence)) {
							inValue = true;
						} else {
							if (inValue) {
								value.append(xml.charAt(i));
							} else {
								key.append(xml.charAt(i));
							}
						}
						i++;
					}
					String tagName = tagNameBuf.toString().trim();
					tag.setName(tagName);
					if (validation && (xml.charAt(i) == '<')) {

						int length = 50;
						int start = i - length;
						int end = i + length;
						if (length > i) {
							start = 0;
						}
						if (end >= xml.length()) {
							end = xml.length() - 1;
						}
						String msg = "tag '" + tagName + "' is not coorectly closed ' char localistaion : " + i + " [" + xml.substring(start, end) + "]";

						throw new BadXMLException(msg, i, tagName, xml.substring(start, end));
					} else {
						if (inValue) {
							if ((key.length() > 0)) {
								String attValue = value.toString();
								if (attValue.startsWith("\"") || attValue.startsWith("'")) {
									if (attValue.length() > 1) {
										attValue = attValue.substring(1, attValue.length() - 1);
									}
								}
								if (attValue.endsWith("\"") || attValue.endsWith("'")) {
									if (attValue.length() > 1) {
										attValue = attValue.substring(0, attValue.length() - 1);
									} else {
										attValue = "";
									}
								}
								attributes.put(key.toString(), attValue);
							}
							inValue = false;
						}
					}
					key.setLength(0);
					value.setLength(0);
					closeTag = i;
					/* /search attribute */

					if (tagName.startsWith("/")) { // close tag
						tagName = tagName.substring(1);
						if (stack.empty()) {
							System.out.println("-- bad close tag : " + tagName);
						} else {
							TagDescription openedTag = stack.pop();
							if (!tagName.equals(openedTag.getName())) {
								if (!tagName.equals(openedTag.getName())) {
									if (validation) {

										int length = 50;
										int start = i - length;
										int end = i + length;
										if (length > i) {
											start = 0;
										}
										if (end >= xml.length()) {
											end = xml.length() - 1;
										}
										String msg = "tag '" + tagName + "' close the tag '" + openedTag.getName() + "' char localistaion : " + i + " [" + xml.substring(start, end) + "]";

										throw new BadXMLException(msg, line, openedTag.getName(), xml.substring(start, end));
									}
								}
							}
							openedTag.setCloseStart(openTag);
							openedTag.setCloseEnd(closeTag);
						}

					} else {
						if (tagName.length() > 0) {
							tag.setOpenStart(openTag);
							tag.setOpenEnd(closeTag);
							stack.push(tag);
							outTags.add(tag);
						}
					}

					if ((xml.charAt(i - 1) == '/') || (xml.charAt(i - 1) == '-')) { // auto
						// close
						// tag
						TagDescription autoCloseTag = stack.pop();
						if (!tagName.equals(autoCloseTag.getName()) && validation) {
							String msg = "tag '" + tagName + "' close the tag '" + tagName + "' char localistaion : " + i;
							throw new BadXMLException(msg, i, tagName, autoCloseTag.getName());
						} else {
							autoCloseTag.setCloseStart(autoCloseTag.getOpenStart());
							autoCloseTag.setCloseEnd(autoCloseTag.getOpenEnd());
						}
					}
				}
			}
		}

		TagDescription[] outTagDescription = new TagDescription[outTags.size()];
		outTags.toArray(outTagDescription);

		return outTagDescription;
	}

	public static List<TagDescription> searchChildren(TagDescription[] allTags, TagDescription tag) {

		List<TagDescription> outChildren = new LinkedList<TagDescription>();

		for (TagDescription allTag : allTags) {
			if (allTag.getOpenEnd() > tag.getOpenEnd()) {
				if (allTag.getCloseStart() < tag.getCloseStart()) {
					outChildren.add(allTag);
				}
			}
		}

		return outChildren;
	}

	public static TagDescription searchParent(TagDescription[] allTags, TagDescription tag) {
		if (allTags.length < 1 || tag.equals(allTags[0])) {
			return null;
		}
		TagDescription bestTag = allTags[0];
		for (int i = 0; i < allTags.length; i++) {
			if (allTags[i].getCloseStart() > tag.getCloseEnd() && allTags[i].getOpenStart() < tag.getOpenStart()) {
				if (allTags[i].getOpenStart() > bestTag.getOpenEnd()) {
					if (!allTags[i].equals(tag)) {
						bestTag = allTags[i];
					}
				}
			}
		}
		return bestTag;
	}

}