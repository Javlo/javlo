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
 * this class is used for sort a array of array 
 */
public class MenuElementPopularityComparator implements Comparator<MenuElement> {
	
	private int multiply = 1;
	private ContentContext ctx = null;

	private boolean seoOrder = false;
	
	public MenuElementPopularityComparator ( ContentContext inCtx, boolean ascending, boolean seoOrder ) {
		if (ascending) {
			multiply = -1;
		}
		ctx = inCtx;
		this.seoOrder = seoOrder;
	}

	public MenuElementPopularityComparator ( ContentContext inCtx, boolean ascending) {
		if (ascending) {
			multiply = -1;
		}
		ctx = inCtx;
	}
	
	public int compare(MenuElement elem1, MenuElement elem2) {

		if (seoOrder && elem1.getSeoWeight() != elem2.getSeoWeight()) {
			return elem2.getSeoWeight() - elem1.getSeoWeight();
		}

		try {
			if (elem1.getToTheTopLevel(ctx) != elem2.getToTheTopLevel(ctx)) {				
				return elem2.getToTheTopLevel(ctx)-elem1.getToTheTopLevel(ctx);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			return (int)Math.round((elem1.getPageRank(ctx)-elem2.getPageRank(ctx))*MenuElement.VOTES_MULTIPLY)*multiply;			
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return 0;
	}
	
	

}
