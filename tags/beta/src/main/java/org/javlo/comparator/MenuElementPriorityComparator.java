/*
 * Created on 28-f�vr.-2004
 */
package org.javlo.comparator;

import java.util.Comparator;

import org.javlo.navigation.MenuElement;


/**
 * @author pvandermaesen
 * for comparaison of the priority of two menu element.
 */
public class MenuElementPriorityComparator implements Comparator<MenuElement> {

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(MenuElement elem1, MenuElement elem2) {
		int p1 = elem1.getPriority();
		int p2 = elem2.getPriority();
		int comp = p1-p2;
		if (comp==0) { // can never equal
			comp=1;
		}
		return comp;
	}
}
