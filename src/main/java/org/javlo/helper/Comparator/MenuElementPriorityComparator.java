/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import java.util.Comparator;

import org.javlo.navigation.MenuElement;


/**
 * @author pvanderm
 *
 * this class is used for sort a array of array 
 */
public class MenuElementPriorityComparator implements Comparator<MenuElement> {
	
	private int multiply = 1;	
	
	public MenuElementPriorityComparator ( boolean ascending ) {		
		if (ascending) {
			multiply = -1;
		}		
	}
	
	public int compare(MenuElement elem1, MenuElement elem2) {
		try {
			return (int)Math.round((elem1.getPriority()-elem2.getPriority())*100)*multiply;
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return 0;
	}
}
