package org.javlo.remote;

import java.beans.XMLDecoder;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.URLHelper;

public class RemoteResourceFactory extends AbstractResourceFactory {

	private GlobalContext globalContext;
	private Map<String, RemoteResourceList> remoteResourcesCache = new HashMap<String, RemoteResourceList>();
	private List<String> typesCache;
	private Map<String, List<String>> categoriesCache = new HashMap<String, List<String>>();
	private static Logger logger = Logger.getLogger(RemoteResourceFactory.class.getName());

	private static final String KEY = RemoteResourceFactory.class.getName();

	public static RemoteResourceFactory getInstance(GlobalContext globalContext) {
		RemoteResourceFactory outFact = (RemoteResourceFactory) globalContext.getAttribute(KEY);
		if (outFact == null) {
			outFact = new RemoteResourceFactory();
			outFact.globalContext = globalContext;
			globalContext.setAttribute(KEY, outFact);
		}
		return outFact;
	}

	@Override
	public RemoteResourceList getResources(ContentContext ctx) throws IOException {
		return getResources(null, null);
	}

	public List<String> getTypes() throws IOException {
		if (typesCache == null) {
			typesCache = (List<String>) loadURI("/types.xml");
		}
		return typesCache;
	}

	public List<String> getCategories(String type) throws IOException {
		List<String> categories = categoriesCache.get(type);
		if (categories == null) {
			categories = (List<String>) loadURI(type + "/categories.xml");
			categoriesCache.put(type, categories);
		}
		return categories;
	}

	public RemoteResourceList getResources(String type, String category) throws IOException {
		String key = type + '-' + category;
		RemoteResourceList remoteResources = remoteResourcesCache.get(key);
		if (remoteResources == null) {
			System.out.println("***** RemoteResourceFactory.getResources : key = "+key); //TODO: remove debug trace
			remoteResources = (RemoteResourceList) loadURI('/' + URLEncoder.encode(type,ContentContext.CHARACTER_ENCODING) + '/' + URLEncoder.encode(category,ContentContext.CHARACTER_ENCODING) + ".xml");
			remoteResourcesCache.put(key, remoteResources);			
		}
		return remoteResources;
	}

	protected Serializable loadURI(String uri) throws IOException {
		Serializable obj;
		StaticConfig staticConfig = StaticConfig.getInstance(globalContext.getServletContext());
		URL url = new URL(URLHelper.mergePath(staticConfig.getMarketURL(), uri));
		logger.info("load remote resources from : " + url);
		URLConnection conn = url.openConnection();
		XMLDecoder decoder = new XMLDecoder(conn.getInputStream());
		obj = (Serializable) decoder.readObject();
		return obj;
	}

	public Map<String, Map<String, List<IRemoteResource>>> getResourcesAsMap(String type, String category) throws IOException {
		Map<String, Map<String, List<IRemoteResource>>> outMap = new HashMap<String, Map<String, List<IRemoteResource>>>();
		List<IRemoteResource> resources = getResources(type, category).getList();
		for (IRemoteResource rse : resources) {
			Map<String, List<IRemoteResource>> typeMap = outMap.get(rse.getType());
			if (typeMap == null) {
				typeMap = new HashMap<String, List<IRemoteResource>>();
				outMap.put(rse.getType(), typeMap);
			}
			List<IRemoteResource> catList = typeMap.get(rse.getCategory());
			if (catList == null) {
				catList = new LinkedList<IRemoteResource>();
				typeMap.put(rse.getCategory(), catList);
			}
			catList.add(rse);
		}
		return outMap;
	}

	public IRemoteResource getResource(ContentContext ctx, String id) throws IOException {
		List<IRemoteResource> resources = getResources(ctx).getList();
		for (IRemoteResource iRemoteResource : resources) {
			if (iRemoteResource.getId().equals(id)) {
				return iRemoteResource;
			}
		}
		return null;
	}

	/*
	 * protected List<IRemoteResource> getRemoteComponents(GlobalContext globalContexdt) { List<IRemoteResource> outResources = new ArrayList<IRemoteResource>();
	 * 
	 * IContentVisualComponent[] components = ComponentFactory.getComponents(globalContext); for (IContentVisualComponent comp : components) { comp.getClass().getClassLoader().- }
	 * 
	 * return outResources; }
	 */

	public void clear() {
		remoteResourcesCache.clear();
		typesCache = null;
		categoriesCache = null;
	}
	
}
