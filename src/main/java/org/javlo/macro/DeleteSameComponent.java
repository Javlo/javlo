package org.javlo.macro;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;

public class DeleteSameComponent extends AbstractMacro {

	@Override
	public String getName() {
		return "delete-same-component";
	}

	protected int deleteComponentInBadArea(ContentContext ctx, MenuElement page) throws Exception {

		Set<Integer> allValue = new HashSet<Integer>();
		List<String> tobeDeleted = new LinkedList<String>();

		ContentContext noAreaCtx = ctx.getContextWithArea(null);
		ContentElementList content = page.getContent(noAreaCtx);
		while (content.hasNext(noAreaCtx)) {
			IContentVisualComponent comp = content.next(noAreaCtx);
			int code = comp.getValue(ctx).hashCode();
			if (comp instanceof DynamicComponent) {
				code = ((DynamicComponent) comp).contentHashCode();
			}

			if (allValue.contains(code)) {
				tobeDeleted.add(comp.getId());
			} else {
				allValue.add(code);
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

	@Override
	public boolean isPreview() {
		return true;
	}

}
