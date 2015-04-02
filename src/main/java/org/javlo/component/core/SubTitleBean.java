package org.javlo.component.core;

import org.javlo.context.ContentContext;

public class SubTitleBean {
	
	private String title;
	private String id;
	private int level;

	public SubTitleBean(ContentContext ctx, ISubTitle subTitle) {
		title = subTitle.getSubTitle(ctx);
		id = subTitle.getXHTMLId(ctx);
		level = subTitle.getSubTitleLevel(ctx);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}	

}
