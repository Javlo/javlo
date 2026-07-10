package org.javlo.macro;

import org.javlo.component.container.Box;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;

import java.util.*;

public class DeleteSection extends AbstractMacro {

	@Override
	public String getName() {
		return "delete-same-component";
	}

	protected int deleteSection(ContentContext ctx, MenuElement page) throws Exception {

		Set<Integer> allValue = new HashSet<Integer>();
		List<String> tobeDeleted = new LinkedList<String>();

		ContentContext noAreaCtx = ctx.getContextWithArea(null);
		ContentElementList content = page.getContent(noAreaCtx);
		while (content.hasNext(noAreaCtx)) {
			IContentVisualComponent comp = content.next(noAreaCtx);
			if (comp instanceof Box) {
				tobeDeleted.add(comp.getId());
			}
		}

		for (String compId : tobeDeleted) {
			ctx.getCurrentPage().removeContent(ctx, compId);
		}

		return tobeDeleted.size();
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		int countDeleted = deleteSection(ctx, ctx.getCurrentPage());
		return "components deleted : " + countDeleted;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

}
