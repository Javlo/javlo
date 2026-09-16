package org.javlo.macro;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.Unknown;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;

/**
 * delete all components of the site with a type not found (unknow component).
 */
public class DeleteUnknownComponent extends AbstractMacro {

	@Override
	public String getName() {
		return "delete-unknown-component";
	}

	protected int deleteUnknownComponent(ContentContext ctx, MenuElement page, Set<String> deletedTypes) throws Exception {
		List<String> toBeDeleted = new LinkedList<String>();
		for (ComponentBean bean : page.getContent()) {
			if (ComponentFactory.createComponent(ctx, bean, page, null, null) instanceof Unknown) {
				toBeDeleted.add(bean.getId());
				deletedTypes.add(bean.getType());
			}
		}
		for (String id : toBeDeleted) {
			page.removeContent(ctx, id);
		}
		return toBeDeleted.size();
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ContentService content = ContentService.getInstance(globalContext);
		MenuElement root = content.getNavigation(ctx);
		Set<String> deletedTypes = new TreeSet<String>();
		int countDeleted = deleteUnknownComponent(ctx, root, deletedTypes);
		for (MenuElement child : root.getAllChildrenList()) {
			countDeleted = countDeleted + deleteUnknownComponent(ctx, child, deletedTypes);
		}
		if (countDeleted > 0) {
			PersistenceService.getInstance(globalContext).setAskStore(true);
		}
		return "unknown components deleted : " + countDeleted + (deletedTypes.isEmpty() ? "" : " " + deletedTypes);
	}

	@Override
	public boolean isPreview() {
		return false;
	}

}
