/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;

import org.javlo.component.links.SmartPageBean;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;

/**
 * compare two element of the menu in Content Date if exist and on modification date else.
 * 
 * @author pvanderm
 * 
 *         this class is used for sort a array of array
 */
public class SmartPageBeanGlobalDateComparator implements Comparator<SmartPageBean> {

	private int multiply = 1;
	private final ContentContext ctx;
	private boolean autoSwitchToDefaultLanguage = false;

	public SmartPageBeanGlobalDateComparator(ContentContext ctx, boolean ascending) {
		if (!ascending) {
			multiply = -1;
		}
		this.ctx = ctx;
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		autoSwitchToDefaultLanguage = globalContext.isAutoSwitchToDefaultLanguage();
	}

	/**
	 * compare two array of Comparable
	 */
	@Override
	public int compare(SmartPageBean elem1, SmartPageBean elem2) {
		
		try {
			if (elem1.getToTheTopLevel() != elem2.getToTheTopLevel()) {				
				return elem2.getToTheTopLevel()-elem1.getToTheTopLevel();
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
				if (!elem1.isRealContent()) {
					ctxPage1 = ctx.getContextWithContentNeverNull(elem1.getPage());
				}				
				if (!elem2.isRealContent()) {
					ctxPage2 = ctx.getContextWithContentNeverNull(elem2.getPage());
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				return 0;
			}
		}
		try {
			if (elem1.getPage().getContentDate(ctxPage1) == null) {
				cal1.setTime(elem1.getPage().getModificationDate());
			} else {
				cal1.setTime(elem1.getPage().getContentDate(ctxPage1));
			}

			if (elem2.getPage().getContentDate(ctxPage2) == null) {
				cal2.setTime(elem2.getPage().getModificationDate());
			} else {
				cal2.setTime(elem2.getPage().getContentDate(ctxPage2));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			if (elem1.getPage().getPageRank(ctxPage1) == elem2.getPage().getPageRank(ctxPage2)) {
				return cal2.compareTo(cal1) * multiply;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (elem1.getPage().getPageRank(ctxPage1) > elem2.getPage().getPageRank(ctxPage2)) {
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

