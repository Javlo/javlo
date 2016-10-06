package org.javlo.component.links;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringEscapeUtils;
import org.htmlparser.util.ParserException;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.service.social.SocialService;
import org.javlo.utils.JSONMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TwitterReader extends AbstractVisualComponent {

	private static Logger logger = Logger.getLogger(TwitterReader.class.getName());

	private long latestLoad = 0;
	private Map<String, TwitterBean> tweets = null;

	public static final String TYPE = "twitter-reader";

	public static class TwitterBean {
		private String authors;
		private String message;
		private String fullName;
		private String displayName;
		private String id;
		private Date date;

		public String getAuthors() {
			return authors;
		}

		public void setAuthors(String authors) {
			this.authors = StringEscapeUtils.escapeHtml4(authors);
			if (displayName == null && authors != null && authors.length() > 1) {
				displayName = authors.substring(1);
			}

		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = StringEscapeUtils.escapeHtml4(message);
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public String getFullName() {
			return fullName;
		}

		public void setFullName(String fullName) {
			this.fullName = StringEscapeUtils.escapeHtml4(fullName);
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

	}
	
	private static class ReadTweetThread extends Thread {
		
		private TwitterReader comp;
		private URL url;
		
		ReadTweetThread(TwitterReader comp, URL url) {
			this.comp = comp;
			this.url = url;
		}
		
		@Override
		public void run() {			
			try {				
				if (url != null) {
					Map<String, TwitterBean> newTweets  = TwitterReader.readTweet(url);
					comp.tweets = newTweets;
				}
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
		
	}
	
	private URL getURL(ContentContext ctx) throws MalformedURLException {
		if (getValue().trim().length() > 0) {
			return new URL(getValue());
		} else {
			String twitterURL = SocialService.getInstance(ctx).getTwitter().getURL();
			if (twitterURL != null && twitterURL.trim().length() > 0) {
				return new URL(twitterURL);
			}
		}
		return null;
	}

	private synchronized Map<String, TwitterBean> getTweet(URL url) throws ParserException, IOException {
		if (tweets == null) {
			tweets = readTweet(url);
			latestLoad = System.currentTimeMillis();
		} else if (url != null && latestLoad < (System.currentTimeMillis() - (30 * 100))) {
			latestLoad = System.currentTimeMillis();
			new ReadTweetThread(this, url).start();
		}
		return tweets;
	}
	
	public static void main(String[] args) {
		try {
			Map<String,TwitterBean> tweets = readTweet(new URL("https://twitter.com/ep_president"));
			for (TwitterBean bean : tweets.values()) {
				System.out.println("");
				System.out.println("authors : "+bean.getAuthors());
				System.out.println("message : "+bean.getMessage());
				System.out.println("date : "+bean.getDate());
			}
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Map<String, TwitterBean> readTweet(URL url) throws ParserException, IOException {
		Map<String, TwitterBean> tweets = new HashMap<String, TwitterReader.TwitterBean>();		
		Map<String, TwitterBean> newList = new HashMap<String, TwitterBean>();
		Document doc = Jsoup.connect(url.toString()).get();
		Elements newsHeadlines = doc.select(".GridTimeline-items .ProfileTweet");
		logger.info("load tweets on : " + url + " items found:"+newsHeadlines.size());		
		Iterator<Element> allItems = newsHeadlines.iterator();
		while (allItems.hasNext()) {
			Element item = allItems.next();			
			if (item != null) {
				TwitterBean bean = new TwitterBean();
				bean.setId(item.attr("data-item-id"));
				bean.setAuthors(item.attr("data-name"));
				Elements textItem = item.select(".ProfileTweet-text");
				if (textItem != null) {
					bean.setMessage(textItem.text());
					Elements dateItem = item.select(".js-short-timestamp ");
					if (dateItem != null && dateItem.first() != null && dateItem.first().attr("data-time") != null) {
						Long time = Long.parseLong(dateItem.first().attr("data-time"));
						bean.setDate(new Date(time * 1000));
						Elements fullName = item.select(".fullname");
						if (fullName != null) {
							bean.setFullName(fullName.text());
							newList.put(bean.getId(), bean);
						}
					}
				}
			}
			tweets = newList;
		}
		return tweets;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);		
		Map<String, TwitterBean> maps = getTweet(getURL(ctx));
		ctx.getRequest().setAttribute("tweets", maps.values());
		StringWriter strWriter = new StringWriter();
		JSONMap.JSON.toJson(maps, strWriter);
		ctx.getRequest().setAttribute("json", strWriter.toString());
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_ADMIN);
	}

}
