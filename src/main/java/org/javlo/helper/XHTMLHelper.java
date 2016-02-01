/*
 * Created on 07-d?c.-2003
 */
package org.javlo.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.form.FormComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.InfoBean;
import org.javlo.ecom.Basket;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.XMLManipulationHelper.BadXMLException;
import org.javlo.helper.XMLManipulationHelper.TagDescription;
import org.javlo.helper.Comparator.DoubleArrayComparator;
import org.javlo.helper.Comparator.MapEntryComparator;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.ListService;
import org.javlo.service.RequestService;
import org.javlo.service.ReverseLinkService;
import org.javlo.user.IUserInfo;
import org.javlo.user.User;
import org.javlo.utils.SuffixPrefix;
import org.javlo.ztatic.StaticInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.safety.Whitelist;

/**
 * This class is a helper for construct XHTML code.
 * 
 * @author pvandermaesen
 */
public class XHTMLHelper {

	/**
	 * create a static logger.
	 */
	protected static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(XHTMLHelper.class.getName());

	private static final String[] TEXT_COLORS = { "#005", "#050", "#500", "#505", "#550", "#055", "#555" };

	public static final List<String> WEB_FONTS = Arrays.asList(new String[] { "Arial, sans-serif", "Courier, monospace, serif", "Myriad Pro, Myriad Pro Regular, PT Sans, sans-serif", "Times New Roman, serif", "Verdana, Geneva, sans-serif", "Open Sans, sans-serif" });

	private static final Pattern CSS_IMPORT_PATTERN = Pattern.compile("@import\\s+" +

	// optional 'url(' part (non capturing subpattern) with optional quote
			"(?:url\\(\\s*)?" + "[\"']?" +

	// file path ending with '.?ss' in capturing subpattern 1
	// word characters, slashes, dash, underscore, dot,
	// colon and question mark (possible for absolute urls) are allowed
	"([\\w\\\\/\\-_.:?]+?\\.?ss)" +

	// the rest of the line until semicolon or line break
			"[^;$]*?(;|$)", Pattern.MULTILINE);

	public static String _textToXHTML(String text, boolean popup) {
		String res = autoLink(text);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		ByteArrayInputStream inStream = new ByteArrayInputStream(res.getBytes());
		BufferedReader in = new BufferedReader(new InputStreamReader(inStream));

		String prefix = "";
		String suffix = "";

		String newParagraph;
		try {
			newParagraph = in.readLine();
			while (newParagraph != null) {
				String nextParagraph = in.readLine();
				if (nextParagraph != null && nextParagraph.trim().length() == 0 && newParagraph.trim().length() > 0) {
					prefix = "</p><p>";
					suffix = "</p>";
					nextParagraph = in.readLine();
				} else {
					prefix = "";
					if (nextParagraph != null) {
						suffix = "<br />";
					} else {
						suffix = "";
					}
				}
				if (newParagraph.trim().length() > 0) {
					out.println(prefix + newParagraph + suffix);
				} else {
					if (nextParagraph != null) {
						out.println("<br />");
					}
				}
				newParagraph = nextParagraph;
			}

			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return new String(outStream.toByteArray()).replaceAll("  ", "&nbsp;&nbsp;");
	}

	public static boolean containsLink(String content) {
		if (content == null) {
			return false;
		} else {
			if (content.toLowerCase().contains("http://")) {
				return true;
			} else {
				return !content.equals(autoLink(content));
			}
		}
	}

	public static String autoLink(String content) {
		return autoLink(content, false, null);
	}

	public static String autoLink(String content, boolean notfollow) {
		return autoLink(content, notfollow, null);
	}

	public static String autoLink(String content, GlobalContext globalContext) {
		return autoLink(content, false, globalContext);
	}

	public static String autoLink(String content, boolean notFollow, GlobalContext globalContext) {
		if (content == null) {
			return "";
		}
		StringWriter out = new StringWriter();
		BufferedWriter writer = new BufferedWriter(out);
		BufferedReader reader = new BufferedReader(new StringReader(content));
		try {
			String line = reader.readLine();
			while (line != null) {

				String[] splitLine = StringHelper.splitStaySeparator(line, " ,;()[]{}<>\n");

				boolean inLink = false;
				boolean inTag = false;
				for (String element : splitLine) {
					if (element.toLowerCase().startsWith("<")) {
						inTag = true;
					} else if (element.toLowerCase().startsWith(">")) {
						inTag = false;
					}
					if (inTag) {
						if (element.equalsIgnoreCase("a")) {
							inLink = true;
						} else if (element.toLowerCase().startsWith("/a")) {
							inLink = false;
						}
					}
					if (!inLink) {
						writer.append(createHTMLLink(element, notFollow, globalContext));
					} else {
						writer.append(element);
					}
				}
				line = reader.readLine();
				if (line != null) {
					writer.newLine();
				}
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toString();
	}

	private static String createHTMLLink(String url, boolean notFollow, GlobalContext globalContext) {
		String suffix = "";
		if (url.endsWith(".")) {
			url = url.substring(0, url.length() - 1);
			suffix = ".";
		}
		String outXHTML = url;
		String target = "";
		if (globalContext != null && globalContext.isOpenExternalLinkAsPopup(url)) {
			target = " target=\"blank\"";
		}

		String cssClass = "auto-link";
		if (url.contains(".")) {
			cssClass = cssClass + " web file-" + StringHelper.getFileExtension(url);
		}

		String notFollowAttr = "";
		if (notFollow) {
			notFollowAttr = " rel=\"nofollow\"";
		}

		if (url.contains("@")) {
			if (PatternHelper.MAIL_PATTERN.matcher(url).matches()) {
				cssClass = "auto-link mail";
				outXHTML = "<a class=\"" + cssClass + "\" href=\"mailto:" + url.trim() + "\">" + url + "</a>";
			}
		} else if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://")) {
			outXHTML = "<a class=\"" + cssClass + "\" href=\"" + url.trim() + "\"" + target + "" + notFollowAttr + ">" + url + "</a>";
		} else if (url.startsWith("www.")) {
			outXHTML = "<a class=\"" + cssClass + "\" href=\"http://" + url.trim() + "\"" + target + "" + notFollowAttr + ">" + url + "</a>";
		}
		return outXHTML + suffix;
	}

	public static String escapeXHTML(String xhtml) {
		if (xhtml == null) {
			return "";
		}
		return StringEscapeUtils.escapeHtml(xhtml);
	}

	public static String escapeXML(String xhtml) {
		if (xhtml == null) {
			return "";
		}
		return StringEscapeUtils.escapeXml(xhtml);
	}

	public static String extractTitle(String xhtml) {
		int startTitle = xhtml.toLowerCase().indexOf("<title>");
		int endTitle = xhtml.toLowerCase().indexOf("</title>");
		if ((startTitle < 0) || (endTitle < 0) || (endTitle < startTitle)) {
			return null;
		} else {
			return xhtml.substring(startTitle + 7, endTitle);
		}
	}

	public static String getCheckbox(ContentContext ctx, String field, FormComponent formComponent) throws ResourceNotFoundException {

		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		String label = formComponent.getViewText(ctx, "form." + field);
		String value = formComponent.getValue(ctx, field, "");

		String addedTag = "";
		if (StringHelper.isTrue(value)) {
			addedTag = addedTag + " checked=\"checked\" ";
		}
		value = "true"; // if false there are nothing in the request

		out.print("<input class=\"in-row\" type=\"checkbox\" name=\"");
		out.print(field);
		out.print("\" " + addedTag + "value=\"");
		out.print(value + "\"/>&nbsp;" + label);

		out.close();
		return res.toString();
	}

	public static String getCheckbox(String field, boolean value) throws ResourceNotFoundException {

		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		String addedTag = "";
		if (value) {
			addedTag = addedTag + " checked=\"checked\" ";
		}

		out.print("<input id=\"" + field + "\"type=\"checkbox\" name=\"");
		out.print(field);
		out.print("\" " + addedTag + "/>");

		out.close();
		return res.toString();
	}

	public static String getComponentPreffixSufixOneSelect(ContentContext ctx, IContentVisualComponent inComp) {
		StringBuffer outXHTML = new StringBuffer();
		if (inComp.getStyleList(ctx) != null) {
			String xhtmlID = "style_select_" + inComp.getId();
			outXHTML.append("<span class=\"select-style\"> | ");
			outXHTML.append("<label for=\"");
			outXHTML.append(xhtmlID);
			outXHTML.append("\">");
			outXHTML.append(inComp.getStyleTitle(ctx));
			outXHTML.append(" : </label>");
			String[][] listContent = new String[inComp.getStyleList(ctx).length][];
			for (int i = 0; i < listContent.length; i++) {
				listContent[i] = new String[2];
				listContent[i][0] = inComp.getStyleList(ctx)[i];
				listContent[i][1] = inComp.getStyleLabelList(ctx)[i];
			}
			outXHTML.append(getInputOneSelectInternal(xhtmlID, xhtmlID, listContent, inComp.getStyle(ctx), null, null, null, false));
			outXHTML.append("</span>");
		}
		return outXHTML.toString();
	}

	public static String getComponentSelectBox(ContentContext ctx, String inputName, int complexityLevel) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, ConfigurationException {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		List<IContentVisualComponent> comps = ComponentFactory.getGlobalContextComponent(ctx, complexityLevel);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		out.println("<select name=\"" + inputName + "\">");
		for (IContentVisualComponent comp : comps) {
			globalContext.getComponents();
			String selected = "";
			if (comp.getType().equals(editCtx.getActiveType())) {
				selected = "selected=selected ";
			}

			out.println("<option " + selected + "value=\"" + comp.getType() + "\">" + i18nAccess.getText("content." + comp.getType()) + "</option>");
		}
		out.println("</select>");
		out.close();
		return writer.toString();
	}

	/**
	 * create a drop down from a map.
	 * 
	 * @param name
	 *            the name of the field.
	 * @param map
	 *            the map with keys and values.
	 * @param value
	 *            the current value of the field.
	 * @return XHTML code with a dropdown.
	 */
	public static String getDropDownFromMap(String name, Map map, String value) {
		return getDropDownFromMap(name, map, value, null, false);
	}

	public static String getDropDownFromMap(String name, Map map, String value, String emptyName, boolean sortValue) {
		return getDropDownFromMap(name, map, value, emptyName, sortValue, null);
	}

	/**
	 * create a drop down from a map.
	 * 
	 * @param name
	 *            the name of the field.
	 * @param map
	 *            the map with keys and values.
	 * @param value
	 *            the current value of the field.
	 * @param emptyName
	 *            the name of the first empty element (empty as value), if null
	 *            no empty element.
	 * @return XHTML code with a dropdown.
	 */
	public static String getDropDownFromMap(String name, Map map, String value, String emptyName, boolean sortValue, String cssClass) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(outStream);

		if (map == null) {
			map = new HashMap();
		}

		List<Map.Entry> entriesList = new LinkedList<Map.Entry>(map.entrySet());
		if (sortValue) {
			Collections.sort(entriesList, new MapEntryComparator(true));
		}
		if (cssClass != null) {
			out.println("<select class=\"select\" id=\"" + name + "\" name=\"" + name + "\">");
		} else {
			out.println("<select class=\"select " + cssClass + "\" id=\"" + name + "\" name=\"" + name + "\">");
		}
		if (emptyName != null) {
			out.print("<option value=\"\">" + emptyName + "</option>");
		}
		for (Map.Entry entry : entriesList) {
			String key = (String) entry.getKey();
			out.print("<option");
			out.print(" value=\"");
			out.print(key);
			out.print("\"");
			if ((value != null) && (value.equals(key))) {
				out.print(" selected=\"selected\" ");
			}
			out.print(">");
			out.print(entry.getValue());
			out.println("</option>");
		}
		out.println("</select>");
		out.flush();
		out.close();
		return new String(outStream.toByteArray());

	}

	public static String getErrorMessage(ContentContext ctx, String field, GenericMessage message) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		if (message != null) {
			String imageURL = URLHelper.createStaticURL(ctx, "/images/error.gif");
			if (message.getType() != GenericMessage.ERROR) {
				imageURL = URLHelper.createStaticURL(ctx, "/images/valid.gif");
			}
			String msg = message.getMessage();

			out.print("<img src=\"");
			out.print(imageURL);
			out.print("\" title=\"");
			out.print(msg);
			out.print("\" alt=\"");
			out.print(msg);
			out.print("\"/>");
			out.print(msg);
		} else {
			out.print("&nbsp;");
		}

		out.close();
		return res.toString();
	}

	public static String getFileBigIcone(ContentContext ctx, String fileName) {
		String imageURL;
		String fileExt = StringHelper.getFileExtension(fileName).toLowerCase();
		String iconeName = fileExt + ".png";
		ServletContext application = ctx.getRequest().getSession().getServletContext();
		File iconeFile = new File(application.getRealPath("/images/minetypes/64x64/" + iconeName));
		if (iconeFile.exists()) {
			imageURL = URLHelper.createStaticURL(ctx, "/images/minetypes/64x64/" + iconeName);
		} else {
			imageURL = URLHelper.createStaticURL(ctx, "/images/minetypes/64x64/unknow.png");
		}
		return imageURL;

	}

	public static String getFileIcone(ContentContext ctx, String fileName) {
		String outXHTML = "";
		String fileExt = StringHelper.getFileExtension(fileName).toLowerCase();
		if (fileExt.equals("pdf")) {
			String imageURL = URLHelper.createStaticURL(ctx, "/images/minetypes/17x17/pdf.gif");
			outXHTML = "<img src=\"" + imageURL + "\" alt=\"pdf\" lang=\"en\"/>";
		} else if (fileExt.equals("zip")) {
			String imageURL = URLHelper.createStaticURL(ctx, "/images/minetypes/17x17/zip.gif");
			outXHTML = "<img src=\"" + imageURL + "\" alt=\"zip\" lang=\"en\"/>";
		} else if (fileExt.equals("doc")) {
			String imageURL = URLHelper.createStaticURL(ctx, "/images/minetypes/17x17/doc.gif");
			outXHTML = "<img src=\"" + imageURL + "\" alt=\"doc\" lang=\"en\"/>";
		} else if (fileExt.equals("avi") || fileExt.equals("mpg") || fileExt.equals("wmv")) {
			String imageURL = URLHelper.createStaticURL(ctx, "/images/minetypes/17x17/video.png");
			outXHTML = "<img src=\"" + imageURL + "\" alt=\"video\" lang=\"en\"/>";
		} else if (fileExt.equals("mp3") || fileExt.equals("wav")) {
			String imageURL = URLHelper.createStaticURL(ctx, "/images/minetypes/17x17/sound.png");
			outXHTML = "<img src=\"" + imageURL + "\" alt=\"sound\" lang=\"en\" />";
		} else if (fileExt.equals("ppt") || fileExt.equals("pps")) {
			String imageURL = URLHelper.createStaticURL(ctx, "/images/minetypes/17x17/ppt.gif");
			outXHTML = "<img src=\"" + imageURL + "\" alt=\"MS PowerPoint\" lang=\"en\" />";
		}
		return outXHTML;
	}

	public static String getHelpAttribute(String helpMessage) {
		String helpScript = "";
		if (helpMessage != null) {
			if ((helpMessage.trim().length() > 0)) {
				helpScript = " onmouseover=\"return overlib('" + helpMessage.replaceAll("'", "\\\\'") + "');\" onmouseout=\"return nd();\"";
			}
		}
		return helpScript;
	}

	public static String getHelpLink(ContentContext ctx, String uri) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if ((uri != null) && (globalContext.isHelpLink())) {
			String url = URLHelper.mergePath(globalContext.getHelpURL(), uri);
			String imageDown = URLHelper.createStaticURL(ctx, "/images/edit/help.png");
			String imageUp = URLHelper.createStaticURL(ctx, "/images/edit/help.png");
			return "<div class=\"help-url\">" + XHTMLHelper.getImagePopupLink("help", imageDown, imageUp, url) + "</div>";
		}
		return "";
	}

	/**
	 * generate a list of navigation element. replace #id with the page id.
	 * 
	 * @param startTag
	 *            insert before path sample : <option value=#id>.
	 * @param endTag
	 *            insert after path : </option>
	 * @param displayParent
	 *            false for only dispay children and not the parent.
	 * @return a string with XHTML code
	 * @throws Exception
	 */
	public static String getHTMLChildList(MenuElement elem, String selectionPath, String startTag, String startSelectedTag, String endTag, boolean displayParent) throws Exception {
		return getHTMLChildListRecursive(elem, selectionPath, startTag, startSelectedTag, endTag, 0, displayParent);
	}

	private static String getHTMLChildListRecursive(MenuElement elem, String selectionPath, String startTag, String startSelectedTag, String endTag, int depth, boolean display) throws Exception {
		StringBuffer result = new StringBuffer();

		String startTagReplaced = startTag;

		if (display) {

			String workinStartTag = startTag;
			if ((selectionPath != null) && (elem.getPath().equals(selectionPath))) {
				workinStartTag = startSelectedTag;
			}

			if (workinStartTag.indexOf("#id") >= 0) {
				startTagReplaced = workinStartTag.replaceAll("#id", elem.getId());
			}

			if (startTagReplaced.indexOf("#name") >= 0) {
				startTagReplaced = workinStartTag.replaceAll("#name", elem.getName());
			}

			result.append(startTagReplaced);
			StringBuffer currentLine = new StringBuffer();
			for (int i = 0; i < depth; i++) {
				currentLine.append("&nbsp;&nbsp;");
			}
			if (elem.getAllChildren().length == 0) {
				currentLine.append(elem.getName());
			} else {
				currentLine.append(elem.getName());
			}
			result.append(currentLine.toString());
			result.append(endTag);

			depth++;

		}

		Collection<MenuElement> childs = elem.getChildMenuElements();
		for (MenuElement child : childs) {
			result.append(getHTMLChildListRecursive(child, selectionPath, startTag, startSelectedTag, endTag, depth, true));
		}
		return result.toString();
	}

	public static String getIconesCode(ContentContext ctx, String imageName, String alt) {
		return getIconesCode(ctx, imageName, alt, null);
	}

	public static String getIconesCode(ContentContext ctx, String imageName, String alt, String onclick) {
		return getIconesCode(ctx, imageName, alt, onclick, null);
	}

	public static String getIconesCode(ContentContext ctx, String imageName, String alt, String onclick, String style) {
		String url = URLHelper.createStaticURL(ctx, "/images/icones/" + imageName);
		String js = "";
		if (onclick != null) {
			js = " onclick=\"" + onclick + "\"";
		}
		String styleTag = "";
		if (style != null) {
			styleTag = " style=\"" + style + "\"";
		}

		return "<img class=\"icone\" src=\"" + url + "\" alt=\"" + alt + "\"" + styleTag + js + " />";
	}

	public static String getIconesFlag(ContentContext ctx) {
		String url = URLHelper.createStaticURL(ctx, "/images/icones/flag_lang/gif/" + ctx.getContentLanguage() + ".gif");
		return "<img class=\"icone\" src=\"" + url + "\" alt=\"" + ctx.getContentLanguage() + "\" />";
	}

	public static String getImageLabelLink(String name, String imageUp, String imageDown, String link, String label) {
		return getImageLink(name, imageUp, imageDown, link, new String[][] { { "title", label }, { "alt", label } }, false, null);
	}

	public static String getImageLink(String name, String imageUp, String imageDown, String link) {
		return getImageLink(name, imageUp, imageDown, link, new String[0][0], false, null);
	}

	public static String getImageLink(String name, String imageUp, String imageDown, String link, String js) {
		return getImageLink(name, imageUp, imageDown, link, new String[0][0], false, js);
	}

	public static String getImageLink(String name, String imageUp, String imageDown, String link, String[][] attributes) {
		return getImageLink(name, imageUp, imageDown, link, attributes, false, null);
	}

	/**
	 * @param name
	 * @param imageUp
	 * @param imageDown
	 * @param link
	 * @param attributes
	 * @param js
	 * @return
	 */
	public static String getImageLink(String name, String imageUp, String imageDown, String link, String[][] attributes, boolean popup, String js) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);
		out.print("<a href=\"");
		out.print(link);
		if (popup) {
			out.println("\" target=\"_blank\">");
		} else {
			out.println("\">");
		}
		out.print("<img src=\"");
		out.print(imageUp);
		out.print("\" onmouseover=\"this.src='");
		out.print(imageDown);
		out.print("'\" onmouseout=\"this.src='");
		out.print(imageUp);
		out.print("'\"");
		boolean altFound = false;
		for (String[] attribute : attributes) {
			if (attribute[0].trim().equalsIgnoreCase("alt")) {
				altFound = true;
			}
			out.print(" ");
			out.print(attribute[0]);
			out.print("=\"");
			out.print(attribute[1]);
			out.print("\"");
		}
		if (!altFound) {
			out.print(" alt=\"\" ");
		}
		if (js != null) {
			out.print(" onclick=\"");
			out.print(js);
			out.print("\" ");
		}
		out.print(" />");
		out.println("</a>");
		return res.toString();
	}

	public static String getImageLink(String imageUp, String imageDown, String link, String[][] attributes, String js) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);
		out.print("<div class=\"image-link\">");
		out.print("<a href=\"");
		out.print(link);
		out.println("\">");
		out.print("<img src=\"");
		out.print(imageUp);
		out.print("\" onmouseover=\"this.src='");
		out.print(imageDown);
		out.print("'\" onmouseout=\"this.src='");
		out.print(imageUp);
		out.print("'\"");
		for (String[] attribute : attributes) {
			out.print(" ");
			out.print(attribute[0]);
			out.print("=\"");
			out.print(attribute[1]);
			out.print("\"");
		}
		if (js != null) {
			out.print(" onclick=\"");
			out.print(js);
			out.print("\" ");
		}
		out.print("/>");
		out.println("</a></div>");
		return res.toString();
	}

	public static String getImagePopupLink(String name, String imageUp, String imageDown, String link) {
		return getImageLink(name, imageUp, imageDown, link, new String[0][0], true, null);
	}

	/**
	 * call .toString on all object, create String array and call the same
	 * method with array as param.
	 * 
	 * @param name
	 * @param content
	 * @param value
	 * @return
	 */
	public static String getInputMultiSelect(String name, Collection content, Collection value) {

		String[] contentArray = new String[content.size()];
		Iterator contentIt = content.iterator();
		for (int i = 0; i < contentArray.length; i++) {
			contentArray[i] = contentIt.next().toString();
		}

		String[] contentValue = new String[value.size()];
		Iterator valueIt = value.iterator();
		for (int i = 0; i < contentValue.length; i++) {
			contentValue[i] = valueIt.next().toString();
		}

		return getInputMultiSelect(name, contentArray, contentValue);
	}

	/**
	 * call .toString on all object, create String array and call the same
	 * method with array as param.
	 * 
	 * @param name
	 * @param content
	 * @param value
	 * @return
	 */
	public static String getInputMultiSelect(String name, Map<String, String> content, Collection value) {

		String[][] contentArray = new String[content.size()][];
		Iterator entries = content.entrySet().iterator();

		for (int i = 0; i < contentArray.length; i++) {
			Map.Entry entry = (Map.Entry) entries.next();
			contentArray[i] = new String[2];
			contentArray[i][0] = (String) entry.getKey();
			contentArray[i][1] = (String) entry.getValue();
		}

		String[] contentValue = new String[0];
		if (value != null) {
			contentValue = new String[value.size()];
			Iterator valueIt = value.iterator();
			for (int i = 0; i < contentValue.length; i++) {
				contentValue[i] = valueIt.next().toString();
			}
		}

		return getInputMultiSelect(name, contentArray, contentValue, null, null);
	}

	public static String getInputMultiSelect(String name, String[] content, String[] value) {
		String[][] newContent = new String[content.length][2];
		for (int i = 0; i < content.length; i++) {
			newContent[i][0] = content[i];
			newContent[i][1] = content[i];
		}
		return getInputMultiSelect(name, newContent, value, null, (String) null);
	}
	
	public static String getInputMultiSelect(String name, Collection<String> content, Collection<String> values, String cssClass) {
		String[][] dblContent = new String[content.size()][];
		Iterator<String> contentIt = content.iterator();
		for (int i = 0; i < content.size(); i++) {
			dblContent[i] = new String[2];
			String v = contentIt.next();
			dblContent[i][0] = v;
			dblContent[i][1] = v;
		}
		String[] contentValue = new String[0];
		if (values != null) {
			contentValue = new String[values.size()];
			Iterator valueIt = values.iterator();
			for (int i = 0; i < contentValue.length; i++) {
				contentValue[i] = valueIt.next().toString();
			}
		}
		return getInputMultiSelect(name, dblContent, contentValue, cssClass, (String) null);
	}

	public static String getInputMultiSelect(String name, String[] content, String[] values, String cssClass) {
		String[][] dblContent = new String[content.length][];
		for (int i = 0; i < content.length; i++) {
			dblContent[i] = new String[2];
			dblContent[i][0] = content[i];
			dblContent[i][1] = content[i];
		}
		return getInputMultiSelect(name, dblContent, values, cssClass, (String) null);
	}

	public static String getInputMultiSelect(String name, String[][] content, String[] values, String cssClass, String jsOnChange) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		if (values == null) {
			values = new String[0];
		}
		Set<String> valuesSet = new TreeSet<String>(Arrays.asList(values));

		int size = 6;
		if (content.length < 6) {
			size = content.length;
		}

		String cssClassCode = "";
		if (cssClass != null) {
			cssClassCode = " class=\"" + cssClass + "\"";
		}

		out.print("<select id=\"" + name + "\"" + cssClassCode + " size=\"" + size + "\" name=\"");
		out.print(name);
		if (jsOnChange != null) {
			out.println("\" multiple=\"multiple\" onchange=\"" + jsOnChange + "\">");
		} else {
			out.println("\" multiple=\"multiple\">");
		}
		for (String[] element : content) {
			if (valuesSet.contains(element[0])) {
				out.print("<option selected=\"selected\" value=\"");
			} else {
				out.print("<option value=\"");
			}
			out.print(element[0]);
			out.println("\">");
			out.println(element[1]);
			out.println("</option>");
		}
		out.println("</select>");
		out.close();
		return res.toString();
	}

	/**
	 * generate a one select widget in XHTML
	 * 
	 * @param content
	 *            a double array with id in 0 index and label in 1 index
	 * @param value
	 * @param jsOnChange
	 * @param sorting
	 * @param jsOnClick
	 *            javascript when we click on a link (@value@ for the value of
	 *            the current line)
	 * @return
	 */
	public static String getInputMultiSelectList(ContentContext ctx, String[][] content, String value, String jsOnClick) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		out.println("<ul>");

		for (String[] element : content) {
			boolean selected = (value != null) && (value.equals(element[0]));
			if (selected) {
				out.print("<li class=\"selected\">");
			} else {
				out.print("<li>");
			}

			out.print("<a href=\"#\" onclick=\"" + jsOnClick.replaceAll("@value@", element[0]) + "\">");

			if (element.length > 2) {
				out.print(getIconesCode(ctx, element[2], "icone:" + element[0]));
			}
			out.print(element[1]);

			out.print("</a>");

			out.println("</li>");
		}
		out.println("</ul>");
		out.close();
		return res.toString();
	}

	public static String getInputOneSelect(String name, Collection<? extends Object> content, String value) {
		String[] contentArray = new String[content.size()];
		int i = 0;
		for (Object obj : content) {
			contentArray[i] = obj.toString();
			i++;
		}
		return getInputOneSelect(name, contentArray, value, null, true);
	}

	public static String getInputOneSelect(String name, Collection<String> content, String value, boolean sorting) {
		String[] contentArray = new String[content.size()];
		content.toArray(contentArray);
		return getInputOneSelect(name, contentArray, value, null, sorting);
	}

	public static String getInputOneSelect(String name, Collection<String> content, String value, String cssClass) {
		String[] contentArray = new String[content.size()];
		content.toArray(contentArray);
		String[][] newContent = new String[contentArray.length][2];
		for (int i = 0; i < contentArray.length; i++) {
			newContent[i][0] = contentArray[i];
			newContent[i][1] = contentArray[i];
		}
		return getInputOneSelectInternal(name, name, newContent, value, cssClass, null, null, true);
	}

	public static String getInputOneSelect(String name, Collection<String> content, String value, String js, boolean sort) {
		String[] contentArray = new String[content.size()];
		content.toArray(contentArray);
		return getInputOneSelect(name, contentArray, value, js, sort);
	}

	public static String getInputOneSelect(String name, List<? extends Object> content, String value, String cssClass, String js, boolean sort) {

		String[][] contentArray = new String[content.size()][];
		for (int i = 0; i < content.size(); i++) {
			contentArray[i] = new String[2];
			contentArray[i][0] = (String) content.get(i);
			contentArray[i][1] = (String) content.get(i);
		}
		return getInputOneSelectInternal(name, name, contentArray, value, cssClass, js, null, sort);
	}

	public static String getInputOneSelect(String name, Map<String, String> content, String value) {
		String[][] newContent = new String[content.size()][2];
		Collection<String> keys = content.keySet();
		int i = 0;
		for (String key : keys) {
			newContent[i] = new String[2];
			newContent[i][0] = key;
			newContent[i][1] = content.get(key);
			i++;
		}

		return getInputOneSelectInternal(name, name, newContent, value, null, null, null, true);
	}

	public static String getInputOneSelect(String name, Map<String, String> content, String value, String cssClass) {
		String[][] newContent = new String[content.size()][2];
		Collection<String> keys = content.keySet();
		int i = 0;
		for (String key : keys) {
			newContent[i] = new String[2];
			newContent[i][0] = key;
			newContent[i][1] = content.get(key);
			i++;
		}

		return getInputOneSelectInternal(name, name, newContent, value, cssClass, null, null, true);
	}

	public static String getInputOneSelect(String name, String[] content, String value) {
		return getInputOneSelect(name, content, value, null, true);
	}

	public static String getInputOneSelect(String name, String[] content, String value, boolean sorting) {
		return getInputOneSelect(name, content, value, null, sorting);
	}

	public static String getInputOneSelect(String name, String[] content, String value, String js, boolean sort) {
		String[][] newContent = new String[content.length][2];
		for (int i = 0; i < content.length; i++) {
			newContent[i][0] = content[i];
			newContent[i][1] = content[i];
		}
		return getInputOneSelect(name, newContent, value, js, sort);
	}
	
	public static String getInputOneSelect(String name, String[] ids, String[] labels, String value, String js) {
		return getInputOneSelect(name, ids, labels, value, js, true);
	}

	public static String getInputOneSelect(String name, String[] ids, String[] labels, String value, String js, boolean sort) {
		String[][] newContent = new String[labels.length][2];
		for (int i = 0; i < labels.length; i++) {
			newContent[i][0] = ids[i];
			newContent[i][1] = labels[i];
		}
		return getInputOneSelectInternal(name, name, newContent, value, null, js, null, sort);
	}

	public static String getInputOneSelect(String name, String[] ids, String value, String cssClass, String js, boolean sort) {
		return getInputOneSelect(name, ids, ids, value, cssClass, js, sort);
	}

	public static String getInputOneSelect(String name, String[] ids, String[] labels, String value, String cssClass, String js, boolean sort) {
		String[][] newContent = new String[labels.length][2];
		for (int i = 0; i < labels.length; i++) {
			newContent[i][0] = ids[i];
			newContent[i][1] = labels[i];
		}
		return getInputOneSelectInternal(name, name, newContent, value, cssClass, js, null, sort);
	}

	public static String getInputOneSelect(String name, String[][] content, String value) {
		return getInputOneSelect(name, content, value, null, true);
	}

	public static String getInputOneSelect(String name, String[][] content, String value, boolean sorting) {
		return getInputOneSelect(name, content, value, null, sorting);
	}

	public static String getInputOneSelect(String name, String[][] content, String value, String jsOnChange) {
		return getInputOneSelect(name, content, value, jsOnChange, true);
	}

	/**
	 * generate a one select widget in XHTML
	 * 
	 * @param name
	 *            the name of the parameter
	 * @param content
	 *            a double array with id in 0 index and label in 1 index
	 * @param value
	 * @param jsOnChange
	 * @param sorting
	 * @return
	 */
	public static String getInputOneSelect(String name, String[][] content, String value, String jsOnChange, boolean sorting) {
		return getInputOneSelectInternal(name, name, content, value, null, jsOnChange, null, sorting);
	}

	public static String getInputOneSelect(String name, String[][] content, String value, String jsOnChange, String popupMessage, boolean sorting) {
		return getInputOneSelectInternal(name, name, content, value, null, jsOnChange, popupMessage, sorting);
	}

	public static String getInputOneSelectFirstEnpty(String inputName, Collection<String> inValues, String currentValue) throws FileNotFoundException, IOException {
		return getInputOneSelectFirstEnpty(inputName, inValues, currentValue, true);
	}

	public static String getInputOneSelectFirstEnpty(String inputName, Collection<String> inValues, String currentValue, boolean sort) throws FileNotFoundException, IOException {
		String[] values = new String[inValues.size() + 1];
		values[0] = "";
		Iterator<String> iteValues = inValues.iterator();
		for (int i = 1; i < values.length; i++) {
			values[i] = iteValues.next();
		}
		return getInputOneSelect(inputName, values, currentValue, null, sort);
	}
	
	public static String getInputOneSelectWithClass(String name, String[][] content, String value, String cssClass) {
		return getInputOneSelectInternal(name, name, content, value, cssClass, null, null, true);
	}

	/**
	 * generate a one select widget in XHTML
	 * 
	 * @param name
	 *            the name of the parameter
	 * @param content
	 *            a double array with id in 0 index and label in 1 index
	 * @param value
	 * @param jsOnChange
	 * @param sorting
	 * @return
	 */
	private static String getInputOneSelectInternal(String name, String id, String[][] content, String value, String cssClass, String jsOnChange, String popupMessage, boolean sorting) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		String popupScript = getHelpAttribute(popupMessage);

		if (sorting) {
			Arrays.sort(content, new DoubleArrayComparator(1));
		}

		String cssAttr = "";
		if (cssClass != null) {
			cssAttr = " class=\"" + cssClass + "\"";
		}

		if (id == null) {
			out.print("<select " + popupScript + cssAttr + " name=\"");
		} else {
			out.print("<select " + popupScript + cssAttr + " id=\"" + id + "\" name=\"");
		}
		out.print(name);
		if (jsOnChange != null) {
			out.println("\" onchange=\"" + jsOnChange + "\">");
		} else {
			out.println("\">");
		}
		for (String[] element : content) {
			if ((value != null) && (value.equals(element[0]))) {
				out.print("<option selected=\"selected\" value=\"");
			} else {
				out.print("<option value=\"");
			}
			out.print(StringHelper.neverNull(element[0]));

			if (element[0] == null) {
				out.print("\" disabled=\"disabled\">");
			} else {
				out.println("\">");
			}

			out.println(element[1]);
			out.println("</option>");
		}
		out.println("</select>");
		out.close();
		return res.toString();
	}

	public static String getInputOneSelectWidthFirstElement(String name, Collection<Map.Entry<String, String>> content, String firstElement, String value, String js) {
		String[][] newContent = new String[content.size() + 1][2];
		newContent[0] = new String[2];
		newContent[0][0] = "";
		newContent[0][1] = firstElement;
		int i = 1;
		for (Map.Entry<String, String> entry : content) {
			newContent[i] = new String[2];
			newContent[i][0] = entry.getKey();
			newContent[i][1] = entry.getValue();
			i++;
		}
		return getInputOneSelectInternal(name, name, newContent, value, null, js, null, false);
	}

	public static String getIntegerInput(ContentContext ctx, String form, String name, int min, int max, String jsOnChange) {
		return getIntegerInput(ctx, form, name, min, max, jsOnChange, new String[0][0]);
	}

	public static String getIntegerInput(ContentContext ctx, String form, String name, int min, int max, String jsOnChange, String[][] attributes) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		if (jsOnChange == null) {
			jsOnChange = "";
		}

		out.print("<table style=\"padding: 0px; margin:0px; width: 55px;\"><tr><td rowspan=\"2\" style=\"padding: 0px; margin:0px; vertical-align: middle;\">");

		out.print("<input readonly=\"true\" name=\"");
		out.print(name);
		out.print("\" type=\"text\" size=\"3\" value=\"" + min + "\"");
		for (String[] attribute : attributes) {
			out.print(" ");
			out.print(attribute[0]);
			out.print("=\"");
			out.print(attribute[1]);
			out.print("\"");
		}
		out.print("/>");

		out.print("</td><td style=\"width: 55px; padding: 0px; padding-left: 1px; margin:0px;\">");

		String lessImg = URLHelper.createStaticURL(ctx, "/images/button_less.gif");
		String lessImgOn = URLHelper.createStaticURL(ctx, "/images/button_less_on.gif");

		StringBuffer js = new StringBuffer();

		js.append("document." + form + "." + name + ".value--;if ((document." + form + ".");
		js.append(name);
		js.append(".value*1)<0){document." + form + ".");
		js.append(name);
		js.append(".value=0;};");
		js.append(jsOnChange);

		String buttonLess = getImageLink("-", lessImg, lessImgOn, "javascript:;", js.toString());

		String moreImg = URLHelper.createStaticURL(ctx, "/images/button_more.gif");
		String moreImgOn = URLHelper.createStaticURL(ctx, "/images/button_more_on.gif");

		js = new StringBuffer();

		js.append("document." + form + "." + name + ".value++;if ((document." + form + ".");
		js.append(name);
		js.append(".value*1)>" + max + "){document." + form + ".");
		js.append(name);
		js.append(".value=" + max + ";};");
		js.append(jsOnChange);

		String buttonMore = getImageLink("+", moreImg, moreImgOn, "javascript:;", js.toString());

		out.print(buttonMore);
		out.print("</td></tr><tr><td style=\"padding: 0px; padding-left: 1px; padding-top: 1px; margin:0px;\">");
		out.print(buttonLess);
		out.println("</td></tr></table>");

		return res.toString();
	}

	public static String getLinkSubmit(String value) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		out.print("<a href=\"javascript:this.form.submit();\">");
		out.print(value);
		out.print("</a>");

		out.close();
		return res.toString();

	}

	public static String getLinkSubmit(String formID, String value) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		out.print("<a href=\"javascript:document.forms['");
		out.print(formID);
		out.print("'].submit();\">");
		out.print(value);
		out.print("</a>");

		out.close();
		return res.toString();

	}

	public static String getListedSelection(ContentContext ctx, IContentVisualComponent inComp) {
		StringBuffer outXHTML = new StringBuffer();
		if (inComp.isListable()) {
			String xhtmlID = "inlist_select_" + inComp.getId();
			outXHTML.append("<span class=\"check-style\"> | ");
			outXHTML.append("<label for=\"");
			outXHTML.append(xhtmlID);
			outXHTML.append("\">");
			try {
				I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				outXHTML.append(i18nAccess.getText("component.inlist"));
			} catch (Exception e) {
				e.printStackTrace();
			}

			outXHTML.append(" : </label>");
			String checked = "";
			if (inComp.isList(ctx)) {
				checked = "checked=\"checked\"";
			}
			outXHTML.append("<input type=\"checkbox\" id=\"" + xhtmlID + "\" name=\"inlist_" + inComp.getId() + "\" " + checked + "/>");
			outXHTML.append("</span>");
		}
		return outXHTML.toString();
	}

	public static String getMarkerSelect(ContentContext ctx, IContentVisualComponent inComp) throws FileNotFoundException, IOException {

		if (inComp == null || inComp.getMarkerList(ctx) == null || inComp.getMarkerList(ctx).size() < 2) {
			return "";
		}

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, ctx.getRequest().getSession());
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		if (inComp.getMarkerList(ctx) != null) {
			out.println("<span class=\"select-style\"> | ");
			List<SuffixPrefix> sufixList = inComp.getMarkerList(ctx);
			String markerId = "marker-" + inComp.getId();
			out.print("<select id=\"" + markerId + "\">");
			out.print("<option value=\"\">" + i18nAccess.getText("component.marker.select") + "</option>");
			for (SuffixPrefix sufixPreffix : sufixList) {
				String preffix = StringUtils.replace(sufixPreffix.getPrefix(), "\"", "&quot;");
				String sufix = StringUtils.replace(sufixPreffix.getSuffix(), "\"", "&quot;");
				out.print("<option value=\"" + preffix + "|" + sufix + "\">");
				out.print(sufixPreffix.getName());
				out.println("</option>");
			}
			out.print("</select>");
			out.print("<a onclick=\"if ($('" + markerId + "').value.trim().length > 0) {insertMarker($('" + inComp.getContentName() + "'), $('" + markerId + "').value.split('|')[0], $('" + markerId + "').value.split('|')[1]);}\" href=\"#\" class=\"mark-link\">" + i18nAccess.getText("component.marker.mark") + "</a>");
			out.println("</span>");
		}
		out.close();
		return writer.toString();
	}

	public static String getRadio(String field, String radioValue, String value) throws ResourceNotFoundException {

		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		String addedTag = "";
		if (radioValue.equals(value)) {
			addedTag = addedTag + " checked=\"checked\" ";
		}

		out.print("<input id=\"" + radioValue + "\" type=\"radio\" name=\"");
		out.print(field);
		out.print("\" value=\"" + radioValue + "\"" + addedTag + "/>");

		out.close();
		return res.toString();
	}

	public static String getRadio(String id, String field, String value, boolean checked) throws ResourceNotFoundException {

		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		String addedTag = "";
		if (checked) {
			addedTag = addedTag + " checked=\"checked\" ";
		}

		out.print("<input id=\"" + id + "\" type=\"radio\" name=\"");
		out.print(field);
		out.print("\" value=\"" + value + "\"" + addedTag + "/>");

		out.close();
		return res.toString();
	}

	public static String getRadioInput(ContentContext ctx, FormComponent formComponent, String field, String choiceValue) {
		return getRadioInput(ctx, formComponent, field, choiceValue, null);
	}

	public static String getRadioInput(ContentContext ctx, FormComponent formComponent, String field, String choiceValue, String jsOnChange) {

		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		out.print("<input type=\"radio\" name=\"" + field + "\" value=\"" + choiceValue + "\"");

		String fieldValue = formComponent.getValue(ctx, field, "");
		if (fieldValue.equals(choiceValue)) {
			out.print(" checked=\"checked\"");
		}

		if (jsOnChange != null) {
			out.print(" onchange=\"" + jsOnChange + "\"");
		}

		out.print(" />");

		return res.toString();
	}

	public static String getRadioInput(String field, String[] values, String value) {
		return getRadioInput(field, values, value, null);
	}

	public static String getRadioInput(String field, String[] values, String value, String jsOnChange) {
		String[][] newValue = new String[values.length][];
		for (int i = 0; i < values.length; i++) {
			newValue[i] = new String[2];
			newValue[i][0] = values[i];
			newValue[i][1] = values[i];
		}
		return getRadioInput(field, newValue, value, jsOnChange);
	}

	public static String getRadioInput(String field, String[][] values, String value, String jsOnChange) {

		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		for (String[] value2 : values) {

			String radioID = value2[0];
			if (radioID == null || radioID.trim().length() == 0) {
				radioID = "___bl___";
			}
			radioID = field + '_' + radioID;

			out.print("<input type=\"radio\" id=\"" + radioID + "\" name=\"" + field + "\" value=\"" + value2[0] + "\"");

			if (value2[0].equals(value)) {
				out.print(" checked=\"checked\"");
			}

			if (jsOnChange != null) {
				out.print(" onchange=\"" + jsOnChange + "\"");
			}

			out.print(" /><label class=\"radio\" for=\"" + radioID + "\">" + value2[1] + "</label>");
		}

		return res.toString();
	}

	public static String getReverlinkSelectType(ContentContext ctx, String inputName, String currentValue) throws FileNotFoundException, IOException {
		List<String> values = ReverseLinkService.LINK_TYPES;
		Map<String, String> typeSelect = new HashMap<String, String>();
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		typeSelect.put(ReverseLinkService.NONE, i18nAccess.getText("global.none"));
		for (String value : values) {
			typeSelect.put(value, i18nAccess.getText("component.reverse-link." + value));
		}
		return getInputOneSelect(inputName, typeSelect, currentValue, "form-control");
	}

	public static String getRowCheckbox(ContentContext ctx, String field, String label, String value, GenericMessage message) throws ResourceNotFoundException {

		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		String addedTag = "";
		if (StringHelper.isTrue(value)) {
			addedTag = addedTag + " checked=\"checked\" ";
		}
		value = "true"; // if false there are nothing in the request

		out.print("<td class=\"label\"><input class=\"in-row\" type=\"checkbox\" name=\"");
		out.print(field);
		out.print("\" " + addedTag + "value=\"");
		out.print(value);
		out.println("\"/>&nbsp;" + label + "</td>");
		out.print("<td class=\"error-message\">");

		out.print(getErrorMessage(ctx, field, message));

		out.println("</td>");

		out.close();
		return res.toString();
	}

	public static String getRowInput(ContentContext ctx, String field, FormComponent formComponent) throws FileNotFoundException, ResourceNotFoundException, IOException {

		return getRowInput(ctx, field, formComponent, "text");
	}

	/**
	 * @param request
	 * @param field
	 * @param formComponent
	 * @param inputType
	 *            if equals to "checkbox", it actually calls getRowCheckbox(...)
	 * @return
	 * @throws ResourceNotFoundException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String getRowInput(ContentContext ctx, String field, FormComponent formComponent, String inputType) throws ResourceNotFoundException, FileNotFoundException, IOException {

		String result;
		String label = formComponent.getViewText(ctx, "form." + field);
		String value = formComponent.getValue(ctx, field, "");
		GenericMessage message = formComponent.getMessage(ctx, field);

		if (inputType != null && inputType.equals("checkbox")) {
			result = getRowCheckbox(ctx, field, label, value, message);
		} else {
			result = getRowInput(ctx, field, label, value, message, inputType);
		}
		return result;
	}

	public static String getRowInput(ContentContext ctx, String field, FormComponent formComponent, String[][] content) throws ResourceNotFoundException, FileNotFoundException, IOException {

		// ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		// PrintWriter out = new PrintWriter(outStream);
		String label = formComponent.getViewText(ctx, "form." + field);
		String value = formComponent.getValue(ctx, field, "");
		GenericMessage message = formComponent.getMessage(ctx, field);
		return getRowInput(ctx, field, label, value, message, content);
	}

	public static String getRowInput(ContentContext ctx, String field, String value, GenericMessage message) throws ResourceNotFoundException {
		return getRowInput(ctx, field, field, value, message, "text");
	}

	public static String getRowInput(ContentContext ctx, String field, String value, String message) throws ResourceNotFoundException {
		return getRowInput(ctx, field, field, value, new GenericMessage(message, GenericMessage.ERROR), "text");
	}

	public static String getRowInput(ContentContext ctx, String field, String label, String value, GenericMessage message, String type) throws ResourceNotFoundException {

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(outStream);

		String addedTag = "";
		if (type.trim().equalsIgnoreCase("checkbox")) {
			if (StringHelper.isTrue(value)) {
				addedTag = addedTag + " checked=\"checked\" ";
			}
			value = "true"; // if false there are nothing in the request
		}

		out.print("<td class=\"label\">");
		out.print(label + "&nbsp;:&nbsp;");
		out.println("</td>");
		out.print("<td class=\"in\"><input class=\"in-row\" type=\"" + type + "\" name=\"");
		out.print(field);
		out.print("\" " + addedTag + "value=\"");
		out.print(value);
		out.println("\"/></td>");
		out.print("<td class=\"error-message\">");

		out.print(getErrorMessage(ctx, field, message));

		out.println("</td>");
		out.flush();
		out.close();
		return new String(outStream.toByteArray());
	}

	public static String getRowInput(ContentContext ctx, String field, String label, String value, GenericMessage message, String[][] content) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(outStream);

		out.print("<td class=\"label\">");
		out.print(label + " : ");
		out.println("</td>");
		out.print("<td class=\"in\">");
		out.print(getInputOneSelect(field, content, value));
		out.println("</td>");
		out.print("<td class=\"error-message\">");

		out.print(getErrorMessage(ctx, field, message));

		out.println("</td>");
		out.flush();
		out.close();
		return new String(outStream.toByteArray());

	}

	public static String getRowInputOneSelect(ContentContext ctx, String field, String[] content, FormComponent formComponent) throws ResourceNotFoundException {

		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		String label = formComponent.getViewText(ctx, "form." + field);
		String value = formComponent.getValue(ctx, field, "");

		String addedTag = "";
		if (StringHelper.isTrue(value)) {
			addedTag = addedTag + " selected=\"selected\" ";
		}

		String[][] contents = new String[content.length][2];
		for (int i = 0; i < content.length; i++) {
			contents[i][0] = content[i];
			contents[i][1] = content[i];
		}

		out.print("<td class=\"label\">" + label + "</td>");
		out.print("<td class=\"in\">");
		out.print(getInputOneSelect(field, contents, value, null, false));
		out.println("</td>");
		out.print("<td class=\"error-message\">");

		out.print(getErrorMessage(ctx, field, formComponent.getMessage(ctx, field)));

		out.print("</td>");

		out.close();
		return res.toString();
	}

	public static String getRowPassword(ContentContext ctx, String field, FormComponent formComponent) throws FileNotFoundException, ResourceNotFoundException, IOException {

		return getRowInput(ctx, field, formComponent, "password");
	}

	public static String getRowTextArea(ContentContext ctx, FormComponent formComponent, String field) throws ResourceNotFoundException {

		// TODO use getActionGroupName() instead of "form"
		String label = formComponent.getViewText(ctx, "form." + field);

		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		out.print("<td class=\"label\">");
		out.print(label + "&nbsp;:&nbsp;");
		out.println("</td>");

		out.print("<td class=\"in\">");
		out.print(getTextArea(ctx, formComponent, field));
		out.println("</td>");

		out.print("<td class=\"error-message\">");

		out.print(getErrorMessage(ctx, field, formComponent.getMessage(ctx, field)));

		out.println("</td>");

		out.flush();
		return res.toString();
	}

	public static String getSelectListFromMap(Map map, String[] currentSelection, boolean editable) {
		Set selection = null;
		if (editable) {
			selection = new TreeSet(Arrays.asList(currentSelection));
		}
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(outStream);

		Iterator keys = map.keySet().iterator();
		String nextKey = (String) keys.next();
		String key = nextKey;
		out.println("<table>");
		while (keys.hasNext()) {
			key = nextKey;
			nextKey = (String) keys.next();
			out.println("<tr><td>");
			boolean title = !(nextKey.split("\\.").length <= key.split("\\.").length);
			if (!title && editable) {
				out.print("<input type=\"checkbox\" name=\"");
				out.print(key);
				if (selection.contains(key)) {
					out.print("\" checked=\"checked\"/>");
				} else {
					out.print("\"/>");
				}
			} else {
				out.print("&nbsp;");
			}
			out.print("</td><td>");
			for (int i = 0; i < key.split("\\.").length; i++) {
				out.print("&nbsp;&nbsp;");
			}
			if (title) {
				out.print("<b>");
			}
			out.print(key);
			out.print(" - ");
			out.print(map.get(key));
			if (title) {
				out.print("</b>");
			}
			out.println("</td></tr>");
		}
		key = nextKey;
		out.println("<tr><td>");
		if (editable) {
			out.print("<input type=\"checkbox\" name=\"");
			out.print(key);
			if (selection.contains(key)) {
				out.print("\" checked=\"checked\"/>");
			} else {
				out.print("\"/>");
			}
		}
		out.print("</td><td>");
		for (int i = 0; i < key.split("\\.").length; i++) {
			out.print("&nbsp;&nbsp;");
		}
		out.print(key);
		out.print(" - ");
		out.print(map.get(key));
		out.println("</td></tr>");

		out.println("</table>");
		out.flush();
		out.close();
		return new String(outStream.toByteArray());

	}

	public static String getSelectOneCountry(ContentContext ctx, String name, String country) {
		Map<String, String> countries = null;
		try {
			countries = I18nAccess.getInstance(ctx.getRequest()).getCountries();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getDropDownFromMap(name, countries, country, "", true);
	}

	public static String getStyleComponentOneSelect(ContentContext ctx, IContentVisualComponent inComp) {
		StringBuffer outXHTML = new StringBuffer();
		if (inComp.getStyleList(ctx) != null && inComp.getStyleList(ctx).length > 0) {
			String xhtmlID = "style_select_" + inComp.getId();
			outXHTML.append("<span class=\"select-style\"> | ");
			if (inComp.getStyleTitle(ctx) != null) {
				outXHTML.append("<label for=\"");
				outXHTML.append(xhtmlID);
				outXHTML.append("\">");
				outXHTML.append(inComp.getStyleTitle(ctx));
				outXHTML.append(" : </label>");
			}
			String[][] listContent = new String[inComp.getStyleList(ctx).length][];
			for (int i = 0; i < listContent.length; i++) {
				listContent[i] = new String[2];
				listContent[i][0] = inComp.getStyleList(ctx)[i];
				listContent[i][1] = inComp.getStyleLabelList(ctx)[i];
			}
			outXHTML.append(getInputOneSelectInternal(xhtmlID, xhtmlID, listContent, inComp.getStyle(ctx), null, null, null, false));
			outXHTML.append("</span>");
		}
		return outXHTML.toString();
	}

	public static String getTextArea(ContentContext ctx, FormComponent formComponent, String field) {
		String value = formComponent.getValue(ctx, field, "");
		return getTextArea(field, value);
	}

	public static String getTextArea(String name, String value) {
		String[][] attributes = { { "rows", "2" }, { "cols", "20" } };
		return getTextArea(name, value, attributes);
	}

	public static String getTextArea(String name, String value, String[][] attributes) {
		return getTextArea(name, value, attributes, null);
	}

	public static String getTextArea(String name, String value, String[][] attributes, String cssClass) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		if (!StringHelper.isEmpty(cssClass)) {
			cssClass = " class=\"" + cssClass + "\"";
		} else {
			cssClass = "";
		}

		out.print("<textarea" + cssClass + " name=\"");
		out.print(name);
		out.print("\"");
		for (String[] attribute : attributes) {
			out.print(" ");
			out.print(attribute[0]);
			out.print("=\"");
			out.print(attribute[1]);
			out.print("\"");
		}
		out.print(">");
		out.print(value);
		out.println("</textarea>");

		return res.toString();
	}

	public static String getTextColor(int index) {
		return TEXT_COLORS[index % TEXT_COLORS.length];
	}

	public static String getTextInput(String name, String value) {
		return getTextInput(name, value, new String[0][0], null);
	}

	public static String getTextInput(String name, String value, String cssValue) {
		return getTextInput(name, value, new String[0][0], cssValue);
	}

	public static String getTextInput(String name, String value, String[][] attributes) {
		return getTextInput(name, value, attributes, null);
	}

	private static String getTextInput(String name, String value, String[][] attributes, String cssClass) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);
		out.print("<input id=\"" + name + "\" type=\"text\" name=\"");
		out.print(name);
		out.print("\"");
		if (cssClass != null) {
			out.print(" class=\"");
			out.print(cssClass);
			out.print("\"");
		}
		out.print(" value=\"");
		out.print(value.replace("\"", "&quot;"));
		out.print("\"");
		for (String[] attribute : attributes) {
			out.print(" ");
			out.print(attribute[0]);
			out.print("=\"");
			out.print(attribute[1]);
			out.print("\"");
		}
		out.println("/>");
		return res.toString();
	}

	public static String removeTag(String html, String tag) throws BadXMLException {
		TagDescription[] tags = XMLManipulationHelper.searchAllTag(html, false);
		StringRemplacementHelper remplacement = new StringRemplacementHelper();
		for (TagDescription tag2 : tags) {
			if (tag2.getName().equalsIgnoreCase(tag)) {
				remplacement.addReplacement(tag2.getOpenStart(), tag2.getOpenEnd() + 1, "");
				remplacement.addReplacement(tag2.getCloseStart(), tag2.getCloseEnd() + 1, "");
			}
		}
		return remplacement.start(html);
	}

	public static String removeEscapeTag(String html) {
		if (html == null) {
			return null;
		} else {
			return html.replaceAll("\\&lt;(.+?)\\&gt;", "");
		}
	}

	public static String renderContentLanguage(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Set<String> languages;
		if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
			languages = globalContext.getVisibleContentLanguages();
		} else {
			languages = globalContext.getContentLanguages();
		}
		StringWriter out = new StringWriter();
		BufferedWriter writer = new BufferedWriter(out);
		try {
			writer.write("<ul>");
			writer.newLine();
			for (String lg : languages) {
				ContentContext localCtx = new ContentContext(ctx);
				localCtx.setRequestContentLanguage(lg);
				localCtx.setContentLanguage(lg);
				String cssClass = "class=\"" + lg;
				if (ctx.getRequestContentLanguage().equals(lg)) {
					cssClass = cssClass + " selected\"";
				} else {
					cssClass = cssClass + "\"";
				}
				ContentService content = ContentService.getInstance(ctx.getRequest());
				try {
					String lgcode = "";
					if (!lg.equals(ctx.getLanguage())) {
						lgcode = "lang=\"" + lg + "\" ";
					}
					if (content.contentExistForContext(localCtx)) {

						Locale currentLg = new Locale(ctx.getRequestContentLanguage());
						Locale targetLg = new Locale(lg);

						writer.write("<li " + cssClass + "><a " + lgcode + "title=\"" + targetLg.getDisplayLanguage(currentLg) + "\" href=\"" + URLHelper.createURL(localCtx) + "\"><span>" + lg + "</span></a></li>");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			writer.write("</ul>");
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toString();
	}

	public static String renderDateTime(ContentContext ctx, String name, Date date) {
		if (date == null) {
			date = new Date();
		}
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		String dateID = "date-" + StringHelper.getRandomId();
		out.println("<div class=\"date-time\">");
		out.println("<input type=\"hidden\" id=\"" + dateID + "\" name=\"" + name + "\" />");
		out.println("<input type=\"text\" name\"date\" value=\"" + StringHelper.renderDate(date) + "\"/>");
		String[] hours = new String[24];
		for (int i = 0; i < hours.length; i++) {
			hours[i] = "" + i;
		}
		out.println(getInputOneSelect("hours", hours, "" + cal.get(Calendar.HOUR_OF_DAY), false));
		out.println("</div>");
		out.close();
		return writer.toString();
	}

	public static boolean alreadyInserted(ContentContext ctx, String resource) {
		if (resource.contains("jquery-1") || resource.contains("jquery-2") || resource.contains("jquery.min") || resource.endsWith("jquery.js")) {
			resource = "_jquery-library_";
		} else if (resource.contains("jquery-ui")) {
			resource = "_jquery-ui_";
		}
		String attKey = "_ari_" + resource;
		if (ctx.getRequest().getAttribute(attKey) != null) {
			return true;
		} else {
			ctx.getRequest().setAttribute(attKey, "1");
			return false;
		}
	}

	/**
	 * return false if a tag is open but not closed.
	 * 
	 * @param ctx
	 * @param resource
	 * @return true if tag is'nt closed but opened.
	 */
	public static boolean alreadyClosedIfOpen(ContentContext ctx, String resource) {
		if (resource.contains("jquery-1") || resource.contains("jquery-2") || resource.contains("jquery.min") || resource.endsWith("jquery.js")) {
			resource = "_jquery-library_";
		} else if (resource.contains("jquery-ui")) {
			resource = "_jquery-ui_";
		}
		String openKey = "_ari_" + resource;
		String closeKey = "_arc_" + resource;
		if (ctx.getRequest().getAttribute(openKey) != null) {
			if (ctx.getRequest().getAttribute(closeKey) != null) {
				return true;
			} else {
				ctx.getRequest().setAttribute(closeKey, "1");
				return false;
			}
		}
		return true;
	}

	public static String renderHeaderResourceInsertionWithoutalreadyTest(ContentContext ctx, String resource) {
		if (StringHelper.getFileExtension(resource).equalsIgnoreCase("css")) {
			return "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + URLHelper.createStaticURL(ctx, resource) + "\" />";
		} else if (StringHelper.getFileExtension(resource).equalsIgnoreCase("js")) {
			return "<script src=\"" + URLHelper.createStaticURL(ctx, resource) + "\" type=\"text/javascript\"></script>";
		} else {
			return "<!-- resource type not identified : " + resource + " -->";
		}
	}

	public static String renderHeaderResourceInsertion(ContentContext ctx, String resource) {
		if (!alreadyInserted(ctx, resource)) {
			if (StringHelper.getFileExtension(resource).equalsIgnoreCase("css")) {
				return "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + URLHelper.createStaticURL(ctx, resource) + "\" />";
			} else if (StringHelper.getFileExtension(resource).equalsIgnoreCase("js")) {
				alreadyClosedIfOpen(ctx, resource); // close </script>
				return "<script src=\"" + URLHelper.createStaticURL(ctx, resource) + "\" type=\"text/javascript\"></script>";
			} else {
				return "<!-- resource type not identified : " + resource + " -->";
			}
		} else {
			return "<!-- resource already insered : " + resource + " -->";
		}
	}

	public static String renderLanguage(ContentContext ctx) {
		return renderLanguage(ctx, (String) null);
	}

	public static String renderLanguage(ContentContext ctx, String cssClass) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Set<String> languages;
		if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
			languages = globalContext.getVisibleLanguages();
		} else {
			languages = globalContext.getLanguages();
		}
		return renderLanguage(ctx, languages, cssClass);
	}

	private static String renderLanguage(ContentContext ctx, Set<String> languages, String ulCssClass) {
		StringWriter out = new StringWriter();
		BufferedWriter writer = new BufferedWriter(out);
		try {
			if (ulCssClass == null) {
				writer.write("<ul>");
			} else {
				writer.write("<ul class=\"" + ulCssClass + "\">");
			}
			writer.newLine();
			for (String lg : languages) {
				ContentContext localCtx = new ContentContext(ctx);
				localCtx.setLanguage(lg);
				localCtx.setContentLanguage(lg);
				localCtx.setRequestContentLanguage(lg);
				String cssClass = "class=\"" + lg;
				if (ctx.getLanguage().equals(lg)) {
					try {
						cssClass = cssClass + " " + ctx.getCurrentTemplate().getSelectedClass() + "\"";
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					cssClass = cssClass + "\"";
				}
				String lgcode = "";
				if (!lg.equals(ctx.getLanguage())) {
					lgcode = "lang=\"" + lg + "\" ";
				}
				Locale currentLg = new Locale(ctx.getRequestContentLanguage());
				Locale targetLg = new Locale(lg);
				Map<String, Object> params = null;
				try {
					if (ctx.getCurrentTemplate().isLanguageLinkKeepGetParams() && !ctx.isPostRequest()) {
						RequestService requestService = RequestService.getInstance(ctx.getRequest());
						params = requestService.getParameterMap();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				writer.write("<li " + cssClass + "><a " + lgcode + "title=\"" + targetLg.getDisplayLanguage(currentLg) + "\" href=\"" + URLHelper.createURL(localCtx, params) + "\"><span>" + lg + "</span></a></li>");
			}
			writer.write("</ul>");
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toString();
	}

	public static String renderSelectLanguage(ContentContext ctx) {
		return renderSelectLanguage(ctx, true);
	}

	public static String renderSelectLanguage(ContentContext ctx, boolean autoChange) {
		return renderSelectLanguage(ctx, autoChange, "select_language", "select_language_submit");
	}

	public static String renderSelectLanguage(ContentContext ctx, boolean autoChange, String selectId, String inputId) {
		return renderSelectLanguage(ctx, autoChange, selectId, inputId, true);
	}

	public static String renderSelectLanguage(ContentContext ctx, boolean autoChange, String selectId, String inputId, boolean renderForm) {
		return renderSelectLanguage(ctx, autoChange, selectId, inputId, ctx.getLanguage(), renderForm);
	}

	public static String renderOnlySelectLangue(ContentContext ctx, String selectId, String inputName, String currentLg, boolean autoChange) {
		if (inputName == null) {
			inputName = "lg";
		}
		StringWriter out = new StringWriter();
		BufferedWriter writer = new BufferedWriter(out);
		try {
			if (autoChange) {
				writer.write("<select id=\"" + selectId + "\" onchange=\"document.forms['select_language_form'].submit();\" name=\"" + inputName + "\">");
			} else {
				writer.write("<select id=\"" + selectId + "\" name=\"" + inputName + "\">");
			}
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			Set<String> languages;
			if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
				languages = globalContext.getVisibleLanguages();
			} else {
				languages = globalContext.getLanguages();
			}
			for (String lg : languages) {
				ContentContext localCtx = new ContentContext(ctx);
				localCtx.setLanguage(lg);
				String cssClass = "class=\"" + lg;
				if (currentLg != null && currentLg.equals(lg)) {
					cssClass = cssClass + " selected\"";
				} else {
					cssClass = cssClass + "\"";
				}
				Locale locale = new Locale(lg);
				String selected = "";
				if (currentLg != null && currentLg.equals(lg)) {
					selected = " selected=\"selected\"";
				}
				writer.write("<option lang=\"" + lg + "\" " + cssClass + " value=\"" + lg + "\"" + selected + ">" + lg + " - " + locale.getDisplayLanguage(locale) + "</option>");
			}
			writer.write("</select>");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toString();
	}

	public static String renderSelectLanguage(ContentContext ctx, boolean autoChange, String selectId, String inputId, String currentLg, boolean renderForm) {
		StringWriter out = new StringWriter();
		BufferedWriter writer = new BufferedWriter(out);
		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			if (renderForm) {
				writer.write("<form id=\"select_language_form\" action=\"" + URLHelper.createURL(ctx) + "\">");
			}
			writer.write("<fieldset><legend>" + i18nAccess.getViewText("global.change-language") + "</legend>");
			writer.newLine();
			writer.write("<input type=\"hidden\" name=\"webaction\" value=\"view.language\" />");
			writer.newLine();
			writer.write(renderOnlySelectLangue(ctx, selectId, null, currentLg, autoChange));
			if (autoChange) {
				writer.write("<input id=\"" + inputId + "\" class=\"submit\" type=\"submit\" value=\"" + i18nAccess.getViewText("global.ok") + "\" />");
				writer.write("<script type=\"text/javascript\">document.getElementById('select_language_submit').style.visibility = 'hidden'; document.getElementById('select_language_submit').style.width = 0;</script>");
			} else {
				writer.newLine();
				writer.write("<input id=\"" + inputId + "\" type=\"submit\" value=\"" + i18nAccess.getContentViewText("global.ok") + "\" />");
			}
			writer.write("</fieldset>");
			if (renderForm) {
				writer.write("</form>");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toString();
	}

	public static String renderSpecialLink(ContentContext ctx, String currentLg, String multimediaFileURL, StaticInfo staticInfo) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println("<div class=\"resource-special-links\"><a class=\"hd\" href=\"" + URLHelper.createResourceURL(ctx, multimediaFileURL) + "\" title=\"" + StringHelper.removeTag(staticInfo.getFullDescription(ctx)) + "\">");
		out.println("HD");
		out.println("</a>");
		out.println("</div>");
		out.close();
		return writer.toString();
	}

	public static String replaceJSTLData(ContentContext ctx, String xhtml) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, UnsupportedEncodingException {
		Collection<String> params = StringHelper.extractItem(xhtml, "${param.", "}");
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		for (String param : params) {
			xhtml = xhtml.replace("${param." + param + "}", URLDecoder.decode(requestService.getParameter(param, ""), ContentContext.CHARACTER_ENCODING));
		}
		InfoBean infoBean = InfoBean.getCurrentInfoBean(ctx.getRequest());
		if (infoBean == null) {
			return xhtml;
		}
		Map<String, Object> properties = BeanUtils.describe(infoBean);
		for (String key : properties.keySet()) {
			String jstlStr = "${" + InfoBean.REQUEST_KEY + '.' + key + '}';
			if (properties.get(key) != null) {
				xhtml = xhtml.replace(jstlStr, properties.get(key).toString());
			}
		}
		properties = BeanUtils.describe(infoBean.getPage());
		for (String key : properties.keySet()) {
			String jstlStr = "${" + InfoBean.REQUEST_KEY + ".page." + key + '}';
			if (properties.get(key) != null) {
				xhtml = xhtml.replace(jstlStr, properties.get(key).toString());
			}
		}
		if (Basket.isInstance(ctx)) {
			properties = BeanUtils.describe(Basket.getInstance(ctx));
			for (String key : properties.keySet()) {
				String jstlStr = "${" + Basket.KEY + '.' + key + '}';
				if (properties.get(key) != null) {
					xhtml = xhtml.replace(jstlStr, properties.get(key).toString());
				}
			}
		}
		return xhtml;
	}

	public static String replaceJSTLUserInfo(String xhtml, IUserInfo userInfo) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, UnsupportedEncodingException {
		Map<String, Object> properties = BeanUtils.describe(userInfo);
		for (String key : properties.keySet()) {
			String jstlStr = "${user." + key + '}';
			if (properties.get(key) != null) {
				xhtml = xhtml.replace(jstlStr, properties.get(key).toString());
			}
		}
		return xhtml;
	}

	/**
	 * replace text out test tag.
	 * 
	 * @param html
	 *            a html
	 * @param token
	 *            the token to be replace
	 * @param newToken
	 *            the new token
	 * @return
	 */
	public static String replaceOutTag(String html, char token, char newToken) {
		boolean inTag = false;
		char[] content = html.toCharArray();
		for (int i = 0; i < html.length(); i++) {
			if (inTag) {
				if (content[i] == '>') {
					inTag = false;
				}
			} else {
				if (content[i] == token) {
					content[i] = newToken;
				}
				if (content[i] == '<') {
					inTag = true;
				}
			}
		}
		return new String(content);
	}

	public static final String stringToAttribute(String str) {
		return escapeXHTML(str.replace("\"", "&quot;"));
	}

	public static String textToXHTML(String text) {
		return textToXHTML(text, false, null, (GlobalContext) null);
	}

	public static String textToXHTML(String text, boolean notFollow) {
		return textToXHTML(text, notFollow, null, (GlobalContext) null);
	}

	public static String textToXHTML(String text, GlobalContext globalContext) {
		return textToXHTML(text, false, null, globalContext);
	}

	public static String textToXHTML(String text, boolean notFollow, GlobalContext globalContext) {
		return textToXHTML(text, notFollow, null, globalContext);
	}

	// cssClass and popup not used
	public static String textToXHTML(String text, boolean notFollow, String cssClass, GlobalContext globalContext) {

		String res = autoLink(text, notFollow, globalContext);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		ByteArrayInputStream inStream = new ByteArrayInputStream(res.getBytes());
		BufferedReader in = new BufferedReader(new InputStreamReader(inStream));

		try {
			String p = in.readLine();
			if (p != null) {
				out.print(p);
			}

			for (p = in.readLine(); p != null; p = in.readLine()) {
				out.println("<br />");
				out.print(p);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			out.close();
		}

		String content = new String(outStream.toByteArray());
		return content.replaceAll("  ", "&nbsp;&nbsp;");
	}

	public static String textToXHTMLP(String text) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		BufferedReader reader = new BufferedReader(new CharArrayReader(text.toCharArray()));
		String line;
		try {
			line = reader.readLine();
			while (line != null) {
				out.println("<p>" + line + "</p>");
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.close();
		return new String(outStream.toByteArray());

	}

	public static String textToXHTMLDIV(String text) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		BufferedReader reader = new BufferedReader(new CharArrayReader(text.toCharArray()));
		String line;
		try {
			line = reader.readLine();
			int i = 0;
			while (line != null) {
				i++;
				out.println("<div class=\"line-" + i + "\">" + line + "</div>");
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.close();
		return new String(outStream.toByteArray());

	}

	public static String renderLine(String label, String inputName, String value) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + inputName + "\">" + label + "</label>");
		out.println("<input class=\"form-control\" type=\"text\" id=\"" + inputName + "\" name=\"" + inputName + "\" value=\"" + value + "\" />");
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	public static String renderLine(String label, String value) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"line\">");
		out.println("<label>" + label + "</label>");
		out.println("<div class=\"value\">" + value + "</div>");
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	public static String renderLine(String label, String inputName, boolean checked) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + inputName + "\">" + label + "</label>");
		String chechedHTML = "";
		if (checked) {
			chechedHTML = " checked=\"checked\"";
		}
		out.println("<input class=\"form-control\" type=\"checkbox\" id=\"" + inputName + "\" name=\"" + inputName + "\"" + chechedHTML + " />");
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	public static String extractBody(String content) {
		String lowerContent = content.toLowerCase();
		int startBody = lowerContent.indexOf("<body");
		if (startBody >= 0) {
			startBody = lowerContent.indexOf(">", startBody + 3);
			int endBody = lowerContent.indexOf("</body");
			if (startBody >= 0 && endBody >= startBody) {
				return content.substring(startBody + 1, endBody);
			}
		}
		return content;

	}

	/**
	 * transform a value to a span with key as class and value inside.
	 * 
	 * @param list
	 * @param key
	 * @return the key if list null, and empty string if key not found in the
	 *         list.
	 */
	public static String renderListItem(List<ListService.Item> list, String key) {
		if (list == null) {
			return key;
		}
		for (ListService.Item item : list) {
			if (item.getKey().equals(key)) {
				return "<span class=\"" + item.getKey() + "\">" + item.getValue() + "</span>";
			}
		}
		return "";
	}

	/**
	 * transform a value to a span with key as class and value inside.
	 * 
	 * @param list
	 * @param key
	 * @return the key if list null, and empty string if key not found in the
	 *         list.
	 */
	public static String renderMultiListItem(List<ListService.Item> list, Collection<String> keys) {
		if (list == null) {
			return StringHelper.collectionToString(keys, ";");
		}
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<ul>");
		for (ListService.Item item : list) {
			if (keys.contains(item.getKey())) {
				out.println("<li class=\"" + item.getKey() + "\">" + item.getValue() + "</li>");
			}
		}
		out.println("</ul>");
		out.close();
		return new String(outStream.toByteArray());
	}

	public static String renderUserData(ContentContext ctx, User user) {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println("<span class=\"user-data\">");
		String avatar = URLHelper.createAvatarUrl(ctx, user.getUserInfo());
		if (avatar != null) {
			out.println("<img class=\"avatar\" src=\"" + avatar + "\">");
		}
		out.println("<span class=\"fullname\">");
		out.println("<span class=\"lastname\">");
		out.println(user.getUserInfo().getLastName());
		out.println("</span>");
		out.println("<span class=\"firstname\">");
		out.println(user.getUserInfo().getFirstName());
		out.println("</span>");
		out.println("</span>");
		out.println("<span class=\"email\">");
		out.println(user.getUserInfo().getEmail());
		out.println("</span>");
		out.println("</span>");
		out.close();
		return writer.toString();
	}

	private XHTMLHelper() {
	}

	public static String safeHTML(String html) {
		return Jsoup.clean(html, Whitelist.relaxed());
	}

	public static void expandCSSImports(File css) throws IOException {
		String expandedCSS;
		try {
			expandedCSS = expandCSSIncludesToString(css);
		} catch (IOException ex) {
			logger.log(Level.WARNING, "Expand CSS imports failed for '" + css + "'.", ex);
			return; // Don't write on error, let the original as it is.
		}
		ResourceHelper.writeStringToFile(css, expandedCSS);
	}

	public static String expandCSSIncludesToString(File css) throws IOException {
		String content = ResourceHelper.loadStringFromFile(css);
		Matcher m = CSS_IMPORT_PATTERN.matcher(content);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String fileName = m.group(1);
			if (!fileName.contains("/") && !fileName.contains("\\")) {
				File importedFile = new File(css.getParentFile(), fileName);
				if (importedFile.exists()) {
					m.appendReplacement(sb, "");
					sb.append("/* START " + fileName + " */\r\n");
					sb.append(expandCSSIncludesToString(importedFile));
					sb.append("/* END " + fileName + " */");
					continue;
				}
			}
			m.appendReplacement(sb, "$0");
		}
		m.appendTail(sb);
		return sb.toString();
	}

	/**
	 * replace link in xhtml with createURL call.
	 * 
	 * @param ctx
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public static String replaceLinks(ContentContext ctx, String content) throws Exception {
		String outContent = content;
		TagDescription[] tags = XMLManipulationHelper.searchAllTag(outContent, false);
		StringRemplacementHelper remplacement = new StringRemplacementHelper();

		for (TagDescription tag : tags) {
			if (tag.getName().equalsIgnoreCase("a") || tag.getName().equalsIgnoreCase("area")) {
				String hrefValue = tag.getAttributes().get("href");
				if (hrefValue != null) {
					hrefValue = hrefValue.trim();
					if (!hrefValue.startsWith("#")) {
						if (hrefValue.startsWith("page:")) {
							String pageName = hrefValue.substring("page:".length());
							tag.getAttributes().put("href", URLHelper.createURLFromPageName(ctx, pageName));
						} else if (hrefValue.toLowerCase().startsWith("rss")) {
							String channel = "";
							if (hrefValue.contains(":")) {
								channel = hrefValue.split(":")[1];
							}
							hrefValue = URLHelper.createRSSURL(ctx, channel);
							tag.getAttributes().put("href", hrefValue);
						} else if (!StringHelper.isURL(hrefValue) && (!StringHelper.isMailURL(hrefValue)) && !hrefValue.contains("${") && !ResourceHelper.isResourceURL(ctx, hrefValue) && !ResourceHelper.isTransformURL(ctx, hrefValue)) {
							String url = URLHelper.removeParam(hrefValue);
							String params = URLHelper.getParamsAsString(hrefValue);
							url = URLHelper.createURLCheckLg(ctx, url);
							tag.getAttributes().put("href", URLHelper.addParams(url, params));
						}
						remplacement.addReplacement(tag.getOpenStart(), tag.getOpenEnd() + 1, tag.toString());
					}
				}
			} else if (tag.getName().equalsIgnoreCase("img")) {
				String src = tag.getAttribute("src", null);
				if (src != null) {
					if (!StringHelper.isURL(src)) { // relative path
						String urlPrefix = URLHelper.mergePath("/", ctx.getRequest().getContextPath(), ctx.getPathPrefix(), "/");
						if (src.startsWith(urlPrefix)) {
							InfoBean info = InfoBean.getCurrentInfoBean(ctx);
							src = URLHelper.mergePath(info.getHostURLPrefix(), src);
						} else {
							src = URLHelper.createResourceURL(ctx, src);
						}
						tag.getAttributes().put("src", src);
						remplacement.addReplacement(tag.getOpenStart(), tag.getOpenEnd() + 1, tag.toString());
					}
				}
			}
		}

		outContent = remplacement.start(outContent);
		return outContent;
	}

	public static void compressCSS(File targetFile) throws IOException {
		String newContent;
		FileInputStream in = null;
		StringWriter out = null;
		try {
			in = new FileInputStream(targetFile);
			InputStreamReader reader = new InputStreamReader(in, ContentContext.CHARACTER_ENCODING);
			out = new StringWriter();
			CSSFastMin.minimize(reader, out);
			newContent = out.toString();
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Compress CSS failed for '" + targetFile + "'.", ex);
			return; // Don't write on error, let the original as it is.
		} finally {
			ResourceHelper.closeResource(in);
			ResourceHelper.closeResource(out);
		}
		ResourceHelper.writeStringToFile(targetFile, newContent, ContentContext.CHARACTER_ENCODING);
	}

	public static String cleanHTML(String html) {
		Document doc = Jsoup.parse(html);
		EscapeMode.xhtml.getMap().put('\u00A0', "#160");
		doc.outputSettings().escapeMode(EscapeMode.xhtml);
		return doc.outerHtml();
	}

	public static void compressJS(final File targetFile) throws IOException {
//		String newContent;
//		FileInputStream in = null;
//		StringWriter out = null;
//		try {
//			in = new FileInputStream(targetFile);
//			InputStreamReader reader = new InputStreamReader(in, ContentContext.CHARACTER_ENCODING);
//			out = new StringWriter();
//			ErrorReporter reporter = new ErrorReporter() {
//
//				@Override
//				public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
//					logger.warning("JS compressor warning: " + message + " (" + targetFile + " L:" + line + " C:" + lineOffset + ")");
//				}
//
//				@Override
//				public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
//					logger.warning("JS compressor runtimeError: " + message + " (" + targetFile + " L:" + line + " C:" + lineOffset + ")");
//					return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
//				}
//
//				@Override
//				public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
//					logger.warning("JS compressor error: " + message + " (" + targetFile + " L:" + line + " C:" + lineOffset + ")");
//				}
//			};
//			new JavaScriptCompressor(reader, reporter).compress(out, 0, false, false, false, false);
//			newContent = out.toString();
//		} catch (Exception ex) {
//			logger.log(Level.WARNING, "Compress JS failed for '" + targetFile + "'.", ex);
//			return; // Don't write on error, let the original as it is.
//		} finally {
//			ResourceHelper.closeResource(in);
//			ResourceHelper.closeResource(out);
//		}
//		ResourceHelper.writeStringToFile(targetFile, newContent, ContentContext.CHARACTER_ENCODING);
	}

	private static int listDepth(TagDescription[] tags, TagDescription tag) {
		int depth = 1;
		for (String parent : XMLManipulationHelper.getAllParentName(tags, tag)) {
			if (parent.equalsIgnoreCase("ul") || parent.equalsIgnoreCase("ol")) {
				depth++;
			}
		}
		return depth;
	}

	public static String prepareToMailing(String xhtml) throws BadXMLException {
		TagDescription[] tags = XMLManipulationHelper.searchAllTag(xhtml, false);
		StringRemplacementHelper remplacement = new StringRemplacementHelper();
		int[] liNumber = new int[100];
		for (TagDescription tag : tags) {
			if (tag.getName().equalsIgnoreCase("ul") || tag.getName().equalsIgnoreCase("ol")) {
				int ind = listDepth(tags, tag);
				if (tag.getName().equalsIgnoreCase("ol")) {
					liNumber[ind] = 1;
				} else {
					liNumber[ind] = 0;
				}
				List<String> parentsNames = XMLManipulationHelper.getAllParentName(tags, tag);
				String prefix = "";
				String suffix = "";
				if ((parentsNames.contains("ul") || parentsNames.contains("ol")) && !parentsNames.contains("li")) {
					prefix = "<tr class=\"table-li\"><td colspan=\"2\" valign=\"top\">";
					suffix = "</td></tr>";
				} else if (!(parentsNames.contains("ul") || parentsNames.contains("ol"))) {
					prefix = "<div class=\"table-list-wrapper\">";
					suffix = "</div>";
				}
				remplacement.addReplacement(tag.getOpenStart(), tag.getOpenEnd() + 1, prefix + "<table class=\"table-" + tag.getName() + "-depth-" + ind + " table-" + tag.getName() + "\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tbody>");
				remplacement.addReplacement(tag.getCloseStart(), tag.getCloseEnd() + 1, "</tbody></table>" + suffix);
			} else if (tag.getName().equalsIgnoreCase("li")) {
				String bullet = "&bull;";
				int ind = listDepth(tags, tag) - 1;
				if (liNumber[ind] > 0) {
					bullet = "" + liNumber[ind] + ".";
					liNumber[ind]++;
				}
				remplacement.addReplacement(tag.getOpenStart(), tag.getOpenEnd() + 1, "<tr class=\"table-li\"><td class=\"bullet\" valign=\"top\" style=\"padding-right:3px; width: 14px;\">" + bullet + "</td><td class=\"text\" valign=\"top\">");
				remplacement.addReplacement(tag.getCloseStart(), tag.getCloseEnd() + 1, "</td></tr>");
			}
		}
		return remplacement.start(xhtml);
	}

	public static void main(String[] args) {
		String xhtml = "<body><b>test</b>pvdm@noctis.be <a href=\"mailto:pvdm@noctis.be\">pvdm@noctis.be</a></body>";
		System.out.println(autoLink(xhtml));

	}

}