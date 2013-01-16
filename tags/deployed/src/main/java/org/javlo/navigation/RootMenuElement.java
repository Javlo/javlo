/*
 * Created on 15-f?vr.-2004
 */
package org.javlo.navigation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.javlo.context.GlobalContext;
import org.javlo.helper.DebugHelper;

/**
 * @author pvandermaesen the root element of the menu.
 */
public class RootMenuElement extends MenuElement {

	public static RootMenuElement getInstance(GlobalContext globalContext) {
		RootMenuElement elem = new RootMenuElement();
		elem.cache = globalContext.getCache(MenuElement.class.getName());
		elem.cache.removeAll();
		return elem;
	}

	Map elems = new HashMap();

	Map noParentElems = new HashMap();

	protected RootMenuElement() {
		setId("0");
		setName("root");
	}

	public void attachElement(MenuElement elem, String parentId) {
		if (parentId == null) {
			addChildMenuElement(elem);
		} else {
			MenuElement parentElem = (MenuElement) elems.get(parentId);
			if (parentElem == null) {
				noParentElems.put(elem, parentId);
			} else {
				parentElem.addChildMenuElement(elem);
			}
		}
		elems.put(elem.getId(), elem);
	}

	/**
	 * attach element without parent at the first pass.
	 * 
	 */
	public void findParents() {
		for (Iterator iter = noParentElems.keySet().iterator(); iter.hasNext();) {
			MenuElement elem = (MenuElement) iter.next();
			String parentId = (String) noParentElems.get(elem);
			MenuElement parentElem = (MenuElement) elems.get(parentId);
			DebugHelper.checkAssert(parentElem == null, "no parent found for elem : " + elem.getName());
			parentElem.addChildMenuElement(elem);
		}
	}

	public void removeElement(String id) {
		elems.remove(id);
	}

}
