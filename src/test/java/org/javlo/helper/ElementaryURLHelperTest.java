package org.javlo.helper;

import junit.framework.TestCase;

public class ElementaryURLHelperTest extends TestCase {
	
	public void testIsImage() throws Exception {
		  assertEquals(ElementaryURLHelper.mergePath("path1", "path2"), "path1/path2");
		  assertEquals(ElementaryURLHelper.mergePath("path1", "/path2"), "path1/path2");
		  assertEquals(ElementaryURLHelper.mergePath("path1/", "/path2"), "path1/path2");		  
	  }
}
