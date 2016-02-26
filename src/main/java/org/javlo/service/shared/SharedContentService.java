package org.javlo.service.shared;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.module.core.IMainModuleName;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;

public class SharedContentService {
	
	private static Logger logger = Logger.getLogger(SharedContentService.class.getName());
	
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
	
	public void clearCache(ContentContext ctx) {
		for (ISharedContentProvider provider : getAllActiveProvider(ctx)) {
			provider.refresh(ctx);
			provider.getContent(ctx);
		}
		getContext().setSearchQuery(null);
		latestReturnedContent = null;		
	}

	public SharedContentContext getContext() {
		return context;
	}

	public void setContext(SharedContentContext context) {
		this.context = context;
	}
	
	public static void prepare(ContentContext ctx) throws ModuleException {
		ModulesContext modulesContext = ModulesContext.getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext());		
		if (modulesContext.searchModule(IMainModuleName.SHARED_CONTENT) != null) {
			SharedContentService sharedContentService = SharedContentService.getInstance(ctx);
			SharedContentContext sharedContentContext = SharedContentContext.getInstance(ctx.getRequest().getSession());
			ctx.getRequest().setAttribute("sharedContentProviders", sharedContentService.getAllActiveProvider(ctx));
			if (ctx.getRequest().getAttribute("sharedContent") == null) {
				if (sharedContentContext.getSearchQuery() == null) {
					ctx.getRequest().setAttribute("currentCategory", sharedContentContext.getCategory());
				}
			}
			ISharedContentProvider provider = sharedContentService.getProvider(ctx, sharedContentContext.getProvider());
			if (provider != null) {
				// set first category by default
				if ((sharedContentContext.getCategory() == null || !provider.getCategories(ctx).containsKey(sharedContentContext.getCategory())) && provider.getCategories(ctx).size() > 0) {
					sharedContentContext.setCategories(new LinkedList<String>(Arrays.asList(provider.getCategories(ctx).keySet().iterator().next())));
				}
				ctx.getRequest().setAttribute("provider", provider);
				ctx.setContentContextIfNeeded(provider);
				if (ctx.getRequest().getAttribute("sharedContent") == null) { // no search
					if (sharedContentContext.getSearchQuery() == null) {
						ctx.getRequest().setAttribute("sharedContent", provider.getContent(ctx, sharedContentContext.getCategories()));
					} else {
						ctx.getRequest().setAttribute("sharedContent", provider.searchContent(ctx, sharedContentContext.getSearchQuery()));
					}
				}
				ctx.getRequest().setAttribute("sharedContentCategories", provider.getCategories(ctx).entrySet());
			} else {
				logger.warning("shared content not found = " + sharedContentContext.getProvider());
			}
		}
	}
	
}
