package org.javlo.navigation;

import java.util.Comparator;

import org.javlo.context.ContentContext;

public class ContentDateComparator implements Comparator<MenuElement> {

	ContentContext ctx;
	Boolean ascendent;

	public ContentDateComparator(ContentContext ctx, boolean ascendent) {
		this.ctx = ctx;
		this.ascendent = ascendent;
	}

	@Override
	public int compare(MenuElement page1, MenuElement page2) {
		try {
			if (ascendent) {
				return page1.getContentDateNeverNull(ctx).compareTo(page2.getContentDateNeverNull(ctx));

			} else {
				return -page1.getContentDateNeverNull(ctx).compareTo(page2.getContentDateNeverNull(ctx));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

}
