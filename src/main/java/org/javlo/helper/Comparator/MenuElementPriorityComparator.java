/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import org.javlo.navigation.MenuElement;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;


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

	// Build a positional numeric key for sorting
	private static long deepPriority(MenuElement elem) {
		// 1) Choose base > max local priority
		final long BASE = 100L; // ex: si priorité locale ∈ [0..99]

		// 2) Collect priorities from root to elem
		Deque<Integer> stack = new ArrayDeque<>();
		MenuElement cur = elem;
		while (cur != null) {
			stack.push(cur.getPriority());
			cur = cur.getParent();
		}

		// 3) Fold as base-B number
		long key = 0L;
		while (!stack.isEmpty()) {
			key = key * BASE + stack.pop();
		}
		return key; // OK for tri; passer en BigInteger si risque d'overflow
	}


	public int compare(MenuElement elem1, MenuElement elem2) {

		if (seoOrder && elem1.getSeoWeight() != elem2.getSeoWeight()) {
			return elem2.getSeoWeight() - elem1.getSeoWeight();
		}

		try {
			return (int)Math.round(deepPriority(elem1)-deepPriority(elem2))*multiply;
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return 0;
	}
}
