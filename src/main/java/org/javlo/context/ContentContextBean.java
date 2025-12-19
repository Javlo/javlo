package org.javlo.context;

import org.javlo.navigation.MenuElement;
import org.javlo.navigation.PageBean;
import org.javlo.rendering.Device;
import org.javlo.service.ContentService;

public class ContentContextBean {

	private String pageId;
	private String path;
	private int renderMode;
	private String area;
	private Device device;
	private String language;
	private String contentLanguage;
	private String contextKey;
	
	public ContentContextBean(ContentContext ctx, boolean page) throws Exception {
		if (page && ctx.getCurrentPage() != null) {
			pageId = ctx.getCurrentPage().getId();
		}
		path=ctx.getPath();
		area=ctx.getArea();
		device=ctx.getDevice();
		renderMode=ctx.getRenderMode();
		language=ctx.getLanguage();
		contentLanguage=ctx.getContentLanguage();
		contextKey=ctx.getGlobalContext().getContextKey();
	}
	
	public ContentContext createContentContext(ContentContext ctx) {
		ContentContext outCtx = new ContentContext(ctx);
		outCtx.resetCache();
		outCtx.setPath(path);
		outCtx.setArea(area);
		outCtx.setDevice(device);
		outCtx.setRenderMode(renderMode);
		outCtx.setLanguage(language);
		outCtx.setContentLanguage(contentLanguage);
		return outCtx;
	}

	public PageBean getCurrentPage(ContentContext ctx) {
		if (pageId != null) {
			MenuElement page = ContentService.getInstance(ctx.getGlobalContext()).getNavigation(ctx).searchChildFromId(pageId);
			if (page != null) {
				return new PageBean(ctx, page);
			}
		}
		return null;
	}

	public String getPageId() {
		return pageId;
	}

	public void setPageId(String pageId) {
		this.pageId = pageId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getRenderMode() {
		return renderMode;
	}

	public void setRenderMode(int renderMode) {
		this.renderMode = renderMode;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getContentLanguage() {
		return contentLanguage;
	}

	public void setContentLanguage(String contentLanguage) {
		this.contentLanguage = contentLanguage;
	}

	public String getContextKey() {
		return contextKey;
	}

	public void setContextKey(String contextKey) {
		this.contextKey = contextKey;
	}	
}