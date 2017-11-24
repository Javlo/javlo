/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;

import org.javlo.component.links.SmartPageBean;
import org.javlo.context.ContentContext;


/**
 * compare two element of the menu in Content Date if exist and on modification date else.
 * 
 * @author pvanderm
 *
 * this class is used for sort a array of array 
 */
public class SmartPageBaanTodayComparator implements Comparator<SmartPageBean> {
	
	
	private ContentContext ctx;
	
	public SmartPageBaanTodayComparator ( ContentContext ctx ) {		
		this.ctx = ctx;
	}

	/**
	 * compare two array of Comparable
	 */
	public int compare(SmartPageBean elem1, SmartPageBean elem2) {
		int multiply = 1;
		Calendar cal1 = GregorianCalendar.getInstance();
		Calendar cal2 = GregorianCalendar.getInstance();
		Calendar today = GregorianCalendar.getInstance();
		
		try {
			if (elem1.getPage().getContentDate(ctx) == null) {
				cal1.setTime(elem1.getPage().getModificationDate());	
			} else {
				cal1.setTime(elem1.getPage().getContentDate(ctx));
			}
			
			if (elem2.getPage().getContentDate(ctx) == null) {
				cal2.setTime(elem2.getPage().getModificationDate());
			} else {
				cal2.setTime(elem2.getPage().getContentDate(ctx));
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

