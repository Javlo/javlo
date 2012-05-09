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
public class MenuElementTodayComparator implements Comparator<MenuElement> {
	
	
	private ContentContext ctx;
	
	public MenuElementTodayComparator ( ContentContext ctx ) {		
		this.ctx = ctx;
	}

	/**
	 * compare two array of Comparable
	 */
	public int compare(MenuElement elem1, MenuElement elem2) {
		int multiply = 1;
		Calendar cal1 = GregorianCalendar.getInstance();
		Calendar cal2 = GregorianCalendar.getInstance();
		Calendar today = GregorianCalendar.getInstance();
		
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
		
		if (cal1.after(today) && cal2.after(today)) {
			multiply = -multiply;
		}
		
		return cal2.compareTo(cal1)*multiply;
	}

}

