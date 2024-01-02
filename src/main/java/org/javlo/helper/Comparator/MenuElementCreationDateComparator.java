/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import org.javlo.navigation.MenuElement;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;


/**
 * compare two element of the menu in Content Date if exist and on modification date else.
 * 
 * @author pvanderm
 *
 * this class is used for sort a array of array 
 */
public class MenuElementCreationDateComparator implements Comparator<MenuElement> {
	
	private int multiply = 1;

	private boolean seoOrder = false;
	
	public MenuElementCreationDateComparator (boolean ascending, boolean seoOrder ) {
		if (!ascending) {
			multiply = -1;
		}
		this.seoOrder = seoOrder;
	}

	/**
	 * compare two array of Comparable
	 */
	public int compare(MenuElement elem1, MenuElement elem2) {

		if (seoOrder && elem1.getSeoWeight() != elem2.getSeoWeight()) {
			return elem2.getSeoWeight() - elem1.getSeoWeight();
		}

		Calendar cal1 = GregorianCalendar.getInstance();
		Calendar cal2 = GregorianCalendar.getInstance();
		cal1.setTime(elem1.getCreationDate());
		cal2.setTime(elem2.getCreationDate());
		return cal2.compareTo(cal1)*multiply;
	}

}
