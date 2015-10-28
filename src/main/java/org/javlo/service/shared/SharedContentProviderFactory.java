package org.javlo.service.shared;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.INeedContentContext;
import org.javlo.service.shared.stockvault.StockvaultSharedContentProvider;
import org.javlo.service.shared.url.URLImageSharedContentProvider;

public class SharedContentProviderFactory {
	
	private static final String SHARED_URL_PREFIX = "_shared_url-";

	private static SharedContentProviderFactory instance = null;
	List<ISharedContentProvider> contentProviders = null;
	List<ISharedContentProvider> staticContentProviders = null;

	public static SharedContentProviderFactory getInstance(ContentContext ctx) {
		if (instance == null) {
			instance = new SharedContentProviderFactory();
		}
		return instance;
	}
	
	private static void addContentProvider (ContentContext ctx, List<ISharedContentProvider> contentProviders, ISharedContentProvider provider) {
		if (provider instanceof INeedContentContext) {
			((INeedContentContext)provider).setContentContext(ctx);
		}
		boolean nameFound = true;
		int number = 1;
		while (nameFound) {
			nameFound = false;
			for (ISharedContentProvider iSharedContentProvider : contentProviders) {
				String name = provider.getName();
				if (name.equals(iSharedContentProvider.getName())) {
					nameFound = true;					
					if (name.endsWith(" ("+(number-1)+')')) {
						String newName = name.substring(0,name.lastIndexOf('(')-1)+" ("+(number+")");
						provider.setName(newName);
					} else {
						provider.setName(name+" ("+number+')');
					}
					number++;
				}
			}			
		}
		contentProviders.add(provider);
	}

	public List<ISharedContentProvider> getAllSharedContentProvider(ContentContext ctx) {
		if (staticContentProviders == null) {
			staticContentProviders = new LinkedList<ISharedContentProvider>();
			addContentProvider(ctx, staticContentProviders,new JavloSharedContentProvider());
			addContentProvider(ctx, staticContentProviders,new CloserJavloSharedContentProvider());
			addContentProvider(ctx, staticContentProviders,new LocalImageSharedContentProvider());
			addContentProvider(ctx, staticContentProviders,new GlobalImageSharedContentProvider());			
			addContentProvider(ctx, staticContentProviders,new ImportedImageSharedContentProvider());
			addContentProvider(ctx, staticContentProviders,new LocalFileSharedContentProvider());
			addContentProvider(ctx, staticContentProviders,new ImportedFileSharedContentProvider());
			
			try {				
				addContentProvider(ctx, staticContentProviders,new StockvaultSharedContentProvider());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (contentProviders == null) {
			contentProviders = new LinkedList<ISharedContentProvider>();
			contentProviders.addAll(staticContentProviders);
			for (URL url : getURLList(ctx.getGlobalContext())) {
				addContentProvider(ctx, contentProviders, new URLImageSharedContentProvider(url));
			}
		}
		return contentProviders;
	}
	
	/**
	 * add url list (separed by CR) to create URLSharedContentProvider
	 * @param urls
	 */
	public void setURLList(GlobalContext globalContext, String urls) {
		contentProviders = null;
		Reader reader = new StringReader (urls);
		BufferedReader bufReader = new BufferedReader(reader);
		String urlText;
		
		globalContext.deletedDateFromKeyPrefix(SHARED_URL_PREFIX);		
		try {
			urlText = bufReader.readLine();			
			int i=0;
			while (urlText != null) {
				try {
					URL url = new URL(urlText);
					globalContext.setData(SHARED_URL_PREFIX+i, url.toString());
					i++;
				} catch (MalformedURLException e) {					
				}
				urlText = bufReader.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Collection<URL> getURLList(GlobalContext globalContext) {
		Collection<URL> outURLS = new LinkedList<URL>();
		for (Object key : globalContext.getDataKeys()) {
			if (key.toString().startsWith(SHARED_URL_PREFIX)) {
				try {
					outURLS.add(new URL(globalContext.getData(key.toString())));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
		return outURLS;
	}

}
