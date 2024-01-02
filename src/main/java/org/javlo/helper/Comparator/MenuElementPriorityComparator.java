/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import org.javlo.navigation.MenuElement;

import java.util.Comparator;


/**
 * @author pvanderm
 *
 * this class is used for sort a array of array 
 */
public class MenuElementPriorityComparator implements Comparator<MenuElement> {
	
	private int multiply = 1;

	private boolean seoOrder = false;
	
	public MenuElementPriorityComparator ( boolean ascending, boolean seoOrder ) {
		if (ascending) {
			multiply = -1;
		}
		this.seoOrder = seoOrder;
	}
	
	public int compare(MenuElement elem1, MenuElement elem2) {

		if (seoOrder && elem1.getSeoWeight() != elem2.getSeoWeight()) {
			return elem2.getSeoWeight() - elem1.getSeoWeight();
		}

		try {
			return (int)Math.round((elem1.getPriority()-elem2.getPriority())*100)*multiply;
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return 0;
	}
}
