/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import java.util.Comparator;

import org.javlo.component.links.SmartPageBean;
import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;


/**
 * @author pvanderm
 *
 * this class is used for sort a array of array 
 */
public class SmartPageBeanPopularityComparator implements Comparator<SmartPageBean> {
	
	private int multiply = 1;
	private ContentContext ctx = null;
	
	public SmartPageBeanPopularityComparator ( ContentContext inCtx, boolean ascending ) {		
		if (ascending) {
			multiply = -1;
		}
		ctx = inCtx;
	}

	
	public int compare(SmartPageBean elem1, SmartPageBean elem2) {
		try {
			if (elem1.getToTheTopLevel() != elem2.getToTheTopLevel()) {				
				return elem2.getToTheTopLevel()-elem1.getToTheTopLevel();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			return (int)Math.round((elem1.getPage().getPageRank(ctx)-elem2.getPage().getPageRank(ctx))*MenuElement.VOTES_MULTIPLY)*multiply;			
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return 0;
	}
	
	

}
