/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;

/**
 * compare two element of the menu in Content Date if exist and on modification date else.
 * 
 * @author pvanderm
 * 
 *         this class is used for sort a array of array
 */
public class MenuElementGlobalDateComparator implements Comparator<MenuElement> {

	private boolean seoOrder = false;
	private int multiply = 1;
	private final ContentContext ctx;
	private boolean autoSwitchToDefaultLanguage = false;

	public MenuElementGlobalDateComparator(ContentContext ctx, boolean ascending, boolean seoOrder) {
		if (!ascending) {
			multiply = -1;
		}
		this.ctx = ctx;
		this.seoOrder = seoOrder;
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		autoSwitchToDefaultLanguage = globalContext.isAutoSwitchToDefaultLanguage();
	}

	/**
	 * compare two array of Comparable
	 */
	@Override
	public int compare(MenuElement elem1, MenuElement elem2) {

		if (seoOrder && elem1.getSeoWeight() != elem2.getSeoWeight()) {
			return elem2.getSeoWeight() - elem1.getSeoWeight();
		}
		
		try {
			if (elem1.getToTheTopLevel(ctx) != elem2.getToTheTopLevel(ctx)) {				
				return elem2.getToTheTopLevel(ctx)-elem1.getToTheTopLevel(ctx);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		Calendar cal1 = GregorianCalendar.getInstance();
		Calendar cal2 = GregorianCalendar.getInstance();
		ContentContext ctxPage1 = ctx;
		ContentContext ctxPage2 = ctx;
		if (autoSwitchToDefaultLanguage) {
			try {				
				if (!elem1.isRealContent(ctx)) {
					ctxPage1 = ctx.getContextWithContentNeverNull(elem1);
				}				
				if (!elem2.isRealContent(ctx)) {
					ctxPage2 = ctx.getContextWithContentNeverNull(elem2);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				return 0;
			}
		}
		try {
			if (elem1.getContentDate(ctxPage1) == null) {
				cal1.setTime(elem1.getModificationDate());
			} else {
				cal1.setTime(elem1.getContentDate(ctxPage1));
			}

			if (elem2.getContentDate(ctxPage2) == null) {
				cal2.setTime(elem2.getModificationDate());
			} else {
				cal2.setTime(elem2.getContentDate(ctxPage2));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			if (elem1.getPageRank(ctxPage1) == elem2.getPageRank(ctxPage2)) {
				return cal2.compareTo(cal1) * multiply;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (elem1.getPageRank(ctxPage1) > elem2.getPageRank(ctxPage2)) {
				return -1;
			} else {
				return 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
}

