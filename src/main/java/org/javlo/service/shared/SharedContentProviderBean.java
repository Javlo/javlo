package org.javlo.service.shared;

import java.net.URL;

import org.javlo.context.ContentContext;

public class SharedContentProviderBean {
	
	private ContentContext ctx = null;
	private ISharedContentProvider provider = null;

	public SharedContentProviderBean(ContentContext ctx, ISharedContentProvider provider) {
		this.ctx = ctx;
		this.provider = provider;
	}
	
	public String getName() {
		return provider.getName();
	}
	
	public URL getURL() {
		return provider.getURL();
	}
	
	public boolean isSearch() {
		return provider.isSearch();
	}
	
	public boolean isEmpty() {
		return provider.isEmpty(ctx);
	}
	
	public String getType() {
		return provider.getType();
	}
	
	public int getCategoriesSize() {
		return provider.getCategoriesSize(ctx);
	}
	
	public int getContentSize() {
		return provider.getContentSize(ctx);
	}
	
	public boolean isUploadable() {
		return provider.isUploadable(ctx);
	}

}
