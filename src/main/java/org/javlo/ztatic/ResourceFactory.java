package org.javlo.ztatic;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ElementaryURLHelper;
import org.javlo.helper.StringHelper;

public class ResourceFactory {
	
	private static Logger logger = Logger.getLogger(ResourceFactory.class.getName());
	
	private static final String KEY = ResourceFactory.class.getCanonicalName();
	private static final StaticInfo NOT_FOUND = new StaticInfo();
	
	private Map<StaticInfo.ReferenceBean, StaticInfo> staticInfoCache = new HashMap<StaticInfo.ReferenceBean, StaticInfo>();
	private Map<String, List<StaticInfo>> staticInfoReferenceCache = new HashMap<String, List<StaticInfo>>();
	
	private static String getKey(ContentContext ctx) {
		if (ctx.isAsViewMode()) {
			return KEY+"-view";
		} else if (ctx.isAsPageMode()) {
			return KEY+"-page";
		} else {
			return KEY+"-edit";
		}
	}
	
	public static ResourceFactory getInstance(ContentContext ctx) {
		GlobalContext globalContext = ctx.getGlobalContext();
		ResourceFactory outFact = (ResourceFactory)globalContext.getAttribute(getKey(ctx));
		if (outFact == null) {
			outFact = new ResourceFactory();
			globalContext.setAttribute(getKey(ctx), outFact);
		}
		return outFact;
	}

	private static void addResources(ContentContext ctx, File dir, Collection<StaticInfo> resources) throws Exception {
		File[] childrenFiles = dir.listFiles();
		for (File file : childrenFiles) {
			if (file.isFile()) {
				resources.add(StaticInfo.getInstance(ctx, file));
			} else {
				addResources(ctx, file, resources);
			}
		}
	}
	
	public Collection<StaticInfo> getResources(ContentContext ctx) throws Exception {
		Collection<StaticInfo> resources = new LinkedList<StaticInfo>();
		File staticDir = new File(ElementaryURLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), ctx.getGlobalContext().getStaticConfig().getStaticFolder()));
		addResources(ctx, staticDir, resources);
		return resources;
	}
	
	public StaticInfo getStaticInfo(ContentContext ctx, StaticInfo.ReferenceBean ref) throws Exception {
		if (ref == null || StringHelper.isEmpty(ref.getReference())) {
			return null;
		}
		StaticInfo outInfo = staticInfoCache.get(ref);
		if (outInfo == null) {
			for (StaticInfo staticInfo : getResources(ctx)) {
				StaticInfo.ReferenceBean refBean = staticInfo.getReferenceBean(ctx);
				if (refBean != null && refBean.equals(ref)) {
					staticInfoCache.put(ref, staticInfo);
					return staticInfo;
				}
			}
			staticInfoCache.put(ref, NOT_FOUND);
		}
		if (outInfo == NOT_FOUND) {
			return null;
		} else {
			return outInfo;
		}
	}
	
	public void clearCache() {
		logger.info("clear cache #staticInfoCache : "+staticInfoCache.size());
		staticInfoCache.clear();
		logger.info("clear cache #staticInfoReferenceCache : "+staticInfoReferenceCache.size());
		staticInfoReferenceCache.clear();
	}
	
	public void update(ContentContext ctx, StaticInfo staticInfo) throws Exception {		
		StaticInfo.ReferenceBean ref = staticInfo.getReferenceBean(ctx);
		if (ref != null) {
			staticInfoCache.remove(ref);
			if (ref.getReference() != null) {
				staticInfoReferenceCache.remove(ref.getReference());
			}
		}
		staticInfo.resetDate();
	}

}
