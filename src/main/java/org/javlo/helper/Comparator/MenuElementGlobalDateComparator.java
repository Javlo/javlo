/*
 * Created on Nov 11, 2004
 */
package org.javlo.helper.Comparator;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;

import java.util.Comparator;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;

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
	 * sort keys of a page, computed once per page for the duration of the sort
	 * (compare is called n.log(n) times and the keys need content scan).
	 */
	private static final class SortKey {
		int toTheTop = 0;
		long date = 0;
		double pageRank = 0;
	}

	private final Map<MenuElement, SortKey> keys = new IdentityHashMap<MenuElement, SortKey>();

	private SortKey getKey(MenuElement elem) {
		SortKey key = keys.get(elem);
		if (key != null) {
			return key;
		}
		key = new SortKey();
		try {
			key.toTheTop = elem.getToTheTopLevel(ctx);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ContentContext ctxPage = ctx;
		try {
			if (autoSwitchToDefaultLanguage && !elem.isRealContent(ctx)) {
				ctxPage = ctx.getContextWithContentNeverNull(elem);
			}
			Date date = elem.getContentDate(ctxPage);
			if (date == null) {
				date = elem.getModificationDate();
			}
			key.date = date.getTime();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			key.pageRank = elem.getPageRank(ctxPage);
		} catch (Exception e) {
			e.printStackTrace();
		}
		keys.put(elem, key);
		return key;
	}

	/**
	 * compare two array of Comparable
	 */
	@Override
	public int compare(MenuElement elem1, MenuElement elem2) {

		if (seoOrder && elem1.getSeoWeight() != elem2.getSeoWeight()) {
			return elem2.getSeoWeight() - elem1.getSeoWeight();
		}

		SortKey key1 = getKey(elem1);
		SortKey key2 = getKey(elem2);

		if (key1.toTheTop != key2.toTheTop) {
			return key2.toTheTop - key1.toTheTop;
		}

		if (key1.pageRank == key2.pageRank) {
			return Long.compare(key2.date, key1.date) * multiply;
		}
		if (key1.pageRank > key2.pageRank) {
			return -1;
		} else {
			return 1;
		}
	}
}
