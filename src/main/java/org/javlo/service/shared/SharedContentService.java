package org.javlo.service.shared;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public class SharedContentService {
	
	private static final String KEY = "sharedContentService";
	
	private Collection<SharedContent> latestReturnedContent = null;
	
	private List<ISharedContentProvider> sharedContentProvider = null;
	
	private SharedContentContext context = null;
	
	public static SharedContentService getInstance(ContentContext ctx) {
		SharedContentService instance = (SharedContentService)ctx.getRequest().getSession().getAttribute(KEY);
		if (instance == null) {
			instance = new SharedContentService();
			ctx.getRequest().getSession().setAttribute(KEY, instance);		
		}
		instance.setContext(SharedContentContext.getInstance(ctx.getRequest().getSession()));
		return instance;
	}
	
	private Collection<SharedContent> getAllSharedContent(ContentContext ctx) {
		if (getContext().getSearchQuery() == null || latestReturnedContent == null) {
			getContext().setSearchQuery(null);
			List<SharedContent> outContent = new LinkedList<SharedContent>();		
			for (ISharedContentProvider provider : getAllActiveProvider(ctx)) {			
				ctx.setContentContextIfNeeded(provider);
				outContent.addAll(provider.getContent(ctx));
			}
		return outContent;
		} else {
			return latestReturnedContent;			
		}
	}
	
	public List<ISharedContentProvider> getAllProvider(ContentContext ctx) {		
		return SharedContentProviderFactory.getInstance(ctx).getAllSharedContentProvider(ctx);
	}
	
	public List<ISharedContentProvider> getAllActiveProvider(ContentContext ctx) {
		if (sharedContentProvider == null) {
			sharedContentProvider = new LinkedList<ISharedContentProvider>();
			Collection<String> active = getActiveProviderNames(ctx);
			for (ISharedContentProvider sharedContent : getAllProvider(ctx)) {
				if (active.contains(sharedContent.getName()) && !sharedContent.isEmpty(ctx)) {					
					sharedContentProvider.add(sharedContent);
				}
			}
		}
		return sharedContentProvider;
	}
	
	public List<String> getActiveProviderNames(ContentContext ctx) {
		 List<String> outActive = StringHelper.stringToCollection(ctx.getGlobalContext().getData("shared-content-active"),";");
		 if (outActive != null) {			 
			 return outActive;
		 } else {
			 return Collections.EMPTY_LIST;
		 }
	}
	
	public void setActiveProviderNames(ContentContext ctx, Collection<String> active) {
		sharedContentProvider = null;
		latestReturnedContent = null;
		ctx.getGlobalContext().setData("shared-content-active", StringHelper.collectionToString(active,";"));
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
	
	public Collection<SharedContent> searchContent (ContentContext ctx, ISharedContentProvider provider, String query) {
		if (query != null && query.trim().length() > 0) {
			getContext().setSearchQuery(query);
			latestReturnedContent = provider.searchContent(ctx,query);
		} else {
			getContext().setSearchQuery(null);
			latestReturnedContent = provider.getContent(ctx);
		}
		return latestReturnedContent;
	}
	
	public void clearCache() {
		getContext().setSearchQuery(null);
		latestReturnedContent = null;
	}

	public SharedContentContext getContext() {
		return context;
	}

	public void setContext(SharedContentContext context) {
		this.context = context;
	}
	
}
