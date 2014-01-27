package org.javlo.service.shared;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public class SharedContentService {

	private static final String KEY = SharedContentService.class.getName();
	
	private Collection<SharedContent> latestReturnedContent = null;
	
	private List<ISharedContentProvider> sharedContentProvider = null;
	
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
		for (ISharedContentProvider provider : getAllActiveProvider(ctx)) {
			ctx.setContentContextIfNeeded(provider);
			outContent.addAll(provider.getContent(ctx));
		}
		return outContent;
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
		latestReturnedContent = provider.searchContent(ctx,query);
		return latestReturnedContent;
	}

}
