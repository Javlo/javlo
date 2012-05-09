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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.form.FormComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.InfoBean;
import org.javlo.exception.RessourceNotFoundException;
import org.javlo.helper.XMLManipulationHelper.BadXMLException;
import org.javlo.helper.XMLManipulationHelper.TagDescription;
import org.javlo.helper.Comparator.DoubleArrayComparator;
import org.javlo.helper.Comparator.MapEntryComparator;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.ReverseLinkService;
import org.javlo.utils.SufixPreffix;
import org.javlo.ztatic.StaticInfo;

/**
 * @author pvandermaesen This class is a helper for construct XHTML code.
 */
public class XHTMLHelper {

	/**
	 * create a static logger.
	 */
	protected static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(XHTMLHelper.class.getName());

	private static final String[] TEXT_COLORS = { "#005", "#050", "#500", "#505", "#550", "#055", "#555" };;

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

	public static String autoLink(String content) {
		return autoLink(content, null);
	}

	public static String autoLink(String content, GlobalContext globalContext) {
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

				for (String element : splitLine) {
					writer.append(createHTMLLink(element, globalContext));
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

	private static String createHTMLLink(String url, GlobalContext globalContext) {
		String outXHTML = url;
		String target = "";
		if (globalContext != null && globalContext.isOpenExernalLinkAsPopup(url)) {
			target = " target=\"blank\"";
		}

		String cssClass = "auto-link";
		if (url.contains(".")) {
			cssClass = cssClass + " web file-" + StringHelper.getFileExtension(url);
		}

		if (url.contains("@")) {
			if (PatternHelper.MAIL_PATTERN.matcher(url).matches()) {
				cssClass = "auto-link mail";
				outXHTML = "<a class=\"" + cssClass + "\" href=\"mailto:" + url.trim() + "\">" + url + "</a>";
			}
		} else if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://")) {
			outXHTML = "<a class=\"" + cssClass + "\" href=\"" + url.trim() + "\"" + target + ">" + url + "</a>";
		} else if (url.startsWith("www.")) {
			outXHTML = "<a class=\"" + cssClass + "\" href=\"http://" + url.trim() + "\"" + target + ">" + url + "</a>";
		}
		return outXHTML;
	}

	public static String escapeXHTML(String xhtml) {
		if (xhtml == null) {
			return "";
		}
		String outNoXHTML = xhtml;
		outNoXHTML = outNoXHTML.replace("&", "&amp;");
		outNoXHTML = outNoXHTML.replace("<", "&lt;");
		outNoXHTML = outNoXHTML.replace(">", "&gt;");
		outNoXHTML = outNoXHTML.replace("\"", "&quot;");		
		return outNoXHTML;
	}

	public static String extractBody(String xhtml) {
		int startBody = xhtml.toLowerCase().indexOf("<body>");
		int endBody = xhtml.toLowerCase().indexOf("</body>");
		if ((startBody < 0) || (endBody < 0) || (endBody < startBody)) {
			return null;
		} else {
			return xhtml.substring(startBody + 7, endBody);
		}
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

	public static String getCheckbox(ContentContext ctx, String field, FormComponent formComponent) throws RessourceNotFoundException {

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

	public static String getCheckbox(String field, boolean value) throws RessourceNotFoundException {

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
	 *            the name of the first empty element (empty as value), if null no empty element.
	 * @return XHTML code with a dropdown.
	 */
	public static String getDropDownFromMap(String name, Map map, String value, String emptyName, boolean sortValue) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintWriter out = new PrintWriter(outStream);

		if (map == null) {
			map = new HashMap();
		}

		List<Map.Entry> entriesList = new LinkedList<Map.Entry>(map.entrySet());
		if (sortValue) {
			Collections.sort(entriesList, new MapEntryComparator(true));
		}

		out.println("<select class=\"select\" id=\"" + name + "\" name=\"" + name + "\">");
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

	public static String getErrorMessage(HttpServletRequest request, String field, GenericMessage message) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		if (message != null) {
			String imageURL = URLHelper.createStaticURL(request, "/images/error.gif");
			if (message.getType() != GenericMessage.ERROR) {
				imageURL = URLHelper.createStaticURL(request, "/images/valid.gif");
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
			if (elem.getAllChilds().length == 0) {
				currentLine.append(elem.getName());
			} else {
				currentLine.append(elem.getName());
			}
			result.append(currentLine.toString());
			result.append(endTag);

			depth++;

		}

		MenuElement[] childs = elem.getChildMenuElements();
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
	 * call .toString on all object, create String array and call the same method with array as param.
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
	 * call .toString on all object, create String array and call the same method with array as param.
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
	 *            javascript when we click on a link (@value@ for the value of the current line)
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
		return getInputOneSelect(name, newContent, value, js, sort);
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

	public static String getIntegerInput(String form, String name, int min, int max, String jsOnChange, HttpServletRequest request) {
		return getIntegerInput(form, name, min, max, jsOnChange, new String[0][0], request);
	}

	public static String getIntegerInput(String form, String name, int min, int max, String jsOnChange, String[][] attributes, HttpServletRequest request) {
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

		String lessImg = URLHelper.createStaticURL(request, "/images/button_less.gif");
		String lessImgOn = URLHelper.createStaticURL(request, "/images/button_less_on.gif");

		StringBuffer js = new StringBuffer();

		js.append("document." + form + "." + name + ".value--;if ((document." + form + ".");
		js.append(name);
		js.append(".value*1)<0){document." + form + ".");
		js.append(name);
		js.append(".value=0;};");
		js.append(jsOnChange);

		String buttonLess = getImageLink("-", lessImg, lessImgOn, "javascript:;", js.toString());

		String moreImg = URLHelper.createStaticURL(request, "/images/button_more.gif");
		String moreImgOn = URLHelper.createStaticURL(request, "/images/button_more_on.gif");

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
			List<SufixPreffix> sufixList = inComp.getMarkerList(ctx);
			String markerId = "marker-" + inComp.getId();
			out.print("<select id=\"" + markerId + "\">");
			out.print("<option value=\"\">" + i18nAccess.getText("component.marker.select") + "</option>");
			for (SufixPreffix sufixPreffix : sufixList) {
				String preffix = StringUtils.replace(sufixPreffix.getPreffix(), "\"", "&quot;");
				String sufix = StringUtils.replace(sufixPreffix.getSufix(), "\"", "&quot;");
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

	public static String getRadio(String field, String radioValue, String value) throws RessourceNotFoundException {

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
			newValue[i][0] = value;
			newValue[i][1] = value;
		}
		return getRadioInput(field, newValue, value, jsOnChange);
	}

	public static String getRadioInput(String field, String[][] values, String value, String jsOnChange) {

		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		for (String[] value2 : values) {
			out.print("<input type=\"radio\" id=\"" + field + "\" name=\"" + field + "\" value=\"" + value2[0] + "\"");

			if (value2[0].equals(value)) {
				out.print(" checked=\"checked\"");
			}

			if (jsOnChange != null) {
				out.print(" onchange=\"" + jsOnChange + "\"");
			}

			out.print(" /><label class=\"radio\" for=\"" + field + "\">" + value2[1] + "</label>");
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
		return getInputOneSelect(inputName, typeSelect, currentValue);
	}

	public static String getRowCheckbox(ContentContext ctx, String field, String label, String value, GenericMessage message) throws RessourceNotFoundException {

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

		out.print(getErrorMessage(ctx.getRequest(), field, message));

		out.println("</td>");

		out.close();
		return res.toString();
	}

	public static String getRowInput(ContentContext ctx, String field, FormComponent formComponent) throws FileNotFoundException, RessourceNotFoundException, IOException {

		return getRowInput(ctx, field, formComponent, "text");
	}

	/**
	 * @param request
	 * @param field
	 * @param formComponent
	 * @param inputType
	 *            if equals to "checkbox", it actually calls getRowCheckbox(...)
	 * @return
	 * @throws RessourceNotFoundException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String getRowInput(ContentContext ctx, String field, FormComponent formComponent, String inputType) throws RessourceNotFoundException, FileNotFoundException, IOException {

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

	public static String getRowInput(ContentContext ctx, String field, FormComponent formComponent, String[][] content) throws RessourceNotFoundException, FileNotFoundException, IOException {

		// ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		// PrintWriter out = new PrintWriter(outStream);
		String label = formComponent.getViewText(ctx, "form." + field);
		String value = formComponent.getValue(ctx, field, "");
		GenericMessage message = formComponent.getMessage(ctx, field);
		return getRowInput(ctx, field, label, value, message, content);
	}

	public static String getRowInput(ContentContext ctx, String field, String value, GenericMessage message) throws RessourceNotFoundException {
		return getRowInput(ctx, field, field, value, message, "text");
	}

	public static String getRowInput(ContentContext ctx, String field, String value, String message) throws RessourceNotFoundException {
		return getRowInput(ctx, field, field, value, new GenericMessage(message, GenericMessage.ERROR), "text");
	}

	public static String getRowInput(ContentContext ctx, String field, String label, String value, GenericMessage message, String type) throws RessourceNotFoundException {

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

		out.print(getErrorMessage(ctx.getRequest(), field, message));

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

		out.print(getErrorMessage(ctx.getRequest(), field, message));

		out.println("</td>");
		out.flush();
		out.close();
		return new String(outStream.toByteArray());

	}

	public static String getRowInputOneSelect(ContentContext ctx, String field, String[] content, FormComponent formComponent) throws RessourceNotFoundException {

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

		out.print(getErrorMessage(ctx.getRequest(), field, formComponent.getMessage(ctx, field)));

		out.print("</td>");

		out.close();
		return res.toString();
	}

	public static String getRowPassword(ContentContext ctx, String field, FormComponent formComponent) throws FileNotFoundException, RessourceNotFoundException, IOException {

		return getRowInput(ctx, field, formComponent, "password");
	}

	public static String getRowTextArea(ContentContext ctx, FormComponent formComponent, String field) throws RessourceNotFoundException {

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

		out.print(getErrorMessage(ctx.getRequest(), field, formComponent.getMessage(ctx, field)));

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
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);

		out.print("<textarea name=\"");
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
		return getTextInput(name, value, new String[0][0]);
	}

	public static String getTextInput(String name, String value, String[][] attributes) {
		StringWriter res = new StringWriter();
		PrintWriter out = new PrintWriter(res);
		out.print("<input id=\"" + name + "\" type=\"text\" name=\"");
		out.print(name);
		out.print("\"");
		out.print(" value=\"");
		out.print(value);
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

	public static void main(String[] args) {
		String text = "\ncoucou \n\n\n\n c'est moi \n\n comment vas \ntu ?";
		System.out.println("*** text = " + text);
		System.out.println("*** html = " + textToXHTML(text));
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
				ContentService content = ContentService.createContent(ctx.getRequest());
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

	public static String renderHeaderResourceInsertion(ContentContext ctx, String resource) {
		String attKey = "___header_resource_insered_" + resource;
		if (ctx.getRequest().getAttribute(attKey) != null) {
			return "";
		} else {
			ctx.getRequest().setAttribute(attKey, resource);
			if (StringHelper.getFileExtension(resource).equalsIgnoreCase("css")) {
				return "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + URLHelper.createStaticURL(ctx, resource) + "\" />";
			} else if (StringHelper.getFileExtension(resource).equalsIgnoreCase("js")) {
				return "<script src=\"" + URLHelper.createStaticURL(ctx, resource) + "\" type=\"text/javascript\"></script>";
			} else {
				return "<!-- resource type not identified : " + resource + " -->";
			}
		}
	}

	public static String renderLanguage(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Set<String> languages;
		if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
			languages = globalContext.getVisibleLanguages();
		} else {
			languages = globalContext.getLanguages();
		}
		return renderLanguage(ctx, languages);
	}

	private static String renderLanguage(ContentContext ctx, Set<String> languages) {
		StringWriter out = new StringWriter();
		BufferedWriter writer = new BufferedWriter(out);
		try {
			writer.write("<ul>");
			writer.newLine();
			for (String lg : languages) {
				ContentContext localCtx = new ContentContext(ctx);
				localCtx.setLanguage(lg);
				localCtx.setContentLanguage(lg);
				localCtx.setRequestContentLanguage(lg);
				String cssClass = "class=\"" + lg;
				if (ctx.getLanguage().equals(lg)) {
					cssClass = cssClass + " selected\"";
				} else {
					cssClass = cssClass + "\"";
				}
				String lgcode = "";
				if (!lg.equals(ctx.getLanguage())) {
					lgcode = "lang=\"" + lg + "\" ";
				}

				Locale currentLg = new Locale(ctx.getRequestContentLanguage());
				Locale targetLg = new Locale(lg);

				writer.write("<li " + cssClass + "><a " + lgcode + "title=\"" + targetLg.getDisplayLanguage(currentLg) + "\" href=\"" + URLHelper.createURL(localCtx) + "\"><span>" + lg + "</span></a></li>");
			}
			writer.write("</ul>");
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toString();
	}

	public static String renderMessage(ContentContext ctx, GenericMessage msg, boolean icone) {
		if (msg == null) {
			return "";
		}
		String imageName = null;
		String alt = "";
		if (icone) {
			switch (msg.getType()) {
			case GenericMessage.ERROR:
				imageName = "error.png";
				alt = "error";
				break;
			case GenericMessage.HELP:
				imageName = "help.png";
				alt = "help";
				break;
			case GenericMessage.WARNING:
				imageName = "warning.png";
				alt = "warning";
				break;
			case GenericMessage.INFO:
				imageName = "info.png";
				alt = "information";
				break;
			default:
				break;
			}
		}
		StringBuffer out = new StringBuffer();
		out.append("<div class=\"message\">");
		out.append("<div class=\"");
		out.append(msg.getTypeLabel());
		out.append("\">");
		if (imageName != null) {
			out.append(getIconesCode(ctx, imageName, alt));
		}
		out.append("<p>");
		out.append(msg.getMessage());
		out.append("</p>");
		out.append("</div></div>");
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

	public static String renderOnlySelectLangue(ContentContext ctx, String selectId, String inputName, String currentLg,  boolean autoChange) {
		if (inputName == null) {
			inputName = "lg";
		}
		StringWriter out = new StringWriter();
		BufferedWriter writer = new BufferedWriter(out);

		try {

			if (autoChange) {
				writer.write("<select id=\"" + selectId + "\" onchange=\"document.forms['select_language_form'].submit();\" name=\""+inputName+"\">");
			} else {
				writer.write("<select id=\"" + selectId + "\" name=\""+inputName+"\">");
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
			writer.write(renderOnlySelectLangue(ctx, selectId, currentLg, null, autoChange));
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
		out.println("<div class=\"resource-special-links\"><a class=\"hd\" href=\"" + URLHelper.createRessourceURL(ctx, multimediaFileURL) + "\" title=\"" + StringHelper.removeTag(staticInfo.getFullDescription(ctx)) + "\">");
		out.println("HD");
		out.println("</a>");
		if (staticInfo.getLinkedPage(ctx) != null) {
			String pageURL = URLHelper.createURL(ctx, staticInfo.getLinkedPage(ctx).getPath());
			out.println("<a class=\"linked-page\" href=\"" + pageURL + "\" title=\"" + StringHelper.removeTag(staticInfo.getLinkedPage(ctx).getTitle(ctx)) + "\">");
			out.println(staticInfo.getLinkedPage(ctx).getTitle(ctx));
			out.println("</a>");
		}
		out.println("</div>");
		out.close();
		return writer.toString();
	}

	public static String replaceManualyJSTLData(ContentContext ctx, String xhtml) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		InfoBean infoBean = InfoBean.getCurrentInfoBean(ctx.getRequest());
		Map<String, Object> properties = BeanUtils.describe(infoBean);
		for (String key : properties.keySet()) {
			String jstlStr = "${" + InfoBean.REQUEST_KEY + '.' + key + '}';
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
		return textToXHTML(text, null, (GlobalContext)null);
	}

	public static String textToXHTML(String text,  GlobalContext globalContext) {
		return textToXHTML(text, null, globalContext);
	}

	// cssClass and popup not used
	public static String textToXHTML(String text, String cssClass, GlobalContext globalContext) {
		String res = autoLink(text,globalContext);

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

		return new String(outStream.toByteArray()).replaceAll("  ", "&nbsp;&nbsp;");
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
			int i=0;
			while (line != null) {
				i++;
				out.println("<div class=\"line-"+i+"\">" + line + "</div>");
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.close();
		return new String(outStream.toByteArray());

	}

	private XHTMLHelper() {
	}
}
