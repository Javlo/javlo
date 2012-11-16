/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;

import org.javlo.navigation.MenuElement;


/**
 * @author pvanderm
 *
 * this class is used for sort a array of array 
 */
public class MenuElementModificationDateComparator implements Comparator<MenuElement> {
	
	private int multiply = 1;
	
	public MenuElementModificationDateComparator ( boolean ascending ) {		
		if (!ascending) {
			multiply = -1;
		}
	}

	/**
	 * compare two array of Comparable
	 */
	public int compare(MenuElement elem1, MenuElement elem2) {
		Calendar cal1 = GregorianCalendar.getInstance();
		cal1.setTime(elem1.getModificationDate());
		Calendar cal2 = GregorianCalendar.getInstance();
		cal2.setTime(elem2.getModificationDate());
		return cal2.compareTo(cal1)*multiply;
	}
	
	

}
