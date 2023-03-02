package org.javlo.helper;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.template.Template;
import org.javlo.template.TemplatePlugin;

public class XMLManipulationHelper {
	
	private static Logger logger = Logger.getLogger(XMLManipulationHelper.class.getName());

	private static final Set<String> autoCloseTag = new HashSet<String>(Arrays.asList(new String[] { "input", "meta", "link", "script", "img", "hr", "br" }));

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

		public String getAttribute(String key, String defaultValue) {
			String value = getAttributes().get(key);
			if (value == null) {
				value = defaultValue;
			}
			return value;
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

		/**
		 * return the tag rendered in HTML
		 * 
		 * @param inside
		 *            the content (null for autoclose tag)
		 * @return a HTML code with the tag
		 */
		public String renderOpen() {
			StringBuffer outTag = new StringBuffer();
			outTag.append("<" + getName());
			Collection<Map.Entry<String, String>> entries = getAttributes().entrySet();
			for (Map.Entry<String, String> entry : entries) {
				outTag.append(" ");
				outTag.append(entry.getKey());
				outTag.append("=");
				outTag.append("\"");
				outTag.append(entry.getValue().trim());
				outTag.append("\"");
			}
			outTag.append(">");
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
				out.append(attributes.get(key).trim());
				out.append("\"");
			}
			if (getOpenEnd() == getCloseEnd()) { // auto close tag
				out.append(" />");
			} else {
				out.append(">");
			}
			return out.toString();
		}

		public void addCssClass(String cssClass) {
			String css = getAttribute("class", "");
			attributes.put("class", (css + ' ' + cssClass).trim());
		}

	}

	public static final String AREA_PREFIX = "area.";

	public static final String AREA_VIEW_PREFIX = "area-view-if-empty.";

	public static final String AREA_VIEW_CONTAINER = "area-container.";

	public static final String HEADER_ZONE = "<!-- INSERT HEADER HERE -->";

	public static String changeLink(String html, String linkPrefix) throws BadXMLException {
		TagDescription[] tags = searchAllTag(html, false);
		StringRemplacementHelper remplacement = new StringRemplacementHelper();
		for (TagDescription tag : tags) {
			if (tag.getAttributes().get("src") != null) {
				if (!tag.getAttributes().get("src").contains(":") && !tag.getAttributes().get("src").contains("${")) {
					tag.getAttributes().put("src", URLHelper.mergePath(linkPrefix, tag.getAttributes().get("src")));
					remplacement.addReplacement(tag.getOpenStart(), tag.getOpenEnd() + 1, tag.toString());
				}
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
	private static int convertHTMLtoJSP(GlobalContext globalContext, Template template, I18nAccess i18nAccess, File htmlFile, File jspFile, Map<String, String> options, List<String> areas, List<String> resources, List<TemplatePlugin> templatePlugins, List<GenericMessage> messages, List<String> ids, boolean isMail, String fontIncluding) throws IOException {

		String templateVersion = StringHelper.getRandomId();
		
		if (fontIncluding.contains("##BASE_URI##")) {
			fontIncluding = fontIncluding.replace("##BASE_URI##", "${info.rootTemplateURL}");
		} else {
			//fontIncluding = FontHelper.loadFont(fontIncluding);
		}
		
		if (resources == null) {
			resources = new LinkedList<String>();
		}
		if (messages == null) {
			messages = new LinkedList<GenericMessage>();
		}
		
		String shortKey = "";
		if (template.isShortkeyToEdit()) {
			shortKey = "<c:if test=\"${!contentContext.asPageMode}\">";
			shortKey += "<script type=\"text/javascript\">"
					+ "function _tmc(e){"
					+ "(e=e||event).which=e.which||e.keyCode,_ky[e.which]=\"keydown\"===e.type,_ky[17]&&_ky[18]&&_ky[74]&&(document.location.href=\"${info.preview?info.currentViewURL:info.userName==null?info.currentEditURL:info.currentPreviewURL}?backPreview=true\")"
					+ "}"
					+ "function addEvent(e,n,t){"
					+ "return e.attachEvent?e.attachEvent(\"on\"+n,t):e.addEventListener(n,t,!1)"
					+ "}"
					+ "var _ky={};addEvent(window,\"keydown\",_tmc),addEvent(window,\"keyup\",_tmc);"
					+ "</script>";
			shortKey += "</c:if>";
		}

		int depth = 0;
		try {

			if (jspFile != null) {
				if (!jspFile.exists()) {
					logger.info("create : "+jspFile);
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

			PrefixHeadContext headContext = new PrefixHeadContext();
			/**
			 * check if description and keyword is already defined.
			 */
			for (int i = 0; i < tags.length; i++) {
				if (!isMail) {
					if (tags[i].getName().equalsIgnoreCase("meta")) {
						if ((tags[i].getAttributes().get("name") != null) && (tags[i].getAttributes().get("name").equalsIgnoreCase("description"))) {
							headContext.setDescription(false);
						} else if ((tags[i].getAttributes().get("name") != null) && (tags[i].getAttributes().get("name").equalsIgnoreCase("keywords"))) {
							headContext.setKeyword(false);
						}
					}
				}
			}

			boolean titleFound = false;
			TagDescription tagHead = null;
			for (int i = 0; i < tags.length; i++) {
				Map<String, String> attributes = tags[i].getAttributes();
				String idValue = attributes.get("id");
				if (ids != null && idValue != null) {
					ids.add(idValue);
				}

				/* area */
				for (String area : areas) {
					String areaValue = getValue(options, AREA_PREFIX + area, area);
					boolean displayIfEmpty = StringHelper.isTrue(getValue(options, AREA_VIEW_PREFIX + area, "true"));
					String areaContainer = getValue(options, AREA_VIEW_CONTAINER + area, null);
					if (areaContainer != null) {
						displayIfEmpty = false;
					}
					String prefix = "";
					String suffix = "";
					if (!displayIfEmpty) {
						String testArea = "";
						if (template.isRemoveEmptyArea()) {
							testArea = " && (!currentPage.isNoComponent(ctx, \"" + area + "\") || (EditContext.getInstance(globalContext, session).isPreviewEditionMode() && ctx.isAsPreviewMode()))";
						}
						prefix = "<%if ((!globalContext.getQuietArea().contains(\"" + area + "\"))" + testArea + ") {%>";
						suffix = "<%}%>";
					}
					if ((idValue != null) && (idValue.trim().equals(areaValue))) {
						remplacement.addReplacement(tags[i].getOpenStart() - 1, tags[i].getOpenStart() - 1, prefix);
						remplacement.addReplacement(tags[i].getOpenEnd() + 1, tags[i].getCloseStart(), "<jsp:include page=\"/jsp/view/content_view.jsp?area=" + area + "\" />");
						remplacement.addReplacement(tags[i].getCloseEnd() + 1, tags[i].getCloseEnd() + 1, suffix);

						String checkEmptyArea = "${empty info.areaEmpty['" + area + "']?' _not_empty_area':' _empty_area'}";
						String cssClass = StringHelper.neverNull(tags[i].getAttributes().get("class")) + "<%if (currentPage.getImageBackgroundForArea(ctx).get(\"" + area + "\") != null){%> width-background<%}%> <%=currentPage.getAreaClass(ctx, \""+area+"\")%>";
						
						tags[i].getAttributes().put("class", (cssClass + " _area").trim() + checkEmptyArea + " pos-<%=currentPage.getPosition()%>");
						String style = StringHelper.neverNull(tags[i].getAttributes().get("style")).trim();
						if (!style.endsWith(";") && style.length() > 0) {
							style = style + ';';
						}
						style += "<%if (currentPage.getImageBackgroundForArea(ctx).get(\"" + area + "\") != null){%>background-image:url('<%=URLHelper.createFileURL(ctx,currentPage.getImageBackgroundForArea(ctx).get(\"" + area + "\").getResourceURL(ctx))%>');<%}%>";
						tags[i].getAttributes().put("style", style);
						remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getOpenEnd() + 1, tags[i].renderOpen());
					} else if (idValue != null && areaContainer != null && idValue.trim().equals(areaContainer)) {
						remplacement.addReplacement(tags[i].getOpenStart() - 1, tags[i].getOpenStart() - 1, prefix);
						remplacement.addReplacement(tags[i].getCloseEnd() + 1, tags[i].getCloseEnd() + 1, suffix);
						remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getOpenEnd() + 1, tags[i].renderOpen());
					}
				}

				/* languages */
				if (!isMail) {
					if ((idValue != null) && (idValue.trim().equals(getValue(options, "tagid.language", "language")))) {
						boolean list = StringHelper.isTrue(getValue(options, "tagid.language.list", "true"));
						boolean languageJS = StringHelper.isTrue(getValue(options, "tagid.language.auto-change", "true"));
						if (list) {
							remplacement.addReplacement(tags[i].getOpenEnd() + 1, tags[i].getCloseStart(), "<%=XHTMLHelper.renderLanguage(ctx, \"" + getValue(options, "language.class", null) + "\" )%>");
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
				if ((idValue != null) && (idValue.trim().equals(getValue(options, "tagid.breadcrumb", "breadcrumb")))) {
					remplacement.addReplacement(tags[i].getOpenEnd() + 1, tags[i].getCloseStart(), "<%=XHTMLNavigationHelper.getBreadcrumbList(ctx)%>");
				}

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

				if (tags[i].getName().equalsIgnoreCase("html")) {
					String cssClass = StringHelper.neverNull(tags[i].getAttributes().get("class"));
					cssClass = cssClass + " " + "<%if (infoBean.getEditUser() != null) {if(userInterfaceContext.isMinimalInterface()) {%>preview-minimal <%} else {%>preview-standard <%} if (ctx.isInteractiveMode() && !ctx.isPreviewOnly()) {%><%=(StringHelper.isTrue(request.getParameter(\"preview-command\"), true)?\"preview-command-visible\":\"preview-command-hidden\")%> <%=StringHelper.neverNull(request.getParameter(\"html-class\"))%> <%if (ctx.isInteractiveMode() && !ctx.isPreviewOnly()) { if(EditContext.getInstance(globalContext, request.getSession()).isPreviewEditionMode() ) {%>edit-preview<%} else {%>preview-only<%} }%><%}}%>";
					tags[i].getAttributes().put("class", cssClass.trim());
					remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getOpenEnd() + 1, tags[i].renderOpen());
				}

				/* insert before all */
				if (tags[i].getName().equalsIgnoreCase("body")) {
					String contentZone = getValue(options, AREA_PREFIX + "content", null);
					String cssClass = StringHelper.neverNull(tags[i].getAttributes().get("class"));
					cssClass = cssClass + ' ' + "<%if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE && !ctx.isPreviewOnly()) { if(ctx.getGlobalContext().getStaticConfig().isFixPreview() ) {%>fix-preview<%} else {%>floating-preview<%} }%>";
					cssClass = cssClass + ' ' + "<%if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE && !ctx.isPreviewOnly()) { if(EditContext.getInstance(globalContext, request.getSession()).isPreviewEditionMode() ) {%>edit-preview<%} else {%>preview-only<%} }%>";
					cssClass = cssClass + ' ' + "<%if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE && !ctx.isPreviewOnly()) { if(ctx.getCurrentEditUser() == null) {%>preview-notlogged<%} else {%>preview-logged<%} }%>";
					cssClass = cssClass + ' ' + "<%if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {%> edition-mode-<%=ctx.getGlobalContext().getEditTemplateMode()%> <%}%>";
					cssClass = cssClass + ' ' + "${info.preview && !info.page.editable?'not-editable':''}";
					cssClass = cssClass + ' ' + "${info.admin?'right-admin':''}";
					cssClass = cssClass + ' ' + "${empty info.userName?'user-not-logged-in':'user-logged-in'}";
					if (globalContext.getTemplateData().isFixSidebar()) {
						cssClass = cssClass + ' ' + "fix-sidebar";
					} else {
						cssClass = cssClass + ' ' + "fluid-sidebar";
					}
					
					for (String area : template.getAreas()) {
						cssClass = cssClass+ ' ' + "<%=(currentPage.isEmpty(ctx, \""+area+"\", true)?\"empty-area-"+area+"\":\"not-empty-area-"+area+"\")%>";
					}
					
					cssClass = StringHelper.trimInternal(cssClass);
					
					tags[i].getAttributes().put("class", cssClass);

					if (template.isPDFRenderer()) {
						tags[i].getAttributes().put("data-pdfheight", "" + template.getPDFHeigth());
					}

					tags[i].getAttributes().put("data-areas", "" + template.getAjaxAreas());

					String mainPageAssociationCode = "<%if (currentPage.isChildrenAssociation() && (request.getParameter(\"" + Template.FORCE_TEMPLATE_PARAM_NAME + "\") == null)) {%><jsp:include page=\"/jsp/view/page_association.jsp\" /><%} else {%>";

					String openPageCode = "<c:if test=\"${contentContext.pageAssociation}\"><div id=\"<%=currentPage.getHtmlId(ctx)%>\" class=\"_page_associate <%if (currentPage.getNextBrother() == null) {%>last<%}%>\"></c:if>" + mainPageAssociationCode;
					String closePageCode = "<%}%><c:if test=\"${contentContext.pageAssociation}\"></div></c:if>";

					tags[i].addCssClass("<%for (String layout : ctx.getCurrentPage().getLayouts(ctx)) {%><%=' '+layout%><%}%>");
					tags[i].addCssClass("${globalContext.templateData.large?'large-content':'not-large-content'} ${globalContext.templateData.small?'small-content':''}");
					tags[i].addCssClass("page-index-${contentContext.currentPage.index} page-index-${contentContext.currentPage.index%2==0?'even':'odd'}");
					String renderBodyAsBody = tags[i].renderOpen();
					String tag = "div";
					if (!template.isMailing()) {
						tag = "section";
					}
					tags[i].setName(tag);
					tags[i].addCssClass("page_association_fake_body");
					if (tags[i].getAttribute("id", null) == null) {
						tags[i].getAttributes().put("id", "<%=ctx.getCurrentPage().getHtmlSectionId(ctx)%>");
					}
					String renderBodyAsDiv = tags[i].renderOpen();
					String openBodyCode = "<c:if test=\"${not contentContext.pageAssociation}\">" + renderBodyAsBody + "</c:if><c:if test=\"${contentContext.pageAssociation}\">" + renderBodyAsDiv + "</c:if>";
					if (htmlFile.getName().contains("pdf") || isMail) {
						fontIncluding = "";
					}

					String GACode = "";
					if (!template.isMailing()) {
						GACode = getGoogleAnalyticsCode();
					}

					String closeBodyCode = StringHelper.neverNull(fontIncluding) + "<c:if test=\"${not contentContext.pageAssociation}\">" + GACode + "</body></c:if><c:if test=\"${contentContext.pageAssociation}\"></" + tag + "></c:if>";
					remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getOpenEnd() + 1, "</c:if>" + openBodyCode + openPageCode);
					remplacement.addReplacement(tags[i].getCloseStart(), tags[i].getCloseEnd() + 1, closePageCode + closeBodyCode + "<c:if test=\"${not contentContext.pageAssociation}\">");

					String previewCode = "<c:if test=\"${not contentContext.pageAssociation}\">" + getPreviewCode(globalContext.getServletContext()) + "</c:if>";
					// remplacement.addReplacement(tags[i].getOpenEnd() + 1,
					// tags[i].getOpenEnd() + 1, previewCode +
					// getEscapeMenu(contentZone) + getResetTemplate() +
					// getAfterBodyCode());

					String targetEscapeMenu = template.getEscapeMenuId();
					if (targetEscapeMenu == null) {
						targetEscapeMenu = contentZone;
					}

					remplacement.addReplacement(tags[i].getOpenEnd() + 1, tags[i].getOpenEnd() + 1, "<%=ctx.getGlobalContext().getHeaderBloc()%>" + getEscapeMenu(targetEscapeMenu) + getResetTemplate() + getAfterBodyCode());
					if (isMail && globalContext.getStaticConfig().isMailingUserTracking()) {
						previewCode = previewCode + "<img class=\"empty_image\" alt=\"\" style=\"height: 0; width: 0; margin:0; padding: 0;\" width=\"0\" height=\"0\" src=\"<%=URLHelper.createStaticURL(ctx, globalContext.getStaticConfig().getMailingFeedBackURI())%>\" /> ";
					}

					String footerResourceIndlue = "";
					if (!globalContext.getStaticConfig().isProd()) {
						footerResourceIndlue  += "<!-- comp resources -->";
					}
					footerResourceIndlue  += "<%for (String uri : currentPage.getExternalResources(ctx)) {%><%=XHTMLHelper.renderHeaderResourceInsertion(ctx, uri, \""+template.getBuildId()+"\")%><%}%>";
					if (!globalContext.getStaticConfig().isProd()) {
						footerResourceIndlue  += "<!-- /comp resources -->";
					}
					remplacement.addReplacement(tags[i].getCloseStart() - 1, tags[i].getCloseStart() - 1, footerResourceIndlue + "<%=ctx.getGlobalContext().getFooterBloc()%>" + previewCode);
				}

				/* link - StyleSheet */
				if (tags[i].getName().equalsIgnoreCase("link")) {
					String hrefValue = attributes.get("href");

					if ((hrefValue != null) && !hrefValue.contains("${") && (!StringHelper.isURL(hrefValue)) && !hrefValue.toLowerCase().startsWith("https")) {
						String newLinkGeneratorIf = "<%if (!XHTMLHelper.alreadyInserted(ctx, \"" + hrefValue + "\")) {%>";
						resources.add(hrefValue);
						String templateVersionCode = "null";
						if (!template.isForStatic()) {
							templateVersionCode = "\"" + templateVersion + "\"";
						}
						attributes.put("href", "<%=URLHelper.createStaticTemplateURL(ctx,\"/" + hrefValue + "\", " + templateVersionCode + ")%>");
						remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getOpenEnd() + 1, newLinkGeneratorIf + tags[i].toString() + "<%}%>");
					}
				}

				/* form action */
				if (tags[i].getName().equalsIgnoreCase("form")) {
					String actionValue = attributes.get("action");
					if ((actionValue != null) && (!StringHelper.isURL(actionValue)) && !actionValue.contains("${")) {
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
								if ((tagDescription.getAttributes().get("type") != null) && (tagDescription.getAttributes().get("type").equalsIgnoreCase("text")) || (tagDescription.getAttributes().get("type").equalsIgnoreCase("search"))) {
									tagDescription.getAttributes().put("name", "keywords");
									tagDescription.getAttributes().put("accesskey", "4");
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
							} else if (!StringHelper.isURL(hrefValue) && !StringHelper.isMailURL(hrefValue)) {
								if (hrefValue.startsWith("/")) {
									attributes.put("href", "<%=URLHelper.createURLCheckLg(ctx,\"" + hrefValue + "\")%>");
								} else {
									attributes.put("href", "<%=URLHelper.createStaticTemplateURL(ctx,\"" + hrefValue + "\")%>");
								}

								remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getOpenEnd() + 1, tags[i].toString());
							}
						}
					}
				}

				/* head - StyleSheet */
				if (tags[i].getName().equalsIgnoreCase("head")) {
					tagHead = tags[i];
					
					// CDN
//					if (tagHead != null && globalContext.getSpecialConfig().getMainCdn() != null) {
//						String preconnectCdn = "<link rel=\"preconnect\" href=\""+StringHelper.extractHostAndProtocol(globalContext.getSpecialConfig().getMainCdn())+"\">";
//						remplacement.addReplacement(tagHead.getOpenEnd()+1, tagHead.getOpenEnd()+2, preconnectCdn);
//					}


					String staticHeader = StringHelper.neverEmpty(globalContext.getStaticConfig().getHtmlHead(), "");

					if (content.indexOf(HEADER_ZONE) > 0) {
						remplacement.addReplacement(content.indexOf(HEADER_ZONE), content.indexOf(HEADER_ZONE) + HEADER_ZONE.length(), getHTMLPrefixHead(globalContext, headContext, isMail) + staticHeader);
					} else {
						remplacement.addReplacement(tags[i].getOpenEnd() + 1, tags[i].getOpenEnd() + 1, getHTMLPrefixHead(globalContext, headContext, isMail) + staticHeader + shortKey);
					}

					/** template plugin **/
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					PrintStream out = new PrintStream(outStream);

					out.println("<%if (ctx.getCurrentPage().isNoIndex()) {%><meta name=\"robots\" content=\"noindex, follow\" /><%}%>");

					/** wysiwyg init css **/
					if (template.getWysiwygCss() != null) {
						out.println("<%if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE && !ctx.isPreviewOnly()) {%>");
						out.println("<script>");
						out.println("var wysiwygCss = '<%=URLHelper.createStaticTemplateURL(ctx,\"" + template.getWysiwygCss() + "\")%>';");
						out.println("</script>");
						out.println("<%}%>");
					}

					out.println("<%if (ctx.getRenderMode() != ContentContext.PAGE_MODE) {%>");
					out.println("<!-- template plugins -->");
					String reg = ".*url\\('(.*)'\\).*";
					Pattern p = Pattern.compile(reg);
					for (TemplatePlugin plugin : templatePlugins) {
						if (plugin != null) {
							String headHTML = plugin.getHTMLHead(globalContext);
							TagDescription[] pluginTags = searchAllTag(headHTML, false);
							for (TagDescription tag : pluginTags) {
								String resource = null;
								if (tag.getAttributes().get("src") != null) {
									resource = new File(tag.getAttributes().get("src")).getName();
									if (!tag.getAttributes().get("src").toLowerCase().startsWith("http://") && !tag.getAttributes().get("src").toLowerCase().startsWith("//") && !tag.getAttributes().get("src").toLowerCase().startsWith("https://")) {
										tag.getAttributes().put("src", "<%=URLHelper.createStaticTemplatePluginURL(ctx, \"" + tag.getAttributes().get("src") + "\", \"" + plugin.getFolder() + "\")%>");
									}
								}
								if (tag.getAttributes().get("href") != null) {
									resource = tag.getAttributes().get("href");
									tag.getAttributes().put("href", "<%=URLHelper.createStaticTemplatePluginURL(ctx, \"" + tag.getAttributes().get("href") + "\", \"" + plugin.getFolder() + "\")%>");
								}
								String inside = tag.getInside(headHTML);
								if (tag.getName().equalsIgnoreCase("link")) {
									inside = null;
								}
								String outHead = tag.render(inside);
								if (resource != null && !resource.toLowerCase().startsWith("https")) {
									outHead = "<%if (!XHTMLHelper.alreadyInserted(ctx,\"" + resource + "\")) { %>" + outHead + "<%} else {%><!-- resource already insered: " + resource + " --><%}%>";
								}
								String homeRendercode = "<%=URLHelper.createStaticTemplatePluginURL(ctx, \"/\", \"" + plugin.getFolder() + "\")%>";
								outHead = outHead.replace(TemplatePlugin.HOME_KEY, homeRendercode);

								Matcher m = p.matcher(outHead);
								if (m.matches()) {
									String uri = m.group(1);
									if (!StringHelper.isURL(uri)) {
										outHead = outHead.replace(uri, "<%=URLHelper.createStaticTemplatePluginURL(ctx, \"" + uri + "\", \"" + plugin.getFolder() + "\")%>");
									}
								}
								if (!plugin.isActiveInEdition()) {
									out.println("<%if (!ctx.isAsPreviewMode() || !EditContext.getInstance(globalContext, request.getSession()).isPreviewEditionMode()) {%>");
								}
								if (!plugin.isActiveOnScreenshot()) {
									out.println("<%if (!StringHelper.isTrue(request.getParameter(\"" + ContentContext.TAKE_SCREENSHOT + "\"))) {%>");
								}
								out.println(outHead);
								if (!plugin.isActiveInEdition()) {
									out.println("<%}%>");
								}
								if (!plugin.isActiveOnScreenshot()) {
									out.println("<%}%>");
								}
							}
						}
					}
					out.println("<!-- end template plugins -->");
					out.println("<%}%>");

					out.println("<%=ctx.getGlobalContext().getMetaBloc()%>");

					out.print("<%if (currentPage.getMetaHead(ctx) != null) {%>");
					out.print("<%=currentPage.getMetaHead(ctx)%>");
					out.println("<%}%>");
					out.close();
					
					String pageModules = "";
					if (!globalContext.getStaticConfig().isProd()) {
						pageModules += "<!-- comp modules -->";
					}
					pageModules += "<%for (String uri : currentPage.getExternalModules(ctx)) {%><%=XHTMLHelper.renderHeaderModuleInsertion(ctx, uri, \""+template.getBuildId()+"\")%><%}%>";
					if (!globalContext.getStaticConfig().isProd()) {
						pageModules += "<!-- /comp modules -->";
					}
					String pageCss = "<%if (currentPage.getCss(ctx).length() > 0) {%><style><%=currentPage.getCss(ctx)%></style><%}%>";
					
					remplacement.addReplacement(tags[i].getCloseStart() - 1, tags[i].getCloseStart(), getHTMLSufixHead(globalContext.getStaticConfig(), template) + new String(outStream.toByteArray()) + pageModules + pageCss);
				}

				/* title */
				if (!isMail) {
					if (tags[i].getName().equalsIgnoreCase("title")) {
						titleFound = true;
						if (tags[i].getCloseStart() - tags[i].getOpenEnd() > 1) { // if content in title tag
							if (!tags[i].getInside(content).toLowerCase().contains("title")) {
								remplacement.addReplacement(tags[i].getCloseStart(), tags[i].getCloseStart(), " - <%=currentTitle%>");
							}
						} else {
							remplacement.addReplacement(tags[i].getCloseStart(), tags[i].getCloseStart(), "<%=currentTitle%> | ${info.globalTitle}");
						}
					}
				}

				/* resource */
				String srcValue = attributes.get("src");
				if ((srcValue != null)) {
					resources.add(srcValue);
					if (tags[i].getName().equalsIgnoreCase("script")) {
						// if (!srcValue.toLowerCase().startsWith("https")) { //
						// restore https because sometime template contains
						// reference to https://ajax.googlecode.com...
						String newLinkGeneratorIf = "<%if (!XHTMLHelper.alreadyInserted(ctx, \"" + srcValue + "\")) {%>";
						if (!StringHelper.isURL(srcValue) && !srcValue.trim().startsWith("${")) {
							attributes.put("src", "<%=URLHelper.createStaticTemplateURL(ctx,\"/" + srcValue + "\")%>");
						}
						remplacement.addReplacement(tags[i].getOpenStart(), tags[i].getOpenEnd() + 1, newLinkGeneratorIf + tags[i].toString() + "<%}%>");
						if (!tags[i].isAutoClose()) {
							newLinkGeneratorIf = "<%if (!XHTMLHelper.alreadyClosedIfOpen(ctx, \"" + srcValue + "\")) {%>";
							remplacement.addReplacement(tags[i].getCloseStart(), tags[i].getCloseEnd() + 1, newLinkGeneratorIf + "</" + tags[i].getName() + "><%}%>");
						}
						// }
					} else {
						if (!StringHelper.isURL(attributes.get("src")) && !attributes.get("src").contains("${")) {
							attributes.put("src", "<%=URLHelper.createStaticTemplateURL(ctx,\"/" + srcValue + "\")%>");
						}
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

			if (!titleFound && !isMail) {
				if (tagHead != null) {
					remplacement.addReplacement(tagHead.getCloseStart(), tagHead.getCloseStart(), "<title><%=currentTitle%> | ${info.globalTitle}</title>");
				}
			}
			
			// src url
			int urlIndex = content.indexOf("url(");
			while (urlIndex >= 0 && !content.contains("url(#")) {
				int closeIndex = content.substring(urlIndex).indexOf(')') + urlIndex;
				String url = content.substring(urlIndex + 4, closeIndex).trim();
				if (url.startsWith("'")) {
					url = url.substring(1, url.length() - 1);
				}
				resources.add(url);
				String newURL = url;
				if (!StringHelper.isURL(url) && !url.contains("${")) { // change
																		// URL
																		// only
																		// if no
																		// jstl
																		// inside
																		// url
					newURL = "'<%=URLHelper.createStaticTemplateURL(ctx,\"" + url + "\")%>'";
				}
				remplacement.addReplacement(urlIndex + 4, closeIndex, newURL);
				urlIndex = content.indexOf("url(", closeIndex);
			}
			String newContent = getJSPHeader(globalContext.getServletContext()) + "<c:if test=\"${not contentContext.pageAssociation}\">" + remplacement.start(content);

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

			newContent = newContent + "</c:if>"; // close pageAssociation test
													// just after body close
			if (jspFile != null) {
				if (globalContext.getStaticConfig().isCompressJsp()) {
					newContent = XHTMLHelper.compress(newContent);
				}
				ResourceHelper.writeStringToFile(jspFile, newContent, ContentContext.CHARACTER_ENCODING);
			}
		} catch (BadXMLException e) {
			if (i18nAccess != null) {
				String[][] params = new String[][] { { "line", "" + e.getLocalisation() }, { "tag", e.getTag() } };
				String msg = i18nAccess.getText("template.error.xml", params);
				GenericMessage outMsg = new GenericMessage(msg, GenericMessage.ERROR);
				messages.add(outMsg);
				ResourceHelper.writeStringToFile(jspFile, outMsg.getMessage(), ContentContext.CHARACTER_ENCODING);
			} else {
				ResourceHelper.writeStringToFile(jspFile, e.getMessage(), ContentContext.CHARACTER_ENCODING);
			}
			e.printStackTrace();
		}

		return depth + 1;

	}

	public static int convertHTMLtoMail(File htmlFile, Template template, File jspFile) throws IOException, BadXMLException {
		return convertHTMLtoJSP(null, template, null, htmlFile, jspFile, Collections.EMPTY_MAP, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, null, null, true, null);
	}

	public static int convertHTMLtoTemplate(GlobalContext globalContext, Template template, File htmlFile, File jspFile, Map<String, String> tagsID, List<String> areas, List<String> resources, List<TemplatePlugin> templatePlugins, List<String> ids, boolean isMailing, String fontIncluding) throws IOException, BadXMLException {
		return convertHTMLtoJSP(globalContext, template, null, htmlFile, jspFile, tagsID, areas, resources, templatePlugins, null, ids, isMailing, fontIncluding);
	}

	public static int convertHTMLtoTemplate(GlobalContext globalContext, Template template, I18nAccess i18nAccess, File htmlFile, File jspFile, Map<String, String> tagsID, List<String> areas, List<String> resources, List<TemplatePlugin> templatePlugins, List<GenericMessage> messages, String fontIncluding) throws IOException, BadXMLException {
		return convertHTMLtoJSP(globalContext, template, i18nAccess, htmlFile, jspFile, tagsID, areas, resources, templatePlugins, messages, null, false, fontIncluding);
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
	public static List<String> getAllParentName(TagDescription[] allTags, TagDescription tag) {
		List<String> outParents = new LinkedList<String>();

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
		out.append("<%if (ctx.isAsViewMode() && !ctx.isPageAssociation() && !ctx.getCurrentTemplate().isMailing()) {%>");
		out.append("<a id=\"jv_escape_menu\" href=\"#" + contentId + "\"><%=i18nAccess.getViewText(\"wai.to_content\")%></a>");
		out.append("<style>#jv_escape_menu {position: absolute; top:-100px;} #jv_escape_menu:focus {position: fixed; margin: 0.5rem; padding: 0.5rem; color: #000; background-color: #000; font-size: 16px; border-radius: 5px;}</style>");
		out.append("<%}%>");
		out.close();
		return outString.toString();
	}

	private static String getGoogleAnalyticsCode() throws IOException {

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		/*
		 * out.println("<script type=\"text/javascript\">"); out.println(
		 * "var gaJsHost = ((\"https:\" == document.location.protocol) ? \"https://ssl.\" : \"http://www.\");"
		 * ); out.println(
		 * "document.write(unescape(\"%3Cscript src='\" + gaJsHost + \"google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E\"));"
		 * ); out.println("</script>");
		 * out.println("<script type=\"text/javascript\">"); out.println("try {");
		 * out.println(
		 * "var pageTracker = _gat._getTracker(\"<%=globalContext.getGoogleAnalyticsUACCT()%>\");"
		 * ); out.println("pageTracker._trackPageview();");
		 * out.println("} catch(err) {}</script>");
		 */

		// out.print("<%if (globalContext.getGoogleAnalyticsUACCT().length() > 4 &&
		// ctx.isTrackingContext()) {%><script type=\"text/javascript\">");
		// out.println("var _gaq = _gaq || [];");
		// out.println("_gaq.push(['_setAccount',
		// '<%=globalContext.getGoogleAnalyticsUACCT()%>']);");
		// out.println("_gaq.push(['_trackPageview']);");
		// out.println(" (function() {");
		// out.println(" var ga = document.createElement('script'); ga.type =
		// 'text/javascript'; ga.async = true;");
		// out.println(" ga.src = ('https:' == document.location.protocol ?
		// 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';");
		// out.println(" var s = document.getElementsByTagName('script')[0];
		// s.parentNode.insertBefore(ga, s);");
		// out.println(" })();");
		// out.print("</script><%}%>");

		// out.print("<%if (globalContext.getGoogleAnalyticsUACCT().length() > 4 &&
		// ctx.isTrackingContext()) {%><script>");
		// out.println("
		// (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){");
		// out.println("(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new
		// Date();a=s.createElement(o),");
		// out.println("m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)");
		// out.println("})(window,document,'script','//www.google-analytics.com/analytics.js','ga');");
		//
		// out.println("ga('create', '<%=globalContext.getGoogleAnalyticsUACCT()%>',
		// 'auto');");
		// out.println("ga('send', 'pageview');");
		//
		// out.println("</script><%}%>");

		out.print("<%if (globalContext.getGoogleAnalyticsUACCT().length() > 4 && ctx.isTrackingContext()) {%>");
		out.print("<jsp:include page=\"${info.rootTemplateFolder}/jsp/googleanalytics.jsp\" />");
		out.println("<%}%>");

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
			if (tag.getName().equalsIgnoreCase("title")) {
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

	private static String getHTMLPrefixHead(GlobalContext globalContext, PrefixHeadContext context, boolean isMail) throws IOException {

		StringWriter outString = new StringWriter();
		BufferedWriter out = new BufferedWriter(outString);
		
		/** forward **/
		out.append("<%if (!StringHelper.isEmpty(currentPage.getForward(ctx))) {%><!--FRW--><meta http-equiv=\"refresh\" content=\"0; url=<%=currentPage.getForward(ctx)%>\" /><%}%>");

		if (!isMail) {			
			out.append("<%if (ctx.isInteractiveMode() && !ctx.isPreviewOnly()) {%>");
			out.append("<style type=\"text/css\">@font-face {font-family: \"javloFont\"; src: url('${info.staticRootURL}fonts/javlo-italic.ttf') format(\"truetype\");}</style>");
			out.append("<%}%>");
			out.append("<%if (ctx.getRenderMode() != ContentContext.PAGE_MODE && ctx.getDevice().isJavascript()) {%>");
			out.newLine();
			out.append("<script>");
			out.newLine();
			out.append("<!--");
			out.newLine();
			out.append("var sLanguage = '<%=ctx.getRequestContentLanguage()%>';");
			out.newLine();
			out.append("var server = '<%=URLHelper.createStaticURL(ctx, \"/\")%>';");
			out.newLine();
			out.append("var currentUrl = '<%=URLHelper.createURL(ctx)%>';");
			out.newLine();
			out.append("var actionUrl = '<%=URLHelper.createActionURL(ctx, \"\")%>';");
			out.newLine();
			out.append("-->");
			out.newLine();
			out.append("</script>");
			out.newLine();
			out.append("<%}%>");
		}
		
		out.append(getTakeScreenShortCode(globalContext));

		
		if (context.isKeyword() && !isMail) {
			out.append("<%if (currentPage.getKeywords(ctx).length()>0){%>");
			out.newLine();
			out.append("<meta name=\"keywords\" content=\"<%=currentPage.getKeywords(ctx)%>\" />");
			out.newLine();
			out.append("<%}%>");
		}
		if (context.isDescription()) {
			out.append("<%if (currentPage.getMetaDescription(ctx).length()>0){%><meta name=\"description\" content=\"${info.pageDescriptionForAttribute}\" />");
			out.newLine();
			out.append("<%}%>");
		}
		if (!isMail) {
			out.newLine();
			out.append("<%=XHTMLNavigationHelper.getRSSHeader(ctx, currentPage)%>");
			out.newLine();
			out.append("<link rel=\"shortcut icon\" type=\"image/ico\" href=\"<%=URLHelper.createStaticURL(ctx,\"/favicon.ico\")%>\" />");
			out.newLine();
			out.append("<%if (ctx.getRenderMode() != ContentContext.VIEW_MODE) {%>");
			out.append("<meta name=\"ROBOTS\" content=\"NONE\" />");
			out.append("<%}%>");
			out.newLine();			
		}
		
		out.append("<%if (ctx.isInteractiveMode()) {%>");
		out.newLine();
		if (globalContext.getStaticConfig().getJSLibPreview() == null) {
			out.append("<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, \"/js/lib/jquery-1.8.3.min.js\")%>");
			out.newLine();
			out.append("<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, \"/js/lib/jquery-ui-1.9.2.custom.min.js\")%>");
			out.append("<%if (ctx.isPreview()) {%>");
			out.newLine();
			out.append("<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, \"/js/lib/jquery.colorbox-min.js\")%>");
			out.newLine();
			out.append("<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, \"/js/lib/jquery.cookie.js\")%>");
			out.newLine();
			out.append("<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, \"/css/lib/colorbox/colorbox.css\")%>");
			out.newLine();
			out.append("<%  }%>");
		} else {
			out.append("<%=XHTMLHelper.renderHeaderResourceInsertionWithoutalreadyTest(ctx, \"" + globalContext.getStaticConfig().getJSLibPreview() + "\")%>");
			out.newLine();
			out.append("<script>var pjq = jQuery.noConflict(true);</script>");
		}
		out.newLine();
		out.append("<%EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());%>");
		out.append("<%}%>");

		out.close();

		return outString.toString();
	}

	private static String getTakeScreenShortCode(GlobalContext globalContext) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<%if (ctx.isTakeScreenShort()) {%><script src=\"<%=URLHelper.createStaticURL(ctx,\"" + globalContext.getStaticConfig().getHTML2Canvas() + "\")%>\"></script>");
		out.println("<script>");
		out.println("__uploadScreenshot = function(canvas) {");
		out.println("var img    = canvas.toDataURL('image/png');");
		out.println("var ajaxURL = server+'?webaction=data.uploadscreenshot';");
		out.println("<c:if test='${not empty param.____screenshot_page}'>");
		out.println("ajaxURL = ajaxURL+'&page=${param.____screenshot_page}';");
		out.println("</c:if>");
		out.println("document.body.innerHTML = '<div style=\"margin: 150px auto; width: 50%; padding: 30px; border-radius: 3px; background-color: #aaaaaa; color: #ffffff; font-size: 32px; text-align: center;\">upload screenshot...</div>';");
		out.println("var fd=new FormData();");
		out.println("var fieldName = 'screenshot';");
		out.println("var blob = new Blob([img], {type: 'image/png'});");
		out.println("fd.append(fieldName,blob);");
		out.println("var request = new XMLHttpRequest();");
		out.println("request.open(\"POST\", ajaxURL, false);");
		out.println("request.send(fd);");
		out.println("window.close();");
		out.println("return false;");
		out.println("}");
		out.println("window.onload = function() { setTimeout(function() { html2canvas(document.querySelector('body')).then(canvas => {__uploadScreenshot(canvas)})}, 1200)}");
		out.println("</script><%}%>");
		out.close();
		return new String(outStream.toByteArray());

	}

	private static String getHTMLSufixHead(StaticConfig staticConfig, Template template) throws IOException {

		StringWriter outString = new StringWriter();
		BufferedWriter out = new BufferedWriter(outString);
		
		out.append("<%=(ctx.isInteractiveMode() ? \"<link rel=\\\"stylesheet\\\" type=\\\"text/css\\\" href=\\\"\"+URLHelper.createStaticURL(ctx,\"css/main_edit_and_preview.css\")+\"?ts=\"+infoBean.getTs()+\"\\\" />\" : \"\")  %>");

		if (template.isEditable()) {
/*			out.append("<%if (StringHelper.isTrue(request.getParameter(\"_display-zone\"))) {%><link rel=\"stylesheet\" type=\"text/css\" href=\"<%=URLHelper.createStaticURL(ctx,\"/css/editable/edit_editable.css\")+\"?ts=\"+infoBean.getTs()%>\" /><%}%>");
			out.newLine(); */
			out.append("<%if (StringHelper.isTrue(request.getParameter(\"_display-zone\"))) {%><script src=\"<%=URLHelper.createStaticURL(ctx,\"/js/editable/edit_editable.js\")%>\"></script><%}%>");
			out.newLine();
		}

		out.append("<%if (StringHelper.isTrue(request.getParameter(\"_display-zone\"))) {%><link rel=\"stylesheet\" type=\"text/css\" href=\"<%=URLHelper.createStaticURL(ctx,\"/css/preview/edit_preview.css\")+\"?ts=\"+infoBean.getTs()%>\" /><%}%>");
		
		out.newLine();
		out.append("<%if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE || ctx.getRenderMode() == ContentContext.TIME_MODE && !ctx.isPreviewOnly()) {");
		out.newLine();
		out.append("EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());%>");
		out.newLine();
		String previewCSS = staticConfig.getCssPreview();
		out.append("<%=(ctx.isInteractiveMode() ? \"<link rel=\\\"stylesheet\\\" type=\\\"text/css\\\" href=\\\"\"+URLHelper.createStaticURL(ctx,\"" + previewCSS + "\")+\"?ts=\"+infoBean.getTs()+\"\\\" />\" : \"\")  %>");
		
		
		
		/*out.newLine();
		out.append("<%=(ctx.isInteractiveMode() && !StringHelper.isEmpty(infoBean.getPreviewTemplateModeURL()) ? \"<link rel=\\\"stylesheet\\\" type=\\\"text/css\\\" href=\\\"\"+infoBean.getPreviewTemplateModeURL()+\"?ts=\"+infoBean.getTs()+\"\\\" />\" : \"\")  %>");*/
		out.newLine();
		out.append("<%=(ctx.isInteractiveMode() ? \"<script src=\\\"\"+URLHelper.createStaticURL(ctx,\"" + staticConfig.getJSPreview() + "\")+\"\\\"></script>\" : \"\")  %>");
		if (!StringHelper.isEmpty(staticConfig.getHTML2Canvas())) {
			out.append("<%=(ctx.isInteractiveMode() ? \"<script src=\\\"\"+URLHelper.createStaticURL(ctx,\"" + staticConfig.getHTML2Canvas() + "\")+\"\\\"></script>\" : \"\")  %>");
		}
		out.append("<%=(ctx.isInteractiveMode() ? \"<script src=\\\"\"+URLHelper.createStaticURL(ctx,\"" + staticConfig.getJSBootStrap() + "\")+\"\\\"></script>\" : \"\")  %>");
		out.newLine();
		if (staticConfig.getJSLibPreview() == null) {
			out.append("<%=(ctx.isInteractiveMode() ? \"<script src=\\\"\"+URLHelper.createStaticURL(ctx,\"/js/edit/ajax.js\")+\"\\\"></script>\" : \"\")  %>");
			out.newLine();
			out.append("<%=(ctx.isInteractiveMode() ? \"<script src=\\\"\"+URLHelper.createStaticURL(ctx,\"/js/edit/core.js\")+\"\\\"></script>\" : \"\")  %>");
			out.newLine();
			out.append("<%=(ctx.isInteractiveMode() ? \"<script src=\\\"\"+URLHelper.createStaticURL(ctx,\"/js/lib/enscroll-0.6.0.min.js\")+\"\\\"></script>\" : \"\")  %>");
			out.newLine();
		}
		out.append("<%if ((ctx.isInteractiveMode())&&(security.haveRight((User)editCtx.getUserPrincipal(), \"update\"))) {%><script>");
		out.newLine();
		out.append("var ajaxURL = \"<%=URLHelper.createAjaxURL(ctx)%>\";");
		out.newLine();
		out.append("var currentURL = \"<%=URLHelper.createURL(ctx)%>\";");
		out.newLine();
		out.append("var currentURLWidthDevice = \"${info.currentURLWidthDevice}\";");
		out.newLine();
		out.append("var currentAjaxURLWidthDevice = \"${info.currentAjaxURLWidthDevice}\";");
		out.newLine();
		out.append("<%String editPreviewURLJava = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE)); if (editPreviewURLJava.contains(\"?\")) { editPreviewURLJava=editPreviewURLJava+\"&\"; } else { editPreviewURLJava=editPreviewURLJava+\"?\"; }%>");
		out.newLine();
		out.append("var editPreviewURL = \"<%=editPreviewURLJava%>module=content&webaction=edit.editPreview&" + ContentContext.PREVIEW_EDIT_PARAM + "=true\";");
		out.newLine();
		out.append("</script><%}%>");

		out.append("<%}%>");
		out.newLine();
		out.append("<%if (currentPage.getHeaderContent(ctx) != null) {%><%=currentPage.getHeaderContent(ctx)%><%}%>");

		out.close();

		return outString.toString();
	}

	private static String getJSPHeader(ServletContext application) throws IOException {
		File headerFile = new File(ResourceHelper.getRealPath(application, "/jsp/view/page/header.jsp"));
		if (!headerFile.exists()) {
			return "<!-- header file not found : '" + headerFile + "' -->";
		} else {
			return ResourceHelper.getFileContent(headerFile);
		}
	}

	private static String getPreviewCode(ServletContext application) throws FileNotFoundException, IOException {
		// File headerFile = new
		// File(application.getRealPath("/jsp/view/page/preview.jsp"));
		File headerFile = new File(ResourceHelper.getRealPath(application, "/jsp/view/page/preview.jsp"));
		if (!headerFile.exists()) {
			return "<!-- preview file not found : '" + headerFile + "' -->";
		} else {
			return ResourceHelper.getFileContent(headerFile);
		}
	}

	private static String getResetTemplate() throws IOException {
		StringWriter outString = new StringWriter();
		BufferedWriter out = new BufferedWriter(outString);
		/*
		 * out.append(
		 * "<%if (request.getParameter(PageConfiguration.FORCE_TEMPLATE_PARAM_NAME) != null) {%>"
		 * ); out.append("<div id=\"_reset_template\">"); out.append(
		 * "<a href=\"<%=URLHelper.createURLNoForceTemplate(ctx)%>\"><%=i18nAccess.getViewText(\"template.clean-force-template\")%> : <%=request.getParameter(PageConfiguration.FORCE_TEMPLATE_PARAM_NAME)%></a>"
		 * ); out.append("<div class=\"_reset_template-close\">"); out.append(
		 * "<%=XHTMLHelper.getIconesCode(ctx, \"close.gif\", \"start\")%>"); out.append(
		 * "<a href=\"#\" onclick=\"document.getElementById('_reset_template').style.visibility='hidden';; return false;\">"
		 * ); out.append("</a></div>"); out.append("</div>"); out.append("<%}%>");
		 * out.newLine();
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
					while ((xml.charAt(i) != '>' || inCharSequence) && (xml.charAt(i) != '<') && (i < xml.length())) {
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

							/** manage auto close tag **/
							while (openedTag != null && !tagName.equals(openedTag.getName()) && autoCloseTag.contains(openedTag.getName().toLowerCase())) {
								openedTag.setCloseStart(openedTag.getOpenStart());
								openedTag.setCloseEnd(openedTag.getOpenEnd());
								openedTag = stack.pop();
							}
							if (openedTag != null) {
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
							} else {
								System.out.println("-- bad close tag : " + tagName);
							}
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
						// close tag
						if (!stack.empty()) {
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