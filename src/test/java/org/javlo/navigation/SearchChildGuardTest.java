package org.javlo.navigation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import junit.framework.TestCase;

/**
 * The tree walk of MenuElement.searchChild / searchRealChild uses a "pastNode"
 * collection as a cycle guard. That collection identifies nodes, so it must key
 * on the page id : MenuElement.equals is a deep metadata/content comparison
 * which ignores the id, and using it to identify a node makes
 * contains()/remove() match the wrong page.
 */
public class SearchChildGuardTest extends TestCase {

	private static int idCounter = 0;

	/** build a detached page, without going through the (lock protected) setters. */
	private static MenuElement page(String name, MenuElement... children) {
		MenuElement page = new MenuElement();
		page.setId("page-" + (++idCounter));
		page.name = name;
		if (children.length > 0) {
			page.childMenuElements = new LinkedList<MenuElement>(Arrays.asList(children));
			for (MenuElement child : children) {
				child.setParent(page);
			}
		}
		return page;
	}

	/**
	 * two distinct pages can be "equals" : an empty page is only identified by its
	 * name, its visibility, its roles and its priority.
	 */
	public void testDistinctPagesCanBeEquals() {
		MenuElement first = page("news");
		MenuElement second = page("news");
		assertFalse("the two pages must have distinct ids.", first.getId().equals(second.getId()));
		assertTrue("two empty pages with the same name are equals() : equals can not identify a page.", first.equals(second));
	}

	/** the guard must not confuse two equals() but distinct pages. */
	public void testGuardKeysOnIdNotOnEquals() {
		MenuElement first = page("news");
		MenuElement second = page("news");

		Set<String> guard = new HashSet<String>();
		assertTrue("first page not visited yet.", guard.add(first.getId()));
		assertTrue("second page is a distinct page, it must not be seen as visited.", guard.add(second.getId()));
		assertFalse("first page is already visited.", guard.add(first.getId()));
	}

	/** a page must still be found when the tree holds equals() pages. */
	public void testSearchRealChildFindsPageInTreeWithDuplicatedNames() throws Exception {
		MenuElement target = page("leaf");
		MenuElement root = page("",
				page("a", page("dup", page("leaf"))),
				page("b", page("dup", target)));

		assertEquals("/b/dup/leaf", target.getPath());
		assertSame(target, MenuElement.searchRealChild(root, null, "/b/dup/leaf", new HashSet<String>()));
	}

	/** the guard must stop a cycle : a page reachable twice is walked once. */
	public void testGuardStopsCycle() throws Exception {
		MenuElement loop = page("loop");
		MenuElement root = page("", loop);
		loop.childMenuElements = new LinkedList<MenuElement>(Arrays.asList(loop));

		assertNull("unknown path must return null and not loop forever.",
				MenuElement.searchRealChild(root, null, "/no-such-page", new HashSet<String>()));
	}

}
