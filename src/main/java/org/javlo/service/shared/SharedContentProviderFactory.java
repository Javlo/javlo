package org.javlo.service.shared;

import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.service.shared.fotogrph.FotogrphSharedContentProvider;
import org.javlo.service.shared.stockvault.StockvaultSharedContentProvider;

public class SharedContentProviderFactory {

	private static SharedContentProviderFactory instance = null;
	List<ISharedContentProvider> contentProviders = null;

	public static SharedContentProviderFactory getInstance(ContentContext ctx) {
		if (instance == null) {
			instance = new SharedContentProviderFactory();
		}
		return instance;
	}
	
	private static void addContentProvider (List<ISharedContentProvider> contentProviders, ISharedContentProvider provider) {
		if (!provider.isEmpty()) {
			contentProviders.add(provider);
		}
	}

	public List<ISharedContentProvider> getAllSharedContentProvider(ContentContext ctx) {
		if (contentProviders == null) {
			contentProviders = new LinkedList<ISharedContentProvider>();
			addContentProvider(contentProviders,new JavloSharedContentProvider(ctx));
			try {				
				addContentProvider(contentProviders,new StockvaultSharedContentProvider());
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {				
				addContentProvider(contentProviders,new FotogrphSharedContentProvider());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return contentProviders;
	}

}
