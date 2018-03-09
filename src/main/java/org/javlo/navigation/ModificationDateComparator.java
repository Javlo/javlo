package org.javlo.navigation;

import java.util.Comparator;

public class ModificationDateComparator implements Comparator<PageBean> {

	Boolean ascendent;

	public ModificationDateComparator(boolean ascendent) {		
		this.ascendent = ascendent;
	}

	@Override
	public int compare(PageBean page1, PageBean page2) {
		try {
			if (ascendent) {
				return page1.getModificationDateSource().compareTo(page2.getModificationDateSource());
			} else {
				return -page1.getModificationDateSource().compareTo(page2.getModificationDateSource());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

}
