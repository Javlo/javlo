package org.javlo.navigation;

import java.util.Comparator;

import org.javlo.context.ContentContext;

public class ReactionMenuElementComparator implements Comparator<MenuElement> {

	private boolean reverse = false;
	private ContentContext ctx = null;

	public ReactionMenuElementComparator(ContentContext ctx, boolean reverse) {
		this.reverse = reverse;
		this.ctx = ctx;
	}

	@Override
	public int compare(MenuElement o1, MenuElement o2) {
		try {
			if (!reverse) {
				return o1.getReactionSize(ctx) - o2.getReactionSize(ctx);
			} else {
				return o2.getReactionSize(ctx) - o1.getReactionSize(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}

}
