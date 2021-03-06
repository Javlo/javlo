package org.javlo.context;

import org.javlo.navigation.PageBean;
import org.javlo.rendering.Device;

public class ContentContextBean {

	private PageBean currentPage;
	private String pageId;
	private String path;
	private int renderMode;
	private String area;
	private Device device;
	private String language;
	private String contentLanguage;
	private String contextKey;
	
	public ContentContextBean(ContentContext ctx) throws Exception {
		currentPage=ctx.getCurrentPage().getPageBean(ctx);		
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

	public PageBean getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(PageBean currentPage) {
		this.currentPage = currentPage;
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