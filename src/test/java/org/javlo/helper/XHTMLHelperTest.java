package org.javlo.helper;

import junit.framework.TestCase;

public class XHTMLHelperTest extends TestCase {

	public void testContainsLink() throws Exception {
		assertTrue(XHTMLHelper.containsLink("go on : http://www.javlo.be."));
		assertTrue(XHTMLHelper.containsLink("go on : www.javlo.be."));
		assertFalse(XHTMLHelper.containsLink("sample test."));
	}

}
