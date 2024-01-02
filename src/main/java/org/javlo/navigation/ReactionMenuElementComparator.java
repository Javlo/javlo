package org.javlo.navigation;

import org.javlo.context.ContentContext;

import java.util.Comparator;

public class ReactionMenuElementComparator implements Comparator<MenuElement> {

	private boolean reverse = false;

	private boolean seoOrdre = false;
	private ContentContext ctx = null;

	public ReactionMenuElementComparator(ContentContext ctx, boolean reverse, boolean seoOrder) {
		this.reverse = reverse;
		this.ctx = ctx;
		this.seoOrdre = seoOrder;
	}

	@Override
	public int compare(MenuElement o1, MenuElement o2) {
		if (seoOrdre && o1.getSeoWeight() != o2.getSeoWeight()) {
			return o2.getSeoWeight() - o1.getSeoWeight();
		}
		try {
			if (o1.getToTheTopLevel(ctx) != o2.getToTheTopLevel(ctx)) {
				return o2.getToTheTopLevel(ctx)-o1.getToTheTopLevel(ctx);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
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
