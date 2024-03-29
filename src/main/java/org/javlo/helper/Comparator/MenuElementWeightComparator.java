/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;

import java.util.Comparator;

/**
 * @author pvanderm
 *
 * this class is used sort pages on the weight
 */
public class MenuElementWeightComparator implements Comparator<MenuElement> {

	private int multiply = 1;

	private boolean seoOrder = false;
	private ContentContext ctx = null;

	public MenuElementWeightComparator(ContentContext inCtx, boolean ascending, boolean seoOrder ) {
		if (ascending) {
			multiply = -1;
		}
		ctx = inCtx;
		this.seoOrder = seoOrder;
	}


	public int compare(MenuElement elem1, MenuElement elem2) {

		if (seoOrder && elem1.getSeoWeight() != elem2.getSeoWeight()) {
			return elem2.getSeoWeight() - elem1.getSeoWeight();
		}

		try {
			if (elem1.getWeight(ctx) != elem2.getWeight(ctx)) {
				return (int)((Math.round(elem2.getWeight(ctx)*10)-Math.round(elem1.getWeight(ctx)*10))*multiply);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return 0;
	}
}