package org.javlo.service.shared;

import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;

public class SharedContentService {
	
	public static SharedContentService getInstance(ContentContext ctx) {
		return new SharedContentService();
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
		for (SharedContent sharedContent : getAllSharedContent(ctx)) {
			if (sharedContent.getId().equals(id)) {
				return sharedContent;
			}
		}
		return null;
	}

}
