package org.javlo.module.market;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.javlo.bean.LinkToRenderer;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.remote.RemoteResourceFactory;

public class MarketContext extends AbstractModuleContext {

	@Override
	public List<LinkToRenderer> getNavigation() {		
		List<LinkToRenderer> nav = new LinkedList<LinkToRenderer>();
		RemoteResourceFactory remoteResourceFactory = RemoteResourceFactory.getInstance(globalContext);
		try {
			List<String> types = remoteResourceFactory.getTypes();
			for (String type : types) {				
				LinkToRenderer link = new LinkToRenderer(type, type, null);
				nav.add(link);
				List<LinkToRenderer> children = new LinkedList<LinkToRenderer>();
				List<String> categories = remoteResourceFactory.getCategories(type);
				for (String category : categories) {					
					children.add(new LinkToRenderer(category, type+'-'+category, "/jsp/list.jsp?type=" + type + "&category=" + category));
				}
				if (children.size() > 0) {
					link.setChildren(children);
				} else {
					link.setChildren(null);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return nav;		
	}

	@Override
	public LinkToRenderer getHomeLink() {
		for (LinkToRenderer link : getNavigation()) {
			if (link.getRenderer() != null) {
				return link;
			}
		}
		return null;
	}

	@Override
	public void init() {
	}

}
