package org.javlo.helper;

import java.io.IOException;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.javlo.css.CSSElement;
import org.javlo.helper.XMLManipulationHelper.BadXMLException;
import org.javlo.helper.XMLManipulationHelper.TagDescription;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CSSParser {

	/**
	 * create a static logger.
	 */
	protected static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CSSParser.class.getName());

	private static class Tag {
		private String name;

		private String id;

		private String clazz;

		public Tag(String inName, String inID, String inClazz) {
			name = inName;
			id = inID;
			clazz = inClazz;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getClazz() {
			return clazz;
		}

		public void setClazz(String clazz) {
			this.clazz = clazz;
		}
	}
	
	public static class Style {
		private String attribute = null;
		private String value = null;
		
		/**
		 * contruct css style with a style as param.
		 * life 'color: ref'
		 * @param style
		 */
		public Style(String style) {
			if (style.contains(":")) {
				style = style.replace(";", "");
				String[] splitedStyle = style.split(":");
				if (splitedStyle.length == 2) {
					attribute = splitedStyle[0].trim();
					value = splitedStyle[1].trim();
				}
			}
		}
		
		public boolean isValid() {
			return attribute != null && value != null && attribute.length() > 0 && value.length() > 0;
		}
		
		public String getAttribute() {
			return attribute;
		}
		public void setAttribute(String attribute) {
			this.attribute = attribute;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			if (isValid()) {
				return getAttribute()+':'+getValue()+';';
			} else {
				return ""; 
			}
		}
	}
	
	public static List<Style> parseStyle(String style) {
		List<Style> outStyles = new LinkedList<CSSParser.Style>();
		String[] allStyle = style.split(";");
		for (String rawStyle : allStyle) {
			Style parsedStyle = new Style(rawStyle);
			if (parsedStyle.isValid()) {
				outStyles.add(parsedStyle);
			}
		}
		return outStyles;
	}

	public static List<CSSElement> parseCSS(String css) {
		boolean inStyle = false;
		List<CSSElement> result = new LinkedList<CSSElement>();
		StringBuffer tag = new StringBuffer();
		StringBuffer style = new StringBuffer();
		CSSElement elem = new CSSElement();
		for (int index = 0; index < css.length(); index++) {
			if (css.charAt(index) == '{') {
				inStyle = true;
			} else if (css.charAt(index) == '}') {
				inStyle = false;

				String tagName = tag.toString();
				String[] tagNames = tagName.split(",");
				for (String splitedName : tagNames) {
					elem.setTag(splitedName.trim());
					elem.setStyle(style.toString().trim());
					tag = new StringBuffer();
					result.add(elem);
					elem = new CSSElement();
				}
				style = new StringBuffer();
			} else {
				if (inStyle) {
					style.append(css.charAt(index));
				} else {
					if ((css.charAt(index) != '\n') && (css.charAt(index) != '\r')) {
						tag.append(css.charAt(index));
					}
				}
			}
		}
		return result;
	}

	public static boolean match(TagDescription[] tags, TagDescription tag, CSSElement elem) {
		Stack<CSSElement.Tag> cssStack = new Stack<CSSElement.Tag>();

		cssStack.addAll(elem.getTags());

		if (!cssStack.peek().match(tag)) { // first tag must match
			return false;
		}
		cssStack.pop();
		try {
			cssStack.peek();
		} catch (EmptyStackException e) {
			return true;
		}
		TagDescription nextTag = XMLManipulationHelper.searchParent(tags, tag);
		while (nextTag != null) {
			if (cssStack.peek().match(nextTag)) {
				cssStack.pop();
				try {
					cssStack.peek();
				} catch (EmptyStackException e) {
					return true;
				}
			}
			nextTag = XMLManipulationHelper.searchParent(tags, nextTag);
		}

		return false;
	}

	public static String mergeCSS(String html, boolean removeCSS) throws BadXMLException {		
		TagDescription[] tags = XMLManipulationHelper.searchAllTag(html, true);
		StringBuffer css = new StringBuffer();
		StringRemplacementHelper remp = new StringRemplacementHelper();
		String htmlWithoutCSS = html;
		for (TagDescription tag : tags) {
			if (tag.getName().toLowerCase().equals("style")) {
				if (removeCSS) {
					remp.addReplacement(tag.getOpenStart(), tag.getCloseEnd() + 1, "");
				}
				String inside = html.substring(tag.getOpenEnd() + 1, tag.getCloseStart() - 1);
				css.append(inside);
			}
		}
		if (removeCSS) {
			htmlWithoutCSS = remp.start(html);
		}
		String cssFinal = StringHelper.removeSequence(css.toString(), "/*", "*/");
		return mergeCSS(cssFinal, htmlWithoutCSS);
	}

	private static String mergeCSS(String css, String html) throws BadXMLException {
		html = html.replace("<br>", "<br />");
		TagDescription[] tags = XMLManipulationHelper.searchAllTag(html, true);
		List<CSSElement> cssElems = parseCSS(css);

		StringRemplacementHelper remp = new StringRemplacementHelper();

		List<TagDescription> modifiedTag = new LinkedList<TagDescription>();

		for (int i = 0; i < tags.length; i++) {
			for (CSSElement cssElement : cssElems) {
				if (match(tags, tags[i], cssElement)) {
					String style = tags[i].getAttributes().get("style");
					if (style == null) {
						style = StringHelper.removeCR(cssElement.getStyle());
						style = style.replace("\"", "'");
					} else {
						style = style.trim();
						if (!style.endsWith(";")) {
							style = style + ';';
						}
						String newStyles = StringHelper.removeCR(cssElement.getStyle());
						newStyles = newStyles.replace("\"", "'");
						
						List<Style> styles = parseStyle(newStyles);
						for (Style newStyle : styles) {
							if (!style.contains(newStyle.getAttribute()+':')) {
								style = style + ' ' + newStyle;
							}
						}
					}
					tags[i].getAttributes().put("style", style);
					if (!modifiedTag.contains(tags[i])) {
						modifiedTag.add(tags[i]);
					}
				}
			}
		}

		for (TagDescription tag : modifiedTag) {
			remp.addReplacement(tag.getOpenStart(), tag.getOpenEnd() + 1, tag.toString());
		}

		return remp.start(html);
	}

	public static String mergeCSS_bk(String css, String html) throws BadXMLException {
		boolean inTag = false;
		StringRemplacementHelper remplacement = new StringRemplacementHelper();
		char[] htmlContent = html.toCharArray();
		Stack<Tag> tags = new Stack<Tag>();
		for (int i = 0; i < htmlContent.length - "class=\"\"".length(); i++) {
			if (htmlContent[i] == '<') {
				inTag = true;
				int endTagName = StringUtils.indexOf(html, ' ', i + 1);
				if (endTagName >= 0) {
					String tagName = html.substring(i, endTagName);
					Tag tag = new Tag(tagName, "", "");
					tags.push(tag);
				}
			} else if (htmlContent[i] == '>') {
				inTag = true;
			} else if (inTag) {
				if (i + "class=".length() + 2 < html.length()) {
					int endTag = i + "class=".length();
					if (htmlContent[i + "class=".length() + 1] == '"') {
						endTag = StringUtils.indexOf(html, '"', i + "class=".length() + 2);
						if (endTag >= 0) {
							remplacement.addReplacement(endTag + 1, endTag + 1, " style=\"\"");
						} else {
							logger.warning("bad end tag at this pos : " + i + ".");
						}
					}
				}
			}
		}
		return remplacement.start(html);
	}

	public static String _cssInline(String html) throws IOException {
		final String style = "style";
		Document doc = Jsoup.parse(html);
		Elements els = doc.select(style);
		for (Element e : els) {
			String styleRules = e.getAllElements().get(0).data().replaceAll("\n", "").trim(), delims = "{}";
			StringTokenizer st = new StringTokenizer(styleRules, delims);
			while (st.countTokens() > 1) {
				String selector = st.nextToken(), properties = st.nextToken();
				Elements selectedElements = doc.select(selector);
				for (Element selElem : selectedElements) {
					String oldProperties = selElem.attr(style);
					selElem.attr(style, oldProperties.length() > 0 ? concatenateProperties(oldProperties, properties) : properties);
				}
			}
			//e.remove();
		}
		return "" + doc;
	}

	private static String concatenateProperties(String oldProp, String newProp) {
		oldProp = oldProp.trim();
		if (!newProp.endsWith(";"))
			newProp += ";";
		return newProp + oldProp;
	}

}
