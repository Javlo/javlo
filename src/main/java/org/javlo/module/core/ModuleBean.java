package org.javlo.module.core;

import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;

public class ModuleBean {
	
	private String name;
	private String title;
	private String url;

	public ModuleBean(ContentContext ctx, Module module) {
		setName(module.getName());
		setTitle(module.getTitle());
		ctx = ctx.getContextForAbsoluteURL();
		ctx.setRenderMode(ContentContext.EDIT_MODE);
		url = URLHelper.createModuleURL(ctx, "", module.getName());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	

}
