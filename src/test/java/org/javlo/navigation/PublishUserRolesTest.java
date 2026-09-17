package org.javlo.navigation;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import org.javlo.helper.NavigationHelper;

import junit.framework.TestCase;

/**
 * the reader roles of a page must survive the publication : copyElement is used
 * to copy the preview page on the view page.
 */
public class PublishUserRolesTest extends TestCase {

	private static MenuElement createPage(String name) throws Exception {
		MenuElement page = new MenuElement();
		/* the lock is normaly given by the global context */
		Field lock = MenuElement.class.getDeclaredField("lock");
		lock.setAccessible(true);
		lock.set(page, new Object());
		page.setName(name);
		/* avoid any call to the content context when the date is read */
		page.setManualModificationDate(new Date());
		return page;
	}

	public void testCopyKeepOwnRoles() throws Exception {
		MenuElement src = createPage("event");
		src.setUserRoles(new HashSet<String>(Arrays.asList("excom")));
		src.setUserRolesInherited(false);

		MenuElement target = createPage("event");
		NavigationHelper.copyElement(null, src, target);

		assertFalse("the page must keep its own roles after publish", target.isUserRolesInherited());
		assertEquals(new HashSet<String>(Arrays.asList("excom")), target.getUserRoles());
	}

	public void testCopyKeepInheritedRoles() throws Exception {
		MenuElement src = createPage("event");
		src.setUserRoles(new HashSet<String>());
		src.setUserRolesInherited(true);

		MenuElement target = createPage("event");
		target.setUserRolesInherited(false);
		NavigationHelper.copyElement(null, src, target);

		assertTrue("the inherited roles must stay inherited after publish", target.isUserRolesInherited());
	}

}
