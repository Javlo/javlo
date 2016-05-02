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
				return page1.getModificationDate().compareTo(page2.getModificationDate());
			} else {
				return -page1.getModificationDate().compareTo(page2.getModificationDate());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

}
