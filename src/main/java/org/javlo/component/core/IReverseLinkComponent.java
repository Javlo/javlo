package org.javlo.component.core;

import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;

public interface IReverseLinkComponent {
	
	/**
	 * get the page of the component. (same method define in IVisualContentComponent.
	 * @return
	 */
	public MenuElement getPage();

	/**
	 * this text must be replace with a link.
	 * @return
	 */
	public String getLinkText(ContentContext ctx);

	/**
	 * return the link code (in HTML) with the link text in.
	 * @return
	 */
	public String getLinkURL(ContentContext ctx);

	/**
	 * return true if the component must be use as reverselink.
	 * @return
	 */
	public boolean isReverseLink();
	
	/**
	 * only modify the first occurrence on the page
	 */
	public boolean isOnlyFirstOccurrence();

	/**
	 * only if reverse link change text only on the same page than the component.
	 * @return
	 */
	public boolean isOnlyThisPage();
}
