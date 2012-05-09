/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;

import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;


/**
 * compare two element of the menu in Content Date if exist and on modification date else.
 * 
 * @author pvanderm
 *
 * this class is used for sort a array of array 
 */
public class MenuElementGlobalDateComparator implements Comparator<MenuElement> {
	
	private int multiply = 1;
	private ContentContext ctx;
	
	public MenuElementGlobalDateComparator ( ContentContext ctx, boolean ascending ) {		
		if (!ascending) {
			multiply = -1;
		}
		this.ctx = ctx;
	}

	/**
	 * compare two array of Comparable
	 */
	public int compare(MenuElement elem1, MenuElement elem2) {
		Calendar cal1 = GregorianCalendar.getInstance();
		Calendar cal2 = GregorianCalendar.getInstance();
		try {
			if (elem1.getContentDate(ctx) == null) {
				cal1.setTime(elem1.getModificationDate());	
			} else {
				cal1.setTime(elem1.getContentDate(ctx));
			}
			
			if (elem2.getContentDate(ctx) == null) {
				cal2.setTime(elem2.getModificationDate());
			} else {
				cal2.setTime(elem2.getContentDate(ctx));
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			if (elem1.getPageRank(ctx) == elem2.getPageRank(ctx)) {
				return cal2.compareTo(cal1)*multiply;	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (elem1.getPageRank(ctx) > elem2.getPageRank(ctx)) {
				return -1;
			} else {
				return 1;
			}
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return 0;
	}

}
