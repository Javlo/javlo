/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import java.util.Comparator;

import org.javlo.component.links.SmartPageBean;
import org.javlo.context.ContentContext;

/**
 * @author pvanderm
 * 
 *         this class is used for sort a array of array
 */
public class SmartPageBeanVisitComparator implements Comparator<SmartPageBean> {

	private int multiply = 1;
	private ContentContext ctx = null;

	public SmartPageBeanVisitComparator(ContentContext inCtx, boolean ascending) {
		if (ascending) {
			multiply = -1;
		}
		ctx = inCtx;
	}

	public static int compareVisit(ContentContext ctx, int multiply, SmartPageBean elem1, SmartPageBean elem2) {
		
		try {
			if (elem1.getToTheTopLevel() != elem2.getToTheTopLevel()) {				
				return elem2.getToTheTopLevel()-elem1.getToTheTopLevel();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			/*
			 * if (elem1.isRealContent(ctx) && !elem2.isRealContent(ctx)) { return multiply; } else if (!elem1.isRealContent(ctx) && elem2.isRealContent(ctx)) { return -multiply; }
			 */
			int access1 = elem1.getPage().getLastAccess(ctx);
			int access2 = elem2.getPage().getLastAccess(ctx);
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
	public int compare(SmartPageBean elem1, SmartPageBean elem2) {
		return compareVisit(ctx, multiply, elem1, elem2);
	}

}
