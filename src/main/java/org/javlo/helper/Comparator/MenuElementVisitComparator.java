/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;

import java.util.Comparator;

/**
 * @author pvanderm
 * 
 *         this class is used for sort a array of array
 */
public class MenuElementVisitComparator implements Comparator<MenuElement> {

	private int multiply = 1;
	private ContentContext ctx = null;

	private boolean seoOrder = false;

	public MenuElementVisitComparator(ContentContext inCtx, boolean ascending, boolean seoOrder) {
		if (ascending) {
			multiply = -1;
		}
		ctx = inCtx;
		this.seoOrder = seoOrder;
	}

	public MenuElementVisitComparator(ContentContext inCtx, boolean ascending) {
		if (ascending) {
			multiply = -1;
		}
		ctx = inCtx;
	}

	public static int compareVisit(ContentContext ctx, int multiply, MenuElement elem1, MenuElement elem2) {

		try {
			if (elem1.getToTheTopLevel(ctx) != elem2.getToTheTopLevel(ctx)) {				
				return elem2.getToTheTopLevel(ctx)-elem1.getToTheTopLevel(ctx);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			/*
			 * if (elem1.isRealContent(ctx) && !elem2.isRealContent(ctx)) { return multiply; } else if (!elem1.isRealContent(ctx) && elem2.isRealContent(ctx)) { return -multiply; }
			 */
			int access1 = elem1.getLastAccess(ctx);
			int access2 = elem2.getLastAccess(ctx);
			if (access1 == access2) { // no equality
				return 0;
			}
			return (access1 - access2) * multiply;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public int compare(MenuElement elem1, MenuElement elem2) {
		if (seoOrder && elem1.getSeoWeight() != elem2.getSeoWeight()) {
			return elem2.getSeoWeight() - elem1.getSeoWeight();
		}
		return compareVisit(ctx, multiply, elem1, elem2);
	}

}
