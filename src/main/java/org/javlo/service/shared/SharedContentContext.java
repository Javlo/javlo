package org.javlo.service.shared;

import java.util.Collection;
import java.util.LinkedList;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public class SharedContentContext {

	private static final String KEY = "sharedContentContext";

	private String provider = null;
	private String searchQuery = null;
	private Collection<String> categories = new LinkedList<String>();

	public static final SharedContentContext getInstance(ContentContext ctx) {
		SharedContentContext outContext = (SharedContentContext) ctx.getSession().getAttribute(KEY);
		if (outContext == null) {
			outContext = new SharedContentContext();
			ctx.getSession().setAttribute(KEY, outContext);
			SharedContentService scService = SharedContentService.getInstance(ctx);
			if (scService.getActiveProviderNames(ctx).size() > 0) {
				for (String provider : scService.getActiveProviderNames(ctx)) {
					if (ImportedImageSharedContentProvider.NAME.equals(provider)) {
						outContext.setProvider(ImportedImageSharedContentProvider.NAME);
					}
				}
				if (StringHelper.isEmpty(outContext.getProvider())) {
					outContext.setProvider(scService.getActiveProviderNames(ctx).get(0));
				}
			}
		}
		return outContext;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		if (provider != null || !this.provider.equals(provider)) {
			setSearchQuery(null);
		}
		this.provider = provider;
	}

	public Collection<String> getCategories() {
		return categories;
	}

	public void setCategories(Collection<String> categories) {
		this.categories = categories;
	}

	public String getCategory() {
		if (categories.size() > 0) {
			return categories.iterator().next();
		} else {
			return null;
		}
	}

	public String getSearchQuery() {
		return searchQuery;
	}

	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
	}

}
