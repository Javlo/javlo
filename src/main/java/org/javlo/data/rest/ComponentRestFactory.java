package org.javlo.data.rest;

import java.util.Map;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.ContentService;

public class ComponentRestFactory implements IRestFactory {
	
	@Override
	public String getName() {
		return "component";		
	}

	@Override
	public IRestItem search(ContentContext ctx, String path, String query, int max) throws Exception {
		IRestItem outItem;
		path = path.replace('/', ' ').trim();
		if (StringHelper.isDigit(path)) {
			IContentVisualComponent comp = ContentService.getInstance(ctx.getGlobalContext()).getComponent(ctx, path);
			if (comp != null && comp.getPage().isReadAccess(ctx, ctx.getCurrentUser())) {
				outItem = comp;
			} else {
				outItem = null;
			}
		} else {
			RestContainer outRest = new RestContainer("components");
			int c=0;
			for (IContentVisualComponent comp : ContentService.getInstance(ctx.getGlobalContext()).getComponentByType(ctx, path)) {
				Map<String,String> params = URLHelper.extractParameterFromURL(query);
				if (params == null || params.size() == 0 || comp.isRestMatch(ctx, params)) {
					if (comp.getPage().isReadAccess(ctx, ctx.getCurrentUser())) {
						outRest.addItem(ctx, comp);
						c++;
						if (c>=max) {
							outItem = outRest;
							return outItem;
						}
					
					}
				}
			}
			outItem = outRest;
		}
		return outItem;
	}
}