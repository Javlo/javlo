package org.javlo.macro;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.MenuElementNameComparator;
import org.javlo.service.PersistenceService;

public class SortChildren extends AbstractMacro {

	@Override
	public String getName() {
		return "sort-children";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		
		List<MenuElement> mustSorted = new LinkedList<MenuElement>(ctx.getCurrentPage().getChildMenuElements());
		
		Collections.sort(mustSorted, new MenuElementNameComparator(ctx, true));		
		int i=0;
		for (MenuElement menuElement : mustSorted) {
			i++;
			menuElement.setPriority(10*i);
		}
		mustSorted=null;

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.setAskStore(true);

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

};
