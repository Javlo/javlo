/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;

import org.javlo.component.links.SmartPageBean;


/**
 * compare two element of the menu in Content Date if exist and on modification date else.
 * 
 * @author pvanderm
 *
 * this class is used for sort a array of array 
 */
public class SmartPageBeanCreationDateComparator implements Comparator<SmartPageBean> {
	
	private int multiply = 1;	
	
	public SmartPageBeanCreationDateComparator (boolean ascending ) {		
		if (!ascending) {
			multiply = -1;
		}		
	}

	/**
	 * compare two array of Comparable
	 */
	public int compare(SmartPageBean elem1, SmartPageBean elem2) {
		
		Calendar cal1 = GregorianCalendar.getInstance();
		Calendar cal2 = GregorianCalendar.getInstance();
		cal1.setTime(elem1.getPage().getCreationDate());
		cal2.setTime(elem2.getPage().getCreationDate());
		if ( cal2.compareTo(cal1) == 0 ) {
			return 1;
		}
		return cal2.compareTo(cal1)*multiply;
	}

}
