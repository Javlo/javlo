package org.javlo.remote;

import java.beans.XMLDecoder;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;

public class RemoteResourceFactory extends AbstractResourceFactory {

	private GlobalContext globalContext;
	private RemoteResourceList remoteResources = null;
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
		return getResources();		
	}
	
	public List<String> getTypes() throws IOException {
		List<String> outTypes = new LinkedList<String>();
		List<IRemoteResource> resources = getResources().getList();
		for (IRemoteResource resource : resources) {
			if (!outTypes.contains(resource.getType())) {
				outTypes.add(resource.getType());
			}
		}
		return outTypes;
	}
	
	public List<String> getCategories(String type) throws IOException {
		List<String> outCategories = new LinkedList<String>();
		List<IRemoteResource> resources = getResources().getList();
		for (IRemoteResource resource : resources) {
			if (resource.getType().equals(type)) {
				if (!outCategories.contains(resource.getCategory())) {
					outCategories.add(resource.getCategory());
				}
			}
		}
		return outCategories;
	}

	
	public RemoteResourceList getResources() throws IOException {
		if (remoteResources == null) {
			StaticConfig staticConfig = StaticConfig.getInstance(globalContext.getServletContext());
			URL url = new URL(staticConfig.getMarketURL());
			logger.info("load remote resources from : " + url);
			URLConnection conn = url.openConnection();
			XMLDecoder decoder = new XMLDecoder(conn.getInputStream());
			remoteResources = (RemoteResourceList) decoder.readObject();
			
			/*for (IRemoteResource resource : remoteResources.getList()) {
				System.out.println("name: "+resource.getName());
				System.out.println("url: "+resource.getURL());
				System.out.println("image URL : "+resource.getImageURL());
				System.out.println("");
			}*/
			
			logger.info("resources loaded : " + remoteResources.getList().size());
		}
		return remoteResources;
	}
	
	public Map<String, Map<String, List<IRemoteResource>>> getResourcesAsMap() throws IOException {
		Map<String, Map<String, List<IRemoteResource>>> outMap = new HashMap<String, Map<String,List<IRemoteResource>>>();
		List<IRemoteResource> resources = getResources().getList();
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
	
	/*protected List<IRemoteResource> getRemoteComponents(GlobalContext globalContexdt) {
		List<IRemoteResource> outResources = new ArrayList<IRemoteResource>();
		
		IContentVisualComponent[] components = ComponentFactory.getComponents(globalContext);
		for (IContentVisualComponent comp : components) {
			comp.getClass().getClassLoader().-
		}
		
		return outResources;
	}*/

	public void clear() {
		remoteResources = null;	
	}


}
