package org.javlo.macro;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;

public class DeleteSameComponent extends AbstractMacro {

	@Override
	public String getName() {
		return "delete-same-component";
	}

	protected int deleteComponentInBadArea(ContentContext ctx, MenuElement page) throws Exception {

		Set<String> allValue = new HashSet<String>();
		List<String> tobeDeleted = new LinkedList<String>();

		ComponentBean[] beans = page.getContent();
		for (ComponentBean comp : beans) {
			if (allValue.contains(comp.getValue())) {
				tobeDeleted.add(comp.getId());
			} else {
				String value = comp.getValue().trim();
				if (value.length() > 0) {
					allValue.add(value);
				}
			}
		}

		for (String compId : tobeDeleted) {
			ctx.getCurrentPage().removeContent(ctx, compId);
		}

		return tobeDeleted.size();
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		int countDeleted = deleteComponentInBadArea(ctx, ctx.getCurrentPage());
		return "components deleted : " + countDeleted;
	}

}
