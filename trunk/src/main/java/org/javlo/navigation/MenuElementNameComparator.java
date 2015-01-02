package org.javlo.navigation;

import java.util.Comparator;

import org.javlo.context.ContentContext;

public class MenuElementNameComparator implements Comparator<MenuElement> {

	ContentContext ctx;
	Boolean ascendent;

	public MenuElementNameComparator(ContentContext ctx, boolean ascendent) {
		this.ctx = ctx;
		this.ascendent = ascendent;
	}

	@Override
	public int compare(MenuElement page1, MenuElement page2) {
		try {
			if (ascendent) {
				return page1.getName().compareTo(page2.getName());
			} else {
				return -page1.getName().compareTo(page2.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

}
