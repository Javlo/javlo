package org.javlo.module.social;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.htmlparser.util.ParserException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TwitterReader {

	public static class TwitterBean {
		private String authors;
		private String message;
		private String fullName;
		private Date date;

		public String getAuthors() {
			return authors;
		}

		public void setAuthors(String authors) {
			this.authors = authors;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
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
			this.fullName = fullName;
		}

	}

	public static List<TwitterBean> readTweet(URL url) throws ParserException, IOException {
		List<TwitterBean> outTweet = new LinkedList<TwitterReader.TwitterBean>();
		Document doc = Jsoup.connect(url.toString()).get();
		Elements newsHeadlines = doc.select(".expanding-stream-item");
		Iterator<Element> allItems = newsHeadlines.iterator();
		while (allItems.hasNext()) {
			Element item = allItems.next();
			Elements authorsItem = item.select(".username");
			if (authorsItem != null) {
				TwitterBean bean = new TwitterBean();				
				bean.setAuthors(authorsItem.text());
				Elements textItem = item.select(".tweet-text");
				if (textItem != null) {					
					bean.setMessage(textItem.text());					
					outTweet.add(bean);
					Elements dateItem = item.select("._timestamp");
					if (dateItem != null) {						
						Long time = Long.parseLong(dateItem.first().attr("data-time"));						
						bean.setDate(new Date(time*1000));
						Elements fullName = item.select(".fullname");
						if (fullName != null) {
							bean.setFullName(fullName.text());
							outTweet.add(bean);
						}
					}
				}
			}
		}
		return outTweet;
	}

	public static void main(String[] args) {
		try {
			readTweet(new URL("https://twitter.com/martinschulz"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}