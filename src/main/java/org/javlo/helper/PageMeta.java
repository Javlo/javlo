package org.javlo.helper;

import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PageMeta {
	
	private String title;
	private String description;
	private List<String> tags = Collections.emptyList();
	private Date date;
	private URL url;
	private URL image;
	private Locale locale;
	private boolean fromOg = false;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public URL getUrl() {
		return url;
	}
	public void setUrl(URL url) {
		this.url = url;
	}
	public URL getImage() {
		return image;
	}
	public void setImage(URL image) {
		this.image = image;
	}
	public Locale getLocale() {
		return locale;
	}
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	public boolean isFromOg() {
		return fromOg;
	}
	public void setFromOg(boolean fromOg) {
		this.fromOg = fromOg;
	}
}