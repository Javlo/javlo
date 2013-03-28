package org.javlo.remote;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;

public abstract class AbstractResourceFactory {

	public abstract RemoteResourceList getResources(ContentContext ctx) throws IOException;

	public List<String> getTypes(ContentContext ctx) throws IOException {
		List<String> outTypes = new LinkedList<String>();
		List<IRemoteResource> resources = getResources(ctx).getList();
		for (IRemoteResource resource : resources) {
			if (!outTypes.contains(resource.getType())) {
				outTypes.add(resource.getType());
			}
		}
		return outTypes;
	}

	public List<String> getCategories(ContentContext ctx, String type) throws IOException {
		List<String> outCategories = new LinkedList<String>();
		List<IRemoteResource> resources = getResources(ctx).getList();
		for (IRemoteResource resource : resources) {
			if (resource.getType().equals(type)) {
				if (!outCategories.contains(resource.getCategory())) {
					outCategories.add(resource.getCategory());
				}
			}
		}
		return outCategories;
	}

}
