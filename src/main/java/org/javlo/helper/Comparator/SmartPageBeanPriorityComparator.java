/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import java.util.Comparator;

import org.javlo.component.links.SmartPageBean;


/**
 * @author pvanderm
 *
 * this class is used for sort a array of array 
 */
public class SmartPageBeanPriorityComparator implements Comparator<SmartPageBean> {
	
	private int multiply = 1;	
	
	public SmartPageBeanPriorityComparator ( boolean ascending ) {		
		if (ascending) {
			multiply = -1;
		}		
	}
	
	public int compare(SmartPageBean elem1, SmartPageBean elem2) {
		
		try {
			return (int)Math.round((elem1.getPriority()-elem2.getPriority())*100)*multiply;
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return 0;
	}
}
