package org.javlo.service.shared;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;

public class SharedContentService {

	private static final String KEY = SharedContentService.class.getName();
	
	private Collection<SharedContent> latestReturnedContent = null;
	
	public static SharedContentService getInstance(ContentContext ctx) {
		SharedContentService instance = (SharedContentService)ctx.getRequest().getSession().getAttribute(KEY);
		if (instance == null) {
			instance = new SharedContentService();
			ctx.getRequest().getSession().setAttribute(KEY, instance);
		}
		return instance;
	}
	
	private List<SharedContent> getAllSharedContent(ContentContext ctx) {
		List<SharedContent> outContent = new LinkedList<SharedContent>();		
		for (ISharedContentProvider provider : getAllProvider(ctx)) {
			if (provider instanceof JavloSharedContentProvider) {
				((JavloSharedContentProvider)provider).setContentContext(ctx);
			}
			outContent.addAll(provider.getContent());
		}
		return outContent;
	}
	
	public List<ISharedContentProvider> getAllProvider(ContentContext ctx) {		
		return SharedContentProviderFactory.getInstance(ctx).getAllSharedContentProvider(ctx);
	}
	
	public ISharedContentProvider getProvider(ContentContext ctx, String name) {
		for (ISharedContentProvider provider : getAllProvider(ctx)) {
			if (provider.getName().equals(name)) {
				return provider;
			}
		}
		return null;
	}
	
	public SharedContent getSharedContent(ContentContext ctx, String id) {
		if (latestReturnedContent == null) {
			latestReturnedContent = getAllSharedContent(ctx);
		}
		for (SharedContent sharedContent : latestReturnedContent) {
			if (sharedContent.getId().equals(id)) {
				return sharedContent;
			}
		}
		return null;
	}
	
	public Collection<SharedContent> searchContent (ISharedContentProvider provider, String query) {
		latestReturnedContent = provider.searchContent(query);
		return latestReturnedContent;
	}

}
