/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.javlo.component.core.ComplexPropertiesLink;
import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.service.RequestService;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * @author pvandermaesen
 */
public class RSSReader extends ComplexPropertiesLink {

	private static class ReadRSSThread extends Thread {

		private Boolean running = false;

		private final String rule;
		private final String style;

		private final DateFormat dateFormat;

		private final RSSReader comp;

		private ReadRSSThread(RSSReader inComp, DateFormat inDateFormat, String inRule, String inStyle) {
			comp = inComp;
			dateFormat = inDateFormat;
			rule = inRule;
			style = inStyle;
		}

		public boolean isRunning() {
			return running;
		}

		@Override
		public void run() {
			synchronized (this) {
				setRunning(true);
			}
			logger.fine("start RSS thread");
			try {
				comp.cachedContent = comp.getRSSContent(dateFormat, rule, style);
			} catch (Exception e) {
				logger.warning("error when update RSS : " + e.getMessage());
				e.printStackTrace();
			}
			logger.fine("end RSS Thread");
			setRunning(false);
		}

		private void setRunning(boolean running) {
			this.running = running;
		}

	}

	private class SyndEntryComparator implements Comparator<SyndEntry> {

		private int multi = 1;

		public SyndEntryComparator(boolean croissant) {
			if (!croissant) {
				multi = -1;
			}
		}

		@Override
		public int compare(SyndEntry o1, SyndEntry o2) {
			return o1.getPublishedDate().compareTo(o2.getPublishedDate()) * multi;
		}

	}

	public static Logger logger = Logger.getLogger(RSSReader.class.getName());

	private ReadRSSThread contentThread = null;

	private final Object lockCreationThread = new Object();

	private String cachedContent = null;

	private static final String ALWAYS = "ALWAYS";

	private static final String STAY_1D = "S1D";

	private static final String STAY_3D = "S3D";

	private static final String STAY_1W = "S1W";

	private static final String STAY_1M = "S1M";

	private static final String STAY_1Y = "S1Y";

	private static final String STAY_1N = "S1N";

	private static final String STAY_3N = "S3N";

	private static final String STAY_6N = "S6N";

	private static final String STAY_10N = "S10N";

	private static final String REVERSE_LINK_KEY = "reverse-link";

	protected static final String RULE_KEY = "rule";

	protected Calendar getBackDate(String style) {
		Calendar backDate = Calendar.getInstance();
		int backDay = 9999; /*
							 * infinity back if no back day defined (all news included)
							 */
		if (style.equals(STAY_1D)) {
			backDay = 1;
		} else if (style.equals(STAY_3D)) {
			backDay = 3;
		} else if (style.equals(STAY_1W)) {
			backDay = 7;
		} else if (style.equals(STAY_1M)) {
			backDay = 30;
		} else if (style.equals(STAY_1Y)) {
			backDay = 365;
		}
		while (backDay > 365) {
			backDate.roll(Calendar.YEAR, false);
			backDay = backDay - 365;
		}
		if (backDate.get(Calendar.DAY_OF_YEAR) <= backDay) {
			backDate.roll(Calendar.YEAR, false);
			backDay = backDay + 365;
		}
		backDate.roll(Calendar.DAY_OF_YEAR, -backDay);
		return backDate;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_EASY;
	}

	public String getRuleParamName() {
		return "rule" + ID_SEPARATOR + getId();
	}

	public String getRule() {
		return properties.getProperty(RULE_KEY, ALWAYS);
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		String link = properties.getProperty(LINK_KEY, "");
		String label = properties.getProperty(LABEL_KEY, "");

		out.println(getSpecialInputTag());

		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String linkTitle = i18nAccess.getText("component.link.link");
			String labelTitle = i18nAccess.getText("component.link.label");
			out.println("<div class=\"line\">");
			out.println("<label for=\"" + getRuleParamName() + "\">" + getRuleTitle(ctx) + " : </label>");
			out.println(XHTMLHelper.getInputOneSelect(getRuleParamName(), getRuleList(ctx), getRuleLabelList(ctx), getRule(), null));
			out.println("</div><div class=\"line\">");
			out.println("<label for=\"" + getLinkName() + "\">" + linkTitle + "</label>");
			out.print(" : <textarea" + " id=\"" + getLinkName() + "\" name=\"" + getLinkName() + "\">");
			out.print(link);
			out.println("</textarea></div>");
			out.println("<div class=\"line\">");
			out.print("<label for=\"" + getLinkLabelName() + "\">" + labelTitle + "</label>");
			out.print(" : ");
			out.println(XHTMLHelper.getTextInput(getLinkLabelName(), label));
			out.println("</div>");
		} catch (Exception e) {
			e.printStackTrace();
		}

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		// validation

		if (getLinksURL().size() > 0) {
			Collection<String> links = getLinksURL();
			for (String currentLink : links) {
				if (!PatternHelper.EXTERNAL_LINK_PATTERN.matcher(currentLink).matches()) {
					setMessage(new GenericMessage(i18nAccess.getText("component.error.external-link"), GenericMessage.ERROR));
				}
			}
		} else {
			setMessage(new GenericMessage(i18nAccess.getText("component.message.help.external_link"), GenericMessage.HELP));
		}

		return writer.toString();
	}

	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

	public String getLabel() {
		String label = properties.getProperty(LABEL_KEY, "");
		String link = properties.getProperty(LINK_KEY, "");
		if (label.trim().length() == 0) {
			label = link;
		}
		return label;
	}

	public Collection<String> getLinksURL() {
		BufferedReader reader = new BufferedReader(new StringReader(properties.getProperty(LINK_KEY, "")));
		Collection<String> rssLinks = new LinkedList<String>();
		String link;
		try {
			link = reader.readLine();
			while (link != null) {
				rssLinks.add(link);
				link = reader.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rssLinks;
	}

	protected int getMaxNews(String style) {
		if (style.equals(STAY_1N)) {
			return 1;
		} else if (style.equals(STAY_3N)) {
			return 3;
		} else if (style.equals(STAY_6N)) {
			return 6;
		} else if (style.equals(STAY_10N)) {
			return 10;
		}
		return 99999; /* infinity news if no limit defined (all news included) */
	}

	public String getRSSContent(DateFormat dateFormat, String rule, String style) throws Exception {

		logger.fine("update RSS content type:" + rule);

		StringWriter writer;
		try {
			writer = new StringWriter();
			PrintWriter out = new PrintWriter(writer);

			if (getLinksURL() != null && getLinksURL().size() > 0) {

				Calendar backDate = getBackDate(rule);
				int maxNews = getMaxNews(rule);

				Collection<SyndEntry> localEntries = new TreeSet<SyndEntry>(new SyndEntryComparator(false));

				Collection<String> links = getLinksURL();
				for (String link : links) {
					URL feedUrl = new URL(link);

					SyndFeedInput input = new SyndFeedInput();
					SyndFeed feed = input.build(new XmlReader(feedUrl));

					List<SyndEntry> entries = feed.getEntries();
					int i = 0;

					for (SyndEntry syndEntry : entries) {
						i++;
						if (i > maxNews) {
							break;
						}
						if (backDate.getTime().before(syndEntry.getPublishedDate())) {
							localEntries.add(syndEntry);
						}
					}
				}

				if (localEntries.size() > 0) {
					if (getLabel().length() > 0) {
						out.println("<h2>" + getLabel() + "</h2>");
					}
					out.println("<ul>");
					int index = 1;
					for (SyndEntry syndEntry : localEntries) {
						out.println("<li class=\"item-" + index + "\">");
						out.println("<div class=\"date\">" + dateFormat.format(syndEntry.getPublishedDate()) + "</div>");
						out.println("<h3><a href=\"" + syndEntry.getLink() + "\">" + syndEntry.getTitle() + "</a></h3>");
						String description = syndEntry.getDescription().getValue();
						try {
							description = XHTMLHelper.removeTag(description, "div");
						} catch (Exception e) {
							// e.printStackTrace();
						}
						out.println("<p class=\"description\">" + description + "<div class=\"content_clear\"></div></p>");
						out.println("</li>");
						index++;
					}
				}
				out.println("</ul>");				
			}

			out.close();
			cachedContent = writer.toString();
			return cachedContent;
		} catch (Exception e) {
			e.printStackTrace();
			if (cachedContent != null) {
				return cachedContent;
			}
			return "<div class=\"error\">RSS Connection error : " + e.getMessage() + "</div>";
		}

	}

	// TODO: apply rules to all components in common bar ?
	public String[] getRuleLabelList(ContentContext ctx) {
		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String[] rules = getRuleList(ctx);
			String[] res = new String[rules.length];
			for (int i = 0; i < rules.length; i++) {
				res[i] = i18nAccess.getText("page-teaser.rules." + rules[i]);
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getRuleList(ctx);
	}

	public String[] getRuleList(ContentContext ctx) {
		return new String[] { ALWAYS, STAY_1D, STAY_3D, STAY_1W, STAY_1M, STAY_1Y, STAY_1N, STAY_3N, STAY_6N, STAY_10N };
	}

	public String getRuleTitle(ContentContext ctx) {
		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			return i18nAccess.getText("content.rss-reader.rule");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "content.rss-reader.rule";
	}

	@Override
	public String getType() {
		return "rss-reader";
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (contentThread == null) {
			contentThread = new ReadRSSThread(this, TimeHelper.getDefaultDateFormat(ctx), getRule(), getStyle(ctx));
		}
		synchronized (lockCreationThread) {
			if (cachedContent != null && !contentThread.isRunning()) {
				contentThread = new ReadRSSThread(this, TimeHelper.getDefaultDateFormat(ctx), getRule(), getStyle(ctx));
				contentThread.start();
			} else {
				GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());				
				cachedContent = getRSSContent(new SimpleDateFormat(globalContext.getMediumDateFormat(), new Locale(ctx.getRequestContentLanguage())), getRule(), getStyle(ctx));
			}
		}
		return cachedContent;
	}

	@Override
	public int getWordCount(ContentContext ctx) {
		String value = getValue();
		if (value != null) {
			return value.split(" ").length;
		}
		return 0;
	}

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {
		super.init(bean, newContext);

		/* check if the content of db is correct version */
		if (getValue().trim().length() > 0) {
			properties.load(stringToStream(getValue()));

			// legacy: get rule from former style
			if (Arrays.asList(getRuleList(newContext)).contains(getStyle(newContext))) {
				properties.setProperty(RULE_KEY, getStyle(newContext));
				setStyle(newContext, null);
			}
		} else {
			properties.setProperty(LINK_KEY, "");
			properties.setProperty(LABEL_KEY, "");
			properties.setProperty(REVERSE_LINK_KEY, "false");
			properties.setProperty(RULE_KEY, ALWAYS);
		}
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean isContentTimeCachable(ContentContext ctx) {
		return true;
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		String msg = super.performEdit(ctx);
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String label = requestService.getParameter(getLinkLabelName(), null);
		String link = requestService.getParameter(getLinkName(), "");
		String rule = requestService.getParameter(getRuleParamName(), getRule());
		if (label != null) {
			if (link != null) {
				properties.setProperty(LINK_KEY, link);
				properties.setProperty(LABEL_KEY, label);
				if (!rule.equals(getRule())) {
					properties.setProperty(RULE_KEY, rule);
				}
				setModify();
				storeProperties();
			}
		}
		return msg;
	}
}
