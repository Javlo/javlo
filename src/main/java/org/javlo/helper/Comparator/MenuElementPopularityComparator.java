/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import java.util.Comparator;

import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;


/**
 * @author pvanderm
 *
 * this class is used for sort a array of array 
 */
public class MenuElementPopularityComparator implements Comparator<MenuElement> {
	
	private int multiply = 1;
	private ContentContext ctx = null;
	
	public MenuElementPopularityComparator ( ContentContext inCtx, boolean ascending ) {		
		if (ascending) {
			multiply = -1;
		}
		ctx = inCtx;
	}

	
	public int compare(MenuElement elem1, MenuElement elem2) {
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
