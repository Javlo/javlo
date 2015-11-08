package org.javlo.data.rest;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.service.ContentService;

public class ComponentRestFactory implements IRestFactory {
	
	@Override
	public String getName() {
		return "component";		
	}

	@Override
	public IRestItem search(ContentContext ctx, String query) throws Exception {
		IRestItem outItem;
		query = query.replace('/', ' ').trim();
		if (StringHelper.isDigit(query)) {
			outItem = ContentService.getInstance(ctx.getGlobalContext()).getComponent(ctx, query);
		} else {
			RestContainer outRest = new RestContainer("components");
			for (IContentVisualComponent comp : ContentService.getInstance(ctx.getGlobalContext()).getComponentByType(ctx, query)) {
				outRest.addItem(ctx, comp);
			}			
			outItem = outRest;
		}
		return outItem;
	}	

}