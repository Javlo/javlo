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
			int outComp = (int)Math.round((elem1.getPageRank(ctx)-elem2.getPageRank(ctx))*100)*multiply;
			if (outComp != 0) {// no equality
				return outComp;
			} else {
				return 1;
			}
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return 0;
	}
	
	

}
