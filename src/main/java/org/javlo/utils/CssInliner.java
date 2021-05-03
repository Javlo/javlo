package org.javlo.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.javlo.helper.ResourceHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CssInliner {
	private static final String STYLE = "style";
	private static final String DELIMS = "{}";
	private static final Logger LOG = Logger.getLogger(CssInliner.class.getName());

	private static final Priority STYLE_PRIORITY = new Priority(1, 0, 0, 0);

	public static String removeFirstMedia(String css) {
		int atmediaIndex = css.indexOf("@media");
		if (atmediaIndex <= 0) {
			return null;
		} else {
			int depth = 1;
			int i = 0;
			for (; atmediaIndex + i < css.length() && depth >= 1; i++) {
				if (css.charAt(atmediaIndex + i) == '{') {
					depth++;
				}
				if (css.charAt(atmediaIndex + i) == '}') {
					depth--;
				}
			}
			return css.substring(0, atmediaIndex) + css.substring(atmediaIndex + i - 1, css.length());
		}
	}

	public static String process(String html) {
		long t1 = System.nanoTime();
		// process the html doc
		Document doc = Jsoup.parse(html);

		// get all the style tags
		Elements styleTags = doc.select(STYLE);

		// this map stores all the styles we need to apply to the elements
		HashMap<String, HashMap<String, ValueWithPriority>> stylesToApply = new HashMap<>();

		for (Element style : styleTags) {
			String rules = style.getAllElements().get(0).data().replaceAll("\n", "") // remove newlines
					.replaceAll("\\/\\*[^*]*\\*+([^/*][^*]*\\*+)*\\/", "") // remove comments
					.trim();

			String nomedia = rules;
			while (nomedia != null) {
				rules = nomedia;
				nomedia = removeFirstMedia(nomedia);
			}

			StringTokenizer st = new StringTokenizer(rules, DELIMS);
			while (st.countTokens() > 1) {
				String selector = st.nextToken().trim();
				// the list of css styles for the selector
				String properties = st.nextToken().trim();
				String[] splitSelectors = selector.split(",");
				for (String sel : splitSelectors) {
					String trimmedSel = sel.trim();
					Elements selectedElements = doc.select(trimmedSel);
					for (Element selElem : selectedElements) {
						if (selElem.equals(style)) {
							LOG.log(Level.SEVERE, "Style tag selected by " + trimmedSel);
							continue;
						}
						HashMap<String, ValueWithPriority> existingStyles;
						String exactSel = selElem.cssSelector();
						if (!stylesToApply.containsKey(exactSel)) {
							existingStyles = stylesOf(STYLE_PRIORITY, selElem.attr(STYLE));
						} else {
							existingStyles = stylesToApply.get(exactSel);
						}

						stylesToApply.put(exactSel, mergeStyle(existingStyles, stylesOf(getPriority(trimmedSel), properties)));
					}
				}
			}
			//style.remove();
		}

		// apply the styles
		for (String exactSelector : stylesToApply.keySet()) {
			Element toApply = doc.select(exactSelector).first();
			if (toApply == null) {
				LOG.log(Level.SEVERE, "Failed to find " + exactSelector);
				continue;
			}
			HashMap<String, ValueWithPriority> styles = stylesToApply.get(exactSelector);
			StringBuilder sb = new StringBuilder();
			for (String property : styles.keySet()) {
				sb.append(property).append(":").append(styles.get(property).getValue()).append(";");
			}
			toApply.attr(STYLE, sb.toString());
		}

		long t2 = System.nanoTime();
		LOG.log(Level.INFO, "Spent " + ((t2 - t1) / 1000L) + " milliseconds inlining CSS");
		return doc.toString();
	}

	private static class Priority implements Comparable<Priority> {
		private int a;
		private int b;
		private int c;
		private int d;

		public Priority(int a, int b, int c, int d) {
			this.a = a;
			this.b = b;
			this.c = c;
			this.d = d;
		}

		public int getA() {
			return a;
		}

		public void setA(int a) {
			this.a = a;
		}

		public int getB() {
			return b;
		}

		public void setB(int b) {
			this.b = b;
		}

		public int getC() {
			return c;
		}

		public void setC(int c) {
			this.c = c;
		}

		public int getD() {
			return d;
		}

		public void setD(int d) {
			this.d = d;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof Priority)) {
				return false;
			}
			Priority other = (Priority) obj;
			return other.getA() == getA() && other.getB() == getB() && other.getC() == getC() && other.getD() == getD();
		}

		@Override
		public int compareTo(Priority o) {
			if (o == this) {
				return 0;
			}
			int comp = Integer.compare(getA(), o.getA());
			if (comp != 0) {
				return comp;
			}
			comp = Integer.compare(getB(), o.getB());
			if (comp != 0) {
				return comp;
			}
			comp = Integer.compare(getC(), o.getC());
			if (comp != 0) {
				return comp;
			}
			return Integer.compare(getD(), o.getD());
		}
	}

	private static class ValueWithPriority {
		private String value;
		private Priority priority;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public Priority getPriority() {
			return priority;
		}

		public void setPriority(Priority priority) {
			this.priority = priority;
		}
	}

	private static final HashMap<String, Priority> PRIORITY_CACHE = new HashMap<>();

	private static Priority getPriority(String selector) {
		selector = selector.trim();
		if (PRIORITY_CACHE.containsKey(selector)) {
			return PRIORITY_CACHE.get(selector);
		}
		int b = 0, c = 0, d = 0;
		String[] pieces = selector.split(" ");
		for (String pc : pieces) {
			if (pc == null || pc.trim().length() == 0) {
				continue;
			}
			pc = pc.trim();
			if (pc.startsWith("#")) {
				b++;
				continue;
			}
			if (pc.contains("[") || pc.startsWith(".") || (pc.contains(":") && (!pc.contains("::")))) {
				c++;
				continue;
			}
			d++;
		}
		Priority p = new Priority(0, b, c, d);
		PRIORITY_CACHE.put(selector, p);
		return p;
	}

	private static HashMap<String, ValueWithPriority> stylesOf(Priority priority, String properties) {
		HashMap<String, ValueWithPriority> vp = new HashMap<>();
		if (properties == null || properties.trim().length() == 0) {
			return vp;
		}
		String[] props = properties.split(";");
		if (props.length > 1) {
			for (String p : props) {
				String[] pcs = p.split(":");
				if (pcs.length != 2) {
					continue;
				}
				String name = pcs[0].trim(), value = pcs[1].trim();
				ValueWithPriority vwp = new ValueWithPriority();
				vwp.setPriority(priority);
				vwp.setValue(value);
				vp.put(name, vwp);
			}
		}
		return vp;
	}

	private static HashMap<String, ValueWithPriority> mergeStyle(HashMap<String, ValueWithPriority> oldProps, HashMap<String, ValueWithPriority> newProps) {
		HashMap<String, ValueWithPriority> finalProps = new HashMap<>();
		Set<String> allProps = new HashSet<>(oldProps.keySet());
		allProps.addAll(newProps.keySet());
		for (String p : allProps) {
			ValueWithPriority oldValue = oldProps.get(p);
			ValueWithPriority newValue = newProps.get(p);
			if (oldValue == null && newValue == null) {
				continue;
			}
			if (oldValue == null) {
				finalProps.put(p, newValue);
				continue;
			}
			if (newValue == null) {
				finalProps.put(p, oldValue);
				continue;
			}
			int compare = oldValue.getPriority().compareTo(newValue.getPriority());
			if (compare < 0) {
				finalProps.put(p, newValue);
			} else {
				finalProps.put(p, oldValue);
			}
		}
		return finalProps;
	}

	public static void main(String[] args) throws IOException {

		// String css = ".appleFooter a {\r\n"
		// + " color: #999999;\r\n"
		// + " text-decoration: none;\r\n"
		// + "}\r\n"
		// + "\r\n"
		// + "1@media screen and (max-width: 525px) {\r\n"
		// + " table[class=\"wrapper\"] {\r\n"
		// + " width: 100% !important;\r\n"
		// + " }\r\n"
		// + " td[class=\"logo\"] {\r\n"
		// + " text-align: left;\r\n"
		// + " padding: 20px 0 20px 0 !important;\r\n"
		// + " }}";
		// System.out.println("css = "+removeFirstMedia(css));

		String html = ResourceHelper.loadStringFromFile(new File("c:/trans/email.html"));
		html = process(html);
		ResourceHelper.writeStringToFile(new File("c:/trans/email_inlined.html"), html);

	}
}
