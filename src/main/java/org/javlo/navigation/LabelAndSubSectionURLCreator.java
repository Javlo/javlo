package org.javlo.navigation;

public class LabelAndSubSectionURLCreator extends LabelAndSectionURLCreator  {

	/**
	 * return the name of the first level page active. "root" if current page in
	 * root.
	 * 
	 * @return
	 * @throws Exception
	 */
	@Override
	protected MenuElement getSectionPage(MenuElement page) {		
		if (page == null) {
			return null;
		}
		if (page.getParent() == null || page.getParent().getParent() == null || page.getParent().getParent().getParent() == null) {
			return null;
		} else {
			while (page.getParent().getParent().getParent() != null) {
				page = page.getParent();
			}
		}
		return page;
	}

}
