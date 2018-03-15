package org.javlo.helper;

import java.util.Date;

public class SiteMapBloc {
	private String text;
	private Date lastmod;

	public SiteMapBloc(String text, Date lastmod) {
		super();
		this.text = text;
		this.lastmod = lastmod;
	}

	public String getText() {
		return text;
	}

	public Date getLastmod() {
		return lastmod;
	}

}
